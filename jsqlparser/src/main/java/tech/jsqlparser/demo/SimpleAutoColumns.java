package tech.jsqlparser.demo;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AutoColumns({
        @AutoColumn(column = "update_time", expression = "'NOW()'", pass = true),
        @AutoColumn(column = "update_time_test", expression = "1024", pass = true),
        @AutoColumn(column = "update_user_id", expression = "T(tech.jsqlparser.demo.JSqlParserDemo).userId"),
        @AutoColumn(column = "update_user_name", expression = "T(tech.jsqlparser.demo.JSqlParserDemo).userName()")
})
public @interface SimpleAutoColumns {
}
