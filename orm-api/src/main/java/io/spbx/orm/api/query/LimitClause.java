package io.spbx.orm.api.query;

public interface LimitClause extends Filter, Representable {
    int limitValue();
}
