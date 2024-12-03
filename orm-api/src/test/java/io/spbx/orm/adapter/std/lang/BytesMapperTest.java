package io.spbx.orm.adapter.std.lang;

import io.spbx.util.base.math.Int128;
import io.spbx.util.base.tuple.OneOf;
import io.spbx.util.base.tuple.Pair;
import io.spbx.util.base.tuple.Triple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.util.testing.AssertReverse.assertRoundtrip;

@Tag("fast")
public class BytesMapperTest {
    @Test
    public void of_pair_roundtrip() {
        assertRoundtrip(
            new BytesMapper<Pair<String, Integer>>(),
            Pair.of("foo", 123), Pair.of("foo", null), Pair.of(null, 123), Pair.empty()
        );
    }

    @Test
    public void of_triple_roundtrip() {
        assertRoundtrip(
            new BytesMapper<Triple<Integer, String, Character>>(),
            Triple.of(1, "2", '3'), Triple.empty()
        );
    }

    @Test
    public void of_one_of_roundtrip() {
        assertRoundtrip(
            new BytesMapper<OneOf<String, Integer>>(),
            OneOf.ofFirst("foo"), OneOf.ofSecond(123)
        );
    }

    @Test
    public void of_int128_roundtrip() {
        assertRoundtrip(
            new BytesMapper<>(),
            Int128.ZERO, Int128.MIN_VALUE, Int128.MAX_VALUE
        );
    }
}
