package io.spbx.orm.arch.model;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.api.ReadFollow;
import io.spbx.util.base.tuple.Pair;
import io.spbx.util.lazy.AtomicCacheCompute;
import io.spbx.util.lazy.AtomicLazyInit;
import io.spbx.util.lazy.CacheCompute;
import io.spbx.util.lazy.LazyInit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.spbx.orm.api.ReadFollow.FOLLOW_ALL;
import static io.spbx.orm.api.ReadFollow.FOLLOW_ONE_LEVEL;
import static io.spbx.orm.arch.InvalidSqlModelException.newInvalidSqlModelException;
import static io.spbx.orm.arch.model.SqlNameValidator.validateSqlName;
import static io.spbx.util.base.error.BasicExceptions.newInternalError;
import static io.spbx.util.code.jvm.JavaNameValidator.validateJavaIdentifier;
import static io.spbx.util.code.jvm.JavaNameValidator.validateJavaPackage;

@Immutable
public final class TableArch implements JavaNameHolder, HasColumns, HasPrefixedColumns {
    private final LazyInit<ImmutableList<TableField>> fieldsRef = AtomicLazyInit.createUninitialized();
    private final CacheCompute<Optional<TableField>> primaryKeyCache = AtomicCacheCompute.createEmpty();

    private final String sqlName;
    private final String javaName;
    private final Class<?> modelClass;
    private final String modelName;
    private final BridgeInfo bridgeInfo;

    public TableArch(@NotNull String sqlName,
                     @NotNull String javaName,
                     @NotNull Class<?> modelClass,
                     @NotNull String modelName,
                     @Nullable BridgeInfo bridgeInfo) {
        this.sqlName = validateSqlName(sqlName);
        this.javaName = validateJavaIdentifier(javaName);
        this.modelClass = modelClass;
        this.modelName = validateJavaIdentifier(modelName);
        this.bridgeInfo = bridgeInfo;
    }

    public @NotNull String sqlName() {
        return sqlName;
    }

    public @NotNull String javaName() {
        return javaName;
    }

    @Override
    public @NotNull String packageName() {
        return validateJavaPackage(modelClass.getPackageName());
    }

    public @NotNull Class<?> modelClass() {
        return modelClass;
    }

    public @NotNull String modelName() {
        return modelName;
    }

    public @NotNull ImmutableList<TableField> fields() {
        return fieldsRef.getOrDie();
    }

    public boolean hasPrimaryKeyField() {
        return primaryKeyField() != null;
    }

    public @Nullable TableField primaryKeyField() {
        return primaryKeyCache.getOrCompute(() -> fields().stream().filter(TableField::isPrimaryKey).findFirst()).orElse(null);
    }

    public boolean isPrimaryKeyInt() {
        TableField field = primaryKeyField();
        return field != null && field.javaType() == int.class;
    }

    public boolean isPrimaryKeyLong() {
        TableField field = primaryKeyField();
        return field != null && field.javaType() == long.class;
    }

    public boolean hasForeignKeyField() {
        return fields().stream().anyMatch(TableField::isForeignKey);
    }

    public @NotNull List<ForeignTableField> foreignFields(@NotNull ReadFollow follow) {
        return switch (follow) {
            case NO_FOLLOW -> List.of();
            case FOLLOW_ONE_LEVEL -> fields().stream()
                .filter(TableField::isForeignKey)
                .map(field -> (ForeignTableField) field).toList();
            case FOLLOW_ALL -> foreignFields(FOLLOW_ONE_LEVEL).stream()
                .flatMap(field -> Stream.concat(Stream.of(field),
                                                field.getForeignTable().foreignFields(FOLLOW_ALL).stream()))
                .toList();
        };
    }

    @Override
    public @NotNull List<Column> columns() {
        return columns(tableField -> true);
    }

    public @NotNull List<Column> columns(@NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).map(HasColumns::columns).flatMap(List::stream).toList();
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        return columns(follow, tableField -> true);
    }

    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow, @NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).flatMap(field -> field.columns(follow).stream()).toList();
    }

    public @NotNull List<Pair<TableField, Column>> columnsWithFields() {
        return fields().stream().flatMap(field -> field.columns().stream().map(column -> Pair.of(field, column))).toList();
    }

    public boolean isBridgeTable() {
        return bridgeInfo != null;
    }

    public @NotNull Optional<ForeignTableField> leftBridgeField() {
        return optionally(isBridgeTable(), this::leftBridgeFieldOrDie);
    }

    public @NotNull Optional<ForeignTableField> rightBridgeField() {
        return optionally(isBridgeTable(), this::rightBridgeFieldOrDie);
    }

    public @NotNull ForeignTableField leftBridgeFieldOrDie() {
        assert bridgeInfo != null : newInternalError("Many-to-many not available for table:", this);
        String leftName = bridgeInfo.leftField();
        Stream<ForeignTableField> stream = foreignFields(FOLLOW_ONE_LEVEL).stream();
        if (leftName != null) {
            return stream.filter(field -> field.javaName().equals(leftName)).findFirst().orElseThrow(() ->
                newInvalidSqlModelException(
                    "Many-to-many left foreign-key field `%s` not found in model `%s`", leftName, describeModel())
            );
        } else {
            return stream.findFirst().orElseThrow(() ->
                newInvalidSqlModelException("Many-to-many right foreign-key field not found in model `%s`", describeModel())
            );
        }
    }

    public @NotNull ForeignTableField rightBridgeFieldOrDie() {
        assert bridgeInfo != null : newInternalError("Many-to-many not available for table:", this);
        String rightName = bridgeInfo.rightField();
        Stream<ForeignTableField> stream = foreignFields(FOLLOW_ONE_LEVEL).stream();
        if (rightName != null) {
            return stream.filter(field -> field.javaName().equals(rightName)).findFirst().orElseThrow(() ->
                newInvalidSqlModelException(
                    "Many-to-many right foreign-key field `%s` not found in model `%s`", rightName, describeModel())
            );
        } else {
            return stream.skip(1).findFirst().orElseThrow(() ->
                newInvalidSqlModelException("Many-to-many right foreign-key field not found in model `%s`", describeModel())
            );
        }
    }

    private @NotNull String describeModel() {
        return modelClass.getSimpleName();
    }

    public boolean isInitialized() {
        return fieldsRef.isInitialized();
    }

    public void initializeOrDie(@NotNull ImmutableList<TableField> fields) {
        fieldsRef.initializeOrDie(fields);
    }

    public void validate() {
        if (isBridgeTable()) {
            leftBridgeFieldOrDie();
            rightBridgeFieldOrDie();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TableArch that &&
               Objects.equals(sqlName, that.sqlName) && Objects.equals(javaName, that.javaName) &&
               Objects.equals(modelClass, that.modelClass) && Objects.equals(modelName, that.modelName) &&
               Objects.equals(bridgeInfo, that.bridgeInfo) &&
               Objects.equals(fieldsRef, that.fieldsRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlName, javaName, modelClass, modelName, bridgeInfo, fieldsRef);
    }

    @Override
    public String toString() {
        return "TableArch[sqlName=%s, javaName=%s, modelClass=%s, modelName=%s, bridgeInfo=%s]"
                .formatted(sqlName, javaName, modelClass, modelName, bridgeInfo);
    }

    private static <T> Optional<T> optionally(boolean cond, @NotNull Supplier<T> supplier) {
        return cond ? Optional.of(supplier.get()) : Optional.empty();
    }
}
