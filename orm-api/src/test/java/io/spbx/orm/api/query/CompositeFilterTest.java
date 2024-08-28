package io.spbx.orm.api.query;

import io.spbx.orm.api.Engine;
import io.spbx.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.testing.AssertSql.assertThat;

public class CompositeFilterTest {
    @Test
    public void with_where() {
        CompositeFilter filter = CompositeFilter.builder().with(Where.of(FakeColumn.FOO.bool())).build();
        assertThat(filter).matches("WHERE foo").containsNoArgs();
    }

    @Test
    public void with_where_two_terms() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(Where.of(FakeColumn.FOO.bool()))
            .with(Where.of(FakeColumn.FOO.bool()))
            .build();
        assertThat(filter).matches("WHERE foo AND foo").containsNoArgs();
    }

    @Test
    public void with_order_by() {
        CompositeFilter filter = CompositeFilter.builder().with(OrderBy.of(FakeColumn.INT, Order.ASC)).build();
        assertThat(filter).matches("ORDER BY i ASC").containsNoArgs();
    }

    @Test
    public void with_order_by_two_terms() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(OrderBy.of(FakeColumn.INT, Order.ASC))
            .with(OrderBy.of(FakeColumn.FOO, Order.DESC))
            .build();
        assertThat(filter).matches("ORDER BY i ASC, foo DESC").containsNoArgs();
    }

    @Test
    public void with_limit() {
        CompositeFilter filter = CompositeFilter.builder().with(Limit.of(55)).build();
        assertThat(filter).matches("LIMIT ?").containsArgsExactly(55);
    }

    @Test
    public void with_offset() {
        CompositeFilter filter = CompositeFilter.builder().with(Offset.of(88)).build();
        assertThat(filter).matches("OFFSET ?").containsArgsExactly(88);
    }

    @Test
    public void with_pagination_offset_sqlite() {
        CompositeFilter filter = CompositeFilter.builder().with(Pagination.ofOffset(3, 5), Engine.SQLite).build();
        assertThat(filter)
            .matches("""
                LIMIT ?
                OFFSET ?
                """)
            .containsArgsExactly(3, 5);
    }

    @Test
    public void with_pagination_offset_oracle() {
        CompositeFilter filter = CompositeFilter.builder().with(Pagination.ofOffset(3, 5), Engine.Oracle).build();
        assertThat(filter)
            .matches("""
                FETCH NEXT ? ROWS ONLY
                OFFSET ?
                """)
            .containsArgsExactly(3, 5);
    }

    @Test
    public void with_pagination_of_column_sqlite() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(Pagination.ofColumn(FakeColumn.FOO.makeVar(99), Order.ASC, 10), Engine.SQLite)
            .build();
        assertThat(filter)
            .matches("""
                WHERE foo > ?
                LIMIT ?
                """)
            .containsArgsExactly(99, 10);
    }

    @Test
    public void with_pagination_first_page_sqlite() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(Pagination.firstPage(777), Engine.SQLite)
            .build();
        assertThat(filter).matches("LIMIT ?").containsArgsExactly(777);
    }
}
