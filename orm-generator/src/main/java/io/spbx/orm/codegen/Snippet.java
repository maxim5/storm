package io.spbx.orm.codegen;

import io.spbx.util.base.annotate.CheckReturnValue;
import io.spbx.util.code.gen.LinesBuilder;
import org.jetbrains.annotations.NotNull;

@CheckReturnValue
class Snippet extends LinesBuilder<Snippet> {
    private boolean isMultilineForced = false;

    public boolean isMultiline() {
        return isMultilineForced || linesNumber() > 1;
    }

    @Override
    public @NotNull Snippet appendMultiline(@NotNull String multiline) {
        return appendLines(multiline.lines()).forceMultiline(containsNewLineChars(multiline));
    }

    public @NotNull Snippet forceMultiline(boolean multiline) {
        isMultilineForced = isMultilineForced || multiline;
        return this;
    }

    public @NotNull String joinLines(@NotNull Indent indent) {
        return join(indent.delimiter());
    }
}
