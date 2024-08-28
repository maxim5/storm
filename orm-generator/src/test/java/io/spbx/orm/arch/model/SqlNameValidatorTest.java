package io.spbx.orm.arch.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.arch.model.SqlNameValidator.isValidSqlName;

@Tag("fast")
public class SqlNameValidatorTest {
    @Test
    public void isValidSqlName_simple() {
        assertThat(isValidSqlName("foo")).isTrue();
        assertThat(isValidSqlName("_")).isTrue();
        assertThat(isValidSqlName("_____")).isTrue();
        assertThat(isValidSqlName("Foo")).isTrue();
        assertThat(isValidSqlName("foo777")).isTrue();
        assertThat(isValidSqlName("_foo_BAR_777_")).isTrue();

        assertThat(isValidSqlName("")).isFalse();
        assertThat(isValidSqlName(" ")).isFalse();
        assertThat(isValidSqlName("777")).isFalse();
        assertThat(isValidSqlName("777foo")).isFalse();
        assertThat(isValidSqlName("-foo-")).isFalse();
    }
}
