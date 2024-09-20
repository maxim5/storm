package io.spbx.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.spbx.orm.arch.factory.FieldResolver.ResolveResult;
import io.spbx.orm.arch.model.AdapterApi;
import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.Defaults;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.MapperApi;
import io.spbx.orm.arch.model.ModelField;
import io.spbx.orm.arch.model.MultiColumnTableField;
import io.spbx.orm.arch.model.OneColumnTableField;
import io.spbx.orm.arch.model.PojoArch;
import io.spbx.orm.arch.model.PojoParent;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import io.spbx.orm.arch.util.AnnotationsAnalyzer;
import io.spbx.orm.arch.util.JavaClassAnalyzer;
import io.spbx.orm.arch.util.JavaField;
import io.spbx.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNull;

class TableFieldArchFactory {
    private final TableArch table;
    private final JavaField field;

    private final FieldResolver fieldResolver;
    private final RecursivePojoArchFactory pojoArchFactory;

    public TableFieldArchFactory(@NotNull RunContext runContext,
                                 @NotNull TableArch table,
                                 @NotNull JavaField field) {
        this.table = table;
        this.field = field;
        this.fieldResolver = new FieldResolver(runContext);
        this.pojoArchFactory = new RecursivePojoArchFactory(runContext);
    }

    public @NotNull TableField buildTableField() {
        ModelField modelField = JavaClassAnalyzer.toModelField(field);
        boolean isPrimaryKey = AnnotationsAnalyzer.isPrimaryKeyField(field);
        boolean isUnique = AnnotationsAnalyzer.isUniqueField(field);
        boolean isNullable = AnnotationsAnalyzer.isNullableField(field);
        String[] defaults = AnnotationsAnalyzer.getDefaults(field);

        FieldInference inference = inferFieldArch();
        if (inference.isForeignTable()) {
            return new ForeignTableField(table,
                                         modelField,
                                         isNullable,
                                         Defaults.ofOneColumn(defaults),
                                         requireNonNull(inference.foreignTable()),
                                         requireNonNull(inference.singleColumn()));
        } else if (inference.isSingleColumn()) {
            return new OneColumnTableField(table,
                                           modelField,
                                           isPrimaryKey,
                                           isUnique,
                                           isNullable,
                                           Defaults.ofOneColumn(defaults),
                                           inference.mapperApi(),
                                           inference.adapterApi(),
                                           requireNonNull(inference.singleColumn()));
        } else {
            return new MultiColumnTableField(table,
                                             modelField,
                                             isPrimaryKey,
                                             isUnique,
                                             isNullable,
                                             Defaults.ofMultiColumns(inference.columnsNumber(), defaults),
                                             requireNonNull(inference.adapterApi()),
                                             requireNonNull(inference.multiColumns()));
        }
    }

    private @NotNull FieldInference inferFieldArch() {
        String fieldSqlName = Naming.fieldSqlName(field);

        ResolveResult resolved = fieldResolver.resolve(field);
        return switch (resolved.type()) {
            case NATIVE -> {
                Column column = Column.of(fieldSqlName, resolved.jdbcType());
                yield FieldInference.ofNativeColumn(column);
            }
            case FOREIGN_KEY -> {
                String foreignIdSqlName = AnnotationsAnalyzer.getSqlName(field)
                    .orElseGet(() -> Naming.concatSqlNames(fieldSqlName, "id"));
                Column column = Column.of(foreignIdSqlName, resolved.foreignTable().second());
                yield FieldInference.ofForeignKey(column, resolved.foreignTable().first());
            }
            case HAS_MAPPER -> {
                boolean nullable = AnnotationsAnalyzer.isNullableField(field);
                MapperApi mapperApi = MapperApi.ofExistingMapper(resolved.mapperClass(), field.getGenericType(), nullable);
                Column column = mapperApi.mapperColumn(fieldSqlName);
                yield FieldInference.ofSingleColumn(column, mapperApi);
            }
            case INLINE_MAPPER -> {
                Column column = resolved.mapperApi().mapperColumn(fieldSqlName);
                yield FieldInference.ofSingleColumn(column, resolved.mapperApi());
            }
            case HAS_ADAPTER -> {
                AdapterApi adapterApi = AdapterApi.ofClass(resolved.adapterClass());
                List<Column> columns = adapterApi.adapterColumns(fieldSqlName);
                yield FieldInference.ofColumns(columns, adapterApi);
            }
            case POJO -> {
                PojoArch pojo = pojoArchFactory.buildPojoArchFor(field)
                    .reattachedTo(PojoParent.ofTerminal(field.getName(), fieldSqlName));
                yield FieldInference.ofColumns(pojo.columns(), AdapterApi.ofSignature(pojo));
            }
        };
    }

    private record FieldInference(@Nullable Column singleColumn,
                                  @Nullable ImmutableList<Column> multiColumns,
                                  @Nullable MapperApi mapperApi,
                                  @Nullable AdapterApi adapterApi,
                                  @Nullable TableArch foreignTable) {
        public static FieldInference ofNativeColumn(@NotNull Column column) {
            return new FieldInference(column, null, null, null, null);
        }

        public static FieldInference ofColumns(@NotNull List<Column> columns, @NotNull AdapterApi adapterApi) {
            return columns.size() == 1 ? ofSingleColumn(columns.getFirst(), adapterApi) : ofMultiColumns(columns, adapterApi);
        }

        public static FieldInference ofSingleColumn(@NotNull Column column, @NotNull MapperApi mapperApi) {
            return new FieldInference(column, null, mapperApi, null, null);
        }

        public static FieldInference ofSingleColumn(@NotNull Column column, @NotNull AdapterApi adapterApi) {
            return new FieldInference(column, null, null, adapterApi, null);
        }

        public static FieldInference ofMultiColumns(@NotNull List<Column> columns, @NotNull AdapterApi adapterApi) {
            return new FieldInference(null, ImmutableList.copyOf(columns), null, adapterApi, null);
        }

        public static FieldInference ofForeignKey(@NotNull Column column, @NotNull TableArch foreignTable) {
            return new FieldInference(column, null, null, null, foreignTable);
        }

        public boolean isForeignTable() {
            return foreignTable != null;
        }

        public boolean isSingleColumn() {
            return singleColumn != null;
        }

        public int columnsNumber() {
            return multiColumns != null ? multiColumns.size() : 1;
        }
    }
}
