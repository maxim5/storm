package io.spbx.orm.api.query;

import io.spbx.orm.testing.FakeColumn;
import io.spbx.orm.testing.PersonTableData.PersonColumn;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.api.query.Shortcuts.*;
import static io.spbx.orm.api.query.TermType.*;
import static io.spbx.orm.testing.AssertSql.assertReprThrows;
import static io.spbx.orm.testing.AssertSql.assertTerm;

public class FuncTest {
    @Test
    public void aggregations() {
        assertTerm(Func.COUNT.apply(FakeColumn.FOO)).isEqualTo("count(foo)").hasType(NUMBER);
        assertTerm(Func.COUNT.apply(FakeColumn.INT)).isEqualTo("count(i)").hasType(NUMBER);
        assertTerm(Func.COUNT.apply(FakeColumn.STR)).isEqualTo("count(s)").hasType(NUMBER);

        assertTerm(Func.SUM.apply(FakeColumn.FOO)).isEqualTo("sum(foo)").hasType(NUMBER);
        assertTerm(Func.SUM.apply(FakeColumn.INT)).isEqualTo("sum(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.SUM.apply(FakeColumn.STR));

        assertTerm(Func.AVG.apply(FakeColumn.FOO)).isEqualTo("avg(foo)").hasType(NUMBER);
        assertTerm(Func.AVG.apply(FakeColumn.INT)).isEqualTo("avg(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.AVG.apply(FakeColumn.STR));

        assertTerm(Func.MIN.apply(FakeColumn.FOO)).isEqualTo("min(foo)").hasType(NUMBER);
        assertTerm(Func.MIN.apply(FakeColumn.INT)).isEqualTo("min(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.MIN.apply(FakeColumn.STR));

        assertTerm(Func.MAX.apply(FakeColumn.FOO)).isEqualTo("max(foo)").hasType(NUMBER);
        assertTerm(Func.MAX.apply(FakeColumn.INT)).isEqualTo("max(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.MAX.apply(FakeColumn.STR));

        assertTerm(Func.FIRST.apply(FakeColumn.FOO)).isEqualTo("first(foo)").hasType(WILDCARD);
        assertTerm(Func.FIRST_NUM.apply(FakeColumn.INT)).isEqualTo("first(i)").hasType(NUMBER);
        assertTerm(Func.FIRST_STR.apply(FakeColumn.STR)).isEqualTo("first(s)").hasType(STRING);

        assertTerm(Func.LAST.apply(FakeColumn.FOO)).isEqualTo("last(foo)").hasType(WILDCARD);
        assertTerm(Func.LAST_NUM.apply(FakeColumn.INT)).isEqualTo("last(i)").hasType(NUMBER);
        assertTerm(Func.LAST_STR.apply(FakeColumn.STR)).isEqualTo("last(s)").hasType(STRING);
    }

    @Test
    public void strings_case() {
        assertTerm(Func.LOWER.apply(literal("ABC"))).isEqualTo("lower('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.LOWER.apply(FakeColumn.INT));

        assertTerm(Func.LCASE.apply(literal("ABC"))).isEqualTo("lcase('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.LCASE.apply(FakeColumn.INT));

        assertTerm(Func.UPPER.apply(literal("ABC"))).isEqualTo("upper('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.UPPER.apply(FakeColumn.INT));

        assertTerm(Func.UCASE.apply(literal("ABC"))).isEqualTo("ucase('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.UCASE.apply(FakeColumn.INT));
    }

    @Test
    public void strings_substring() {
        assertTerm(Func.SUBSTRING.apply(FakeColumn.STR, num(1), num(2)))
            .isEqualTo("substring(s, 1, 2)")
            .hasType(STRING);
        assertTerm(Func.SUBSTRING.apply(literal("ABC"), num(1), num(3)))
            .isEqualTo("substring('ABC', 1, 3)")
            .hasType(STRING);

        assertReprThrows(() -> Func.SUBSTRING.apply(num(0), num(1), num(3)));
        assertReprThrows(() -> Func.SUBSTRING.apply(FakeColumn.STR, FakeColumn.STR, FakeColumn.STR));
        assertReprThrows(() -> Func.SUBSTRING.apply(literal("ABC")));
        assertReprThrows(() -> Func.SUBSTRING.apply(literal("ABC"), num(0)));
    }

    @Test
    public void strings_translate() {
        assertTerm(Func.TRANSLATE.apply(FakeColumn.STR, literal("x"), literal("y")))
            .isEqualTo("translate(s, 'x', 'y')")
            .hasType(STRING);
        assertTerm(Func.TRANSLATE.apply(literal("X"), FakeColumn.STR, FakeColumn.FOO))
            .isEqualTo("translate('X', s, foo)")
            .hasType(STRING);

        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(1), num(3)));
        assertReprThrows(() -> Func.TRANSLATE.apply(num(0), num(1), num(3)));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC")));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(0)));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(0), num(1)));
    }

    @Test
    public void string_length() {
        assertTerm(Func.LENGTH.apply(FakeColumn.STR)).isEqualTo("length(s)").hasType(NUMBER);
        assertReprThrows(() -> Func.LENGTH.apply(PersonColumn.birthday));
    }

    @Test
    public void cast_as() {
        assertTerm(Func.CAST_AS.apply(NULL, new HardcodedStringTerm("TEXT"))).isEqualTo("CAST(NULL AS TEXT)").hasType(WILDCARD);
        assertTerm(Func.CAST_AS_SIGNED.apply(FakeColumn.FOO)).isEqualTo("CAST(foo AS SIGNED)").hasType(NUMBER);
        assertTerm(Func.CAST_AS_SIGNED.apply(FakeColumn.INT)).isEqualTo("CAST(i AS SIGNED)").hasType(NUMBER);
        assertTerm(Func.CAST_AS_SIGNED.apply(FakeColumn.STR)).isEqualTo("CAST(s AS SIGNED)").hasType(NUMBER);
        assertTerm(Func.CAST_AS_CHAR.apply(FakeColumn.FOO)).isEqualTo("CAST(foo AS CHAR)").hasType(STRING);
        assertTerm(Func.CAST_AS_CHAR.apply(FakeColumn.INT)).isEqualTo("CAST(i AS CHAR)").hasType(STRING);
        assertTerm(Func.CAST_AS_CHAR.apply(FakeColumn.STR)).isEqualTo("CAST(s AS CHAR)").hasType(STRING);
    }

    @Test
    public void coalesce() {
        assertTerm(Func.COALESCE.apply(NULL, num(1)))
            .isEqualTo("coalesce(NULL, 1)")
            .hasType(WILDCARD);
        assertTerm(Func.COALESCE3.apply(NULL, num(1), literal("foo")))
            .isEqualTo("coalesce(NULL, 1, 'foo')")
            .hasType(WILDCARD);
        assertTerm(Func.COALESCE4.apply(NULL, NULL, num(1), FakeColumn.FOO))
            .isEqualTo("coalesce(NULL, NULL, 1, foo)")
            .hasType(WILDCARD);
        assertTerm(Func.COALESCE5.apply(NULL, NULL, num(1), NULL, FakeColumn.FOO))
            .isEqualTo("coalesce(NULL, NULL, 1, NULL, foo)")
            .hasType(WILDCARD);
    }
}
