package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static io.spbx.orm.api.query.Args.flattenArgsOf;
import static io.spbx.orm.api.query.Representables.joinWithLines;

/**
 * A {@code SELECT ... FROM ... JOIN ...} query.
 */
@Immutable
public class SelectJoin extends Unit implements TypedSelectQuery {
    private final SelectFrom selectFrom;
    private final JoinOn joinOn;
    private final CompositeFilter clause;

    public SelectJoin(@NotNull SelectFrom selectFrom, @NotNull JoinOn joinOn, @NotNull CompositeFilter clause) {
        super(joinWithLines(selectFrom, joinOn, clause), flattenArgsOf(selectFrom, joinOn, clause));
        this.selectFrom = selectFrom;
        this.joinOn = joinOn;
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

    @Override
    public int columnsNumber() {
        return selectFrom.terms().size();
    }

    @Override
    public @NotNull List<TermType> columnTypes() {
        return selectFrom.termsTypes();
    }

    public @NotNull Builder toBuilder() {
        return new Builder(selectFrom, joinOn, clause);
    }

    public static class Builder {
        private final String table;
        private final ImmutableList.Builder<Term> terms = ImmutableList.builder();
        private JoinOn joinOn;
        private final CompositeFilter.Builder filter;

        public Builder(@NotNull String table) {
            this.table = table;
            this.filter = new CompositeFilter.Builder();
        }

        Builder(@NotNull SelectFrom selectFrom, @NotNull JoinOn joinOn, @NotNull CompositeFilter clause) {
            this.table = selectFrom.table();
            this.terms.addAll(selectFrom.terms());
            this.joinOn = joinOn;
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

        public @NotNull Builder select(@NotNull Iterable<Term> terms) {
            this.terms.addAll(terms);
            return this;
        }

        public @NotNull Builder leftJoinOn(@NotNull FullColumn fullColumn) {
            return joinOn(JoinOn.of(JoinType.LEFT_JOIN, fullColumn.table(), table, fullColumn));
        }

        public @NotNull Builder innerJoinOn(@NotNull FullColumn fullColumn) {
            return joinOn(JoinOn.of(JoinType.INNER_JOIN, fullColumn.table(), table, fullColumn));
        }

        public @NotNull Builder joinOn(@NotNull JoinOn joinOn) {
            InvalidQueryException.assure(this.joinOn == null, "Duplicate JoinOn specified: existing: %s, updated: %s", this.joinOn, joinOn);
            this.joinOn = joinOn;
            return this;
        }

        public @NotNull Builder orderBy(@NotNull OrderBy orderBy) {
            filter.with(orderBy);
            return this;
        }

        public @NotNull Builder orderBy(@NotNull Term term) {
            return orderBy(OrderBy.of(term, Order.ASC));
        }

        public @NotNull SelectJoin build() {
            return new SelectJoin(new SelectFrom(table, terms.build()), joinOn, filter.build());
        }
    }
}
