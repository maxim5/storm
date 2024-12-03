package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Stateless
@JdbcAdapt(LocalDate.class)
public class LocalDateJdbcAdapter implements JdbcSingleValueAdapter<LocalDate>, ResultSetIterator.Converter<LocalDate> {
    public static final LocalDateJdbcAdapter ADAPTER = new LocalDateJdbcAdapter();

    public @NotNull LocalDate createInstance(@NotNull Date value) {
        return value.toLocalDate();
    }

    @Override
    public @NotNull Date toValueObject(@NotNull LocalDate instance) {
        return Date.valueOf(instance);
    }

    @Override
    public @NotNull LocalDate apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getDate(1));
    }
}
