//package zingg.client;
//
//import org.apache.spark.sql.SparkSession;
//import org.junit.jupiter.api.extension.AfterAllCallback;
//import org.junit.jupiter.api.extension.BeforeAllCallback;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.junit.jupiter.api.extension.ParameterContext;
//import org.junit.jupiter.api.extension.ParameterResolutionException;
//import org.junit.jupiter.api.extension.ParameterResolver;
//
//import zingg.spark.core.executor.ZinggSparkTester;
//
//public class TestSparkBase extends ZinggSparkTester implements BeforeAllCallback, AfterAllCallback, ParameterResolver{
//
//    public SparkSession sparkSession;
//
//    static boolean isSetUp;
//
//    @Override
//    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
//            throws ParameterResolutionException {
//        return parameterContext.getParameter().getType()
//                .equals(SparkSession.class);
//    }
//
//    @Override
//    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
//            throws ParameterResolutionException {
//        return sparkSession;
//    }
//
//    @Override
//    public void afterAll(ExtensionContext context) throws Exception {
//
//    }
//
//    @Override
//    public void beforeAll(ExtensionContext context) throws Exception {
//        if (!isSetUp || sparkSession == null) {
//            super.setup();
//            sparkSession = ZinggSparkTester.spark;
//        }
//        isSetUp = true;
//    }
//
//
//}
