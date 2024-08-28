package io.spbx.orm.api;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.LazyArgs;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.orm.api.query.AlterTableAddForeignKeyQuery;
import io.spbx.orm.api.query.AlterTableQuery;
import io.spbx.orm.api.query.Args;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.orm.api.query.DataDefinitionQuery;
import io.spbx.orm.api.query.DropTableQuery;
import io.spbx.orm.api.query.HardcodedSelectQuery;
import io.spbx.orm.api.query.TruncateTableQuery;
import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import static io.spbx.util.base.BasicExceptions.notImplemented;

/**
 * An admin API to the database, e.g. DDL queries for DB schema manipulation.
 *
 * @see DataDefinitionQuery
 */
public class DbAdmin {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Connector connector;

    public DbAdmin(@NotNull Connector connector) {
        this.connector = connector;
    }

    public static @NotNull DbAdmin ofFixed(@NotNull Connection connection) {
        return new DbAdmin(() -> connection);
    }

    public static @NotNull DbAdmin ofFixed(@NotNull QueryRunner runner) {
        return new DbAdmin(runner::connection);
    }

    public @Nullable String getDatabase() {
        try {
            String catalog = connector.connection().getCatalog();
            if (catalog != null) {
                return catalog;
            }
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }

        return switch (engine()) {
            case MySQL, MariaDB -> runner().runAndGetString(HardcodedSelectQuery.of("SELECT DATABASE()"));
            case SQLite ->
                runner().runAndGetString(HardcodedSelectQuery.of("SELECT name FROM pragma_database_list LIMIT 1"));
            default -> throw notImplemented("Failed to get the current database for %s DB", engine());
        };
    }

    public @NotNull DbAdmin ignoringForeignKeyChecks() {
        return new DbAdmin(connector) {
            @Override
            protected void doRunUpdate(@NotNull DataDefinitionQuery query) {
                try {
                    DbAdmin.this.setOptionIfSupported("foreign_key_checks", "0");
                    super.doRunUpdate(query);
                } finally {
                    DbAdmin.this.setOptionIfSupported("foreign_key_checks", "1");
                }
            }
        };
    }

    @CanIgnoreReturnValue
    public boolean setOptionIfSupported(@NotNull String name, @NotNull String value) {
        if (engine().isOneOf(Engine.MySQL, Engine.MariaDB, Engine.H2)) {
            doRunUpdate(hardcoded("SET %s = %s".formatted(name, value)));
            return true;
        }
        return false;
    }

    public void createTable(@NotNull CreateTableQuery query) {
        doRunUpdate(query);
    }

    public void createTable(@NotNull CreateTableQuery.Builder builder) {
        createTable(builder.build(engine()));
    }

    public void alterTable(@NotNull AlterTableQuery query) {
        doRunUpdate(query);
    }

    public void alterTable(@NotNull AlterTableAddForeignKeyQuery.Builder builder) {
        for (AlterTableAddForeignKeyQuery query : builder.build(engine())) {
            doRunUpdate(query);
        }
    }

    public void dropTable(@NotNull DropTableQuery query) {
        doRunUpdate(query);
    }

    public void dropTable(@NotNull DropTableQuery.Builder builder) {
        dropTable(builder.build(engine()));
    }

    public void truncateTable(@NotNull TruncateTableQuery query) {
        doRunUpdate(query);
    }

    public void truncateTable(@NotNull TruncateTableQuery.Builder builder) {
        truncateTable(builder.build(engine()));
    }

    protected void doRunUpdate(@NotNull DataDefinitionQuery query) {
        try {
            log.at(Level.INFO).log("Running: %s ...", LazyArgs.lazy(() -> describeQuery(query.repr())));
            int rows = runner().runUpdate(query);
            log.at(Level.FINER).log("Query OK, %d rows affected", rows);
        } catch (SQLException e) {
            throw new QueryException("Failed to run admin query", query.repr(), query.args(), e);
        }
    }

    private @NotNull Engine engine() {
        return connector.engine();
    }

    private @NotNull QueryRunner runner() {
        return connector.runner();
    }

    private static @NotNull String describeQuery(@NotNull String query) {
        return query.lines().limit(1).findFirst().orElse(query).replaceAll("[()]", "").trim();
    }

    private static @NotNull DataDefinitionQuery hardcoded(@NotNull String query) {
        return new DataDefinitionQuery() {
            @Override
            public @NotNull String repr() {
                return query;
            }
            @Override
            public @NotNull Args args() {
                return Args.of();
            }
        };
    }
}
