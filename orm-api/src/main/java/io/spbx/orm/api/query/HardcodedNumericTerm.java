package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class HardcodedNumericTerm extends Unit implements Term {
    public HardcodedNumericTerm(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public HardcodedNumericTerm(@NotNull String repr) {
        super(repr);
    }

    @Override
    public @NotNull TermType type() {
        return TermType.NUMBER;
    }
}
