package io.spbx.orm.codegen;

import io.spbx.orm.adapter.std.chars.CharArrayJdbcAdapter;
import io.spbx.orm.adapter.std.chars.CharSequenceJdbcAdapter;
import io.spbx.orm.adapter.std.chars.CharacterJdbcAdapter;
import io.spbx.orm.adapter.std.lang.BytesMapper;
import io.spbx.orm.adapter.std.time.InstantJdbcAdapter;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Foreign;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.api.TableInt;
import io.spbx.orm.api.TableLong;
import io.spbx.orm.api.TableObj;
import io.spbx.orm.api.annotate.Sql;
import io.spbx.orm.arch.factory.TestingArch;
import io.spbx.orm.testing.FakeModelAdaptersLocator;
import io.spbx.util.base.Maybe;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;

@Tag("fast")
public class ImportsCollectorTest {
    private static final FakeModelAdaptersLocator LOCATOR = makeLocator();
    private static final String ORM_API_PACKAGE = BaseTable.class.getPackageName();  // io.spbx.orm.api
    private static final Predicate<String> IS_ORM_API = importLine -> importLine.startsWith(ORM_API_PACKAGE);
    private static final Predicate<String> IS_NOT_API = IS_ORM_API.negate();

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_primitive_fields_except_char(Class<?> baseClass) {
        record User(int i, long l, short s, byte b, boolean bool, double d, float f) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(FQN.of(baseClass).toImportName());
        assertThat(imports.stream().filter(IS_NOT_API)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_char_field(Class<?> baseClass) {
        record User(char ch) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports.stream().filter(IS_NOT_API)).containsExactly(importNameOf(CharacterJdbcAdapter.class));
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_string_fields(Class<?> baseClass) {
        record User(String s, CharSequence sequence, char[] buf) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports.stream().filter(IS_NOT_API))
            .containsExactly(importNameOf(CharSequenceJdbcAdapter.class), importNameOf(CharArrayJdbcAdapter.class));
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_optional_field(Class<?> baseClass) {
        record User(Optional<String> optional) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports.stream().filter(IS_NOT_API)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_enum_field(Class<?> baseClass) {
        record User(Maybe maybe) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports.stream().filter(IS_NOT_API)).containsExactly(importNameOf(Maybe.class));
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_instant_field(Class<?> baseClass) {
        record User(Instant instant) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports.stream().filter(IS_NOT_API))
            .containsExactly(importNameOf(Instant.class), importNameOf(InstantJdbcAdapter.class));
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_instant_field_with_mapper(Class<?> baseClass) {
        class InstantMapper extends BytesMapper<Instant> {}
        record User(@Sql.Via(InstantMapper.class) Instant instant) {}

        List<String> imports = newImportsCollector(User.class, baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports.stream().filter(IS_NOT_API)).containsExactly(importNameOf(Instant.class));
    }

    @ParameterizedTest
    @ValueSource(classes = { BaseTable.class, TableInt.class, TableLong.class, TableObj.class })
    public void imports_foreign_key_instant_field(Class<?> baseClass) {
        record Other(int otherId, Instant instant) {}
        record User(ForeignInt<Other> other) {}

        List<String> imports = newImportsCollector(User.class, List.of(Other.class), baseClass).imports();
        assertThat(imports).isInStrictOrder();
        assertThat(imports).contains(importNameOf(baseClass));
        assertThat(imports).containsAtLeast(importNameOf(Foreign.class),
                                            importNameOf(ForeignInt.class),
                                            importNameOf(ForeignLong.class),
                                            importNameOf(ForeignObj.class));
        assertThat(imports.stream().filter(IS_NOT_API)).isEmpty();
    }

    private static @NotNull ImportsCollector newImportsCollector(@NotNull Class<?> model, @NotNull Class<?> baseClass) {
        return new ImportsCollector(LOCATOR, TestingArch.buildTableArch(LOCATOR, model), baseClass);
    }

    private static @NotNull ImportsCollector newImportsCollector(@NotNull Class<?> model,
                                                                 @NotNull List<Class<?>> rest,
                                                                 @NotNull Class<?> baseClass) {
        return new ImportsCollector(LOCATOR, TestingArch.buildTableArch(LOCATOR, model, rest), baseClass);
    }

    private static @NotNull FakeModelAdaptersLocator makeLocator() {
        FakeModelAdaptersLocator locator = FakeModelAdaptersLocator.defaults();
        locator.setupAdapter(CharSequence.class, CharSequenceJdbcAdapter.class);
        locator.setupAdapter(char[].class, CharArrayJdbcAdapter.class);
        locator.setupAdapter(Instant.class, InstantJdbcAdapter.class);
        return locator;
    }

    private static @NotNull String importNameOf(@NotNull Class<?> klass) {
        return FQN.of(klass).toImportName();
    }
}
