package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.arch.model.AdapterArch;
import io.spbx.orm.arch.model.PojoArch;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

import static io.spbx.util.collect.BasicMaps.newOrderedMap;

class PojoArchCollector {
    private final Map<Class<?>, PojoArch> pojos = newOrderedMap();

    public @NotNull ImmutableList<AdapterArch> getAdapterArches() {
        return pojos.values().stream().map(AdapterArch::new).collect(ImmutableList.toImmutableList());
    }

    public @NotNull PojoArch getOrCompute(@NotNull Class<?> type, @NotNull Supplier<PojoArch> compute) {
        PojoArch pojo = pojos.get(type);
        if (pojo == null) {
            pojo = compute.get();
            pojos.put(type, pojo);
        }
        return pojo;
    }
}
