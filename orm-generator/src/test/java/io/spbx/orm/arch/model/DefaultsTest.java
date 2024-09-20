package io.spbx.orm.arch.model;

import io.spbx.orm.arch.InvalidSqlModelException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.arch.model.Defaults.EMPTY_COLUMN_DEFAULTS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("fast")
public class DefaultsTest {
    @Test
    public void of_one_column_empty() {
        assertThat(Defaults.ofOneColumn((String[]) null)).isSameInstanceAs(EMPTY_COLUMN_DEFAULTS);
        assertThat(Defaults.ofOneColumn((String) null)).isSameInstanceAs(EMPTY_COLUMN_DEFAULTS);
        assertThat(Defaults.ofOneColumn(new String[1])).isSameInstanceAs(EMPTY_COLUMN_DEFAULTS);
    }

    @Test
    public void of_one_column_valid_value() {
        assertThat(Defaults.ofOneColumn("")).isEqualTo(new Defaults(new String[]{""}));
        assertThat(Defaults.ofOneColumn("foo")).isEqualTo(new Defaults(new String[]{"foo"}));
    }

    @Test
    public void of_one_column_invalid_value() {
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofOneColumn("foo", "bar"));
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofOneColumn("foo", "bar", "baz"));
    }

    @Test
    public void of_multi_columns_empty() {
        assertThat(Defaults.ofMultiColumns(1, (String[]) null)).isSameInstanceAs(EMPTY_COLUMN_DEFAULTS);
        assertThat(Defaults.ofMultiColumns(1, (String) null)).isSameInstanceAs(EMPTY_COLUMN_DEFAULTS);
        assertThat(Defaults.ofMultiColumns(1, new String[1])).isSameInstanceAs(EMPTY_COLUMN_DEFAULTS);

        assertThat(Defaults.ofMultiColumns(2, (String[]) null)).isEqualTo(new Defaults(new String[2]));
        assertThat(Defaults.ofMultiColumns(2, null, null)).isEqualTo(new Defaults(new String[2]));
        assertThat(Defaults.ofMultiColumns(2, new String[2])).isEqualTo(new Defaults(new String[2]));

        assertThat(Defaults.ofMultiColumns(3, (String[]) null)).isEqualTo(new Defaults(new String[3]));
        assertThat(Defaults.ofMultiColumns(3, null, null, null)).isEqualTo(new Defaults(new String[3]));
        assertThat(Defaults.ofMultiColumns(3, new String[3])).isEqualTo(new Defaults(new String[3]));
    }

    @Test
    public void of_multi_columns_valid_values() {
        assertThat(Defaults.ofMultiColumns(1, "")).isEqualTo(new Defaults(new String[]{""}));
        assertThat(Defaults.ofMultiColumns(1, "foo")).isEqualTo(new Defaults(new String[]{"foo"}));

        assertThat(Defaults.ofMultiColumns(2, "", "")).isEqualTo(new Defaults(new String[]{"", ""}));
        assertThat(Defaults.ofMultiColumns(2, null, "")).isEqualTo(new Defaults(new String[]{null, ""}));
        assertThat(Defaults.ofMultiColumns(2, "", null)).isEqualTo(new Defaults(new String[]{"", null}));
        assertThat(Defaults.ofMultiColumns(2, "foo", "bar")).isEqualTo(new Defaults(new String[]{"foo", "bar"}));

        assertThat(Defaults.ofMultiColumns(3, "", "", "")).isEqualTo(new Defaults(new String[]{"", "", ""}));
        assertThat(Defaults.ofMultiColumns(3, null, "", "")).isEqualTo(new Defaults(new String[]{null, "", ""}));
        assertThat(Defaults.ofMultiColumns(3, "", null, "")).isEqualTo(new Defaults(new String[]{"", null, ""}));
        assertThat(Defaults.ofMultiColumns(3, "", "", null)).isEqualTo(new Defaults(new String[]{"", "", null}));
        assertThat(Defaults.ofMultiColumns(3, "foo", "bar", "baz")).isEqualTo(new Defaults(new String[]{"foo", "bar", "baz"}));
    }

    @Test
    public void of_multi_columns_invalid() {
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofMultiColumns(2, ""));
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofMultiColumns(2, "", "", ""));
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofMultiColumns(2, null, null, null));
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofMultiColumns(3, null, null));
        assertThrows(InvalidSqlModelException.class, () -> Defaults.ofMultiColumns(3, "", "", null, null));
    }

    @Test
    public void size() {
        assertThat(EMPTY_COLUMN_DEFAULTS.size()).isEqualTo(1);
        assertThat(Defaults.ofOneColumn("foo").size()).isEqualTo(1);
        assertThat(Defaults.ofMultiColumns(2, "foo", "bar").size()).isEqualTo(2);
        assertThat(Defaults.ofMultiColumns(2, null, null).size()).isEqualTo(2);
    }

    @Test
    public void value_at() {
        assertThat(EMPTY_COLUMN_DEFAULTS.at(0)).isEqualTo(null);
        assertThat(Defaults.ofOneColumn("foo").at(0)).isEqualTo("foo");

        assertThat(Defaults.ofMultiColumns(2, "foo", null).at(0)).isEqualTo("foo");
        assertThat(Defaults.ofMultiColumns(2, "foo", null).at(1)).isEqualTo(null);
    }
}
