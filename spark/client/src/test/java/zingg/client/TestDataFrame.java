package zingg.client;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.junit.jupiter.api.Test;
import zingg.client.helper.DFObjectUtil;
import zingg.client.schema.Schema;
import zingg.client.schema.SchemaWithMixedDataType;
import zingg.client.utility.Constant;
import zingg.client.utility.PojoToArrayConverter;
import zingg.common.client.ZFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.apache.spark.sql.functions.col;
import static org.junit.jupiter.api.Assertions.*;

public class TestDataFrame<D, R, C> {

    public static final String NEW_COLUMN = "newColumn";
    public static final String STR_RECID = "recid";
    public DFObjectUtil dfObjectUtil;


    public void setDfObjectUtil(DFObjectUtil dfObjectUtil){
        this.dfObjectUtil = dfObjectUtil;
    }


    @Test
    public void testCreateSparkDataFrameAndGetDF() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList();

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        //assert rows
        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.collectAsList();
        for(int idx = 0; idx < sampleDataSet.size(); idx++) {
            assertArrayEquals(pojoList.get(idx).values(), PojoToArrayConverter.getObjectArray(sampleDataSet.get(idx)));
        }
    }

    @Test
    public void testColumnsNamesandCount() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList();

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        //assert on fields
        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        Arrays.stream(zFrame.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        assertEquals(fieldsInTestData, fieldsInZFrame, "Columns of sample data and zFrame are not equal");
    }

    @Test
    public void testSelectWithSingleColumnName() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String colName = "recid";
        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.select(colName).collectAsList();

        for (int idx = 0; idx < sampleDataSet.size(); idx++){
            assertEquals(pojoList.get(idx).values()[0], sampleDataSet.get(idx).recid, "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testSelectWithColumnList() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>

        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        List<Column> columnList = Arrays.asList(col("recid"), col("surname"), col("postcode"));

        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.select(columnList).collectAsList();

        for(int idx = 0; idx < sampleDataSet.size(); idx++) {
            assertEquals(pojoList.get(idx).values()[0], sampleDataSet.get(idx).recid, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[1], sampleDataSet.get(idx).surname, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[2], sampleDataSet.get(idx).postcode, "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testSelectWithColumnArray() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        Column[] columnArray = new Column[] { col("recid"), col("surname"), col("postcode") };

        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.select(columnArray).collectAsList();

        for(int idx = 0; idx < sampleDataSet.size(); idx++) {
            assertEquals(pojoList.get(idx).values()[0], sampleDataSet.get(idx).recid, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[1], sampleDataSet.get(idx).surname, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[2], sampleDataSet.get(idx).postcode, "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testSelectWithMultipleColumnNamesAsString() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.select("recid", "surname", "postcode").collectAsList();

        for(int idx = 0; idx < sampleDataSet.size(); idx++) {
            assertEquals(pojoList.get(idx).values()[0], sampleDataSet.get(idx).recid, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[1], sampleDataSet.get(idx).surname, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[2], sampleDataSet.get(idx).postcode, "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testSelectExprByPassingColumnStringsAsInSQLStatement() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.selectExpr("recid as RecordId", "surname as FamilyName",
                "postcode as Pin").collectAsList();

        for(int idx = 0; idx < sampleDataSet.size(); idx++) {
            assertEquals(pojoList.get(idx).values()[0], sampleDataSet.get(idx).recid, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[1], sampleDataSet.get(idx).surname, "value from zFrame and sampleData doesn't match");
            assertEquals(pojoList.get(idx).values()[2], sampleDataSet.get(idx).postcode, "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testDropSingleColumn() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<String> fieldsInZFrame = new ArrayList<>();
        List<String> fieldsInTestData = new ArrayList<>();
        Arrays.stream(zFrame.drop("recid").fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.remove("recid");

        assertEquals(fieldsInTestData, fieldsInZFrame, "Fields in zFrame and sample data doesn't match");
    }

    @Test
    public void testDropColumnsAsStringArray() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<String> fieldsInZFrame = new ArrayList<>();
        List<String> fieldsInTestData = new ArrayList<>();
        Arrays.stream(zFrame.drop("recid", "surname", "postcode").fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.remove("recid");
        fieldsInTestData.remove("surname");
        fieldsInTestData.remove("postcode");

        assertEquals(fieldsInTestData, fieldsInZFrame, "Fields in zFrame and sample data doesn't match");
    }

    @Test
    public void testLimit() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        int len = 5;
        List<GenericRowWithSchema> pojoList = (List<GenericRowWithSchema>) zFrame.limit(len).collectAsList();

        assertEquals(pojoList.size(), len, "Size is not equal");

        //assert on rows
        for(int idx = 0; idx < len; idx++) {
            assertArrayEquals(pojoList.get(idx).values(), PojoToArrayConverter.getObjectArray(sampleDataSet.get(idx)));
        }
    }

    @Test
    public void testHead() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        GenericRowWithSchema row = (GenericRowWithSchema) zFrame.head();

        assertArrayEquals(row.values(), PojoToArrayConverter.getObjectArray(sampleDataSet.get(0)),
                "Top row from zFrame and sample data doesn't match");
    }

    @Test
    public void testGetAsInt() throws Exception {
        List<SchemaWithMixedDataType> sampleDataSet = Constant.createSampleDataListWithMixedDataType(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaWithMixedDataType.class);

        R row = zFrame.head();
        assertTrue(zFrame.getAsInt(row, "recid") == sampleDataSet.get(0).recid,
                "row.getAsInt(col) hasn't returned correct int value");
    }

    @Test
    public void testGetAsString() throws Exception {
        List<SchemaWithMixedDataType> sampleDataSet = Constant.createSampleDataListWithMixedDataType(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaWithMixedDataType.class);

        R row = zFrame.head();
        assertEquals(zFrame.getAsString(row, "surname"), sampleDataSet.get(0).surname, "row.getAsString(col) hasn't returned correct string value");
    }

    @Test
    public void testGetAsDouble() throws Exception {
        List<SchemaWithMixedDataType> sampleDataSet = Constant.createSampleDataListWithMixedDataType(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaWithMixedDataType.class);

        R row = zFrame.head();
        assertEquals(zFrame.getAsDouble(row, "cost"), sampleDataSet.get(0).cost, "row.getAsDouble(col) hasn't returned correct double value");
    }

    @Test
    public void testWithColumnForIntegerValue() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        String newCol = NEW_COLUMN;
        int newColVal = 36;
        ZFrame<D, R, Column> zFrameWithAddedColumn = zFrame.withColumn(newCol, newColVal);

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame, "Columns of sample data and zFrame are not equal");

        //Assert on first row
        GenericRowWithSchema row = (GenericRowWithSchema) zFrameWithAddedColumn.head();
        assertEquals(row.getAs(newCol), Integer.valueOf(newColVal), "value of added column is not as expected");
    }

    @Test
    public void testWithColumnForDoubleValue() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String newCol = NEW_COLUMN;
        double newColVal = 3.14;
        ZFrame<D, R, Column> zFrameWithAddedColumn = zFrame.withColumn(newCol, newColVal);

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame, "Columns of sample data and zFrame are not equal");

        //Assert on first row
        GenericRowWithSchema row = (GenericRowWithSchema) zFrameWithAddedColumn.head();
        assertEquals(row.getAs(newCol), Double.valueOf(newColVal), "value of added column is not as expected");
    }

    @Test
    public void testWithColumnforStringValue() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String newCol = NEW_COLUMN;
        String newColVal = "zingg";
        ZFrame<D, R, Column> zFrameWithAddedColumn = zFrame.withColumn(newCol, newColVal);

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame, "Columns of sample data and zFrame are not equal");

        //Assert on first row
        GenericRowWithSchema row = (GenericRowWithSchema) zFrameWithAddedColumn.head();
        assertEquals(row.getAs(newCol), newColVal, "value of added column is not as expected");
    }

    @Test
    public void testWithColumnforAnotherColumn() throws Exception {
        List<Schema> sampleDataSet = Constant.createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, Column> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String oldCol = STR_RECID;
        String newCol = NEW_COLUMN;
        ZFrame<D, R, Column> zFrameWithAddedColumn = zFrame.withColumn(newCol, col(oldCol));

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame, "Columns of sample data and zFrame are not equal");

        //Assert on first row
        GenericRowWithSchema row = (GenericRowWithSchema) zFrameWithAddedColumn.head();
        assertEquals(Optional.of(row.getAs(newCol)), Optional.of(row.getAs(oldCol)), "value of added column is not as expected");
    }
}
