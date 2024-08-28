package io.spbx.orm.api;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.LongContainer;
import io.spbx.orm.api.query.Args;
import io.spbx.util.prima.extern.hppc.HppcInt;
import io.spbx.util.prima.extern.hppc.HppcLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.spbx.util.collect.BasicIterables.asList;

/**
 * An exception for errors during SQL query preparation, execution or result processing.
 */
public class QueryException extends RuntimeException {
    private final String query;
    private final List<?> args;

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Object @NotNull[] args) {
        this(message, query, Arrays.asList(args), null);
    }

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Throwable cause) {
        this(message, query, List.of(), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @NotNull Args arg, @Nullable Throwable cause) {
        this(message, query, arg.asList(), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Object arg, @Nullable Throwable cause) {
        this(message, query, tryToUnwrap(arg), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @NotNull Collection<?> args, @Nullable Throwable cause) {
        super(format(message, query, args), cause);
        this.query = query;
        this.args = asList(args);
    }

    /**
     * Returns the query that caused the failure.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the list of arguments for the query.
     */
    public List<?> getArgs() {
        return args;
    }

    private static @NotNull String format(@NotNull String message, @NotNull String query, @NotNull Collection<?> args) {
        return "%s. Query:\n```\n%s\n```\nArgs: `%s`".formatted(message, query, args);
    }

    private static @NotNull List<?> tryToUnwrap(@Nullable Object arg) {
        if (arg instanceof IntContainer ints) {
            return HppcInt.toJavaList(ints);
        }
        if (arg instanceof LongContainer longs) {
            return HppcLong.toJavaList(longs);
        }
        return Collections.singletonList(arg);
    }
}
