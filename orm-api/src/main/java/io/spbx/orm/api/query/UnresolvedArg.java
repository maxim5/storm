package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Immutable
public record UnresolvedArg(@NotNull String name, @Nullable Object defaultValue) {
    public static @Nullable Object defaultValueForType(@NotNull TermType type) {
        return switch (type) {
            case NUMBER -> 0;
            case BOOL -> false;
            case STRING -> "";
            default -> null;
        };
    }
}
