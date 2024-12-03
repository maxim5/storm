package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

import static io.spbx.orm.api.query.Args.flattenArgsOf;

@Immutable
public class Compare extends Unit implements BoolTerm {
    public Compare(@NotNull Term lhs, @NotNull Term rhs, @NotNull CompareType compareType) {
        super("%s %s %s".formatted(lhs.repr(), compareType.repr(), rhs.repr()), flattenArgsOf(lhs, rhs));
        InvalidQueryException.assure(TermType.match(lhs.type(), rhs.type()),
                                     "Incompatible types to compare: lhs=%s rhs=%s compare=%s", lhs, rhs, compareType);
    }
}
