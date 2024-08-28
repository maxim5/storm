package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.PageToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Immutable
public record Pagination(@Nullable ColumnTerm lastItem, @Nullable Order order, int offset, int limit) {
    public static final int NO_OFFSET = -1;

    public Pagination {
        assert offset > 0 || offset == NO_OFFSET : "Invalid offset: " + offset;
        assert (lastItem != null) == (order != null) : "`lastItem` and `order` must be set together";
        assert !(lastItem != null && offset > 0) : "Only one of `lastItem`/`order` and `offset` can be set";
        assert limit > 0 : "Page `limit` must be set: " + limit;
    }

    public static @NotNull Pagination firstPage(int limit) {
        return new Pagination(null, null, NO_OFFSET, limit);
    }

    public static @NotNull Pagination ofOffset(int offset, int limit) {
        return new Pagination(null, null, offset > 0 ? offset : NO_OFFSET, limit);
    }

    public static @NotNull Pagination ofColumn(@NotNull ColumnTerm lastItem, @NotNull Order order, int limit) {
        return new Pagination(lastItem, order, NO_OFFSET, limit);
    }

    public static @NotNull Pagination ofColumnAsc(@NotNull ColumnTerm lastItem, int limit) {
        return ofColumn(lastItem, Order.ASC, limit);
    }

    public static @NotNull Pagination ofColumnDesc(@NotNull ColumnTerm lastItem, int limit) {
        return ofColumn(lastItem, Order.DESC, limit);
    }

    public static @Nullable Pagination ofColumnIfMatches(@NotNull PageToken token,
                                                         @NotNull Column column, @NotNull Order order,
                                                         int limit) {
        if (token.hasLastItem()) {
            ColumnTerm columnTerm = new ColumnTerm(column, new Variable(token.lastItem(), TermType.WILDCARD));
            return ofColumn(columnTerm, order, limit);
        }
        return null;
    }

    public static @Nullable Pagination ofOffsetIfMatches(@NotNull PageToken token, int limit) {
        return token.hasOffset() ? ofOffset(token.offset(), limit) : null;
    }

    public boolean hasLastItem() {
        return lastItem != null;
    }

    public boolean hasOffset() {
        return offset != NO_OFFSET;
    }
}
