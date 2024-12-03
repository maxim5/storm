package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

import static io.spbx.orm.arch.model.SqlNameValidator.validateSqlName;

@Immutable
public record Column(@NotNull String sqlName, @NotNull ColumnType type) {
    public Column {
        validateSqlName(sqlName);
    }

    public static @NotNull Column of(@NotNull String sqlName, @NotNull JdbcType jdbcType) {
        return new Column(sqlName, new ColumnType(jdbcType));
    }

    public @NotNull Column renamed(@NotNull String newName) {
        return new Column(newName, type);
    }

    public @NotNull PrefixedColumn prefixed(@NotNull String prefix) {
        return new PrefixedColumn(this, prefix);
    }

    public @NotNull JdbcType jdbcType() {
        return type.jdbcType();
    }

    @Override
    public String toString() {
        return "Column(%s/%s)".formatted(sqlName, type);
    }
}
