package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import static io.spbx.util.code.jvm.JavaNameValidator.validateJavaIdentifier;
import static io.spbx.util.code.jvm.JavaNameValidator.validateJavaPackage;

public record AdapterArch(@NotNull PojoArch pojoArch) implements JavaNameHolder {
    @Override
    public @NotNull String javaName() {
        return validateJavaIdentifier(pojoArch.adapterName());
    }

    @Override
    public @NotNull String packageName() {
        return validateJavaPackage(pojoArch.pojoType().getPackageName());
    }
}
