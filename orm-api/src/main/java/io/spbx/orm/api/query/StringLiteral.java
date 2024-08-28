package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class StringLiteral extends Unit implements Term {
    public StringLiteral(@NotNull String literal) {
        super("'%s'".formatted(literal));
    }

    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }
}
