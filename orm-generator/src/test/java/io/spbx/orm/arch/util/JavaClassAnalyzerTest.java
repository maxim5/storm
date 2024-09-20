package io.spbx.orm.arch.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.arch.InvalidSqlModelException;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoPrivateFieldLocalGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoPrivateFieldPrivateGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoPrivateFieldProtectedGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoPrivateFieldPublicGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoProtectedFieldLocalGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoProtectedFieldPrivateGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoProtectedFieldProtectedGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoProtectedFieldPublicGetterDerived;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoPublicFieldDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.DerivedExtended;
import io.spbx.orm.arch.testing.TestingArchClasses.FooClassInterface;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldLocalGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldLocalGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldPrivateGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldPrivateGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldProtectedGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldProtectedGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldPublicGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldPublicGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldLocalGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldLocalGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPrivateGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPrivateGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldProtectedGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldProtectedGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPublicGetterDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPublicGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PublicFieldDerived;
import io.spbx.orm.arch.testing.TestingArchClasses.PublicFieldHolder;
import io.spbx.util.reflect.BasicMembers.Fields;
import io.spbx.util.reflect.BasicMembers.Scope;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@Tag("fast")
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JavaClassAnalyzerTest {
    /** {@link JavaClassAnalyzer#getAllFieldsOrderedRecursive(Class)} **/

    @Test
    public void getAllFieldsOrdered_simple_class() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(Point.class), "x", "y");
    }

    @Test
    public void getAllFieldsOrdered_class_implements_interface() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(FooClassInterface.class), "i");
    }

    @Test
    public void getAllFieldsOrdered_own_fields() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(PrivateFieldPublicGetterHolder.class), "foo");
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(PublicFieldHolder.class), "foo");
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(ProtectedFieldPublicGetterHolder.class), "foo");
    }

    @Test
    public void getAllFieldsOrdered_inherited_fields() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(PrivateFieldPublicGetterDerived.class), "foo");
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(PublicFieldDerived.class), "foo");
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(ProtectedFieldPublicGetterDerived.class), "foo");
    }

    @Test
    public void getAllFieldsOrdered_extended_fields() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrderedRecursive(DerivedExtended.class), "foo", "bar", "string");
    }

    /** {@link JavaClassAnalyzer#findAccessorOrDie(JavaField)} **/

    @Test
    public void findAccessorOrDie_own_fields() {
        assertJavaClass(PrivateFieldPublicGetterHolder.class).findsAccessor("foo", "foo()");
        assertJavaClass(PrivateFieldPrivateGetterHolder.class).doesNotFindAccessor("foo");
        assertJavaClass(PrivateFieldLocalGetterHolder.class).findsAccessor("foo", "foo()");
        assertJavaClass(PrivateFieldProtectedGetterHolder.class).findsAccessor("foo", "foo()");

        assertJavaClass(PublicFieldHolder.class).findsAccessor("foo", "foo");

        assertJavaClass(ProtectedFieldPublicGetterHolder.class).findsAccessor("foo", "foo");
        assertJavaClass(ProtectedFieldPrivateGetterHolder.class).findsAccessor("foo", "foo");
        assertJavaClass(ProtectedFieldLocalGetterHolder.class).findsAccessor("foo", "foo");
        assertJavaClass(ProtectedFieldProtectedGetterHolder.class).findsAccessor("foo", "foo");
    }

    @Test
    public void findAccessorOrDie_inherited_fields() {
        assertJavaClass(PrivateFieldPublicGetterDerived.class).findsAccessor("foo", "foo()");
        assertJavaClass(PrivateFieldPrivateGetterDerived.class).doesNotFindAccessor("foo");
        assertJavaClass(PrivateFieldLocalGetterDerived.class).findsAccessor("foo", "foo()");
        assertJavaClass(PrivateFieldProtectedGetterDerived.class).findsAccessor("foo", "foo()");

        assertJavaClass(PublicFieldDerived.class).findsAccessor("foo", "foo");

        assertJavaClass(ProtectedFieldPublicGetterDerived.class).findsAccessor("foo", "foo");
        assertJavaClass(ProtectedFieldPrivateGetterDerived.class).findsAccessor("foo", "foo");
        assertJavaClass(ProtectedFieldLocalGetterDerived.class).findsAccessor("foo", "foo");
        assertJavaClass(ProtectedFieldProtectedGetterDerived.class).findsAccessor("foo", "foo");
    }

    @Test
    public void findAccessorOrDie_inherited_fields_from_another_package() {
        assertJavaClass(AlsoPrivateFieldPublicGetterDerived.class).findsAccessor("foo", "foo()");
        assertJavaClass(AlsoPrivateFieldPrivateGetterDerived.class).doesNotFindAccessor("foo");
        assertJavaClass(AlsoPrivateFieldLocalGetterDerived.class).doesNotFindAccessor("foo");
        assertJavaClass(AlsoPrivateFieldProtectedGetterDerived.class).doesNotFindAccessor("foo");

        assertJavaClass(AlsoPublicFieldDerived.class).findsAccessor("foo", "foo");

        assertJavaClass(AlsoProtectedFieldPublicGetterDerived.class).findsAccessor("foo", "foo()");
        assertJavaClass(AlsoProtectedFieldPrivateGetterDerived.class).doesNotFindAccessor("foo");
        assertJavaClass(AlsoProtectedFieldLocalGetterDerived.class).doesNotFindAccessor("foo");
        assertJavaClass(AlsoProtectedFieldProtectedGetterDerived.class).doesNotFindAccessor("foo");
    }

    @Test
    public void findAccessorOrDie_extended_fields() {
        assertJavaClass(DerivedExtended.class).findsAccessor("foo", "foo()");
        assertJavaClass(DerivedExtended.class).findsAccessor("bar", "bar()");
        assertJavaClass(DerivedExtended.class).findsAccessor("string", "string()");
    }

    /** {@link JavaClassAnalyzer#findGetterMethodOrNull(JavaField)} **/

    @Test
    public void findGetterMethodOrNull_record_plain() {
        record FooRecord(int i, long l, String s) {}

        assertJavaClass(FooRecord.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
    }

    @Test
    public void findGetterMethodOrNull_record_with_getters() {
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
    public void findGetterMethodOrNull_pojo_getters() {
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
    public void findGetterMethodOrNull_pojo_standard_point() {
        assertJavaClass(Point.class)
            .doesNotFindMethod("x")
            .doesNotFindMethod("y");
    }

    @Test
    public void findGetterMethodOrNull_pojo_boolean() {
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
    public void findGetterMethodOrNull_pojo_accessors() {
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
    public void findGetterMethodOrNull_pojo_do_not_match_object_methods() {
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
    public void findGetterMethodOrNull_pojo_do_not_match_unrelated_methods() {
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

    @Test
    public void findGetterMethodOrNull_own_fields() {
        assertJavaClass(PrivateFieldPublicGetterHolder.class).findsMethod("foo", "foo");
        assertJavaClass(PrivateFieldPrivateGetterHolder.class).doesNotFindMethod("foo");
        assertJavaClass(PrivateFieldLocalGetterHolder.class).findsMethod("foo", "foo");
        assertJavaClass(PrivateFieldProtectedGetterHolder.class).findsMethod("foo", "foo");

        assertJavaClass(PublicFieldHolder.class).doesNotFindMethod("foo");

        assertJavaClass(ProtectedFieldPublicGetterHolder.class).findsMethod("foo", "foo");
        assertJavaClass(ProtectedFieldPrivateGetterHolder.class).doesNotFindMethod("foo");
        assertJavaClass(ProtectedFieldLocalGetterHolder.class).findsMethod("foo", "foo");
        assertJavaClass(ProtectedFieldProtectedGetterHolder.class).findsMethod("foo", "foo");
    }

    @Test
    public void findGetterMethodOrNull_inherited_fields() {
        assertJavaClass(PrivateFieldPublicGetterDerived.class).findsMethod("foo", "foo");
        assertJavaClass(PrivateFieldPrivateGetterDerived.class).doesNotFindMethod("foo");
        assertJavaClass(PrivateFieldLocalGetterDerived.class).findsMethod("foo", "foo");
        assertJavaClass(PrivateFieldProtectedGetterDerived.class).findsMethod("foo", "foo");

        assertJavaClass(PublicFieldDerived.class).doesNotFindMethod("foo");

        assertJavaClass(ProtectedFieldPublicGetterDerived.class).findsMethod("foo", "foo");
        assertJavaClass(ProtectedFieldPrivateGetterDerived.class).doesNotFindMethod("foo");
        assertJavaClass(ProtectedFieldLocalGetterDerived.class).findsMethod("foo", "foo");
        assertJavaClass(ProtectedFieldProtectedGetterDerived.class).findsMethod("foo", "foo");
    }

    @Test
    public void findGetterMethodOrNull_inherited_fields_from_another_package() {
        assertJavaClass(AlsoPrivateFieldPublicGetterDerived.class).findsMethod("foo", "foo");
        assertJavaClass(AlsoPrivateFieldPrivateGetterDerived.class).doesNotFindMethod("foo");
        assertJavaClass(AlsoPrivateFieldLocalGetterDerived.class).doesNotFindMethod("foo");
        assertJavaClass(AlsoPrivateFieldProtectedGetterDerived.class).doesNotFindMethod("foo");

        assertJavaClass(AlsoPublicFieldDerived.class).doesNotFindMethod("foo");

        assertJavaClass(AlsoProtectedFieldPublicGetterDerived.class).findsMethod("foo", "foo");
        assertJavaClass(AlsoProtectedFieldPrivateGetterDerived.class).doesNotFindMethod("foo");
        assertJavaClass(AlsoProtectedFieldLocalGetterDerived.class).doesNotFindMethod("foo");
        assertJavaClass(AlsoProtectedFieldProtectedGetterDerived.class).doesNotFindMethod("foo");
    }

    @Test
    public void findGetterMethodOrNull_extended_fields() {
        assertJavaClass(DerivedExtended.class).findsMethod("foo", "foo");
        assertJavaClass(DerivedExtended.class).findsMethod("bar", "bar");
        assertJavaClass(DerivedExtended.class).findsMethod("string", "string");
    }

    /** Implementation **/

    private static void assertFields(@NotNull List<Field> fields, @NotNull String ... names) {
        assertThat(fields.stream().map(Field::getName).toList()).containsExactlyElementsIn(names);
    }

    @CheckReturnValue
    private static @NotNull JavaClassAnalyzerSubject assertJavaClass(@NotNull Class<?> klass) {
        return new JavaClassAnalyzerSubject(klass);
    }

    @CanIgnoreReturnValue
    private record JavaClassAnalyzerSubject(@NotNull Class<?> klass) {
        public @NotNull JavaClassAnalyzerSubject findsAccessor(@NotNull String name, @NotNull String expected) {
            JavaField field = getFieldByName(name);
            Accessor accessor = JavaClassAnalyzer.findAccessorOrDie(field);
            assertThat(accessor.value()).isEqualTo(expected);
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject doesNotFindAccessor(@NotNull String name) {
            JavaField field = getFieldByName(name);
            Assertions.assertThrows(InvalidSqlModelException.class, () -> JavaClassAnalyzer.findAccessorOrDie(field));
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject findsMethod(@NotNull String name, @NotNull String expected) {
            JavaField field = getFieldByName(name);
            JavaMethod getterMethod = JavaClassAnalyzer.findGetterMethodOrNull(field);
            assertThat(getterMethod).isNotNull();
            assertThat(getterMethod.getName()).isEqualTo(expected);
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject doesNotFindMethod(@NotNull String name) {
            JavaField field = getFieldByName(name);
            JavaMethod getterMethod = JavaClassAnalyzer.findGetterMethodOrNull(field);
            assertThat(getterMethod).isNull();
            return this;
        }

        private @NotNull JavaField getFieldByName(@NotNull String name) {
            Field field = Fields.of(klass).getOrDie(Scope.HIERARCHY_ALL, name);
            return new JavaField(field, klass);
        }
    }
}
