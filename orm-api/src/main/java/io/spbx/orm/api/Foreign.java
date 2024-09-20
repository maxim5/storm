package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents foreign entity referenced from another entity. In SQL, it's represented by a foreign key, i.e. just an id.
 * In case Table API retrieves a shallow entity, the {@link Foreign} instance contains only id of type {@link I}.
 * In case Table API retrieves a full entity (following foreign references), the {@link Foreign} instance in addition
 * contains the referenced entity of type {@link E} (which too can be shallow or full).
 * <p>
 * If the column does not permit {@code NULL}s, then the id is always going to be set. Otherwise, it's possible
 * to have an empty {@link Foreign} which corresponds to {@code NULL} value in the DB.
 * If the key is {@code null}, then the entity must be {@code null}.
 * <p>
 * Instances of {@link Foreign} are essentially immutable, but can be changed from shallow to full, i.e. updated with
 * an entity reference.
 *
 * @param <I> foreign key type
 * @param <E> foreign entity type
 *
 * @see ReadFollow
 */
public interface Foreign<I, E> {
    /**
     * Returns the foreign key (id). If the column does not permit {@code NULL}s, it is always set.
     */
    @Nullable I getFk();

    /**
     * Returns the non-null foreign key (id). Applicable to cases when the column does not permit {@code NULL}s.
     */
    default @NotNull I getFkOrDie() {
        I result = getFk();
        assert result != null : "The foreign is empty: " + this;
        return result;
    }

    /**
     * Returns the nullable foreign entity, if it is set.
     */
    @Nullable E getEntity();

    /**
     * Returns whether this instance doesn't hold any reference.
     * If the column does not permit {@code NULL}s, it's always false.
     */
    default boolean isEmpty() {
        return getFk() == null;
    }

    /**
     * Returns whether this instance holds any reference.
     * If the column does not permit {@code NULL}s, it's always true.
     */
    default boolean isPresent() {
        return !isEmpty();
    }

    /**
     * Returns whether the foreign entity is present.
     */
    default boolean hasEntity() {
        return getEntity() != null;
    }

    /**
     * Returns whether the foreign entity is not present.
     */
    default boolean hasNullEntity() {
        return getEntity() == null;
    }

    /**
     * Sets the {@code entity} reference if it's not present in this instance.
     * Fails if this {@link Foreign} instance is empty, i.e. holds a {@code NULL} foreign key.
     * @return true if updated
     */
    boolean setEntityIfMissing(@NotNull E entity);

    /**
     * Sets the {@code entity} regardless of the current present value.
     * Fails if this {@link Foreign} instance is empty, i.e. holds a {@code NULL} foreign key.
     */
    void setEntityUnconditionally(@NotNull E entity);

    /**
     * Returns whether two {@link Foreign} instances match, i.e. the foreign keys are equal and
     * the foreign entities are equal if present in both instances.
     */
    static <I, E> boolean isMatch(@NotNull Foreign<I, E> left, @NotNull Foreign<I, E> right) {
        return Objects.equals(left.getFk(), right.getFk()) && isEntityMatch(left, right);
    }

    /**
     * Returns whether two {@link Foreign} entities match, i.e. the entities are equal if present in both instances.
     */
    static <I, E> boolean isEntityMatch(@NotNull Foreign<I, E> left, @NotNull Foreign<I, E> right) {
        return left.hasNullEntity() || right.hasNullEntity() || Objects.equals(left.getEntity(), right.getEntity());
    }
}
