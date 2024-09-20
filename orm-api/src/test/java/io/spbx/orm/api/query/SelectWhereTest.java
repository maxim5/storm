package io.spbx.orm.api.query;

import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.api.Engine;
import io.spbx.orm.testing.AssertSql;
import io.spbx.orm.testing.FakeColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.api.query.Shortcuts.num;
import static io.spbx.orm.testing.AssertSql.assertReprThrows;

@Tag("fast")
public class SelectWhereTest {
    @Test
    public void select_column_from_table() {
        SelectWhere query = SelectWhere.from("table").select(FakeColumn.INT).build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                """)
            .containsNoArgs();
    }

    @Test
    public void select_few_columns_from_table() {
        SelectWhere query = SelectWhere.from("table").select(FakeColumn.INT, FakeColumn.FOO).build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i, foo
                FROM table
                """)
            .containsNoArgs();
    }

    @Test
    public void select_function_from_table() {
        SelectWhere query = SelectWhere.from("table").select(Func.COUNT.apply(FakeColumn.INT)).build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT count(i)
                FROM table
                """)
            .containsNoArgs();
    }

    @Test
    public void select_const_from_table() {
        SelectWhere query = SelectWhere.from("table").select(num(77)).build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT 77
                FROM table
                """)
            .containsNoArgs();
    }

    @Test
    public void select_column_from_table_where() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .where(Where.of(FakeColumn.FOO.bool()))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                WHERE foo
                """)
            .containsNoArgs();
    }

    @Test
    public void select_column_from_table_order_by() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .orderBy(OrderBy.of(FakeColumn.STR, Order.ASC))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                ORDER BY s ASC
                """)
            .containsNoArgs();
    }

    @Test
    public void select_column_from_table_offset() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Offset.of(5))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                OFFSET ?
                """)
            .containsArgsExactly(5);
    }

    @Test
    public void select_column_from_table_limit() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Limit.of(10))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                LIMIT ?
                """)
            .containsArgsExactly(10);
    }

    @Test
    public void select_column_from_table_pagination_limit() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Pagination.ofOffset(3, 5), Engine.SQLite)
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                LIMIT ?
                OFFSET ?
                """)
            .containsArgsExactly(3, 5);
    }

    @Test
    public void select_column_from_table_pagination_fetch_next() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Pagination.ofOffset(3, 5), Engine.Oracle)
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i
                FROM table
                FETCH NEXT ? ROWS ONLY
                OFFSET ?
                """)
            .containsArgsExactly(3, 5);
    }

    @Test
    public void select_invalid() {
        assertReprThrows(() -> SelectWhere.from("table").build());
        assertReprThrows(() -> SelectWhere.from("table").with(Limit.of(5)).build());
        assertReprThrows(() -> SelectWhere.from("table").select(num(1)).with(Limit.of(5)).with(Limit.of(8)).build());
        assertReprThrows(() -> SelectWhere.from("table").select(num(1)).with(Offset.of(3)).with(Offset.of(9)).build());
    }

    @CheckReturnValue
    private static @NotNull SelectWhereSubject assertThat(@NotNull SelectWhere query) {
        return new SelectWhereSubject(query);
    }

    private static class SelectWhereSubject extends AssertSql.UnitSubject<SelectWhereSubject> {
        private final SelectWhere query;

        private SelectWhereSubject(@NotNull SelectWhere query) {
            super(query);
            this.query = query;
        }

        public @NotNull SelectWhereSubject hasConsistentIntern() {
            AssertSql.assertThat((Unit) query.intern()).isEqualTo(query);
            return this;
        }

        public @NotNull SelectWhereSubject hasConsistentBuilder() {
            AssertSql.assertThat(query.toBuilder().build()).isEqualTo(query);
            return this;
        }
    }
}
