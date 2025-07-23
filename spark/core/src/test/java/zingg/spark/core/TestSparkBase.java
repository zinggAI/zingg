package zingg.spark.core;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import zingg.common.client.arguments.model.IArguments;
import zingg.spark.core.session.SparkSessionProvider;
import zingg.spark.core.context.ZinggSparkContext;

public class TestSparkBase implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
    public static IArguments args;
    public static JavaSparkContext ctx;
    public static SparkSession spark;
    public static ZinggSparkContext zsCTX;
    static boolean isSetUp = false;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType()
                .equals(SparkSession.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return spark;
    }

    @Override
    public void afterAll(ExtensionContext context) {

    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!isSetUp || spark == null) {
            SparkSessionProvider sparkSessionProvider = SparkSessionProvider.getInstance();
            spark = sparkSessionProvider.getSparkSession();
            ctx = sparkSessionProvider.getJavaSparkContext();
            args = sparkSessionProvider.getArgs();
            zsCTX = sparkSessionProvider.getZinggSparkContext();
        }
        isSetUp = true;
    }


}
