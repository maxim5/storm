package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;

@Stateless
@JdbcAdapt(ZoneOffset.class)
public class ZoneOffsetJdbcAdapter implements JdbcSingleValueAdapter<ZoneOffset>, ResultSetIterator.Converter<ZoneOffset> {
    public static final ZoneOffsetJdbcAdapter ADAPTER = new ZoneOffsetJdbcAdapter();

    public @NotNull ZoneOffset createInstance(int totalSeconds) {
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    @Override
    public @NotNull Integer toValueObject(@NotNull ZoneOffset instance) {
        return instance.getTotalSeconds();
    }

    @Override
    public @NotNull ZoneOffset apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getInt(1));
    }
}
