package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

<<<<<<< HEAD:common/core/src/test/java/zingg/documenter/TestModelDocumenter.java
import zingg.ZinggSparkTester;
import zingg.common.client.Arguments;
import zingg.common.client.util.ColName;
import zingg.common.core.documenter.ModelDocumenter;
import zingg.common.core.documenter.TemplateFields;
=======
import zingg.spark.ZinggSparkTester;
import zingg.client.Arguments;
import zingg.client.util.ColName;
>>>>>>> dad33a5 (Untrack files in .gitignore):spark_zingg/core/src/test/java/zingg/documenter/TestModelDocumenter.java
import zingg.util.PipeUtil;

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

	@DisplayName ("Test ModelDocumenter successfully generates doc")
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

	@DisplayName ("Test template data when marked records are available")
	@Test
	public void testPopulateTemplateDataWhenMarkedRecordsAreAvailable() throws Throwable {
		ModelDocumenter modelDoc = new ModelDocumenter(spark, args);
		modelDoc.markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));

		Map<String, Object> root =  modelDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(args.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(modelDoc.markedRecords.columns().length, root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(modelDoc.markedRecords.collectAsList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(modelDoc.markedRecords.schema().fieldIndex(ColName.MATCH_FLAG_COL), root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(modelDoc.markedRecords.schema().fieldIndex(ColName.CLUSTER_COLUMN), root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}

	@DisplayName ("Test template data when marked records are not available")
	@Test
	public void testPopulateTemplateDataWhenMarkedRecordsAreNone() throws Throwable {
		ModelDocumenter modelDoc = new ModelDocumenter(spark, args);
		modelDoc.markedRecords = spark.emptyDataFrame();

		Map<String, Object> root =  modelDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(args.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(args.getFieldDefinition().size(), root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(Collections.emptyList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(0, root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(1, root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}
}
