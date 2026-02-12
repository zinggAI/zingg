package zingg.spark.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SparkSession;

import zingg.common.core.util.ICleanUpUtil;
import zingg.common.core.util.TestType;

public class SparkCleanUpUtil implements ICleanUpUtil<SparkSession> {
    public static final Log LOG = LogFactory.getLog(SparkCleanUpUtil.class);
    private static final String JUNIT_DIR = "/tmp/junit_integration_spark";
    private static final String SINGLE_TEST_DIR = "single";
    private static final String COMPOUND_TEST_DIR = "compound";
    private static SparkCleanUpUtil sparkCleanUpUtil = null;

    protected SparkCleanUpUtil() {

    }

    public static void cleanUpBeforeTest() {
        cleanUp(JUNIT_DIR);
    }

    @Override
    public boolean performCleanup(SparkSession session, TestType testType, String modelId) {
        try {
            String suffix = TestType.SINGLE.equals(testType) ? SINGLE_TEST_DIR : COMPOUND_TEST_DIR;
            String path = JUNIT_DIR + "/" + suffix;
            File dir = new File(path);

            if (!dir.exists()) {
                LOG.info("Cleanup not needed. Directory does not exist: " + path);
                return true;
            }

            LOG.info("Starting cleanup for directory: " + path);
            deleteRecursively(dir.toPath());
            LOG.info("Successfully cleaned up directory: " + path);
            return true;
        } catch (Exception exception) {
            String suffix = TestType.SINGLE.equals(testType) ? SINGLE_TEST_DIR : COMPOUND_TEST_DIR;
            String path = JUNIT_DIR + "/" + suffix;
            LOG.error("Failed to clean up directory: " + path, exception);
            return false;
        }
    }

    private static void cleanUp(String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.exists()) {
            try {
                LOG.info("Starting cleanup for directory: " + directoryPath);
                deleteRecursively(dir.toPath());
                LOG.info("Successfully cleaned up directory: " + directoryPath);
            } catch (IOException e) {
                LOG.error("Failed to clean up directory: " + directoryPath, e);
            }
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static SparkCleanUpUtil getInstance() {
        if (sparkCleanUpUtil == null) {
            sparkCleanUpUtil = new SparkCleanUpUtil();
        }
        return sparkCleanUpUtil;
    }
}
