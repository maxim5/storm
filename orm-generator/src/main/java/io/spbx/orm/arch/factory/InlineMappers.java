package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.arch.model.JdbcType;
import io.spbx.orm.arch.model.MapperApi;
import io.spbx.orm.arch.model.MapperApi.MapperCallFormatter;
import io.spbx.orm.arch.util.JavaField;
import io.spbx.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

class InlineMappers {
    private static final ImmutableList<Inliner> SUPPORTED_INLINERS = ImmutableList.of(
        wrap(field -> field.getType().isEnum(), InlineMappers::ofEnum),
        wrap(field -> field.getType() == Optional.class && hasNativeArgument(field), InlineMappers::ofOptional),
        wrap(field -> field.getType() == AtomicReference.class && hasNativeArgument(field), InlineMappers::ofAtomicReference)
    );

    public static @Nullable MapperApi tryInlineMapperIfSupported(@NotNull JavaField field) {
        for (Inliner inliner : SUPPORTED_INLINERS) {
            if (inliner.matches(field)) {
                return inliner.buildInlineMapper(field);
            }
        }
        return null;
    }

    public static @NotNull MapperApi ofEnum(@NotNull JavaField field) {
        assert field.getType().isEnum() : "Field is not an enum: %s".formatted(field);
        MapperCallFormatter formatter = new MapperCallFormatter() {
            @Override public @NotNull String jdbcToField(@NotNull String index) {
                return "%s.values()[%s]".formatted(Naming.shortCanonicalJavaName(field.getType()), index);
            }
            @Override public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return "%s.ordinal()".formatted(fieldParam);
            }
        };
        return MapperApi.ofInlineMapper(JdbcType.Int, formatter);
    }

    public static @NotNull MapperApi ofOptional(@NotNull JavaField field) {
        assert field.getType() == Optional.class : "Field is not an Optional: %s".formatted(field);
        JdbcType jdbcType = getJdbcTypeFromGenericTypeArguments(field);
        MapperCallFormatter formatter = new MapperCallFormatter() {
            @Override public @NotNull String jdbcToField(@NotNull String jdbcParam) {
                return "Optional.ofNullable(%s)".formatted(jdbcParam);
            }
            @Override public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return "%s.orElse(null)".formatted(fieldParam);
            }
        };
        return MapperApi.ofInlineMapper(requireNonNull(jdbcType), formatter);
    }

    public static @NotNull MapperApi ofAtomicReference(@NotNull JavaField field) {
        assert field.getType() == AtomicReference.class : "Field is not an AtomicReference: %s".formatted(field);
        JdbcType jdbcType = getJdbcTypeFromGenericTypeArguments(field);
        MapperCallFormatter formatter = new MapperCallFormatter() {
            @Override public @NotNull String jdbcToField(@NotNull String jdbcParam) {
                return "new AtomicReference<>(%s)".formatted(jdbcParam);
            }
            @Override public @NotNull String fieldToJdbc(@NotNull String fieldParam) {
                return "%s.get()".formatted(fieldParam);
            }
        };
        return MapperApi.ofInlineMapper(requireNonNull(jdbcType), formatter);
    }

    private static boolean hasNativeArgument(@NotNull JavaField field) {
        return getJdbcTypeFromGenericTypeArguments(field) != null;
    }

    private static @Nullable JdbcType getJdbcTypeFromGenericTypeArguments(@NotNull JavaField field) {
        Type[] types = field.getGenericTypeArguments();
        if (types.length != 1) {
            return null;
        }
        return types[0] instanceof Class<?> klass ? JdbcType.findByMatchingNativeType(klass) : null;
    }

    private static @NotNull Inliner wrap(@NotNull Predicate<JavaField> matcher,
                                         @NotNull Function<JavaField, MapperApi> builder) {
        return new Inliner() {
            @Override public boolean matches(@NotNull JavaField field) {
                return matcher.test(field);
            }
            @Override public @NotNull MapperApi buildInlineMapper(@NotNull JavaField field) {
                return builder.apply(field);
            }
        };
    }

    private interface Inliner {
        boolean matches(@NotNull JavaField field);

        @NotNull MapperApi buildInlineMapper(@NotNull JavaField field);
    }
}
