package io.spbx.orm.codegen;

import org.jetbrains.annotations.NotNull;

enum Indent {
    INDENT0(0),
    INDENT1(1),
    INDENT2(2),
    INDENT3(3),
    INDENT4(4);

    private static final int NUM_SPACES = 4;
    private final String spaces;

    Indent(int len) {
        spaces = (" ".repeat(len * NUM_SPACES)).intern();
    }

    public @NotNull String delimiter() {
        return ("\n" + spaces).intern();
    }

    public @NotNull String commaDelimiter() {
        return (",\n" + spaces).intern();
    }

    public @NotNull String prefix() {
        return spaces;
    }

    public @NotNull String spaces() {
        return spaces;
    }

    @Override
    public @NotNull String toString() {
        return spaces;
    }
}
