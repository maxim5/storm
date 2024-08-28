package io.spbx.orm.arch.model;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.arch.InvalidSqlModelException;
import io.spbx.orm.arch.util.Naming;
import io.spbx.util.base.OneOf;
import io.spbx.util.collect.Streamer;
import io.spbx.util.prima.wrap.MutableInt;
import io.spbx.util.reflect.BasicAnnotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import static io.spbx.util.reflect.BasicMembers.*;

public class AdapterApi implements ApiFormatter<AdapterApi.AdapterApiCallFormatter> {
    private static final String CREATE_INSTANCE = "createInstance";
    private static final String FILL_VALUES = "fillArrayValues";
    private static final String NEW_ARRAY = "toNewValuesArray";

    private final @NotNull OneOf<Class<?>, PojoArch> oneOf;
    private final String staticRef;

    private AdapterApi(@NotNull OneOf<Class<?>, PojoArch> oneOf) {
        this.oneOf = oneOf;
        this.staticRef = oneOf.mapToObj(AdapterApi::classToStaticRef, AdapterApi::signatureStaticRef);
    }

    public static @Nullable AdapterApi ofClass(@Nullable Class<?> adapterClass) {
        return adapterClass != null ? new AdapterApi(OneOf.ofFirst(adapterClass)) : null;
    }

    public static @NotNull AdapterApi ofSignature(@NotNull PojoArch pojoArch) {
        return new AdapterApi(OneOf.ofSecond(pojoArch));
    }

    public @NotNull AdapterApiCallFormatter formatter(@NotNull FormatMode mode) {
        return new AdapterApiCallFormatter() {
            @Override
            public @NotNull String fillArrayValues(@NotNull String instance, @NotNull String array, @NotNull Object index) {
                return "%s.fillArrayValues(%s, %s, %s)".formatted(staticRef, instance, array, index) + mode.eol();
            }

            @Override
            public @NotNull String createInstance(@NotNull String params) {
                return "%s.createInstance(%s)".formatted(staticRef, params) + mode.eol();
            }

            @Override
            public @NotNull String toValueObject(@NotNull String param) {
                return "%s.toValueObject(%s)".formatted(staticRef, param) + mode.eol();
            }

            @Override
            public @NotNull String toNewValuesArray(@NotNull String param) {
                return "%s.toNewValuesArray(%s)".formatted(staticRef, param) + mode.eol();
            }
        };
    }

    public static boolean isValidAdapter(@NotNull Class<?> adapterClass) {
        return adapterClass.isAnnotationPresent(JdbcAdapt.class);
    }

    @VisibleForTesting
    public @NotNull String staticRef() {
        return staticRef;
    }

    private static @NotNull String classToStaticRef(@NotNull Class<?> klass) {
        String canonicalName = Naming.shortCanonicalJavaName(klass);

        if (hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(CREATE_INSTANCE)) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(FILL_VALUES)) &&
            hasMethod(klass, Scope.ALL, method -> isPublicStatic(method) && method.getName().equals(NEW_ARRAY))) {
            return canonicalName;
        }

        Field staticField = findPublicStaticInstance(klass);
        if (staticField != null) {
            return "%s.%s".formatted(canonicalName, staticField.getName());
        }

        return "new %s()".formatted(canonicalName);
    }

    private static @NotNull String signatureStaticRef(@NotNull PojoArch pojoArch) {
        return "%s.ADAPTER".formatted(pojoArch.adapterName());
    }

    public @NotNull List<Column> adapterColumns(@NotNull String fieldSqlName) {
        return oneOf.mapToObj(klass -> classToAdapterColumns(klass, fieldSqlName), pojo -> pojoToColumns(pojo, fieldSqlName));
    }

    public int adapterColumnsNumber() {
        return oneOf.mapToInt(klass -> getCreationParameters(klass).length, HasColumns::columnsNumber);
    }

    @VisibleForTesting
    static @NotNull List<Column> classToAdapterColumns(@NotNull Class<?> klass, @NotNull String fieldSqlName) {
        Parameter[] parameters = getCreationParameters(klass);
        if (parameters.length == 1) {
            JdbcType paramType = JdbcType.findByMatchingNativeType(parameters[0].getType());
            InvalidSqlModelException.failIf(paramType == null, "JDBC adapter `%s` has incompatible parameters: %s", CREATE_INSTANCE, klass);
            Column column = Column.of(fieldSqlName, paramType);
            return List.of(column);
        } else {
            Optional<String[]> names = BasicAnnotations.getOptionalAnnotation(klass, JdbcAdapt.class).map(JdbcAdapt::names);
            MutableInt count = new MutableInt();
            return Streamer.of(parameters)
                .zipRight(param -> JdbcType.findByMatchingNativeType(param.getType()))
                .mapKeys(param -> Naming.concatSqlNames(
                    fieldSqlName,
                    names.map(array -> array[count.value++]).orElseGet(() -> Naming.fieldSqlName(param))))
                .mapValues(ColumnType::new)
                .mapToObj(Column::new)
                .toList();
        }
    }

    private static @NotNull List<Column> pojoToColumns(@NotNull PojoArch pojo, @NotNull String fieldName) {
        return pojo.reattachedTo(PojoParent.ofTerminal(fieldName, Naming.fieldSqlName(fieldName))).columns();
    }

    private static @NotNull Parameter[] getCreationParameters(@NotNull Class<?> adapterClass) {
        Method method = findMethod(adapterClass, Scope.DECLARED, CREATE_INSTANCE);
        InvalidSqlModelException.failIf(method == null, "JDBC adapter does not implement `%s` method: %s", CREATE_INSTANCE, adapterClass);
        return method.getParameters();
    }

    public interface AdapterApiCallFormatter {
        @NotNull String fillArrayValues(@NotNull String instance, @NotNull String array, @NotNull Object index);
        @NotNull String createInstance(@NotNull String params);
        @NotNull String toValueObject(@NotNull String param);
        @NotNull String toNewValuesArray(@NotNull String param);
    }
}
