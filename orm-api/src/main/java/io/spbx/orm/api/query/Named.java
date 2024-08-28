package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * Any term that has a {@link #name()} and can be referenced in the query by that name.
 */
public interface Named extends Term {
    /**
     * Returns the term name.
     */
    @NotNull String name();

    @Override
    default @NotNull Named namedAs(@NotNull String name) {
        return name().equals(name) ? this : new NamedAs(this, name);
    }
}
