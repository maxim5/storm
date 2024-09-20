package io.spbx.orm.adapter.std.time;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * A default adapter for {@link Instant} values.
 * <p>
 * Maps to {@link Timestamp} in the DB.
 * Conversion truncates the {@link Instant} to {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
 * <p>
 * Other possible ways:
 * <ul>
 *     <li>{@code @Sql.Via(Instant96Codec.class) Instant field}</li>
 *     <li>{@code @Sql.Via(Instant64MicroCodec.class) Instant field}</li>
 *     <li>{@code @Sql.Via(Instant64NanoCodec.class) Instant field}</li>
 * </ul>
 *
 * @see io.spbx.webby.db.codec.std.time.Instant96Codec
 * @see io.spbx.webby.db.codec.std.time.Instant64NanoCodec
 * @see io.spbx.webby.db.codec.std.time.Instant64MicroCodec
 */
@JdbcAdapt(Instant.class)
public class InstantJdbcAdapter implements JdbcSingleValueAdapter<Instant>, ResultSetIterator.Converter<Instant> {
    public static final InstantJdbcAdapter ADAPTER = new InstantJdbcAdapter();

    public @NotNull Instant createInstance(@NotNull Timestamp value) {
        return value.toInstant();
    }

    @Override
    public @NotNull Timestamp toValueObject(@NotNull Instant instance) {
        return Timestamp.from(instance);
    }

    @Override
    public @NotNull Instant apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTimestamp(1));
    }
}
