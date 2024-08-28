package io.spbx.orm.arch.model;

import com.google.errorprone.annotations.Immutable;
import io.spbx.util.collect.ImmutableArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Objects;

import static io.spbx.orm.arch.InvalidSqlModelException.assure;

@Immutable
public class Defaults {
    @VisibleForTesting
    static final Defaults EMPTY_COLUMN_DEFAULTS = new Defaults(new String[1]);

    private final ImmutableArray<String> values;

    @VisibleForTesting
    Defaults(@Nullable String @NotNull[] values) {
        this.values = ImmutableArray.copyOf(values);
    }

    public static @NotNull Defaults ofOneColumn(@Nullable String @Nullable ... values) {
        assure(isEmpty(values) || values.length == 1, "Defaults for a column contains more than one column value: `%s`",
               Arrays.toString(values));
        return isEmpty(values) || values[0] == null ? EMPTY_COLUMN_DEFAULTS : new Defaults(values);
    }

    public static @NotNull Defaults ofMultiColumns(int size, @Nullable String @Nullable ... values) {
        assure(isEmpty(values) || values.length == size, "Defaults number does not match the columns number %d: `%s`",
               size, Arrays.toString(values));
        return size == 1 ? ofOneColumn(values) : isEmpty(values) ? new Defaults(new String[size]) : new Defaults(values);
    }

    public int size() {
        return values.size();
    }

    public @Nullable String at(int index) {
        return values.get(index);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Defaults defaults && Objects.equals(values, defaults.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    private static boolean isEmpty(@Nullable String @Nullable[] values) {
        return values == null || values.length == 0;
    }
}
