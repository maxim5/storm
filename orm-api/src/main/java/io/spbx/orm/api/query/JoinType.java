package io.spbx.orm.api.query;

import javax.annotation.concurrent.Immutable;

@Immutable
public enum JoinType {
    INNER_JOIN,
    LEFT_JOIN,
    RIGHT_JOIN,
    FULL_JOIN,
    LEFT_OUTER_JOIN,
    RIGHT_OUTER_JOIN,
    FULL_OUTER_JOIN,
    NATURAL_JOIN,
    CROSS_JOIN,
}
