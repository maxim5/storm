package io.spbx.orm.api;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a page of items and an (optional) token for the next page.
 */
@Immutable
public record Page<E>(@NotNull List<E> items, @Nullable PageToken nextToken) {
    /**
     * Returns whether there exist a next page or this page is last.
     */
    public boolean hasNextPage() {
        return nextToken != null;
    }
}
