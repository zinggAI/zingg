package zingg.common.core.documenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;

public abstract class TestDataDocumenterBase<S,D,R,C,T> {

    public static final Log LOG = LogFactory.getLog(TestDataDocumenterBase.class);
    protected Context<S, D, R, C, T> context;
    IArguments docArguments = new Arguments();

    public TestDataDocumenterBase(){
	}
	
	public void initialize(Context<S, D, R, C, T> context) throws ZinggClientException {
		this.context = context;
	}

    protected abstract DataDocumenter<S,D,R,C,T> getDataDocumenter(IContext<S,D,R,C,T> context, IArguments args, ClientOptions options);

    @BeforeEach
	public void setUp(){
		try {
			String configPath = getClass().getResource("../../../../documenter/config.json").getFile();
			IArgumentService<Arguments> argsUtil = new ArgumentServiceImpl<>(Arguments.class);
			docArguments = argsUtil.loadArguments(configPath);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@Test
	public void testPopulateTemplateData() throws Throwable {

		DataDocumenter<S,D,R,C,T> dataDoc = getDataDocumenter(context, docArguments, new ClientOptions());
		Pipe[] dataPipeArr = docArguments.getData();
		
		for (int i = 0; i < dataPipeArr.length; i++) {
			String file = getClass().getResource("../../../../documenter/test.csv").getFile();
			dataPipeArr[i].setProp(FilePipe.PATH, file);
		}
		dataDoc.setData(context.getPipeUtil().read(false, false, dataPipeArr));


		Map<String, Object> root =  dataDoc.populateTemplateData();
		
		assertTrue(root.containsKey(TemplateFields.TITLE), "The field does not exist - " + TemplateFields.TITLE);
		assertEquals(DataDocumenter.TEMPLATE_TITLE, root.get(TemplateFields.TITLE));

		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertTrue(root.containsKey(TemplateFields.DATA_FIELDS_LIST), "The field does not exist. - " + TemplateFields.DATA_FIELDS_LIST);
	}
    
}
