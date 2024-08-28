package io.spbx.orm.api.annotate;

import io.spbx.util.func.Reversible;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds the SQL-specific information to the model field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Sql {
    /**
     * Indicates the name to be used in SQL definition.
     */
    String name() default "";

    /**
     * Indicates whether this field corresponds to a PRIMARY KEY column.
     * In case of multiple columns, indicates a composite PRIMARY KEY.
     */
    boolean primary() default false;

    /**
     * Indicates whether this field corresponds to a UNIQUE column.
     * In case of multiple columns, indicates a composite UNIQUE.
     */
    boolean unique() default false;

    /**
     * Indicates whether this field corresponds to a NULL column.
     * In case of multiple columns, all of them are marked as NULL.
     */
    boolean nullable() default false;

    /**
     * Indicates the default value corresponding to the column's DEFAULT datatype definition.
     * In case of multiple columns, the size of an array must match the number of columns.
     */
    String[] defaults() default {};

    /**
     * Indicates the JDBC mapper or adapter that should be used to convert this field.
     * <p>
     * The JDBC mapper class must implement a {@link Reversible} interface with
     * one argument matching the field class and the other argument matching a JDBC supported type.
     * <p>
     * The JDBC adapter must follow the conventions specified in {@link io.spbx.orm.adapter.JdbcArrayAdapter}.
     *
     * @see io.spbx.orm.arch.model.JdbcType
     * @see Reversible
     * @see io.spbx.orm.adapter.JdbcArrayAdapter
     */
    Class<?> via() default Void.class;

    /**
     * A shortcut for {@link Sql#name()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @interface Name {
        String value() default "";
    }

    /**
     * A shortcut for {@link Sql#primary()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @interface PK {
    }

    /**
     * A shortcut for {@link Sql#unique()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @interface Unique {
    }

    /**
     * A shortcut for {@link Sql#nullable()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @interface Null {
    }

    /**
     * A shortcut for {@link Sql#defaults()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @interface Default {
        String[] value() default {};
    }

    /**
     * A shortcut for {@link Sql#via()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @interface Via {
        Class<?> value() default Void.class;
    }
}
