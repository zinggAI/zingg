package zingg;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;

public class TestDocumenter extends ZinggSparkTester{
    
    @BeforeEach
    public void setUp(){

        try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testConfig.json").getFile());
           	//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
            e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
            fail(e.getMessage());
		}
    }

    @Test
    public void testOutput() throws Throwable{
        Documenter doc = new Documenter();
        doc.init(args, "");
        doc.setArgs(args);
        doc.execute();
    }
}
