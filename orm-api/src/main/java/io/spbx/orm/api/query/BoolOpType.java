package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public enum BoolOpType {
    AND,
    OR;

    public @NotNull Collector<CharSequence, ?, String> joiner() {
        return Collectors.joining(" %s ".formatted(name()));
    }
}
