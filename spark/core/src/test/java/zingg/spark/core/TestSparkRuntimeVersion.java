package zingg.spark.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Checks that the tests run on the Spark and Scala versions of the selected
 * build profile (-Dspark=3.5 / 4.1 / 4.2), so a mis-resolved classpath fails
 * here instead of passing silently. The expected versions are passed in by
 * surefire; when absent (e.g. running from an IDE) the checks are skipped.
 */
@ExtendWith(TestSparkBaseLite.class)
public class TestSparkRuntimeVersion {

	public static final String EXPECTED_SPARK = "zingg.expected.spark.binary.version";
	public static final String EXPECTED_SCALA = "zingg.expected.scala.binary.version";

	private final SparkSession sparkSession;

	public TestSparkRuntimeVersion(SparkSession sparkSession) {
		this.sparkSession = sparkSession;
	}

	@Test
	public void testSparkVersionMatchesProfile() {
		String expected = System.getProperty(EXPECTED_SPARK);
		assumeTrue(expected != null && !expected.isEmpty(), EXPECTED_SPARK + " not set");
		String actual = sparkSession.version();
		assertTrue(actual.startsWith(expected + "."),
				"Spark runtime " + actual + " does not match build profile " + expected);
	}

	@Test
	public void testScalaVersionMatchesProfile() {
		String expected = System.getProperty(EXPECTED_SCALA);
		assumeTrue(expected != null && !expected.isEmpty(), EXPECTED_SCALA + " not set");
		String actual = scala.util.Properties.versionNumberString();
		assertTrue(actual.startsWith(expected + "."),
				"Scala runtime " + actual + " does not match build profile " + expected);
	}
}
