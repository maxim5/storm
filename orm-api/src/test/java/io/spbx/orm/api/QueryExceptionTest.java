package io.spbx.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBasics.*;

@Tag("fast")
public class QueryExceptionTest {
    @Test
    public void message_format() {
        QueryException e = new QueryException("Original text", "SELECT count(*) FROM table\nWHERE val > 0", arrayOf(1, 2));
        assertThat(e.getMessage()).isEqualTo("""
            Original text. Query:
            ```
            SELECT count(*) FROM table
            WHERE val > 0
            ```
            Args: `[1, 2]`""");
        assertThat(e.getQuery()).isEqualTo("SELECT count(*) FROM table\nWHERE val > 0");
        assertThat(e.getArgs()).containsExactly(1, 2);
    }

    @Test
    public void args_list() {
        assertThat(new QueryException("Msg", "DROP x", listOf(1, 2), null).getArgs()).containsExactly(1, 2);
        assertThat(new QueryException("Msg", "DROP x", setOf(1, 2), null).getArgs()).containsExactly(1, 2);
        assertThat(new QueryException("Msg", "DROP x", IntArrayList.from(1, 2), null).getArgs()).containsExactly(1, 2);
        assertThat(new QueryException("Msg", "DROP x", LongArrayList.from(1, 2), null).getArgs()).containsExactly(1L, 2L);
    }
}
