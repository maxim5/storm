package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.function.Consumer;

import static io.spbx.orm.api.query.Args.flattenArgsOf;
import static io.spbx.orm.api.query.Representables.joinWithLines;

/**
 * A standard {@code SELECT ... FROM ... WHERE ...} query. Supports additional {@link CompositeFilter} clauses.
 */
@Immutable
public class SelectWhere extends Unit implements TypedSelectQuery {
    private final SelectFrom selectFrom;
    private final CompositeFilter clause;

    public SelectWhere(@NotNull SelectFrom selectFrom, @NotNull CompositeFilter clause) {
        super(joinWithLines(selectFrom, clause), flattenArgsOf(selectFrom, clause));
        this.selectFrom = selectFrom;
        this.clause = clause;
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
        return new Builder(selectFrom, clause);
    }

    public static class Builder {
        private final String table;
        private final ImmutableList.Builder<Term> terms = ImmutableList.builder();
        private final CompositeFilter.Builder filter;
    
        public Builder(@NotNull String table) {
            this.table = table;
            this.filter = new CompositeFilter.Builder();
        }

        Builder(@NotNull SelectFrom selectFrom, @NotNull CompositeFilter clause) {
            this.table = selectFrom.table();
            this.terms.addAll(selectFrom.terms());
            this.filter = clause.toBuilder();
        }

        public @NotNull Builder select(@NotNull Term term) {
            terms.add(term);
            return this;
        }
    
        public @NotNull Builder select(@NotNull Term term1, @NotNull Term term2) {
            terms.add(term1, term2);
            return this;
        }
    
        public @NotNull Builder select(@NotNull Term @NotNull ... terms) {
            this.terms.add(terms);
            return this;
        }
    
        public @NotNull Builder select(@NotNull Iterable<? extends Term> terms) {
            this.terms.addAll(terms);
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

        public @NotNull Builder orderBy(@NotNull Term term) {
            return orderBy(OrderBy.of(term, Order.ASC));
        }
    
        public @NotNull Builder with(@NotNull LimitClause limit) {
            filter.with(limit);
            return this;
        }
    
        public @NotNull Builder with(@NotNull Offset offset) {
            filter.with(offset);
            return this;
        }
    
        public @NotNull Builder with(@NotNull Pagination pagination, @NotNull Engine engine) {
            filter.with(pagination, engine);
            return this;
        }
    
        public @NotNull Builder applying(@NotNull Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }

        public @NotNull SelectUnion.Builder union(@NotNull SelectQuery query) {
            return build().union(query).toBuilder();
        }
    
        public @NotNull SelectWhere build() {
            return new SelectWhere(new SelectFrom(table, terms.build()), filter.build());
        }
    }
}
