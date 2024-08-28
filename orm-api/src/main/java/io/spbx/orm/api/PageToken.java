package io.spbx.orm.api;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Represents a token for requesting the next page. Can be one of:
 * <ul>
 *     <li>the integer offset from the start</li>
 *     <li>the last item of the previous page in a string form</li>
 * </ul>
 *
 * @see Page
 */
@Immutable
public record PageToken(@Nullable String lastItem, int offset) {
    private static final int NO_OFFSET = -1;

    public static @NotNull PageToken ofLastItem(@NotNull String lastItem) {
        return new PageToken(lastItem, NO_OFFSET);
    }

    public static @NotNull PageToken ofOffset(int offset) {
        return new PageToken(null, offset);
    }

    public boolean hasLastItem() {
        return lastItem != null;
    }

    public boolean hasOffset() {
        return offset != NO_OFFSET;
    }

    public @NotNull String serializeHumanToken(@NotNull Preference preference) {
        if (lastItem != null && preference == Preference.PREFER_LAST_ITEM) {
            return lastItem;
        }
        return ":" + offset;
    }

    public static @Nullable PageToken parseHumanTokenOrNull(@NotNull String token) {
        return token.startsWith(":") ? parseOffset(token) : PageToken.ofLastItem(token);
    }

    public static @Nullable PageToken parseHumanTokenOrNull(@NotNull String token,
                                                            @NotNull Preference preference) {
        return switch (preference) {
            case PREFER_LAST_ITEM -> PageToken.ofLastItem(token);
            case PREFER_OFFSET -> token.startsWith(":") ? requireNonNull(parseOffset(token)) : null;
        };
    }

    public static @NotNull Optional<PageToken> parseHumanToken(@NotNull String token) {
        return Optional.ofNullable(parseHumanTokenOrNull(token));
    }

    public static @NotNull Optional<PageToken> parseHumanToken(@NotNull String token, @NotNull Preference preference) {
        return Optional.ofNullable(parseHumanTokenOrNull(token, preference));
    }

    private static @Nullable PageToken parseOffset(@NotNull String token) {
        try {
            int offset = Integer.parseInt(token.substring(1));
            return PageToken.ofOffset(offset);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public enum Preference {
        PREFER_LAST_ITEM,
        PREFER_OFFSET,
    }
}
