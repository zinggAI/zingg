package zingg.spark.core.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.SparkSession;

import zingg.common.core.util.TestType;

public class SparkVerifyBlockingCleanUpUtil extends SparkCleanUpUtil{

    private static final String JUNIT_DIR = "/tmp/zingg";
    private static final String JUNIT_VB = "junit_vb";

    private static SparkVerifyBlockingCleanUpUtil sparkVerifyBlockingCleanUpUtil = null;

    protected SparkVerifyBlockingCleanUpUtil() {
        super();
    }

    @Override
    public boolean performCleanup(SparkSession session, TestType testType, String modelId) {
        try {
            File dir = new File(JUNIT_DIR + "/" + JUNIT_VB);
            if(dir.exists()){
                FileUtils.forceDelete(dir);
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static SparkVerifyBlockingCleanUpUtil getInstance() {
        if (sparkVerifyBlockingCleanUpUtil == null) {
            sparkVerifyBlockingCleanUpUtil = new SparkVerifyBlockingCleanUpUtil();
        }
        return sparkVerifyBlockingCleanUpUtil;
    }
    
}
