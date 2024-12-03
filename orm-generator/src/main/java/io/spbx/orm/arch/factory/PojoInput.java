package io.spbx.orm.arch.factory;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public record PojoInput(@NotNull Class<?> pojoClass) {
    public static @NotNull PojoInput of(@NotNull Class<?> pojoClass) {
        return new PojoInput(pojoClass);
    }
}
