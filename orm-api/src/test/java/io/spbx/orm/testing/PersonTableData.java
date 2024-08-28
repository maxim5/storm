package io.spbx.orm.testing;

import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.query.Column;
import io.spbx.orm.api.query.FullColumn;
import io.spbx.orm.api.query.TermType;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.List;

public class PersonTableData {
    public enum PersonColumn implements Column {
        id(TermType.NUMBER),
        name(TermType.STRING),
        country(TermType.STRING),
        sex(TermType.BOOL),
        birthday(TermType.TIME),
        iq(TermType.NUMBER),
        height(TermType.NUMBER),
        photo(TermType.STRING);

        public final FullColumn FULL = this.fullFrom(PERSON_META);

        private final TermType type;

        PersonColumn(TermType type) {
            this.type = type;
        }

        @Override
        public @NotNull TermType type() {
            return type;
        }
    }

    public static final TableMeta PERSON_META = new TableMeta() {
        @Override
        public @NotNull String sqlTableName() {
            return "person";
        }

        @Override
        public @NotNull List<ColumnMeta> sqlColumns() {
            return List.of(
                ColumnMeta.of(PersonColumn.id, int.class).withPrimaryKey(ConstraintStatus.SINGLE_COLUMN),
                ColumnMeta.of(PersonColumn.name, String.class),
                ColumnMeta.of(PersonColumn.country, String.class),
                ColumnMeta.of(PersonColumn.sex, boolean.class),
                ColumnMeta.of(PersonColumn.birthday, Timestamp.class),
                ColumnMeta.of(PersonColumn.iq, int.class).withUnique(ConstraintStatus.SINGLE_COLUMN),
                ColumnMeta.of(PersonColumn.height, double.class),
                ColumnMeta.of(PersonColumn.photo, byte[].class)
            );
        }

        @Override
        public @NotNull Constraint primaryKeys() {
            return Constraint.of(PersonColumn.id);
        }

        @Override
        public @NotNull Iterable<Constraint> unique() {
            return List.of(Constraint.of(PersonColumn.photo));
        }
    };

    public static final boolean FEMALE = true;
    public static final boolean MALE = false;

    public static @NotNull Timestamp parseDate(@NotNull String date) {
        return Timestamp.valueOf(date + " 00:00:00");
    }

    public static byte @NotNull[] photo(int value) {
        return Integer.toHexString(value).getBytes();
    }
}
