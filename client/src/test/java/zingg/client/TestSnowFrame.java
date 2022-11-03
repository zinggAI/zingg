package zingg.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.Row;


public class TestSnowFrame extends TestSnowFrameBase {
	public static final Log LOG = LogFactory.getLog(TestSnowFrame.class);

	public static final String NEW_COLUMN = "newColumn";

	@Test
	public void testCreateSnowDataFrameAndGetDF() {
		SnowFrame sf = new SnowFrame(createSampleDataset());
		DataFrame df = sf.df();
		assertTrue(new SnowFrame(df.except(createSampleDataset())).isEmpty(), "Two datasets are not equal");
	}

	@Test
	public void testColumnsNamesandCount() {
		SnowFrame sf = new SnowFrame(createSampleDataset());
		assertTrue(Arrays.equals(sf.columns(), createSampleDataset().schema().names()),
				"Columns of SparkFrame and the dataset are not equal");
	}

	@Test
	public void testSelectWithSingleColumnName() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		String colName = "recid";
		ZFrame<DataFrame, Row, Column> sf2 = sf.select(colName);
		SnowFrame sf3 = new SnowFrame(df.select(colName));
		assertTrueCheckingExceptOutput(sf2, sf3, "SnowFrame.select(colName) does not have expected value");
	}

	@Test
	public void testSelectWithColumnList() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		List<Column> columnList = Arrays.asList(df.col("recid"), df.col("surname"), df.col("postcode"));
		ZFrame<DataFrame, Row, Column> sf2 = sf.select(columnList);
		Column[] arrOfCols = columnList.toArray(new Column[columnList.size()]);
		SnowFrame sf3 = new SnowFrame(df.select(arrOfCols));
		assertTrueCheckingExceptOutput(sf2, sf3, "SnowFrame.select(columnList) does not have expected value");
	}

	@Test
	public void testSelectWithColumnArray() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		Column[] columnArray = new Column[] {df.col("recid"), df.col("surname"), df.col("postcode")};
		ZFrame<DataFrame, Row, Column> sf2 = sf.select(columnArray);
		SnowFrame sf3 = new SnowFrame(df.select(columnArray));
		assertTrueCheckingExceptOutput(sf2, sf3, "SnowFrame.select(columnArray) value does not match with standard select output");
	}

	@Test
	public void testSelectWithMultipleColumnNamesAsString() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		ZFrame<DataFrame, Row, Column> sf2 = sf.select("recid","surname","postcode");
		SnowFrame sf3 = new SnowFrame(df.select("recid",  "surname",  "postcode"));
		assertTrueCheckingExceptOutput(sf2, sf3, "SnowFrame.select(str1, str2, ...) value does not match with standard select output");
	}

	@Test
	public void testSelectExprByPassingColumnStringsAsInSQLStatement() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		ZFrame<DataFrame, Row, Column> sf2 = sf.selectExpr("recid as RecordId",  "surname as FamilyName",  "postcode as Pin");
 		SnowFrame sf3 = new SnowFrame(df.select("recid",  "surname",  "postcode"));
 		assertTrueCheckingExceptOutput(sf2, sf3, "SnowFrame.selectExpr(str1, str2, ...) value does not match with standard selectExpr output");
	}

	@Test
	public void testDistinct() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
 		SnowFrame sf2 = new SnowFrame(df.distinct());
		 assertTrueCheckingExceptOutput(sf.distinct(), sf2, "SnowFrame.distict() does not match with standard distict() output");
	}

	@Test
	public void testDropSingleColumn() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		ZFrame<DataFrame, Row, Column> sf2 = new SnowFrame(df.drop("recid"));
		assertTrueCheckingExceptOutput(sf2, sf.drop("recid"), "SnowFrame.drop(str) does not match with standard drop() output");
	}

	@Test
	public void testDropColumnsAsStringArray() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		ZFrame<DataFrame, Row, Column> sf2 = new SnowFrame(df.drop("recid",  "surname",  "postcode"));
		assertTrueCheckingExceptOutput(sf2, sf.drop("recid", "surname", "postcode"), "SnowFrame.drop(str...) does not match with standard drop(str...) output");
	}

	@Test
	public void testLimit() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		int len = 5;
		ZFrame<DataFrame, Row, Column> sf2 = sf.limit(len);
		assertTrue(sf2.count() == len);
		assertTrueCheckingExceptOutput(sf2, sf.limit(len), "SnowFrame.limit(len) does not match with standard limit(len) output");
	}

	@Test
	public void testDropDuplicatesConsideringGivenColumnsAsStringArray() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		String[] columnArray = new String[] {"surname", "postcode"};
		ZFrame<DataFrame, Row, Column> sf2 = new SnowFrame(df.dropDuplicates(columnArray));
		assertTrueCheckingExceptOutput(sf2, sf.dropDuplicates(columnArray), "SnowFrame.dropDuplicates(str[]) does not match with standard dropDuplicates(str[]) output");
	}

	@Test
	public void testDropDuplicatesConsideringGivenIndividualColumnsAsString() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		ZFrame<DataFrame, Row, Column> sf2 = new SnowFrame(df.dropDuplicates("surname", "postcode"));
		assertTrueCheckingExceptOutput(sf2, sf.dropDuplicates("surname"), "SnowFrame.dropDuplicates(col1, col2) does not match with standard dropDuplicates(col1, col2) output");
	}

	@Test
	public void testHead() {
		DataFrame df = createSampleDataset();
		SnowFrame sf = new SnowFrame(df);
		Row row = sf.head();
		assertTrue(row.equals(df.collect()[0]), "Top Row is not the expected one");
	 }

	@Test
	public void testIsEmpty() {
		DataFrame df = createEmptyDataFrame();
		SnowFrame sf = new SnowFrame(df);
		assertTrue(sf.isEmpty(), "DataFrame is not empty");
	 }

	 @Test
	public void testGetAsInt() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		Row row = sf.head();
		LOG.debug("Value: " + row.getInt(getColIndex(sf, "recid")));
		assertTrue(sf.getAsInt(row, "recid") == row.getInt(getColIndex(sf, "recid")), "row.getAsInt(col) hasn't returned correct int value");
	}

	@Test
	public void testGetAsString() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		Row row = sf.head();
		LOG.debug("Value: " + row.getString(getColIndex(sf, "recid")));
		assertTrue(sf.getAsString(row, "surname").equals(row.getString(getColIndex(sf, "recid")).toString()), "row.getAsString(col) hasn't returned correct string value");
	}

	@Test
	public void testGetAsDouble() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		Row row = sf.head();
		LOG.debug("Value: " + row.getDouble(getColIndex(sf, "recid")));
		assertTrue(sf.getAsDouble(row, "cost") == row.getDouble(getColIndex(sf, "recid")), "row.getAsDouble(col) hasn't returned correct double value");
	}

	@Test
	public void testSortDescending() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		String col = STR_RECID;
		ZFrame<DataFrame,Row,Column> sf2 = sf.sortDescending(col);
		assertTrueCheckingExceptOutput(sf2, df.sort(df.col(col).desc()), "SnowFrame.sortDescending() output is not as expected");
	}
	
	@Test
	public void testSortAscending() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		String col = STR_RECID;
		ZFrame<DataFrame,Row,Column> sf2 = sf.sortAscending(col);
		assertTrueCheckingExceptOutput(sf2, df.sort(df.col(col).asc()), "SnowFrame.sortAscending() output is not as expected");
	}

	@Test
	public void testWithColumnforIntegerValue() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		String newCol = NEW_COLUMN;
		int newColVal = 36;
		ZFrame<DataFrame,Row,Column> sf2 = sf.withColumn(newCol, newColVal);
 		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, Functions.lit(newColVal)), "SnowFrame.withColumn(c, int) output is not as expected");
	}

	@Test
	public void testWithColumnforDoubleValue() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		String newCol = NEW_COLUMN;
		double newColVal = 3.14;
		ZFrame<DataFrame,Row,Column> sf2 = sf.withColumn(newCol, newColVal);
 		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, Functions.lit(newColVal)), "SnowFrame.withColumn(c, double) output is not as expected");
	}

	@Test
	public void testWithColumnforStringValue() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		String newCol = NEW_COLUMN;
		String newColVal = "zingg";
		ZFrame<DataFrame,Row,Column> sf2 = sf.withColumn(newCol, newColVal);
 		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, Functions.lit(newColVal)), "SnowFrame.withColumn(c, String) output is not as expected");
	}

	@Test
	public void testWithColumnforAnotherColumn() {
		DataFrame df = createSampleDatasetHavingMixedDataTypes();
		SnowFrame sf = new SnowFrame(df);
		String oldCol = STR_RECID;
		String newCol = NEW_COLUMN;
		ZFrame<DataFrame,Row,Column> sf2 = sf.withColumn(newCol, df.col(oldCol));
  		assertTrueCheckingExceptOutput(sf2, df.withColumn(newCol, df.col(oldCol)), "SnowFrame.withColumn(c, Column) output is not as expected");
	}

	public int getColIndex(SnowFrame sf, String colName){
        String[] colNames = sf.df().schema().names();
        int index = -1;
        for (int idx=0;idx<colNames.length;++idx){
            if (colNames[idx].equalsIgnoreCase(colName)){
                index = idx;
                break;
            }
        }
        return index;
    }
}