package io.spbx.orm.api.query;

import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;

@Stateless
public class Shortcuts {
    public static final Term NULL = new HardcodedTerm("NULL", TermType.WILDCARD);
    public static final Term STAR = new HardcodedTerm("*", TermType.WILDCARD);

    public static final BoolTerm TRUE = new HardcodedBoolTerm("TRUE");
    public static final BoolTerm FALSE = new HardcodedBoolTerm("FALSE");

    public static final Term ZERO = num(0);
    public static final Term ONE = num(1);

    public static final Variable UNRESOLVED_NUM = unresolved("noname", TermType.NUMBER);

    public static @NotNull NumberLiteral num(int value) {
        return new NumberLiteral(value);
    }

    public static @NotNull NumberLiteral num(long value) {
        return new NumberLiteral(value);
    }

    public static @NotNull NumberLiteral num(@NotNull Number value) {
        return new NumberLiteral(value);
    }

    public static @NotNull StringLiteral literal(@NotNull String value) {
        return new StringLiteral(value);
    }

    public static @NotNull Variable var(int value) {
        return new Variable(value);
    }

    public static @NotNull Variable var(long value) {
        return new Variable(value);
    }

    public static @NotNull Variable var(@NotNull String value) {
        return new Variable(value, TermType.STRING);
    }

    public static @NotNull Variable var(byte @NotNull[] value) {
        return new Variable(value, TermType.STRING);
    }

    public static @NotNull Variable var(@NotNull Timestamp timestamp) {
        return new Variable(timestamp, TermType.TIME);
    }

    public static @NotNull Variable unresolved(@NotNull String name, @NotNull TermType type) {
        return unresolved(name, type, UnresolvedArg.defaultValueForType(type));
    }

    public static @NotNull Variable unresolved(@NotNull String name, @NotNull TermType type, @Nullable Object defaultValue) {
        return new Variable(new UnresolvedArg(name, defaultValue), type);
    }

    public static @NotNull BoolTerm lookupBy(@NotNull Column column, int variable) {
        return lookupBy(column, var(variable));
    }

    public static @NotNull BoolTerm lookupBy(@NotNull Column column, long variable) {
        return lookupBy(column, var(variable));
    }

    public static @NotNull BoolTerm lookupBy(@NotNull Column column, @NotNull String variable) {
        return lookupBy(column, var(variable));
    }

    public static @NotNull BoolTerm lookupBy(@NotNull Column column, @NotNull Variable variable) {
        return CompareType.EQ.compare(column, variable);
    }

    public static @NotNull IsIn isIn(@NotNull Term lhs, @NotNull Term @NotNull ... terms) {
        return IsIn.isIn(lhs, terms);
    }

    public static @NotNull IsIn isIn(@NotNull Term lhs, @NotNull List<? extends Term> terms) {
        return IsIn.isIn(lhs, terms);
    }

    // FIX[minor]: isIn for ints, longs

    public static @NotNull Like like(@NotNull Term lhs, @NotNull Term rhs) {
        return Like.like(lhs, rhs);
    }

    public static @NotNull Between between(@NotNull Term term, @NotNull Term left, @NotNull Term right) {
        return Between.between(term, left, right);
    }
}
