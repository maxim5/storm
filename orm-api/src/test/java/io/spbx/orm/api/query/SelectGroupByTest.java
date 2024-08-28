package io.spbx.orm.api.query;

import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.testing.AssertSql;
import io.spbx.orm.testing.FakeColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.api.query.Func.*;
import static io.spbx.orm.api.query.Shortcuts.STAR;
import static io.spbx.orm.testing.AssertSql.assertReprThrows;

public class SelectGroupByTest {
    @Test
    public void select_one_column_group_by_one() {
        SelectGroupBy query = SelectGroupBy.from("table").select(FakeColumn.INT, COUNT.apply(STAR)).build();
        assertThat(query)
            .hasConsistentBuilder()
            .hasConsistentIntern()
            .matches("""
                SELECT i, count(*)
                FROM table
                GROUP BY i
                """)
            .containsNoArgs();
    }

    @Test
    public void select_group_by_having() {
        SelectGroupBy query = SelectGroupBy.from("table")
            .select(FakeColumn.INT, COUNT.apply(STAR))
            .having(Having.of(FakeColumn.FOO.isNotNull()))
            .build();
        assertThat(query)
            .hasConsistentBuilder()
            .hasConsistentIntern()
            .matches("""
                SELECT i, count(*)
                FROM table
                GROUP BY i
                HAVING foo IS NOT NULL
                """)
            .containsNoArgs();
    }

    @Test
    public void select_one_column_group_by_two() {
        SelectGroupBy query = SelectGroupBy.from("table")
            .groupBy(FakeColumn.FOO, FakeColumn.STR)
            .aggregate(MAX.apply(FakeColumn.INT))
            .build();
        assertThat(query)
            .hasConsistentBuilder()
            .hasConsistentIntern()
            .matches("""
                SELECT foo, s, max(i)
                FROM table
                GROUP BY foo, s
                """)
            .containsNoArgs();
    }

    @Test
    public void select_invalid() {
        assertReprThrows(() -> SelectGroupBy.from("table").build());
        assertReprThrows(() -> SelectGroupBy.from("table").aggregate(SQUARE.apply(FakeColumn.INT)).build());
        assertReprThrows(() ->
            SelectGroupBy.from("table").groupBy(FakeColumn.FOO).aggregate(SQUARE.apply(FakeColumn.INT)).build());
    }

    @CheckReturnValue
    private static @NotNull SelectGroupBySubject assertThat(@NotNull SelectGroupBy query) {
        return new SelectGroupBySubject(query);
    }

    private static class SelectGroupBySubject extends AssertSql.UnitSubject<SelectGroupBySubject> {
        private final SelectGroupBy query;

        private SelectGroupBySubject(@NotNull SelectGroupBy query) {
            super(query);
            this.query = query;
        }

        public @NotNull SelectGroupBySubject hasConsistentIntern() {
            AssertSql.assertThat((Unit) query.intern()).isEqualTo(query);
            return this;
        }

        public @NotNull SelectGroupBySubject hasConsistentBuilder() {
            AssertSql.assertThat(query.toBuilder().build()).isEqualTo(query);
            return this;
        }
    }
}
