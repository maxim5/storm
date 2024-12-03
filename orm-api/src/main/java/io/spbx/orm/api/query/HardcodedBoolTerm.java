package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class HardcodedBoolTerm extends Unit implements BoolTerm {
    public HardcodedBoolTerm(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public HardcodedBoolTerm(@NotNull String repr) {
        super(repr);
    }
}
