package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a type of term or expression. An expression can have one or more inputs and an output,
 * and to be valid their types must match. There exists a universal {@link #WILDCARD} type which matches all types.
 */
public enum TermType {
    NUMBER,
    STRING,
    BOOL,
    TIME,
    WILDCARD;

    /**
     * Returns whether {@code first} type matches the {@code second} type.
     * The relation is symmetric.
     */
    public static boolean match(@NotNull TermType first, @NotNull TermType second) {
        return first == second || first == WILDCARD || second == WILDCARD;
    }

    /**
     * Returns whether all {@code first}, {@code second} and {@code third} types match pairwise.
     * The relation is symmetric.
     */
    public static boolean match(@NotNull TermType first, @NotNull TermType second, @NotNull TermType third) {
        return match(first, second) && match(second, third) && match(first, third);
    }
}
