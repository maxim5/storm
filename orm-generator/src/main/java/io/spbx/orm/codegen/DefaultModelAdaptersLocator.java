package io.spbx.orm.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.util.classpath.ClassNamePredicate;
import io.spbx.util.classpath.ClasspathScanner;
import io.spbx.util.classpath.GuavaClasspathScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;
import java.util.Set;

@Immutable
public class DefaultModelAdaptersLocator implements ModelAdaptersLocator {
    protected static final ClassNamePredicate FILTER_BY_NAME = (pkg, cls) -> cls.endsWith("JdbcAdapter");
    protected static final String NAME = "model adapter";

    protected final ImmutableListMultimap<Class<?>, Class<?>> index;

    protected DefaultModelAdaptersLocator(@NotNull Iterable<Class<?>> adapters) {
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

    public static @NotNull DefaultModelAdaptersLocator fromCurrentClassLoader() {
        return from(DefaultModelAdaptersLocator.class.getClassLoader());
    }

    public static @NotNull DefaultModelAdaptersLocator fromSystemClassLoader() {
        return from(ClassLoader.getSystemClassLoader());
    }

    public static @NotNull DefaultModelAdaptersLocator from(@NotNull ClassLoader classLoader) {
        return from(GuavaClasspathScanner.fromClassLoader(classLoader));
    }

    public static @NotNull DefaultModelAdaptersLocator from(@NotNull ClasspathScanner scanner) {
        Set<Class<?>> adapters = scanner.timed(NAME).getAnnotatedClasses(FILTER_BY_NAME, JdbcAdapt.class);
        return new DefaultModelAdaptersLocator(adapters);
    }

    @Override
    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        ImmutableList<Class<?>> adapters = index.get(model);
        return adapters.isEmpty() ? null : Iterables.getOnlyElement(adapters);
    }
}
