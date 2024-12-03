package io.spbx.orm.adapter.std.math;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.base.annotate.Stateless;
import io.spbx.util.base.math.Int128;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

@Stateless
@JdbcAdapt(Int128.class)
public class Int128JdbcAdapter implements JdbcSingleValueAdapter<Int128>, ResultSetIterator.Converter<Int128> {
    public static final Int128JdbcAdapter ADAPTER = new Int128JdbcAdapter();

    public @NotNull Int128 createInstance(byte @NotNull[] bytes) {
        return Int128.fromBits(bytes);
    }

    @Override
    public byte @NotNull[] toValueObject(Int128 instance) {
        return instance.toByteArray();
    }

    @Override
    public @NotNull Int128 apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getBytes(1));
    }
}
