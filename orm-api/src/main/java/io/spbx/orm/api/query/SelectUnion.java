package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spbx.util.collect.Streamer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.spbx.orm.api.query.Args.flattenArgsOf;
import static io.spbx.orm.api.query.InvalidQueryException.newInvalidQueryException;

/**
 * A <code>UNION</code> of several {@link SelectQuery} statements.
 */
@Immutable
public class SelectUnion extends Unit implements TypedSelectQuery {
    private final ImmutableList<SelectQuery> selects;
    private final int columnsNumber;

    public SelectUnion(@NotNull ImmutableList<SelectQuery> selects) {
        super(Streamer.of(selects).map(Representables::trimmed).join("\nUNION\n"), flattenArgsOf(selects));
        InvalidQueryException.assure(!selects.isEmpty(), "No select queries provided");
        InvalidQueryException.assure(selects.size() > 1, "A single select query provided for a union:", selects);

        List<TypedSelectQuery> typedSelects = asTypedSelectQueries(selects);
        this.selects = selects;
        this.columnsNumber = typedSelects.stream().map(TypedSelectQuery::columnsNumber).findFirst()
            .orElseThrow(() -> newInvalidQueryException("Failed to detect columns number:", selects));
    }

    private static @NotNull List<TypedSelectQuery> asTypedSelectQueries(@NotNull ImmutableList<SelectQuery> selects) {
        List<TypedSelectQuery> typedSelects = selects.stream()
            .map(query -> query instanceof TypedSelectQuery typed ? typed : null)
            .filter(Objects::nonNull)
            .toList();
        InvalidQueryException.assure(!typedSelects.isEmpty(), "Provided select queries are all non-typed:", selects);
        InvalidQueryException.assure(Streamer.of(typedSelects).map(TypedSelectQuery::columnsNumber).allEqual(),
                                     "Provided select queries have different number of columns:", selects);
        return typedSelects;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public int columnsNumber() {
        return columnsNumber;
    }

    @Override
    public @NotNull List<TermType> columnTypes() {
        return Collections.nCopies(columnsNumber, TermType.WILDCARD);  // detect from the typed queries?
    }

    public @NotNull Builder toBuilder() {
        return new Builder().with(selects);
    }

    public static class Builder {
        private final ImmutableList.Builder<SelectQuery> selects = new ImmutableList.Builder<>();

        public @NotNull Builder with(@NotNull SelectQuery query) {
            selects.add(query);
            return this;
        }

        public @NotNull Builder with(@NotNull Iterable<SelectQuery> queries) {
            selects.addAll(queries);
            return this;
        }

        public @NotNull SelectUnion build() {
            return new SelectUnion(selects.build());
        }
    }
}
