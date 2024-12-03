package io.spbx.orm.api.tx;

import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.query.HardcodedSelectQuery;
import io.spbx.util.base.error.Unchecked;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.util.func.ThrowFunction;
import io.spbx.util.testing.MoreTruth;
import io.spbx.util.testing.func.CalledOnce;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.testing.MockingJdbc.assertThat;
import static io.spbx.orm.testing.MockingJdbc.mockConnection;
import static io.spbx.orm.testing.MockingJdbc.mockResultSet;
import static org.junit.jupiter.api.Assertions.assertThrows;

// FIX[minor]: more tests: force .commit() or .rollback() to fail
@Tag("fast")
public class InTransactionTest {
    private MockConnection mockedConnection;
    private PreparedStatementResultSetHandler resultSetHandler;
    private InTransaction<QueryRunner> tx;

    @BeforeEach
    void setUp() {
        mockedConnection = mockConnection();
        resultSetHandler = mockedConnection.getPreparedStatementResultSetHandler();
        tx = new QueryRunner(mockedConnection).tx();
    }

    @AfterEach
    void tearDown() {
        resultSetHandler.getPreparedStatements().forEach(statement -> {
            MoreTruth.assertThat(statement.isClosed())
                .withMessage("Statement was not closed: \"%s\"", statement.getSQL())
                .isTrue();
        });
    }

    /** {@link InTransaction#run(ThrowConsumer)} **/

    @Test
    public void run_success_with_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(true);

        try (CalledOnce<QueryRunner, SQLException> calledOnce = new CalledOnce<>()) {
            tx.run(calledOnce.alsoCall(runner ->
                assertThat(mockedConnection.getAutoCommit()).isFalse()
            ));
        }

        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(1).wasRolledBack(0);
    }

    @Test
    public void run_success_without_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(false);

        try (CalledOnce<QueryRunner, SQLException> calledOnce = new CalledOnce<>()) {
            tx.run(calledOnce.alsoCall(runner ->
                assertThat(mockedConnection.getAutoCommit()).isFalse()
            ));
        }

        assertThat(mockedConnection).hasAutocommit(false).wasCommitted(1).wasRolledBack(0);
    }

    @Test
    public void run_throws_sql_exception() throws SQLException {
        mockedConnection.setAutoCommit(true);

        Exception exception = assertThrows(Exception.class, () ->
            tx.run(runner -> {
                Unchecked.throwAny(new SQLException("Fail"));
            })
        );

        assertThat(exception).hasCauseThat().hasMessageThat().isEqualTo("Fail");
        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(0).wasRolledBack(1);
    }

    @Test
    public void run_throws_other_exception() throws SQLException {
        mockedConnection.setAutoCommit(true);

        AssertionError exception = assertThrows(AssertionError.class, () ->
            tx.run(runner -> {
                Unchecked.throwAny(new AssertionError("Oops"));
            })
        );

        assertThat(exception).hasMessageThat().isEqualTo("Oops");
        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(0).wasRolledBack(1);
    }

    /** {@link InTransaction#run(ThrowFunction)} **/

    @Test
    public void run_returns_result() {
        resultSetHandler.prepareResultSet("select string", mockResultSet("foobar"));

        String result = tx.run(runner -> {
            return runner.runAndGetString(HardcodedSelectQuery.of("select string"));
        });
        assertThat(result).isEqualTo("foobar");

        assertThat(mockedConnection).wasCommitted(1).wasRolledBack(0);
    }
}
