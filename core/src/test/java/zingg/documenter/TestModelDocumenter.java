package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;
import zingg.client.Arguments;

public class TestModelDocumenter extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestModelDocumenter.class);

	@BeforeEach
	public void setUp(){

		try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/documenter/config.json").getFile());
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@DisplayName ("Test ModelDocumneter successfully generates doc")
	@Test
	public void testIfModelDocumenterGeneratedDocFile() throws Throwable {
		try {
			Files.deleteIfExists(Paths.get(args.getZinggModelDocFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ModelDocumenter modelDoc = new ModelDocumenter(spark, args);
		modelDoc.createModelDocument();

		assertTrue(Files.exists(Paths.get(args.getZinggModelDocFile())), "Model documentation file is not generated");
	}
}
