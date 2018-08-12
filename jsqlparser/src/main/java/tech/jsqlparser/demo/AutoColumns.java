package tech.jsqlparser.demo;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface AutoColumns {

    enum JdbcType {
        STRING,
        NUMBER,
        TEMPLATE//把值原封不动的传递给SQL
    }

    String[] columns() default {};

    JdbcType[] jdbcType() default {};

    String[] values() default {};
}
