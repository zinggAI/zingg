package zingg.common.core.documenter;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;

public abstract class TestModelDocumenterBase<S,D,R,C,T> {

    public static final Log LOG = LogFactory.getLog(TestModelDocumenterBase.class);
	protected final Context<S, D, R, C, T> context;
    IArguments docArguments = new Arguments();

    public TestModelDocumenterBase(Context<S, D, R, C, T> context) throws ZinggClientException {
		this.context = context;
	}
	
	@BeforeEach
	public void setUp(){

		try {
			String configPath = getClass().getResource("../../../../documenter/config.json").getFile();
			ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);
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
		
		ModelDocumenter<S,D,R,C,T> modelDoc = getModelDocumenter(context, docArguments, new ClientOptions());
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
		
		ModelDocumenter<S,D,R,C,T> modelDoc = getModelDocumenter(context, docArguments, new ClientOptions());
		modelDoc.setMarkedRecords(context.getPipeUtil().read(false, false, modelDoc.getModelHelper().getTrainingDataMarkedPipe(docArguments)));

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
		
		ModelDocumenter<S,D,R,C,T> modelDoc = getModelDocumenter(context, docArguments, new ClientOptions());
		modelDoc.setMarkedRecords(getMarkedRecordsZFrame());

		Map<String, Object> root =  modelDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(docArguments.getFieldDefinition().size(), root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(Collections.emptyList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(0, root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(1, root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}

    protected abstract ModelDocumenter<S,D,R,C,T> getModelDocumenter(IContext<S,D,R,C,T> context, IArguments args, ClientOptions options);

    protected abstract ZFrame<D,R,C> getMarkedRecordsZFrame();
}
