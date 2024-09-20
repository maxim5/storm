package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@code DISTINCT} column.
 */
@Immutable
public class DistinctColumn extends Unit implements Term {
    private final Column column;

    public DistinctColumn(@NotNull Column column) {
        super("DISTINCT %s".formatted(column.repr()));
        this.column = column;
    }

    @Override
    public @NotNull TermType type() {
        return column.type();
    }

    public @NotNull Column column() {
        return column;
    }
}
