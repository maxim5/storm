package io.spbx.orm.api.entity;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import io.spbx.orm.api.query.Contextual;
import io.spbx.orm.api.query.TermType;
import io.spbx.orm.api.query.Where;
import io.spbx.orm.testing.FakeEnumColumn;
import io.spbx.orm.testing.MockingJdbc;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.SQLException;
import java.util.Map;

import static io.spbx.orm.api.query.Shortcuts.*;
import static io.spbx.orm.testing.MockingJdbc.assertThat;
import static io.spbx.util.collect.BasicIterables.asList;
import static io.spbx.util.testing.TestingBasics.listOf;
import static io.spbx.util.testing.TestingBasics.mapOf;

@Tag("fast")
public class BatchEntityDataTest {
    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideBatchValues_without_query(Scenario scenario) throws SQLException {
        BatchEntityData<?> entityData = scenario.toEntityData(11, 12, 13, 14);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideBatchValues(statement, null);
        assertThat(statement).withParamsBatch().hasSize(2);
        assertThat(statement).withParamsBatch().batchAt(0).equalExactly(scenario.typed(11), scenario.typed(12));
        assertThat(statement).withParamsBatch().batchAt(1).equalExactly(scenario.typed(13), scenario.typed(14));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideBatchValues_with_query_without_args(Scenario scenario) throws SQLException {
        BatchEntityData<?> entityData = scenario.toEntityData(11, 12, 13, 14);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideBatchValues(statement, Contextual.resolved(
            Where.of(between(FakeEnumColumn.INT, num(0), num(9))))
        );
        assertThat(statement).withParamsBatch().hasSize(2);
        assertThat(statement).withParamsBatchAt(0).equalExactly(scenario.typed(11), scenario.typed(12));
        assertThat(statement).withParamsBatchAt(1).equalExactly(scenario.typed(13), scenario.typed(14));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideBatchValues_with_query_args_one_unresolved(Scenario scenario) throws SQLException {
        BatchEntityData<?> entityData = scenario.toEntityData(11, 12, 13, 14);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideBatchValues(statement, Contextual.resolvingByOrderedList(
            Where.and(lookupBy(FakeEnumColumn.INT, unresolved("U", TermType.NUMBER)), like(FakeEnumColumn.FOO, var("s"))),
            batch -> listOf(scenario.extract(batch, 0))  // takes first
        ));
        assertThat(statement).withParamsBatch().hasSize(2);
        assertThat(statement).withParamsBatchAt(0)
            .equalExactly(scenario.typed(11), scenario.typed(12), scenario.typed(11), "s");
        assertThat(statement).withParamsBatchAt(1)
            .equalExactly(scenario.typed(13), scenario.typed(14), scenario.typed(13), "s");
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideBatchValues_with_query_args_two_unresolved(Scenario scenario) throws SQLException {
        BatchEntityData<?> entityData = scenario.toEntityData(11, 12, 13, 14);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideBatchValues(statement, Contextual.resolvingByOrderedList(
            Where.of(between(FakeEnumColumn.INT, unresolved("U", TermType.NUMBER), unresolved("U", TermType.NUMBER))),
            batch -> listOf(scenario.extract(batch, 1), scenario.extract(batch, 0))  // takes first and second
        ));
        assertThat(statement).withParamsBatch().hasSize(2);
        assertThat(statement).withParamsBatchAt(0)
            .equalExactly(scenario.typed(11), scenario.typed(12), scenario.typed(12), scenario.typed(11));
        assertThat(statement).withParamsBatchAt(1)
            .equalExactly(scenario.typed(13), scenario.typed(14), scenario.typed(14), scenario.typed(13));
    }

    private enum Scenario {
        INTS,
        LONGS,
        LIST_OF_ENUM_MAP;

        public @NotNull BatchEntityData<?> toEntityData(int a, int b, int c, int d) {
            return switch (this) {
                case INTS -> new BatchEntityIntData(
                    listOf(FakeEnumColumn.FOO, FakeEnumColumn.INT),
                    IntArrayList.from(a, b, c, d)
                );
                case LONGS -> new BatchEntityLongData(
                    listOf(FakeEnumColumn.FOO, FakeEnumColumn.INT),
                    LongArrayList.from(a, b, c, d)
                );
                case LIST_OF_ENUM_MAP -> new BatchEntityDataList<>(listOf(
                    new EntityColumnMap<>(mapOf(FakeEnumColumn.FOO, a, FakeEnumColumn.INT, b)),
                    new EntityColumnMap<>(mapOf(FakeEnumColumn.FOO, c, FakeEnumColumn.INT, d))
                ));
            };
        }

        public Object typed(Object value) {
            return switch (this) {
                case INTS -> ((Number) value).intValue();
                case LONGS -> ((Number) value).longValue();
                case LIST_OF_ENUM_MAP -> value;
            };
        }

        private Object extract(Object batch, int index) {
            return switch (this) {
                case INTS -> ((IntArrayList) batch).get(index);
                case LONGS -> ((LongArrayList) batch).get(index);
                case LIST_OF_ENUM_MAP -> asList(((Map<?, ?>) batch).values()).get(index);
            };
        }
    }
}
