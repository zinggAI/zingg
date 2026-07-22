package zingg.spark.core.session;

public class SparkSessionProviderLite extends SparkSessionProviderBase {

    @Override
    protected String getPropertiesFile() {
        return "/zingg-lite.properties";
    }
}
