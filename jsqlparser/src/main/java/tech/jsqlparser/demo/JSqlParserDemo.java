package tech.jsqlparser.demo;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.managed.ManagedTransaction;

public class JSqlParserDemo {

    @SimpleAutoColumns
    public void update() {
    }

    public static final Long userId = 123L;

    public static String userName() {
        return "吕途";
    }

    public static void main(String[] nouseargs) throws Throwable {
        String sql = "update mytable set a=?,b=?,c=? where 1=1";
        Configuration configuration = new Configuration();
        Executor executor1 = configuration.newExecutor(new ManagedTransaction(null, false));
        StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, "1", sqlSource, SqlCommandType.UPDATE);
        MappedStatement makedMs = builder.build();

        Invocation invocation = new Invocation(executor1, JSqlParserDemo.class.getMethod("update"), new Object[]{makedMs, 1});
        AutoColumnInterceptor interceptor = new AutoColumnInterceptor();
        interceptor.intercept(invocation);

    }

}
