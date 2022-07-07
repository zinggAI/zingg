package zingg.hash;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;

public class TestGetAs extends ZinggSparkTester {

	private static Dataset<Row> dfWithNulls;
	private static Dataset<Row> dfWithoutNulls;

	@BeforeAll
	public static void setupGetAs() {
		dfWithNulls = createDFWithSampleNumerics(true);
		dfWithoutNulls = createDFWithSampleNumerics(false);
	}

	/*test values: 0.5 gbp/gbp<blank> etc. and that are expected to be converted to nulls*/
	@Test
	public void testGetAsForDoubleType() {
		List<Row> rows = dfWithNulls.collectAsList();
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

	/*test values: 5 gbp/gbp/<blank> etc. are expected to be converted to nulls*/
	@Test
	public void testGetAsForIntegerType() {
		List<Row> rows = dfWithNulls.collectAsList();
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

	/*test values: 0.5 gbp/gbp<blank> etc. that are expected to be converted to 0.0*/
	@Test
	public void testGetAsForDoubleTypeWithoutNulls() {
		List<Row> rows = dfWithoutNulls.collectAsList();
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

	/*test values: 5 gbp/gbp/<blank> etc. that are expected to be converted to 0.0*/
	@Test
	public void testGetAsForIntegerTypeWithoutNulls() {
		List<Row> rows = dfWithoutNulls.collectAsList();
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

	private static Dataset<Row> createDFWithSampleNumerics(boolean nullable) {
		StructType schema = new StructType(new StructField[] {
				new StructField(FIELD_DOUBLE, DataTypes.DoubleType, nullable, Metadata.empty()),
				new StructField(FIELD_INTEGER, DataTypes.IntegerType, nullable, Metadata.empty())
		});
		String a[] = new String[] { 
			"0.55,55",
			"1.234,1234",
			"34,gbp",
			"99.56,9956",
			"56gbp,56",
			"23,23gbp",
			",45",
			"65,",
			",",
			"0.5 gbp,23",
			"56.00,56",
			"$,64.0",
			"null,34",
			"78,null",
			"78,87",
		};
		Dataset<String> dsStr = spark.createDataset(Arrays.asList(a), Encoders.STRING());
		Dataset<Row> df = spark.read().schema(schema).csv(dsStr);
		return df;
	}
}
