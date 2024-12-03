package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class PojoFieldNative extends PojoField {
    private final JdbcType jdbcType;

    private PojoFieldNative(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull JdbcType jdbcType) {
        super(parent, field, TypeSupport.NATIVE);
        this.jdbcType = jdbcType;
    }

    public static @NotNull PojoField ofNative(@NotNull ModelField field, @NotNull JdbcType jdbcType) {
        return new PojoFieldNative(PojoParent.DETACHED, field, jdbcType);
    }

    public @NotNull JdbcType jdbcType() {
        return jdbcType;
    }

    public @NotNull Column column() {
        return Column.of(fullSqlName(), jdbcType);
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return new PojoFieldNative(parent, field, jdbcType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldNative that && super.equals(obj) && Objects.equals(this.jdbcType, that.jdbcType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), jdbcType);
    }

    @Override
    public String toString() {
        return "PojoFieldNative[parent=%s, field=%s, type=%s]".formatted(parent, field, jdbcType);
    }
}
