package io.spbx.orm.api.entity;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import io.spbx.orm.api.query.Where;
import io.spbx.orm.testing.FakeEnumColumn;
import io.spbx.orm.testing.MockingJdbc;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.SQLException;
import java.util.EnumMap;

import static io.spbx.orm.api.query.Shortcuts.*;
import static io.spbx.orm.testing.MockingJdbc.assertThat;
import static io.spbx.util.testing.TestingBasics.listOf;
import static io.spbx.util.testing.TestingBasics.mapOf;

@Tag("fast")
public class EntityDataTest {
    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideValues(Scenario scenario) throws SQLException {
        EntityData<?> entityData = scenario.toEntityData(11, 12);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideValues(statement);
        assertThat(statement).withParams().equalExactly(scenario.typed(11), scenario.typed(12));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideValues_with_query_without_args(Scenario scenario) throws SQLException {
        EntityData<?> entityData = scenario.toEntityData(11, 12);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideValues(statement, Where.of(between(FakeEnumColumn.INT, num(0), num(9))));
        assertThat(statement).withParams().equalExactly(scenario.typed(11), scenario.typed(12));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void provideValues_with_query_args(Scenario scenario) throws SQLException {
        EntityData<?> entityData = scenario.toEntityData(11, 12);
        MockPreparedStatement statement = MockingJdbc.mockPreparedStatement();
        entityData.provideValues(statement,
            Where.and(lookupBy(FakeEnumColumn.INT, var(777)), like(FakeEnumColumn.FOO, var("s"))));
        assertThat(statement).withParams().equalExactly(scenario.typed(11), scenario.typed(12), 777, "s");
    }

    private enum Scenario {
        INTS,
        LONGS,
        MAP,
        ENUM_MAP;

        public @NotNull EntityData<?> toEntityData(int a, int b) {
            return switch (this) {
                case INTS -> new EntityIntData(listOf(FakeEnumColumn.FOO, FakeEnumColumn.INT), IntArrayList.from(a, b));
                case LONGS -> new EntityLongData(listOf(FakeEnumColumn.FOO, FakeEnumColumn.INT), LongArrayList.from(a, b));
                case MAP -> new EntityColumnMap<>(mapOf(FakeEnumColumn.FOO, a, FakeEnumColumn.INT, b));
                case ENUM_MAP -> new EntityColumnMap<>(new EnumMap<>(mapOf(FakeEnumColumn.FOO, a, FakeEnumColumn.INT, b)));
            };
        }

        public Object typed(Object value) {
            return switch (this) {
                case INTS -> ((Number) value).intValue();
                case LONGS -> ((Number) value).longValue();
                case MAP, ENUM_MAP -> value;
            };
        }
    }
}
