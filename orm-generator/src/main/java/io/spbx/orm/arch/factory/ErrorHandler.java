package io.spbx.orm.arch.factory;

import io.spbx.orm.arch.InvalidSqlModelException;
import io.spbx.orm.arch.util.JavaField;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

class ErrorHandler {
    private final AtomicReference<ModelInput> currentModel = new AtomicReference<>();
    private final AtomicReference<JavaField> currentField = new AtomicReference<>();

    public void setCurrentModel(@NotNull ModelInput input) {
        currentModel.set(input);
    }

    public void setCurrentField(@NotNull JavaField field) {
        currentField.set(field);
    }

    public void dropCurrentField() {
        currentField.set(null);
    }

    public @NotNull RuntimeException handleRuntimeException(@NotNull RuntimeException e) {
        ModelInput modelInput = currentModel.get();
        JavaField field = currentField.get();
        if (modelInput == null) {
            return e;
        }

        String message = field == null ?
            "Error while processing the model: `%s`".formatted(modelInput.modelClass().getSimpleName()) :
            "Error while processing the model: `%s.%s`".formatted(field.ownerClass().getSimpleName(), field.getName());
        return new InvalidSqlModelException(message, e);
    }
}
