package io.spbx.orm.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.util.classpath.ClassNamePredicate;
import io.spbx.util.classpath.GuavaClasspathScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DefaultModelAdaptersLocator implements ModelAdaptersLocator {
    protected static final ClassNamePredicate FILTER_BY_NAME = (pkg, cls) -> cls.endsWith("JdbcAdapter");
    protected static final String NAME = "model adapter";

    protected final ImmutableListMultimap<Class<?>, Class<?>> index;

    public DefaultModelAdaptersLocator() {
        this(GuavaClasspathScanner.fromSystemClassLoader().timed(NAME).getAnnotatedClasses(FILTER_BY_NAME, JdbcAdapt.class));
    }

    protected DefaultModelAdaptersLocator(@NotNull Set<Class<?>> adapters) {
        ImmutableListMultimap.Builder<Class<?>, Class<?>> builder = new ImmutableListMultimap.Builder<>();
        for (Class<?> adapter : adapters) {
            if (adapter.isAnnotationPresent(JdbcAdapt.class)) {
                JdbcAdapt annotation = adapter.getAnnotation(JdbcAdapt.class);
                for (Class<?> modelClass : annotation.value()) {
                    builder.put(modelClass, adapter);
                }
            }
        }
        this.index = builder.build();
    }

    @Override
    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        ImmutableList<Class<?>> adapters = index.get(model);
        return adapters.isEmpty() ? null : Iterables.getOnlyElement(adapters);
    }
}
