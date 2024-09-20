package io.spbx.orm.api.query;

import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.testing.AssertSql;
import io.spbx.orm.testing.FakeColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.api.query.Func.*;
import static io.spbx.orm.api.query.Shortcuts.*;
import static io.spbx.orm.testing.AssertSql.assertReprThrows;

@Tag("fast")
public class SelectUnionTest {
    @Test
    public void select_where_union() {
        SelectWhere query1 = SelectWhere.from("foo").select(COUNT.apply(FakeColumn.INT)).build();
        SelectWhere query2 = SelectWhere.from("bar").select(num(777)).build();
        assertThat(query1.union(query2))
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT count(i)
                FROM foo
                UNION
                SELECT 777
                FROM bar
                """)
            .containsNoArgs()
            .isEqualTo(query1.toBuilder().union(query2).build());
    }

    @Test
    public void select_where_union_with_variables() {
        SelectWhere query1 = SelectWhere.from("foo").select(var(111), var(222)).build();
        SelectWhere query2 = SelectWhere.from("bar").select(var(333), var(444)).build();
        assertThat(query1.union(query2))
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT ?, ?
                FROM foo
                UNION
                SELECT ?, ?
                FROM bar
                """)
            .containsArgsExactly(111, 222, 333, 444)
            .isEqualTo(query1.toBuilder().union(query2).build());
    }

    @Test
    public void select_group_by_union() {
        SelectGroupBy query1 = SelectGroupBy.from("foo").select(FakeColumn.INT, COUNT.apply(STAR)).build();
        SelectGroupBy query2 = SelectGroupBy.from("bar").select(FakeColumn.FOO, MAX.apply(STAR)).build();
        assertThat(query1.union(query2))
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i, count(*)
                FROM foo
                GROUP BY i
                UNION
                SELECT foo, max(*)
                FROM bar
                GROUP BY foo
                """)
            .containsNoArgs();
    }

    @Test
    public void select_group_by_union_with_variables() {
        SelectGroupBy query1 = SelectGroupBy.from("foo")
            .select(FakeColumn.INT.namedAs("x"), COUNT.apply(FakeColumn.STR))
            .having(Having.of(CompareType.LE.compare(FakeColumn.INT, var(777))))
            .build();
        SelectGroupBy query2 = SelectGroupBy.from("bar")
            .select(FakeColumn.FOO.namedAs("y"), FIRST.apply(FakeColumn.STR))
            .where(Where.of(CompareType.GT.compare(FakeColumn.FOO, var(888))))
            .build();
        assertThat(query1.union(query2))
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i AS x, count(s)
                FROM foo
                GROUP BY x
                HAVING i <= ?
                UNION
                SELECT foo AS y, first(s)
                FROM bar
                WHERE foo > ?
                GROUP BY y
                """)
            .containsArgsExactly(777, 888);
    }

    @Test
    public void select_invalid() {
        assertReprThrows(() -> SelectUnion.builder().build());
        assertReprThrows(() -> {
            SelectWhere query = SelectWhere.from("foo").select(FakeColumn.INT).build();
            return SelectUnion.builder().with(query).build();
        });
        assertReprThrows(() -> {
            SelectWhere query1 = SelectWhere.from("foo").select(FakeColumn.INT, FakeColumn.FOO).build();
            SelectWhere query2 = SelectWhere.from("bar").select(num(777)).build();
            return SelectUnion.builder().with(query1).with(query2).build();
        });
        assertReprThrows(() -> {
            SelectWhere query1 = SelectWhere.from("foo").select(FakeColumn.INT).build();
            SelectWhere query2 = SelectWhere.from("bar").select(num(777), num(888)).build();
            return SelectUnion.builder().with(query1).with(query2).build();
        });
    }

    @CheckReturnValue
    private static @NotNull SelectUnionSubject assertThat(@NotNull SelectUnion query) {
        return new SelectUnionSubject(query);
    }

    private static class SelectUnionSubject extends AssertSql.UnitSubject<SelectUnionSubject> {
        private final SelectUnion query;

        private SelectUnionSubject(@NotNull SelectUnion query) {
            super(query);
            this.query = query;
        }

        public @NotNull SelectUnionSubject hasConsistentIntern() {
            AssertSql.assertThat((Unit) query.intern()).isEqualTo(query);
            return this;
        }

        public @NotNull SelectUnionSubject hasConsistentBuilder() {
            AssertSql.assertThat(query.toBuilder().build()).isEqualTo(query);
            return this;
        }
    }
}
