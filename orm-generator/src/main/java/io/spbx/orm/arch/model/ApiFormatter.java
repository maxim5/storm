package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

public interface ApiFormatter<F> {
    default @NotNull F expr() {
        return formatter(FormatMode.EXPRESSION);
    }

    default @NotNull F statement() {
        return formatter(FormatMode.STATEMENT);
    }

    @NotNull F formatter(@NotNull FormatMode mode);

    enum FormatMode {
        EXPRESSION,
        STATEMENT;

        @NotNull String eol() {
            return this == STATEMENT ? ";" : "";
        }
    }
}
