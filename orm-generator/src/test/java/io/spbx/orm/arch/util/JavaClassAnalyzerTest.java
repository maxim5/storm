package io.spbx.orm.arch.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.util.reflect.BasicMembers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JavaClassAnalyzerTest {
    @Test
    public void getAllFieldsOrdered_class() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrdered(Point.class), "x", "y");
    }

    @Test
    public void getAllFieldsOrdered_class_implements_interface() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrdered(FooClassInterface.class), "i");
    }

    @SuppressWarnings("FieldMayBeFinal")
    static class FooClassInterface implements Serializable {
        private int i;
        public FooClassInterface(int i) {
            this.i = i;
        }
    }

    @Test
    public void findGetterMethod_record_plain() {
        record FooRecord(int i, long l, String s) {}

        assertJavaClass(FooRecord.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
    }

    @Test
    public void findGetterMethod_record_with_getters() {
        record FooRecord(int i, long l, String s) {
            public int getI() { return i; }
            public String getS() { return s; }
        }

        assertJavaClass(FooRecord.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
    }

    @Test
    public void findGetterMethod_pojo_getters() {
        class FooPojo {
            private int i;
            private long l;
            private String s;

            public int getI() { return i; }
            public long getL() { return l; }
            public String getS() { return s; }
        }

        assertJavaClass(FooPojo.class)
            .findsMethod("i", "getI")
            .findsMethod("l", "getL")
            .findsMethod("s", "getS");
    }

    @Test
    public void findGetterMethod_pojo_standard_point() {
        assertJavaClass(Point.class)
            .doesNotFindMethod("x")
            .doesNotFindMethod("y");
    }

    @Test
    public void findGetterMethod_pojo_boolean() {
        class FooPojo {
            private boolean enabled;
            private boolean disabled;
            private boolean off;

            public boolean isEnabled() { return enabled; }
            public boolean disabled() { return disabled; }
            public boolean getOff() { return off; }
        }

        assertJavaClass(FooPojo.class)
            .findsMethod("enabled", "isEnabled")
            .findsMethod("disabled", "disabled")
            .findsMethod("off", "getOff");
    }

    @Test
    public void findGetterMethod_pojo_accessors() {
        class FooPojo {
            private int i;
            private long l;
            private String s;

            public int i() { return i; }
            public long l() { return l; }
            public String s() { return s; }
        }

        assertJavaClass(FooPojo.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
    }

    @Test
    public void findGetterMethod_pojo_do_not_match_object_methods() {
        class FooPojo {
            private int i;
            private String s;
            // hashCode() and toString()
        }

        assertJavaClass(FooPojo.class)
            .doesNotFindMethod("i")
            .doesNotFindMethod("s");
    }

    @Test
    public void findGetterMethod_pojo_do_not_match_unrelated_methods() {
        class FooPojo {
            private int foo;
            private long bar;
            private String baz;

            public int value() { return foo; }
            public long barCode() { return bar; }
            public String bazValue() { return baz; }
        }

        assertJavaClass(FooPojo.class)
            .doesNotFindMethod("foo")
            .doesNotFindMethod("bar")
            .doesNotFindMethod("baz");
    }

    private static void assertFields(@NotNull List<Field> fields, @NotNull String ... names) {
        assertThat(fields.stream().map(Field::getName).toList()).containsExactlyElementsIn(names);
    }

    @CheckReturnValue
    private static @NotNull JavaClassAnalyzerSubject assertJavaClass(@NotNull Class<?> klass) {
        return new JavaClassAnalyzerSubject(klass);
    }

    @CanIgnoreReturnValue
    private record JavaClassAnalyzerSubject(@NotNull Class<?> klass) {
        public @NotNull JavaClassAnalyzerSubject findsMethod(@NotNull String name, @NotNull String expected) {
            Method getterMethod = findGetterMethod(name);
            assertThat(getterMethod).isNotNull();
            assertThat(getterMethod.getName()).isEqualTo(expected);
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject doesNotFindMethod(@NotNull String name) {
            Method getterMethod = findGetterMethod(name);
            assertThat(getterMethod).isNull();
            return this;
        }

        private @Nullable Method findGetterMethod(@NotNull String name) {
            return JavaClassAnalyzer.findGetterMethod(getFieldByName(name));
        }

        private @NotNull Field getFieldByName(@NotNull String name) {
            Field field = BasicMembers.findField(klass, name);
            assertThat(field).isNotNull();
            return field;
        }
    }
}
