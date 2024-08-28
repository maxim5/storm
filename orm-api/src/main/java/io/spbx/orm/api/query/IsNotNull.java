package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class IsNotNull extends Unit implements BoolTerm {
    public IsNotNull(@NotNull Term term) {
        super("%s IS NOT NULL".formatted(term.repr()), term.args());
    }

    public static @NotNull IsNotNull isNotNull(@NotNull Term term) {
        return new IsNotNull(term);
    }
}
