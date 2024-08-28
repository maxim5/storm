package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;

/**
 * A common interface for any class that knows the connected {@link QueryRunner}.
 * Usually (but not necessarily) this means that the instance operates in the context of a {@link java.sql.Connection},
 * and can run SQL queries using that connection.
 */
public interface HasRunner {
    /**
     * Returns the query runner this instance is connected to or has in its context.
     */
    @NotNull QueryRunner runner();
}
