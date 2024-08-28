package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Immutable
public class Having extends Unit {
    private final BoolTerm term;

    public Having(@NotNull BoolTerm term) {
        super("HAVING %s".formatted(term.repr()), term.args());
        this.term = term;
    }

    public static @NotNull Having of(@NotNull BoolTerm term) {
        return new Having(term);
    }

    public static @NotNull Having and(@NotNull BoolTerm term1, @NotNull BoolTerm term2) {
        return new Having(new BoolOp(List.of(term1, term2), BoolOpType.AND));
    }

    public static @NotNull Having and(@NotNull BoolTerm ... terms) {
        return new Having(new BoolOp(List.of(terms), BoolOpType.AND));
    }

    public static @NotNull Having and(@NotNull Having having, @NotNull BoolTerm term) {
        return and(having.term, term);
    }

    public static @NotNull Having or(@NotNull BoolTerm term1, @NotNull BoolTerm term2) {
        return new Having(new BoolOp(List.of(term1, term2), BoolOpType.AND));
    }

    public static @NotNull Having or(@NotNull BoolTerm ... terms) {
        return new Having(new BoolOp(List.of(terms), BoolOpType.OR));
    }

    public static @NotNull Having or(@NotNull Having having, @NotNull BoolTerm term) {
        return and(having.term, term);
    }

    public static @NotNull Having hardcoded(@NotNull String raw) {
        return new Having(new HardcodedBoolTerm(raw));
    }

    public @NotNull Having andTerm(@NotNull BoolTerm term) {
        return Having.and(this, term);
    }

    public @NotNull Having orTerm(@NotNull BoolTerm term) {
        return Having.or(this, term);
    }

    @NotNull BoolTerm term() {
        return term;
    }
}
