package io.spbx.orm.codegen;

import io.spbx.orm.api.Foreign;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.api.ReadFollow;
import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.OneColumnTableField;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import io.spbx.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Stream;

import static io.spbx.orm.api.ReadFollow.*;
import static io.spbx.orm.codegen.Indent.INDENT1;
import static io.spbx.orm.codegen.Joining.COMMA_JOINER;

class ResultSetConversionMaker {
    private static final Set<Class<?>> FULL_NAME_CLASSES = Set.of(java.util.Date.class, java.sql.Date.class);

    private final String resultSetParam;
    private final String followParam;
    private final String indexParam;

    public ResultSetConversionMaker(@NotNull String resultSetParam, @NotNull String followParam, @NotNull String indexParam) {
        this.resultSetParam = resultSetParam;
        this.followParam = followParam;
        this.indexParam = indexParam;
    }

    public @NotNull Snippet make(@NotNull TableArch table) {
        return new Snippet().appendMultilines(table.fields().stream().map(this::assignFieldLine));
    }

    private @NotNull String assignFieldLine(@NotNull TableField field) {
        Class<?> fieldType = field.javaType();
        String fieldClassName = FULL_NAME_CLASSES.contains(fieldType) ?
            fieldType.getName() :
            Naming.shortCanonicalJavaName(fieldType);
        return "%s %s = %s;".formatted(fieldClassName, field.javaName(), fieldCreateExpr(field));
    }

    private @NotNull String fieldCreateExpr(@NotNull TableField field) {
        return switch (field.typeSupport()) {
            case NATIVE -> {
                assert field instanceof OneColumnTableField : "Native field is not one column: %s".formatted(field);
                yield resultSetGetterExpr(((OneColumnTableField) field).column());
            }
            case FOREIGN_KEY -> {
                assert field instanceof ForeignTableField : "Expected a foreign key field, but found %s".formatted(field);
                yield foreignFieldSwitchExpr((ForeignTableField) field);
            }
            case MAPPER_API -> {
                assert field instanceof OneColumnTableField : "Mapped field is not one column: %s".formatted(field);
                String resultSetGetterExpr = resultSetGetterExpr(((OneColumnTableField) field).column());
                yield field.mapperApiOrDie().expr().jdbcToField(resultSetGetterExpr);
            }
            case ADAPTER_API -> {
                String params = field.columns().stream().map(this::resultSetGetterExpr).collect(COMMA_JOINER);
                yield field.adapterApiOrDie().expr().createInstance(params);
            }
        };
    }

    private @NotNull String foreignFieldSwitchExpr(@NotNull ForeignTableField field) {
        Stream<String> indented = Stream.of(ReadFollow.values())
            .map(follow -> field.isNotNull() ? caseForNotNull(field, follow) : caseForNullable(field, follow))
            .flatMap(String::lines)
            .map(line -> INDENT1.spaces() + line);
        return new Snippet()
            .appendFormattedLine("switch (%s) {", followParam)
            .appendLines(indented)
            .appendLine("}")
            .joinLines(INDENT1);
    }

    private @NotNull String caseForNotNull(@NotNull ForeignTableField field, @NotNull ReadFollow follow) {
        String fkParam = resultSetGetterExpr(field.foreignKeyColumn());
        String params;
        String factoryMethod;
        if (follow == NO_FOLLOW) {
            params = fkParam;
            factoryMethod = "ofId";
        } else {
            int columnsNum = field.columnsNumber(follow) - 1;  // exclude FK columns
            String fromRow = fromRowExpr(field, follow == FOLLOW_ONE_LEVEL ? NO_FOLLOW : FOLLOW_ALL, columnsNum);
            params = "%s, %s".formatted(fkParam, fromRow);
            factoryMethod = "ofEntity";
        }
        Class<?> factoryClass = field.javaType() == Foreign.class ? ForeignObj.class : field.javaType();
        return "case %s -> %s.%s(%s);".formatted(follow, factoryClass.getSimpleName(), factoryMethod, params);
    }

    private @NotNull String caseForNullable(@NotNull ForeignTableField field, @NotNull ReadFollow follow) {
        Class<?> underlyingType = field.primaryKeyFieldInForeignTable().javaType();
        boolean isPrimitive = underlyingType.isPrimitive();
        if (follow == NO_FOLLOW && isPrimitive) {
            return caseForNotNull(field, follow);
        }

        int columnsNum = field.columnsNumber(follow) - 1;  // exclude FK columns
        String tmpVar = "_%s".formatted(field.javaName());
        String nullValue = isPrimitive ? "0" : "null";
        Class<?> factoryClass = field.javaType() == Foreign.class ? ForeignObj.class : field.javaType();

        String increment;
        String params;
        String factoryMethod;
        if (follow == NO_FOLLOW) {
            increment = "/* no need to increment `%s` */".formatted(indexParam);
            factoryMethod = "ofId";
            params = tmpVar;
        } else {
            increment = "%s += %s".formatted(indexParam, columnsNum);
            factoryMethod = "ofEntity";
            String fromRow = fromRowExpr(field, follow == FOLLOW_ONE_LEVEL ? NO_FOLLOW : FOLLOW_ALL, columnsNum);
            params = "%s, %s".formatted(tmpVar, fromRow);
        }

        return """
            case %s -> {
                %s %s = %s;
                if (%s == %s) {
                    %s;
                    yield %s.empty();
                } else {
                    yield %s.%s(%s);
                }
            }
            """.formatted(
            follow,
            Naming.shortCanonicalJavaName(underlyingType), tmpVar, resultSetGetterExpr(field.foreignKeyColumn()),
            tmpVar, nullValue,
            increment,
            factoryClass.getSimpleName(),
            factoryClass.getSimpleName(), factoryMethod, params
        );
    }

    // Example: ForeignTable.fromRow(result, ReadFollow.NO_FOLLOW, (start += 2) - 2)
    private @NotNull String fromRowExpr(@NotNull ForeignTableField field, @NotNull ReadFollow follow, int columnsNum) {
        return "%s.fromRow(%s, %s.%s, (%s += %d) - %d)".formatted(
            field.getForeignTable().javaName(),
            resultSetParam,
            ReadFollow.class.getSimpleName(), follow,
            indexParam, columnsNum, columnsNum
        );
    }

    // Example: result.getLong(++start)
    private @NotNull String resultSetGetterExpr(@NotNull Column column) {
        String resultSetGetter = column.jdbcType().getterMethod();
        return "%s.%s(++%s)".formatted(resultSetParam, resultSetGetter, indexParam);
    }
}
