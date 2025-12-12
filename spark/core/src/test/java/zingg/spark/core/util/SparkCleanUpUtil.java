package zingg.spark.core.util;

import java.io.File;

import org.apache.spark.sql.SparkSession;

import zingg.common.core.util.ICleanUpUtil;
import zingg.common.core.util.TestType;

public class SparkCleanUpUtil implements ICleanUpUtil<SparkSession> {
    private static final String JUNIT_DIR = "/tmp/junit_integration_spark";
    private static final String SINGLE_TEST_DIR = "single";
    private static final String COMPOUND_TEST_DIR = "compound";
    private static SparkCleanUpUtil sparkCleanUpUtil = null;

    protected SparkCleanUpUtil() {

    }

    @Override
    public boolean performCleanup(SparkSession session, TestType testType, String modelId) {
        try {
            String suffix = TestType.SINGLE.equals(testType) ? SINGLE_TEST_DIR : COMPOUND_TEST_DIR;
            File dir = new File(JUNIT_DIR + "/" + suffix);
            /* force delete since we want to make sure
            * dir gets deleted even if it is non-empty*/
            dir.delete();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static SparkCleanUpUtil getInstance() {
        if (sparkCleanUpUtil == null) {
            sparkCleanUpUtil = new SparkCleanUpUtil();
        }
        return sparkCleanUpUtil;
    }
}
