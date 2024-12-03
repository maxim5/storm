package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static io.spbx.orm.api.query.Args.flattenArgsOf;

/**
 * Represents a functional expression in SQL. Consists of a function {@link Func} and a number of input terms.
 * The type of {@link FuncExpr} is the type of function output.
 * If the function is aggregate, then the expression is also aggregate ({@link #isAggregate()}),
 * then can be used in {@link SelectGroupBy} queries.
 */
@Immutable
public class FuncExpr extends Unit implements Term {
    private final Func func;

    public FuncExpr(@NotNull Func func, @NotNull Term term) {
        super(func.format(term), term.args());
        this.func = func;
    }

    public FuncExpr(@NotNull Func func, @NotNull Term term1, @NotNull Term term2) {
        super(func.format(term1, term2), flattenArgsOf(term1, term2));
        this.func = func;
    }

    public FuncExpr(@NotNull Func func, @NotNull List<? extends Term> terms) {
        super(func.format(terms), flattenArgsOf(terms));
        this.func = func;
    }

    @Override
    public @NotNull TermType type() {
        return func.resultType();
    }

    public boolean isAggregate() {
        return func.isAggregate();
    }
}
