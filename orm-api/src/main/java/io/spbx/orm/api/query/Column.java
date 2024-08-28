package io.spbx.orm.api.query;

import io.spbx.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a column mention in the SQL query. Is a {@link Term} and is {@link Named}. Does not have args.
 */
public interface Column extends Named {
    @Override
    default @NotNull String repr() {
        return name();
    }

    @Override
    default @NotNull Args args() {
        return Args.of();
    }

    /**
     * Wraps the column to make it distinct.
     */
    default @NotNull DistinctColumn distinct() {
        return new DistinctColumn(this);
    }

    /**
     * Wraps this column to make it attributed to a given table.
     */
    default @NotNull FullColumn fullFrom(@NotNull String table) {
        return new FullColumn(this, table);
    }

    /**
     * Wraps this column to make it attributed to a given table.
     */
    default @NotNull FullColumn fullFrom(@NotNull TableMeta table) {
        return fullFrom(table.sqlTableName());
    }
}
