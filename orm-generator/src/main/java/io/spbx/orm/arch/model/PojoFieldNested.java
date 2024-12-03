package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class PojoFieldNested extends PojoField {
    private final PojoArch pojo;

    protected PojoFieldNested(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull PojoArch pojo) {
        super(parent, field, TypeSupport.ADAPTER_API);
        this.pojo = pojo;
    }

    public static @NotNull PojoField ofNestedPojo(@NotNull ModelField field, @NotNull PojoArch pojo) {
        return newReattachedSubPojo(PojoParent.DETACHED, field, pojo);
    }

    public @NotNull PojoArch pojo() {
        return pojo;
    }

    @Override
    public @NotNull AdapterApi adapterApiOrDie() {
        return AdapterApi.ofSignature(pojo());
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return newReattachedSubPojo(parent, field, pojo());
    }

    private static @NotNull PojoField newReattachedSubPojo(@NotNull PojoParent parent,
                                                           @NotNull ModelField field,
                                                           @NotNull PojoArch pojo) {
        PojoParent thisParent = PojoParent.ofUninitializedField();
        PojoArch copy = pojo.reattachedTo(thisParent);
        PojoField thisField = new PojoFieldNested(parent, field, copy);
        thisParent.initializeOrDie(thisField);
        return thisField;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldNested that && super.equals(obj) && Objects.equals(this.pojo, that.pojo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pojo);
    }

    @Override
    public String toString() {
        return "PojoFieldNested[parent=%s, field=%s, pojo=%s]".formatted(parent, field, pojo);
    }
}
