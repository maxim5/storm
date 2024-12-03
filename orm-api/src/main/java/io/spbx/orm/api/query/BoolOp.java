package io.spbx.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static io.spbx.orm.api.query.Args.flattenArgsOf;

@Immutable
public class BoolOp extends Unit implements BoolTerm {
    private final ImmutableList<BoolTerm> terms;
    private final BoolOpType type;

    public BoolOp(@NotNull List<BoolTerm> terms, @NotNull BoolOpType type) {
        super(composeRepr(terms, type), flattenArgsOf(terms));
        this.terms = ImmutableList.copyOf(terms);
        this.type = type;
    }

    private static @NotNull String composeRepr(@NotNull List<BoolTerm> terms, @NotNull BoolOpType type) {
        boolean isCompositeThis = terms.size() > 1;
        return terms.stream().map(term -> {
            String repr = term.repr();
            return isCompositeThis && isComposite(term) ? "(%s)".formatted(repr) : repr;
        }).collect(type.joiner());
    }

    static boolean isComposite(@NotNull BoolTerm term) {
        if (term instanceof BoolOp boolOp) {
            return boolOp.terms.size() > 1;
        }
        if (term instanceof HardcodedBoolTerm) {
            return true;
        }
        return false;
    }
}
