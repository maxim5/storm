package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToLongFunction;

import static io.spbx.util.base.lang.EasyCast.castAny;

/**
 * A {@link Foreign} implementation for long FK. Note: zero ids are treated as {@code NULL}s.
 */
public final class ForeignLong<E> implements Foreign<Long, E> {
    private static final ForeignLong<?> EMPTY = new ForeignLong<>(0, null);

    private final long id;
    private final AtomicReference<E> entity;

    public ForeignLong(long id, @Nullable E entity) {
        assert id != 0 || entity == null : "Invalid foreign: id=%s entity=%s".formatted(id, entity);
        this.id = id;
        this.entity = new AtomicReference<>(entity);
    }

    public static <T> @NotNull ForeignLong<T> empty() {
        return castAny(EMPTY);
    }

    public static <T> @NotNull ForeignLong<T> ofId(long id) {
        return new ForeignLong<>(id, null);
    }

    public static <T> @NotNull ForeignLong<T> ofEntity(long id, @NotNull T entity) {
        return new ForeignLong<>(id, entity);
    }

    public long getLongId() {
        return id;
    }

    @Override
    public @Nullable Long getFk() {
        return id == 0 ? null : id;
    }

    @Override
    public @NotNull Long getFkOrDie() {
        assert id != 0 : "The foreign is empty: " + this;
        return id;
    }

    @Override
    public @Nullable E getEntity() {
        return entity.get();
    }

    @Override
    public boolean isEmpty() {
        return id == 0;
    }

    @Override
    public boolean isPresent() {
        return id != 0;
    }

    @Override
    public boolean setEntityIfMissing(@NotNull E entity) {
        assert isPresent() : "ForeignLong reference is empty, can't set the entity: " + entity;
        return this.entity.compareAndSet(null, entity);
    }

    @Override
    public void setEntityUnconditionally(@NotNull E entity) {
        assert isPresent() : "ForeignLong reference is empty, can't set the entity: " + entity;
        this.entity.set(entity);
    }

    public boolean isConsistent(@NotNull ToLongFunction<E> func) {
        return entity.get() == null || id == func.applyAsLong(entity.get());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ForeignLong<?> that && id == that.id && Objects.equals(entity.get(), that.entity.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entity.get());
    }

    @Override
    public String toString() {
        return "ForeignLong{id=%d, entity=%s}".formatted(id, entity);
    }

    public static <E> boolean isMatch(@NotNull ForeignLong<E> left, @NotNull ForeignLong<E> right) {
        return left.id == right.id && Foreign.isEntityMatch(left, right);
    }
}
