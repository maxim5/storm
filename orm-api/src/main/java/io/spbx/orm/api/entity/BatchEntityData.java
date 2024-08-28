package io.spbx.orm.api.entity;

import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.query.Contextual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Holds the batch of data associated with the columns. If a row is represented by an Entity, then this class
 * represents a {@code List<Entity>}, but can do it much more efficiently. For example, if the data is all int values,
 * the batch can be stored in a single int array.
 * <p>
 * The main requirement for the implementations is ability to split and add batches to the JDBC statement
 * using {@link PreparedStatement}'s {@code set} methods and {@link PreparedStatement#addBatch()}.
 * <p>
 * Instances of this class can be used to insert or update to the table using batch table API.
 * <p>
 * Example data:
 * <pre>
 *     Name  | Age
 *     -----------
 *     Maxim | 20
 *     Ivan  | 25
 *     Roman | 30
 * </pre>
 * <p>
 * This batch consists of 6 objects forming 3 rows by 2. The batch size (same as data size) is 2.
 * Given a statement, the parameters and {@link PreparedStatement#addBatch()} must be called 3 times.
 * <p>
 * In addition to the batch itself, it's possible to provide {@link Contextual} parameters to the statement.
 * This is necessary to handle queries like <code>"UPDATE users SET name=?, age=? WHERE name = ?"</code>, where
 * the parameter set includes not only the batch itself (<code>name</code>, <code>age</code>), but the context too
 * (<code>name</code>) to be used in a filter clause. Note that the filter clause may have its own args too.
 * <p>
 * This is handled this way: the <code>WHERE</code> query uses unresolved arguments for each context parameter. Then
 * for each batch, query args get resolved from the current batch and also added to the statement
 * (before {@link PreparedStatement#addBatch()} call).
 *
 * @param <B> the type used by implementations for each chunk (i.e. each row)
 * @see BaseTable#insertDataBatch(BatchEntityData)
 * @see BaseTable#updateDataWhereBatch(BatchEntityData, Contextual)
 */
public interface BatchEntityData<B> extends ColumnSet {
    /**
     * The size of each data row in this batch. Equals to the number of columns.
     */
    default int dataSize() {
        return columns().size();
    }

    /**
     * Updates the JDBC {@code statement} with the parameters from the data of this instance.
     * <p>
     * Optionally, a {@code contextual} can be passed in as well. In this case, {@code contextual} unresolved
     * args will be resolved from each chunk and added to the {@code statement}.
     */
    void provideBatchValues(@NotNull PreparedStatement statement,
                            @Nullable Contextual<?, B> contextual) throws SQLException;
}
