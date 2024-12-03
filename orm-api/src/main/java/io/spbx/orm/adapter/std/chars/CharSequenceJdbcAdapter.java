package io.spbx.orm.adapter.std.chars;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.util.base.annotate.Stateless;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

@Stateless
@JdbcAdapt(CharSequence.class)
public class CharSequenceJdbcAdapter implements JdbcSingleValueAdapter<CharSequence>, ResultSetIterator.Converter<CharSequence> {
    public static final CharSequenceJdbcAdapter ADAPTER = new CharSequenceJdbcAdapter();

    public @NotNull CharSequence createInstance(@NotNull String value) {
        return value;
    }

    @Override
    public @NotNull String toValueObject(@NotNull CharSequence instance) {
        return instance.toString();
    }

    @Override
    public @NotNull CharSequence apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getString(1));
    }
}
