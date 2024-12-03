package io.spbx.orm.testing;

import com.google.common.collect.ImmutableMap;
import io.spbx.orm.adapter.JdbcArrayAdapter;
import io.spbx.orm.adapter.std.chars.CharacterJdbcAdapter;
import io.spbx.orm.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.spbx.util.collect.map.BasicMaps.newOrderedMap;

public class FakeModelAdaptersLocator implements ModelAdaptersLocator {
    public static final ImmutableMap<Class<?>, Class<?>> DEFAULT_MAP = ImmutableMap.of(
        Character.class, CharacterJdbcAdapter.class,
        char.class, CharacterJdbcAdapter.class
    );
    public static final FakeModelAdaptersLocator FAKE_LOCATOR = immutableOf(DEFAULT_MAP);

    private final Map<Class<?>, Class<?>> byClassMap;

    public FakeModelAdaptersLocator(@NotNull Map<Class<?>, Class<?>> byClassMap) {
        this.byClassMap = byClassMap;
    }

    public static @NotNull FakeModelAdaptersLocator empty() {
        return new FakeModelAdaptersLocator(newOrderedMap());
    }

    public static @NotNull FakeModelAdaptersLocator defaults() {
        return new FakeModelAdaptersLocator(newOrderedMap(DEFAULT_MAP));
    }

    public static @NotNull FakeModelAdaptersLocator immutableOf(@NotNull Map<Class<?>, Class<?>> byClassMap) {
        return new FakeModelAdaptersLocator(ImmutableMap.copyOf(byClassMap));
    }

    @Override
    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        return byClassMap.get(model);
    }

    public <T> void setupAdapter(@NotNull Class<T> model, @NotNull Class<? extends JdbcArrayAdapter<T>> adapter) {
        setupStaticAdapter(model, adapter);
    }

    public <T> void setupStaticAdapter(@NotNull Class<T> model, @NotNull Class<?> adapter) {
        assert !byClassMap.containsKey(model) : "Map already contains key: key=" + model + " map=" + byClassMap;
        byClassMap.put(model, adapter);
    }

    public void setupAdapters(@NotNull Map<Class<?>, Class<?>> map) {
        for (Map.Entry<Class<?>, Class<?>> entry : map.entrySet()) {
            setupStaticAdapter(entry.getKey(), entry.getValue());
        }
    }
}
