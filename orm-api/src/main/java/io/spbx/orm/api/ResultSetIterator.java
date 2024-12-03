package io.spbx.orm.api;

import io.spbx.util.base.annotate.MustBeClosed;
import io.spbx.util.base.error.Unchecked;
import io.spbx.util.base.tuple.Pair;
import io.spbx.util.func.ThrowFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.spbx.util.base.lang.EasyCast.castAny;

/**
 * An adapter that allows to iterate over the JDBC {@link ResultSet} via {@link Iterator}.
 * The {@link ResultSetIterator} takes in a {@code converter} to convert rows to {@code E} objects.
 * Note that {@link ResultSetIterator} is {@link Closeable} and is recommended to be used
 * with a {@code try-resource} syntax.
 */
public class ResultSetIterator<E> implements Iterator<E>, Closeable {
    private final ResultSet resultSet;
    private final boolean ownsStatement;
    private final Converter<E> converter;
    private final AtomicBoolean nextCalled = new AtomicBoolean();
    private final AtomicBoolean hasNextCached = new AtomicBoolean();

    public ResultSetIterator(@NotNull ResultSet resultSet, boolean ownsStatement, @NotNull Converter<E> converter) {
        this.resultSet = resultSet;
        this.ownsStatement = ownsStatement;
        this.converter = converter;
    }

    @MustBeClosed
    public static <E> @NotNull ResultSetIterator<E> of(@NotNull ResultSet resultSet, @NotNull Converter<E> converter) {
        return new ResultSetIterator<>(resultSet, true, converter);
    }

    @Override
    public boolean hasNext() {
        // switch to resultSet.isLast() when it's supported:
        // https://github.com/xerial/sqlite-jdbc/issues/682
        try {
            if (nextCalled.compareAndSet(false, true)) {
                hasNextCached.set(resultSet.next());
            }
            return hasNextCached.get();
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }
    }

    @Override
    public E next() {
        try {
            nextCalled.set(false);
            return converter.apply(resultSet);
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }
    }

    @Override
    public void remove() {
        try {
            resultSet.deleteRow();
        } catch (SQLException e) {
            Unchecked.rethrow(e);
        }
    }

    @Override
    public void close() {
        try {
            // Best-effort to close the statement (if owns). Notes:
            // - Calling the method close on a ResultSet object that is already closed is a no-op.
            // - Calling the method close on a Statement object that is already closed has no effect.
            Statement statement = ownsStatement ? getStatementOrNull() : null;
            if (statement != null) {
                statement.close();
            }
            resultSet.close();
        } catch (SQLException e) {
            Unchecked.rethrow(e);
        }
    }

    private @Nullable Statement getStatementOrNull() {
        try {
            return resultSet.getStatement();
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * A converter from a {@link ResultSet} to an {@code E} object.
     */
    @FunctionalInterface
    public interface Converter<E> extends ThrowFunction<ResultSet, E, SQLException> {
        /**
         * Converts the {@code resultSet} to the {@code E} entity object.
         * The implementation is expected to return a non-null result or throw an exception if impossible.
         */
        @Override
        @NotNull E apply(@NotNull ResultSet resultSet) throws SQLException;
    }

    /**
     * Returns the converter that takes the first column only (as an {@link Object}).
     */
    public static @NotNull Converter<Object> firstColumn() {
        return resultSet -> resultSet.getObject(1);
    }

    /**
     * Returns the converter that takes the first two columns only (as a pair of {@link Object}s).
     */
    public static @NotNull Converter<Pair<Object, Object>> twoColumns() {
        return resultSet -> castAny(Pair.of(resultSet.getObject(1), resultSet.getObject(2)));
    }
}
