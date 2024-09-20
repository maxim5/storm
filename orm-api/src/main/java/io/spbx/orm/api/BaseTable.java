package io.spbx.orm.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import io.spbx.orm.api.entity.BatchEntityData;
import io.spbx.orm.api.entity.EntityData;
import io.spbx.orm.api.query.CompositeFilter;
import io.spbx.orm.api.query.Contextual;
import io.spbx.orm.api.query.Filter;
import io.spbx.orm.api.query.LimitClause;
import io.spbx.orm.api.query.Offset;
import io.spbx.orm.api.query.Where;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * A base interface providing common API for table operations. Each {@link BaseTable} instance is working in context
 * of a JDBC {@link java.sql.Connection} or {@link Connector}. {@link BaseTable} implementations are stateless and
 * essentially represent thin clients of DBMS API. Multiple instances of the same type can operate concurrently.
 * <p>
 * Most read/write operations are performed via <i>entity</i>, which is a Java representation of a row in the table.
 * The implementation is responsible for constructing entities from JDBC objects and back.
 * <p>
 * Some high-performance operations are performed via <i>data</i> classes, which store the same information as entities,
 * (though possibly incomplete) but in a more time and memory efficient way.
 *
 * @param <E> the entity type
 * @see EntityData
 * @see BatchEntityData
 */
public interface BaseTable<E> extends Iterable<E>, HasEngine, HasRunner {
    // Base

    /**
     * Returns the {@link TableMeta} of this table.
     */
    @NotNull TableMeta meta();

    /**
     * Returns the admin interface which can control this and other tables and system in general.
     */
    @NotNull DbAdmin admin();

    /**
     * Returns a copy of {@link BaseTable} instance with custom {@code follow} level for read operations in the table.
     * Default value is {@link ReadFollow#NO_FOLLOW}.
     */
    @NotNull BaseTable<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    // Size

    /**
     * Returns the table size (result of {@code SELECT COUNT(*)} query).
     */
    int count();

    /**
     * Returns the count of the rows matching the {@code filter}
     * (result of {@code SELECT COUNT(*) ...[filter]} query).
     */
    int count(@NotNull Filter filter);

    /**
     * Returns whether the table is empty, i.e. contains 0 rows.
     */
    default boolean isEmpty() {
        return !isNotEmpty();
    }

    /**
     * Returns whether the table is not empty, i.e. contains at least 1 row.
     */
    default boolean isNotEmpty() {
        return count() > 0;
    }

    /**
     * Returns whether the table contains at least one row matching the {@code where} condition.
     */
    default boolean exists(@NotNull Where where) {
        return count(where) > 0;
    }

    // Iteration

    /**
     * Returns an iterator over all entries in the table.
     * <b>Important</b>: the caller is responsible for closing the iterator:
     * {@snippet lang="java" :
     *     try (ResultSetIterator<Entity> iterator = table.iterator()) {
     *         iterator.forEachRemaining(action);
     *     }
     * }
     */
    @Override
    @MustBeClosed
    @NotNull ResultSetIterator<E> iterator();

    /**
     * Returns an iterator over the entries matching the {@code filter}.
     * <b>Important</b>: the caller is responsible for closing the iterator:
     * {@snippet lang="java" :
     *     try (ResultSetIterator<Entity> iterator = table.iterator(filter)) {
     *         iterator.forEachRemaining(action);
     *     }
     * }
     */
    @MustBeClosed
    @NotNull ResultSetIterator<E> iterator(@NotNull Filter filter);

    /**
     * Iterates over all entries in the table and calls an {@code action} on each one.
     */
    @Override
    default void forEach(@NotNull Consumer<? super E> action) {
        try (ResultSetIterator<E> iterator = iterator()) {
            iterator.forEachRemaining(action);
        }
    }

    /**
     * Iterates over the entries matching the {@code filter} and calls an {@code action} on each one.
     */
    default void forEach(@NotNull Filter filter, @NotNull Consumer<? super E> action) {
        try (ResultSetIterator<E> iterator = iterator(filter)) {
            iterator.forEachRemaining(action);
        }
    }

    /**
     * Fetches the whole table into a list of entries.
     */
    default @NotNull List<E> fetchAll() {
        try (ResultSetIterator<E> iterator = iterator()) {
            return Lists.newArrayList(iterator);
        }
    }

    /**
     * Fetches all rows matching the {@code filter} into a list of entries.
     */
    default @NotNull List<E> fetchAllMatching(@NotNull Filter filter) {
        try (ResultSetIterator<E> iterator = iterator(filter)) {
            return Lists.newArrayList(iterator);
        }
    }

    /**
     * Fetches a single page of entries from the table. The page is determined by filtering params of the {@code clause}.
     * The result can be empty, and may or may not have the next page.
     */
    default @NotNull Page<E> fetchPage(@NotNull CompositeFilter clause) {
        List<E> items = fetchAllMatching(clause);
        Offset offset = clause.offset();
        LimitClause limit = clause.limit();
        boolean isFullPage = limit != null && items.size() == limit.limitValue();
        if (isFullPage) {
            PageToken pageToken = PageToken.ofOffset((offset != null ? offset.offsetValue() : 0) + limit.limitValue());
            return new Page<>(items, pageToken);
        }
        return new Page<>(items, null);
    }

    /**
     * Returns a single entry matching the {@code filter}, or null if none match.
     */
    default @Nullable E getFirstMatchingOrNull(@NotNull Filter filter) {
        try (ResultSetIterator<E> iterator = iterator(filter)) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    // INSERT

    /**
     * Inserts the {@code entity} into the table.
     *
     * @return the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int insert(@NotNull E entity);

    /**
     * Inserts the {@code entity} into the table, ignoring PK or FK conflicts.
     *
     * @return the number of affected rows in a table (1 if successful, 0 otherwise)
     * @throws QueryException if the insertion failed unexpectedly
     */
    @CanIgnoreReturnValue
    int insertIgnore(@NotNull E entity);

    /**
     * Inserts the {@code EntityData} into the table. The {@code data} row may or may not be complete.
     *
     * @return the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int insertData(@NotNull EntityData<?> data);

    /**
     * Inserts multiple entities into the table in batch mode.
     *
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int[] insertBatch(@NotNull Collection<? extends E> batch);

    /**
     * Inserts the {@code BatchEntityData} into the table. The {@code batchData} rows may or may not be complete.
     *
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int[] insertDataBatch(@NotNull BatchEntityData<?> batchData);

    // UPDATE

    /**
     * Updates the {@code entity} in the table given the {@code where} condition.
     *
     * @return the number of affected rows in a table (1 or more if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int updateWhere(@NotNull E entity, @NotNull Where where);

    /**
     * Updates the {@code EntityData} in the table given the {@code where} condition.
     *
     * @return the number of affected rows in a table (1 or more if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int updateDataWhere(@NotNull EntityData<?> data, @NotNull Where where);

    /**
     * Updates multiple rows in the table given the contextual {@code where} condition.
     * In order to pass correct query params, the condition gets resolved from each entity in the {@code batch}.
     *
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     * @see Contextual for arg resolution
     */
    @CanIgnoreReturnValue
    int[] updateWhereBatch(@NotNull Collection<? extends E> batch, @NotNull Contextual<Where, E> where);

    /**
     * Updates multiple rows in the table given the contextual {@code where} condition.
     * In order to pass correct query params, the condition gets resolved from each chunk in the {@code batchData}.
     *
     * @param <B> the type of batch used for context resolution
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     * @see Contextual for arg resolution
     */
    @CanIgnoreReturnValue
    <B> int[] updateDataWhereBatch(@NotNull BatchEntityData<B> batchData, @NotNull Contextual<Where, B> where);

    // INSERT OR UPDATE

    /**
     * Updates if exists or otherwise inserts the {@code entity} in the table.
     * The match is done via {@code where} condition. It is possible for the condition to match more than one row.
     *
     * @return the number of affected rows (either updated or inserted).
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    default int updateWhereOrInsert(@NotNull E entity, @NotNull Where where) {
        int updated = updateWhere(entity, where);
        if (updated == 0) {
            return insert(entity);
        }
        return updated;
    }

    /**
     * Updates if exists or otherwise inserts the {@link EntityData} in the table.
     * The match is done via {@code where} condition. It is possible for the condition to match more than one row.
     *
     * @return the number of affected rows (either updated or inserted).
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    default int updateWhereOrInsertData(@NotNull EntityData<?> data, @NotNull Where where) {
        int updated = updateDataWhere(data, where);
        if (updated == 0) {
            return insertData(data);
        }
        return updated;
    }

    // DELETE

    /**
     * Deletes the rows in the table matching the given {@code where} condition. Can delete all rows.
     *
     * @return the number of affected rows in a table
     */
    @CanIgnoreReturnValue
    int deleteWhere(@NotNull Where where);
}
