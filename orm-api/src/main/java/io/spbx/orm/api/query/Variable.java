package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;

/**
 * A class for the variable occurrence in the SQL query. Is a {@link Term}.
 * SQL repr: {@code ?}.
 */
@Immutable
public class Variable extends Unit implements Term {
    private final TermType type;

    public Variable(@Nullable Object value, @NotNull TermType type) {
        super("?", Args.of(value));
        this.type = type;
    }

    public Variable(int value) {
        super("?", Args.of(value));
        this.type = TermType.NUMBER;
    }

    public Variable(long value) {
        super("?", Args.of(value));
        this.type = TermType.NUMBER;
    }

    @Override
    public @NotNull TermType type() {
        return type;
    }
}
