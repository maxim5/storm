package io.spbx.orm.api.entity;

import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * A {@link EntityData} implementation which stores the arbitrary row using a {@link Map}.
 * It is recommended use a deterministically-ordered map,
 * such as {@link java.util.EnumMap} or {@link java.util.LinkedHashMap}, to avoid confusion with the params order.
 * <p>
 * The statement is updated via {@link PreparedStatement#setObject(int, Object)} from index 0.
 */
public record EntityColumnMap<C extends Column>(@NotNull Map<C, ?> map) implements EntityData<Map<C, ?>> {
    public EntityColumnMap {
        assert !map.isEmpty() : "Entity data is empty: " + map;
    }

    @Override
    public @NotNull Set<C> columns() {
        return map.keySet();
    }

    @Override
    public @NotNull Map<C, ?> data() {
        return map;
    }

    @Override
    public int provideValues(@NotNull PreparedStatement statement) throws SQLException {
        return QueryRunner.setPreparedParams(statement, map.values());
    }
}
