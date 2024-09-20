package io.spbx.orm.api.query;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongContainer;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.QueryRunner;
import io.spbx.util.base.BasicNulls;
import io.spbx.util.collect.ImmutableArrayList;
import io.spbx.util.collect.ListBuilder;
import io.spbx.util.collect.Streamer;
import io.spbx.util.prima.extern.hppc.HppcInt;
import io.spbx.util.prima.extern.hppc.HppcLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.spbx.util.base.BasicExceptions.newInternalError;

/**
 * Holds the ordered list of arguments to be applied to JDBC queries. The list corresponds to all {@code "?"}
 * in the query, i.e. all non-hardcoded values. The {@code "?"} usually appear in the query via {@link Variable}s.
 * <p>
 * Arguments can include constant values (e.g., ints, {@code String} literals, or arbitrary {@code Object}s) or
 * unresolved values (instances of {@link UnresolvedArg} class). Unresolved args are essentially named placeholders
 * that are unknown at the query construction time (for example, a query may be a static constant) and will be replaced
 * with real values just before query execution.
 * <p>
 * Usually each unit of a query inherits {@link HasArgs} interface and knows the number of required arguments, so asks
 * them at construction time. Bigger units collect the {@link Args} from all individual constituent units. The final
 * query has all {@link Args} (some maybe unresolved) in the right order.
 * <p>
 * The class does it's best to optimize the memory necessary for the values. Default is a simple {@link List<Object>}.
 * Specially supported cases are all-ints (stored as {@link IntArrayList}) and all-longs (stored as {@link LongArrayList}).
 * The mix of ints and {@link Object}s is stored in a default way.
 *
 * @see HasArgs
 * @see Variable
 * @see UnresolvedArg
 */
@Immutable
public final class Args {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final Args EMPTY_ARGS = new Args(InternalType.GENERIC_LIST, ImmutableArrayList.of());

    private final InternalType type;     // used to optimize the storage (internal and external lists)
    private final Iterable<?> internal;  // original set of args, may contain unresolved
    private final Iterable<?> external;  // args for external use, all unresolved replaced with default values

    private Args(@NotNull InternalType type, @NotNull Iterable<?> internal) {
        assert type != InternalType.GENERIC_LIST || warnAboutPotentialBugs(internal) : "This is Impossible?!";
        this.type = type;
        this.internal = internal;
        this.external = type == InternalType.GENERIC_LIST ? unwrapArgs((List<?>) internal) : internal;
    }

    private Args(@NotNull InternalType type, @NotNull Iterable<?> internal, @NotNull Iterable<?> external) {
        this.type = type;
        this.internal = internal;
        this.external = external;
    }

    public static @NotNull Args of() {
        return EMPTY_ARGS;
    }

    public static @NotNull Args of(@Nullable Object @NotNull ... args) {
        return new Args(InternalType.GENERIC_LIST, ImmutableArrayList.copyOf(args));
    }

    public static @NotNull Args of(@NotNull Iterable<?> args) {
        return new Args(InternalType.GENERIC_LIST, ImmutableArrayList.copyOf(args));
    }

    public static @NotNull Args of(int arg) {
        return new Args(InternalType.INTS, IntArrayList.from(arg));
    }

    public static @NotNull Args of(int arg1, int arg2) {
        return new Args(InternalType.INTS, IntArrayList.from(arg1, arg2));
    }

    public static @NotNull Args of(int ... args) {
        return new Args(InternalType.INTS, IntArrayList.from(args));
    }

    public static @NotNull Args of(@NotNull IntContainer args) {
        return new Args(InternalType.INTS, args);
    }

    public static @NotNull Args of(long arg) {
        return new Args(InternalType.LONGS, LongArrayList.from(arg));
    }

    public static @NotNull Args of(long arg1, long arg2) {
        return new Args(InternalType.LONGS, LongArrayList.from(arg1, arg2));
    }

    public static @NotNull Args of(long ... args) {
        return new Args(InternalType.LONGS, LongArrayList.from(args));
    }

    public static @NotNull Args of(@NotNull LongContainer args) {
        return new Args(InternalType.LONGS, args);
    }

    public static @NotNull Args concat(@NotNull Args lhs, @NotNull Args rhs) {
        // FIX[minor]: optimize for ints and longs
        return Args.of(new ListBuilder<>().addAll(lhs.internalList()).addAll(rhs.internalList()).toBasicsImmutableArrayList());
    }

    public static @NotNull Args flattenArgsOf(@NotNull HasArgs left, @NotNull HasArgs right) {
        return Args.concat(left.args(), right.args());
    }

    public static @NotNull Args flattenArgsOf(@Nullable HasArgs @NotNull ... items) {
        return Args.of(
            Streamer.of(items)
            .filter(Objects::nonNull)
            .map(HasArgs::args)
            .flatMap(Args::internalStream)
            .toBasicsImmutableArrayList()
        );
    }

    public static @NotNull Args flattenArgsOf(@NotNull Iterable<? extends HasArgs> items) {
        return Args.of(
            Streamer.of(items)
            .filter(Objects::nonNull)
            .map(HasArgs::args)
            .flatMap(Args::internalStream)
            .toBasicsImmutableArrayList()
        );
    }

    /**
     * For internal use: when all {@code args} are resolved.
     */
    private static @NotNull Args resolved(@NotNull Iterable<?> args) {
        // FIX[minor]: assert all resolved?
        return new Args(InternalType.GENERIC_LIST, ImmutableArrayList.copyOf(args), ImmutableArrayList.copyOf(args));
    }

    public boolean isEmpty() {
        return switch (type) {
            case GENERIC_LIST -> ((List<?>) external).isEmpty();
            case INTS -> ((IntContainer) external).isEmpty();
            case LONGS -> ((LongContainer) external).isEmpty();
        };
    }

    public int size() {
        return switch (type) {
            case GENERIC_LIST -> ((List<?>) external).size();
            case INTS -> ((IntContainer) external).size();
            case LONGS -> ((LongContainer) external).size();
        };
    }

    public @NotNull List<?> asList() {
        return switch (type) {
            case GENERIC_LIST -> (List<?>) external;
            case INTS -> HppcInt.toJavaList((IntContainer) external);
            case LONGS -> HppcLong.toJavaList((LongContainer) external);
        };
    }

    public boolean isAllResolved() {
        return type != InternalType.GENERIC_LIST || ((List<?>) internal).stream().noneMatch(arg -> arg instanceof UnresolvedArg);
    }

    public int setPreparedParams(@NotNull PreparedStatement statement) throws SQLException {
        return setPreparedParams(statement, 0);
    }

    public int setPreparedParams(@NotNull PreparedStatement statement, int index) throws SQLException {
        return switch (type) {
            case GENERIC_LIST -> QueryRunner.setPreparedParams(statement, external, index);
            case INTS -> QueryRunner.setPreparedParams(statement, (IntContainer) external, index);
            case LONGS -> QueryRunner.setPreparedParams(statement, (LongContainer) external, index);
        };
    }

    private @NotNull List<?> internalList() {
        return switch (type) {
            case GENERIC_LIST -> (List<?>) internal;
            case INTS -> HppcInt.toJavaList((IntContainer) internal);
            case LONGS -> HppcLong.toJavaList((LongContainer) internal);
        };
    }

    private @NotNull Stream<?> internalStream() {
        return internalList().stream();
    }

    /**
     * Used to transform raw {@code internal} values to {@code external}.
     */
    private static @NotNull ImmutableArrayList<Object> unwrapArgs(@NotNull List<?> args) {
        return Streamer.of(args)
            .map(arg -> arg instanceof UnresolvedArg unresolved ? unresolved.defaultValue() : arg)
            .toBasicsImmutableArrayList();
    }

    /**
     * Returns a map of unresolved arguments of this instance: {@code position -> arg}.
     * If all resolved, result is empty.
     */
    // FIX[minor]: Use array or IntObjMap to optimize storage?
    private @NotNull ImmutableMap<Integer, UnresolvedArg> unresolvedArgs() {
        if (type != InternalType.GENERIC_LIST) {
            return ImmutableMap.of();
        }
        Map<Integer, UnresolvedArg> map = null;
        int index = 0;
        for (Object arg : internal) {
            ++index;
            if (arg instanceof UnresolvedArg unresolved) {
                map = BasicNulls.firstNonNull(map, HashMap::new);
                map.put(index, unresolved);
            }
        }
        return map != null ? ImmutableMap.copyOf(map) : ImmutableMap.of();
    }

    /**
     * Returns a copy of this {@code Args} in which all unresolved values are mapped to concrete ones.
     * The mapping is done by matching {@link UnresolvedArg#name()} to a {@code resolved} dictionary.
     * If all args are resolved, this instance is returned.
     *
     * @param resolved the map holding the values to be inserted for unresolved args
     */
    /*package*/ @NotNull Args resolveArgsByName(@NotNull Map<String, ?> resolved) {
        Map<Integer, UnresolvedArg> unresolvedArgs = unresolvedArgs();
        assert unresolvedArgs.size() == resolved.size() :
            "Provided resolved args size doesn't match unresolved placeholders: " +
            "internal=%s, provided=%s".formatted(internal, resolved);
        assert unresolvedArgs.values().stream().map(UnresolvedArg::name).collect(Collectors.toSet()).equals(resolved.keySet()) :
            "Provided resolved args keys don't match unresolved placeholders: " +
            "internal=%s, provided=%s".formatted(internal, resolved);

        if (unresolvedArgs.isEmpty()) {
            return this;
        }

        assert type == InternalType.GENERIC_LIST : newInternalError("Wrong type: type=%s, internal=%s", type, internal);

        ListBuilder<Object> result = new ListBuilder<>();
        int index = 0;
        for (Object arg : internal) {
            UnresolvedArg unresolvedArg = unresolvedArgs.get(++index);
            result.add(unresolvedArg != null ? resolved.get(unresolvedArg.name()) : arg);
        }
        return Args.resolved(result.toBasicsImmutableArrayList());
    }

    /**
     * Returns a copy of this {@code Args} in which all unresolved values are mapped to concrete ones.
     * The mapping is done by taking values a {@code resolved} ordered list in the same order as the args.
     * If all args are resolved, this instance is returned.
     *
     * @param resolved the list holding the values to be inserted for unresolved args
     */
    /*package*/ @NotNull Args resolveArgsByOrderedList(@NotNull List<?> resolved) {
        Map<Integer, UnresolvedArg> unresolvedArgs = unresolvedArgs();
        assert unresolvedArgs.size() == resolved.size() :
            "Provided resolved args size doesn't match unresolved placeholders: " +
            "internal=%s, provided=%s".formatted(internal, resolved);

        if (type == InternalType.GENERIC_LIST && unresolvedArgs.size() == ((List<?>) internal).size()) {
            return Args.resolved(resolved);
        }
        if (unresolvedArgs.isEmpty()) {
            return this;
        }

        assert type == InternalType.GENERIC_LIST : newInternalError("Wrong type: type=%s, internal=%s", type, internal);

        ListBuilder<Object> result = new ListBuilder<>();
        Iterator<?> iterator = resolved.iterator();
        int index = 0;
        for (Object arg : internal) {
            UnresolvedArg unresolvedArg = unresolvedArgs.get(++index);
            result.add(unresolvedArg != null ? iterator.next() : arg);
        }
        return Args.resolved(result.toBasicsImmutableArrayList());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Args args && type == args.type && internal.equals(args.internal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, internal);
    }

    @Override
    public String toString() {
        return internal.toString();
    }

    @SuppressWarnings("SameReturnValue")
    private static boolean warnAboutPotentialBugs(@NotNull Iterable<?> args) {
        if (!log.at(Level.WARNING).isEnabled()) {
            return true;
        }
        for (Object arg : args) {
            if (arg instanceof Variable) {
                log.at(Level.WARNING).log("Variable `%s` passed in as an arg. Did you forget to call `.args()`?", arg);
            } else if (arg instanceof HasArgs) {
                log.at(Level.WARNING).log("HasArgs instance `%s` passed in as an arg. Did you forget to call `.args()`?", arg);
            } else if (arg instanceof Args) {
                log.at(Level.WARNING).log("Args instance `%s` passed in as an arg. Did you extra call `Args.of()`?", arg);
            }
        }
        return true;
    }

    private enum InternalType {
        GENERIC_LIST,
        INTS,
        LONGS,
    }
}
