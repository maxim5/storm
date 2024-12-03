package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.spbx.orm.arch.model.AdapterArch;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

import static java.util.Objects.requireNonNull;

@Immutable
class RunResult {
    private final @NotNull ImmutableMap<Class<?>, TableArch> tables;
    private final @NotNull ImmutableList<AdapterArch> adapters;

    RunResult(@NotNull ImmutableMap<Class<?>, TableArch> tables, @NotNull ImmutableList<AdapterArch> adapters) {
        this.tables = tables;
        this.adapters = adapters;
    }

    public @NotNull ImmutableList<TableArch> tables() {
        return ImmutableList.copyOf(tables.values());
    }

    public @NotNull ImmutableList<AdapterArch> adapters() {
        return adapters;
    }

    public @NotNull TableArch getTableOrDie(@NotNull Class<?> key) {
        return requireNonNull(tables.get(key));
    }
}
