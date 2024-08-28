package io.spbx.orm.codegen;

import io.spbx.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

class DeleteMaker {
    public static @NotNull Snippet make(@NotNull TableArch table) {
         return new Snippet().appendLine("DELETE FROM ", table.sqlName());
    }
}
