package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class Not extends Unit implements BoolTerm {
    public Not(@NotNull BoolTerm term) {
        super(composeRepr(term), term.args());
    }

    public static @NotNull Not not(@NotNull BoolTerm term) {
        return new Not(term);
    }

    private static @NotNull String composeRepr(@NotNull BoolTerm term) {
        if (BoolOp.isComposite(term)) {
            return "NOT (%s)".formatted(term.repr());
        }
        return "NOT %s".formatted(term.repr());
    }
}
