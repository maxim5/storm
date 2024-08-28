package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

import static io.spbx.orm.codegen.Joining.COMMA_JOINER;

class InsertMaker {
    private final Ignore ignore;

    public InsertMaker(@NotNull Ignore ignore) {
        this.ignore = ignore;
    }

    public @NotNull Snippet makeAll(@NotNull TableArch table) {
        return make(table, table.columns());
    }

    public @NotNull Snippet make(@NotNull TableArch table, @NotNull List<Column> columns) {
         return new Snippet()
             .appendLine("INSERT", ignore.value(), " INTO ", table.sqlName(),
                         " (", columns.stream().map(Column::sqlName).collect(COMMA_JOINER), ")")
             .appendLine("VALUES (", Stream.generate(() -> "?").limit(columns.size()).collect(COMMA_JOINER), ")");
    }

    enum Ignore {
        DEFAULT(""),
        IGNORE(" IGNORE"),
        OR_IGNORE(" OR IGNORE");

        private final String value;

        Ignore(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
