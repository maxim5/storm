package io.spbx.orm.arch.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import io.spbx.orm.arch.model.ModelField;
import io.spbx.util.collect.BasicCollectors;
import io.spbx.util.collect.Streamer;
import io.spbx.util.reflect.BasicMembers;
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
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.spbx.orm.arch.InvalidSqlModelException.failIf;
import static io.spbx.orm.arch.InvalidSqlModelException.newInvalidSqlModelException;
import static io.spbx.util.base.BasicNulls.firstNonNullIfExist;
import static io.spbx.util.reflect.BasicMembers.isPrivate;
import static io.spbx.util.reflect.BasicMembers.isStatic;

public class JavaClassAnalyzer {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static @NotNull List<Field> getAllFieldsOrdered(@NotNull Class<?> klass) {
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
            allFields.addAll(getAllFieldsOrdered(superclass));
        }
        allFields.addAll(ownFields(klass));

        log.at(Level.FINER).log("Field matching for the model class: %s", klass.getSimpleName());
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
    static boolean matchFieldsToConstructors(@NotNull List<Field> fields, @NotNull Class<?> klass) {
        List<Class<?>> fieldTypes = fields.stream().map(Field::getType).collect(Collectors.toList());
        List<String> fieldNames = fields.stream().map(Field::getName).toList();
        log.at(Level.FINER).log("Fields: %s", fields);

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
                    log.at(Level.FINE).log("Constructor param names don't match the fields: fields=%s params=%s",
                                           fieldNames, paramNames);
                } else {
                    log.at(Level.WARNING).log(
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
    static @NotNull List<Field> ownFields(@NotNull Class<?> klass) {
        return Stream.of(klass.getDeclaredFields()).filter(Predicate.not(BasicMembers::isStatic)).toList();
    }

    public static @NotNull ModelField toModelField(@NotNull Field field) {
        return new ModelField(
            field.getName(),
            findAccessorOrDie(field).value(),
            Naming.fieldSqlName(field),
            field.getType(),
            field.getDeclaringClass());
    }

    @VisibleForTesting
    static @NotNull Accessor findAccessorOrDie(@NotNull Field field) {
        if (!isPrivate(field)) {
            return Accessor.ofJavaField(field);
        }

        Method getter = findGetterMethodOrDie(field);
        assert field.getType() == getter.getReturnType() :
            "Incompatible field and getter types: `%s` vs `%s`".formatted(field, getter);
        assert field.getDeclaringClass() == getter.getDeclaringClass() :
            "Incompatible field and getter container classes: `%s` vs `%s`".formatted(field, getter);
        return Accessor.ofJavaMethod(getter);
    }

    @VisibleForTesting
    static @NotNull Method findGetterMethodOrDie(@NotNull Field field) {
        Method getter = findGetterMethod(field);
        failIf(getter == null, "Model class `%s` exposes no getter for field `%s`",
               field.getDeclaringClass().getSimpleName(), field.getName());
        return getter;
    }

    @VisibleForTesting
    static @Nullable Method findGetterMethod(@NotNull Field field) {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();

        Map<String, Method> eligibleMethods = Stream.of(field.getDeclaringClass().getMethods())
            .filter(method -> method.getReturnType() == fieldType &&
                              method.getParameterCount() == 0 &&
                              !isPrivate(method) &&
                              !isStatic(method))
            .collect(ImmutableMap.toImmutableMap(Method::getName, Function.identity()));

        return firstNonNullIfExist(
            List.of(
                () -> eligibleMethods.get(fieldName),
                () -> eligibleMethods.get("%s%s".formatted(
                    fieldType == boolean.class ? "is" : "get",
                    Naming.camelLowerToUpper(fieldName))),
                () -> eligibleMethods.values().stream().filter(method -> {
                    String name = method.getName().toLowerCase();
                    return name.startsWith("get") && name.contains(fieldName.toLowerCase());
                }).collect(BasicCollectors.toOnlyNonNullOrEmpty()).orElse(null)
            )
        );
    }
}
