package io.spbx.orm.arch.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import io.spbx.util.collect.BasicMaps;
import io.spbx.util.collect.MapBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum JdbcType {
    Boolean(boolean.class),
    Int(int.class),
    Long(long.class),
    Short(short.class),
    Byte(byte.class),
    Float(float.class),
    Double(double.class),
    String(String.class),
    Bytes(byte[].class),
    Date(java.sql.Date.class),
    Time(java.sql.Time.class),
    Timestamp(java.sql.Timestamp.class);

    private final Class<?> nativeType;

    JdbcType(@NotNull Class<?> nativeType) {
        this.nativeType = nativeType;
    }

    public @NotNull Class<?> nativeType() {
        return nativeType;
    }

    public @NotNull String getterMethod() {
        return "get%s".formatted(name());
    }

    private static final ImmutableMap<Class<?>, JdbcType> TYPES_BY_CLASS = MapBuilder.<Class<?>, JdbcType>builder()
        .putAll(BasicMaps.indexBy(JdbcType.values(), JdbcType::nativeType))
        .put(java.util.Date.class, Date)
        .toGuavaImmutableMap();

    public static @Nullable JdbcType findByMatchingNativeType(@NotNull Class<?> nativeType) {
        return TYPES_BY_CLASS.get(Primitives.unwrap(nativeType));
    }
}
