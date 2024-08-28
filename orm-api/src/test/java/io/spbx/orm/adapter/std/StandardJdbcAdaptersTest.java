package io.spbx.orm.adapter.std;

import io.spbx.orm.adapter.std.chars.CharArrayJdbcAdapter;
import io.spbx.orm.adapter.std.chars.CharSequenceJdbcAdapter;
import io.spbx.orm.adapter.std.chars.CharacterJdbcAdapter;
import io.spbx.orm.adapter.std.lang.AtomicBooleanJdbcAdapter;
import io.spbx.orm.adapter.std.lang.AtomicIntegerJdbcAdapter;
import io.spbx.orm.adapter.std.lang.AtomicLongJdbcAdapter;
import io.spbx.orm.adapter.std.math.BigIntegerJdbcAdapter;
import io.spbx.orm.adapter.std.math.Int128JdbcAdapter;
import io.spbx.orm.adapter.std.net.Inet4AddressJdbcAdapter;
import io.spbx.orm.adapter.std.net.Inet6AddressJdbcAdapter;
import io.spbx.orm.adapter.std.net.InetAddressJdbcAdapter;
import io.spbx.orm.adapter.std.time.DurationJdbcAdapter;
import io.spbx.orm.adapter.std.time.InstantJdbcAdapter;
import io.spbx.orm.adapter.std.time.LocalDateJdbcAdapter;
import io.spbx.orm.adapter.std.time.LocalDateTimeJdbcAdapter;
import io.spbx.orm.adapter.std.time.LocalTimeJdbcAdapter;
import io.spbx.orm.adapter.std.time.OffsetDateTimeJdbcAdapter;
import io.spbx.orm.adapter.std.time.OffsetTimeJdbcAdapter;
import io.spbx.orm.adapter.std.time.PeriodJdbcAdapter;
import io.spbx.orm.adapter.std.time.ZoneOffsetJdbcAdapter;
import io.spbx.orm.adapter.std.time.ZonedDateTimeJdbcAdapter;
import io.spbx.util.base.Int128;
import io.spbx.util.func.Reversible;
import io.spbx.util.io.BasicNet;
import io.spbx.util.testing.ext.TimeZoneExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.TimeZone;

import static io.spbx.util.func.Reversibles.fromNotNullFunctions;
import static io.spbx.util.testing.AssertBasics.assertReversibleRoundtrip;

public class StandardJdbcAdaptersTest {
    @RegisterExtension private static final TimeZoneExtension GMT = TimeZoneExtension.force(TimeZone.getTimeZone("GMT"));

    /** {@link CharacterJdbcAdapter} */

    @ParameterizedTest @ValueSource(strings = { "0", "a", "A", "\n" })
    public void characters(String ch) {
        assertReversibleRoundtrip(
            fromNotNullFunctions(CharacterJdbcAdapter::createInstance, CharacterJdbcAdapter::toValueObject), ch);
    }

    /** {@link CharArrayJdbcAdapter}, {@link CharSequenceJdbcAdapter} */

    private static final CharArrayJdbcAdapter CHAR_ARRAYS = CharArrayJdbcAdapter.ADAPTER;
    private static final CharSequenceJdbcAdapter SEQUENCES = CharSequenceJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(strings = { "", "\n", "foo", "\0\1\2\3\4\5\6\7!" })
    public void char_arrays(String str) {
        assertReversibleRoundtrip(fromNotNullFunctions(CHAR_ARRAYS::createInstance, CHAR_ARRAYS::toValueObject), str);
        assertReversibleRoundtrip(fromNotNullFunctions(SEQUENCES::createInstance, SEQUENCES::toValueObject), str);
    }

    /** {@link AtomicBooleanJdbcAdapter}, {@link AtomicIntegerJdbcAdapter}, {@link AtomicLongJdbcAdapter} */

    private static final AtomicBooleanJdbcAdapter ATOMIC_BOOLS = AtomicBooleanJdbcAdapter.ADAPTER;
    private static final AtomicIntegerJdbcAdapter ATOMIC_INTS = AtomicIntegerJdbcAdapter.ADAPTER;
    private static final AtomicLongJdbcAdapter ATOMIC_LONGS = AtomicLongJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(booleans = { true, false })
    public void booleans(boolean bool) {
        assertReversibleRoundtrip(fromNotNullFunctions(ATOMIC_BOOLS::createInstance, ATOMIC_BOOLS::toValueObject), bool);
    }

    @ParameterizedTest @ValueSource(ints = { 0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE })
    public void ints(int i) {
        assertReversibleRoundtrip(fromNotNullFunctions(ATOMIC_INTS::createInstance, ATOMIC_INTS::toValueObject), i);
    }

    @ParameterizedTest @ValueSource(longs = { 0, 1, -1, Long.MIN_VALUE, Long.MAX_VALUE })
    public void longs(long l) {
        assertReversibleRoundtrip(fromNotNullFunctions(ATOMIC_LONGS::createInstance, ATOMIC_LONGS::toValueObject), l);
    }

    /** {@link CharArrayJdbcAdapter}, {@link CharSequenceJdbcAdapter} */

    private static final Int128JdbcAdapter INT128 = Int128JdbcAdapter.ADAPTER;
    private static final BigIntegerJdbcAdapter BIG_INTEGERS = BigIntegerJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(strings = { "0", "12345678901234567890", "-12345678901234567890" })
    public void math_integers(String str) {
        assertReversibleRoundtrip(fromNotNullFunctions(INT128::toValueObject, INT128::createInstance), Int128.from(str));
        assertReversibleRoundtrip(fromNotNullFunctions(BIG_INTEGERS::toValueObject, BIG_INTEGERS::createInstance),
                                  new BigInteger(str));
    }

    /** {@link InstantJdbcAdapter}, {@link DurationJdbcAdapter} */

    private static final InstantJdbcAdapter INSTANTS = InstantJdbcAdapter.ADAPTER;
    private static final DurationJdbcAdapter DURATIONS = DurationJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(longs = { 0, 1, 1_000_000_000, 1_720_105_807_094L })
    public void times(long epochMillis) {
        assertReversibleRoundtrip(fromNotNullFunctions(INSTANTS::createInstance, INSTANTS::toValueObject),
                                  new Timestamp(epochMillis));
        assertReversibleRoundtrip(fromNotNullFunctions(DURATIONS::createInstance, DURATIONS::toValueObject), epochMillis);
    }

    /** {@link LocalDateJdbcAdapter}, {@link LocalDateTimeJdbcAdapter}, {@link LocalTimeJdbcAdapter} */

    private static final LocalDateJdbcAdapter LOCAL_DATES = LocalDateJdbcAdapter.ADAPTER;
    private static final LocalDateTimeJdbcAdapter LOCAL_DATE_TIMES = LocalDateTimeJdbcAdapter.ADAPTER;
    private static final LocalTimeJdbcAdapter LOCAL_TIMES = LocalTimeJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(longs = { 0, 950_400_000, 1_720_051_200_000L })
    public void local_dates(long dateEpochMillis) {
        assertReversibleRoundtrip(fromNotNullFunctions(LOCAL_DATES::createInstance, LOCAL_DATES::toValueObject),
                                  new Date(dateEpochMillis));
        assertReversibleRoundtrip(fromNotNullFunctions(LOCAL_DATE_TIMES::createInstance, LOCAL_DATE_TIMES::toValueObject),
                                  new Timestamp(dateEpochMillis));
    }

    @ParameterizedTest @ValueSource(longs = { 0, 1_000_000_000L, 10_000_000_000_000L, 86_399_999_999_999L })
    public void local_time(long nanos) {
        assertReversibleRoundtrip(fromNotNullFunctions(LOCAL_TIMES::createInstance, LOCAL_TIMES::toValueObject),
                                  Time.valueOf(LocalTime.ofNanoOfDay(nanos)));
    }

    /** {@link OffsetDateTimeJdbcAdapter} */

    private static final OffsetDateTimeJdbcAdapter OFFSET_DATE_TIMES = OffsetDateTimeJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(longs = { 0, 950_400_000, 1_720_051_200_000L })
    public void offset_date_time(long dateEpochMillis) {
        record Pack(Timestamp timestamp, int zoneOffsetSeconds) {}

        Reversible<Pack, OffsetDateTime> reversible = fromNotNullFunctions(
            pack -> OFFSET_DATE_TIMES.createInstance(pack.timestamp, pack.zoneOffsetSeconds),
            offsetDateTime -> {
                Object[] objects = OFFSET_DATE_TIMES.toNewValuesArray(offsetDateTime);
                return new Pack((Timestamp) objects[0], (Integer) objects[1]);
            }
        );
        assertReversibleRoundtrip(reversible, new Pack(new Timestamp(dateEpochMillis), 0));
    }

    /** {@link OffsetTimeJdbcAdapter} */

    private static final OffsetTimeJdbcAdapter OFFSET_TIMES = OffsetTimeJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(longs = { 0, 1_000_000_000L, 10_000_000_000_000L, 86_399_999_999_999L })
    public void offset_time(long nanos) {
        record Pack(Time time, int zoneOffsetSeconds) {}

        Reversible<Pack, OffsetTime> reversible = fromNotNullFunctions(
            pack -> OFFSET_TIMES.createInstance(pack.time, pack.zoneOffsetSeconds),
            offsetTime -> {
                Object[] objects = OFFSET_TIMES.toNewValuesArray(offsetTime);
                return new Pack((Time) objects[0], (Integer) objects[1]);
            }
        );
        assertReversibleRoundtrip(reversible, new Pack(Time.valueOf(LocalTime.ofNanoOfDay(nanos)), 0));
    }

    /** {@link PeriodJdbcAdapter} */

    private static final PeriodJdbcAdapter PERIODS = PeriodJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(strings = { "P2Y", "P3M", "P28D", "P1Y2M3D", "P1Y2M25D", "P-1Y2M", "P-1Y-2M" })
    public void periods(String period) {
        assertReversibleRoundtrip(fromNotNullFunctions(PERIODS::createInstance, PERIODS::toValueObject), period);
    }

    /** {@link ZonedDateTimeJdbcAdapter}, {@link ZoneOffsetJdbcAdapter} */

    private static final ZonedDateTimeJdbcAdapter ZONED_DATE_TIMES = ZonedDateTimeJdbcAdapter.ADAPTER;
    private static final ZoneOffsetJdbcAdapter ZONE_OFFSETS = ZoneOffsetJdbcAdapter.ADAPTER;

    @ParameterizedTest @ValueSource(longs = { 0, 950_400_000, 1_720_051_200_000L })
    public void zoned_date_time(long dateEpochMillis) {
        assertReversibleRoundtrip(fromNotNullFunctions(ZONED_DATE_TIMES::createInstance, ZONED_DATE_TIMES::toValueObject),
                                  new Timestamp(dateEpochMillis));
    }

    @ParameterizedTest @ValueSource(ints = { 0, 3600, 7200, -3600, -7200 })
    public void zone_offset(int seconds) {
        assertReversibleRoundtrip(fromNotNullFunctions(ZONE_OFFSETS::createInstance, ZONE_OFFSETS::toValueObject), seconds);
    }

    /** {@link InetAddressJdbcAdapter}, {@link Inet4AddressJdbcAdapter}, {@link Inet6AddressJdbcAdapter}, */

    private static final InetAddressJdbcAdapter INET = InetAddressJdbcAdapter.ADAPTER;
    private static final Inet4AddressJdbcAdapter INET4 = Inet4AddressJdbcAdapter.ADAPTER;
    private static final Inet6AddressJdbcAdapter INET6 = Inet6AddressJdbcAdapter.ADAPTER;

    @ParameterizedTest
    @ValueSource(strings = { "0.0.0.0", "0.0.0.1", "255.255.255.255", "255.255.254.0", "192.168.0.1" })
    public void inet4_address(String addr) {
        byte[] address = BasicNet.parseIp4Address(addr).getAddress();
        assertReversibleRoundtrip(fromNotNullFunctions(INET4::createInstance, INET4::toValueObject), address);
        assertReversibleRoundtrip(fromNotNullFunctions(INET::createInstance, INET::toValueObject), address);
    }

    @ParameterizedTest
    @ValueSource(strings = { "::", "::1", "2001:db8:0:1:1:1:1:1", "2001:0DB8:AC10:FE01:0000:0000:0000:0000" })
    public void inet6_address(String addr) {
        byte[] address = BasicNet.parseIp6Address(addr).getAddress();
        assertReversibleRoundtrip(fromNotNullFunctions(INET6::createInstance, INET6::toValueObject), address);
        assertReversibleRoundtrip(fromNotNullFunctions(INET::createInstance, INET::toValueObject), address);
    }
}
