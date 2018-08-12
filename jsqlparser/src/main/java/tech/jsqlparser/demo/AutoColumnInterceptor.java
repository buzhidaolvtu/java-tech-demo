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
import java.util.Objects;
import java.util.Properties;

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
        AutoColumns autoColumnCfg = AnnotationUtils.findAnnotation(method, AutoColumns.class);
        if (Objects.isNull(autoColumnCfg)) {
            return invocation.proceed();
        }

        Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        String boundSql = ms.getSqlSource().getBoundSql(parameter).getSql();

        Update update = (Update) CCJSqlParserUtil.parse(boundSql);

        AutoColumn[] autoColumns = autoColumnCfg.value();
        Arrays.stream(autoColumns).forEach(autoColumn -> {
            resolveAutoColumn(autoColumn, update);
        });
        String newSql = update.toString();
        logger.info("new sql:{}", newSql);

        StaticSqlSource newSqlSource = new StaticSqlSource(ms.getConfiguration(), newSql);
        MappedStatement.Builder newBuilder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "$" + AutoColumns.class.getCanonicalName(), newSqlSource, ms.getSqlCommandType());
        MappedStatement newMs = newBuilder.build();
        return executor.update(newMs, parameter);
    }

    private void resolveAutoColumn(AutoColumn autoColumn, Update update) {
        String column = autoColumn.column();
        boolean pass = autoColumn.pass();
        String expression = autoColumn.expression();

        update.getColumns().add(new Column(column));

        Expression exp = parser.parseExpression(expression);
        if (pass) {
            update.getExpressions().add(new ValueExpression(String.valueOf(exp.getValue())));
        } else if (isString(exp.getValueType())) {
            update.getExpressions().add(new StringValue(String.valueOf(exp.getValue())));
        } else if (isNumber(exp.getValueType())) {
            update.getExpressions().add(new LongValue(String.valueOf(exp.getValue())));
        }
    }

    private boolean isString(Class<?> valueType) {
        return String.class.isAssignableFrom(valueType);
    }

    private boolean isNumber(Class<?> valueType) {
        return Number.class.isAssignableFrom(valueType);
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
