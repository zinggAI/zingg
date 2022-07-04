package zingg.hash;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;

public class TestGetAs extends ZinggSparkTester {

	private static final String FIELD_INTEGER = "fieldInteger";
	private static final String FIELD_DOUBLE = "fieldDouble";
	Dataset<Row> df;

	@BeforeEach
	private void setupGetAs() {
		String filePath = getClass().getResource("/hash/testHash.csv").getFile();
		// String filePath =
		StructType schema = new StructType(new StructField[] {
				new StructField(FIELD_DOUBLE, DataTypes.DoubleType, false, Metadata.empty()),
				new StructField(FIELD_INTEGER, DataTypes.IntegerType, false, Metadata.empty())
		});
		df = spark.read().format("csv").schema(schema).load(filePath);
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
