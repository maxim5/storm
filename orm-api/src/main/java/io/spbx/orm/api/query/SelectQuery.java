package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an SQL query which selects some data from the DB. May include one or more {@code SELECT} clauses,
 * as well as filtering, grouping, etc.
 */
public interface SelectQuery extends Representable, HasArgs {
    /**
     * Returns a copy of this query which strips the internal fields off. Useful for memory management,
     * e.g. if the query is stored statically.
     */
    default @NotNull SelectQuery intern() {
        return HardcodedSelectQuery.of(repr(), args());
    }
}
