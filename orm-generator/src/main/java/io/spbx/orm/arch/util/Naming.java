package io.spbx.orm.arch.util;

import com.google.common.base.CaseFormat;
import io.spbx.util.base.BasicStrings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Parameter;

public class Naming {
    public static @NotNull String fieldSqlName(@NotNull JavaField field) {
         return AnnotationsAnalyzer.getSqlName(field).orElseGet(() -> fieldSqlName(field.getName()));
    }

    public static @NotNull String fieldSqlName(@NotNull Parameter param) {
         return AnnotationsAnalyzer.getSqlName(param).orElseGet(() -> fieldSqlName(param.getName()));
    }

    public static @NotNull String fieldSqlName(@NotNull String fieldName) {
        return cleanupSql(camelToSnake(fieldName));
    }

    public static @NotNull String modelSqlName(@NotNull String modelName) {
        return cleanupSql(camelToSnake(modelName));
    }

    public static @NotNull String concatSqlNames(@NotNull String sqlName1, @NotNull String sqlName2) {
        return String.join("_", cleanupSql(sqlName1), cleanupSql(sqlName2));
    }

    private static @NotNull String cleanupSql(@NotNull String name) {
        return name.replaceAll("_+", "_").replaceAll("^_", "").replaceAll("_$", "");
    }

    public static @NotNull String generatedSimpleJavaName(@NotNull Class<?> model) {
        assert !model.isAnonymousClass() : "Invalid model class: %s".formatted(model);
        if (model.isMemberClass()) {
            return shortCanonicalJavaName(model).replace('.', '_');
        }
        return model.getSimpleName();
    }

    public static @NotNull String generatedJavaTableName(@NotNull String modelName) {
        return "%sTable".formatted(modelName);
    }

    public static @NotNull String shortCanonicalJavaName(@NotNull Class<?> type) {
        assert !type.isAnonymousClass() : "Canonical java name not available for: %s".formatted(type);
        if (type.isMemberClass()) {
            String withoutPackage = BasicStrings.removePrefix(type.getCanonicalName(), type.getPackageName());
            return BasicStrings.removePrefix(withoutPackage, ".");
        }
        return type.getSimpleName();
    }

    public static @NotNull String variableJavaName(@NotNull String sqlName) {
        return snakeToCamel(sqlName);
    }

    public static @NotNull String variableJavaName(@NotNull Class<?> type) {
        return camelUpperToLower(type.getSimpleName());
    }

    public static @NotNull String idJavaName(@NotNull String typeName) {
        return camelUpperToLower(typeName) + "Id";
    }

    public static @NotNull String idJavaName(@NotNull Class<?> type) {
        return idJavaName(type.getSimpleName());
    }

    public static @NotNull String defaultAdapterName(@NotNull Class<?> model) {
        assert !model.isAnonymousClass() : "Invalid model class: %s".formatted(model);
        if (model.isMemberClass()) {
            return "%s_JdbcAdapter".formatted(generatedSimpleJavaName(model));
        }
        return "%sJdbcAdapter".formatted(model.getSimpleName());
    }

    public static @NotNull String defaultModelClassName(@NotNull String adapterName) {
        assert adapterName.endsWith("JdbcAdapter") : "Unexpected adapter name: %s".formatted(adapterName);
        return BasicStrings.removeSuffix(adapterName, "JdbcAdapter");
    }

    @VisibleForTesting
    @SuppressWarnings("ConstantConditions")
    static @NotNull String camelToSnake(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(s);
    }

    @VisibleForTesting
    @SuppressWarnings("ConstantConditions")
    static @NotNull String snakeToCamel(@NotNull String s) {
        return CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelUpperToLower(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelLowerToUpper(@NotNull String s) {
        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL).convert(s);
    }
}
