package io.spbx.orm.api.query;

import io.spbx.util.collect.stream.Streamer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static io.spbx.orm.api.query.TermType.*;

/**
 * Represents an SQL function. May have one or more inputs and an output. All inputs and output have a type.
 * Some functions are aggregate ({@link #isAggregate()}), hence can participate in {@link SelectGroupBy} queries.
 */
@Immutable
public enum Func implements Representable {
    COUNT("count", List.of(WILDCARD), NUMBER),
    SUM("sum", List.of(NUMBER), NUMBER),
    AVG("avg", List.of(NUMBER), NUMBER),
    MAX("max", List.of(NUMBER), NUMBER),
    MIN("min", List.of(NUMBER), NUMBER),

    FIRST("first", List.of(WILDCARD), WILDCARD),
    FIRST_NUM("first", List.of(NUMBER), NUMBER),
    FIRST_STR("first", List.of(STRING), STRING),

    LAST("last", List.of(WILDCARD), WILDCARD),
    LAST_NUM("last", List.of(NUMBER), NUMBER),
    LAST_STR("last", List.of(STRING), STRING),

    ABS("abs", List.of(NUMBER), NUMBER),
    SQUARE("square", List.of(NUMBER), NUMBER),
    SQRT("sqrt", List.of(NUMBER), NUMBER),

    LOWER("lower", List.of(STRING), STRING),
    LCASE("lcase", List.of(STRING), STRING),
    UPPER("upper", List.of(STRING), STRING),
    UCASE("ucase", List.of(STRING), STRING),

    TRIM("trim", List.of(STRING), STRING),
    LTRIM("ltrim", List.of(STRING), STRING),
    RTRIM("rtrim", List.of(STRING), STRING),
    LPAD("lpad", List.of(STRING, NUMBER, STRING), STRING),
    RPAD("rpad", List.of(STRING, NUMBER, STRING), STRING),

    ASCII("ascii", List.of(STRING), NUMBER),
    CHAR("char", List.of(NUMBER), STRING),
    STR("str", List.of(NUMBER), STRING),
    HEX("hex", List.of(STRING), STRING),

    LEFT("left", List.of(STRING, NUMBER), STRING),
    RIGHT("right", List.of(STRING, NUMBER), STRING),
    SUBSTR("substr", List.of(STRING, NUMBER, NUMBER), STRING),
    SUBSTRING("substring", List.of(STRING, NUMBER, NUMBER), STRING),
    MID("mid", List.of(STRING, NUMBER, NUMBER), STRING),

    FIND_IN_SET("find_in_set", List.of(STRING, STRING), NUMBER),
    LOCATE("locate", List.of(STRING, STRING), NUMBER),
    LOCATE_FROM("locate", List.of(STRING, STRING, NUMBER), NUMBER),
    POSITION("position", "position(%s IN %s)", List.of(STRING, STRING), NUMBER),
    INSTR("instr", List.of(STRING, STRING), NUMBER),

    INSERT("insert", List.of(STRING, NUMBER, NUMBER, STRING), STRING),
    TRANSLATE("translate", List.of(STRING, STRING, STRING), STRING),
    REPLACE("replace", List.of(STRING, STRING, STRING), STRING),
    REPEAT("repeat", List.of(STRING, NUMBER), STRING),
    REVERSE("reverse", List.of(STRING), STRING),

    LEN("len", List.of(STRING), NUMBER),
    LENGTH("length", List.of(STRING), NUMBER),
    CHAR_LEN("char_len", List.of(STRING), NUMBER),
    CHAR_LENGTH("char_length", List.of(STRING), NUMBER),
    STRCMP("format", List.of(STRING, STRING), NUMBER),
    FORMAT("format", List.of(NUMBER, NUMBER), STRING),

    CAST_AS("CAST", "CAST(%s AS %s)", List.of(WILDCARD, STRING), WILDCARD),
    CAST_AS_SIGNED("CAST", "CAST(%s AS SIGNED)", List.of(WILDCARD), NUMBER),
    CAST_AS_CHAR("CAST", "CAST(%s AS CHAR)", List.of(WILDCARD), STRING),

    CONCAT("concat", List.of(STRING, STRING), STRING),
    CONCAT3("concat", List.of(STRING, STRING, STRING), STRING),
    CONCAT4("concat", List.of(STRING, STRING, STRING, STRING), STRING),
    CONCAT5("concat", List.of(STRING, STRING, STRING, STRING, STRING), STRING),

    CONCAT_WS("concat_ws", List.of(STRING, STRING, STRING), STRING),
    CONCAT_WS3("concat_ws", List.of(STRING, STRING, STRING, STRING), STRING),
    CONCAT_WS4("concat_ws", List.of(STRING, STRING, STRING, STRING, STRING), STRING),
    CONCAT_WS5("concat_ws", List.of(STRING, STRING, STRING, STRING, STRING, STRING), STRING),

    ISNULL("isnull", List.of(WILDCARD), BOOL),
    IFNULL("ifnull", List.of(WILDCARD, WILDCARD), WILDCARD),
    IFNULL_NUM("ifnull", List.of(NUMBER, NUMBER), NUMBER),
    IFNULL_STR("ifnull", List.of(STRING, STRING), STRING),

    COALESCE("coalesce", List.of(WILDCARD, WILDCARD), WILDCARD),
    COALESCE3("coalesce", List.of(WILDCARD, WILDCARD, WILDCARD), WILDCARD),
    COALESCE4("coalesce", List.of(WILDCARD, WILDCARD, WILDCARD, WILDCARD), WILDCARD),
    COALESCE5("coalesce", List.of(WILDCARD, WILDCARD, WILDCARD, WILDCARD, WILDCARD), WILDCARD);

    private final String repr;
    private final String pattern;
    private final List<TermType> inputTypes;
    private final TermType resultType;

    Func(@NotNull String repr, @NotNull String pattern, @NotNull List<TermType> inputTypes, @NotNull TermType resultType) {
        this.repr = repr;
        this.pattern = pattern;
        this.inputTypes = inputTypes;
        this.resultType = resultType;
    }

    Func(@NotNull String repr, @NotNull List<TermType> inputTypes, @NotNull TermType resultType) {
        this(repr, defaultFuncPattern(repr, inputTypes.size()), inputTypes, resultType);
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    public int arity() {
        return inputTypes.size();
    }

    public boolean isAggregate() {
        return AGGREGATE_VALUES.contains(this);
    }

    public @NotNull String format(@NotNull Term term) {
        InvalidQueryException.assure(matchesInput(term), "Incompatible function `%s` input: `%s`", this, term);
        return pattern.formatted(term.repr());
    }

    public @NotNull String format(@NotNull Term term1, @NotNull Term term2) {
        InvalidQueryException.assure(matchesInput(term1, term2),
                                     "Incompatible function `%s` inputs: [`%s`, `%s`]", this, term1, term2);
        return pattern.formatted(term1.repr(), term2.repr());
    }

    public @NotNull String format(@NotNull List<? extends Term> terms) {
        InvalidQueryException.assure(matchesInput(terms), "Incompatible function `%s` inputs: `%s`", this, terms);
        return pattern.formatted(terms.stream().map(Representable::repr).toArray());
    }

    public boolean matchesInput(@NotNull Term term) {
        return arity() == 1 && match(inputTypes.getFirst(), term.type());
    }

    public boolean matchesInput(@NotNull Term term1, @NotNull Term term2) {
        return arity() == 2 && match(inputTypes.get(0), term1.type()) && match(inputTypes.get(1), term2.type());
    }

    public boolean matchesInput(@NotNull List<? extends Term> terms) {
        return Streamer.zip(inputTypes, terms).allMatch(((inputType, term) -> match(inputType, term.type())));
    }

    public @NotNull TermType resultType() {
        return resultType;
    }

    public @NotNull FuncExpr apply(@NotNull Term term) {
        return new FuncExpr(this, term);
    }

    public @NotNull FuncExpr apply(@NotNull Term term1, @NotNull Term term2) {
        return new FuncExpr(this, term1, term2);
    }

    public @NotNull FuncExpr apply(@NotNull Term ... terms) {
        return apply(List.of(terms));
    }

    public @NotNull FuncExpr apply(@NotNull List<? extends Term> terms) {
        return new FuncExpr(this, terms);
    }

    private static @NotNull String defaultFuncPattern(@NotNull String func, int arity) {
        return func + Streamer.repeat("%s", arity).join(", ", "(", ")");
    }

    private static final Set<Func> AGGREGATE_VALUES = EnumSet.of(
        COUNT, SUM, AVG, MIN, MAX,
        FIRST, FIRST_NUM, FIRST_STR,
        LAST, LAST_NUM, LAST_STR
    );
}
