package zingg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Format;
import zingg.client.pipe.Pipe;

public class TestPipeUtil extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestPipeUtil.class);

	@Test
	public void testGetDataFRameReaderforGivenFormat() throws Exception {
		Method f = PipeUtil.class.getDeclaredMethod("getReader", SparkSession.class, Pipe.class);
		f.setAccessible(true);

		Pipe p = new Pipe();
		p.setName("InvalidFormat");
		p.setFormat(null);
		p.setProp(FilePipe.HEADER, "true");
		try {
			Object reader = f.invoke(null, spark, p);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOG.warn(e.getCause().getMessage());
			assertEquals("Format is null", e.getCause().getMessage());
		}
	}

	@Test
	public void testGetDataFRameReaderforGivenValidFormat() throws Exception {
		Method f = PipeUtil.class.getDeclaredMethod("getReader", SparkSession.class, Pipe.class);
		f.setAccessible(true);

		Pipe p = new Pipe();
		p.setName("ValidFormat");
		p.setFormat(Format.CSV);
		p.setProp(FilePipe.HEADER, "true");
		try {
			Object reader = f.invoke(null, spark, p);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			fail("Format is valid. still getReader() failed. Reason is " + e.getCause().getMessage());
		}
	}
}
