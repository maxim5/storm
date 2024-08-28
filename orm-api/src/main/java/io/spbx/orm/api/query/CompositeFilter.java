package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.Engine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static io.spbx.orm.api.query.Args.flattenArgsOf;
import static io.spbx.orm.api.query.Representables.joinWithLines;
import static java.util.Objects.requireNonNull;

/**
 * Represents one or more SQL filter clauses: <code>WHERE</code>, <code>ORDER BY</code>, <code>LIMIT</code> and
 * <code>OFFSET</code>.
 */
@Immutable
public class CompositeFilter extends Unit implements Filter {
    private final Where where;
    private final OrderBy orderBy;
    private final LimitClause limit;
    private final Offset offset;

    public CompositeFilter(@Nullable Where where,
                           @Nullable OrderBy orderBy,
                           @Nullable LimitClause limit,
                           @Nullable Offset offset) {
        super(joinWithLines(where, orderBy, limit, offset), flattenArgsOf(Arrays.asList(where, orderBy, limit, offset)));
        this.where = where;
        this.orderBy = orderBy;
        this.limit = limit;
        this.offset = offset;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @Nullable Where where() {
        return where;
    }

    public @Nullable OrderBy orderBy() {
        return orderBy;
    }

    public @Nullable LimitClause limit() {
        return limit;
    }

    public @Nullable Offset offset() {
        return offset;
    }

    public @NotNull Builder toBuilder() {
        return new Builder(where, orderBy, limit, offset);
    }

    public static class Builder {
        private Where where;
        private OrderBy orderBy;
        private LimitClause limit;
        private Offset offset;

        Builder() {
        }

        Builder(@Nullable Where where, @Nullable OrderBy orderBy, @Nullable LimitClause limit, @Nullable Offset offset) {
            this.where = where;
            this.orderBy = orderBy;
            this.limit = limit;
            this.offset = offset;
        }

        public @NotNull Builder with(@NotNull Where where) {
            this.where = this.where == null ? where : this.where.andTerm(where.term());
            return this;
        }

        public @NotNull Builder with(@NotNull OrderBy orderBy) {
            this.orderBy = this.orderBy == null ? orderBy : this.orderBy.withMoreTerms(orderBy.terms());
            return this;
        }

        public @NotNull Builder with(@NotNull LimitClause limit) {
            InvalidQueryException.assure(this.limit == null, "Duplicate limit specified: existing: %s, updated: %s", this.limit, limit);
            this.limit = limit;
            return this;
        }

        public @NotNull Builder with(@NotNull Offset offset) {
            InvalidQueryException.assure(this.offset == null, "Duplicate offset specified: existing: %s, updated: %s", this.offset, offset);
            this.offset = offset;
            return this;
        }

        public @NotNull Builder with(@NotNull Pagination pagination, @NotNull Engine engine) {
            if (pagination.hasLastItem()) {
                ColumnTerm lastItem = requireNonNull(pagination.lastItem());
                Order order = requireNonNull(pagination.order());
                with(Where.of((order == Order.ASC ? CompareType.GT : CompareType.LT).compare(lastItem.column(), lastItem.term())));
            }
            if (pagination.hasOffset()) {
                with(Offset.of(pagination.offset()));
            }
            switch (engine) {
                case H2, MySQL, MariaDB, PostgreSQL, SQLite -> with(Limit.of(pagination.limit()));
                case MsSqlServer, Oracle -> with(FetchOnly.of(pagination.limit()));
            }
            return this;
        }

        public @NotNull Builder with(@NotNull CompositeFilter filter) {
            if (filter.where != null) {
                with(filter.where);
            }
            if (filter.orderBy != null) {
                with(filter.orderBy);
            }
            if (filter.limit != null) {
                with(filter.limit);
            }
            if (filter.offset != null) {
                with(filter.offset);
            }
            return this;
        }

        public @NotNull CompositeFilter build() {
            return new CompositeFilter(where, orderBy, limit, offset);
        }
    }
}
