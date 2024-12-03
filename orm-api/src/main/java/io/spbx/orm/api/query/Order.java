package io.spbx.orm.api.query;

import javax.annotation.concurrent.Immutable;

/**
 * Represents the order direction in the {@code ORDER BY} query.
 */
@Immutable
public enum Order {
    ASC,
    DESC,
}
