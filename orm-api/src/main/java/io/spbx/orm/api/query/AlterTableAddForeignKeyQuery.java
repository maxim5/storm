package io.spbx.orm.api.query;

import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.TableMeta.ColumnMeta;
import io.spbx.orm.api.TableMeta.ForeignColumn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Immutable
public class AlterTableAddForeignKeyQuery extends Unit implements AlterTableQuery {
    private AlterTableAddForeignKeyQuery(@NotNull String query) {
        super(query);
    }

    public static @NotNull Builder of(@NotNull TableMeta meta) {
        return new Builder(meta);
    }

    public static @NotNull Builder of(@NotNull BaseTable<?> table) {
        return of(table.meta());
    }

    public static @NotNull SingleForeignKeyBuilder of(@NotNull TableMeta meta, @NotNull ColumnMeta column) {
        return new SingleForeignKeyBuilder(meta, column);
    }

    public static @NotNull SingleForeignKeyBuilder of(@NotNull BaseTable<?> table, @NotNull ColumnMeta column) {
        return of(table.meta(), column);
    }

    public static class Builder {
        private final TableMeta meta;

        Builder(@NotNull TableMeta meta) {
            this.meta = meta;
        }

        public @NotNull List<AlterTableAddForeignKeyQuery> build(@NotNull Engine engine) {
            if (engine == Engine.SQLite) {
                // Unsupported:
                // https://stackoverflow.com/questions/1884818/how-do-i-add-a-foreign-key-to-an-existing-sqlite-table
                return List.of();
            }
            return meta.sqlColumns().stream()
                .filter(ColumnMeta::isForeignKey)
                .map(column -> new SingleForeignKeyBuilder(meta, column))
                .map(builder -> builder.build(engine))
                .toList();
        }
    }

    public static class SingleForeignKeyBuilder {
        private final TableMeta meta;
        private final ColumnMeta column;

        public SingleForeignKeyBuilder(@NotNull TableMeta meta, @NotNull ColumnMeta column) {
            InvalidQueryException.assure(column.isForeignKey(), "The column must be have a foreign key:", column);
            this.meta = meta;
            this.column = column;
        }

        public @NotNull AlterTableAddForeignKeyQuery build(@NotNull Engine engine) {
            ForeignColumn foreignColumn = requireNonNull(column.foreignColumn());
            String query = """
                ALTER TABLE %s
                ADD FOREIGN KEY (%s) REFERENCES %s(%s)
                """.formatted(meta.sqlTableName(), column.name(),
                              foreignColumn.meta().sqlTableName(), foreignColumn.column().name());
            return new AlterTableAddForeignKeyQuery(query);
        }
    }
}
