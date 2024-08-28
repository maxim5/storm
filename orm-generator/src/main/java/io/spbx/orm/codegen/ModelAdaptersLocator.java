package io.spbx.orm.codegen;

import io.spbx.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModelAdaptersLocator {
    @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model);

    default @NotNull FQN locateAdapterFqn(@NotNull Class<?> model) {
        Class<?> klass = locateAdapterClass(model);
        if (klass != null) {
            return FQN.of(klass);
        }
        return new FQN(model.getPackageName(), Naming.defaultAdapterName(model));
    }
}
