package tech.jsqlparser.demo;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        }
)
public class AutoColumnInterceptor implements Interceptor {

    private final static Logger logger = LoggerFactory.getLogger(AutoColumnInterceptor.class);

    private ExpressionParser parser = new SpelExpressionParser();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        AutoColumns autoColumn = AnnotationUtils.findAnnotation(method, AutoColumns.class);
        if (Objects.isNull(autoColumn)) {
            return invocation.proceed();
        }
        String[] autoColumns = autoColumn.columns();
        AutoColumns.JdbcType[] valueTypes = autoColumn.jdbcType();
        List<Object> values = Arrays.stream(autoColumn.values()).map(value -> {
            Expression exp = parser.parseExpression(value);
            return exp.getValue();
        }).collect(Collectors.toList());


        Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        String boundSql = ms.getSqlSource().getBoundSql(parameter).getSql();

        Update update = (Update) CCJSqlParserUtil.parse(boundSql);
        List<Column> columns = update.getColumns();
        for (int i = 0; i < autoColumns.length; i++) {
            columns.add(new Column(autoColumns[i]));
        }
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            switch (valueTypes[i]) {
                case NUMBER:
                    update.getExpressions().add(new LongValue(Long.parseLong(String.valueOf(value))));
                    break;
                case STRING:
                    update.getExpressions().add(new StringValue(String.valueOf(value)));
                    break;
                case TEMPLATE:
                    update.getExpressions().add(new TemplateExpression(String.valueOf(value)));
                    break;
            }
        }
        String newSql = update.toString();
        logger.info("new sql:{}", newSql);

        StaticSqlSource newSqlSource = new StaticSqlSource(ms.getConfiguration(), newSql);
        MappedStatement.Builder newBuilder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "$" + AutoColumns.class.getCanonicalName(), newSqlSource, ms.getSqlCommandType());
        MappedStatement newMs = newBuilder.build();
        return executor.update(newMs, parameter);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
