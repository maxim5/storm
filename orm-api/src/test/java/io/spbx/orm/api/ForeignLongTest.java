package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ForeignLongTest implements BaseForeignTest<Long, ForeignLong<String>> {
    @Test
    public void isMatch_simple() {
        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofId(1))).isTrue();
        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofEntity(1, "foo"))).isTrue();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofId(1))).isTrue();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofEntity(1, "foo"))).isTrue();

        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofId(2))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofId(2))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofEntity(1, "bar"))).isFalse();
    }

    @Override
    public @NotNull ForeignLong<String> ofEmpty() {
        return ForeignLong.empty();
    }

    @Override
    public @NotNull ForeignLong<String> ofId(int id) {
        return ForeignLong.ofId(id);
    }

    @Override
    public @NotNull ForeignLong<String> ofEntity(int id, @NotNull String entity) {
        return ForeignLong.ofEntity(id, entity);
    }
}
