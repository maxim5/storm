package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.orm.arch.model.BridgeInfo;
import io.spbx.orm.arch.model.PojoArch;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import io.spbx.orm.arch.util.JavaClassAnalyzer;
import io.spbx.orm.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

class ArchFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final ModelAdaptersLocator locator;

    public ArchFactory(@NotNull ModelAdaptersLocator locator) {
        this.locator = locator;
    }

    public @NotNull RunResult build(@NotNull RunInputs inputs) {
        final RunContext runContext = new RunContext(inputs, locator);
        try (runContext) {
            for (ModelInput modelInput : runContext.inputs().models()) {
                log.at(Level.FINE).log("Stage 1 (shallow inspection): %s", modelInput.javaModelName());
                TableArch table = buildShallowTable(modelInput);
                modelInput.keys().forEach(key -> runContext.tables().putTable(key, table));
            }
            for (ModelInput modelInput : runContext.inputs().models()) {
                log.at(Level.FINE).log("Stage 2 (deep inspection): %s", modelInput.javaModelName());
                completeTable(modelInput, runContext);
            }
            for (PojoInput pojoInput : runContext.inputs().pojos()) {
                log.at(Level.FINE).log("Stage 3 (pojo): %s", pojoInput.pojoClass().getSimpleName());
                buildPojo(pojoInput, runContext);
            }
            return new RunResult(runContext.tables().getAllTables(), runContext.pojos().getAdapterArches());
        } catch (RuntimeException e) {
            throw runContext.errorHandler().handleRuntimeException(e);
        }
    }

    private @NotNull TableArch buildShallowTable(@NotNull ModelInput modelInput) {
        return new TableArch(
            modelInput.sqlName(), modelInput.javaTableName(), modelInput.modelClass(), modelInput.javaModelName(),
            BridgeInfo.fromModelClass(modelInput.modelClass())
        );
    }

    private void completeTable(@NotNull ModelInput modelInput, @NotNull RunContext runContext) {
        runContext.errorHandler().setCurrentModel(modelInput);
        TableArch table = runContext.tables().getTableOrDie(modelInput.modelClass());

        ImmutableList<TableField> fields = JavaClassAnalyzer.getAllFieldsOrdered(modelInput.modelClass()).stream()
            .map(field -> {
                runContext.errorHandler().setCurrentField(field);
                return new TableFieldArchFactory(runContext, table, field).buildTableField();
            })
            .collect(ImmutableList.toImmutableList());
        runContext.errorHandler().dropCurrentField();

        table.initializeOrDie(fields);
        table.validate();
    }

    @CanIgnoreReturnValue
    private @NotNull PojoArch buildPojo(@NotNull PojoInput pojoInput, @NotNull RunContext runContext) {
        return new RecursivePojoArchFactory(runContext).buildPojoArchFor(pojoInput.pojoClass());
    }
}
