package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class HardcodedTerm extends Unit implements Term {
    private final @NotNull TermType type;

    public HardcodedTerm(@NotNull String repr, @NotNull Args args, @NotNull TermType type) {
        super(repr, args);
        this.type = type;
    }

    public HardcodedTerm(@NotNull String repr, @NotNull TermType type) {
        super(repr);
        this.type = type;
    }

    @Override
    public @NotNull TermType type() {
        return type;
    }
}
