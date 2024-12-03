package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Immutable
public enum BoolOpType {
    AND,
    OR;

    public @NotNull Collector<CharSequence, ?, String> joiner() {
        return Collectors.joining(" %s ".formatted(name()));
    }
}
