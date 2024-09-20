package io.spbx.orm.arch.model;

import io.spbx.orm.arch.util.Naming;
import io.spbx.util.func.Reversible;
import io.spbx.util.reflect.BasicMembers.Fields;
import io.spbx.util.reflect.BasicMembers.Methods;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.spbx.orm.arch.InvalidSqlModelException.failIf;
import static io.spbx.orm.arch.InvalidSqlModelException.newInvalidSqlModelException;
import static io.spbx.util.reflect.BasicGenerics.getGenericTypeArgumentsOfInterface;
import static io.spbx.util.reflect.BasicGenerics.isWildcardType;
import static io.spbx.util.reflect.BasicMembers.hasName;
import static io.spbx.util.reflect.BasicMembers.hasType;
import static io.spbx.util.reflect.BasicMembers.isPublicStatic;
import static java.util.Objects.requireNonNull;

public class MapperApi implements ApiFormatter<MapperApi.MapperCallFormatter> {
    private final JdbcType jdbcType;
    private final MapperCallFormatter formatter;
    private final Optional<Class<?>> importedClass;

    private MapperApi(@NotNull JdbcType jdbcType, @NotNull MapperCallFormatter formatter, @Nullable Class<?> importedClass) {
        this.jdbcType = jdbcType;
        this.formatter = formatter;
        this.importedClass = Optional.ofNullable(importedClass);
    }

    public static @NotNull MapperApi ofExistingMapper(@NotNull Class<?> mapperClass,
                                                      @NotNull Type fieldType,
                                                      boolean nullable) {
        String staticRef = classToStaticRef(mapperClass);
        MapperClassInference inference = infer(mapperClass, fieldType);
        String forwardCall = (inference.isJdbcFirstArgument() ? "backward" : "forward") + (nullable ? "Nullable" : "");
        String backwardCall = (inference.isJdbcFirstArgument() ? "forward" : "backward") + (nullable ? "Nullable" : "");
        MapperCallFormatter formatter = new MapperCallFormatter() {
            @Override public @NotNull String jdbcToField(@NotNull String jdbcParam) {
                return "%s.%s(%s)".formatted(staticRef, forwardCall, jdbcParam);
            }
            @Override public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return "%s.%s(%s)".formatted(staticRef, backwardCall, fieldParam);
            }
        };
        return new MapperApi(inference.jdbcType(), formatter, mapperClass);
    }

    public static @NotNull MapperApi ofInlineMapper(@NotNull JdbcType jdbcType, @NotNull MapperCallFormatter formatter) {
        return new MapperApi(jdbcType, formatter, null);
    }

    public static boolean isValidMapper(@NotNull Class<?> mapperClass) {
        return Reversible.class.isAssignableFrom(mapperClass);
    }

    private record MapperClassInference(@NotNull JdbcType jdbcType, boolean isJdbcFirstArgument) {}

    private static @NotNull MapperClassInference infer(@NotNull Class<?> mapperClass, @NotNull Type fieldType) {
        Type[] types = getGenericTypeArgumentsOfInterface(mapperClass, Reversible.class);
        failIf(types == null, "Mapper class must implement Reversible<U, V> interface:", mapperClass);
        failIf(types.length != 2, "Mapper class must have exactly two type arguments:", mapperClass);
        failIf(isWildcardType(types[0]) || isWildcardType(types[1]),
               "Mapper class must actualize Reversible<U, V> arguments:", mapperClass);

        Type from = types[0];
        Type to = types[1];
        if (fieldType.equals(from)) {
            JdbcType jdbcType = to instanceof Class<?> klass ? JdbcType.findByMatchingNativeType(klass) : null;
            if (jdbcType != null) {
                return new MapperClassInference(jdbcType, true);
            }
        }
        if (fieldType.equals(to)) {
            JdbcType jdbcType = from instanceof Class<?> klass ? JdbcType.findByMatchingNativeType(klass) : null;
            if (jdbcType != null) {
                return new MapperClassInference(jdbcType, false);
            }
        }

        throw newInvalidSqlModelException("Failed to identify mapper class arguments: mapper=%s, field=%s",
                                          mapperClass, fieldType);
    }

    public @NotNull JdbcType jdbcType() {
        return jdbcType;
    }

    public @NotNull Optional<Class<?>> importedClass() {
        return importedClass;
    }

    public @NotNull Column mapperColumn(@NotNull String fieldSqlName) {
        return Column.of(fieldSqlName, jdbcType);
    }

    @Override
    public @NotNull MapperCallFormatter formatter(@NotNull FormatMode mode) {
        return new MapperCallFormatter() {
            @Override public @NotNull String jdbcToField(@NotNull String jdbcParam) {
                return formatter.jdbcToField(jdbcParam) + mode.eol();
            }
            @Override public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return formatter.fieldToJdbc(fieldParam) + mode.eol();
            }
        };
    }

    private static @NotNull String classToStaticRef(@NotNull Class<?> klass) {
        String canonicalName = Naming.shortCanonicalJavaName(klass);

        if (Methods.of(klass).has(method -> isPublicStatic(method) && hasName(method, "forward")) &&
            Methods.of(klass).has(method -> isPublicStatic(method) && hasName(method, "backward"))) {
            return canonicalName;
        }

        Field fieldInstance = Fields.of(klass).find(field -> isPublicStatic(field) && hasType(field, klass));
        if (fieldInstance != null) {
            return "%s.%s".formatted(canonicalName, fieldInstance.getName());
        }

        if (klass.isAnonymousClass()) {
            Class<?> enclosingClass = requireNonNull(klass.getEnclosingClass());
            fieldInstance = Fields.of(enclosingClass).find(field -> isPublicStatic(field) && hasType(field, klass));
            if (fieldInstance != null) {
                return "%s.%s".formatted(Naming.shortCanonicalJavaName(enclosingClass), fieldInstance.getName());
            }
        }

        return "new %s()".formatted(canonicalName);
    }

    public interface MapperCallFormatter {
        @NotNull String jdbcToField(@NotNull String jdbcParam);
        @NotNull String fieldToJdbc(@NotNull String fieldParam);
    }
}
