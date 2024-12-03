package io.spbx.orm.testing;

import com.google.common.truth.Ordered;
import com.google.common.truth.Truth;
import io.spbx.orm.api.debug.DebugSql;
import io.spbx.orm.api.query.Args;
import io.spbx.orm.api.query.InvalidQueryException;
import io.spbx.orm.api.query.Representable;
import io.spbx.orm.api.query.Term;
import io.spbx.orm.api.query.TermType;
import io.spbx.orm.api.query.Unit;
import io.spbx.orm.testing.AssertCode.CodeSubject;
import io.spbx.util.base.annotate.CanIgnoreReturnValue;
import io.spbx.util.base.annotate.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

import static io.spbx.util.base.lang.EasyCast.castAny;
import static io.spbx.util.base.lang.EasyCast.castToInt;
import static io.spbx.util.testing.TestingBasics.streamOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertSql {
    @CheckReturnValue
    public static @NotNull SqlSubject<SqlSubject<?>> assertThatSql(@NotNull String query) {
        return new SqlSubject<>(query);
    }

    @CheckReturnValue
    public static @NotNull SqlSubject<SqlSubject<?>> assertThatSql(@NotNull Representable repr) {
        return assertThatSql(repr.repr());
    }

    @CheckReturnValue
    public static @NotNull ArgsSubject<ArgsSubject<?>> assertThat(@NotNull Args args) {
        return new ArgsSubject<>(args);
    }

    @CheckReturnValue
    public static @NotNull UnitSubject<UnitSubject<?>> assertThat(@NotNull Unit unit) {
        return new UnitSubject<>(unit);
    }

    @CheckReturnValue
    public static @NotNull TermSubject<TermSubject<?>> assertTerm(@NotNull Term term) {
        return new TermSubject<>(term);
    }

    @CheckReturnValue
    public static @NotNull RowsSubject assertRows(@NotNull List<DebugSql.Row> rows) {
        return new RowsSubject(rows);
    }

    public static void assertReprThrows(@NotNull Supplier<Representable> repr) {
        assertThrows(InvalidQueryException.class, repr::get);
    }

    public static class SqlSubject<T extends SqlSubject<?>> extends CodeSubject<T> {
        public SqlSubject(@NotNull String query) {
            super(query);
        }

        public @NotNull T matchesIgnoringCase(@NotNull String expected) {
            return matchesConverted(expected, String::toLowerCase);
        }
    }

    @CanIgnoreReturnValue
    public static class ArgsSubject<T extends ArgsSubject<?>> {
        private final @NotNull Args args;

        public ArgsSubject(@NotNull Args args) {
            this.args = args;
        }

        public @NotNull T hasArgs(@NotNull Args expected) {
            Truth.assertThat(args).isEqualTo(expected);
            return castAny(this);
        }

        public @NotNull T containsArgsExactly(@NotNull Object @Nullable ... expected) {
            Truth.assertThat(args.asList()).containsExactly(expected);
            return castAny(this);
        }

        public @NotNull T containsNoArgs() {
            Truth.assertThat(args.asList()).isEmpty();
            return castAny(this);
        }

        public @NotNull T allArgsResolved() {
            Truth.assertThat(args.isAllResolved()).isTrue();
            return castAny(this);
        }

        public @NotNull T containsUnresolved() {
            Truth.assertThat(args.isAllResolved()).isFalse();
            return castAny(this);
        }
    }

    @CanIgnoreReturnValue
    public static class UnitSubject<T extends UnitSubject<?>> extends ArgsSubject<UnitSubject<T>> {
        private final @NotNull Unit unit;

        public UnitSubject(@NotNull Unit unit) {
            super(unit.args());
            this.unit = unit;
        }

        public @NotNull T isEqualTo(@NotNull Unit expected) {
            return hasArgs(expected.args()).matches(expected.repr());
        }

        public @NotNull T matches(@NotNull String expected) {
            assertThatSql(unit.repr()).matches(expected);
            return castAny(this);
        }
    }

    @CanIgnoreReturnValue
    public static class TermSubject<T extends TermSubject<?>> extends SqlSubject<TermSubject<?>> {
        private final Term term;

        public TermSubject(@NotNull Term term) {
            super(term.repr());
            this.term = term;
        }

        public @NotNull T hasType(@NotNull TermType type) {
            Truth.assertThat(term.type()).isEqualTo(type);
            return castAny(this);
        }
    }

    @CanIgnoreReturnValue
    public record RowsSubject(@NotNull List<DebugSql.Row> rows) {
        public @NotNull Ordered containsExactly(@NotNull Object[] ... expected) {
            List<List<Object>> values = rows.stream()
                .map(row -> row.values().stream().map(DebugSql.RowValue::value).map(RowsSubject::adjust).toList())
                .toList();
            List<List<Object>> expectedList = streamOf(expected)
                .map(row -> streamOf(row).map(RowsSubject::adjust).toList())
                .toList();
            return Truth.assertThat(values).containsExactlyElementsIn(expectedList);
        }

        private static @Nullable Object adjust(@Nullable Object val) {
            if (val instanceof Boolean) {
                return castToInt(val == Boolean.TRUE);
            }
            if (val instanceof Number number && (long) (number.intValue()) == number.longValue() &&
                (val instanceof Long || val instanceof BigInteger)) {
                return number.intValue();
            }
            if (val instanceof BigDecimal decimal) {
                return decimal.doubleValue();
            }
            return val;
        }
    }

    public static <T> @Nullable T adjustType(@Nullable Object val) {
        return castAny(RowsSubject.adjust(val));
    }
}
