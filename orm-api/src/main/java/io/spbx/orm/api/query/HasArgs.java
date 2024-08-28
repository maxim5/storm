package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for all pieces of an SQL query that have arguments (possibly unresolved or empty).
 * The args correspond to the <code>"?"</code> in the current piece of SQL.
 *
 * @see Args
 */
public interface HasArgs extends Representable {
    /**
     * Returns the {@link Args} used by this instance.
     */
    @NotNull Args args();
}
