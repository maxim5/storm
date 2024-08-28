package io.spbx.orm.codegen;

import io.spbx.util.io.UncheckedAppendable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

import static io.spbx.orm.codegen.Indent.INDENT1;
import static io.spbx.orm.codegen.JavaSupport.EMPTY_LINE;

@SuppressWarnings("UnusedReturnValue")
public abstract class BaseCodegen {
    protected final UncheckedAppendable writer;

    protected BaseCodegen(@NotNull Appendable writer) {
        this.writer = new UncheckedAppendable(writer);
    }

    private BaseCodegen append(@NotNull String value) {
        writer.append(value);
        return this;
    }

    private BaseCodegen indent(int level) {
        for (int i = 0; i < level; i++) {
            writer.append(INDENT1.spaces());
        }
        return this;
    }

    protected BaseCodegen appendCode(@NotNull String code) {
        return appendCode(1, code, Map.of());
    }

    protected BaseCodegen appendCode(@NotNull String code, @NotNull Map<String, String> context) {
        return appendCode(1, code, context);
    }

    protected BaseCodegen appendCode(int indent, @NotNull String code, @NotNull Map<String, String> context) {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            code = code.replace(entry.getKey(), entry.getValue());
        }
        Stream<String> lines = code.lines().filter(line -> !line.trim().equals(EMPTY_LINE));
        for (String line : (Iterable<String>) lines::iterator) {
            indent(indent).appendLine(line);
        }
        return this;
    }

    protected BaseCodegen appendLine(@NotNull String... values) {
        for (String value : values) {
            writer.append(value);
        }
        writer.append("\n");
        return this;
    }
}
