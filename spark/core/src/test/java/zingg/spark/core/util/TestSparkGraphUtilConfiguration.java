package zingg.spark.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestSparkGraphUtilConfiguration {

    @Test
    public void connectedComponentsAlgorithmIsExplicitlyConfigured() {
        assertEquals("two_phase", SparkGraphUtil.CONNECTED_COMPONENTS_ALGORITHM);
    }
}
