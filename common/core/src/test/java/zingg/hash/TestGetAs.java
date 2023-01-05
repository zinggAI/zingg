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
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;

public class TestGetAs extends ZinggSparkTester {

	Dataset<Row> df;
	List<Row> rows;
	StructType schema = new StructType();

	/*test values: 0.5 gbp/gbp<blank> etc. and that are expected to be converted to nulls*/
	@Test
	public void testGetAsForDoubleType() {
		String a[] = new String[] { 
			"$,64.0",
			"0.5 gbp,23",
			"56.00,56",
		};
		StructType schema = new StructType(new StructField[] {
			new StructField(FIELD_DOUBLE, DataTypes.DoubleType, true, Metadata.empty())
		});
		List<Row> rows = createDFWithSampleNumerics(a, schema);
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
		String a[] = new String[] { 
			"56gbp,56",
			"78,null",
			"78,87",
		};
		StructType schema = new StructType(new StructField[] {
			new StructField(FIELD_INTEGER, DataTypes.IntegerType, true, Metadata.empty())
		});
		List<Row> rows = createDFWithSampleNumerics(a, schema);
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
		String a[] = new String[] { 
			"$,64.0",
			"0.5 gbp,23",
			"56.00,56",
		};
		StructType schema = new StructType(new StructField[] {
			new StructField(FIELD_DOUBLE, DataTypes.DoubleType, false, Metadata.empty())
		});
		List<Row> rows = createDFWithSampleNumerics(a, schema);
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
		String a[] = new String[] { 
			"56gbp,56",
			"78,null",
			"78,87",
		};
		StructType schema = new StructType(new StructField[] {
			new StructField(FIELD_INTEGER, DataTypes.IntegerType, false, Metadata.empty())
		});
		List<Row> rows = createDFWithSampleNumerics(a, schema);
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


	private static List<Row> createDFWithSampleNumerics(String a[], StructType schema) {
		Dataset<String> dsStr = spark.createDataset(Arrays.asList(a), Encoders.STRING());
		Dataset<Row> df = spark.read().schema(schema).csv(dsStr);
		List<Row> row = df.collectAsList();
		return row;
	}
}
