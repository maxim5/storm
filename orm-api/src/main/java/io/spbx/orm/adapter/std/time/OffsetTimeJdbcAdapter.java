package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcMultiValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

@Stateless
@JdbcAdapt(value = OffsetTime.class, names = {"time", "zone_offset_seconds"})
public class OffsetTimeJdbcAdapter implements JdbcMultiValueAdapter<OffsetTime>, ResultSetIterator.Converter<OffsetTime> {
    public static final OffsetTimeJdbcAdapter ADAPTER = new OffsetTimeJdbcAdapter();

    @Override
    public int valuesNumber() {
        return 2;
    }

    public @NotNull OffsetTime createInstance(@NotNull Time time, int zoneOffsetSeconds) {
        LocalTime localTime = LocalTimeJdbcAdapter.ADAPTER.createInstance(time);
        ZoneOffset zoneOffset = ZoneOffsetJdbcAdapter.ADAPTER.createInstance(zoneOffsetSeconds);
        return OffsetTime.of(localTime, zoneOffset);
    }

    @Override
    public void fillArrayValues(@NotNull OffsetTime instance, @Nullable Object @NotNull[] array, int start) {
        array[start] = LocalTimeJdbcAdapter.ADAPTER.toValueObject(instance.toLocalTime());
        array[start + 1] = ZoneOffsetJdbcAdapter.ADAPTER.toValueObject(instance.getOffset());
    }

    @Override
    public @NotNull OffsetTime apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTime(1), resultSet.getInt(2));
    }
}
