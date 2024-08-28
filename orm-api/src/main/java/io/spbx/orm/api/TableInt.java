package io.spbx.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.orm.api.query.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides the API for table operations keyed by an {@code int} id and storing {@code E} entities.
 * <p>
 * Most methods of {@link TableObj} accepting an {@link Integer} key have a more efficient alternative method
 * with native {@code int}.
 *
 * @param <E> the entity type
 */
public interface TableInt<E> extends TableObj<Integer, E> {
    /**
     * Returns a copy of {@link TableInt} instance with custom {@code follow} level for read operations in the table.
     * Default value is {@link ReadFollow#NO_FOLLOW}.
     */
    @Override
    @NotNull TableInt<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    /**
     * Returns whether the table contains the entity matching the {@code key} (native {@code int} version).
     */
    boolean exists(int key);

    /**
     * Returns whether the table contains the entity matching the {@code key} ({@link Integer} version).
     */
    @Override
    default boolean exists(@NotNull Integer key) {
        return exists(key.intValue());
    }

    /**
     * Returns the entity given the specified {@code key} or null if not found (native {@code int} version).
     */
    @Nullable E getByPkOrNull(int key);

    /**
     * Returns the entity given the specified {@code key} or null if not found ({@link Integer} version).
     */
    @Override
    default @Nullable E getByPkOrNull(@NotNull Integer key) {
        return getByPkOrNull(key.intValue());
    }

    /**
     * Returns the entity given the specified {@code key} or throws if not found (native {@code int} version).
     */
    default @NotNull E getByPkOrDie(int key) {
        return requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    /**
     * Returns the entity given the specified {@code key} or throws if not found ({@link Integer} version).
     */
    @Override
    default @NotNull E getByPkOrDie(@NotNull Integer key) {
        return getByPkOrDie(key.intValue());
    }

    /**
     * Returns the optional entity given the specified {@code key} (native {@code int} version).
     */
    default @NotNull Optional<E> getOptionalByPk(int key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    /**
     * Returns the optional entity given the specified {@code key} ({@link Integer} version).
     */
    @Override
    default @NotNull Optional<E> getOptionalByPk(@NotNull Integer key) {
        return getOptionalByPk(key.intValue());
    }

    /**
     * Fetches and returns the int map of entities for an {@link IntContainer} of {@code keys}.
     * The implementations are likely to optimize this operation using batch DB requests.
     */
    @NotNull IntObjectMap<E> getBatchByPk(@NotNull IntContainer keys);

    /**
     * Fetches and returns the int array list of ids for entities that match specified {@code filter}.
     */
    @NotNull IntArrayList fetchPks(@NotNull Filter filter);

    /**
     * Inserts the {@code entity} into the table, ignoring the provided {@code entity}'s id field and
     * using the auto-increment id.
     *
     * @return the new auto-assigned id
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    int insertAutoIncPk(@NotNull E entity);

    /**
     * A helper utility function that returns the key of the specified {@code entity}.
     * This allows to use this generic interface in higher-level components.
     * A native {@code int} version.
     */
    int intKeyOf(@NotNull E entity);

    /**
     * A helper utility function that returns the key of the specified {@code entity}.
     * This allows to use this generic interface in higher-level components.
     * A {@link Integer} version.
     */
    @Override
    default @NotNull Integer keyOf(@NotNull E entity) {
        return intKeyOf(entity);
    }

    /**
     * Deletes the rows in the table matching the given {@code entity} id. Can delete several (possibly all) rows.
     * A native {@code int} version.
     *
     * @return the number of affected rows in a table
     */
    @CanIgnoreReturnValue
    int deleteByPk(int key);

    /**
     * Deletes the rows in the table matching the given {@code entity} id. Can delete several (possibly all) rows.
     * A {@link Integer} version.
     *
     * @return the number of affected rows in a table
     */
    @Override
    @CanIgnoreReturnValue
    default int deleteByPk(@NotNull Integer key) {
        return deleteByPk(key.intValue());
    }
}
