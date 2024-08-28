package io.spbx.orm.testing;

import io.spbx.orm.api.query.Column;
import io.spbx.orm.api.query.ColumnTerm;
import io.spbx.orm.api.query.Shortcuts;
import io.spbx.orm.api.query.TermType;
import org.jetbrains.annotations.NotNull;

public record FakeColumn(@NotNull String name, @NotNull TermType type) implements Column {
    public static final FakeColumn FOO = of("foo");
    public static final FakeColumn INT = new FakeColumn("i", TermType.NUMBER);
    public static final FakeColumn STR = new FakeColumn("s", TermType.STRING);

    public static @NotNull FakeColumn of(@NotNull String name) {
        return new FakeColumn(name, TermType.WILDCARD);
    }

    public @NotNull ColumnTerm makeVar(int value) {
        return new ColumnTerm(this, Shortcuts.var(value));
    }

    public @NotNull ColumnTerm makeVar(@NotNull String value) {
        return new ColumnTerm(this, Shortcuts.var(value));
    }
}
