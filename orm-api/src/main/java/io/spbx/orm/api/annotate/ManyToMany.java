package io.spbx.orm.api.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the model class as a many-to-many association, for which a bridge table is to be generated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ManyToMany {
    /**
     * An optional name of the field which is the first ("left") foreign model in this association.
     */
    String left() default "";

    /**
     *
     * An optional name of the field which is the second ("right") foreign model in this association.
     */
    String right() default "";
}
