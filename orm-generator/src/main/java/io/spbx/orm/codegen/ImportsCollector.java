package io.spbx.orm.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import io.spbx.orm.api.BridgeTable;
import io.spbx.orm.api.Connector;
import io.spbx.orm.api.DbAdmin;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.Foreign;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.api.QueryException;
import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.ReadFollow;
import io.spbx.orm.api.ResultSetIterator;
import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.entity.BatchEntityData;
import io.spbx.orm.api.entity.BatchEntityIntData;
import io.spbx.orm.api.entity.BatchEntityLongData;
import io.spbx.orm.api.entity.EntityColumnMap;
import io.spbx.orm.api.entity.EntityData;
import io.spbx.orm.api.entity.EntityIntData;
import io.spbx.orm.api.entity.EntityLongData;
import io.spbx.orm.api.query.Args;
import io.spbx.orm.api.query.Contextual;
import io.spbx.orm.api.query.Filter;
import io.spbx.orm.api.query.FullColumn;
import io.spbx.orm.api.query.TermType;
import io.spbx.orm.api.query.Where;
import io.spbx.orm.arch.model.ForeignTableField;
import io.spbx.orm.arch.model.JavaNameHolder;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.model.TableField;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ImportsCollector {
    private static final ImmutableSet<String> IMPORTED_BY_STAR = ImmutableSet.of("java.lang", "java.sql", "java.util");

    private static final ImmutableList<Class<?>> DEFAULT_ORM_CLASSES = ImmutableList.of(
        Connector.class, QueryRunner.class, QueryException.class, Engine.class, ReadFollow.class, DbAdmin.class,
        Filter.class, Where.class, Args.class, FullColumn.class, TermType.class,
        io.spbx.orm.api.query.Column.class,
        ResultSetIterator.class, TableMeta.class,
        EntityData.class, EntityIntData.class, EntityLongData.class, EntityColumnMap.class,
        BatchEntityData.class, BatchEntityIntData.class, BatchEntityLongData.class,
        Contextual.class
    );

    private final ModelAdaptersLocator locator;
    private final TableArch table;
    private final Class<?> baseClass;

    public ImportsCollector(@NotNull ModelAdaptersLocator locator, @NotNull TableArch table, @NotNull Class<?> baseClass) {
        this.locator = locator;
        this.table = table;
        this.baseClass = baseClass;
    }

    public @NotNull List<String> imports() {
        List<Class<?>> mappedTypes = table.fields().stream()
            .filter(TableField::isMapperSupportedType)
            .map(TableField::javaType)
            .collect(Collectors.toList());
        List<Class<?>> mapperTypes = table.fields().stream()
            .filter(TableField::isMapperSupportedType)
            .flatMap(field -> field.mapperApiOrDie().importedClass().stream())
            .toList();
        List<Class<?>> adapterClasses = table.fields().stream()
            .filter(TableField::isAdapterSupportType)
            .map(TableField::javaType)
            .collect(Collectors.toList());

        List<Class<?>> foreignKeyClasses = table.hasForeignKeyField() ?
            List.of(Foreign.class, ForeignInt.class, ForeignLong.class, ForeignObj.class) :
            List.of();
        /*List<Class<?>> foreignKeyClasses = table.foreignFields(ReadFollow.FOLLOW_ALL).stream()
                .map(TableField::javaType)
                .collect(Collectors.toList());*/
        List<JavaNameHolder> foreignTables = table.foreignFields(ReadFollow.FOLLOW_ALL).stream()
            .map(ForeignTableField::getForeignTable)
            .collect(Collectors.toList());
        List<Class<?>> foreignModelClasses = table.isBridgeTable() ?
            Stream.of(table.leftBridgeFieldOrDie(), table.rightBridgeFieldOrDie())
                .map(ForeignTableField::getForeignTable)
                .map(TableArch::modelClass)
                .collect(Collectors.toList()) :
            List.of();

        List<Class<?>> allClasses = Streams.concat(
            DEFAULT_ORM_CLASSES.stream(),
            baseTableClasses().stream(),
            mappedTypes.stream(),
            mapperTypes.stream(),
            foreignKeyClasses.stream(),
            foreignModelClasses.stream()
        ).filter(this::isImportable).toList();

        List<FQN> adapterFqns = Streams.concat(
            adapterClasses.stream().filter(this::isImportable).map(FQN::of),
            adapterClasses.stream().map(locator::locateAdapterFqn)
        ).toList();

        List<FQN> fqns = Streams.concat(
            allClasses.stream().map(FQN::of),
            adapterFqns.stream(),
            foreignTables.stream().map(FQN::of)
        ).filter(fqn -> !isSkippable(fqn)).toList();

        return fqns.stream().map(FQN::toImportName).sorted().distinct().toList();
    }

    private @NotNull List<Class<?>> baseTableClasses() {
        if (table.isBridgeTable()) {
            return List.of(baseClass, BridgeTable.class);
        }
        return List.of(baseClass);
    }

    private boolean isImportable(@NotNull Class<?> klass) {
        return !klass.isPrimitive() && !klass.isArray();
    }

    private boolean isSkippable(@NotNull FQN fqn) {
        // BitSet is in both `java.util` and `hppc`.
        return !fqn.className().equals("BitSet") &&
               (fqn.packageName().equals(table.packageName()) || IMPORTED_BY_STAR.contains(fqn.packageName()));
    }
}
