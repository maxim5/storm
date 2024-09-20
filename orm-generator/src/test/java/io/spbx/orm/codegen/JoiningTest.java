package io.spbx.orm.codegen;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.codegen.Indent.INDENT0;
import static io.spbx.orm.codegen.Indent.INDENT1;
import static io.spbx.orm.codegen.Joining.linesJoiner;
import static io.spbx.util.testing.TestingBasics.streamOf;

@Tag("fast")
public class JoiningTest {
    @Test
    public void linesJoiner_simple() {
        assertThat(streamOf("foo", "bar").collect(linesJoiner(INDENT0))).isEqualTo("foo\n" +
                                                                                   "bar");
        assertThat(streamOf("foo", "bar").collect(linesJoiner(INDENT1))).isEqualTo("    foo\n" +
                                                                                   "    bar");
        assertThat(streamOf("", "").collect(linesJoiner(INDENT0))).isEqualTo("\n");
        assertThat(streamOf("foo", "", "bar").collect(linesJoiner(INDENT1))).isEqualTo("    foo\n" +
                                                                                       "    \n" +
                                                                                       "    bar");
    }

    @Test
    public void linesJoiner_filter_empty() {
        assertThat(streamOf("", "").collect(linesJoiner(INDENT0, true))).isEqualTo("");
        assertThat(streamOf("foo", "", "bar").collect(linesJoiner(INDENT1, true))).isEqualTo("    foo\n" +
                                                                                             "    bar");
        assertThat(streamOf("foo", "", "bar").collect(linesJoiner(INDENT1, false))).isEqualTo("    foo\n" +
                                                                                              "    \n" +
                                                                                              "    bar");
    }
}
