package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class FullColumn extends Unit implements Column {
    private final Column column;
    private final String table;

    public FullColumn(@NotNull Column column, @NotNull String table) {
        super("%s.%s".formatted(table, column.name()));
        this.column = column;
        this.table = table;
    }

    @Override
    public @NotNull TermType type() {
        return column.type();
    }

    @Override
    public @NotNull String name() {
        return column.name();
    }

    public @NotNull String table() {
        return table;
    }
}
