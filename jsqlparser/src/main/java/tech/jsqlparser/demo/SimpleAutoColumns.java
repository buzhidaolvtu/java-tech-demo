package tech.jsqlparser.demo;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AutoColumn(columns = {"update_time", "update_user_id", "update_user_name"},
        values = {"'NOW()'", "T(tech.jsqlparser.demo.JSqlParserDemo).userId", "T(tech.jsqlparser.demo.JSqlParserDemo).userName()"},
        jdbcType = {AutoColumn.JdbcType.FUNCTION, AutoColumn.JdbcType.NUMBER, AutoColumn.JdbcType.STRING})
public @interface SimpleAutoColumns {
}
