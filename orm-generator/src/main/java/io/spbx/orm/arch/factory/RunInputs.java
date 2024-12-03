package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Optional;

@Immutable
public class RunInputs {
    private final ImmutableList<ModelInput> models;
    private final ImmutableList<PojoInput> pojos;

    public RunInputs(@NotNull List<ModelInput> models, @NotNull List<PojoInput> pojos) {
        this.models = ImmutableList.copyOf(models);
        this.pojos = ImmutableList.copyOf(pojos);
    }

    public static @NotNull RunInputs of(@NotNull ModelInput @NotNull ... models) {
        return of(ImmutableList.copyOf(models), ImmutableList.of());
    }

    public static @NotNull RunInputs of(@NotNull List<ModelInput> models, @NotNull List<PojoInput> pojos) {
        return new RunInputs(models, pojos);
    }

    public @NotNull Optional<ModelInput> findInputByModel(@NotNull Class<?> modelClass) {
        return models.stream()
            .filter(input -> input.modelClass().equals(modelClass))
            .findFirst();
    }

    public @NotNull ImmutableList<ModelInput> models() {
        return models;
    }

    public @NotNull ImmutableList<PojoInput> pojos() {
        return pojos;
    }
}
