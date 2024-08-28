package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.JavaNameHolder;
import io.spbx.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;

import static io.spbx.util.code.jvm.JavaNameValidator.validateJavaClassName;
import static io.spbx.util.code.jvm.JavaNameValidator.validateJavaPackage;

record FQN(@NotNull String packageName, @NotNull String className) {
    public FQN {
        validateJavaPackage(packageName);
        validateJavaClassName(className);
    }

    public static @NotNull FQN of(@NotNull Class<?> klass) {
        return new FQN(klass.getPackageName(), Naming.shortCanonicalJavaName(klass));
    }

    public static @NotNull FQN of(@NotNull JavaNameHolder nameHolder) {
        return new FQN(nameHolder.packageName(), nameHolder.javaName());
    }

    public @NotNull String toImportName() {
        return "%s.%s".formatted(packageName, className);
    }
}
