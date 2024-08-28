package io.spbx.orm.api.entity;

import io.spbx.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a structure that holds an ordered collection of columns.
 */
public interface ColumnSet {
    /**
     * Returns the stored columns
     */
    @NotNull Collection<? extends Column> columns();
}
