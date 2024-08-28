package io.spbx.orm.api.query;

import io.spbx.util.base.BasicRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an error related to query preparation or execution.
 */
public class InvalidQueryException extends BasicRuntimeException {
    public InvalidQueryException(@NotNull String message) {
        super(message);
    }

    private InvalidQueryException(@NotNull String message, @Nullable Object @NotNull[] args) {
        super(message, args);
    }

    public static @NotNull InvalidQueryException newInvalidQueryException(@NotNull String message,
                                                                          @Nullable Object @NotNull... args) {
        return new InvalidQueryException(message, args);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull... args) {
        if (!cond) {
            throw newInvalidQueryException(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull... args) {
        if (cond) {
            throw newInvalidQueryException(message, args);
        }
    }
}
