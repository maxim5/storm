package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Period;

@JdbcAdapt(Period.class)
public class PeriodJdbcAdapter implements JdbcSingleValueAdapter<Period>, ResultSetIterator.Converter<Period> {
    public static final PeriodJdbcAdapter ADAPTER = new PeriodJdbcAdapter();

    public @NotNull Period createInstance(@NotNull String value) {
        return Period.parse(value);
    }

    @Override
    public @NotNull String toValueObject(@NotNull Period instance) {
        return instance.toString();
    }

    @Override
    public @NotNull Period apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getString(1));
    }
}
