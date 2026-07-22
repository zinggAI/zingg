package zingg.spark.core.session;

public class SparkSessionProviderHeavy extends SparkSessionProviderBase {

    @Override
    protected String getPropertiesFile() {
        return "/zingg-heavy.properties";
    }
}
