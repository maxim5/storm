package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for SQL pieces that have a (usually human-readable) string representation.
 * The same representation is usually but not necessarily used in {@link #toString()}.
 */
public interface Representable {
    /**
     * Returns a string representation of this instance.
     */
    @NotNull String repr();
}
