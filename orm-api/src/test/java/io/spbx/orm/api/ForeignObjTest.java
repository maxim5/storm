package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@Tag("fast")
public class ForeignObjTest implements BaseForeignTest<Integer, ForeignObj<Integer, String>> {
    @Test
    public void isMatch_simple() {
        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofId(1))).isTrue();
        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofEntity(1, "foo"))).isTrue();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofId(1))).isTrue();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofEntity(1, "foo"))).isTrue();

        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofId(2))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofId(2))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofEntity(1, "bar"))).isFalse();
    }

    @Override
    public @NotNull ForeignObj<Integer, String> ofEmpty() {
        return ForeignObj.empty();
    }

    @Override
    public @NotNull ForeignObj<Integer, String> ofId(int id) {
        return ForeignObj.ofId(id);
    }

    @Override
    public @NotNull ForeignObj<Integer, String> ofEntity(int id, @NotNull String entity) {
        return ForeignObj.ofEntity(id, entity);
    }
}
