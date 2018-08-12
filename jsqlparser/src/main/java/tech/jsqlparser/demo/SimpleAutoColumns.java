package tech.jsqlparser.demo;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AutoColumn(columns = {"create_time", "create_user_id", "create_user_name"},
        values = {"'NOW()'", "T(tech.jsqlparser.demo.JSqlParserDemo).userId", "T(tech.jsqlparser.demo.JSqlParserDemo).userName()"},
        valueType = {AutoColumn.JdbcType.FUNCTION, AutoColumn.JdbcType.NUMBER, AutoColumn.JdbcType.STRING})
public @interface SimpleAutoColumns {
}
