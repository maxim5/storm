package io.spbx.orm.arch.model;

import com.google.common.base.Strings;
import io.spbx.orm.api.annotate.ManyToMany;
import io.spbx.util.reflect.BasicAnnotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BridgeInfo(@Nullable String leftField, @Nullable String rightField) {
    public static @Nullable BridgeInfo fromModelClass(@NotNull Class<?> modelClass) {
        return BasicAnnotations.getOptionalAnnotation(modelClass, ManyToMany.class)
                .map(ann -> new BridgeInfo(Strings.emptyToNull(ann.left()), Strings.emptyToNull(ann.right())))
                .orElse(null);
    }
}
