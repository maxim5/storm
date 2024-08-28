package io.spbx.orm.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An adapter which requires a single JDBC value to represent the entity.
 *
 * @param <E> the entity type
 */
public interface JdbcSingleValueAdapter<E> extends JdbcArrayAdapter<E> {
    /**
     * Returns the JDBC value corresponding to the {@code instance}.
     * <p>
     * The implementation may assume that {@code instance} is not-null if the adapter is only being used within
     * {@code io.spbx.orm.api.BaseTable} implementation. Otherwise, the implementation may handle the null as well.
     */
    Object toValueObject(E instance);

    @Override
    default void fillArrayValues(E instance, @Nullable Object @NotNull[] array, int start) {
        array[start] = toValueObject(instance);
    }

    @Override
    default @Nullable Object @NotNull[] toNewValuesArray(E instance) {
        Object[] array = new Object[1];
        fillArrayValues(instance, array, 0);
        return array;
    }
}
