package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

import static io.spbx.orm.api.query.Args.flattenArgsOf;

@Immutable
public class Like extends Unit implements BoolTerm {
    public Like(@NotNull Term lhs, @NotNull Term rhs) {
        super("%s LIKE %s".formatted(lhs.repr(), rhs.repr()), flattenArgsOf(lhs, rhs));
        InvalidQueryException.assure(TermType.match(lhs.type(), rhs.type(), TermType.STRING),
                                     "Incompatible types to like: lhs=%s rhs=%s", lhs, rhs);
    }

    public static @NotNull Like like(@NotNull Term lhs, @NotNull Term rhs) {
        return new Like(lhs, rhs);
    }
}
