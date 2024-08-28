package io.spbx.orm.api.query;

import com.carrotsearch.hppc.IntArrayList;
import io.spbx.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.api.query.Shortcuts.*;
import static io.spbx.orm.testing.AssertSql.assertThat;
import static io.spbx.orm.testing.AssertSql.assertThatSql;
import static io.spbx.util.testing.TestingBasics.listOf;
import static io.spbx.util.testing.TestingBasics.mapOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContextualTest {
    @Test
    public void invalid_resolver_map_is_empty() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByName(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> mapOf()
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_map_is_larger() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByName(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> mapOf("U", array.get(0), "V", array.get(0))
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_map_has_wrong_name() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByName(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> mapOf("V", array.get(0))
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args keys don't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_list_is_empty() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByOrderedList(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> listOf()
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_list_is_larger() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByOrderedList(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> listOf(array.get(0), array.get(0))
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void one_unresolved_ByName_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByName(
            Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
            array -> mapOf("U", array.get(0))  // takes first
        );
        assertThatSql(contextual).isEqualTo("WHERE i = ?");
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11))).containsArgsExactly(11);
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12))).containsArgsExactly(11);
    }

    @Test
    public void one_unresolved_ByOrder_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByOrderedList(
            Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
            array -> listOf(array.get(0))  // takes first
        );
        assertThatSql(contextual).isEqualTo("WHERE i = ?");
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12))).containsArgsExactly(11);
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13))).containsArgsExactly(11);
    }

    @Test
    public void one_unresolved_and_other_args_ByOrder_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByOrderedList(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), var(100))),
            array -> listOf(array.get(1))  // takes second
        );
        assertThatSql(contextual).isEqualTo("WHERE i BETWEEN ? AND ?");
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12))).containsArgsExactly(12, 100);
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13))).containsArgsExactly(12, 100);
    }

    @Test
    public void two_unresolved_ByName_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByName(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), unresolved("V", TermType.NUMBER))),
            array -> mapOf("U", array.get(1), "V", array.get(2))  // takes second and third
        );
        assertThatSql(contextual).isEqualTo("WHERE i BETWEEN ? AND ?");
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13))).containsArgsExactly(12, 13);
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13, 14))).containsArgsExactly(12, 13);
    }

    @Test
    public void two_unresolved_ByName_int_array_duplicate_value() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByName(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), unresolved("V", TermType.NUMBER))),
            array -> mapOf("U", array.get(1), "V", array.get(1))  // takes second twice!
        );
        assertThatSql(contextual).isEqualTo("WHERE i BETWEEN ? AND ?");
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13))).containsArgsExactly(12, 12);
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13, 14))).containsArgsExactly(12, 12);
    }

    @Test
    public void two_unresolved_ByOrder_int_array_duplicate_value() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByOrderedList(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), unresolved("V", TermType.NUMBER))),
            array -> listOf(array.get(0), array.get(0))  // takes first twice!
        );
        assertThatSql(contextual).isEqualTo("WHERE i BETWEEN ? AND ?");
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12))).containsArgsExactly(11, 11);
        assertThat(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13))).containsArgsExactly(11, 11);
    }
}
