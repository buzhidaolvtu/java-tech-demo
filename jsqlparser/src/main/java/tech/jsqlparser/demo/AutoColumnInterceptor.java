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

    private ExpressionParser parser = new SpelExpressionParser();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        AutoColumn autoColumn = method.getAnnotation(AutoColumn.class);
        if (Objects.isNull(autoColumn)) {
            return invocation.proceed();
        }
        String[] autoColumns = autoColumn.columns();
        AutoColumn.JdbcType[] valueTypes = autoColumn.valueType();
        List<String> values = Arrays.stream(autoColumn.values()).map(value -> {
            Expression exp = parser.parseExpression(value);
            String message = String.valueOf(exp.getValue());
            return message;
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
            String value = values.get(i);
            switch (valueTypes[i]) {
                case NUMBER:
                    update.getExpressions().add(new LongValue(Long.parseLong(value)));
                    break;
                case STRING:
                    update.getExpressions().add(new StringValue(value));
                    break;
                case FUNCTION:
                    update.getExpressions().add(new TemplateExpression(value));
                    break;
            }
        }
        String newSql = update.toString();
        System.out.println(newSql);

        StaticSqlSource newSqlSource = new StaticSqlSource(ms.getConfiguration(), newSql);
        MappedStatement.Builder newBuilder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "$" + AutoColumn.class.getCanonicalName(), newSqlSource, ms.getSqlCommandType());
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
