package zingg.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import zingg.client.helper.DFObjectUtil;
import zingg.client.helper.SparkDFObjectUtil;
import zingg.client.utility.Constant;
import zingg.common.client.Arguments;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.spark.client.SparkFrame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static zingg.client.helper.HelperFunctions.assertTrueCheckingExceptOutput;
import static zingg.client.utility.Constant.*;


public class TestSparkDataFrame extends TestDataFrame {

    public static IArguments args;
    public static JavaSparkContext ctx;
    public static SparkSession sparkSession;
    public static final Log LOG = LogFactory.getLog(TestSparkDataFrame.class);

    @BeforeAll
    public static void setup() {
        setUpSpark();
    }

    public static void setUpSpark() {
        try {
            sparkSession = SparkSession
                    .builder()
                    .master("local[*]")
                    .appName("Zingg" + "Junit")
                    .getOrCreate();
            ctx = new JavaSparkContext(sparkSession.sparkContext());
            JavaSparkContext.jarOfClass(TestSparkDataFrame.class);
            args = new Arguments();
        } catch (Throwable e) {
            if (LOG.isDebugEnabled())
                e.printStackTrace();
            LOG.info("Problem in spark env setup");
        }
    }

    @AfterAll
    public static void teardown() {
        if (ctx != null) {
            ctx.stop();
            ctx = null;
        }
        if (sparkSession != null) {
            sparkSession.stop();
            sparkSession = null;
        }
    }

    public TestSparkDataFrame() {
        setDfObjectUtil(getDFObjectUtil(sparkSession));
    }

    @Test
    public void testAliasOfSparkFrame() {
        SparkFrame sf = new SparkFrame(createSampleDataset(sparkSession));
        String aliasName = "AnotherName";
        sf.as(aliasName);
        assertTrueCheckingExceptOutput(sf.as(aliasName), sf, "Dataframe and its alias are not same");
    }

    @Test
    public void testDistinct() {
        Dataset<Row> df = createSampleDataset(sparkSession);
        SparkFrame sf = new SparkFrame(df);
        SparkFrame sf2 = new SparkFrame(df.distinct());
        assertTrueCheckingExceptOutput(sf.distinct(), sf2, "SparkFrame.distict() does not match with standard distict() output");
    }

    @Test
    public void testDropDuplicatesConsideringGivenColumnsAsStringArray() {
        Dataset<Row> df = createSampleDataset(sparkSession);
        SparkFrame sf = new SparkFrame(df);
        String[] columnArray = new String[] {"surname", "postcode"};
        ZFrame<Dataset<Row>, Row, Column> sf2 = new SparkFrame(df.dropDuplicates(columnArray));
        assertTrueCheckingExceptOutput(sf2, sf.dropDuplicates(columnArray), "SparkFrame.dropDuplicates(str[]) does not match with standard dropDuplicates(str[]) output");
    }

    @Test
    public void testDropDuplicatesConsideringGivenIndividualColumnsAsString() {
        Dataset<Row> df = createSampleDataset(sparkSession);
        SparkFrame sf = new SparkFrame(df);
        ZFrame<Dataset<Row>, Row, Column> sf2 = new SparkFrame(df.dropDuplicates("surname", "postcode"));
        assertTrueCheckingExceptOutput(sf2, sf.dropDuplicates("surname"), "SparkFrame.dropDuplicates(col1, col2) does not match with standard dropDuplicates(col1, col2) output");
    }

    @Test
    public void testIsEmpty() {
        if (sparkSession == null) {
            setUpSpark();
        }
        Dataset<Row> df = sparkSession.emptyDataFrame();
        SparkFrame sf = new SparkFrame(df);
        assertTrue(sf.isEmpty(), "DataFrame is not empty");
    }

    @Test
    public void testSortDescending() {
        Dataset<Row> df = createSampleDatasetHavingMixedDataTypes(sparkSession);
        SparkFrame sf = new SparkFrame(df);
        String col = STR_RECID;
        ZFrame<Dataset<Row>,Row,Column> sf2 = sf.sortDescending(col);
        assertTrueCheckingExceptOutput(sf2, df.sort(functions.desc(col)), "SparkFrame.sortDescending() output is not as expected");
    }

    @Test
    public void testSortAscending() {
        Dataset<Row> df = createSampleDatasetHavingMixedDataTypes(sparkSession);
        SparkFrame sf = new SparkFrame(df);
        String col = STR_RECID;
        ZFrame<Dataset<Row>,Row,Column> sf2 = sf.sortAscending(col);
        assertTrueCheckingExceptOutput(sf2, df.sort(functions.asc(col)), "SparkFrame.sortAscending() output is not as expected");
    }

    @Test
    public void testGetMaxVal(){
        SparkFrame zScoreDF = getZScoreDF(sparkSession);
        assertEquals(400,zScoreDF.getMaxVal(ColName.CLUSTER_COLUMN));
    }

    @Test
    public void testGroupByMinMax(){
        SparkFrame zScoreDF = getZScoreDF(sparkSession);
        ZFrame<Dataset<Row>, Row, Column> groupByDF = zScoreDF.groupByMinMaxScore(zScoreDF.col(ColName.ID_COL));

        Dataset<Row> assertionDF = groupByDF.df();
        List<Row> assertionRows = assertionDF.collectAsList();
        for (Row row : assertionRows) {
            if(row.getInt(0)==1) {
                assertEquals(1001,row.getInt(1));
                assertEquals(2002,row.getInt(2));
            }
        }
    }

    @Test
    public void testGroupByMinMax2(){
        SparkFrame zScoreDF = getZScoreDF(sparkSession);
        ZFrame<Dataset<Row>, Row, Column> groupByDF = zScoreDF.groupByMinMaxScore(zScoreDF.col(ColName.CLUSTER_COLUMN));

        Dataset<Row> assertionDF = groupByDF.df();
        List<Row>  assertionRows = assertionDF.collectAsList();
        for (Row row : assertionRows) {
            if(row.getInt(0)==100) {
                assertEquals(900,row.getInt(1));
                assertEquals(9002,row.getInt(2));
            }
        }
    }

    @Test
    public void testRightJoinMultiCol(){
        ZFrame<Dataset<Row>, Row, Column> inpData = getInputData(sparkSession);
        ZFrame<Dataset<Row>, Row, Column> clusterData = Constant.getClusterData(sparkSession);
        ZFrame<Dataset<Row>, Row, Column> joinedData = clusterData.join(inpData,ColName.ID_COL,ColName.SOURCE_COL,ZFrame.RIGHT_JOIN);
        assertEquals(10,joinedData.count());
    }

    @Test
    public void testFilterInCond(){
        SparkFrame inpData = getInputData(sparkSession);
        SparkFrame clusterData = getClusterDataWithNull(sparkSession);
        ZFrame<Dataset<Row>, Row, Column> filteredData = inpData.filterInCond(ColName.ID_COL, clusterData, ColName.COL_PREFIX+ ColName.ID_COL);
        assertEquals(5,filteredData.count());
    }

    @Test
    public void testFilterNotNullCond(){
        SparkFrame clusterData = Constant.getClusterDataWithNull(sparkSession);
        ZFrame<Dataset<Row>, Row, Column> filteredData = clusterData.filterNotNullCond(ColName.SOURCE_COL);
        assertEquals(3,filteredData.count());
    }

	@Test
	public void testFilterNullCond() {
		SparkFrame clusterData = getClusterDataWithNull(sparkSession);
		ZFrame<Dataset<Row>, Row, Column> filteredData = clusterData.filterNullCond(ColName.SOURCE_COL);
		assertEquals(2, filteredData.count());
	}

    private DFObjectUtil getDFObjectUtil(SparkSession sparkSession) {
        return new SparkDFObjectUtil(sparkSession);
    }
}
