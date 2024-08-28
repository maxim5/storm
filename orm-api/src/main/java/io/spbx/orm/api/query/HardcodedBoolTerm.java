package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class HardcodedBoolTerm extends Unit implements BoolTerm {
    public HardcodedBoolTerm(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public HardcodedBoolTerm(@NotNull String repr) {
        super(repr);
    }
}
