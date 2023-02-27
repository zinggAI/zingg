package zingg;
import zingg.spark.ZinggSparkTester;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;

import zingg.common.client.Arguments;

public class TestDocumenter extends ZinggSparkTester{
    
    @BeforeEach
    public void setUp(){

        try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testDocumenter/config.json").getFile());
           	//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
            e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
            fail(e.getMessage());
		}
    }

    /*
    @Test
    public void testOutput() throws Throwable{
        Documenter doc = new Documenter();
        doc.init(args, "");
        doc.setArgs(args);
        doc.execute();
    }
    */
}
