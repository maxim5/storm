package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableMap;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.spbx.orm.arch.InvalidSqlModelException.assure;
import static io.spbx.util.base.error.BasicExceptions.runOnlyInDev;
import static io.spbx.util.collect.map.BasicMaps.newOrderedMap;
import static java.util.Objects.requireNonNull;

class TableArchCollector {
    private static final Logger log = Logger.forEnclosingClass();
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
        tables.keySet().stream().filter(klass -> klass != key && isSameName(klass, key)).forEach(klass -> {
            log.warn().log(
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
