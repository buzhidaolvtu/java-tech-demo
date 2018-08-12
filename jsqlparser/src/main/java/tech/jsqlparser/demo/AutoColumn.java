package tech.jsqlparser.demo;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface AutoColumn {

    enum JdbcType {
        STRING,
        NUMBER,
        FUNCTION
    }

    String[] columns() default {};

    JdbcType[] valueType() default {};

    String[] values() default {};

}
