package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.entity.EntityData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A wrapper for the unresolved query that can resolve args once the context is provided.
 * <p>
 * A typical use-case is the following. The top level query is a batch INSERT or UPDATE with a filter.
 * A filter is thus contextually dependent on each chunk in the batch and can't be calculated statically.
 * <p>
 * Example query:
 * <pre>
 *   UPDATE user_model
 *   SET user_id = ?, age = ?    -- Two columns updated => chunk_size=2 => batch can be size of 2, 4, 6, ...
 *   WHERE user_id = ?           -- For each chunk, its first value must be added to params again
 * </pre>
 * <p>
 * So the filter query is wrapped into a {@code Contextual}. In order to resolve to work, a {@code resolver} function
 * is also necessary, which will be called for each chunk in a batch (called a context {@link C}).
 *
 * @param <Q> the type of query having unresolved args (usually a {@link Filter} instance, e.g. {@link Where})
 * @param <C> the type of context used for resolution (usually a chunk, e.g. {@link List< EntityData >})
 */
@Immutable
public abstract class Contextual<Q extends HasArgs, C> implements Representable {
    private final Q query;

    public Contextual(@NotNull Q query) {
        this.query = query;
    }

    public @NotNull Q query() {
        return query;
    }

    @Override
    public @NotNull String repr() {
        return query.repr();
    }

    /**
     * Resolves {@code query} args from the given {@code context} and returns the result.
     */
    public abstract @NotNull Args resolveQueryArgs(@NotNull C context);

    /**
     * Returns a new {@code Contextual} for a resolved {@code query}.
     */
    public static <Q extends HasArgs, C> @NotNull Contextual<Q, C> resolved(@NotNull Q query) {
        assert query.args().isAllResolved() : "The query contains unresolved args: " + query;
        return new Contextual<>(query) {
            @Override
            public @NotNull Args resolveQueryArgs(@NotNull C context) {
                return query.args();
            }
        };
    }

    /**
     * Returns a new {@code Contextual} for a {@code query}, which uses name-referencing for resolve.
     * The {@code resolver} should be able to return the map by name for context {@code C} called.
     */
    public static <Q extends HasArgs, C>
            @NotNull Contextual<Q, C> resolvingByName(@NotNull Q query, @NotNull Function<C, Map<String, ?>> resolver) {
        return new Contextual<>(query) {
            @Override
            public @NotNull Args resolveQueryArgs(@NotNull C context) {
                Map<String, ?> map = resolver.apply(context);
                return query.args().resolveArgsByName(map);
            }
        };
    }

    /**
     * Returns a new {@code Contextual} for a {@code query}, which uses args order for resolve.
     * The {@code resolver} should be able to return the list by arg values for context {@code C} called.
     */
    public static <Q extends HasArgs, C>
            @NotNull Contextual<Q, C> resolvingByOrderedList(@NotNull Q query, @NotNull Function<C, List<?>> resolver) {
        return new Contextual<>(query) {
            @Override
            public @NotNull Args resolveQueryArgs(@NotNull C context) {
                List<?> args = resolver.apply(context);
                return query.args().resolveArgsByOrderedList(args);
            }
        };
    }

    // FIX[norm]: resolvingByInts, resolvingByLongs
}
