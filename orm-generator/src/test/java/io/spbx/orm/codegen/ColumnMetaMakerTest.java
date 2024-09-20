package io.spbx.orm.codegen;

import com.google.common.collect.MoreCollectors;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.api.annotate.Sql;
import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.arch.factory.TestingArch.buildTableArch;

@Tag("fast")
public class ColumnMetaMakerTest {
    @Test
    public void int_column() {
        record User(int foo) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("foo")
            .isEqualTo("ColumnMeta.of(OwnColumn.foo, int.class)");
    }

    @Test
    public void long_column() {
        record User(boolean foo) {}

        TableArch tableArch = buildTableArch(User.class);

        assertThat(tableArch)
            .columnMetaOf("foo")
            .isEqualTo("ColumnMeta.of(OwnColumn.foo, boolean.class)");
    }

    @Test
    public void string_column() {
        record User(String foo) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("foo")
            .isEqualTo("ColumnMeta.of(OwnColumn.foo, String.class)");
    }

    @Test
    public void boolean_column() {
        record User(boolean foo) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("foo")
            .isEqualTo("ColumnMeta.of(OwnColumn.foo, boolean.class)");
    }

    @Test
    public void primary_key() {
        record User(int userId) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("userId")
            .isEqualTo("ColumnMeta.of(OwnColumn.user_id, int.class).withPrimaryKey(ConstraintStatus.SINGLE_COLUMN)");
    }

    @Test
    public void unique_column() {
        record User(@Sql(unique = true) int foo) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("foo")
            .isEqualTo("ColumnMeta.of(OwnColumn.foo, int.class).withUnique(ConstraintStatus.SINGLE_COLUMN)");
    }

    @Test
    public void default_value_column() {
        record User(@Sql.Default("0") int foo) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("foo")
            .isEqualTo("ColumnMeta.of(OwnColumn.foo, int.class).withDefault(\"0\")");
    }

    @Test
    public void column_with_many_constraints() {
        record User(@Sql(name = "bar", unique = true, nullable = true, defaults = "111") int foo) {}

        assertThat(buildTableArch(User.class))
            .columnMetaOf("foo")
            .isEqualTo(
                "ColumnMeta.of(OwnColumn.bar, int.class)" +
                    ".withUnique(ConstraintStatus.SINGLE_COLUMN)" +
                    ".withNullable(true)" +
                    ".withDefault(\"111\")"
            );
    }

    @CheckReturnValue
    private static @NotNull ColumnMetaMakerSubject assertThat(@NotNull TableArch tableArch) {
        return new ColumnMetaMakerSubject(tableArch);
    }

    @CanIgnoreReturnValue
    private record ColumnMetaMakerSubject(@NotNull TableArch tableArch) {
        public @NotNull StringSubject columnMetaOf(@NotNull String javaName) {
            Pair<TableField, Column> matched = tableArch.columnsWithFields().stream()
                .filter(pair -> pair.mapFirst(TableField::javaName).testFirst(javaName::equals))
                .collect(MoreCollectors.onlyElement());
            String columnMeta = ColumnMetaMaker.makeColumnMeta(matched.first(), matched.second());
            return Truth.assertThat(columnMeta);
        }
    }
}
