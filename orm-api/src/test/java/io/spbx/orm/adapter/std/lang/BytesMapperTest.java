package io.spbx.orm.adapter.std.lang;

import io.spbx.util.base.Int128;
import io.spbx.util.base.OneOf;
import io.spbx.util.base.Pair;
import io.spbx.util.base.Triple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.util.testing.AssertBasics.assertReversibleRoundtrip;

@Tag("fast")
public class BytesMapperTest {
    @Test
    public void of_pair_roundtrip() {
        assertReversibleRoundtrip(
            new BytesMapper<Pair<String, Integer>>(),
            Pair.of("foo", 123), Pair.of("foo", null), Pair.of(null, 123), Pair.empty()
        );
    }

    @Test
    public void of_triple_roundtrip() {
        assertReversibleRoundtrip(
            new BytesMapper<Triple<Integer, String, Character>>(),
            Triple.of(1, "2", '3'), Triple.empty()
        );
    }

    @Test
    public void of_one_of_roundtrip() {
        assertReversibleRoundtrip(
            new BytesMapper<OneOf<String, Integer>>(),
            OneOf.ofFirst("foo"), OneOf.ofSecond(123)
        );
    }

    @Test
    public void of_int128_roundtrip() {
        assertReversibleRoundtrip(
            new BytesMapper<>(),
            Int128.ZERO, Int128.MIN_VALUE, Int128.MAX_VALUE
        );
    }
}
