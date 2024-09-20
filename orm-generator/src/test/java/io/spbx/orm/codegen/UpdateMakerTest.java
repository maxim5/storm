package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.orm.arch.factory.TestingArch.buildTableArch;
import static io.spbx.orm.codegen.AssertSnippet.assertThatSql;

@Tag("fast")
public class UpdateMakerTest {
    @Test
    public void make_one_column() {
        record User(boolean value) {}

        TableArch tableArch = buildTableArch(User.class);

        assertThatSql(UpdateMaker.make(tableArch, tableArch.columns())).matches("""
            UPDATE user
            SET value=?
            """);
    }

    @Test
    public void make_two_columns() {
        record User(int userId, String name) {}

        TableArch tableArch = buildTableArch(User.class);

        assertThatSql(UpdateMaker.make(tableArch, tableArch.columns())).matches("""
            UPDATE user
            SET user_id=?, name=?
            """);
        assertThatSql(UpdateMaker.make(tableArch, tableArch.columns(TableField::isNotPrimaryKey))).matches("""
            UPDATE user
            SET name=?
            """);
    }
}
