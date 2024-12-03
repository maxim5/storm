package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class IsNotNull extends Unit implements BoolTerm {
    public IsNotNull(@NotNull Term term) {
        super("%s IS NOT NULL".formatted(term.repr()), term.args());
    }

    public static @NotNull IsNotNull isNotNull(@NotNull Term term) {
        return new IsNotNull(term);
    }
}
