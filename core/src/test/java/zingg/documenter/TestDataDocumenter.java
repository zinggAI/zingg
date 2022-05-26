package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;
import zingg.client.Arguments;
import zingg.util.PipeUtil;

public class TestDataDocumenter extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestDataDocumenter.class);

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

	@DisplayName ("Test DataDocumenter successfully generates doc")
	@Test
	public void testIfDataDocumenterGeneratedDocFile() throws Throwable {
		try {
			Files.deleteIfExists(Paths.get(args.getZinggDataDocFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DataDocumenter dataDoc = new DataDocumenter(spark, args);
		Field f = DataDocumenter.class.getDeclaredField("data");
		f.setAccessible(true);
		Dataset<Row> data = PipeUtil.read(spark, false, false, args.getData());
		f.set(dataDoc, data);
		dataDoc.createDataDocument();

		assertTrue(Files.exists(Paths.get(args.getZinggDataDocFile())), "Data documentation file is not generated");
	}
}
