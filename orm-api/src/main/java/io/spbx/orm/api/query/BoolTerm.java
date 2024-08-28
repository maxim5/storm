package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a term that can participate in boolean expressions. Has a {@link TermType#BOOL} type.
 */
public interface BoolTerm extends Term {
    @Override
    default @NotNull TermType type() {
        return TermType.BOOL;
    }

    default @NotNull BoolTerm not() {
        return Not.not(this);
    }
}
