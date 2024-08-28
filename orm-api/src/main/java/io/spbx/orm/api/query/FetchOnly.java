package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class FetchOnly extends Unit implements LimitClause {
    private final int value;

    public FetchOnly(int value, boolean withTies) {
        super("FETCH NEXT ? ROWS ONLY" + (withTies ? " WITH TIES" : ""), Args.of(value));
        assert value > 0 : "Invalid limit value: " + value;
        this.value = value;
    }

    public static @NotNull FetchOnly of(int value) {
        return new FetchOnly(value, false);
    }

    public static @NotNull FetchOnly of(int value, boolean withTies) {
        return new FetchOnly(value, withTies);
    }

    public int limitValue() {
        return value;
    }
}
