package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static io.spbx.orm.api.query.Args.flattenArgsOf;
import static io.spbx.orm.api.query.Representables.joinWithCommas;

@Immutable
public class SelectFrom extends Unit {
    private static final String PATTERN = """
        SELECT %s
        FROM %s""";

    private final String table;
    private final ImmutableList<Term> terms;

    public SelectFrom(@NotNull String table, @NotNull List<? extends Term> terms) {
        super(PATTERN.formatted(joinWithCommas(terms), table), flattenArgsOf(terms));
        InvalidQueryException.assure(!terms.isEmpty(), "No terms provided for select query: table=%s", table);
        this.table = table;
        this.terms = ImmutableList.copyOf(terms);
    }

    public @NotNull String table() {
        return table;
    }

    public @NotNull ImmutableList<Term> terms() {
        return terms;
    }

    public @NotNull List<TermType> termsTypes() {
        return terms.stream().map(Term::type).toList();
    }
}
