package io.spbx.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.TableMeta.ColumnMeta;
import io.spbx.util.base.BasicExceptions.IllegalArgumentExceptions;
import io.spbx.util.collect.MapBuilder;
import io.spbx.util.text.BasicJoin;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.spbx.util.extern.guava.GuavaMaps.immutableMapOf;

@Immutable
public class CreateTableQuery extends Unit implements DataDefinitionQuery {
    private CreateTableQuery(@NotNull String query) {
        super(query);
    }

    public static @NotNull Builder of(@NotNull TableMeta meta) {
        return new Builder(meta);
    }

    public static @NotNull Builder of(@NotNull BaseTable<?> table) {
        return of(table.meta());
    }

    public static class Builder {
        private final TableMeta meta;
        private boolean ifNotExists;
        private boolean enforceForeignKey = true;

        Builder(@NotNull TableMeta meta) {
            this.meta = meta;
        }

        public @NotNull Builder ifNotExists() {
            ifNotExists = true;
            return this;
        }

        public @NotNull Builder withEnforceForeignKey(boolean value) {
            enforceForeignKey = value;
            return this;
        }

        public @NotNull CreateTableQuery build(@NotNull Engine engine) {
            SchemaSupport support = SchemaSupport.of(engine);
            boolean inlineForeignKeys = enforceForeignKey && support.canInlineForeignKey();
            boolean outlineForeignKeys = enforceForeignKey && !support.canInlineForeignKey();
            List<String> lines = new ArrayList<>();

            meta.sqlColumns().forEach(column -> {
                String definition = BasicJoin.of(
                    "%s %s".formatted(column.name(), support.columnTypeFor(column)),
                    column.isNotNull() ? support.inlineNotNull(column) : "",
                    column.primaryKey().isSingle() ? support.inlinePrimaryKeyFor(column) : "",
                    column.primaryKey().isSingle() ? support.inlineAutoIncrementFor(column) : "",
                    column.isForeignKey() && inlineForeignKeys ? support.inlineForeignKey(column) : "",
                    column.hasDefault() ? support.inlineDefaultFor(column) : "",
                    column.unique().isSingle() ? support.inlineUniqueFor(column) : ""
                ).onlyNonEmpty().join(" ");
                lines.add(definition);
            });

            if (meta.primaryKeys().isComposite()) {
                String columns = BasicJoin.of(meta.primaryKeys().columns()).join(", ");
                lines.add("PRIMARY KEY (%s)".formatted(columns));
            }

            if (outlineForeignKeys) {
                meta.sqlColumns().stream().filter(ColumnMeta::isForeignKey).forEach(column ->
                    lines.add(support.foreignKeyConstraint(column))
                );
            }

            meta.unique().forEach(constraint -> {
                if (constraint.isComposite()) {
                    String columns = BasicJoin.of(constraint.columns()).join(", ");
                    lines.add("UNIQUE (%s)".formatted(columns));
                }
            });

            String query = """
                CREATE TABLE %s%s (
                    %s
                )
                """.formatted(ifNotExists ? "IF NOT EXISTS " : "", meta.sqlTableName(), String.join(",\n    ", lines));
            return new CreateTableQuery(query);
        }
    }

    private interface SchemaSupport {
        @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta);

        default @NotNull String inlineNotNull(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isNotNull() ? "NOT NULL" : "";
        }

        default @NotNull String inlineUniqueFor(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isUnique() ? "UNIQUE" : "";
        }

        default @NotNull String inlinePrimaryKeyFor(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isPrimaryKey() ? "PRIMARY KEY" : "";
        }

        default @NotNull String inlineAutoIncrementFor(@NotNull ColumnMeta columnMeta) {
            return isAutoIncrement(columnMeta) ? "AUTO_INCREMENT" : "";
        }

        static boolean isAutoIncrement(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isPrimaryKey() && (columnMeta.type() == int.class || columnMeta.type() == long.class);
        }

        default @NotNull String inlineDefaultFor(@NotNull ColumnMeta column) {
            return column.hasDefault() && !isAutoIncrement(column) ? "DEFAULT (%s)".formatted(column.defaultValue()) : "";
        }

        default boolean canInlineForeignKey() {
            return false;
        }

        default @NotNull String inlineForeignKey(@NotNull ColumnMeta column) {
            TableMeta.ForeignColumn foreignColumn = column.foreignColumn();
            return foreignColumn == null ? "" :
                "FOREIGN KEY REFERENCES %s(%s)".formatted(foreignColumn.meta().sqlTableName(), foreignColumn.column());
        }

        default @NotNull String foreignKeyConstraint(@NotNull ColumnMeta column) {
            TableMeta.ForeignColumn foreign = column.foreignColumn();
            return foreign == null ? "" :
                "FOREIGN KEY(%s) REFERENCES %s(%s)".formatted(column.name(), foreign.meta().sqlTableName(), foreign.column());
        }

        static @NotNull SchemaSupport of(@NotNull Engine engine) {
            return switch (engine) {
                case SQLite -> new SqliteSchemaSupport();
                case MySQL, MariaDB -> new MySqlSchemaSupport();
                case H2 -> new H2SchemaSupport();
                default -> IllegalArgumentExceptions.fail("Engine not supported for table creation:", engine);
            };
        }
    }

    private abstract static class DefaultSchemaSupport implements SchemaSupport {
        protected static final Map<Class<?>, String> DEFAULT_DATA_TYPES = immutableMapOf(
            boolean.class, "BOOLEAN",
            byte.class, "TINYINT",
            short.class, "SMALLINT",
            int.class, "INTEGER",
            long.class, "BIGINT",
            float.class, "REAL",
            double.class, "DOUBLE",
            String.class, "VARCHAR",
            byte[].class, "BLOB",
            Time.class, "TIME",
            Date.class, "DATE",
            Timestamp.class, "TIMESTAMP"
        );

        protected static boolean isKey(@NotNull ColumnMeta columnMeta) {
            return columnMeta.isPrimaryKey() || columnMeta.isForeignKey();
        }
    }

    private static class SqliteSchemaSupport extends DefaultSchemaSupport {
        private static final Map<Class<?>, String> SQLITE_DATA_TYPES = immutableMapOf(
            float.class, "REAL",
            double.class, "REAL",
            String.class, "VARCHAR",
            byte[].class, "BLOB"
        );
        private static final Map<Class<?>, String> SQLITE_PK_DATA_TYPES = MapBuilder.copyOf(SQLITE_DATA_TYPES).overwrite(
            byte[].class, "VARCHAR"
        ).toGuavaImmutableMap();

        @Override
        public @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta) {
            return (isKey(columnMeta) ? SQLITE_PK_DATA_TYPES : SQLITE_DATA_TYPES).getOrDefault(columnMeta.type(), "INTEGER");
        }

        @Override
        public @NotNull String inlineAutoIncrementFor(@NotNull ColumnMeta columnMeta) {
            return "";  // Not recommended by https://www.sqlite.org/autoinc.html
        }
    }

    private static class MySqlSchemaSupport extends DefaultSchemaSupport {
        private static final Map<Class<?>, String> MYSQL_DATA_TYPES = MapBuilder.copyOf(DEFAULT_DATA_TYPES).overwrite(
            String.class, "VARCHAR(4096)",
            byte[].class, "BLOB",
            Timestamp.class, "TIMESTAMP(3)"
        ).toGuavaImmutableMap();
        // https://stackoverflow.com/questions/29292353/whats-the-difference-between-varchar-binary-and-varbinary-in-mysql
        private static final Map<Class<?>, String> MYSQL_PK_DATA_TYPES = MapBuilder.copyOf(MYSQL_DATA_TYPES).overwrite(
            String.class, "VARCHAR(255)",
            byte[].class, "VARBINARY(255)"
        ).toGuavaImmutableMap();

        @Override
        public @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta) {
            return (isKey(columnMeta) ? MYSQL_PK_DATA_TYPES : MYSQL_DATA_TYPES).get(columnMeta.type());
        }
    }

    private static class H2SchemaSupport extends DefaultSchemaSupport {
        private static final Map<Class<?>, String> H2_PK_DATA_TYPES = MapBuilder.copyOf(DEFAULT_DATA_TYPES).overwrite(
            byte[].class, "VARCHAR"
        ).toGuavaImmutableMap();

        @Override
        public @NotNull String columnTypeFor(@NotNull ColumnMeta columnMeta) {
            return (isKey(columnMeta) ? H2_PK_DATA_TYPES : DEFAULT_DATA_TYPES).get(columnMeta.type());
        }

        @Override
        public @NotNull String inlineUniqueFor(@NotNull ColumnMeta columnMeta) {
            // Index on BLOB or CLOB column not supported
            return !columnMeta.isUnique() || columnMeta.type() == byte[].class ? "" : "UNIQUE";
        }
    }
}
