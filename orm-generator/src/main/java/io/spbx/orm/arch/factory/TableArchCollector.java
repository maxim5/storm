package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.logging.Level;

import static io.spbx.orm.arch.InvalidSqlModelException.assure;
import static io.spbx.util.base.BasicExceptions.runOnlyInDev;
import static io.spbx.util.collect.BasicMaps.newOrderedMap;
import static java.util.Objects.requireNonNull;

class TableArchCollector {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final Map<Class<?>, TableArch> tables = newOrderedMap();

    public @NotNull ImmutableMap<Class<?>, TableArch> getAllTables() {
        return ImmutableMap.copyOf(tables);
    }

    public void putTable(@NotNull Class<?> key, @NotNull TableArch table) {
        assure(tables.put(key, table) == null, "Duplicate input:", key);
    }

    public @Nullable TableArch getTable(@NotNull Class<?> key) {
        TableArch table = tables.get(key);
        assert table != null || runOnlyInDev(() -> reportDifferentClassVersionsIfExist(key));
        return table;
    }

    public @NotNull TableArch getTableOrDie(@NotNull Class<?> key) {
        TableArch table = tables.get(key);
        assert table != null || runOnlyInDev(() -> reportDifferentClassVersionsIfExist(key));
        return requireNonNull(table);
    }

    private void reportDifferentClassVersionsIfExist(@NotNull Class<?> key) {
        tables.keySet().stream().filter(klass -> isSameName(klass, key)).forEach(klass -> {
            log.at(Level.WARNING).log(
                "Found a different Class<?> instance with the identical name. " +
                "Could be a different Class<?> version or different class-loader: " +
                "exists=[name=%s id=%s classloader=%s] missing=[name=%s id=%s classloader=%s]",
                klass.getName(), System.identityHashCode(klass), klass.getClassLoader(),
                key.getName(), System.identityHashCode(key), key.getClassLoader()
            );
        });
    }

    private static boolean isSameName(@NotNull Class<?> first, @NotNull Class<?> second) {
        return first.getName().equals(second.getName());
    }
}
