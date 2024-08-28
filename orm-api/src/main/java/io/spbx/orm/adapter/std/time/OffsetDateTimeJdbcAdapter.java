package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcMultiValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@JdbcAdapt(value = OffsetDateTime.class, names = {"timestamp", "zone_offset_seconds"})
public class OffsetDateTimeJdbcAdapter implements JdbcMultiValueAdapter<OffsetDateTime>, ResultSetIterator.Converter<OffsetDateTime> {
    public static final OffsetDateTimeJdbcAdapter ADAPTER = new OffsetDateTimeJdbcAdapter();

    @Override
    public int valuesNumber() {
        return 2;
    }

    public @NotNull OffsetDateTime createInstance(@NotNull Timestamp timestamp, int zoneOffsetSeconds) {
        LocalDateTime localDateTime = LocalDateTimeJdbcAdapter.ADAPTER.createInstance(timestamp);
        ZoneOffset zoneOffset = ZoneOffsetJdbcAdapter.ADAPTER.createInstance(zoneOffsetSeconds);
        return OffsetDateTime.of(localDateTime, zoneOffset);
    }

    @Override
    public void fillArrayValues(@NotNull OffsetDateTime instance, @Nullable Object @NotNull[] array, int start) {
        array[start] = LocalDateTimeJdbcAdapter.ADAPTER.toValueObject(instance.toLocalDateTime());
        array[start + 1] = ZoneOffsetJdbcAdapter.ADAPTER.toValueObject(instance.getOffset());
    }

    @Override
    public @NotNull OffsetDateTime apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTimestamp(1), resultSet.getInt(2));
    }
}
