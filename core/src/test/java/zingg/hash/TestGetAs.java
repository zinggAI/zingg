package zingg.hash;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;

public class TestGetAs extends ZinggSparkTester {

	Dataset<Row> df;

	@BeforeEach
	private void setupGetAs() {
		df = createDFWithSampleNumerics();
	}

	/*test values: 0.5 gbp/gbp<blank> etc.*/
	@Test
	public void testGetAsForDoubleType() {
		List<Row> rows = df.collectAsList();
		int index = 0;
		try {
			Object obj;
			for (Row row : rows) {
				++index;
				obj = (Double) row.getAs(FIELD_DOUBLE);
			}
		} catch (NullPointerException e) {
			fail("NullPointerException Caught for DoubleType value at row number: " + index);
		}
	}

	/*test values: 5 gbp/gbp/<blank> etc. */
	@Test
	public void testGetAsForIntegerType() {
		List<Row> rows = df.collectAsList();
		int index = 0;
		try {
			Object obj;
			for (Row row : rows) {
				++index;
				obj = (Integer) row.getAs(FIELD_INTEGER);
			}
		} catch (NullPointerException e) {
			fail("NullPointerException Caught for IntegerType value at row number: " + index);
		}
	}
}
