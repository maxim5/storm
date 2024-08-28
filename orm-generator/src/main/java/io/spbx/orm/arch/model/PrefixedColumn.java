package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import static io.spbx.orm.arch.model.SqlNameValidator.validateSqlName;

public record PrefixedColumn(@NotNull Column column, @NotNull String prefix) {
    public @NotNull String sqlName() {
        return validateSqlName(column.sqlName());
    }

    public @NotNull ColumnType type() {
        return column.type();
    }

    public @NotNull String sqlPrefixedName() {
        return "%s.%s".formatted(prefix, sqlName());
    }

    @Override
    public String toString() {
        return "PrefixedColumn(%s::%s/%s)".formatted(prefix, sqlName(), type());
    }
}
