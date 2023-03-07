package zingg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
<<<<<<< HEAD:common/core/src/test/java/zingg/TestPeekModel.java

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.InMemoryPipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
=======
import zingg.spark.ZinggSparkTester;
import zingg.client.Arguments;
import zingg.client.ClientOptions;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
>>>>>>> dad33a5 (Untrack files in .gitignore):spark_zingg/core/src/test/java/zingg/TestPeekModel.java
/**end to end integration test*/
public class TestPeekModel extends ZinggSparkTester{
	public static final Log LOG = LogFactory.getLog(TestPeekModel.class);

	/*
	InMemoryPipe outputPipe;
	
	@BeforeEach
    public void setUp() throws Exception, ZinggClientException{
		args = Arguments.createArgumentsFromJSON(getClass().getResource("/testPeekModel/config.json").getFile());
		args.setZinggDir(getClass().getResource("/testFebrl/models").getPath());
		Pipe dataPipe = args.getData()[0];
		dataPipe.setProp(FilePipe.LOCATION, getClass().getResource("/testPeekModel/test.csv").getPath());
		args.setData(new Pipe[]{dataPipe});
		outputPipe = new InMemoryPipe(dataPipe);
		args.setOutput(new Pipe[]{outputPipe});
    }

    
	@Test
	public void testOutput(){
		PeekModel pm = new PeekModel();
		try {
			pm.init(args, "abc");
			pm.setSpark(spark);
			pm.setArgs(args);
			pm.setClientOptions(new ClientOptions("--phase", "assessModel", "--conf", "testPeekModel/config.json", "--license", "licText.txt"));
			pm.execute();
			
			Dataset<Row> dfm = pm.getMarkedRecords();
			assertEquals(80,dfm.count());


		} catch (ZinggClientException e) {
			// TODO Auto-generated catch block
			fail("did not expect " + e);
			
		}
		 
	}
		*/
}
