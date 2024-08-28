package io.spbx.orm.api.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds the generator options to the model class, which affect the table classes generated from the model.
 * The annotation is not mandatory for a model class, but can be helpful for documentation and tools analysis
 * purposes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Model {
    /**
     * Indicates a custom name of this model to used for other generated names, such as
     * {@link #sqlName()}, {@link #javaTableName()}, primary key inference, etc.
     */
    String javaName() default "";

    /**
     * Indicates the table name in SQL. By default, uses a <code>[model_name]_table</code> pattern.
     * This option overwrites {@link #javaName()}.
     */
    String sqlName() default "";

    /**
     * Indicates the java name for the table class. By default, uses a <code>[ModelName]Table</code> pattern.
     * This option overwrites {@link #javaName()}.
     */
    String javaTableName() default "";

    /**
     * Indicates an alternative class (commonly, interface) which foreign keys can use to reference this model.
     * If not specified, foreign reference have to use this model class exactly.
     *
     * @see io.spbx.orm.api.Foreign
     */
    Class<?> exposeAs() default Void.class;
}
