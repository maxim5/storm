package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

public record ModelField(@NotNull String name,
                         @NotNull String accessor,
                         @NotNull String sqlName,
                         @NotNull Class<?> type,
                         @NotNull Class<?> container) {
}
