package tech.jsqlparser.demo;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface AutoColumn {

    String column() default "";

    boolean pass() default false;//把值原封不动的传递给SQL,不经过任何类型转换

    String expression() default "";
}
