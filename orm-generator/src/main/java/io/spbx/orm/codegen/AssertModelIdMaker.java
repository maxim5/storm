package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

class AssertModelIdMaker {
    public static @NotNull Snippet makeAssert(@NotNull String param, @NotNull TableArch table) {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return new Snippet().appendLine(JavaSupport.EMPTY_LINE);
        }
        String pattern = """
            assert !(%s.%s == 0 && engine() == Engine.MySQL) :
                    "Null PK is treated as an auto-increment in MySQL. Call insertAutoIncPk() instead. Value: " + %s;""";
        String primaryKeyAccessor = requireNonNull(table.primaryKeyField()).javaAccessor();
        return new Snippet().appendFormattedMultiline(pattern, param, primaryKeyAccessor, param);
    }
}
