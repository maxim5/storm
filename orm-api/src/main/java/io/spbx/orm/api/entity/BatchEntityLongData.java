package io.spbx.orm.api.entity;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongContainer;
import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.query.Column;
import io.spbx.orm.api.query.Contextual;
import io.spbx.util.base.Unchecked;
import io.spbx.util.prima.extern.hppc.HppcLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link BatchEntityData} implementation which stores the only-int row set using a single {@link LongContainer} and
 * splits it into {@link LongArrayList} chunks.
 * <p>
 * The statement is updated via {@link PreparedStatement#setLong(int, long)} from index 0.
 */
public record BatchEntityLongData(@NotNull List<Column> columns,
                                  @NotNull LongContainer values) implements BatchEntityData<LongArrayList> {
    public BatchEntityLongData {
        assert !columns.isEmpty() : "Entity data batch columns are empty: columns=%s, data=%s".formatted(columns, values);
        assert !values.isEmpty() : "Entity data batch empty: columns=%s, data=%s".formatted(columns, values);
        assert values.size() % columns.size() == 0 :
            "Entity values don't match the columns size: columns=%s, data=%s".formatted(columns, values);
    }

    @Override
    public void provideBatchValues(@NotNull PreparedStatement statement,
                                   @Nullable Contextual<?, LongArrayList> contextual) throws SQLException {
        int dataSize = dataSize();
        HppcLong.iterateChunks(values, dataSize, Unchecked.Consumers.rethrow(chunk -> {
            QueryRunner.setPreparedParams(statement, chunk);
            if (contextual != null) {
                contextual.resolveQueryArgs(chunk).setPreparedParams(statement, /*index=*/ dataSize);
            }
            statement.addBatch();
        }));
    }
}
