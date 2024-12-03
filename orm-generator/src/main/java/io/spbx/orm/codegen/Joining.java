package io.spbx.orm.codegen;

import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Stateless
class Joining {
    public static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(", ");
    public static final Collector<CharSequence, ?, String> LINE_JOINER = Collectors.joining("\n");

    public static @NotNull Collector<CharSequence, ?, String> linesJoiner(@NotNull Indent indent) {
        return Collectors.joining(indent.delimiter(), indent.prefix(), "");
    }

    public static @NotNull Collector<CharSequence, ?, String> linesJoiner(@NotNull Indent indent, boolean filterEmpty) {
        return new Collector<CharSequence, StringJoiner, String>() {
            @Override
            public Supplier<StringJoiner> supplier() {
                return () -> new StringJoiner(indent.delimiter(), indent.prefix(), "");
            }

            @Override
            public BiConsumer<StringJoiner, CharSequence> accumulator() {
                return (stringJoiner, newElement) -> {
                    if (!filterEmpty || !newElement.isEmpty()) {
                        stringJoiner.add(newElement);
                    }
                };
            }

            @Override
            public BinaryOperator<StringJoiner> combiner() {
                return StringJoiner::merge;
            }

            @Override
            public Function<StringJoiner, String> finisher() {
                return StringJoiner::toString;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }
}
