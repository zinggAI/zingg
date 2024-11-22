package zingg.spark.core.documenter;

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
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.documenter.ModelDocumenter;
import zingg.common.core.documenter.TemplateFields;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.TestSparkBase;
import zingg.spark.core.context.ZinggSparkContext;


@ExtendWith(TestSparkBase.class)
public class TestModelDocumenter {
	public static final Log LOG = LogFactory.getLog(TestModelDocumenter.class);

	private final SparkSession sparkSession;
	private final ZinggSparkContext zinggSparkContext;

	public TestModelDocumenter(SparkSession sparkSession) throws ZinggClientException {
		this.sparkSession = sparkSession;
		this.zinggSparkContext = new ZinggSparkContext();
		this.zinggSparkContext.init(sparkSession);
	}

	IArguments docArguments = new Arguments();
	
	@BeforeEach
	public void setUp(){

		try {
			String configPath = getClass().getResource("../../../../documenter/config.json").getFile();
			ArgumentsUtil<IArguments> argsUtil = new ArgumentsUtil<IArguments>(IArguments.class);
			docArguments = argsUtil.createArgumentsFromJSON(configPath);
			String zinggDirPath = getClass().getResource("../../../../"+docArguments.getZinggDir()).getFile();
			docArguments.setZinggDir(zinggDirPath);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@Test
	public void testIfModelDocumenterGeneratedDocFile() throws Throwable {
		
		
		ModelDocumenter<SparkSession, Dataset<Row>, Row, Column, DataType> modelDoc = new SparkModelDocumenter(zinggSparkContext, docArguments, new ClientOptions());
		try {
			Files.deleteIfExists(Paths.get(modelDoc.getModelHelper().getZinggModelDocFile(docArguments)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		modelDoc.createModelDocument();

		assertTrue(Files.exists(Paths.get(modelDoc.getModelHelper().getZinggModelDocFile(docArguments))), "Model documentation file is not generated");
	}

	@Test
	public void testPopulateTemplateDataWhenMarkedRecordsAreAvailable() throws Throwable {
		
		ModelDocumenter<SparkSession, Dataset<Row>, Row, Column, DataType> modelDoc = new SparkModelDocumenter(zinggSparkContext, docArguments, new ClientOptions());
		modelDoc.setMarkedRecords(zinggSparkContext.getPipeUtil().read(false, false, modelDoc.getModelHelper().getTrainingDataMarkedPipe(docArguments)));

		Map<String, Object> root =  modelDoc.populateTemplateData();
		Assertions.assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(modelDoc.getMarkedRecords().columns().length, root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(modelDoc.getMarkedRecords().collectAsList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(modelDoc.getMarkedRecords().fieldIndex(ColName.MATCH_FLAG_COL), root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(modelDoc.getMarkedRecords().fieldIndex(ColName.CLUSTER_COLUMN), root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}

	@Test
	public void testPopulateTemplateDataWhenMarkedRecordsAreNone() throws Throwable {
		
		SparkModelDocumenter modelDoc = new SparkModelDocumenter(zinggSparkContext, docArguments, new ClientOptions());
		modelDoc.setMarkedRecords(new SparkFrame(sparkSession.emptyDataFrame()));

		Map<String, Object> root =  modelDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(docArguments.getFieldDefinition().size(), root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(Collections.emptyList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(0, root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(1, root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}
}
