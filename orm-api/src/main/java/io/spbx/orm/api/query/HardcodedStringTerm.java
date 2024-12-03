package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class HardcodedStringTerm extends Unit implements Term {
    public HardcodedStringTerm(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public HardcodedStringTerm(@NotNull String repr) {
        super(repr);
    }

    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }
}
