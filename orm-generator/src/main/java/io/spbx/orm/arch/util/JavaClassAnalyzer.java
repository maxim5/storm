package io.spbx.orm.arch.util;

import io.spbx.orm.arch.model.ModelField;
import io.spbx.util.base.annotate.Stateless;
import io.spbx.util.collect.stream.BasicCollectors;
import io.spbx.util.collect.stream.Streamer;
import io.spbx.util.extern.asm.AsmClassScanner;
import io.spbx.util.extern.asm.AsmClassScanner.Access;
import io.spbx.util.extern.asm.AsmClassScanner.MethodData;
import io.spbx.util.logging.Logger;
import io.spbx.util.reflect.BasicClasses;
import io.spbx.util.reflect.BasicMembers;
import io.spbx.util.reflect.BasicMembers.Methods;
import io.spbx.util.reflect.BasicMembers.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.spbx.orm.arch.InvalidSqlModelException.failIf;
import static io.spbx.orm.arch.InvalidSqlModelException.newInvalidSqlModelException;
import static io.spbx.util.base.error.BasicExceptions.newInternalError;
import static io.spbx.util.base.lang.BasicNulls.firstNonNullIfExists;
import static io.spbx.util.reflect.BasicMembers.isPrivate;

@Stateless
public class JavaClassAnalyzer {
    private static final Logger log = Logger.forEnclosingClass();

    public static @NotNull List<JavaField> getAllFieldsOrdered(@NotNull Class<?> klass) {
        return getAllFieldsOrderedRecursive(klass).stream().map(field -> new JavaField(field, klass)).toList();
    }

    public static @NotNull ModelField toModelField(@NotNull JavaField field) {
        return new ModelField(field, findAccessorOrDie(field).value(), Naming.fieldSqlName(field));
    }

    @VisibleForTesting
    static @NotNull List<Field> getAllFieldsOrderedRecursive(@NotNull Class<?> klass) {
        failIf(klass.isAnonymousClass(), "Model class `%s` is anonymous", klass);
        failIf(BasicClasses.isInnerNested(klass),
               "Model class `%s` is inner nested in `%s`. Migrate to static model class",
               klass, BasicClasses.getNestHost(klass));

        if (klass.isRecord()) {
            Map<String, Field> fields = Streamer.of(ownFields(klass)).toMapBy(Field::getName);
            return Stream.of(klass.getRecordComponents()).map(RecordComponent::getName).map(fields::get).toList();
        }

        Class<?> superclass = klass.getSuperclass();
        if (superclass == null) {
            return List.of();
        }

        List<Field> allFields = new ArrayList<>();
        if (superclass != Object.class) {
            allFields.addAll(getAllFieldsOrderedRecursive(superclass));
        }
        allFields.addAll(ownFields(klass));

        log.debug().log("Field matching for the model class: %s", klass.getSimpleName());
        if (matchFieldsToConstructors(allFields, klass)) {
            return allFields;
        }

        throw newInvalidSqlModelException(
            "Model class `%s` does not have a constructor matching the fields: %s. " +
            "Consider adding a JDBC adapter or JDBC mapper to the field using @Sql.Via annotation",
            klass.getSimpleName(), allFields
        );
    }

    @VisibleForTesting
    static @NotNull List<Field> ownFields(@NotNull Class<?> klass) {
        return Streamer.of(klass.getDeclaredFields()).skipIf(BasicMembers::isStatic).toList();
    }

    @VisibleForTesting
    static boolean matchFieldsToConstructors(@NotNull List<Field> fields, @NotNull Class<?> klass) {
        List<Class<?>> fieldTypes = fields.stream().map(Field::getType).collect(Collectors.toList());
        List<String> fieldNames = fields.stream().map(Field::getName).toList();
        log.debug().log("Fields: %s", fields);

        for (Constructor<?> constructor : klass.getDeclaredConstructors()) {
            if (isPrivate(constructor)) {
                continue;
            }
            Parameter[] parameters = constructor.getParameters();
            if (fields.size() != parameters.length) {
                continue;
            }
            if (fieldTypes.equals(List.of(constructor.getParameterTypes()))) {
                if (Stream.of(parameters).allMatch(Parameter::isNamePresent)) {
                    List<String> paramNames = Stream.of(parameters).map(Parameter::getName).toList();
                    if (fieldNames.equals(paramNames)) {
                        return true;
                    }
                    log.debug().log("Constructor param names don't match the fields: fields=%s params=%s",
                                    fieldNames, paramNames);
                } else {
                    log.warn().log(
                        "Parameter names aren't available in the compiled `%s` class. " +
                        "Field matching can be incorrect. Generated code needs manual review.",
                        klass.getSimpleName()
                    );
                    return true;
                }
            }
        }

        return false;
    }

    @VisibleForTesting
    static @NotNull Accessor findAccessorOrDie(@NotNull JavaField field) {
        if (field.isPackageVisible()) {
            return Accessor.ofJavaField(field);
        }

        JavaMethod getter = findGetterMethodOrNull(field);
        if (getter != null) {
            assert field.getType() == getter.getReturnType() :
                newInternalError("Incompatible field and getter types: `%s` vs `%s`", field, getter);
            assert field.ownerClass() == getter.ownerClass() :
                newInternalError("Incompatible field and getter container classes: `%s` vs `%s`", field, getter);
            return Accessor.ofJavaMethod(getter);
        }

        String getterName = findGetterMethodNameOrNull(field);
        if (getterName != null) {
            return Accessor.ofJavaMethodByName(getterName);
        }

        throw newInvalidSqlModelException("Model class `%s` exposes no visible getter for field `%s`",
                                          field.ownerClass().getSimpleName(), field.getName());
    }

    @VisibleForTesting
    static @Nullable JavaMethod findGetterMethodOrNull(@NotNull JavaField field) {
        Class<?> ownerClass = field.ownerClass();
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();

        Map<String, JavaMethod> eligibleMethods = getClassMethodsBestEffort(ownerClass, field.isOwn())
            .skipIf(BasicMembers::isStatic)
            .map(method -> new JavaMethod(method, ownerClass))
            .filter(method -> method.getReturnType() == fieldType &&
                              method.getParameterCount() == 0 &&
                              method.getExceptionTypes().length == 0 &&
                              method.isPackageVisible())
            .maps(JavaMethod::getName, Function.identity())
            .toMapIgnoreDuplicateKeys();  // Likely duplicate method names: `hashCode()` and `toString()`

        return firstNonNullIfExists(
            List.of(
                () -> eligibleMethods.get(fieldName),
                () -> eligibleMethods.get(getterName(fieldType, fieldName)),
                () -> eligibleMethods.values().stream().filter(method -> {
                    String name = method.getName().toLowerCase();
                    return name.startsWith("get") && name.contains(fieldName.toLowerCase());
                }).collect(BasicCollectors.toOnlyNonNullOrEmpty()).orElse(null)
            )
        );
    }

    @VisibleForTesting
    static @NotNull Streamer<Method> getClassMethodsBestEffort(@NotNull Class<?> ownerClass, boolean ownMethodsOnly) {
        try {
            Scope scope = ownMethodsOnly ? Scope.DECLARED : Scope.HIERARCHY_ALL;
            Stream<Method> methods = Methods.of(ownerClass).list(scope);
            return Streamer.of(methods);
        } catch (NoClassDefFoundError ignore) {}

        try {
            if (!ownMethodsOnly) {  // Try getting just own methods
                Stream<Method> methods = Methods.of(ownerClass).list(Scope.DECLARED);
                return Streamer.of(methods);
            }
        } catch (NoClassDefFoundError ignore) {}

        return Streamer.of();
    }

    @VisibleForTesting
    static @Nullable String findGetterMethodNameOrNull(@NotNull JavaField field) {
        String fieldName = field.getName();
        String getterName = getterName(field.getType(), fieldName);
        String packageName = field.ownerClass().getPackageName();

        String className = field.ownerClass().getName();
        try {
            while (className != null) {
                String superPackageName = AsmClassScanner.packageName(className);
                AsmClassScanner scanner = AsmClassScanner.of(field.ownerClass().getClassLoader(), className);   // !!!
                List<MethodData> methods = scanner.methods((access, name, descriptor, signature, exceptions) -> {
                    boolean isStatic = Access.isStatic(access);
                    boolean isVisible = Access.isPublic(access) ||
                                        ((Access.isProtected(access) || Access.isPackageLocal(access)) &&
                                         packageName.equals(superPackageName));
                    boolean nameMatches = fieldName.equals(name) || getterName.equals(name);
                    boolean hasNoParams = descriptor.contains("()");  // FIX[debt]: check return value as well
                    boolean hasNoExceptions = exceptions == null;
                    return !isStatic && isVisible && nameMatches && hasNoParams && hasNoExceptions;
                });
                if (methods.size() == 1) {
                    return methods.getFirst().name();
                }
                className = scanner.superClass();
            }
        } catch (Throwable e) {
            log.warn().withCause(e).log("ASM class reader for `%s` failed: %s", className, e.getMessage());
        }

        return null;
    }

    @VisibleForTesting
    static @NotNull String getterName(@NotNull Class<?> fieldType, @NotNull String fieldName) {
        return "%s%s".formatted(fieldType == boolean.class ? "is" : "get", Naming.camelLowerToUpper(fieldName));
    }
}
