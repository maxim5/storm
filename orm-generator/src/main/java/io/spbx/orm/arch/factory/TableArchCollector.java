package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableMap;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.spbx.orm.arch.InvalidSqlModelException.assure;
import static io.spbx.util.collect.BasicMaps.newOrderedMap;
import static java.util.Objects.requireNonNull;

class TableArchCollector {
    private final Map<Class<?>, TableArch> tables = newOrderedMap();

    public @NotNull ImmutableMap<Class<?>, TableArch> getAllTables() {
        return ImmutableMap.copyOf(tables);
    }

    public void putTable(@NotNull Class<?> key, @NotNull TableArch table) {
        assure(tables.put(key, table) == null, "Duplicate input:", key);
    }

    public @Nullable TableArch getTable(@NotNull Class<?> key) {
        return tables.get(key);
    }

    public @NotNull TableArch getTableOrDie(@NotNull Class<?> key) {
        return requireNonNull(tables.get(key));
    }
}
