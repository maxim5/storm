package io.spbx.orm.testing;

import io.spbx.orm.api.query.Column;
import io.spbx.orm.api.query.TermType;
import org.jetbrains.annotations.NotNull;

public enum FakeEnumColumn implements Column {
    FOO(TermType.WILDCARD),
    INT(TermType.NUMBER),
    STR(TermType.STRING);

    private final TermType type;

    FakeEnumColumn(@NotNull TermType type) {
        this.type = type;
    }

    @Override
    public @NotNull TermType type() {
        return type;
    }
}
