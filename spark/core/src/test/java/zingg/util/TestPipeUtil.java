package zingg.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SaveMode;
import org.junit.jupiter.api.Test;

<<<<<<< HEAD:common/core/src/test/java/zingg/util/TestPipeUtil.java
import zingg.ZinggSparkTester;
import zingg.common.client.Arguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
=======
import zingg.spark.ZinggSparkTester;
import zingg.client.Arguments;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Pipe;
>>>>>>> dad33a5 (Untrack files in .gitignore):spark_zingg/core/src/test/java/zingg/util/TestPipeUtil.java

public class TestPipeUtil extends ZinggSparkTester{
	public static final Log LOG = LogFactory.getLog(TestPipeUtil.class);

	@Test
	public void testStopWordsPipe() {
		Arguments args = new Arguments();
		String fileName = args.getStopWordsDir() + "file";
		Pipe p = PipeUtil.getStopWordsPipe(args, fileName);

		assertEquals(Pipe.FORMAT_CSV, p.getFormat(), "Format is not CSV");
		assertEquals("true", p.get(FilePipe.HEADER).toLowerCase(), "Property 'header' is set to 'false'");
		assertEquals(SaveMode.Overwrite, p.getMode(), "SaveMode is not 'Overwrite'");
		assertEquals(fileName, p.get(FilePipe.LOCATION), "Absolute location of file differs");
	}
}
