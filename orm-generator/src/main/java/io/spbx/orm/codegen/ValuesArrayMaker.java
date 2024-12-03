package io.spbx.orm.codegen;

import com.google.common.primitives.Primitives;
import io.spbx.orm.arch.model.TableField;
import io.spbx.util.func.BiIntObjFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class ValuesArrayMaker {
    private final String param;
    private final List<FieldConverter> converters;

    public ValuesArrayMaker(@NotNull String param, @NotNull Iterable<TableField> fields) {
        this.param = param;
        this.converters = zipWithColumnIndex(fields, FieldConverter::new);
    }

    public @NotNull Snippet makeInitValues() {
        return new Snippet().appendLines(converters.stream().flatMap(FieldConverter::initLines));
    }

    public @NotNull Snippet makeConvertValues() {
        return new Snippet().appendLines(converters.stream().map(FieldConverter::fillValuesLine));
    }

    private static <T> @NotNull List<T> zipWithColumnIndex(@NotNull Iterable<TableField> fields,
                                                           @NotNull BiIntObjFunction<TableField, T> converter) {
        ArrayList<T> result = new ArrayList<>();
        int columnIndex = 0;
        for (TableField field : fields) {
            result.add(converter.apply(columnIndex, field));
            columnIndex += field.columnsNumber();
        }
        return result;
    }

    private class FieldConverter {
        private final TableField field;
        private final int columnIndex;

        private FieldConverter(int columnIndex, @NotNull TableField field) {
            this.field = field;
            this.columnIndex = columnIndex;
        }

        public @NotNull Stream<String> initLines() {
            return switch (field.typeSupport()) {
                case NATIVE -> Stream.of(accessFieldExpr() + ",");
                case FOREIGN_KEY -> Stream.of(accessFieldExpr() + ".getFk(),");
                case MAPPER_API -> Stream.of(field.mapperApiOrDie().expr().fieldToJdbc(accessFieldExpr()) + ",");
                case ADAPTER_API -> Stream.generate(() -> "null /* via %s */,".formatted(field.adapterApiOrDie().staticRef()))
                    .limit(field.columnsNumber());
            };
        }

        public @NotNull String fillValuesLine() {
            return switch (field.typeSupport()) {
                case NATIVE, FOREIGN_KEY, MAPPER_API -> "";
                case ADAPTER_API -> {
                    // Special case: `char` type. Has a custom support, but shouldn't be handled for null.
                    if (field.isNotNull() || Primitives.allPrimitiveTypes().contains(field.javaType())) {
                        yield field.adapterApiOrDie().statement().fillArrayValues(accessFieldExpr(), "array", columnIndex);
                    } else {
                        yield "Optional.ofNullable(%s).ifPresent(%s -> %s);".formatted(
                            accessFieldExpr(),
                            field.javaName(),
                            field.adapterApiOrDie().expr().fillArrayValues(field.javaName(), "array", columnIndex)
                        );
                    }
                }
            };
        }

        private @NotNull String accessFieldExpr() {
            return "%s.%s".formatted(param, field.javaAccessor());
        }
    }
}
