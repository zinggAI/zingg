package zingg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.InMemoryPipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.TrainMatcher;
/**end to end integration test*/
public class TestFebrlDataset extends ZinggSparkTester{
	public static final Log LOG = LogFactory.getLog(TestFebrlDataset.class);

	
	InMemoryPipe outputPipe;
	
	@BeforeEach
    public void setUp() throws Exception, ZinggClientException{
		args = Arguments.createArgumentsFromJSON(getClass().getResource("/testFebrl/config.json").getFile());
		args.setZinggDir(getClass().getResource("/testFebrl/models").getPath());
		Pipe dataPipe = args.getData()[0];
		dataPipe.setProp(FilePipe.LOCATION, getClass().getResource("/testFebrl/test.csv").getPath());
		args.setData(new Pipe[]{dataPipe});
		outputPipe = new InMemoryPipe();
		args.setOutput(new Pipe[]{outputPipe});
    }

    
	@Test
	public void testModelAccuracy(){
		TrainMatcher tm = new TrainMatcher();
		try {
			tm.init(args, "");
			tm.setSpark(spark);
			tm.setCtx(ctx);
			tm.setArgs(args);
			tm.execute();
			

			Dataset<Row> df = outputPipe.getRecords();
			assertEquals(65,df.count());

			
			//assess accuracy 
			
			df = df.select("id", ColName.CLUSTER_COLUMN);
			df = df.withColumn("dupeId",df.col("id").substr(0,8)).cache();
			Dataset<Row> df1 = df.withColumnRenamed("id", "id1").withColumnRenamed("dupeId", "dupeId1")
								.withColumnRenamed(ColName.CLUSTER_COLUMN, ColName.CLUSTER_COLUMN + "1").cache();
						
			
			Dataset<Row> gold = joinAndFilter("dupeId", df, df1).cache();
			Dataset<Row> result = joinAndFilter(ColName.CLUSTER_COLUMN, df, df1).cache();

			//gold.repartition(1).rdd().saveAsTextFile("/tmp/gold");
			//result.repartition(1).rdd().saveAsTextFile("/tmp/result");
			//gold.show(100);result.show(100);
			Dataset<Row> fn = gold.except(result);
			Dataset<Row> tp = gold.intersect(result);
			Dataset<Row> fp = result.except(gold);

			long fnCount = fn.count();
			long tpCount = tp.count();
			long fpCount = fp.count();

			LOG.info("False negative " + fnCount);
			LOG.info("True positive " + tpCount);
			LOG.info("False positive " + fpCount);
			LOG.info("precision " + (tpCount*1.0d/(tpCount+fpCount)));
			LOG.info("recall " + tpCount + " denom " + (tpCount+fnCount) + " overall " + (tpCount*1.0d/(tpCount+fnCount)));

			assertTrue(0.8 < (tpCount*1.0d/(tpCount+fpCount)));
			assertTrue(0.8 < (tpCount*1.0d/(tpCount+fnCount)));
			

		} catch (ZinggClientException e) {
			// TODO Auto-generated catch block
			fail("did not expect " + e);
		}
	}

	protected Dataset<Row> joinAndFilter(String colName, Dataset<Row> df, Dataset<Row> df1){
		Dataset<Row> joined = df.join(df1, df.col(colName).equalTo(df1.col(colName+"1")));
		return joined.filter(joined.col("id").gt(joined.col("id1")));
	}
		
}
