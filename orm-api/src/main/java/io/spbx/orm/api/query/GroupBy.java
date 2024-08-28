package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.spbx.orm.api.query.Representables.COMMA_JOINER;

@Immutable
public class GroupBy extends Unit {
    private final ImmutableList<Named> terms;

    public GroupBy(@NotNull List<? extends Named> terms) {
        super("GROUP BY " + terms.stream().map(Named::name).collect(COMMA_JOINER), Args.of());
        this.terms = ImmutableList.copyOf(terms);
    }

    public static @NotNull GroupBy of(@NotNull Named term) {
        return new GroupBy(List.of(term));
    }

    public static @NotNull GroupBy of(@NotNull Named @NotNull ... terms) {
        return new GroupBy(List.of(terms));
    }

    public @NotNull List<Named> terms() {
        return terms;
    }
}
