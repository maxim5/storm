package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;

/**
 * A common interface for any class that knows the connected {@link Engine}.
 * Usually (but not necessarily) this means that the instance operates in the context of a {@link java.sql.Connection}.
 */
public interface HasEngine {
    /**
     * Returns the engine this instance is connected to or has in its context.
     */
    @NotNull Engine engine();
}
