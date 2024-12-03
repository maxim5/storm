package io.spbx.orm.arch.model;

import io.spbx.orm.arch.util.JavaField;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public record ModelField(@NotNull JavaField javaField, @NotNull String accessor, @NotNull String sqlName) {
    public @NotNull String name() {
        return javaField.getName();
    }

    public @NotNull Class<?> type() {
        return javaField.getType();
    }
}
