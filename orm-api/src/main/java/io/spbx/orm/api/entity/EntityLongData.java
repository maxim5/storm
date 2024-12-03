package io.spbx.orm.api.entity;

import com.carrotsearch.hppc.LongContainer;
import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link EntityData} implementation which stores the only-longs row using a {@link LongContainer}.
 * The statement is updated via {@link PreparedStatement#setLong(int, long)} from index 0.
 */
@Immutable
public record EntityLongData(@NotNull List<Column> columns, @NotNull LongContainer data) implements EntityData<LongContainer> {
    public EntityLongData {
        assert !columns.isEmpty() : "Entity data is empty: columns=%s, data=%s".formatted(columns, data);
        assert columns.size() == data.size() : "Entity columns do not match the data: %s vs %s".formatted(columns, data);
    }

    @Override
    public int provideValues(@NotNull PreparedStatement statement) throws SQLException {
        return QueryRunner.setPreparedParams(statement, data);
    }
}
