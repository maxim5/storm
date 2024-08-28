package io.spbx.orm.arch.factory;

import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.JdbcType;
import io.spbx.orm.arch.model.MultiColumnTableField;
import io.spbx.orm.arch.model.OneColumnTableField;
import io.spbx.orm.arch.model.PojoArch;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import io.spbx.orm.testing.FakeModelAdaptersLocator;
import io.spbx.util.base.Pair;
import io.spbx.util.collect.BasicIterables;
import io.spbx.util.testing.TestingBasics.SimpleBitSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static io.spbx.util.testing.TestingBasics.streamOf;

public class TestingArch {
    public static @NotNull TableArch buildTableArch(@NotNull Class<?> model) {
        return TestingArch.buildTableArch(FakeModelAdaptersLocator.FAKE_LOCATOR, model);
    }

    public static @NotNull TableArch buildTableArch(@NotNull Class<?> model, @NotNull List<Class<?>> rest) {
        return TestingArch.buildTableArch(FakeModelAdaptersLocator.FAKE_LOCATOR, model, rest);
    }

    public static @NotNull TableArch buildTableArch(@NotNull FakeModelAdaptersLocator fakeLocator, @NotNull Class<?> model) {
        RunInputs inputs = TestingArch.newRunInputs(model);
        RunResult runResult = new ArchFactory(fakeLocator).build(inputs);
        return runResult.getTableOrDie(model);
    }

    public static @NotNull TableArch buildTableArch(@NotNull FakeModelAdaptersLocator fakeLocator,
                                                    @NotNull Class<?> model,
                                                    @NotNull List<Class<?>> rest) {
        RunInputs inputs = TestingArch.newRunInputs(BasicIterables.appendToList(rest, model));
        RunResult runResult = new ArchFactory(fakeLocator).build(inputs);
        return runResult.getTableOrDie(model);
    }

    static @NotNull RunContext newRunContext() {
        return TestingArch.newRunContext(FakeModelAdaptersLocator.FAKE_LOCATOR);
    }

    static @NotNull RunContext newRunContext(@NotNull FakeModelAdaptersLocator fakeLocator) {
        return new RunContext(newRunInputs(), fakeLocator);
    }

    public static @NotNull RunInputs newRunInputs(@NotNull Class<?> @NotNull ... models) {
        List<ModelInput> modelInputs = streamOf(models).map(ModelInput::of).toList();
        return new RunInputs(modelInputs, List.of());
    }

    public static @NotNull RunInputs newRunInputs(@NotNull Collection<Class<?>> models) {
        List<ModelInput> modelInputs = models.stream().map(ModelInput::of).toList();
        return new RunInputs(modelInputs, List.of());
    }

    @CheckReturnValue
    public static @NotNull TableArchSubject assertThat(@NotNull TableArch tableArch) {
        return new TableArchSubject(tableArch);
    }

    @CheckReturnValue
    public static @NotNull TableFieldSubject assertThat(@NotNull TableField tableField) {
        return new TableFieldSubject(tableField);
    }

    @CheckReturnValue
    public static @NotNull PojoArchSubject assertThat(@NotNull PojoArch pojoArch) {
        return new PojoArchSubject(pojoArch);
    }

    @CanIgnoreReturnValue
    public record TableArchSubject(@NotNull TableArch tableArch) {
        public @NotNull TableArchSubject hasTableName(@NotNull String sqlName,
                                                      @NotNull String packageName,
                                                      @NotNull String javaName) {
            Truth.assertThat(tableArch.sqlName()).isEqualTo(sqlName);
            Truth.assertThat(tableArch.packageName()).isEqualTo(packageName);
            Truth.assertThat(tableArch.javaName()).isEqualTo(javaName);
            return this;
        }

        public @NotNull TableArchSubject hasModel(@NotNull String modelName, @NotNull Class<?> modelClass) {
            Truth.assertThat(tableArch.modelName()).isEqualTo(modelName);
            Truth.assertThat(tableArch.modelClass()).isEqualTo(modelClass);
            return this;
        }

        public @NotNull TableArchSubject hasFields(@NotNull TableFieldsStatus status) {
            Truth.assertThat(tableArch.hasPrimaryKeyField()).isEqualTo(status.hasPrimaryField());
            Truth.assertThat(tableArch.isPrimaryKeyInt()).isEqualTo(status.hasPrimaryIntField());
            Truth.assertThat(tableArch.isPrimaryKeyLong()).isEqualTo(status.hasPrimaryLongField());
            Truth.assertThat(tableArch.hasForeignKeyField()).isEqualTo(status.hasForeignField());
            return this;
        }

        public @NotNull TableFieldSubject hasSingleFieldThat(@NotNull String name) {
            Truth.assertThat(tableArch.fields()).hasSize(1);
            return hasFieldThat(name);
        }

        public @NotNull TableFieldSubject hasFieldThat(@NotNull String name) {
            TableField tableField = tableArch.fields().stream()
                .filter(field -> field.javaName().equals(name))
                .findAny()
                .orElseThrow();
            return assertThat(tableField);
        }
    }

    public static class TableFieldsStatus extends SimpleBitSet<TableFieldsStatus> {
        public static final TableFieldsStatus ONLY_ORDINARY = new TableFieldsStatus();
        public static final TableFieldsStatus PRIMARY_OBJ = new TableFieldsStatus().withPrimaryObj();
        public static final TableFieldsStatus PRIMARY_INT = new TableFieldsStatus().withPrimaryInt();
        public static final TableFieldsStatus PRIMARY_LONG = new TableFieldsStatus().withPrimaryLong();
        public static final TableFieldsStatus FOREIGN = new TableFieldsStatus().withForeign();

        public @NotNull TableFieldsStatus withPrimaryObj() { return setBit(0); }
        public @NotNull TableFieldsStatus withPrimaryInt() { return setBit(0).setBit(1); }
        public @NotNull TableFieldsStatus withPrimaryLong() { return setBit(0).setBit(2); }
        public @NotNull TableFieldsStatus withForeign() { return setBit(3); }

        public boolean hasPrimaryField() { return bitSet.get(0); }
        public boolean hasPrimaryIntField() { return bitSet.get(1); }
        public boolean hasPrimaryLongField() { return bitSet.get(2); }
        public boolean hasForeignField() { return bitSet.get(3); }
    }

    @CanIgnoreReturnValue
    public record TableFieldSubject(@NotNull TableField tableField) {
        public @NotNull TableFieldSubject isFromTable(@NotNull String table) {
            Truth.assertThat(tableField.parent().sqlName()).isEqualTo(table);
            return this;
        }

        public @NotNull TableFieldSubject hasInJava(@NotNull Class<?> javaType, @NotNull String accessor) {
            Truth.assertThat(tableField.javaType()).isEqualTo(javaType);
            Truth.assertThat(tableField.javaAccessor()).isEqualTo(accessor);
            return this;
        }

        public @NotNull TableFieldSubject hasSize(int size) {
            Truth.assertThat(tableField.columns().size()).isEqualTo(size);
            Truth.assertThat(tableField.columnsNumber()).isEqualTo(size);
            Truth.assertThat(tableField.isSingleColumn()).isEqualTo(size == 1);
            Truth.assertThat(tableField.isMultiColumn()).isEqualTo(size > 1);
            Truth.assertThat(tableField instanceof MultiColumnTableField).isEqualTo(size > 1);
            Truth.assertThat(tableField instanceof OneColumnTableField || tableField instanceof ForeignTableField)
                .isEqualTo(size == 1);
            return this;
        }

        public @NotNull TableFieldSubject hasColumns(@NotNull String @NotNull ... columns) {
            List<String> names = tableField.columns().stream().map(Column::sqlName).toList();
            Truth.assertThat(names).containsExactly((Object[]) columns).inOrder();
            return hasSize(columns.length);
        }

        public @NotNull TableFieldSubject hasColumnTypes(@NotNull JdbcType @NotNull ... types) {
            List<JdbcType> jdbcTypes = tableField.columns().stream().map(Column::jdbcType).toList();
            Truth.assertThat(jdbcTypes).containsExactly((Object[]) types).inOrder();
            return hasSize(types.length);
        }

        public final @SafeVarargs @NotNull TableFieldSubject hasColumns(@NotNull Pair<String, JdbcType> @NotNull ... columns) {
            List<Pair<String, JdbcType>> typedColumns = tableField.columns().stream()
                .map(column -> Pair.of(column.sqlName(), column.jdbcType()))
                .toList();
            Truth.assertThat(typedColumns).containsExactly((Object[]) columns).inOrder();
            return hasSize(columns.length);
        }

        public @NotNull TableFieldSubject isSingleColumn(@NotNull String column, @NotNull JdbcType type) {
            return hasColumns(Pair.of(column, type));
        }

        public @NotNull TableFieldSubject hasConstraints(@NotNull FieldConstraints constraints) {
            Truth.assertThat(tableField.isPrimaryKey()).isEqualTo(constraints.isPrimaryKey());
            Truth.assertThat(tableField.isUnique()).isEqualTo(constraints.isUnique());
            Truth.assertThat(tableField.isForeignKey()).isEqualTo(constraints.isForeignKey());
            Truth.assertThat(tableField.isNullable()).isEqualTo(constraints.isNullable());
            Truth.assertThat(tableField instanceof ForeignTableField).isEqualTo(tableField.isForeignKey());
            return this;
        }

        public @NotNull TableFieldSubject isForeign(@NotNull String column, @NotNull String table) {
            Truth.assertThat(tableField.isForeignKey()).isTrue();
            Truth.assertThat(((ForeignTableField) tableField).foreignKeyColumn().sqlName()).isEqualTo(column);
            Truth.assertThat(((ForeignTableField) tableField).getForeignTable()).isNotNull();
            Truth.assertThat(((ForeignTableField) tableField).getForeignTable().sqlName()).isEqualTo(table);
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.FOREIGN_KEY);
            return this;
        }

        public @NotNull TableFieldSubject hasDefault(@NotNull String column, @Nullable String value) {
            Column col = tableField.columns().stream().filter(c -> c.sqlName().equals(column)).collect(onlyElement());
            Truth.assertThat(tableField.columnDefault(col)).isEqualTo(value);
            return this;
        }

        public @NotNull TableFieldSubject doesNotHaveAnyDefaults() {
            tableField.columns().forEach(column -> Truth.assertThat(tableField.columnDefault(column)).isNull());
            return this;
        }

        public @NotNull TableFieldSubject hasDefault(@Nullable String value) {
            hasSize(1);
            Truth.assertThat(tableField.columnDefault(tableField.columns().getFirst())).isEqualTo(value);
            return this;
        }

        public @NotNull TableFieldSubject doesNotHaveDefault() {
            return hasDefault(null);
        }

        public @NotNull TableFieldSubject isNativelySupportedType() {
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.NATIVE);
            Truth.assertThat(tableField.isNativelySupportedType()).isTrue();
            Truth.assertThat(tableField.isMapperSupportedType()).isFalse();
            Truth.assertThat(tableField.isAdapterSupportType()).isFalse();
            return this;
        }

        public @NotNull TableFieldSubject isAdapterSupportedType() {
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.ADAPTER_API);
            Truth.assertThat(tableField.isAdapterSupportType()).isTrue();
            Truth.assertThat(tableField.isNativelySupportedType()).isFalse();
            Truth.assertThat(tableField.isMapperSupportedType()).isFalse();
            return this;
        }

        public @NotNull TableFieldSubject isMapperSupportedType() {
            Truth.assertThat(tableField.typeSupport()).isEqualTo(TableField.TypeSupport.MAPPER_API);
            Truth.assertThat(tableField.isAdapterSupportType()).isFalse();
            Truth.assertThat(tableField.isNativelySupportedType()).isFalse();
            Truth.assertThat(tableField.isMapperSupportedType()).isTrue();
            return this;
        }

        public @NotNull TableFieldSubject usesAdapter(@NotNull String staticRef) {
            Truth.assertThat(tableField.adapterApiOrDie().staticRef()).isEqualTo(staticRef);
            return this;
        }
    }

    public static class FieldConstraints extends SimpleBitSet<FieldConstraints> {
        public static final FieldConstraints ORDINARY = new FieldConstraints();
        public static final FieldConstraints PRIMARY_KEY = new FieldConstraints().primaryKey();
        public static final FieldConstraints FOREIGN_KEY = new FieldConstraints().foreignKey();
        public static final FieldConstraints UNIQUE = new FieldConstraints().unique();

        public @NotNull FieldConstraints primaryKey() { return setBit(0); }
        public @NotNull FieldConstraints foreignKey() { return setBit(1); }
        public @NotNull FieldConstraints unique() { return setBit(2); }
        public @NotNull FieldConstraints nullable() { return setBit(3); }
        public @NotNull FieldConstraints nonnull() { return unsetBit(3); }

        public boolean isPrimaryKey() { return bitSet.get(0); }
        public boolean isForeignKey() { return bitSet.get(1); }
        public boolean isUnique() { return bitSet.get(2); }
        public boolean isNullable() { return bitSet.get(3); }
    }

    @CanIgnoreReturnValue
    public record PojoArchSubject(@NotNull PojoArch pojoArch) {
        public @NotNull PojoArchSubject hasAdapterName(@NotNull String name) {
            Truth.assertThat(pojoArch.adapterName()).isEqualTo(name);
            return this;
        }

        public @NotNull PojoArchSubject hasColumns(@NotNull Column @NotNull... columns) {
            Truth.assertThat(pojoArch.columns()).containsExactly((Object[]) columns).inOrder();
            return this;
        }
    }
}
