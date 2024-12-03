package io.spbx.orm.codegen;

import io.spbx.orm.testing.AssertCode;
import io.spbx.util.base.annotate.CanIgnoreReturnValue;
import io.spbx.util.base.annotate.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.codegen.JavaSupport.wrapAsStringLiteral;

@Tag("fast")
public class JavaSupportTest {
    @Test
    public void wrapAsStringLiteral_one_line() {
        assertThatText("").wrappedAsLiteralEqualsTo("\"\"");
        assertThatText(" ").wrappedAsLiteralEqualsTo("\" \"");
        assertThatText("foo").wrappedAsLiteralEqualsTo("\"foo\"");
        assertThatText("foo bar").wrappedAsLiteralEqualsTo("\"foo bar\"");
        assertThatText("'foo").wrappedAsLiteralEqualsTo("\"\\'foo\"");
        assertThatText("\"foo").wrappedAsLiteralEqualsTo("\"\\\"foo\"");
        assertThatText("\t").wrappedAsLiteralEqualsTo("\"\\t\"");
    }

    @Test
    public void wrapAsStringLiteral_text_block() {
        assertThatText("""
                       
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   
                                   \"\"\"\
                                   """);

        assertThatText("""
                       foo
                       bar
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   foo
                                   bar
                                   \"\"\"\
                                   """);

        assertThatText("""
                       foo
                       bar\
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   foo
                                   bar
                                   \"\"\"\
                                   """);

        assertThatText("""
                       "foo"
                       'bar'\
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   \\"foo\\"
                                   \\'bar\\'
                                   \"\"\"\
                                   """);
    }

    @CheckReturnValue
    private static @NotNull StringLiteralSubject assertThatText(@NotNull String text) {
        return new StringLiteralSubject(text);
    }

    @CanIgnoreReturnValue
    private record StringLiteralSubject(@NotNull String text) {
        public @NotNull StringLiteralSubject wrappedAsLiteralEqualsTo(@NotNull String expected) {
            AssertCode.assertThatJava(wrapAsStringLiteral(text)).isEqualTo(expected);
            AssertSnippet.assertThatJava(wrapAsStringLiteral(new Snippet().appendMultiline(text))).isEqualTo(expected);
            return this;
        }
    }
}
