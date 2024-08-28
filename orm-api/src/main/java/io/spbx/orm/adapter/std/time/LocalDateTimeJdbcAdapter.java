package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@JdbcAdapt(LocalDateTime.class)
public class LocalDateTimeJdbcAdapter implements JdbcSingleValueAdapter<LocalDateTime>, ResultSetIterator.Converter<LocalDateTime> {
    public static final LocalDateTimeJdbcAdapter ADAPTER = new LocalDateTimeJdbcAdapter();

    public @NotNull LocalDateTime createInstance(@NotNull Timestamp value) {
        return value.toLocalDateTime();
    }

    @Override
    public @NotNull Timestamp toValueObject(@NotNull LocalDateTime instance) {
        return Timestamp.valueOf(instance);
    }

    @Override
    public @NotNull LocalDateTime apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTimestamp(1));
    }
}
