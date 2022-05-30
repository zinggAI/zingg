package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		dataDoc.data = PipeUtil.read(spark, false, false, args.getData());
		dataDoc.createDataDocument();

		assertTrue(Files.exists(Paths.get(args.getZinggDataDocFile())), "Data documentation file is not generated");
	}

	@DisplayName ("Test template data")
	@Test
	public void testPopulateTemplateData() throws Throwable {
		DataDocumenter dataDoc = new DataDocumenter(spark, args);
		dataDoc.data = PipeUtil.read(spark, false, false, args.getData());

		Map<String, Object> root =  dataDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.TITLE), "The field does not exist - " + TemplateFields.TITLE);
		assertEquals(DataDocumenter.TEMPLATE_TITLE, root.get(TemplateFields.TITLE));

		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(args.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertTrue(root.containsKey(TemplateFields.DATA_FIELDS_LIST), "The field does not exist. - " + TemplateFields.DATA_FIELDS_LIST);
	}
}
