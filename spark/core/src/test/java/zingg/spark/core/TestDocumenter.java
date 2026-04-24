package zingg.spark.core;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;

import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.arguments.model.Arguments;

public class TestDocumenter {

    public static final Log LOG = LogFactory.getLog(TestDocumenter.class);

    @BeforeEach
    public void setUp(){
        try {
            IArgumentService<Arguments> argumentService = new ArgumentServiceImpl<>(Arguments.class);
			IArguments args = argumentService.loadArguments(getClass().getResource("/testDocumenter/config.json").getFile());
           	//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
            if(LOG.isDebugEnabled()) {
                e.printStackTrace();
            }
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
