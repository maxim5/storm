package io.spbx.orm.arch.factory;

import org.jetbrains.annotations.NotNull;

public record PojoInput(@NotNull Class<?> pojoClass) {
    public static @NotNull PojoInput of(@NotNull Class<?> pojoClass) {
        return new PojoInput(pojoClass);
    }
}
