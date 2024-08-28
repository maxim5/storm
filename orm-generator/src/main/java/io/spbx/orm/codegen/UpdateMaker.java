package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.spbx.orm.codegen.Joining.COMMA_JOINER;
import static io.spbx.orm.codegen.SqlSupport.EQ_QUESTION;

class UpdateMaker {
    public static @NotNull Snippet make(@NotNull TableArch table, @NotNull List<Column> columns) {
        assert !columns.isEmpty() : "No columns to update: " + table.sqlName();
        return new Snippet()
            .appendLine("UPDATE ", table.sqlName())
            .appendLine("SET ", columns.stream().map(Column::sqlName).map(EQ_QUESTION::formatted).collect(COMMA_JOINER));
    }
}
