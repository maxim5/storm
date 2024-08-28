package io.spbx.orm.arch.model;

import com.google.errorprone.annotations.Immutable;
import io.spbx.util.base.OneOf;
import io.spbx.util.lazy.AtomicLazyInit;
import io.spbx.util.lazy.LazyInit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Immutable
public class PojoParent {
    public static final PojoParent DETACHED = new PojoParent(OneOf.ofFirst(Optional.empty()));

    private final OneOf<Optional<TerminalData>, LazyInit<PojoField>> parent;

    private PojoParent(@NotNull OneOf<Optional<TerminalData>, LazyInit<PojoField>> parent) {
        this.parent = parent;
    }

    public static @NotNull PojoParent ofTerminal(@NotNull String fieldName, @NotNull String sqlName) {
        TerminalData data = new TerminalData(fieldName, sqlName);
        return new PojoParent(OneOf.ofFirst(Optional.of(data)));
    }

    /*package*/ static @NotNull PojoParent ofUninitializedField() {
        return new PojoParent(OneOf.ofSecond(AtomicLazyInit.createUninitialized()));
    }

    /*package*/ void initializeOrDie(@NotNull PojoField field) {
        assert parent.hasSecond() : "Parent expected to hold pojo: %s".formatted(this);
        requireNonNull(parent.second()).initializeOrDie(field);
    }

    public boolean isTerminal() {
        return parent.hasFirst();
    }

    public @NotNull Optional<String> terminalSqlNameOrDie() {
        return requireNonNull(parent.first()).map(TerminalData::sqlName);
    }

    public @NotNull PojoField pojoFieldOrDie() {
        return requireNonNull(parent.second()).getOrDie();
    }

    @Override
    public String toString() {
        return "PojoParent%s".formatted(parent);
    }

    private record TerminalData(@NotNull String fieldName, @NotNull String sqlName) {}
}
