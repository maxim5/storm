package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for all terms of an SQL query. A term has a {@link TermType} and can participate in expressions.
 */
public interface Term extends Representable, HasArgs {
    /**
     * Returns the term type.
     */
    @NotNull TermType type();

    /**
     * Wraps this term to add it an alias {@code name}.
     */
    default @NotNull Named namedAs(@NotNull String name) {
        return new NamedAs(this, name);
    }

    /**
     * Wraps this term as a {@link BoolTerm} (or throws if it's not boolean).
     */
    default @NotNull BoolWrapper bool() {
        return new BoolWrapper(this);
    }

    /**
     * Wraps this term to add it an {@code order}.
     */
    default @NotNull OrderTerm ordered(@NotNull Order order) {
        return new OrderTerm(this, order);
    }

    /**
     * Converts this term to a boolean expression that it is not null.
     */
    default @NotNull IsNotNull isNotNull() {
        return IsNotNull.isNotNull(this);
    }

    /**
     * Converts this term to a boolean expression that it is not null.
     */
    default @NotNull IsNull isNull() {
        return IsNull.isNull(this);
    }
}
