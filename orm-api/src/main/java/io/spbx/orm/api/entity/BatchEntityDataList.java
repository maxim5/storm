package io.spbx.orm.api.entity;

import io.spbx.orm.api.query.Column;
import io.spbx.orm.api.query.Contextual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * A {@link BatchEntityData} implementation which stores the arbitrary row set using a {@link List<EntityData>}.
 * <p>
 * The statement is updated via {@link EntityData#provideValues(PreparedStatement)}.
 *
 * @param <D> the underlying type used by implementations to store each {@code EntityData} (i.e., each chunk)
 */
public record BatchEntityDataList<D>(@NotNull List<EntityData<D>> batch) implements BatchEntityData<D> {
    public BatchEntityDataList {
        assert !batch.isEmpty() : "Entity batch is empty: " + batch;
        assert batch.stream().allMatch(data -> data.columns().equals(batch.getFirst().columns())) :
            "All batch data items must have the same columns: " + batch;
    }

    @Override
    public @NotNull Collection<? extends Column> columns() {
        return batch.getFirst().columns();
    }

    @Override
    public void provideBatchValues(@NotNull PreparedStatement statement,
                                   @Nullable Contextual<?, D> contextual) throws SQLException {
        int dataSize = dataSize();
        for (EntityData<D> entityData : batch) {
            entityData.provideValues(statement);
            if (contextual != null) {
                contextual.resolveQueryArgs(entityData.data()).setPreparedParams(statement, /*index=*/ dataSize);
            }
            statement.addBatch();
        }
    }
}
