package io.spbx.orm.arch;

import io.spbx.util.base.BasicRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an error related to SQL model processing and table generation.
 */
public class InvalidSqlModelException extends BasicRuntimeException {
    public InvalidSqlModelException() {
    }

    public InvalidSqlModelException(@NotNull String message) {
        super(message);
    }

    public InvalidSqlModelException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    private InvalidSqlModelException(@NotNull String message, @Nullable Object @NotNull[] args) {
        super(message, args);
    }

    public static @NotNull InvalidSqlModelException newInvalidSqlModelException(@NotNull String message,
                                                                                @Nullable Object @NotNull... args) {
        return new InvalidSqlModelException(message, args);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull... args) {
        if (!cond) {
            throw newInvalidSqlModelException(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull... args) {
        if (cond) {
            throw newInvalidSqlModelException(message, args);
        }
    }
}
