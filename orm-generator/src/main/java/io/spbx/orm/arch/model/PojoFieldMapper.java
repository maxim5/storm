package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class PojoFieldMapper extends PojoField {
    private final MapperApi mapperApi;

    protected PojoFieldMapper(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull MapperApi mapperApi) {
        super(parent, field, TypeSupport.MAPPER_API);
        this.mapperApi = mapperApi;
    }

    public static @NotNull PojoField ofMapper(@NotNull ModelField field, @NotNull MapperApi mapperApi) {
        return new PojoFieldMapper(PojoParent.DETACHED, field, mapperApi);
    }

    public @NotNull JdbcType jdbcType() {
        return mapperApi.jdbcType();
    }

    public @NotNull Column column() {
        return mapperApi.mapperColumn(fullSqlName());
    }

    @Override
    public @NotNull MapperApi mapperApiOrDie() {
        return mapperApi;
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return new PojoFieldMapper(parent, field, mapperApi);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldMapper that && super.equals(obj) && Objects.equals(this.mapperApi, that.mapperApi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mapperApi);
    }

    @Override
    public String toString() {
        return "PojoFieldMapper[parent=%s, field=%s, mapper=%s]".formatted(parent, field, mapperApi);
    }
}
