package io.spbx.orm.arch.model;

import io.spbx.orm.adapter.std.time.DurationJdbcAdapter;
import io.spbx.orm.adapter.std.time.InstantJdbcAdapter;
import io.spbx.orm.adapter.std.time.OffsetDateTimeJdbcAdapter;
import io.spbx.orm.adapter.std.time.OffsetTimeJdbcAdapter;
import io.spbx.orm.adapter.std.time.PeriodJdbcAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class AdapterApiTest {
    @Test
    public void classToAdapterColumns_duration_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(DurationJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(Column.of("foo", JdbcType.Long)).inOrder();
    }

    @Test
    public void classToAdapterColumns_instant_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(InstantJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(Column.of("foo", JdbcType.Timestamp)).inOrder();
    }

    @Test
    public void classToAdapterColumns_offset_time_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(OffsetTimeJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(Column.of("foo_time", JdbcType.Time),
                                            Column.of("foo_zone_offset_seconds", JdbcType.Int)).inOrder();
    }

    @Test
    public void classToAdapterColumns_offset_date_time_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(OffsetDateTimeJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(Column.of("foo_timestamp", JdbcType.Timestamp),
                                            Column.of("foo_zone_offset_seconds", JdbcType.Int)).inOrder();
    }

    @Test
    public void classToAdapterColumns_period_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(PeriodJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(Column.of("foo", JdbcType.String)).inOrder();
    }
}
