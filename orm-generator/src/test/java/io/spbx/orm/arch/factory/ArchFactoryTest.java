package io.spbx.orm.arch.factory;

import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.orm.adapter.std.lang.AtomicIntegerJdbcAdapter;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.api.annotate.Model;
import io.spbx.orm.api.annotate.Sql;
import io.spbx.orm.arch.InvalidSqlModelException;
import io.spbx.orm.arch.factory.MoreTestingArchClasses.AlsoProtectedFieldPublicGetterDerived;
import io.spbx.orm.arch.model.JdbcType;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPublicGetterDerived;
import io.spbx.orm.testing.FakeModelAdaptersLocator;
import io.spbx.util.base.Maybe;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.spbx.orm.api.annotate.Sql.Default;
import static io.spbx.orm.api.annotate.Sql.PK;
import static io.spbx.orm.arch.factory.TestingArch.FieldConstraints.*;
import static io.spbx.orm.arch.factory.TestingArch.TableFieldsStatus.*;
import static io.spbx.orm.arch.factory.TestingArch.assertThat;
import static io.spbx.orm.arch.factory.TestingArch.buildTableArch;
import static io.spbx.util.testing.TestingBasics.listOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("fast")
public class ArchFactoryTest {
    /** Generic tables **/

    @Test
    public void empty_table() {
        record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class));  // maybe should throw?
    }

    @Test
    public void default_table() {
        record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class))
            .hasTableName("simple_user", "io.spbx.orm.arch.factory", "SimpleUserTable")
            .hasModel("SimpleUser", SimpleUser.class)
            .hasFields(ONLY_ORDINARY);
    }

    @Test
    public void renamed_model_table() {
        @Model(javaName = "MyUser") record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class))
            .hasTableName("my_user", "io.spbx.orm.arch.factory", "MyUserTable")
            .hasModel("MyUser", SimpleUser.class)
            .hasFields(ONLY_ORDINARY);
    }

    @Test
    public void renamed_all_table() {
        @Model(javaName = "MyUser", sqlName = "users", javaTableName = "UserTable") record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class))
            .hasTableName("users", "io.spbx.orm.arch.factory", "UserTable")
            .hasModel("MyUser", SimpleUser.class)
            .hasFields(ONLY_ORDINARY);
    }

    /** Single usual field **/

    @Test
    public void single_field_int() {
        record User(int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_long() {
        record User(long foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(long.class, "foo()")
            .isSingleColumn("foo", JdbcType.Long)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_boolean() {
        record User(boolean foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(boolean.class, "foo()")
            .isSingleColumn("foo", JdbcType.Boolean)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_string() {
        record User(String foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_integer() {
        record User(Integer foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Integer.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_char() {
        record User(char foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(char.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isAdapterSupportedType()
            .usesAdapter("CharacterJdbcAdapter");
    }

    @Test
    public void single_field_int_renamed() {
        record User(@Sql(name = "bar") int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("bar", JdbcType.Int)
            .doesNotHaveDefault()
            .hasConstraints(ORDINARY.nonnull())
            .isNativelySupportedType();
    }

    @Test
    public void single_field_enum() {
        record User(Maybe bool) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("bool")
            .isFromTable("user")
            .hasInJava(Maybe.class, "bool()")
            .isSingleColumn("bool", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveAnyDefaults()
            .isMapperSupportedType();
    }

    @Test
    public void single_field_complex() {
        record Tuple(int a, String b) {}
        record User(Tuple foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Tuple.class, "foo()")
            .hasColumns(Pair.of("foo_a", JdbcType.Int), Pair.of("foo_b", JdbcType.String))
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveAnyDefaults()
            .isAdapterSupportedType()
            .usesAdapter("TupleJdbcAdapter.ADAPTER");
    }

    @Test
    public void single_field_complex_standard_point() {
        record User(Point point) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("point")
            .isFromTable("user")
            .hasInJava(Point.class, "point()")
            .hasColumns(Pair.of("point_x", JdbcType.Int), Pair.of("point_y", JdbcType.Int))
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveAnyDefaults()
            .isAdapterSupportedType()
            .usesAdapter("PointJdbcAdapter.ADAPTER");
    }

    @Test
    public void single_field_optional() {
        record User(Optional<Integer> opt) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("opt")
            .isFromTable("user")
            .hasInJava(Optional.class, "opt()")
            .isSingleColumn("opt", JdbcType.Int)
            .hasConstraints(ORDINARY.nullable())
            .doesNotHaveAnyDefaults()
            .isMapperSupportedType();
    }

    @Test
    public void single_field_atomic_integer() {
        record User(AtomicInteger atomic) {}

        FakeModelAdaptersLocator locator = FakeModelAdaptersLocator.empty();
        locator.setupAdapter(AtomicInteger.class, AtomicIntegerJdbcAdapter.class);
        assertThat(buildTableArch(locator, User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("atomic")
            .isFromTable("user")
            .hasInJava(AtomicInteger.class, "atomic()")
            .isSingleColumn("atomic", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveAnyDefaults()
            .isAdapterSupportedType();
    }

    @Test
    public void single_field_atomic_reference() {
        record User(AtomicReference<Integer> atomic) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("atomic")
            .isFromTable("user")
            .hasInJava(AtomicReference.class, "atomic()")
            .isSingleColumn("atomic", JdbcType.Int)
            .hasConstraints(ORDINARY.nullable())
            .doesNotHaveAnyDefaults()
            .isMapperSupportedType();
    }

    /** Single primary key field **/

    @Test
    public void single_field_int_pk() {
        record User(int userId) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_INT).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(int.class, "userId()")
            .isSingleColumn("user_id", JdbcType.Int)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_int_pk_just_id() {
        record User(int id) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_INT).hasSingleFieldThat("id")
            .isFromTable("user")
            .hasInJava(int.class, "id()")
            .isSingleColumn("id", JdbcType.Int)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_long_pk() {
        record User(long userId) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_LONG).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(long.class, "userId()")
            .isSingleColumn("user_id", JdbcType.Long)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_string_pk() {
        record User(String userId) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_OBJ).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(String.class, "userId()")
            .isSingleColumn("user_id", JdbcType.String)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_integer_pk() {
        record User(Integer userId) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_OBJ).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(Integer.class, "userId()")
            .isSingleColumn("user_id", JdbcType.Int)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_int_pk_annotated() {
        record User(@Sql(primary = true) int userKey) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_INT).hasSingleFieldThat("userKey")
            .isFromTable("user")
            .hasInJava(int.class, "userKey()")
            .isSingleColumn("user_key", JdbcType.Int)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_string_pk_annotated() {
        record User(@PK String userKey) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_OBJ).hasSingleFieldThat("userKey")
            .isFromTable("user")
            .hasInJava(String.class, "userKey()")
            .isSingleColumn("user_key", JdbcType.String)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    /** Single foreign key field **/

    @Test
    public void single_field_int_fk() {
        record Type(int id) {}
        record User(ForeignInt<Type> type) {}

        assertThat(buildTableArch(User.class, listOf(Type.class))).hasFields(FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignInt.class, "type()")
            .isSingleColumn("type_id", JdbcType.Int)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY.nonnull())
            .doesNotHaveDefault();
    }

    @Test
    public void single_field_long_fk() {
        record Type(long typeId) {}
        record User(ForeignLong<Type> type) {}

        assertThat(buildTableArch(User.class, listOf(Type.class))).hasFields(FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignLong.class, "type()")
            .isSingleColumn("type_id", JdbcType.Long)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY.nonnull())
            .doesNotHaveDefault();
    }

    @Test
    public void single_field_string_fk() {
        record Type(String typeId) {}
        record User(ForeignObj<String, Type> type) {}

        assertThat(buildTableArch(User.class, listOf(Type.class))).hasFields(FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignObj.class, "type()")
            .isSingleColumn("type_id", JdbcType.String)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY.nonnull())
            .doesNotHaveDefault();
    }

    /** Single field with default **/

    @Test
    public void single_field_int_with_default() {
        record User(@Default("0") int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .hasDefault("0")
            .isNativelySupportedType();
    }

    @Test
    public void single_field_long_with_default() {
        record User(@Default("-1") long foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(long.class, "foo()")
            .isSingleColumn("foo", JdbcType.Long)
            .hasConstraints(ORDINARY.nonnull())
            .hasDefault("-1")
            .isNativelySupportedType();
    }

    @Test
    public void single_field_string_with_default() {
        record User(@Default("") String foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(ORDINARY.nonnull())
            .hasDefault("")
            .isNativelySupportedType();
    }

    @Test
    public void single_field_int_pk_annotated_with_default() {
        record User(@Sql(primary = true, defaults = "-1") int userKey) {}

        assertThat(buildTableArch(User.class)).hasFields(PRIMARY_INT).hasSingleFieldThat("userKey")
            .isFromTable("user")
            .hasInJava(int.class, "userKey()")
            .isSingleColumn("user_key", JdbcType.Int)
            .hasConstraints(PRIMARY_KEY.nonnull())
            .hasDefault("-1")
            .isNativelySupportedType();
    }

    @Test
    public void single_field_complex_with_default() {
        record Tuple(int a, String b) {}
        record User(@Sql(defaults = {"0", ""}) Tuple foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Tuple.class, "foo()")
            .hasColumns(Pair.of("foo_a", JdbcType.Int), Pair.of("foo_b", JdbcType.String))
            .hasConstraints(ORDINARY.nonnull())
            .hasDefault("foo_a", "0")
            .hasDefault("foo_b", "")
            .isAdapterSupportedType()
            .usesAdapter("TupleJdbcAdapter.ADAPTER");
    }

    /** Single unique field **/

    @Test
    public void single_field_int_unique() {
        record User(@Sql(unique = true) int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(UNIQUE.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_long_unique() {
        record User(@Sql(unique = true) long foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(long.class, "foo()")
            .isSingleColumn("foo", JdbcType.Long)
            .hasConstraints(UNIQUE.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_string_unique() {
        record User(@Sql(unique = true) String foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(UNIQUE.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_integer_unique() {
        record User(@Sql(unique = true) Integer foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Integer.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(UNIQUE.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void single_field_string_unique_shortcut_annotation() {
        record User(@Sql.Unique String foo) {}

        assertThat(buildTableArch(User.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(UNIQUE.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    /** Special cases **/

    @Test
    public void inherited_same_package_protected_fields() {
        assertThat(buildTableArch(ProtectedFieldPublicGetterDerived.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("testing_arch_classes_protected_field_public_getter_derived")
            .hasInJava(int.class, "foo")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    public void inherited_another_package_protected_fields() {
        assertThat(buildTableArch(AlsoProtectedFieldPublicGetterDerived.class)).hasFields(ONLY_ORDINARY).hasSingleFieldThat("foo")
            .isFromTable("more_testing_arch_classes_also_protected_field_public_getter_derived")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(ORDINARY.nonnull())
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    /** Invalid models **/

    @Test
    public void invalid_list_field() {
        record ListModel(List<Object> value) {}

        InvalidSqlModelException e = assertInvalidModel(ListModel.class);
        Truth.assertThat(e).hasMessageThat().contains("ListModel.value");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().containsMatch("a collection.*java.util.List<java.lang.Object>");
    }

    @Test
    public void invalid_set_field() {
        record SetModel(Set<String> value) {}

        InvalidSqlModelException e = assertInvalidModel(SetModel.class);
        Truth.assertThat(e).hasMessageThat().contains("SetModel.value");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().containsMatch("a collection.*java.util.Set<java.lang.String>");
    }

    @Test
    public void invalid_collection_field() {
        record CollectionModel(Collection<String> value) {}

        InvalidSqlModelException e = assertInvalidModel(CollectionModel.class);
        Truth.assertThat(e).hasMessageThat().contains("CollectionModel.value");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().containsMatch("a collection.*java.util.Collection<java.lang.String>");
    }

    @Test
    public void invalid_interface_field() {
        record SerializableModel(Serializable value) {}

        InvalidSqlModelException e = assertInvalidModel(SerializableModel.class);
        Truth.assertThat(e).hasMessageThat().contains("SerializableModel.value");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().containsMatch("an interface.*java.io.Serializable");
    }

    @Test
    public void invalid_array_field() {
        record ArrayModel(int[] array) {}

        InvalidSqlModelException e = assertInvalidModel(ArrayModel.class);
        Truth.assertThat(e).hasMessageThat().contains("ArrayModel.array");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().containsMatch("array.*int\\[]");
    }

    @Test
    public void invalid_object_field() {
        record ObjectModel(Object object) {}

        InvalidSqlModelException e = assertInvalidModel(ObjectModel.class);
        Truth.assertThat(e).hasMessageThat().contains("ObjectModel.object");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().containsMatch("a raw.*java.lang.Object");
    }

    @Test
    public void invalid_foreign_key_mismatch() {
        record User(int userId, String name) {}
        record Song(ForeignLong<User> author) {}

        InvalidSqlModelException e = assertInvalidModel(User.class, Song.class);
        Truth.assertThat(e).hasMessageThat().contains("Song.author");
        Truth.assertThat(e).hasCauseThat().hasMessageThat()
            .isEqualTo("Foreign model `User` primary key `int` doesn't match the foreign key");
    }

    @Test
    public void invalid_nested_inner_class() {
        class Nested {}

        InvalidSqlModelException e = assertInvalidModel(Nested.class);
        Truth.assertThat(e).hasMessageThat().contains("Nested");
        Truth.assertThat(e).hasCauseThat().hasMessageThat().contains("is inner nested in");
    }

    @CanIgnoreReturnValue
    private static @NotNull InvalidSqlModelException assertInvalidModel(@NotNull Class<?> @NotNull ... models) {
        return assertThrows(InvalidSqlModelException.class, () ->
            new ArchFactory(FakeModelAdaptersLocator.FAKE_LOCATOR).build(TestingArch.newRunInputs(models))
        );
    }
}
