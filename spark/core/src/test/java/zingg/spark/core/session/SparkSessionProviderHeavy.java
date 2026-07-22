package zingg.spark.core.session;

public class SparkSessionProviderHeavy extends SparkSessionProviderBase {

    private static SparkSessionProviderHeavy sparkSessionProvider;

    @Override
    protected String getPropertiesFile() {
        return "/zingg-heavy.properties";
    }

    public static SparkSessionProviderHeavy getInstance() {
        if (sparkSessionProvider == null) {
            sparkSessionProvider = new SparkSessionProviderHeavy();
            sparkSessionProvider.initializeSession();
        }
        return sparkSessionProvider;
    }
}
