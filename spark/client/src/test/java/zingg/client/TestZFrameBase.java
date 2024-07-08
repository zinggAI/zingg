package zingg.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.ZinggException;
import zingg.common.client.schema.Schema;
import zingg.common.client.schema.SchemaWithMixedDataType;
import zingg.common.client.schema.SchemaZScore;
import zingg.common.client.schema.SchemaInput;
import zingg.common.client.schema.SchemaCluster;
import zingg.common.client.schema.SchemaClusterNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static zingg.common.client.data.Constant.createEmptySampleData;
import static zingg.common.client.data.Constant.createSampleDataCluster;
import static zingg.common.client.data.Constant.createSampleDataClusterWithNull;
import static zingg.common.client.data.Constant.createSampleDataInput;
import static zingg.common.client.data.Constant.createSampleDataList;
import static zingg.common.client.data.Constant.createSampleDataListDistinct;
import static zingg.common.client.data.Constant.createSampleDataListWithDistinctSurnameAndPostcode;
import static zingg.common.client.data.Constant.createSampleDataListWithMixedDataType;
import static zingg.common.client.data.Constant.createSampleDataZScore;

public abstract class TestZFrameBase<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TestZFrameBase.class);
    public static final String NEW_COLUMN = "newColumn";
    public static final String STR_RECID = "recid";
    private final DFObjectUtil<S, D, R, C> dfObjectUtil;

    public TestZFrameBase(DFObjectUtil<S, D, R, C> dfObjectUtil) {
        this.dfObjectUtil = dfObjectUtil;
    }


    @Test
    public void testCreateSparkDataFrameAndGetDF() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList();

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        //assert rows
        List<R> rows = zFrame.collectAsList();
        List<Field> fields = List.of(Schema.class.getDeclaredFields());
        for (int idx = 0; idx < sampleDataSet.size(); idx++) {
            R row = rows.get(idx);
            for (Field column : fields) {
                String columnName  = column.getName();
                assertEquals(column.get(sampleDataSet.get(idx)).toString(), zFrame.getAsString(row, columnName),
                        "value in ZFrame and sample input is not same");
            }
        }
    }

    @Test
    public void testColumnsNamesandCount() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList();

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        //assert on fields
        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        Arrays.stream(zFrame.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        assertEquals(fieldsInTestData, fieldsInZFrame,
                "Columns of sample data and zFrame are not equal");
    }

    @Test
    public void testSelectWithSingleColumnName() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String colName = "recid";
        List<R> rows = zFrame.select(colName).collectAsList();
        for (int idx = 0; idx < sampleDataSet.size(); idx++) {
            R row = rows.get(idx);
            assertEquals(sampleDataSet.get(idx).recid, zFrame.getAsString(row, colName),
                    "value in ZFrame and sample input is not same");
        }
    }

    /*
        list of string can not be cast to list of C
        zFrame select does not have an interface method for List<String>
    */
    @Disabled
    @Test
    public void testSelectWithColumnList() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>

        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<C> columnList = (List<C>) Arrays.asList("recid", "surname", "postcode");
        List<R> rows = zFrame.select(columnList).collectAsList();

        for (int idx = 0; idx < sampleDataSet.size(); idx++) {
            R row = rows.get(idx);
            Assertions.assertEquals(zFrame.getAsString(row, "recid"), sampleDataSet.get(idx).recid,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "surname"), sampleDataSet.get(idx).surname,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "postcode"), sampleDataSet.get(idx).postcode,
                    "value from zFrame and sampleData doesn't match");
        }
    }

    /*
        string can not be cast to C
        zFrame doesn't have an interface method for C[]
     */
    @Disabled
    @Test
    public void testSelectWithColumnArray() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        C[] columnArray = (C[]) new Object[3];
        columnArray[0] = (C) "recid";
        columnArray[1] = (C) "surname";
        columnArray[2] = (C) "postcode";

        List<R> rows = zFrame.select(columnArray).collectAsList();

        for (int idx = 0; idx < sampleDataSet.size(); idx++) {
            R row = rows.get(idx);
            Assertions.assertEquals(zFrame.getAsString(row, "recid"), sampleDataSet.get(idx).recid,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "surname"), sampleDataSet.get(idx).surname,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "postcode"), sampleDataSet.get(idx).postcode,
                    "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testSelectWithMultipleColumnNamesAsString() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<R> rows = zFrame.select("recid", "surname", "postcode").collectAsList();

        for (int idx = 0; idx < sampleDataSet.size(); idx++) {
            R row = rows.get(idx);
            Assertions.assertEquals(zFrame.getAsString(row, "recid"), sampleDataSet.get(idx).recid,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "surname"), sampleDataSet.get(idx).surname,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "postcode"), sampleDataSet.get(idx).postcode,
                    "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testSelectExprByPassingColumnStringsAsInSQLStatement() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<R> rows = zFrame.selectExpr("recid as RecordId", "surname as FamilyName",
                "postcode as Pin").collectAsList();

        for (int idx = 0; idx < sampleDataSet.size(); idx++) {
            R row = rows.get(idx);
            Assertions.assertEquals(zFrame.getAsString(row, "RecordId"), sampleDataSet.get(idx).recid,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "FamilyName"), sampleDataSet.get(idx).surname,
                    "value from zFrame and sampleData doesn't match");
            Assertions.assertEquals(zFrame.getAsString(row, "Pin"), sampleDataSet.get(idx).postcode,
                    "value from zFrame and sampleData doesn't match");
        }
    }

    @Test
    public void testDropSingleColumn() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<String> fieldsInZFrame = new ArrayList<>();
        List<String> fieldsInTestData = new ArrayList<>();
        Arrays.stream(zFrame.drop("recid").fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.remove("recid");

        assertEquals(fieldsInTestData, fieldsInZFrame, "Fields in zFrame and sample data doesn't match");
    }

    @Test
    public void testDropColumnsAsStringArray() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        List<String> fieldsInZFrame = new ArrayList<>();
        List<String> fieldsInTestData = new ArrayList<>();
        Arrays.stream(zFrame.drop("recid", "surname", "postcode").fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.remove("recid");
        fieldsInTestData.remove("surname");
        fieldsInTestData.remove("postcode");

        assertEquals(fieldsInTestData, fieldsInZFrame,
                "Fields in zFrame and sample data doesn't match");
    }

    @Test
    public void testLimit() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        int len = 5;
        List<R> rows = zFrame.limit(len).collectAsList();

        assertEquals(rows.size(), len, "Size is not equal");

        //assert on rows
        List<Field> fields = List.of(Schema.class.getDeclaredFields());
        for (int idx = 0; idx < len; idx++) {
            R row = rows.get(idx);
            for (Field column : fields) {
                String columnName  = column.getName();
                assertEquals(column.get(sampleDataSet.get(idx)).toString(), zFrame.getAsString(row, columnName),
                        "value in ZFrame and sample input is not same");
            }
        }
    }

    @Test
    public void testHead() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        R row = zFrame.head();
        List<Field> fields = List.of(Schema.class.getDeclaredFields());
        for (Field column : fields) {
            String columnName  = column.getName();
            assertEquals(column.get(sampleDataSet.get(0)).toString(), zFrame.getAsString(row, columnName),
                    "value in ZFrame and sample input is not same");
        }
    }

    @Test
    public void testGetAsInt() throws Exception {
        List<SchemaWithMixedDataType> sampleDataSet = createSampleDataListWithMixedDataType(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaWithMixedDataType.class);

        R row = zFrame.head();
        assertTrue(zFrame.getAsInt(row, "recid") == sampleDataSet.get(0).recid,
                "row.getAsInt(col) hasn't returned correct int value");
    }

    @Test
    public void testGetAsString() throws Exception {
        List<SchemaWithMixedDataType> sampleDataSet = createSampleDataListWithMixedDataType(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaWithMixedDataType.class);

        R row = zFrame.head();
        assertEquals(zFrame.getAsString(row, "surname"), sampleDataSet.get(0).surname,
                "row.getAsString(col) hasn't returned correct string value");
    }

    @Test
    public void testGetAsDouble() throws Exception {
        List<SchemaWithMixedDataType> sampleDataSet = createSampleDataListWithMixedDataType(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaWithMixedDataType.class);

        R row = zFrame.head();
        assertEquals(zFrame.getAsDouble(row, "cost"), sampleDataSet.get(0).cost,
                "row.getAsDouble(col) hasn't returned correct double value");
    }

    @Test
    public void testWithColumnForIntegerValue() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);

        String newCol = NEW_COLUMN;
        int newColVal = 36;
        ZFrame<D, R, C> zFrameWithAddedColumn = zFrame.withColumn(newCol, newColVal);

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame,
                "Columns of sample data and zFrame are not equal");

        //Assert on first row
        R row = zFrameWithAddedColumn.head();
        Assertions.assertEquals(zFrame.getAsInt(row, newCol), Integer.valueOf(newColVal),
                "value of added column is not as expected");
    }

    @Test
    public void testWithColumnForDoubleValue() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String newCol = NEW_COLUMN;
        double newColVal = 3.14;
        ZFrame<D, R, C> zFrameWithAddedColumn = zFrame.withColumn(newCol, newColVal);

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame,
                "Columns of sample data and zFrame are not equal");

        //Assert on first row
        R row = zFrameWithAddedColumn.head();
        Assertions.assertEquals(zFrame.getAsDouble(row, newCol), Double.valueOf(newColVal),
                "value of added column is not as expected");
    }

    @Test
    public void testWithColumnForStringValue() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String newCol = NEW_COLUMN;
        String newColVal = "zingg";
        ZFrame<D, R, C> zFrameWithAddedColumn = zFrame.withColumn(newCol, newColVal);

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame,
                "Columns of sample data and zFrame are not equal");

        //Assert on first row
        R row = zFrameWithAddedColumn.head();
        Assertions.assertEquals(zFrame.getAsString(row, newCol), newColVal,
                "value of added column is not as expected");
    }

    @Test
    public void testWithColumnForAnotherColumn() throws Exception {
        List<Schema> sampleDataSet = createSampleDataList(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, Schema.class);
        String oldCol = STR_RECID;
        String newCol = NEW_COLUMN;
        ZFrame<D, R, C> zFrameWithAddedColumn = zFrame.withColumn(newCol, zFrame.col(oldCol));

        List<String> fieldsInTestData = new ArrayList<>();
        List<String> fieldsInZFrame = new ArrayList<>();
        Arrays.stream(zFrameWithAddedColumn.fields()).iterator().forEachRemaining(fieldZ -> fieldsInZFrame.add(fieldZ.getName()));
        Arrays.stream(Schema.class.getFields()).sequential().forEach(fieldS -> fieldsInTestData.add(fieldS.getName()));
        fieldsInTestData.add(newCol);

        //Assert on columns
        assertEquals(fieldsInTestData, fieldsInZFrame,
                "Columns of sample data and zFrame are not equal");

        //Assert on first row
        R row = zFrameWithAddedColumn.head();
        assertEquals(Optional.of(zFrameWithAddedColumn.getAsString(row, newCol)), Optional.of(zFrameWithAddedColumn.getAsString(row, oldCol)),
                "value of added column is not as expected");
    }

    @Test
    public void testGetMaxVal() throws Exception {
        List<SchemaZScore> sampleDataSet = createSampleDataZScore(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaZScore.class);

        assertEquals(400, zFrame.getMaxVal(ColName.CLUSTER_COLUMN),
                "Max value is not as expected");
    }

    @Test
    public void testGroupByMinMax() throws Exception {
        List<SchemaZScore> sampleDataSet = createSampleDataZScore(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaZScore.class);

        ZFrame<D, R, C> groupByDF = zFrame.groupByMinMaxScore(zFrame.col(ColName.ID_COL));

		List<R> assertionRows = groupByDF.collectAsList();
		for (R row : assertionRows) {
            if (groupByDF.getAsInt(row, "z_zid") == 1) {
                assertEquals(1001, groupByDF.getAsInt(row, "z_minScore"),
                        "z_minScore is not as expected");
                assertEquals(2002, groupByDF.getAsInt(row, "z_maxScore"),
                        "z_maxScore is not as expected");
            }
		}
    }

    @Test
    public void testGroupByMinMax2() throws Exception {
        List<SchemaZScore> sampleDataSet = createSampleDataZScore(); //List<TestPOJO>
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSet, SchemaZScore.class);

        ZFrame<D, R, C> groupByDF = zFrame.groupByMinMaxScore(zFrame.col(ColName.CLUSTER_COLUMN));

		List<R>  assertionRows = groupByDF.collectAsList();
        for (R row : assertionRows) {
            if (groupByDF.getAsInt(row, "z_cluster") == 100) {
                assertEquals(900, groupByDF.getAsInt(row, "z_minScore"),
                        "z_minScore is not as expected");
                assertEquals(9002, groupByDF.getAsInt(row, "z_maxScore"),
                        "z_maxScore is not as expected");
            }
        }
    }

    @Test
    public void testRightJoinMultiCol() throws Exception {
        List<SchemaInput> sampleDataSetInput = createSampleDataInput(); //List<TestPOJO>
        ZFrame<D, R, C> zFrameInput = dfObjectUtil.getDFFromObjectList(sampleDataSetInput, SchemaInput.class);
        List<SchemaCluster> sampleDataSetCluster = createSampleDataCluster(); //List<TestPOJO>
        ZFrame<D, R, C> zFrameCluster = dfObjectUtil.getDFFromObjectList(sampleDataSetCluster, SchemaCluster.class);

        ZFrame<D, R, C> joinedData = zFrameCluster.join(zFrameInput, ColName.ID_COL, ColName.SOURCE_COL, ZFrame.RIGHT_JOIN);
        assertEquals(10, joinedData.count());
    }

    @Test
    public void testFilterInCond() throws Exception {
        List<SchemaInput> sampleDataSetInput = createSampleDataInput(); //List<TestPOJO>
        ZFrame<D, R, C> zFrameInput = dfObjectUtil.getDFFromObjectList(sampleDataSetInput, SchemaInput.class);
        List<SchemaClusterNull> sampleDataSetCluster = createSampleDataClusterWithNull(); //List<TestPOJO>
        ZFrame<D, R, C> zFrameCluster = dfObjectUtil.getDFFromObjectList(sampleDataSetCluster, SchemaClusterNull.class);
        ZFrame<D, R, C> filteredData = zFrameInput.filterInCond(ColName.ID_COL, zFrameCluster, ColName.COL_PREFIX + ColName.ID_COL);
        assertEquals(5, filteredData.count());
    }

    @Test
    public void testFilterNotNullCond() throws Exception {
        List<SchemaClusterNull> sampleDataSetCluster = createSampleDataClusterWithNull(); //List<TestPOJO>
        ZFrame<D, R, C> zFrameCluster = dfObjectUtil.getDFFromObjectList(sampleDataSetCluster, SchemaClusterNull.class);

        ZFrame<D, R, C> filteredData = zFrameCluster.filterNotNullCond(ColName.SOURCE_COL);
        assertEquals(3, filteredData.count());
    }

    @Test
    public void testFilterNullCond() throws Exception {
        List<SchemaClusterNull> sampleDataSetCluster = createSampleDataClusterWithNull(); //List<TestPOJO>
        ZFrame<D, R, C> zFrameCluster = dfObjectUtil.getDFFromObjectList(sampleDataSetCluster, SchemaClusterNull.class);

        ZFrame<D, R, C> filteredData = zFrameCluster.filterNullCond(ColName.SOURCE_COL);
        assertEquals(2, filteredData.count());
    }

    @Test
    public void testDropDuplicatesConsideringGivenColumnsAsStringArray() throws Exception {
        List<Schema> sampleData = createSampleDataList();
        List<Schema> sampleDataWithDistinctSurnameAndPostCode = createSampleDataListWithDistinctSurnameAndPostcode();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleData, Schema.class);

        String[] columnArray = new String[]{"surname", "postcode"};
        ZFrame<D, R, C> zFrameDeDuplicated = zFrame.dropDuplicates(columnArray);

        List<R> rows = zFrameDeDuplicated.collectAsList();

        List<Field> fields = List.of(Schema.class.getDeclaredFields());
        int matchedCount = 0;
        for (Schema schema : sampleDataWithDistinctSurnameAndPostCode) {
            for (R row : rows) {
                boolean rowMatched = true;
                for (Field column : fields) {
                    String columnName = column.getName();
                    if (!column.get(schema).toString().
                            equals(zFrame.getAsString(row, columnName))) {
                        rowMatched = false;
                        break;
                    }
                }
                if (rowMatched) {
                    matchedCount++;
                    break;
                }
            }
        }


        assertEquals(rows.size(), matchedCount,
                "rows count is not as expected");
        assertEquals(sampleDataWithDistinctSurnameAndPostCode.size(), matchedCount,
                "rows count is not as expected");
    }

    @Test
    public void testDropDuplicatesConsideringGivenIndividualColumnsAsString() throws Exception {
        List<Schema> sampleDataSetCluster = createSampleDataList();
        List<Schema> sampleDataWithDistinctSurnameAndPostCode = createSampleDataListWithDistinctSurnameAndPostcode();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleDataSetCluster, Schema.class);
        ZFrame<D, R, C> zFrameDeDuplicated = zFrame.dropDuplicates("surname", "postcode");

        List<R> rows = zFrameDeDuplicated.collectAsList();
        List<Field> fields = List.of(Schema.class.getDeclaredFields());
        int matchedCount = 0;
        for (Schema schema : sampleDataWithDistinctSurnameAndPostCode) {
            for (R row : rows) {
                boolean rowMatched = true;
                for (Field column : fields) {
                    String columnName = column.getName();
                    if (!column.get(schema).toString().
                            equals(zFrame.getAsString(row, columnName))) {
                        rowMatched = false;
                        break;
                    }
                }
                if (rowMatched) {
                    matchedCount++;
                    break;
                }
            }
        }


        assertEquals(rows.size(), matchedCount,
                "rows count is not as expected");
        assertEquals(sampleDataWithDistinctSurnameAndPostCode.size(), matchedCount,
                "rows count is not as expected");
    }

    @Test
    public void testSortDescending() throws Exception {
        List<SchemaWithMixedDataType> sampleData = createSampleDataListWithMixedDataType();
        sampleData.sort((a, b) -> a.recid > b.recid ? -1 : 1);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleData, SchemaWithMixedDataType.class);

        String col = STR_RECID;
        ZFrame<D, R, C> zFrameSortedDesc = zFrame.sortDescending(col);
        List<R> rows = zFrameSortedDesc.collectAsList();

        List<Field> fields = List.of(SchemaWithMixedDataType.class.getDeclaredFields());
        for (int idx = 0; idx < sampleData.size(); idx++) {
            R row = rows.get(idx);
            for (Field column : fields) {
                String columnName  = column.getName();
                if (column.getType() == String.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrameSortedDesc.getAsString(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else if (column.getType() == Integer.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrameSortedDesc.getAsInt(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else if (column.getType() == Double.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrameSortedDesc.getAsDouble(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else if (column.getType() == Long.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrameSortedDesc.getAsLong(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else {
                    throw new ZinggException("Not a valid data type");
                }
            }
        }
    }

    @Test
    public void testSortAscending() throws Exception {
        List<SchemaWithMixedDataType> sampleData = createSampleDataListWithMixedDataType();
        sampleData.sort((a, b) -> a.recid < b.recid ? -1 : 1);
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleData, SchemaWithMixedDataType.class);

        String col = STR_RECID;
        ZFrame<D, R, C> zFrameSortedAsc = zFrame.sortAscending(col);
        List<R> rows = zFrameSortedAsc.collectAsList();

        List<Field> fields = List.of(SchemaWithMixedDataType.class.getDeclaredFields());
        for (int idx = 0; idx < sampleData.size(); idx++) {
            R row = rows.get(idx);
            for (Field column : fields) {
                String columnName  = column.getName();
                if (column.getType() == String.class) {
                    assertEquals(column.get(sampleData.get(idx)).toString(), zFrame.getAsString(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else if (column.getType() == Integer.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrame.getAsInt(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else if (column.getType() == Double.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrame.getAsDouble(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else if (column.getType() == Long.class) {
                    assertEquals(column.get(sampleData.get(idx)), zFrame.getAsLong(row, columnName),
                            "value in ZFrame and sample input is not same");
                } else {
                    throw new ZinggException("Not a valid data type");
                }
            }
        }
    }

    @Test
    public void testIsEmpty() throws Exception {
        List<Schema> emptySampleData = createEmptySampleData();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(emptySampleData, Schema.class);

        assertTrue(zFrame.isEmpty(), "zFrame is not empty");
    }

    @Test
    public void testDistinct() throws Exception {
        List<Schema> sampleData = createSampleDataList();
        List<Schema> sampleDataDistinct = createSampleDataListDistinct();
        ZFrame<D, R, C> zFrame = dfObjectUtil.getDFFromObjectList(sampleData, Schema.class);

        List<R> rows = zFrame.distinct().collectAsList();

        List<Field> fields = List.of(Schema.class.getDeclaredFields());
        for (int idx = 0; idx < sampleDataDistinct.size(); idx++) {
            R row = rows.get(idx);
            for (Field column : fields) {
                String columnName  = column.getName();
                assertEquals(column.get(sampleDataDistinct.get(idx)).toString(), zFrame.getAsString(row, columnName),
                        "value in ZFrame and sample input is not same");
            }
        }
    }
}