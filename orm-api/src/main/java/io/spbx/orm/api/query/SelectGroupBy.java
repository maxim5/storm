package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.function.Consumer;

import static io.spbx.orm.api.query.Args.flattenArgsOf;
import static io.spbx.orm.api.query.Representables.joinWithLines;

/**
 * A standard {@code SELECT ... GROUP BY ...} query. Supports additional {@code HAVING} and
 * {@link CompositeFilter} clauses.
 */
@Immutable
public class SelectGroupBy extends Unit implements TypedSelectQuery {
    private final SelectFrom selectFrom;
    private final FuncExpr aggregate;
    private final Where where;
    private final GroupBy groupBy;
    private final Having having;
    private final OrderBy orderBy;

    public SelectGroupBy(@NotNull SelectFrom selectFrom, @NotNull FuncExpr aggregate, @Nullable Where where,
                         @NotNull GroupBy groupBy, @Nullable Having having, @Nullable OrderBy orderBy) {
        super(joinWithLines(selectFrom, where, groupBy, having, orderBy),
              flattenArgsOf(selectFrom, where, groupBy, having, orderBy));
        this.selectFrom = selectFrom;
        this.aggregate = aggregate;
        this.where = where;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
    }

    public static @NotNull Builder from(@NotNull String table) {
        return new Builder(table);
    }

    public static @NotNull Builder from(@NotNull TableMeta meta) {
        return from(meta.sqlTableName());
    }

    public static @NotNull Builder from(@NotNull BaseTable<?> table) {
        return from(table.meta());
    }

    public @NotNull SelectUnion union(@NotNull SelectQuery query) {
        return SelectUnion.builder().with(this).with(query).build();
    }

    @Override
    public int columnsNumber() {
        return selectFrom.terms().size();
    }

    @Override
    public @NotNull List<TermType> columnTypes() {
        return selectFrom.termsTypes();
    }

    public @NotNull Builder toBuilder() {
        return new Builder(selectFrom, aggregate, where, groupBy, having, orderBy);
    }

    public static class Builder {
        private final String table;
        private FuncExpr funcExpr;
        private final ImmutableList.Builder<Named> terms = ImmutableList.builder();
        private Having having;
        private final CompositeFilter.Builder filter;
    
        public Builder(@NotNull String table) {
            this.table = table;
            this.filter = new CompositeFilter.Builder();
        }

        Builder(@NotNull SelectFrom selectFrom, @NotNull FuncExpr aggregate, @Nullable Where where,
                @NotNull GroupBy groupBy, @Nullable Having having, @Nullable OrderBy orderBy) {
            this.table = selectFrom.table();
            this.funcExpr = aggregate;
            this.terms.addAll(groupBy.terms());
            this.having = having;
            this.filter = new CompositeFilter(where, orderBy, null, null).toBuilder();
        }

        public @NotNull Builder select(@NotNull Named term, @NotNull FuncExpr aggregate) {
            return groupBy(term).aggregate(aggregate);
        }
    
        public @NotNull Builder select(@NotNull List<Named> terms, @NotNull FuncExpr aggregate) {
            return groupBy(terms).aggregate(aggregate);
        }
    
        public @NotNull Builder aggregate(@NotNull FuncExpr aggregate) {
            InvalidQueryException.assure(funcExpr == null, "Aggregate function already set:", funcExpr);
            InvalidQueryException.assure(aggregate.isAggregate(), "Non-aggregate function supplied:", aggregate);
            funcExpr = aggregate;
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Named term) {
            terms.add(term);
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Named term1, @NotNull Named term2) {
            terms.add(term1, term2);
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Named @NotNull... terms) {
            this.terms.add(terms);
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Iterable<Named> terms) {
            this.terms.addAll(terms);
            return this;
        }
    
        public @NotNull Builder having(@NotNull Having having) {
            this.having = having;
            return this;
        }
    
        public @NotNull Builder where(@NotNull Where where) {
            filter.with(where);
            return this;
        }
    
        public @NotNull Builder orderBy(@NotNull OrderBy orderBy) {
            filter.with(orderBy);
            return this;
        }
    
        public @NotNull Builder applying(@NotNull Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }

        public @NotNull SelectUnion.Builder union(@NotNull SelectQuery query) {
            return build().union(query).toBuilder();
        }
    
        public @NotNull SelectGroupBy build() {
            InvalidQueryException.assure(funcExpr != null,
                                         "Aggregate function not provided for SelectGroupBy: table=%s", table);
            ImmutableList<Named> groupByTerms = terms.build();
            ImmutableList<Term> allTerms = ImmutableList.<Term>builder().addAll(groupByTerms).add(funcExpr).build();
            CompositeFilter composite = filter.build();
            return new SelectGroupBy(new SelectFrom(table, allTerms), funcExpr, composite.where(),
                                     new GroupBy(groupByTerms), having, composite.orderBy());
        }
    }
}
