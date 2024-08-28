package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.spbx.util.base.EasyCast.castAny;

/**
 * A generic {@link Foreign} implementation for a non-primitive FK.
 */
public final class ForeignObj<I, E> implements Foreign<I, E> {
    private static final ForeignObj<?, ?> EMPTY = new ForeignObj<>(null, null);

    private final I id;
    private final AtomicReference<E> entity;

    public ForeignObj(@Nullable I id, @Nullable E entity) {
        assert id != null || entity == null : "Invalid foreign: id=%s entity=%s".formatted(id, entity);
        this.id = id;
        this.entity = new AtomicReference<>(entity);
    }

    public static <K, V> @NotNull ForeignObj<K, V> empty() {
        return castAny(EMPTY);
    }

    public static <K, V> @NotNull ForeignObj<K, V> ofId(@NotNull K id) {
        return new ForeignObj<>(id, null);
    }

    public static <K, V> @NotNull ForeignObj<K, V> ofEntity(@NotNull K id, @NotNull V entity) {
        return new ForeignObj<>(id, entity);
    }

    @Override
    public @Nullable I getFk() {
        return id;
    }

    @Override
    public @Nullable E getEntity() {
        return entity.get();
    }

    @Override
    public boolean isEmpty() {
        return id == null;
    }

    @Override
    public boolean isPresent() {
        return id != null;
    }

    @Override
    public boolean setEntityIfMissing(@NotNull E entity) {
        assert isPresent() : "ForeignObj reference is empty, can't set the entity: " + entity;
        return this.entity.compareAndSet(null, entity);
    }

    @Override
    public void setEntityUnconditionally(@NotNull E entity) {
        assert isPresent() : "ForeignObj reference is empty, can't set the entity: " + entity;
        this.entity.set(entity);
    }

    public boolean isConsistent(@NotNull Function<E, I> func) {
        return entity.get() == null || Objects.equals(id, func.apply(entity.get()));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ForeignObj<?, ?> that &&
            Objects.equals(id, that.id) &&
            Objects.equals(entity.get(), that.entity.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entity.get());
    }

    @Override
    public String toString() {
        return "ForeignObj{id=%s, entity=%s}".formatted(id, entity);
    }

    public static <I, E> boolean isMatch(@NotNull ForeignObj<I, E> left, @NotNull ForeignObj<I, E> right) {
        return Foreign.isMatch(left, right);
    }
}
