package io.spbx.orm.api.entity;

import io.spbx.orm.api.query.HasArgs;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Holds the row data associated with the columns. The data can form the whole Entity (i.e. the whole row) or
 * can be a subset of an Entity (if the rest of the values have defaults).
 * <p>
 * {@code EntityData} can store the values more efficiently than an Entity. For example, if the data is all int values,
 * it can be stored in a single int array. The main requirement for the implementations is ability to
 * update the {@link PreparedStatement} with {@code set} methods.
 * <p>
 * Example data:
 * {@snippet lang="TEXT" :
 *     Width | Height
 *     --------------
 *     100   | 200
 * }
 * This row can be represented by an array of 2.
 *
 * @param <D> the underlying type used by implementations to store the row
 */
public interface EntityData<D> extends ColumnSet {
    /**
     * Returns the raw data.
     */
    @NotNull D data();

    /**
     * Updates the JDBC {@code statement} with the parameters from the data of this instance.
     * @return the number of added params.
     */
    int provideValues(@NotNull PreparedStatement statement) throws SQLException;

    /**
     * Updates the JDBC {@code statement} with the parameters from the data of this instance and then
     * parameters of {@code hasArgs}.
     * @return total number of added params.
     */
    default int provideValues(@NotNull PreparedStatement statement, @NotNull HasArgs hasArgs) throws SQLException {
        int added = provideValues(statement);
        return hasArgs.args().setPreparedParams(statement, added);
    }
}
