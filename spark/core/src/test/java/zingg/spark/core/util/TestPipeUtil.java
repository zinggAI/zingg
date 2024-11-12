package zingg.spark.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.Arguments;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestPipeUtil {
	public static final Log LOG = LogFactory.getLog(TestPipeUtil.class);

	private final ZinggSparkContext zinggSparkContext;

	public TestPipeUtil(SparkSession sparkSession) throws ZinggClientException {
		this.zinggSparkContext = new ZinggSparkContext();
		this.zinggSparkContext.init(sparkSession);
	}

	@Test
	public void testStopWordsPipe() {
		IArguments args = new Arguments();
		String fileName = args.getStopWordsDir() + "file";
		Pipe p = zinggSparkContext.getPipeUtil().getStopWordsPipe(args, fileName);

		assertEquals(Pipe.FORMAT_CSV, p.getFormat(), "Format is not CSV");
		assertEquals("true", p.get(FilePipe.HEADER).toLowerCase(), "Property 'header' is set to 'false'");
		assertEquals(SaveMode.Overwrite.toString(), p.getMode(), "SaveMode is not 'Overwrite'");
		assertEquals(fileName, p.get(FilePipe.LOCATION), "Absolute location of file differs");
	}
}
