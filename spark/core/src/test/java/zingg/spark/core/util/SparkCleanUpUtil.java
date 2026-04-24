package zingg.spark.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

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
            Path path = Paths.get(JUNIT_DIR, suffix);

            if (!Files.exists(path)) {
                return true;
            }

            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            }

            return true;

        } catch (IOException | UncheckedIOException e) {
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
