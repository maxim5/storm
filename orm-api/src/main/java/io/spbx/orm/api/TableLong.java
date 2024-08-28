package io.spbx.orm.api;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongContainer;
import com.carrotsearch.hppc.LongObjectMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.orm.api.query.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides the API for table operations keyed by an {@code long} id and storing {@code E} entities.
 * <p>
 * Most methods of {@link TableObj} accepting an {@link Long} key have a more efficient alternative method
 * with native {@code long}.
 *
 * @param <E> the entity type
 */
public interface TableLong<E> extends TableObj<Long, E> {
    /**
     * Returns a copy of {@link TableLong} instance with custom {@code follow} level for read operations in the table.
     * Default value is {@link ReadFollow#NO_FOLLOW}.
     */
    @Override
    @NotNull TableLong<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    /**
     * Returns whether the table contains the entity matching the {@code key} (native {@code long} version).
     */
    boolean exists(long key);

    /**
     * Returns whether the table contains the entity matching the {@code key} ({@link Long} version).
     */
    @Override
    default boolean exists(@NotNull Long key) {
        return exists(key.longValue());
    }

    /**
     * Returns the entity given the specified {@code key} or null if not found (native {@code long} version).
     */
    @Nullable E getByPkOrNull(long key);

    /**
     * Returns the entity given the specified {@code key} or null if not found ({@link Long} version).
     */
    @Override
    default @Nullable E getByPkOrNull(@NotNull Long key) {
        return getByPkOrNull(key.longValue());
    }

    /**
     * Returns the entity given the specified {@code key} or throws if not found (native {@code long} version).
     */
    default @NotNull E getByPkOrDie(long key) {
        return requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    /**
     * Returns the entity given the specified {@code key} or throws if not found ({@link Long} version).
     */
    @Override
    default @NotNull E getByPkOrDie(@NotNull Long key) {
        return getByPkOrDie(key.longValue());
    }

    /**
     * Returns the optional entity given the specified {@code key} (native {@code long} version).
     */
    default @NotNull Optional<E> getOptionalByPk(long key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    /**
     * Returns the optional entity given the specified {@code key} ({@link Long} version).
     */
    @Override
    default @NotNull Optional<E> getOptionalByPk(@NotNull Long key) {
        return getOptionalByPk(key.longValue());
    }

    /**
     * Fetches and returns the long map of entities for an {@link LongContainer} of {@code keys}.
     * The implementations are likely to optimize this operation using batch DB requests.
     */
    @NotNull LongObjectMap<E> getBatchByPk(@NotNull LongContainer keys);

    /**
     * Fetches and returns the long array list of ids for entities that match specified {@code filter}.
     */
    @NotNull LongArrayList fetchPks(@NotNull Filter filter);

    /**
     * Inserts the {@code entity} into the table, ignoring the provided {@code entity}'s id field and
     * using the auto-increment id.
     *
     * @return the new auto-assigned id
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    long insertAutoIncPk(@NotNull E entity);

    /**
     * A helper utility function that returns the key of the specified {@code entity}.
     * This allows to use this generic interface in higher-level components.
     * A native {@code long} version.
     */
    long longKeyOf(@NotNull E entity);

    /**
     * A helper utility function that returns the key of the specified {@code entity}.
     * This allows to use this generic interface in higher-level components.
     * A {@link Long} version.
     */
    @Override
    default @NotNull Long keyOf(@NotNull E entity) {
        return longKeyOf(entity);
    }

    /**
     * Deletes the rows in the table matching the given {@code entity} id. Can delete several (possibly all) rows.
     * A native {@code long} version.
     *
     * @return the number of affected rows in a table
     */
    @CanIgnoreReturnValue
    int deleteByPk(long key);

    /**
     * Deletes the rows in the table matching the given {@code entity} id. Can delete several (possibly all) rows.
     * A {@link Long} version.
     *
     * @return the number of affected rows in a table
     */
    @Override
    @CanIgnoreReturnValue
    default int deleteByPk(@NotNull Long key) {
        return deleteByPk(key.longValue());
    }
}
