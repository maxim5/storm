package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HasColumns {
    @NotNull List<Column> columns();

    default int columnsNumber() {
        return columns().size();
    }

    default boolean isMultiColumn() {
        return columnsNumber() > 1;
    }

    default boolean isSingleColumn() {
        return columnsNumber() == 1;
    }
}
