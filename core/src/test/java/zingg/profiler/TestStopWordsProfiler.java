package zingg.profiler;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

public class TestStopWordsProfiler extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestStopWordsProfiler.class);

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

	@DisplayName ("Test DataColProfiler successfully generated doc")
	@Test
	public void testIfStopWordsFilesAreGeneratedAndAreNonEmpty() throws Throwable {
		String field1 = args.getZinggBaseModelDir() + "/stopWords/add1";
		String field2 = args.getZinggBaseModelDir() + "/stopWords/fname";

		try {
			Files.deleteIfExists(Paths.get(field1));
			Files.deleteIfExists(Paths.get(field2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DataColProfiler dataColDoc = new DataColProfiler(spark, ctx, args);
		Method f = DataColProfiler.class.getDeclaredMethod("createStopWordsDocuments", Dataset.class);
		f.setAccessible(true);
		Dataset<Row> data = PipeUtil.read(spark, false, false, args.getData());
		f.invoke(dataColDoc, data);

		//read the generated files and check if they are not empty
		Dataset<Row> add1 = PipeUtil.read(spark,false,false, PipeUtil.getStopWordsPipe(args, field1));
		Dataset<Row> fname = PipeUtil.read(spark,false,false, PipeUtil.getStopWordsPipe(args, field2));
		assertFalse(add1.isEmpty(), "StopWord file add1 is not generated or is empty");
		assertFalse(fname.isEmpty(), "StopWord file fname is not generated or is empty");
	}
}
