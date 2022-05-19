package zingg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
/**end to end integration test*/
public class TestFebrlDataset {
	
	private transient JavaSparkContext sc;
	Arguments args;
	InMemoryPipe outputPipe;
	
	@BeforeEach
    public void setUp() throws Exception, ZinggClientException{
		args = Arguments.createArgumentsFromJSON(getClass().getResource("/testFebrl/config.json").getFile());
		args.setZinggDir(getClass().getResource("/testFebrl/models").getPath());
		Pipe dataPipe = args.getData()[0];
		dataPipe.setProp(FilePipe.LOCATION, getClass().getResource("/testFebrl/test.csv").getPath());
		args.setData(new Pipe[]{dataPipe});
		sc = new JavaSparkContext("local", "JavaAPISuite");
		outputPipe = new InMemoryPipe();
		args.setOutput(new Pipe[]{outputPipe});
    }

    @AfterEach
    public void tearDown() {
      sc.stop();
      sc = null;
      // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
      System.clearProperty("spark.driver.port");
    }

	@Test
	public void testModelAccuracy(){
		TrainMatcher tm = new TrainMatcher();
		try {
			tm.init(args, "");
		
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

			gold.show(100);result.show(100);
			Dataset<Row> fn = gold.except(result);
			Dataset<Row> tp = gold.intersect(result);
			Dataset<Row> fp = result.except(gold);

			assertTrue(0.9 < (tp.count()/(tp.count()+fp.count())));
			assertTrue(0.9 < (tp.count()/(tp.count()+fn.count())));
			

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
