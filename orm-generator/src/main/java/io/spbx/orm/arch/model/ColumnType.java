package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

public record ColumnType(@NotNull JdbcType jdbcType) {
    @Override
    public String toString() {
        return jdbcType.toString();
    }
}
