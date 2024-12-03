package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

/**
 * A query unit with an alias.
 * SQL repr: {@code [term] AS [name]}.
 */
@Immutable
public class NamedAs extends Unit implements Named {
    private final Term term;
    private final String name;

    public NamedAs(@NotNull Term term, @NotNull String name) {
        super(formatTermAs(term, name), term.args());
        this.term = term;
        this.name = name;
    }

    @Override
    public @NotNull TermType type() {
        return term.type();
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    private static String formatTermAs(@NotNull Term term, @NotNull String name) {
        if (term instanceof NamedAs as) {
            term = as.term;
        }
        assert !(term instanceof NamedAs) : "Circular named term: %s".formatted(term);
        return "%s AS %s".formatted(term.repr(), name);
    }
}
