package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class HardcodedSelectQuery extends Unit implements SelectQuery {
    public HardcodedSelectQuery(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public static @NotNull HardcodedSelectQuery of(@NotNull String query, @NotNull Args args) {
        return new HardcodedSelectQuery(query, args);
    }

    public static @NotNull HardcodedSelectQuery of(@NotNull String query) {
        return of(query, Args.of());
    }
}
