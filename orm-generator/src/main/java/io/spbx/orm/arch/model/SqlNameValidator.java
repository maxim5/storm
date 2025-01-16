package io.spbx.orm.arch.model;

import io.spbx.util.base.annotate.Stateless;
import io.spbx.util.base.str.Regex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.regex.Pattern;

@Stateless
public class SqlNameValidator {
    public static @NotNull String validateSqlName(@NotNull String name) {
        assert isValidSqlName(name) : "Invalid sql name: " + name;
        return name;
    }

    private static final Pattern SQL_NAME_PATTERN = Pattern.compile("^[a-zA-Z_]\\w*$");

    @VisibleForTesting
    static boolean isValidSqlName(@NotNull String name) {
        return Regex.on(name).matches(SQL_NAME_PATTERN);
    }
}
