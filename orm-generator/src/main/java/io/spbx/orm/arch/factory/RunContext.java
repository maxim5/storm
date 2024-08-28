package io.spbx.orm.arch.factory;

import io.spbx.orm.codegen.ModelAdaptersLocator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

class RunContext implements AutoCloseable {
    private final AtomicBoolean closed = new AtomicBoolean();
    private final RunInputs inputs;
    private final ModelAdaptersLocator adapters;
    private final PojoArchCollector pojos;
    private final TableArchCollector tables;
    private final ErrorHandler errorHandler;

    public RunContext(@NotNull RunInputs inputs, @NotNull ModelAdaptersLocator adapters) {
        this.inputs = inputs;
        this.adapters = adapters;
        this.pojos = new PojoArchCollector();
        this.tables = new TableArchCollector();
        this.errorHandler = new ErrorHandler();
    }

    public @NotNull RunInputs inputs() {
        assert !closed.get() : "RunContext is closed";
        return inputs;
    }

    public @NotNull ModelAdaptersLocator adapters() {
        assert !closed.get() : "RunContext is closed";
        return adapters;
    }

    public @NotNull PojoArchCollector pojos() {
        assert !closed.get() : "RunContext is closed";
        return pojos;
    }

    public @NotNull TableArchCollector tables() {
        assert !closed.get() : "RunContext is closed";
        return tables;
    }

    public @NotNull ErrorHandler errorHandler() {
        return errorHandler;
    }

    @Override
    public void close() {
        closed.set(true);
    }
}
