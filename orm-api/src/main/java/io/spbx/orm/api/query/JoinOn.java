package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class JoinOn extends Unit {
    public JoinOn(@NotNull JoinType joinType, @NotNull String tableName, @NotNull BoolTerm term) {
        super("""
            %s %s
            ON %s
            """.formatted(joinType.name().replace('_', ' '), tableName, term));
    }

    public static @NotNull JoinOn of(@NotNull JoinType joinType,
                                     @NotNull String table1,
                                     @NotNull String table2,
                                     @NotNull Column column) {
        return new JoinOn(joinType, table1, CompareType.EQ.compare(column.fullFrom(table2), column.fullFrom(table1)));
    }

    public static @NotNull JoinOn of(@NotNull JoinType joinType,
                                     @NotNull String table1,
                                     @NotNull String table2,
                                     @NotNull Column column1,
                                     @NotNull Column column2) {
        return new JoinOn(joinType, table1, CompareType.EQ.compare(column2.fullFrom(table2), column1.fullFrom(table1)));
    }
}
