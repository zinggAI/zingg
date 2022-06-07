package zingg;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;

public class TestPeekModel extends ZinggSparkTester{
    
    @BeforeEach
    public void setUp(){

        try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testPeekModel/config.json").getFile());
           	//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
            e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
            fail(e.getMessage());
		}
    }

    @Test
    public void testOutput() throws Throwable{
        PeekModel doc = new PeekModel();
        doc.init(args, "");
        doc.setArgs(args);
        doc.execute();
        doc.getMarkedRecords();
    }
}
