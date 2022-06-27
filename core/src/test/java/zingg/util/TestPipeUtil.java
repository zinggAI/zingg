package zingg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SaveMode;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;
import zingg.client.Arguments;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Format;
import zingg.client.pipe.Pipe;

public class TestPipeUtil extends ZinggSparkTester{
	public static final Log LOG = LogFactory.getLog(TestPipeUtil.class);

	@Test
	public void testStopWordsPipe() {
		Arguments args = new Arguments();
		String fileName = args.getStopWordsDir() + "file";
		Pipe p = PipeUtil.getStopWordsPipe(args, fileName);

		assertEquals(Format.CSV, p.getFormat(), "Format is not CSV");
		assertEquals("true", p.get(FilePipe.HEADER).toLowerCase(), "Property 'header' is set to 'false'");
		assertEquals(SaveMode.Overwrite, p.getMode(), "SaveMode is not 'Overwrite'");
		assertEquals(fileName, p.get(FilePipe.LOCATION), "Absolute location of file differs");
	}
}
