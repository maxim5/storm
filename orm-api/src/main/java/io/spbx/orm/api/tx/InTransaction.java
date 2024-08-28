package io.spbx.orm.api.tx;

import io.spbx.util.base.Unchecked;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.util.func.ThrowFunction;
import io.spbx.util.func.ThrowRunnable;
import io.spbx.util.func.ThrowSupplier;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a runner within a DB transaction.
 *
 * @param <P> the parameter type to be used by the working lambdas
 */
public class InTransaction<P> {
    private final Connection connection;
    private final P param;

    public InTransaction(@NotNull Connection connection, @NotNull P param) {
        this.connection = connection;
        this.param = param;
    }

    public void run(@NotNull ThrowConsumer<P, SQLException> action) {
        try {
            runChecked(action);
        } catch (SQLException e) {
            Unchecked.rethrow("Transaction failed", e);
        }
    }

    public void run(@NotNull ThrowRunnable<SQLException> action) {
        try {
            runChecked(action);
        } catch (SQLException e) {
            Unchecked.rethrow("Transaction failed", e);
        }
    }

    public <T> T run(@NotNull ThrowFunction<P, T, SQLException> action) {
        try {
            return runChecked(action);
        } catch (SQLException e) {
            return Unchecked.rethrow("Transaction failed", e);
        }
    }

    public <T> T run(@NotNull ThrowSupplier<T, SQLException> action) {
        try {
            return runChecked(action);
        } catch (SQLException e) {
            return Unchecked.rethrow("Transaction failed", e);
        }
    }

    public <T> T runChecked(@NotNull ThrowFunction<P, T, SQLException> action) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            T t = action.apply(param);
            connection.commit();
            return t;
        } catch (SQLException | RuntimeException | Error exception) {
            connection.rollback();
            throw exception;
        } catch (Throwable throwable) {
            connection.rollback();
            return Unchecked.rethrow(throwable);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public void runChecked(@NotNull ThrowConsumer<P, SQLException> action) throws SQLException {
        runChecked(runner -> {
            action.accept(runner);
            return null;
        });
    }

    public void runChecked(@NotNull ThrowRunnable<SQLException> action) throws SQLException {
        runChecked(runner -> {
            action.run();
            return null;
        });
    }

    public <T> T runChecked(@NotNull ThrowSupplier<T, SQLException> action) throws SQLException {
        return runChecked(runner -> {
            return action.get();
        });
    }
}
