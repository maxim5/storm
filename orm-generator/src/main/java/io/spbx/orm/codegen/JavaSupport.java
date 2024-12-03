package io.spbx.orm.codegen;

import io.spbx.util.base.annotate.Stateless;
import io.spbx.util.extern.guava.SourceCodeEscapers;
import org.jetbrains.annotations.NotNull;

@Stateless
class JavaSupport {
    public static final String EMPTY_LINE = "/* EMPTY */";

    public static @NotNull Snippet wrapAsStringLiteral(@NotNull Snippet snippet) {
        if (snippet.isMultiline()) {
            return wrapAsTextBlock(snippet);
        } else {
            return wrapAsSingleLineLiteral(snippet);
        }
    }

    public static @NotNull String wrapAsStringLiteral(@NotNull String snippet) {
        return wrapAsStringLiteral(new Snippet().appendMultiline(snippet)).joinLines();
    }

    private static @NotNull Snippet wrapAsSingleLineLiteral(@NotNull Snippet snippet) {
        return new Snippet().appendFormattedLine("\"%s\"", snippet.transform(JavaSupport::escapeJavaStringLiteral).joinLines());
    }

    private static @NotNull Snippet wrapAsTextBlock(@NotNull Snippet snippet) {
        return new Snippet()
            .appendLine("\"\"\"")
            .appendLines(snippet.transform(JavaSupport::escapeJavaStringLiteral))
            .appendLine("\"\"\"");
    }

    private static @NotNull String escapeJavaStringLiteral(@NotNull String str) {
        return SourceCodeEscapers.javaCharEscaper().escape(str);
    }
}
