package io.spbx.orm.codegen;

import io.spbx.orm.api.TableMeta.ConstraintStatus;
import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.TableField;
import io.spbx.util.base.str.BasicJoin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ColumnMetaMaker {
    public static @NotNull String makeColumnMeta(@NotNull TableField field, @NotNull Column column) {
        String sqlName = column.sqlName();
        Class<?> nativeType = column.jdbcType().nativeType();
        String type = nativeType.isPrimitive() || nativeType == String.class ?
            nativeType.getSimpleName() :
            nativeType == byte[].class ?
                "byte[]" :
                nativeType.getName();

        return BasicJoin.of(
            "ColumnMeta.of(OwnColumn.%s, %s.class)".formatted(sqlName, type),
            toConstraintStatus(".withPrimaryKey", field.isPrimaryKey(), field.isMultiColumn()),
            toConstraintStatus(".withUnique", field.isUnique(), field.isMultiColumn()),
            toForeignColumn(".withForeignColumn", field),
            toBooleanStatus(".withNullable", field.isNullable()),
            toStringStatus(".withDefault", field.columnDefault(column))
        ).join();
    }

    private static @NotNull String toConstraintStatus(@NotNull String method, boolean value, boolean isMultiColumn) {
        ConstraintStatus status = isMultiColumn ? ConstraintStatus.COMPOSITE : ConstraintStatus.SINGLE_COLUMN;
        return value ? "%s(ConstraintStatus.%s)".formatted(method, status) : "";
    }

    private static @NotNull String toForeignColumn(@NotNull String method, @NotNull TableField field) {
        if (field.isForeignKey()) {
            ForeignTableField foreignField = (ForeignTableField) field;
            String tableName = foreignField.getForeignTable().javaName();
            String columnName = foreignField.primaryKeyColumnInForeignTable().sqlName();
            return "%s(%s.META, %s.OwnColumn.%s)".formatted(method, tableName, tableName, columnName);
        }
        return "";
    }

    private static @NotNull String toBooleanStatus(@NotNull String method, boolean value) {
        return value ? "%s(true)".formatted(method) : "";
    }

    private static @NotNull String toStringStatus(@NotNull String method, @Nullable String value) {
        return value != null ? "%s(\"%s\")".formatted(method, value) : "";
    }
}
