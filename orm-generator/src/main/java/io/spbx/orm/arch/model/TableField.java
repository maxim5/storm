package io.spbx.orm.arch.model;

import io.spbx.orm.api.ReadFollow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Immutable
public abstract class TableField implements HasColumns, HasPrefixedColumns {
    protected final TableArch parent;
    protected final ModelField field;
    protected final boolean primaryKey;
    protected final boolean unique;
    protected final boolean nullable;
    protected final Defaults defaults;
    protected final MapperApi mapperApi;
    protected final AdapterApi adapterApi;
    protected final TypeSupport typeSupport;

    public TableField(@NotNull TableArch parent,
                      @NotNull ModelField field,
                      boolean primaryKey,
                      boolean unique,
                      boolean nullable,
                      @NotNull Defaults defaults,
                      @Nullable MapperApi mapperApi,
                      @Nullable AdapterApi adapterApi) {
        assert !(mapperApi != null && adapterApi != null) : "Field can't have both mapper and adapter: " + field;
        this.parent = parent;
        this.field = field;
        this.primaryKey = primaryKey;
        this.unique = unique;
        this.nullable = nullable;
        this.defaults = defaults;
        this.mapperApi = mapperApi;
        this.adapterApi = adapterApi;
        this.typeSupport = TypeSupport.detectFrom(isForeignKey(), mapperApi, adapterApi);
    }

    public @NotNull TableArch parent() {
        return parent;
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaAccessor() {
        return field.accessor();
    }

    public @NotNull Class<?> javaType() {
        return field.type();
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isNotPrimaryKey() {
        return !primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNotNull() {
        return !nullable;
    }

    public boolean isForeignKey() {
        return false;
    }

    public @NotNull Defaults defaults() {
        return defaults;
    }

    public @Nullable String columnDefault(@NotNull Column column) {
        int index = columns().indexOf(column);
        assert index >= 0 : "The column not found in the field: field_columns=%s column=%s".formatted(columns(), column);
        return defaults.at(index);
    }

    public @NotNull TypeSupport typeSupport() {
        return typeSupport;
    }

    public boolean isNativelySupportedType() {
        return typeSupport == TypeSupport.NATIVE;
    }

    public boolean isMapperSupportedType() {
        return typeSupport == TypeSupport.MAPPER_API;
    }

    public boolean isAdapterSupportType() {
        return typeSupport == TypeSupport.ADAPTER_API;
    }

    public @NotNull MapperApi mapperApiOrDie() {
        return requireNonNull(mapperApi);
    }

    public @NotNull AdapterApi adapterApiOrDie() {
        return requireNonNull(adapterApi);
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        return columns().stream().map(column -> column.prefixed(parent.sqlName())).toList();
    }

    @Override
    public String toString() {
        return "%s(%s::%s, primary:%s, unique:%s, null:%s, defaults:%s)".formatted(
            getClass().getSimpleName(),
            parent.javaName(),
            field.name(),
            primaryKey,
            unique,
            nullable,
            defaults
        );
    }

    public enum TypeSupport {
        NATIVE,
        FOREIGN_KEY,
        MAPPER_API,
        ADAPTER_API;

        static @NotNull TypeSupport detectFrom(boolean isForeignKey, @Nullable MapperApi mapper, @Nullable AdapterApi adapter) {
            if (mapper != null) {
                return TypeSupport.MAPPER_API;
            }
            if (adapter != null) {
                return TypeSupport.ADAPTER_API;
            }
            if (isForeignKey) {
                return TypeSupport.FOREIGN_KEY;
            }
            return TypeSupport.NATIVE;
        }
    }
}
