package io.spbx.orm.api;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface BaseForeignTest<I, F extends Foreign<I, String>> {
    @NotNull F ofEmpty();
    @NotNull F ofId(int id);
    @NotNull F ofEntity(int id, @NotNull String entity);

    @Test
    default void setEntityIfMissing_has_key() {
        F foreign = ofId(1);
        assertThat(foreign.setEntityIfMissing("foo")).isTrue();
        assertThat(foreign).isEqualTo(ofEntity(1, "foo"));
    }

    @Test
    default void setEntityIfMissing_has_entity() {
        F foreign = ofEntity(1, "foo");
        assertThat(foreign.setEntityIfMissing("bar")).isFalse();
        assertThat(foreign).isEqualTo(ofEntity(1, "foo"));
    }

    @Test
    default void setEntityIfMissing_empty_throws() {
        F foreign = ofEmpty();
        assertThrows(AssertionError.class, () -> foreign.setEntityIfMissing("foo"));
    }

    @Test
    default void setEntityUnconditionally_has_key() {
        F foreign = ofId(1);
        foreign.setEntityUnconditionally("foo");
        assertThat(foreign).isEqualTo(ofEntity(1, "foo"));
    }

    @Test
    default void setEntityUnconditionally_has_entity() {
        F foreign = ofEntity(1, "foo");
        foreign.setEntityUnconditionally("bar");
        assertThat(foreign).isEqualTo(ofEntity(1, "bar"));
    }

    @Test
    default void setEntityUnconditionally_empty_throws() {
        F foreign = ofEmpty();
        assertThrows(AssertionError.class, () -> foreign.setEntityUnconditionally("foo"));
    }
}
