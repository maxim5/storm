package io.spbx.orm.testing;

import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.util.testing.TestingBytes;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static io.spbx.util.base.EasyCast.castAny;

public class AssertCode {
    @CheckReturnValue
    public static @NotNull JavaSubject assertThatJava(@NotNull String code) {
        return new JavaSubject(code);
    }

    public static class JavaSubject extends CodeSubject<JavaSubject> {
        public JavaSubject(@NotNull String code) {
            super(code);
        }
    }

    @CanIgnoreReturnValue
    public static class CodeSubject<T extends CodeSubject<?>> {
        private final @NotNull String code;

        public CodeSubject(@NotNull String query) {
            this.code = query;
        }

        protected @NotNull String code() {
            return code;
        }

        @CheckReturnValue
        public @NotNull StringSubject asText() {
            return Truth.assertThat(code);
        }

        public @NotNull T isEqualTo(@NotNull String expected) {
            Truth.assertThat(code).isEqualTo(expected);
            return castAny(this);
        }

        public @NotNull T matches(@NotNull String expected) {
            return matchesConverted(expected, String::trim);
        }

        public @NotNull T matchesIgnoringSpaces(@NotNull String expected) {
            return matchesConverted(expected, TestingBytes::oneLiner);
        }

        protected @NotNull T matchesConverted(@NotNull String expected, @NotNull Function<String, String> convert) {
            Truth.assertThat(convert.apply(code)).isEqualTo(convert.apply(expected));
            return castAny(this);
        }
    }
}
