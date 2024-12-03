package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class NumberLiteral extends Unit implements Term {
    public NumberLiteral(@NotNull Number num) {
        super(num.toString());
    }

    public NumberLiteral(int num) {
        super(String.valueOf(num));
    }

    public NumberLiteral(long num) {
        super(String.valueOf(num));
    }

    @Override
    public @NotNull TermType type() {
        return TermType.NUMBER;
    }
}
