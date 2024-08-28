package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

/**
 * Represents a class which can establish or maintain a JDBC {@link Connection}.
 */
public interface Connector extends HasRunner, HasEngine {
    /**
     * Creates or returns an existing JDBC connection.
     * Is not idempotent: derived implementations may reconnect if necessary and return a new {@link Connection}.
     */
    @NotNull Connection connection();

    /**
     * Returns the runner working on top of the {@link #connection()}.
     */
    @Override
    default @NotNull QueryRunner runner() {
        return new QueryRunner(connection());
    }

    /**
     * Returns the engine of the {@link #connection()}.
     */
    @Override
    default @NotNull Engine engine() {
        return Engine.fromConnectionSafe(connection());
    }
}
