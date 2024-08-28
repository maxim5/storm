package io.spbx.orm.arch.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

record Accessor(@NotNull String value) {
    public static @NotNull Accessor ofJavaMethod(@NotNull Method method) {
        return new Accessor("%s()".formatted(method.getName()));
    }

    public static @NotNull Accessor ofJavaField(@NotNull Field field) {
        return new Accessor(field.getName());
    }
}
