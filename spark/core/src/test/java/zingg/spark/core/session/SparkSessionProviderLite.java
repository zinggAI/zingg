package zingg.spark.core.session;

public class SparkSessionProviderLite extends SparkSessionProviderBase {

    private static SparkSessionProviderLite sparkSessionProvider;

    @Override
    protected String getPropertiesFile() {
        return "/zingg-lite.properties";
    }

    public static SparkSessionProviderLite getInstance() {
        if (sparkSessionProvider == null) {
            sparkSessionProvider = new SparkSessionProviderLite();
            sparkSessionProvider.initializeSession();
        }
        return sparkSessionProvider;
    }
}
