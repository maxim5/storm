package io.spbx.orm.api;

import io.spbx.util.base.annotate.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides the API for table operations keyed by an {@code K} id and storing {@code E} entities.
 *
 * @param <K> the key type
 * @param <E> the entity type
 */
public interface TableObj<K, E> extends BaseTable<E> {
    /**
     * Returns a copy of {@link TableObj} instance with custom {@code follow} level for read operations in the table.
     * Default value is {@link ReadFollow#NO_FOLLOW}.
     */
    @Override
    @NotNull TableObj<K, E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    /**
     * Returns whether the table contains the entity matching the {@code key}.
     */
    boolean exists(@NotNull K key);

    /**
     * Returns the entity given the specified {@code key} or null if not found.
     */
    @Nullable E getByPkOrNull(@NotNull K key);

    /**
     * Returns the entity given the specified {@code key} or throws if not found.
     */
    default @NotNull E getByPkOrDie(@NotNull K key) {
        return requireNonNull(getByPkOrNull(key), () -> "Entity not found by PK=" + key);
    }

    /**
     * Returns the optional entity given the specified {@code key}.
     */
    default @NotNull Optional<E> getOptionalByPk(@NotNull K key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    /**
     * Fetches and returns the map of entities for a collection of {@code keys}.
     * The implementations are likely to optimize this operation using batch DB requests.
     */
    default @NotNull Map<K, E> getBatchByPk(@NotNull Collection<? extends K> keys) {
        // The following code is simpler, but doesn't handle nulls correctly.
        //   return keys.stream().collect(Collectors.toMap(k -> k, this::getByPkOrNull));
        // See https://stackoverflow.com/questions/24630963/nullpointerexception-in-collectors-tomap-with-null-entry-values
        HashMap<K, E> map = new HashMap<>();
        keys.forEach(k -> map.put(k, getByPkOrNull(k)));
        return map;
    }

    /**
     * A helper utility function that returns the key of the specified {@code entity}.
     * This allows to use this generic interface in higher-level components.
     */
    @NotNull K keyOf(@NotNull E entity);

    /**
     * Updates the existing entity in the table. More precisely, it modifies the rows that match the {@code entity} id
     * with the specified data.
     *
     * @return the number of updated rows
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int updateByPk(@NotNull E entity);

    /**
     * Updates if exists or otherwise inserts the {@code entity} in the table.
     * The match is done via {@code entity} id.
     *
     * @return the number of affected rows (either updated or inserted).
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    default int updateByPkOrInsert(@NotNull E entity) {
        int updated = updateByPk(entity);
        if (updated == 0) {
            return insert(entity);
        }
        return updated;
    }

    /**
     * Deletes the rows in the table matching the given {@code entity} id. Can delete several (possibly all) rows.
     *
     * @return the number of affected rows in a table
     */
    @CanIgnoreReturnValue
    int deleteByPk(@NotNull K key);
}
