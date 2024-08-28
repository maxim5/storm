package io.spbx.orm.api;

import io.spbx.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.spbx.orm.api.TableMeta.ConstraintStatus.NO_CONSTRAINT;

/**
 * Holds the meta information of the SQL table.
 */
public interface TableMeta {
    /**
     * Returns the table name in SQL.
     */
    @NotNull String sqlTableName();

    /**
     * Returns the meta info for all columns in the table.
     */
    @NotNull List<ColumnMeta> sqlColumns();

    /**
     * Holds the meta information of the SQL column.
     */
    record ColumnMeta(@NotNull Column column,
                      @NotNull Class<?> type,
                      @NotNull ConstraintStatus primaryKey,
                      @NotNull ConstraintStatus unique,
                      @Nullable ForeignColumn foreignColumn,
                      boolean nullable,
                      @Nullable String defaultValue) {
        public static @NotNull ColumnMeta of(@NotNull Column column, @NotNull Class<?> type) {
            return new ColumnMeta(column, type, NO_CONSTRAINT, NO_CONSTRAINT, null, false, null);
        }

        public @NotNull String name() {
            return column.name();
        }

        public boolean isPrimaryKey() {
            return primaryKey != NO_CONSTRAINT;
        }

        public boolean isUnique() {
            return unique != NO_CONSTRAINT;
        }

        public boolean isForeignKey() {
            return foreignColumn != null;
        }

        public boolean isNullable() {
            return nullable;
        }

        public boolean isNotNull() {
            return !nullable;
        }

        public boolean hasDefault() {
            return defaultValue != null;
        }

        public @NotNull ColumnMeta withPrimaryKey(@NotNull ConstraintStatus status) {
            return new ColumnMeta(column, type, status, unique, foreignColumn, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withUnique(@NotNull ConstraintStatus status) {
            return new ColumnMeta(column, type, primaryKey, status, foreignColumn, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withForeignColumn(@NotNull ForeignColumn foreignColumn) {
            return new ColumnMeta(column, type, primaryKey, unique, foreignColumn, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withForeignColumn(@NotNull TableMeta meta, @NotNull Column column) {
            return withForeignColumn(new ForeignColumn(meta, column));
        }

        public @NotNull ColumnMeta withNullable(boolean nullable) {
            return new ColumnMeta(column, type, primaryKey, unique, foreignColumn, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withDefault(@NotNull String defaultValue) {
            return new ColumnMeta(column, type, primaryKey, unique, foreignColumn, nullable, defaultValue);
        }
    }

    /**
     * Represents the type of column constraint.
     */
    enum ConstraintStatus {
        NO_CONSTRAINT, SINGLE_COLUMN, COMPOSITE;

        public boolean isSingle() {
            return this == SINGLE_COLUMN;
        }
    }

    /**
     * Holds the data about the column in another table referenced by a foreign key.
     */
    record ForeignColumn(@NotNull TableMeta meta, @NotNull Column column) {}

    /**
     * Returns the primary key constraint.
     */
    @NotNull Constraint primaryKeys();

    /**
     * Returns the collection of unique constraints.
     */
    @NotNull Iterable<Constraint> unique();

    /**
     * Holds the constraint that spans across one or more columns.
     */
    record Constraint(@NotNull List<Column> columns, boolean isComposite) {
        public static @NotNull Constraint of(@NotNull Column @NotNull ... columns) {
            return new Constraint(List.of(columns), columns.length > 1);
        }

        public boolean isSingle() {
            return !isComposite;
        }
    }
}
