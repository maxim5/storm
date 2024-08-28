package io.spbx.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongContainer;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import io.spbx.orm.api.query.Args;
import io.spbx.orm.api.query.DataDefinitionQuery;
import io.spbx.orm.api.query.SelectQuery;
import io.spbx.orm.api.tx.InTransaction;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.util.func.ThrowFunction;
import io.spbx.util.func.ThrowSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * A utility wrapper-class of a {@code Connection} which provides a more convenient API methods for
 * query preparation and running.
 */
public class QueryRunner {
    private final Connection connection;

    public QueryRunner(@NotNull Connection connection) {
        this.connection = connection;
    }

    /*package*/ @NotNull Connection connection() {
        return connection;
    }

    @CheckReturnValue
    public @NotNull InTransaction<QueryRunner> tx() {
        return new InTransaction<>(connection, this);
    }

    @CheckReturnValue
    public @NotNull InTransaction<DbAdmin> adminTx() {
        return new InTransaction<>(connection, DbAdmin.ofFixed(this));
    }

    // Run SelectQuery

    public @Nullable Object runAndGet(@NotNull SelectQuery query) {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getObject(1) : null;
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public <E> @Nullable E runAndGet(@NotNull SelectQuery query,
                                     @NotNull ThrowFunction<ResultSet, E, SQLException> converter) {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? converter.apply(resultSet) : null;
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public @Nullable String runAndGetString(@NotNull SelectQuery query) {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public int runAndGetInt(@NotNull SelectQuery query, int def) {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : def;
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public long runAndGetLong(@NotNull SelectQuery query, long def) {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong(1) : def;
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public void run(@NotNull SelectQuery query,
                    @NotNull ThrowConsumer<ResultSet, SQLException> resultsConsumer) {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            resultsConsumer.accept(resultSet);
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public void forEach(@NotNull SelectQuery query, @NotNull ThrowConsumer<ResultSet, SQLException> rowConsumer) {
        run(query, result -> {
           while (result.next()) {
                rowConsumer.accept(result);
           }
        });
    }

    @MustBeClosed
    public <E> @NotNull ResultSetIterator<E> iterate(@NotNull SelectQuery query,
                                                     @NotNull ResultSetIterator.Converter<E> converter) {
        try {
            return ResultSetIterator.of(prepareQuery(query).executeQuery(), converter);
        } catch (SQLException e) {
            throw new QueryException("Failed to execute select query", query.repr(), query.args(), e);
        }
    }

    public <E> @NotNull List<E> fetchAll(@NotNull SelectQuery query, @NotNull ResultSetIterator.Converter<E> converter) {
        try (ResultSetIterator<E> iterator = iterate(query, converter)) {
            return Lists.newArrayList(iterator);
        }
    }

    public @NotNull IntArrayList fetchIntColumn(@NotNull SelectQuery query) {
        IntArrayList list = new IntArrayList();
        forEach(query, resultSet -> list.add(resultSet.getInt(1)));
        return list;
    }

    public @NotNull LongArrayList fetchLongColumn(@NotNull SelectQuery query) {
        LongArrayList list = new LongArrayList();
        forEach(query, resultSet -> list.add(resultSet.getLong(1)));
        return list;
    }

    //  Run PreparedStatement

    public void forEach(@NotNull ThrowSupplier<PreparedStatement, SQLException> preparedProvider,
                        @NotNull ThrowConsumer<ResultSet, SQLException> rowConsumer) throws SQLException {
        try (PreparedStatement statement = preparedProvider.get();
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rowConsumer.accept(resultSet);
            }
        }
    }

    public @NotNull IntArrayList fetchIntColumn(@NotNull ThrowSupplier<PreparedStatement, SQLException> provider) throws SQLException {
        IntArrayList list = new IntArrayList();
        forEach(provider, resultSet -> list.add(resultSet.getInt(1)));
        return list;
    }

    public @NotNull LongArrayList fetchLongColumn(@NotNull ThrowSupplier<PreparedStatement, SQLException> provider) throws SQLException {
        LongArrayList list = new LongArrayList();
        forEach(provider, resultSet -> list.add(resultSet.getLong(1)));
        return list;
    }

    // Prepare Query

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull Args args) throws SQLException {
        assert args.isAllResolved() : "Query contains unresolved args: query=`%s` args=`%s`".formatted(sql, args);
        PreparedStatement prepared = connection.prepareStatement(sql);
        args.setPreparedParams(prepared);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @Nullable Object param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, int param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setInt(1, param);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, long param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setLong(1, param);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql,
                                                   @Nullable Object param1,
                                                   @Nullable Object param2) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql,
                                                   @Nullable Object param1,
                                                   @Nullable Object param2,
                                                   @Nullable Object param3) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        prepared.setObject(3, param3);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql,
                                                   @Nullable Object @NotNull ... params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        setPreparedParams(prepared, params);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull Iterable<?> params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        setPreparedParams(prepared, params);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull IntContainer params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        setPreparedParams(prepared, params);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull LongContainer params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        setPreparedParams(prepared, params);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull SelectQuery query) throws SQLException {
        return prepareQuery(query.repr(), query.args());
    }

    // Set params

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                        @Nullable Object @NotNull ... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return params.length;
    }

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                        @NotNull Iterable<?> params) throws SQLException {
        return setPreparedParams(statement, params, 0);
    }

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                        @NotNull Iterable<?> params,
                                        int index) throws SQLException {
        for (Object value : params) {
            statement.setObject(++index, value);
        }
        return index;
    }

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                        @NotNull IntContainer params) throws SQLException {
        return setPreparedParams(statement, params, 0);
    }

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                        @NotNull IntContainer params,
                                        int index) throws SQLException {
        for (IntCursor cursor : params) {
            statement.setInt(++index, cursor.value);
        }
        return index;
    }

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                         @NotNull LongContainer params) throws SQLException {
        return setPreparedParams(statement, params, 0);
    }

    public static int setPreparedParams(@NotNull PreparedStatement statement,
                                         @NotNull LongContainer params,
                                         int index) throws SQLException {
        for (LongCursor cursor : params) {
            statement.setLong(++index, cursor.value);
        }
        return index;
    }

    // Run Updates

    public int runUnpreparedUpdate(@NotNull String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    public int runUpdate(@NotNull DataDefinitionQuery query) throws SQLException {
        return runUpdate(query.repr(), query.args());
    }

    public int runUpdate(@NotNull String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @NotNull Args param) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object param) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, int param) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, long param) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object param1, @Nullable Object param2) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param1, param2)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql,
                         @Nullable Object param1,
                         @Nullable Object param2,
                         @Nullable Object param3) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param1, param2, param3)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object @NotNull ... params) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, params)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @NotNull Iterable<?> params) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, params)) {
            return prepared.executeUpdate();
        }
    }

    public @NotNull AutoIncResult runAutoIncUpdate(@NotNull String sql,
                                                   @Nullable Object @NotNull ... params) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPreparedParams(prepared, params);

            // See https://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
            int changedRowCount = prepared.executeUpdate();
            try (ResultSet generatedKeys = prepared.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new AutoIncResult(changedRowCount, generatedKeys.getLong(1));
                } else {
                    throw new QueryException("No generated keys returned. Changed row count=%d".formatted(changedRowCount), sql, params);
                }
            }
        }
    }

    public record AutoIncResult(int changedRowCount, long lastId) {
    }

    public int @NotNull[] runUpdateBatch(@NotNull String sql,
                                          @NotNull Collection<Object[]> paramsBatch) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql)) {
            setBatchPreparedParams(prepared, paramsBatch);
            return prepared.executeBatch();
        }
    }

    public static void setBatchPreparedParams(@NotNull PreparedStatement prepared,
                                              @NotNull Collection<Object[]> paramsBatch) throws SQLException {
        for (Object[] params : paramsBatch) {
            setPreparedParams(prepared, params);
            prepared.addBatch();
        }
    }

    public int @NotNull[] runUpdateBatch(@NotNull String sql,
                                          @NotNull Iterable<? extends Iterable<?>> paramsBatch) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql)) {
            setBatchPreparedParams(prepared, paramsBatch);
            return prepared.executeBatch();
        }
    }

    public static void setBatchPreparedParams(@NotNull PreparedStatement prepared,
                                              @NotNull Iterable<? extends Iterable<?>> paramsBatch) throws SQLException {
        for (Iterable<?> params : paramsBatch) {
            setPreparedParams(prepared, params);
            prepared.addBatch();
        }
    }
}
