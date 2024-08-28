package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ForeignIntTest implements BaseForeignTest<Integer, ForeignInt<String>> {
    @Test
    public void isMatch_simple() {
        assertThat(ForeignInt.isMatch(ForeignInt.ofId(1), ForeignInt.ofId(1))).isTrue();
        assertThat(ForeignInt.isMatch(ForeignInt.ofId(1), ForeignInt.ofEntity(1, "foo"))).isTrue();
        assertThat(ForeignInt.isMatch(ForeignInt.ofEntity(1, "foo"), ForeignInt.ofId(1))).isTrue();
        assertThat(ForeignInt.isMatch(ForeignInt.ofEntity(1, "foo"), ForeignInt.ofEntity(1, "foo"))).isTrue();

        assertThat(ForeignInt.isMatch(ForeignInt.ofId(1), ForeignInt.ofId(2))).isFalse();
        assertThat(ForeignInt.isMatch(ForeignInt.ofId(1), ForeignInt.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignInt.isMatch(ForeignInt.ofEntity(1, "foo"), ForeignInt.ofId(2))).isFalse();
        assertThat(ForeignInt.isMatch(ForeignInt.ofEntity(1, "foo"), ForeignInt.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignInt.isMatch(ForeignInt.ofEntity(1, "foo"), ForeignInt.ofEntity(1, "bar"))).isFalse();
    }

    @Override
    public @NotNull ForeignInt<String> ofEmpty() {
        return ForeignInt.empty();
    }

    @Override
    public @NotNull ForeignInt<String> ofId(int id) {
        return ForeignInt.ofId(id);
    }

    @Override
    public @NotNull ForeignInt<String> ofEntity(int id, @NotNull String entity) {
        return ForeignInt.ofEntity(id, entity);
    }
}
