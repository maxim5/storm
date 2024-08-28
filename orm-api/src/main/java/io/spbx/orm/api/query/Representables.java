package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Representables {
    public static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(", ");
    public static final Collector<CharSequence, ?, String> LINE_JOINER = Collectors.joining("\n");

    public static @NotNull String joinWithCommas(@NotNull Collection<? extends Representable> terms) {
        return terms.stream().map(Representables::trimmed).collect(COMMA_JOINER);
    }

    public static @NotNull String joinWithLines(@Nullable Representable @NotNull ... clauses) {
        return Stream.of(clauses).filter(Objects::nonNull).map(Representables::trimmed).collect(LINE_JOINER);
    }

    public static @NotNull String trimmed(@NotNull Representable repr) {
        return repr.repr().trim();
    }
}
