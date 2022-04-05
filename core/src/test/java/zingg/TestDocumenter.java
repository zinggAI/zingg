package zingg;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;

public class TestDocumenter {
    Arguments args;
    JavaSparkContext sc;
    
    @BeforeEach
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
