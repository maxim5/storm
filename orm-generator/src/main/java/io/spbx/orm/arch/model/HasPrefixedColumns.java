package io.spbx.orm.arch.model;

import io.spbx.orm.api.ReadFollow;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HasPrefixedColumns {
    @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow);

    default int columnsNumber(@NotNull ReadFollow follow) {
        return columns(follow).size();
    }
}
