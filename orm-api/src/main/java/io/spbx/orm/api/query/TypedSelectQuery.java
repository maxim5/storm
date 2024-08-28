package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TypedSelectQuery extends SelectQuery {
    /**
     * Returns the number of columns of the query result.
     */
    int columnsNumber();

    /**
     * Returns the types for each of the returned column
     */
    @NotNull List<TermType> columnTypes();
}
