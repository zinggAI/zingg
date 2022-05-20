package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Method;
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

public class TestStopWordsDocumenter extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestStopWordsDocumenter.class);

	@BeforeEach
	public void setUp(){
		try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testConfig.json").getFile());
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@DisplayName ("Test DataColDocumenter successfully generated doc")
	@Test
	public void testIfDataColDocumenterGeneratedDocFile() throws Throwable {
		String file1 = args.getZinggDocDir() + "add1.html";
		String file2 = args.getZinggDocDir() + "fname.html";

		try {
			Files.deleteIfExists(Paths.get(file1));
			Files.deleteIfExists(Paths.get(file2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DataColDocumenter dataColDoc = new DataColDocumenter(spark, args);
		Method f = DataColDocumenter.class.getDeclaredMethod("createStopWordsDocuments", Dataset.class);
		f.setAccessible(true);
		Dataset<Row> data = PipeUtil.read(spark, false, false, args.getData());
		f.invoke(dataColDoc, data);

		assertTrue(Files.exists(Paths.get(file1)), "Data col documentation add1.html is not generated");
		assertTrue(Files.exists(Paths.get(args.getZinggDocDir() + "fname.html")), "Data col documentation fname.html is not generated");
	}
}
