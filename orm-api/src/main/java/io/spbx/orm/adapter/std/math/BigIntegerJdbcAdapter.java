package io.spbx.orm.adapter.std.math;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

@Stateless
@JdbcAdapt(BigInteger.class)
public class BigIntegerJdbcAdapter implements JdbcSingleValueAdapter<BigInteger>, ResultSetIterator.Converter<BigInteger> {
    public static final BigIntegerJdbcAdapter ADAPTER = new BigIntegerJdbcAdapter();

    public @NotNull BigInteger createInstance(byte @NotNull[] bytes) {
        return new BigInteger(bytes);
    }

    @Override
    public byte @NotNull[] toValueObject(BigInteger instance) {
        return instance.toByteArray();
    }

    @Override
    public @NotNull BigInteger apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getBytes(1));
    }
}
