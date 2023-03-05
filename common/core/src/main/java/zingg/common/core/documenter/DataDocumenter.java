package zingg.common.core.documenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.FieldData;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.Context;

public abstract class DataDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.DataDocumenter";
	protected static String TEMPLATE_TITLE = "Data Documentation";
	private final String DATA_DOC_TEMPLATE = "dataDocTemplate.ftlh";

	public static final Log LOG = LogFactory.getLog(DataDocumenter.class);
	protected  ZFrame<D,R,C>  data;

	public DataDocumenter(Context<S,D,R,C,T> context, Arguments args) {
		super(context, args);
		data = getDSUtil().emptyDataFrame();
	}
	

	public void process() throws ZinggClientException {
		try {
			LOG.info("Data document generation starts");

			try {
				data = getPipeUtil().read(false,false, args.getData());
				LOG.info("Read input data : " + data.count());
			} catch (ZinggClientException e) {
				LOG.warn("No data has been found");
			}
			if (!data.isEmpty()) {
				createDataDocument();
			} else {
				LOG.info("No data document generated");
			}
			LOG.info("Data document generation finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	protected void createDataDocument() throws ZinggClientException {
		if (!data.isEmpty()) {
			Map<String, Object> root = populateTemplateData();
			writeModelDocument(root);
		}
	}

	protected void writeModelDocument(Map<String, Object> root) throws ZinggClientException {
		writeDocument(DATA_DOC_TEMPLATE, root, args.getZinggDataDocFile());
	}

	protected Map<String, Object> populateTemplateData() {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.TITLE, TEMPLATE_TITLE);
		root.put(TemplateFields.MODEL_ID, args.getModelId());

		List<String[]> list = getFieldDataList();
		root.put(TemplateFields.DATA_FIELDS_LIST, list);
		return root;
	}
	
	protected List<String[]> getFieldDataList() {
		List<String[]> list = new ArrayList<String[]> ();
		 
		for (FieldData field: data.fields()) {
			String[] row = new String [3];
			row[0] = field.getName();
			row[1] = field.getDataType();
			row[2] = String.valueOf(field.isNullable());
			list.add(row);
		}
		return list;
	}

	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}
}