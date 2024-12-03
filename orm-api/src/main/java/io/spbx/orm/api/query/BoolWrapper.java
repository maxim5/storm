package io.spbx.orm.api.query;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

/**
 * A wrapper around any term of type {@link TermType#BOOL} type that inherits from {@link BoolTerm}.
 */
@Immutable
public class BoolWrapper extends Unit implements BoolTerm {
    public BoolWrapper(@NotNull Term term) {
        super(term.repr(), term.args());
        assert term.type() == TermType.BOOL || term.type() == TermType.WILDCARD :
            "Term can't be used for boolean: %s".formatted(term);
    }
}
