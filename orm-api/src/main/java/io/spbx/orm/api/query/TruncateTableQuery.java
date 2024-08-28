package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

@Immutable
public class TruncateTableQuery extends Unit implements DataDefinitionQuery {
    private TruncateTableQuery(@NotNull String query) {
        super(query);
    }

    public static @NotNull Builder of(@NotNull String tableName) {
        return new Builder(tableName);
    }

    public static @NotNull Builder of(@NotNull TableMeta meta) {
        return of(meta.sqlTableName());
    }

    public static @NotNull Builder of(@NotNull BaseTable<?> table) {
        return of(table.meta());
    }

    public static class Builder {
        private final String tableName;

        Builder(@NotNull String tableName) {
            this.tableName = tableName;
        }

        public @NotNull TruncateTableQuery build(@NotNull Engine engine) {
            String query = switch (engine) {
                case SQLite -> "DELETE FROM %s".formatted(tableName);
                default -> "TRUNCATE TABLE %s".formatted(tableName);
            };
            return new TruncateTableQuery(query);
        }
    }
}
