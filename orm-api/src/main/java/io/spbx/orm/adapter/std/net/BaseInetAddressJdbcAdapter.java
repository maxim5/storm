package io.spbx.orm.adapter.std.net;

import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.io.BasicNet;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.spbx.util.base.lang.EasyCast.castAny;

public abstract class BaseInetAddressJdbcAdapter<T extends InetAddress>
        implements JdbcSingleValueAdapter<T>, ResultSetIterator.Converter<T> {
    public @NotNull T createInstance(byte @NotNull[] bytes) {
        return castAny(BasicNet.ipAddressFromBytes(bytes));
    }

    @Override
    public byte @NotNull[] toValueObject(T instance) {
        return instance.getAddress();
    }

    @Override
    public @NotNull T apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getBytes(1));
    }
}
