package io.spbx.orm.arch.model;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Immutable
public class OneColumnTableField extends TableField {
    private final Column column;

    public OneColumnTableField(@NotNull TableArch parent,
                               @NotNull ModelField field,
                               boolean primaryKey,
                               boolean unique,
                               boolean nullable,
                               @NotNull Defaults defaults,
                               @Nullable MapperApi mapperApi,
                               @Nullable AdapterApi adapterApi,
                               @NotNull Column column) {
        super(parent, field, primaryKey, unique, nullable, defaults, mapperApi, adapterApi);
        assert defaults.size() == 1 : "Columns of `%s` don't match the defaults: column=%s, defaults=%s"
            .formatted(field.name(), column, defaults);
        this.column = column;
    }

    public @NotNull Column column() {
        return column;
    }

    @Override
    public @NotNull List<Column> columns() {
        return Collections.singletonList(column);
    }

    @Override
    public int columnsNumber() {
        return 1;
    }

    @Override
    public boolean isMultiColumn() {
        return false;
    }

    @Override
    public boolean isSingleColumn() {
        return true;
    }
}
