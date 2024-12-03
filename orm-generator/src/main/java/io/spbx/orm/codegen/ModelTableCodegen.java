package io.spbx.orm.codegen;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.ReadFollow;
import io.spbx.orm.api.TableInt;
import io.spbx.orm.api.TableLong;
import io.spbx.orm.api.TableObj;
import io.spbx.orm.arch.model.Column;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.HasColumns;
import io.spbx.orm.arch.model.JdbcType;
import io.spbx.orm.arch.model.OneColumnTableField;
import io.spbx.orm.arch.model.PrefixedColumn;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import io.spbx.orm.arch.util.Naming;
import io.spbx.util.base.error.BasicExceptions.IllegalArgumentExceptions;
import io.spbx.util.collect.map.BasicMaps;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.spbx.orm.codegen.Indent.*;
import static io.spbx.orm.codegen.JavaSupport.EMPTY_LINE;
import static io.spbx.orm.codegen.JavaSupport.wrapAsStringLiteral;
import static io.spbx.orm.codegen.Joining.*;
import static io.spbx.util.collect.map.BasicMaps.mergeToMap;
import static io.spbx.util.collect.map.BasicMaps.orderedMapOf;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnnecessaryStringEscape")
public class ModelTableCodegen extends BaseCodegen {
    private final ModelAdaptersLocator locator;
    private final TableArch table;

    private final Map<String, String> mainContext;
    private final Map<String, String> pkContext;

    public ModelTableCodegen(@NotNull ModelAdaptersLocator locator, @NotNull TableArch table, @NotNull Appendable writer) {
        super(writer);
        this.locator = locator;
        this.table = table;

        this.mainContext = orderedMapOf(
            "$TableClass", table.javaName(),
            "$table_sql", table.sqlName(),
            "$ModelClass", Naming.shortCanonicalJavaName(table.modelClass()),
            "$model_param", Naming.variableJavaName(table.modelClass())
        );

        TableField primaryKeyField = table.primaryKeyField();
        this.pkContext = primaryKeyField == null ? Map.of() : orderedMapOf(
            "$pk_type", Naming.shortCanonicalJavaName(primaryKeyField.javaType()),
            "$PkClass", primaryKeyField.javaType().isPrimitive() ?
                Primitives.wrap(primaryKeyField.javaType()).getSimpleName() :
                Naming.shortCanonicalJavaName(primaryKeyField.javaType()),
            "$pk_annotation", primaryKeyField.javaType().isPrimitive() ? "" : "@Nonnull ",
            "$pk_name", primaryKeyField.javaName()
        );
    }

    public void generateJava() {
        imports();

        classDef();
        constructors();
        withFollowOnRead();

        admin();
        getters();
        count();
        exists();

        selectConstants();

        existsByPk();
        getByPk();
        getBatchByPk();
        fetchPks();
        keyOf();
        iterator();

        insert();
        insertIgnore();
        valuesForInsert();
        insertAutoIncPk();
        valuesForInsertAutoIncPk();

        updateByPk();
        valuesForUpdateByPk();
        updateWhere();
        valuesForUpdateWhere();

        insertData();
        updateDataWhere();

        insertBatch();
        updateWhereBatch();

        insertDataBatch();
        updateDataWhereBatch();

        deleteByPk();
        deleteWhere();

        fromRow();

        bridge();
        bridgeNative();

        internalMeta();
        columnsEnum();
        tableMeta();
        dataFactoryMethods();

        appendLine("}");
    }

    private void imports() {
        List<String> classesToImport = new ImportsCollector(locator, table, pickBaseTableClass()).imports();

        Map<String, String> context = orderedMapOf(
            "$package", table.packageName(),
            "$imports", classesToImport.stream().map("import %s;"::formatted).collect(LINE_JOINER)
        );

        appendCode(0, """
        package $package;

        import java.sql.*;
        import java.util.*;
        import java.util.function.*;
        import java.util.stream.*;
        import javax.annotation.*;

        import com.carrotsearch.hppc.*;
        
        $imports\n
        """, context);
    }

    private void classDef() {
        Class<?> baseTableClass = pickBaseTableClass();
        Map<String, String> context = orderedMapOf(
            "$BaseClass", Naming.shortCanonicalJavaName(baseTableClass),
            "$BaseGenerics", baseTableClass == TableObj.class ? "$pk_type, $ModelClass" : "$ModelClass"
        );

        if (table.isBridgeTable()) {
            appendCode(0, """
            public class $TableClass implements $BaseClass<$BaseGenerics>, \
            BridgeTable<$left_id_wrap, $left_entity, $right_id_wrap, $right_entity> {
            """, mergeToMap(mergeToMap(context, bridgeContext()), mainContext, pkContext));
        } else {
            appendCode(0, "public class $TableClass implements $BaseClass<$BaseGenerics> {",
                       mergeToMap(context, mainContext, pkContext));
        }
    }

    private @NotNull Class<?> pickBaseTableClass() {
        TableField primaryKeyField = table.primaryKeyField();
        if (primaryKeyField == null) {
            return BaseTable.class;
        }
        Class<?> javaType = primaryKeyField.javaType();
        if (javaType == int.class) {
            return TableInt.class;
        }
        if (javaType == long.class) {
            return TableLong.class;
        }
        return TableObj.class;
    }

    private void constructors() {
        Optional<String> leftTable = table.leftBridgeField().map(ForeignTableField::getForeignTable).map(TableArch::javaName);
        Optional<String> rightTable = table.rightBridgeField().map(ForeignTableField::getForeignTable).map(TableArch::javaName);
        Map<String, String> context = orderedMapOf(
            "$left_table_decl", leftTable.map("protected final %s leftsTable;"::formatted).orElse(EMPTY_LINE),
            "$right_table_decl", rightTable.map("protected final %s rightsTable;"::formatted).orElse(EMPTY_LINE),
            "$left_table_init", leftTable.map("this.leftsTable = new %s(connector, follow);"::formatted).orElse(EMPTY_LINE),
            "$right_table_init", rightTable.map("this.rightsTable = new %s(connector, follow);"::formatted).orElse(EMPTY_LINE)
        );

        appendCode("""
        protected final Connector connector;
        protected final ReadFollow follow;
        $left_table_decl
        $right_table_decl
    
        public $TableClass(@Nonnull Connector connector, @Nonnull ReadFollow follow) {
            this.connector = connector;
            this.follow = follow;
            $left_table_init
            $right_table_init
        }
        
        public $TableClass(@Nonnull Connector connector) {
            this(connector, ReadFollow.NO_FOLLOW);
        }\n
        """, mergeToMap(mainContext, context));
    }

    private void withFollowOnRead() {
        String code = (table.hasForeignKeyField()) ?
            """
            @Override
            public @Nonnull $TableClass withReferenceFollowOnRead(@Nonnull ReadFollow follow) {
                return this.follow == follow ? this : new $TableClass(connector, follow);
            }\n
            """ :
            """
            @Override
            public @Nonnull $TableClass withReferenceFollowOnRead(@Nonnull ReadFollow follow) {
                return this;
            }\n
            """;
        appendCode(code, mainContext);
    }

    private void admin() {
        appendCode("""
        @Override
        public @Nonnull DbAdmin admin() {
            return new DbAdmin(connector);
        }\n
        """);
    }

    private void getters() {
        appendCode("""
        @Override
        public @Nonnull Engine engine() {
            return connector.engine();
        }
        
        @Override
        public @Nonnull QueryRunner runner() {
            return connector.runner();
        }\n
        """);
    }

    private void count() {
        appendCode("""
        @Override
        public int count() {
            String query = "SELECT COUNT(*) FROM $table_sql";
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            } catch (SQLException e) {
                throw new QueryException("Failed to count in $TableClass", query, e);
            }
        }
        
        @Override
        public int count(@Nonnull Filter filter) {
            String query = "SELECT COUNT(*) FROM $table_sql\\n" + filter.repr();
            try (PreparedStatement statement = runner().prepareQuery(query, filter.args());
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            } catch (SQLException e) {
                throw new QueryException("Failed to count in $TableClass", query, e);
            }
        }
        
        @Override
        public long count64() {
            String query = "SELECT COUNT(*) FROM $table_sql";
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0;
            } catch (SQLException e) {
                throw new QueryException("Failed to count in $TableClass", query, e);
            }
        }
        
        @Override
        public long count64(@Nonnull Filter filter) {
            String query = "SELECT COUNT(*) FROM $table_sql\\n" + filter.repr();
            try (PreparedStatement statement = runner().prepareQuery(query, filter.args());
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0;
            } catch (SQLException e) {
                throw new QueryException("Failed to count in $TableClass", query, e);
            }
        }\n
        """, mainContext);
    }

    private void exists() {
        appendCode("""
        @Override
        public boolean isNotEmpty() {
            String query = "SELECT EXISTS (SELECT * FROM $table_sql LIMIT 1)";
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            } catch (SQLException e) {
                throw new QueryException("Failed to check exists in $TableClass", query, e);
            }
        }
    
        @Override
        public boolean exists(@Nonnull Where where) {
            String query = "SELECT EXISTS (SELECT * FROM $table_sql " + where.repr() + " LIMIT 1)";
            try (PreparedStatement statement = runner().prepareQuery(query, where.args());
                 ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            } catch (SQLException e) {
                throw new QueryException("Failed to check exists in $TableClass", query, where.args(), e);
            }
        }\n
        """, mainContext);
    }

    private void selectConstants() {
        String constants = Stream.of(ReadFollow.values())
            .map(follow -> new SelectMaker(table).make(follow))
            .map(snippet -> new Snippet().appendLines(snippet))
            .map(query -> wrapAsStringLiteral(query).joinLines(INDENT1))
            .collect(Collectors.joining(INDENT1.commaDelimiter(), INDENT1.prefix(), ""));

        appendCode("""
        private static final String[] SELECT_ENTITY_ALL = {
        $constants
        };\n
        """, orderedMapOf("$constants", constants));
    }

    private void existsByPk() {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Snippet where = new Snippet().appendLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = orderedMapOf(
            "$sql_where_literal", wrapAsStringLiteral(where).joinLines(INDENT2),
            "$pk_object", toPrimaryKeyObject(requireNonNull(table.primaryKeyField()), "$pk_name")
        );

        appendCode("""
        @Override
        public boolean exists($pk_annotation$pk_type $pk_name) {
            String query = "SELECT EXISTS (SELECT * FROM $table_sql " + $sql_where_literal + " LIMIT 1)";
            try (PreparedStatement statement = runner().prepareQuery(query, $pk_object);
                 ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            } catch (SQLException e) {
                throw new QueryException("Failed to check exists in $TableClass", query, $pk_name, e);
            }
        }\n
        """, mergeToMap(context, mainContext, pkContext));
    }

    private void getByPk() {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Snippet where = new Snippet().appendLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = orderedMapOf(
            "$sql_where_literal", wrapAsStringLiteral(where).joinLines(INDENT2),
            "$pk_object", toPrimaryKeyObject(requireNonNull(table.primaryKeyField()), "$pk_name")
        );

        appendCode("""
        @Override
        public @Nullable $ModelClass getByPkOrNull($pk_annotation$pk_type $pk_name) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + $sql_where_literal;
            try (PreparedStatement statement = runner().prepareQuery(query, $pk_object);
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? fromRow(result, follow, 0) : null;
            } catch (SQLException e) {
                throw new QueryException("Failed to find by PK in $TableClass", query, $pk_name, e);
            }
        }\n
        """, mergeToMap(context, mainContext, pkContext));
    }

    private static @NotNull String toPrimaryKeyObject(@NotNull TableField field, @NotNull String paramName) {
        assert field.isPrimaryKey() : "Primary key field expected: " + field;
        return switch (field.typeSupport()) {
            case NATIVE -> paramName;
            case FOREIGN_KEY -> IllegalArgumentExceptions.fail("Primary key and foreign key field: %s", field);
            case MAPPER_API -> field.mapperApiOrDie().expr().fieldToJdbc(paramName);
            case ADAPTER_API -> {
                if (field.isSingleColumn()) {
                    yield field.adapterApiOrDie().expr().toValueObject(paramName);
                } else {
                    yield field.adapterApiOrDie().expr().toNewValuesArray(paramName);
                }
            }
        };
    }

    private void getBatchByPk() {
        TableField primaryField = table.primaryKeyField();
        if (primaryField == null || !primaryField.isNativelySupportedType()) {
            return;  // non-native fields require keys conversion
        }

        List<String> primaryColumns = primaryField.columns(ReadFollow.NO_FOLLOW)
            .stream()
            .map(PrefixedColumn::sqlPrefixedName)
            .toList();
        if (primaryColumns.size() != 1) {
            return;  // will use a slow default implementation
        }

        String queryExecution = """
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + "WHERE $pk_column IN (" + "?,".repeat(keys.size() - 1) + "?)";
            try (PreparedStatement statement = runner().prepareQuery(query, keys);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    $ModelClass entity = fromRow(result, follow, 0);
                    map.put(entity.$pk_getter, entity);
                }
            } catch (SQLException e) {
                throw new QueryException("Failed to find by PK batch in $TableClass", query, keys, e);
            }\
        """;

        Map<String, String> context = orderedMapOf(
            "$query_execution", queryExecution,
            "$pk_column", primaryColumns.getFirst(),
            "$pk_getter", primaryField.javaAccessor()
        );

        appendCode("""
        @Override
        public @Nonnull Map<$PkClass, $ModelClass> getBatchByPk(@Nonnull Collection<? extends $PkClass> keys) {
            if (keys.isEmpty()) {
                return Map.of();
            }
            HashMap<$PkClass, $ModelClass> map = new HashMap<>(keys.size());
        $query_execution
            return map;
        }\n
        """, mergeToMap(context, mainContext, pkContext));

        if (table.isPrimaryKeyInt()) {
            appendCode("""
            @Override
            public @Nonnull IntObjectMap<$ModelClass> getBatchByPk(@Nonnull IntContainer keys) {
                if (keys.isEmpty()) {
                    return new IntObjectHashMap<>();
                }
                IntObjectHashMap<$ModelClass> map = new IntObjectHashMap<>(keys.size());
            $query_execution
                return map;
            }\n
            """, mergeToMap(context, mainContext, pkContext));
        }
        if (table.isPrimaryKeyLong()) {
            appendCode("""
            @Override
            public @Nonnull LongObjectMap<$ModelClass> getBatchByPk(@Nonnull LongContainer keys) {
                if (keys.isEmpty()) {
                    return new LongObjectHashMap<>();
                }
                LongObjectHashMap<$ModelClass> map = new LongObjectHashMap<>(keys.size());
            $query_execution
                return map;
            }\n
            """, mergeToMap(context, mainContext, pkContext));
        }
    }

    private void fetchPks() {
        String primaryKeyColumn = Optional.ofNullable(table.primaryKeyField())
            .map(HasColumns::columns)
            .map(List::getFirst)
            .map(Column::sqlName)
            .orElse("");

        Map<String, String> context = orderedMapOf(
            "$pk_column", primaryKeyColumn
        );

        if (table.isPrimaryKeyInt()) {
            appendCode("""
            @Override
            public @Nonnull IntArrayList fetchPks(@Nonnull Filter filter) {
                String query = "SELECT $pk_column FROM $table_sql\\n" + filter.repr();
                try {
                    return runner().fetchIntColumn(() -> runner().prepareQuery(query, filter.args()));
                } catch (SQLException e) {
                    throw new QueryException("Failed to fetch by PKs in $TableClass", query, filter.args(), e);
                }
            }\n
            """, mergeToMap(context, mainContext, pkContext));
        }
        if (table.isPrimaryKeyLong()) {
            appendCode("""
            @Override
            public @Nonnull LongArrayList fetchPks(@Nonnull Filter filter) {
                String query = "SELECT $pk_column FROM $table_sql\\n" + filter.repr();
                try {
                    return runner().fetchLongColumn(() -> runner().prepareQuery(query, filter.args()));
                } catch (SQLException e) {
                    throw new QueryException("Failed to fetch by PKs in $TableClass", query, filter.args(), e);
                }
            }\n
            """, mergeToMap(context, mainContext, pkContext));
        }
    }

    private void keyOf() {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Map<String, String> context = orderedMapOf(
            "$KeyOfMethod", table.isPrimaryKeyInt() ? "intKeyOf" : table.isPrimaryKeyLong() ? "longKeyOf" : "keyOf",
            "$pk_getter", requireNonNull(table.primaryKeyField()).javaAccessor()
        );

        appendCode("""
        @Override
        public $pk_annotation$pk_type $KeyOfMethod(@Nonnull $ModelClass $model_param) {
            return $model_param.$pk_getter;
        }\n
        """, mergeToMap(context, mainContext, pkContext));
    }

    private void iterator() {
        appendCode("""
        @Override
        public void forEach(@Nonnull Consumer<? super $ModelClass> consumer) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()];
            try (PreparedStatement statement = runner().prepareQuery(query);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    consumer.accept(fromRow(result, follow, 0));
                }
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, e);
            }
        }
    
        @Override
        public @Nonnull ResultSetIterator<$ModelClass> iterator() {
            String query = SELECT_ENTITY_ALL[follow.ordinal()];
            try {
                return ResultSetIterator.of(runner().prepareQuery(query).executeQuery(),
                                            result -> fromRow(result, follow, 0));
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, e);
            }
        }

        @Override
        public @Nonnull ResultSetIterator<$ModelClass> iterator(@Nonnull Filter filter) {
            String query = SELECT_ENTITY_ALL[follow.ordinal()] + filter.repr();
            try {
                return ResultSetIterator.of(runner().prepareQuery(query, filter.args()).executeQuery(),
                                            result -> fromRow(result, follow, 0));
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, filter.args(), e);
            }
        }\n
        """, mainContext);
    }

    private void insert() {
        Snippet query = new InsertMaker(InsertMaker.Ignore.DEFAULT).makeAll(table);
        Map<String, String> context = orderedMapOf(
            "$model_id_assert", AssertModelIdMaker.makeAssert("$model_param", table).joinLines(),
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public int insert(@Nonnull $ModelClass $model_param) {
            $model_id_assert
            String query = $sql_query_literal;
            try {
                return runner().runUpdate(query, valuesForInsert($model_param));
            } catch (SQLException e) {
                throw new QueryException("Failed to insert entity into $TableClass", query, $model_param, e);
            }
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void insertIgnore() {
        Map<String, String> context = orderedMapOf(
            "$model_id_assert", AssertModelIdMaker.makeAssert("$model_param", table).joinLines(),
            "$sql_query_literal1", wrapAsStringLiteral(new InsertMaker(InsertMaker.Ignore.IGNORE).makeAll(table)).joinLines(INDENT3),
            "$sql_query_literal2", wrapAsStringLiteral(new InsertMaker(InsertMaker.Ignore.OR_IGNORE).makeAll(table)).joinLines(INDENT3)
        );

        appendCode("""
        @Override
        public int insertIgnore(@Nonnull $ModelClass $model_param) {
            $model_id_assert
            String query = switch (engine()) {
                case MySQL, MariaDB, H2 -> $sql_query_literal1;
                case SQLite -> $sql_query_literal2;
                default -> throw new UnsupportedOperationException(
                    "Insert-ignore unsupported for %s. Use insert() inside try-catch block".formatted(engine()));
            };
            try {
                return runner().runUpdate(query, valuesForInsert($model_param));
            } catch (SQLException e) {
                throw new QueryException("Failed to insert entity into $TableClass", query, $model_param, e);
           }
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void valuesForInsert() {
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", table.fields());
        Map<String, String> context = orderedMapOf(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
        );

        appendCode("""
        protected static @Nonnull Object[] valuesForInsert(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void insertAutoIncPk() {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return;
        }

        Snippet query = new InsertMaker(InsertMaker.Ignore.DEFAULT)
            .make(table, table.columns(TableField::isNotPrimaryKey));
        Map<String, String> context = orderedMapOf(
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public $pk_type insertAutoIncPk(@Nonnull $ModelClass $model_param) {
           String query = $sql_query_literal;
           try {
                return ($pk_type) runner().runAutoIncUpdate(query, valuesForInsertAutoIncPk($model_param)).lastId();
           } catch (SQLException e) {
                throw new QueryException("Failed to insert new entity into $TableClass", query, $model_param, e);
           }
        }\n
        """, mergeToMap(mainContext, pkContext, context));
    }

    private void valuesForInsertAutoIncPk() {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return;
        }

        List<TableField> nonPrimary = table.fields().stream().filter(TableField::isNotPrimaryKey).toList();
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", nonPrimary);
        Map<String, String> context = orderedMapOf(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
        );

        appendCode("""
        protected static @Nonnull Object[] valuesForInsertAutoIncPk(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void updateWhere() {
        Snippet query = new Snippet()
            .appendLines(UpdateMaker.make(table, table.columns(TableField::isNotPrimaryKey)));
        Map<String, String> context = orderedMapOf(
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public int updateWhere(@Nonnull $ModelClass $model_param, @Nonnull Where where) {
            String query = $sql_query_literal + where.repr();
            List<Object> args = valuesForUpdateWhere($model_param, where.args());
            try {
                return runner().runUpdate(query, args);
            } catch (SQLException e) {
                throw new QueryException("Failed to update entities in $TableClass by a filter", query, args, e);
            }
        }\n
        """, mergeToMap(mainContext, context));
    }

    private void valuesForUpdateWhere() {
        List<TableField> nonPrimary = table.fields().stream().filter(TableField::isNotPrimaryKey).toList();
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", nonPrimary);
        Map<String, String> context = orderedMapOf(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
        );

        appendCode("""
        protected static @Nonnull List<Object> valuesForUpdateWhere(@Nonnull $ModelClass $model_param, @Nonnull Args args) {
            Object[] values = valuesForUpdateWhere($model_param);
            if (args.isEmpty()) {
                return Arrays.asList(values);
            }
            List<Object> result = new ArrayList<>(values.length + args.size());
            result.addAll(Arrays.asList(values));
            result.addAll(args.asList());
            return result;
        }
        
        protected static @Nonnull Object[] valuesForUpdateWhere(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void updateByPk() {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Snippet query = new Snippet()
            .appendLines(UpdateMaker.make(table, table.columns(TableField::isNotPrimaryKey)))
            .appendLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = orderedMapOf(
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public int updateByPk(@Nonnull $ModelClass $model_param) {
            String query = $sql_query_literal;
            try {
                return runner().runUpdate(query, valuesForUpdateByPk($model_param));
            } catch (SQLException e) {
                throw new QueryException("Failed to update entity in $TableClass by PK", query, $model_param, e);
            }
        }\n
        """, mergeToMap(mainContext, pkContext, context));
    }

    private void valuesForUpdateByPk() {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        List<TableField> primary = table.fields().stream().filter(TableField::isPrimaryKey).toList();
        List<TableField> nonPrimary = table.fields().stream().filter(TableField::isNotPrimaryKey).toList();
        ValuesArrayMaker maker = new ValuesArrayMaker("$model_param", Iterables.concat(nonPrimary, primary));
        Map<String, String> context = orderedMapOf(
            "$array_init", maker.makeInitValues().join(linesJoiner(INDENT2)),
            "$array_convert", maker.makeConvertValues().join(linesJoiner(INDENT1, true))
        );

        appendCode("""
        protected static @Nonnull Object[] valuesForUpdateByPk(@Nonnull $ModelClass $model_param) {
            Object[] array = {
        $array_init
            };
        $array_convert
            return array;
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void insertData() {
        appendCode("""
        @Override
        public int insertData(@Nonnull EntityData<?> data) {
            Collection<? extends Column> columns = data.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + data;

            String query = makeInsertQueryForColumns(columns);
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                data.provideValues(statement);
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new QueryException("Failed to insert entity data into $TableClass", query, data, e);
            }
        }
        
        protected static @Nonnull String makeInsertQueryForColumns(@Nonnull Collection<? extends Column> columns) {
            String columnsSql = columns.stream().map(Column::name).collect(Collectors.joining(", "));
            String valuesSql = "?,".repeat(columns.size() - 1);
            return "INSERT INTO $table_sql (" + columnsSql + ")\\n" +
                   "VALUES (" + valuesSql + "?)\\n";
        }\n
        """, mainContext);
    }

    private void updateDataWhere() {
        appendCode("""
        @Override
        public int updateDataWhere(@Nonnull EntityData<?> data, @Nonnull Where where) {
            Collection<? extends Column> columns = data.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + data;
    
            String query = makeUpdateQueryForColumns(columns, where);
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                data.provideValues(statement, where);
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new QueryException("Failed to update entities data in $TableClass by a filter", query, data, e);
            }
        }
        
        protected static @Nonnull String makeUpdateQueryForColumns(@Nonnull Collection<? extends Column> columns, @Nonnull Where where) {
            String valuesSql = columns.stream().map(column -> column.name() + "=?").collect(Collectors.joining(", ", "", "\\n"));
            return "UPDATE $table_sql\\n" +
                   "SET " + valuesSql + where.repr();
        }\n
        """, mainContext);
    }

    private void insertBatch() {
        Snippet query = new InsertMaker(InsertMaker.Ignore.DEFAULT).makeAll(table);
        Map<String, String> context = orderedMapOf(
            // TODO[minor]: add an assert for batch
            // "$model_id_assert", AssertModelIdMaker.makeAssert("$model_param", table).join(),
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public int[] insertBatch(@Nonnull Collection<? extends $ModelClass> batch) {
            String query = $sql_query_literal;
            try {
                return runner().runUpdateBatch(query, batch.stream().map($TableClass::valuesForInsert).toList());
            } catch (SQLException e) {
                throw new QueryException("Failed to insert a batch of entities into $TableClass", query, batch, e);
            }
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void updateWhereBatch() {
        Snippet query = new Snippet()
            .appendLines(UpdateMaker.make(table, table.columns(TableField::isNotPrimaryKey)));
        Map<String, String> context = orderedMapOf(
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public int[] updateWhereBatch(@Nonnull Collection<? extends $ModelClass> batch, @Nonnull Contextual<Where, $ModelClass> where) {
            String query = $sql_query_literal + where.repr();
            try {
                return runner().runUpdateBatch(
                    query,
                    batch.stream().map(entity -> valuesForUpdateWhere(entity, where.resolveQueryArgs(entity))).toList()
                );
            } catch (SQLException e) {
                throw new QueryException("Failed to update a batch of entities in $TableClass by a filter", query, batch, e);
            }
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void insertDataBatch() {
        appendCode("""
        @Override
        public int[] insertDataBatch(@Nonnull BatchEntityData<?> batchData) {
            Collection<? extends Column> columns = batchData.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + batchData;
    
            String query = makeInsertQueryForColumns(columns);
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                batchData.provideBatchValues(statement, null);
                return statement.executeBatch();
            } catch (SQLException e) {
                throw new QueryException("Failed to insert a batch of entity data into $TableClass", query, batchData, e);
            }
        }\n
        """, mainContext);
    }

    private void updateDataWhereBatch() {
        appendCode("""
        @Override
        public <B> int[] updateDataWhereBatch(@Nonnull BatchEntityData<B> batchData, @Nonnull Contextual<Where, B> where) {
            Collection<? extends Column> columns = batchData.columns();
            assert columns.size() > 0 : "Entity data contains empty columns: " + batchData;
    
            String query = makeUpdateQueryForColumns(columns, where.query());
            try (PreparedStatement statement = runner().prepareQuery(query)) {
                batchData.provideBatchValues(statement, where);
                return statement.executeBatch();
            } catch (SQLException e) {
                throw new QueryException("Failed to update batch of entity data in $TableClass by a filter", query, batchData, e);
            }
        }\n
        """, mainContext);
    }

    private void deleteByPk() {
        if (!table.hasPrimaryKeyField()) {
            return;
        }

        Snippet query = new Snippet()
            .appendLines(DeleteMaker.make(table))
            .appendLines(WhereMaker.makeForPrimaryColumns(table));
        Map<String, String> context = orderedMapOf(
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2),
            "$pk_object", toPrimaryKeyObject(requireNonNull(table.primaryKeyField()), "$pk_name")
        );

        appendCode("""
        @Override
        public int deleteByPk($pk_annotation$pk_type $pk_name) {
            String query = $sql_query_literal;
            try {
               return runner().runUpdate(query, $pk_object);
           } catch (SQLException e) {
               throw new QueryException("Failed to delete entity in $TableClass by PK", query, $pk_name, e);
           }
        }\n
        """, mergeToMap(context, mainContext, pkContext));
    }

    private void deleteWhere() {
        Snippet query = new Snippet().appendLines(DeleteMaker.make(table)).forceMultiline(true);
        Map<String, String> context = orderedMapOf(
            "$sql_query_literal", wrapAsStringLiteral(query).joinLines(INDENT2)
        );

        appendCode("""
        @Override
        public int deleteWhere(@Nonnull Where where) {
            String query = $sql_query_literal + where.repr();
            try {
               return runner().runUpdate(query, where.args());
           } catch (SQLException e) {
               throw new QueryException("Failed to delete entities in $TableClass by a filter", query, where.args(), e);
           }
        }\n
        """, mergeToMap(context, mainContext));
    }

    private void fromRow() {
        ResultSetConversionMaker maker = new ResultSetConversionMaker("result", "follow", "start");
        Map<String, String> context = orderedMapOf(
            "$fields_assignments", maker.make(table).join(linesJoiner(INDENT1)),
            "$model_fields", table.fields().stream().map(TableField::javaName).collect(COMMA_JOINER)
        );

        appendCode("""
        public static @Nonnull $ModelClass fromRow(@Nonnull ResultSet result, @Nonnull ReadFollow follow, int start) throws SQLException {
        $fields_assignments
            return new $ModelClass($model_fields);
        }\n
        """, mergeToMap(mainContext, context));
    }

    private void bridge() {
        if (!table.isBridgeTable()) {
            return;
        }

        Map<String, String> context = bridgeContext();

        appendCode("""
        public @Nonnull $LeftTable leftsTable() {
            return leftsTable;
        }
        
        public @Nonnull $RightTable rightsTable() {
            return rightsTable;
        }
        
        @Override
        public boolean exists(@Nonnull $left_id_wrap leftId, @Nonnull $right_id_wrap rightId) {
            return exists(Where.hardcoded("$left_fk_sql = ? AND $right_fk_sql = ?", Args.of(leftId, rightId)));
        }
        
        @Override
        public int countLefts(@Nonnull $right_id_wrap rightId) {
            String sql = "$left_pk_sql IN (SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?)";
            return leftsTable.count(Where.hardcoded(sql, Args.of(rightId)));
        }
        
        @Override
        public int countRights(@Nonnull $left_id_wrap leftId) {
            String sql = "$right_pk_sql IN (SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?)";
            return rightsTable.count(Where.hardcoded(sql, Args.of(leftId)));
        }
        
        @Override
        public @Nonnull ResultSetIterator<$left_id_wrap> iterateLeftIds(@Nonnull $right_id_wrap rightId) {
            String query = "SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?";
            try {
                return ResultSetIterator.of(runner().prepareQuery(query, rightId).executeQuery(),
                                            result -> result.$left_result_getter(1));
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, rightId, e);
            }
        }
        
        @Override
        public @Nonnull ResultSetIterator<$left_entity> iterateLefts(@Nonnull $right_id_wrap rightId) {
            String sql = "$left_pk_sql IN (SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?)";
            return leftsTable.iterator(Where.hardcoded(sql, Args.of(rightId)));
        }
        
        @Override
        public @Nonnull ResultSetIterator<$right_id_wrap> iterateRightIds(@Nonnull $left_id_wrap leftId) {
            String query = "SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?";
            try {
                return ResultSetIterator.of(runner().prepareQuery(query, leftId).executeQuery(),
                                            result -> result.$right_result_getter(1));
            } catch (SQLException e) {
                throw new QueryException("Failed to iterate over $TableClass", query, leftId, e);
            }
        }
        
        @Override
        public @Nonnull ResultSetIterator<$right_entity> iterateRights(@Nonnull $left_id_wrap leftId) {
            String sql = "$right_pk_sql IN (SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?)";
            return rightsTable.iterator(Where.hardcoded(sql, Args.of(leftId)));
        }\n
        """, mergeToMap(mainContext, context));
    }

    private void bridgeNative() {
        if (!table.isBridgeTable()) {
            return;
        }

        boolean isLeftNative = isNativeIntOrLong(table.leftBridgeFieldOrDie().primaryKeyFieldInForeignTable());
        boolean isRightNative = isNativeIntOrLong(table.rightBridgeFieldOrDie().primaryKeyFieldInForeignTable());
        if (!isLeftNative && !isRightNative) {
            return;
        }

        Map<String, String> context = bridgeContext();

        if (isLeftNative && isRightNative) {
            appendCode("""
            public boolean exists($left_id_native leftId, $right_id_native rightId) {
                return exists(Where.hardcoded("$left_fk_sql = ? AND $right_fk_sql = ?", Args.of(leftId, rightId)));
            }\n
            """, mergeToMap(mainContext, context));
        }

        if (isLeftNative) {
            Map<String, String> extraContext = orderedMapOf(
                "$TypeMarker$", table.leftBridgeFieldOrDie().primaryKeyFieldInForeignTable().column().jdbcType().name()
            );

            appendCode("""
            public int countRights($left_id_native leftId) {
                String sql = "$right_pk_sql IN (SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?)";
                return rightsTable.count(Where.hardcoded(sql, Args.of(leftId)));
            }
            
            public @Nonnull ResultSetIterator<$right_entity> iterateRights($left_id_native leftId) {
                String sql = "$right_pk_sql IN (SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?)";
                return rightsTable.iterator(Where.hardcoded(sql, Args.of(leftId)));
            }
            
            public void forEachLeftId$TypeMarker$($right_id_native rightId, @Nonnull $TypeMarker$Consumer consumer) {
                String query = "SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?";
                try (PreparedStatement statement = runner().prepareQuery(query, rightId);
                     ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        consumer.accept(result.$left_result_getter(1));
                    }
                } catch (SQLException e) {
                    throw new QueryException("Failed to iterate over $TableClass", query, rightId, e);
                }
            }
            
            public @Nonnull $TypeMarker$ArrayList fetchAllLeftIds$TypeMarker$($right_id_native rightId) {
                String query = "SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?";
                try {
                    return runner().fetch$TypeMarker$Column(() -> runner().prepareQuery(query, rightId));
                } catch (SQLException e) {
                    throw new QueryException("Failed to fetch left ids in $TableClass", query, rightId, e);
                }
            }\n
            """, mergeToMap(mainContext, context, extraContext));
        }

        if (isRightNative) {
            Map<String, String> extraContext = orderedMapOf(
                "$TypeMarker$", table.rightBridgeFieldOrDie().primaryKeyFieldInForeignTable().column().jdbcType().name()
            );

            appendCode("""
            public int countLefts($right_id_native rightId) {
                String sql = "$left_pk_sql IN (SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?)";
                return leftsTable.count(Where.hardcoded(sql, Args.of(rightId)));
            }
            
            public @Nonnull ResultSetIterator<$left_entity> iterateLefts($right_id_native rightId) {
                String sql = "$left_pk_sql IN (SELECT $left_fk_sql FROM $table_sql WHERE $right_fk_sql = ?)";
                return leftsTable.iterator(Where.hardcoded(sql, Args.of(rightId)));
            }

            public void forEachRightId$TypeMarker$($left_id_native leftId, @Nonnull $TypeMarker$Consumer consumer) {
                String query = "SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?";
                try (PreparedStatement statement = runner().prepareQuery(query, leftId);
                     ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        consumer.accept(result.$right_result_getter(1));
                    }
                } catch (SQLException e) {
                    throw new QueryException("Failed to iterate over $TableClass", query, leftId, e);
                }
            }
            
            public @Nonnull $TypeMarker$ArrayList fetchAllRightIds$TypeMarker$($left_id_native leftId) {
                String query = "SELECT $right_fk_sql FROM $table_sql WHERE $left_fk_sql = ?";
                try {
                    return runner().fetch$TypeMarker$Column(() -> runner().prepareQuery(query, leftId));
                } catch (SQLException e) {
                    throw new QueryException("Failed to fetch right ids in $TableClass", query, leftId, e);
                }
            }\n
            """, mergeToMap(mainContext, context, extraContext));
        }
    }

    private static boolean isNativeIntOrLong(@NotNull OneColumnTableField field) {
        JdbcType jdbcType = field.column().jdbcType();
        return jdbcType == JdbcType.Int || jdbcType == JdbcType.Long;
    }

    private @NotNull Map<String, String> bridgeContext() {
        if (!table.isBridgeTable()) {
            return Map.of();
        }

        ForeignTableField leftField = table.leftBridgeFieldOrDie();
        ForeignTableField rightField = table.rightBridgeFieldOrDie();

        TableArch leftTable = leftField.getForeignTable();
        TableArch rightTable = rightField.getForeignTable();

        OneColumnTableField leftTablePrimaryField = leftField.primaryKeyFieldInForeignTable();
        OneColumnTableField rightTablePrimaryField = rightField.primaryKeyFieldInForeignTable();

        return BasicMaps.toMapUnchecked(
            "$LeftTable", leftTable.javaName(),
            "$left_table_sql", leftTable.sqlName(),
            "$left_id_native", Naming.shortCanonicalJavaName(leftTablePrimaryField.javaType()),
            "$left_id_wrap", Naming.shortCanonicalJavaName(Primitives.wrap(leftTablePrimaryField.javaType())),
            "$left_pk_sql", leftTablePrimaryField.column().sqlName(),
            "$left_fk_sql", leftField.foreignKeyColumn().sqlName(),
            "$left_result_getter", leftTablePrimaryField.column().jdbcType().getterMethod(),
            "$left_entity", Naming.shortCanonicalJavaName(leftTable.modelClass()),

            "$RightTable", rightTable.javaName(),
            "$right_table_sql", rightTable.sqlName(),
            "$right_id_native", Naming.shortCanonicalJavaName(rightTablePrimaryField.javaType()),
            "$right_id_wrap", Naming.shortCanonicalJavaName(Primitives.wrap(rightTablePrimaryField.javaType())),
            "$right_pk_sql", rightTablePrimaryField.column().sqlName(),
            "$right_fk_sql", rightField.foreignKeyColumn().sqlName(),
            "$right_result_getter", rightTablePrimaryField.column().jdbcType().getterMethod(),
            "$right_entity", Naming.shortCanonicalJavaName(rightTable.modelClass())
        );
    }

    private void internalMeta() {
        Map<String, String> context = orderedMapOf(
            "$KeyClass", table.hasPrimaryKeyField() ? "$pk_type.class" : "null"
        );

        appendCode("""
        public static final String NAME = "$table_sql";
        public static final Class<?> KEY_CLASS = $KeyClass;
        public static final Class<?> ENTITY_CLASS = $ModelClass.class;
        public static final Function<Connector, $TableClass> INSTANTIATE = $TableClass::new;\n
        """, mergeToMap(mainContext, context, pkContext));
    }

    private void columnsEnum() {
        Map<String, String> context = orderedMapOf(
            "$own_enum_values", ColumnEnumMaker.make(table.columns(ReadFollow.NO_FOLLOW)).join(INDENT1.commaDelimiter()),
            "$all_columns_list", table.columns().stream().map(column -> "OwnColumn.%s".formatted(column.sqlName()))
                .collect(Collectors.joining(INDENT2.commaDelimiter()))
        );

        appendCode("""
        public enum OwnColumn implements Column {
            $own_enum_values;

            public static final List<Column> ALL_COLUMNS = List.of(
                $all_columns_list
            );

            public final FullColumn FULL = this.fullFrom(META);

            private final TermType type;

            OwnColumn(TermType type) {
                this.type = type;
            }

            @Override
            public @Nonnull TermType type() {
                return type;
            }
        }\n
        """, mergeToMap(mainContext, context));
    }

    private void tableMeta() {
        Map<String, String> context = orderedMapOf(
            "$column_meta_list", table.columnsWithFields().stream()
                .map(pair -> ColumnMetaMaker.makeColumnMeta(pair.first(), pair.second()))
                .collect(Collectors.joining(INDENT3.commaDelimiter())),
            "$primary_keys", table.columnsWithFields().stream().filter(pair -> pair.first().isPrimaryKey()).map(pair -> {
                String column = pair.second().sqlName();
                return "OwnColumn.%s".formatted(column);
            }).collect(Collectors.joining(", ")),
            "$unique_keys", table.fields().stream().filter(TableField::isUnique).map(field -> {
                String columns = field.columns().stream().map(Column::sqlName).map("OwnColumn.%s"::formatted)
                    .collect(Collectors.joining(", "));
                return "Constraint.of(%s)".formatted(columns);
            }).collect(Collectors.joining(INDENT3.commaDelimiter()))
        );

        appendCode("""
        public static final TableMeta META = new TableMeta() {
            @Override
            public @Nonnull String sqlTableName() {
                return "$table_sql";
            }
            @Override
            public @Nonnull List<ColumnMeta> sqlColumns() {
                return List.of(
                    $column_meta_list
                );
            }
            @Override
            public @Nonnull Constraint primaryKeys() {
                return Constraint.of($primary_keys);
            }
            @Override
            public @Nonnull Iterable<Constraint> unique() {
                return List.of(
                    $unique_keys
                );
            }
        };
        
        @Override
        public @Nonnull TableMeta meta() {
            return META;
        }\n
        """, mergeToMap(mainContext, context));
    }

    private void dataFactoryMethods() {
        List<JdbcType> allTypes = table.columns().stream().map(Column::jdbcType).toList();
        boolean isAllInts = allTypes.stream().allMatch(x -> x == JdbcType.Int);
        boolean isAllLongs = allTypes.stream().allMatch(x -> x == JdbcType.Long);

        Map<String, String> context = orderedMapOf(
            "$columns_num", String.valueOf(table.columnsNumber()),
            "$data_method_name", "new%sData".formatted(table.modelName()),
            "$data_method_batch_name", "new%sBatch".formatted(table.modelName())
        );

        if (isAllInts) {
            appendCode("""
            public static @Nonnull EntityIntData $data_method_name(@Nonnull IntContainer data) {
                assert data.size() == $columns_num :
                    "Provided $TableClass data does not match required columns: data=%s, columns=%s"
                    .formatted(data, OwnColumn.ALL_COLUMNS);
                return new EntityIntData(OwnColumn.ALL_COLUMNS, data);
            }
    
            public static @Nonnull BatchEntityIntData $data_method_batch_name(@Nonnull IntContainer data) {
                assert data.size() % $columns_num == 0 :
                    "Provided $TableClass batch data does not match required columns: data=%s, columns=%s"
                    .formatted(data, OwnColumn.ALL_COLUMNS);
                return new BatchEntityIntData(OwnColumn.ALL_COLUMNS, data);
            }\n
            """, mergeToMap(mainContext, context));
        }

        if (isAllLongs) {
            appendCode("""
            public static @Nonnull EntityLongData $data_method_name(@Nonnull LongContainer data) {
                assert data.size() == $columns_num :
                    "Provided $TableClass data does not match required columns: data=%s, columns=%s"
                    .formatted(data, OwnColumn.ALL_COLUMNS);
                return new EntityLongData(OwnColumn.ALL_COLUMNS, data);
            }
            
            public static @Nonnull BatchEntityLongData $data_method_batch_name(@Nonnull LongContainer data) {
                assert data.size() % $columns_num == 0 :
                    "Provided $TableClass batch data does not match required columns: data=%s, columns=%s"
                    .formatted(data, OwnColumn.ALL_COLUMNS);
                return new BatchEntityLongData(OwnColumn.ALL_COLUMNS, data);
            }\n
            """, mergeToMap(mainContext, context));
        }

        appendCode("""
        public static @Nonnull EntityColumnMap<OwnColumn> $data_method_name(@Nonnull Map<OwnColumn, Object> map) {
            assert map.size() == $columns_num :
                "Provided $TableClass data map does not match required columns: map=%s, columns=%s"
                .formatted(map, OwnColumn.ALL_COLUMNS);
            return new EntityColumnMap<>(map);
        }
        """, mergeToMap(mainContext, context));
    }
}
