package io.spbx.orm.codegen;

import io.spbx.orm.api.ReadFollow;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.PrefixedColumn;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

class SelectMaker {
    private final TableArch table;

    public SelectMaker(@NotNull TableArch table) {
        this.table = table;
    }

    public @NotNull Snippet make(@NotNull ReadFollow follow) {
        Function<PrefixedColumn, String> toSqlName =
                follow == ReadFollow.NO_FOLLOW ?
                        PrefixedColumn::sqlName :
                        PrefixedColumn::sqlPrefixedName;

        String sqlName = table.sqlName();
        List<String> columns = table.columns(follow).stream().map(toSqlName).toList();
        List<LeftJoin> joins = table.foreignFields(follow).stream().map(LeftJoin::from).toList();
        return compose(sqlName, columns, joins);
    }

    private static @NotNull Snippet compose(@NotNull String table,
                                            @NotNull List<String> columns,
                                            @NotNull List<LeftJoin> joins) {
        return new Snippet()
                .appendFormattedLine("SELECT %s", String.join(", ", columns))
                .appendFormattedLine("FROM %s", table)
                .appendLines(joins.stream().map(join -> "LEFT JOIN %s ON %s".formatted(join.table, join.on)));
    }

    private record LeftJoin(@NotNull String table, @NotNull String on) {
        public static @NotNull LeftJoin from(@NotNull ForeignTableField field) {
            String on = "%s.%s = %s.%s".formatted(field.parent().sqlName(),
                                                  field.foreignKeyColumn().sqlName(),
                                                  field.getForeignTable().sqlName(),
                                                  field.primaryKeyColumnInForeignTable().sqlName());
            return new LeftJoin(field.getForeignTable().sqlName(), on);
        }
    }
}
