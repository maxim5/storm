package io.spbx.orm.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an adapter that can convert the entity from an array and back for JDBC communication.
 * The array must contain only JDBC acceptable types, otherwise the JDBC {@link java.sql.Connection}
 * won't understand them.
 * <p>
 * The adapter should be annotated by {@link JdbcAdapt} and by convention called <code>%Type%JdbcAdapter</code>,
 * so that the system could identify it in the classpath and assign to corresponding values automatically.
 * This is recommended though optional, because the adapter can be assigned manually to a field using
 * {@link io.spbx.orm.api.annotate.Sql} or {@link io.spbx.orm.api.annotate.Sql.Via} annotation.
 * <p>
 * In addition to the methods below, the adapter is expected to implement a creation method with a signature:
 * <pre>
 *     public E createInstance(T1 value1, T2 value2, ...)
 * </pre>
 * Where <code>T1</code>, <code>T2</code>, etc. are all JDBC values necessary for the entity. The number of values
 * must match the length of an array from {@link #toNewValuesArray(Object)}.
 * <p>
 * Finally, the adapter is expected to expose a public static final instance, usually called <code>ADAPTER</code>.
 * It's not mandatory, but recommended for efficiency reasons.
 *
 * @param <E> the entity type
 */
public interface JdbcArrayAdapter<E> {
    /**
     * Fills the specified {@code array} with the JDBC representation of the {@code instance}.
     * The first index is indicated by {@code start}.
     * <p>
     * The implementation may assume that {@code instance} is not-null if the adapter is only being used within
     * {@code io.spbx.orm.api.BaseTable} implementation. Otherwise, the implementation may handle the null as well.
     */
    void fillArrayValues(E instance, @Nullable Object @NotNull[] array, int start);

    /**
     * Returns a newly allocated array holding the full JDBC representation of the {@code instance}.
     * <p>
     * Typically, the implementation just allocates the array and delegates to
     * {@link #fillArrayValues(Object, Object[], int)}.
     * <p>
     * The implementation may assume that {@code instance} is not-null if the adapter is only being used within
     * {@code io.spbx.orm.api.BaseTable} implementation. Otherwise, the implementation may handle the null as well.
     */
    @Nullable Object @NotNull[] toNewValuesArray(E instance);
}
