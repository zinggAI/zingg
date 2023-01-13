package zingg.documenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.util.PipeUtilBase;

public abstract class DataDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.DataDocumenter";
	protected static String TEMPLATE_TITLE = "Data Documentation";
	private final String DATA_DOC_TEMPLATE = "dataDocTemplate.ftlh";

	public static final Log LOG = LogFactory.getLog(DataDocumenter.class);
	protected  ZFrame<D,R,C>  data;

	public DataDocumenter(S session,Arguments args) {
		super(session, args);
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

		List<String[]> list = new ArrayList<String[]> ();
		/* 
		for (StructField field: data.schema().fields()) {
			String[] row = new String [3];
			row[0] = field.name();
			row[1] = field.dataType().toString();
			row[2] = field.nullable()? "true": "false";
			list.add(row);
		}*/
		root.put(TemplateFields.DATA_FIELDS_LIST, list);
		return root;
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