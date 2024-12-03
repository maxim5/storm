package io.spbx.orm.api.query;

import io.spbx.orm.testing.AssertSql;
import io.spbx.orm.testing.FakeColumn;
import io.spbx.util.base.annotate.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
public class SelectJoinTest {
    @Test
    public void select_join_on_simple() {
        SelectJoin query = SelectJoin.from("A")
            .select(FakeColumn.FOO.fullFrom("A"))
            .joinOn(JoinOn.of(JoinType.LEFT_JOIN, "B", "A", FakeColumn.INT))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT A.foo
                FROM A
                LEFT JOIN B
                ON A.i = B.i
                """)
            .containsNoArgs();
    }

    @Test
    public void select_left_join_on_simple() {
        SelectJoin query = SelectJoin.from("A")
            .select(FakeColumn.FOO.fullFrom("A"))
            .leftJoinOn(FakeColumn.INT.fullFrom("Z"))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT A.foo
                FROM A
                LEFT JOIN Z
                ON A.i = Z.i
                """)
            .containsNoArgs();
    }

    @CheckReturnValue
    private static @NotNull SelectJoinSubject assertThat(@NotNull SelectJoin query) {
        return new SelectJoinSubject(query);
    }

    private static class SelectJoinSubject extends AssertSql.UnitSubject<SelectJoinSubject> {
        private final SelectJoin query;

        private SelectJoinSubject(@NotNull SelectJoin query) {
            super(query);
            this.query = query;
        }

        public @NotNull SelectJoinSubject hasConsistentIntern() {
            AssertSql.assertThat((Unit) query.intern()).isEqualTo(query);
            return this;
        }

        public @NotNull SelectJoinSubject hasConsistentBuilder() {
            AssertSql.assertThat(query.toBuilder().build()).isEqualTo(query);
            return this;
        }
    }
}
