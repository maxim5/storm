package io.spbx.orm.arch.util;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

import static io.spbx.util.base.error.BasicExceptions.newInternalError;

@Immutable
record Accessor(@NotNull String value) {
    public static @NotNull Accessor ofJavaMethod(@NotNull JavaMethod method) {
        assert method.isNonStatic(): newInternalError("Invalid java method:", method);
        assert method.isPackageVisible(): newInternalError("Invalid java method:", method);
        assert method.getParameterCount() == 0 : newInternalError("Invalid java method:", method);
        assert method.getExceptionTypes().length == 0 : newInternalError("Invalid java method:", method);
        return new Accessor("%s()".formatted(method.getName()));
    }

    public static @NotNull Accessor ofJavaMethodByName(@NotNull String methodName) {
        assert !methodName.isBlank() : newInternalError("Invalid method name:", methodName);
        return new Accessor("%s()".formatted(methodName));
    }

    public static @NotNull Accessor ofJavaField(@NotNull JavaField field) {
        assert field.isNonStatic() : newInternalError("Invalid java field:", field);
        assert field.isPackageVisible() : newInternalError("Invalid java field:", field);
        return new Accessor(field.getName());
    }
}
