package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**end to end integration test*/
public class TestPeekModel {
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
