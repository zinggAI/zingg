package zingg;

import static org.junit.Assert.fail;

import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;

public class TestDocumenter {
    Arguments args;
    JavaSparkContext sc;
    
    @Before
    public void setUp(){

        try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testConfig.json").getFile());
            sc = new JavaSparkContext("local", "JavaAPISuite");
         	//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
            e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
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
