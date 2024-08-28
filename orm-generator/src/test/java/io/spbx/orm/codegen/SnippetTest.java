package io.spbx.orm.codegen;

import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.spbx.util.testing.TestingBasics.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class SnippetTest {
    @Test
    public void build_via_appendLine() {
        Snippet snippet = new Snippet();
        assertThat(snippet).containsLinesExactly().isNotBlock();
        assertThat(snippet.appendLine("foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.appendLine("bar")).containsLinesExactly("foo", "bar").isBlock();
    }

    @Test
    public void build_via_appendLine_parts() {
        assertThat(new Snippet().appendLine("a", "b")).containsLinesExactly("ab").isNotBlock();
        assertThat(new Snippet().appendLine("a", "b", "c")).containsLinesExactly("abc").isNotBlock();
        assertThat(new Snippet().appendLine("a", "b", "c", "d")).containsLinesExactly("abcd").isNotBlock();
    }

    @Test
    public void build_via_appendFormattedLine() {
        assertThat(new Snippet().appendFormattedLine("%s", "foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(new Snippet().appendFormattedLine("%s%s", "foo", "bar")).containsLinesExactly("foobar").isNotBlock();
        assertThat(new Snippet().appendFormattedLine("%d-%d-%d", 1, 2, 3)).containsLinesExactly("1-2-3").isNotBlock();
    }

    @Test
    public void build_via_appendLines() {
        Snippet snippet = new Snippet();
        assertThat(snippet).containsLinesExactly().isNotBlock();
        assertThat(snippet.appendLines(listOf("foo"))).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.appendLines(streamOf("bar"))).containsLinesExactly("foo", "bar").isBlock();
        assertThat(snippet.appendLines(arrayOf("baz"))).containsLinesExactly("foo", "bar", "baz").isBlock();
    }

    @Test
    public void build_via_appendMultiline() {
        Snippet snippet = new Snippet();
        assertThat(snippet).containsLinesExactly().isNotBlock();
        assertThat(snippet.appendMultiline("foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.appendMultiline("\n")).containsLinesExactly("foo", "").isBlock();
        assertThat(snippet.appendMultiline("bar\n")).containsLinesExactly("foo", "", "bar").isBlock();
    }

    @Test
    public void build_via_appendMultiline_block() {
        assertThat(new Snippet().appendMultiline("foo\n")).containsLinesExactly("foo").isBlock();
        assertThat(new Snippet().appendMultiline("foo\nbar")).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().appendMultiline("foo\rbar")).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().appendMultiline("foo\r\nbar")).containsLinesExactly("foo", "bar").isBlock();
    }

    @Test
    public void build_via_appendFormattedMultiline() {
        assertThat(new Snippet().appendFormattedMultiline("%s", "foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(new Snippet().appendFormattedMultiline("%s\n", "foo")).containsLinesExactly("foo").isBlock();
        assertThat(new Snippet().appendFormattedMultiline("%d\r\n%d", 1, 2)).containsLinesExactly("1", "2").isBlock();
        assertThat(new Snippet().appendFormattedMultiline("\n%d-%d", 1, 2)).containsLinesExactly("", "1-2").isBlock();
    }

    @Test
    public void build_via_appendMultilines_block() {
        assertThat(new Snippet().appendMultilines(listOf("foo\nbar"))).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().appendMultilines(streamOf("foo\nbar"))).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().appendMultilines(arrayOf("foo\nbar"))).containsLinesExactly("foo", "bar").isBlock();
    }

    @Test
    public void build_forceMultiline() {
        Snippet snippet = new Snippet().appendLine("foo");
        assertThat(snippet).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.forceMultiline(true)).containsLinesExactly("foo").isBlock();
        assertThat(snippet.forceMultiline(false)).containsLinesExactly("foo").isBlock();  // remains a block!
    }

    @Test
    public void isolation() {
        Snippet snippet1 = new Snippet();
        assertThat(snippet1).containsLinesExactly().isNotBlock();
        assertThat(snippet1.appendLine("foo")).containsLinesExactly("foo").isNotBlock();

        Snippet snippet2 = new Snippet().appendLines(snippet1);
        assertThat(snippet2).containsLinesExactly("foo").isNotBlock();

        assertThat(snippet1.appendLine("bar")).containsLinesExactly("foo", "bar").isBlock();
        assertThat(snippet2).containsLinesExactly("foo").isNotBlock();
    }

    @Test
    public void build_incorrectly() {
        assertThrows(AssertionError.class, () -> new Snippet().appendLine("\n"));
        assertThrows(AssertionError.class, () -> new Snippet().appendLine("\r"));
        assertThrows(AssertionError.class, () -> new Snippet().appendLine("foo\n\rbar"));
        assertThrows(AssertionError.class, () -> new Snippet().appendFormattedLine("foo\n\rbar"));
        assertThrows(AssertionError.class, () -> new Snippet().appendLines(listOf("\n")));
        assertThrows(AssertionError.class, () -> new Snippet().appendLines(streamOf("\n")));
        assertThrows(AssertionError.class, () -> new Snippet().appendLines(arrayOf("\n")));
    }

    @CheckReturnValue
    private static @NotNull SnippetSubject assertThat(@NotNull Snippet snippet) {
        return new SnippetSubject(snippet);
    }

    @CanIgnoreReturnValue
    private record SnippetSubject(@NotNull Snippet snippet) {
        public @NotNull SnippetSubject containsLinesExactly(@NotNull String @NotNull ... lines) {
            Truth.assertThat(snippet).containsExactlyElementsIn(lines);
            return this;
        }

        public @NotNull SnippetSubject isBlock() {
            return isBlock(true);
        }

        public @NotNull SnippetSubject isNotBlock() {
            return isBlock(false);
        }

        public @NotNull SnippetSubject isBlock(boolean expected) {
            Truth.assertThat(snippet.isMultiline()).isEqualTo(expected);
            return this;
        }
    }
}
