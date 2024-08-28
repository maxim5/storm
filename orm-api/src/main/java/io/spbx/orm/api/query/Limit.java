package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class Limit extends Unit implements LimitClause {
    private final int value;

    public Limit(int value) {
        super("LIMIT ?", Args.of(value));
        assert value > 0 : "Invalid limit value: " + value;
        this.value = value;
    }

    public static @NotNull Limit of(int value) {
        return new Limit(value);
    }

    public int limitValue() {
        return value;
    }
}
