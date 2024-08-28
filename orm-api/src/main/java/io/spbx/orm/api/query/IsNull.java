package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class IsNull extends Unit implements BoolTerm {
    public IsNull(@NotNull Term term) {
        super("%s IS NULL".formatted(term.repr()), term.args());
    }

    public static @NotNull IsNull isNull(@NotNull Term term) {
        return new IsNull(term);
    }
}
