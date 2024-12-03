package io.spbx.orm.adapter.std.lang;

import io.spbx.util.base.annotate.Stateless;
import io.spbx.util.func.Reversible;
import io.spbx.util.io.BasicIo;
import org.jetbrains.annotations.NotNull;

@Stateless
public class BytesMapper<T> implements Reversible<T, byte[]> {
    public static <T> @NotNull BytesMapper<T> newInstance() {
        return new BytesMapper<>();
    }

    @Override
    public byte @NotNull[] forward(@NotNull T instance) {
        return BasicIo.serialize(instance);
    }

    @Override
    public @NotNull T backward(byte @NotNull[] bytes) {
        return BasicIo.deserialize(bytes);
    }
}
