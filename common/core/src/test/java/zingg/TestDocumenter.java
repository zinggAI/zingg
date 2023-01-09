package zingg;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;

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
