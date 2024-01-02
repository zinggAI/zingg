package zingg.common.core.documenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefUtil;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;


public abstract class ModelDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	
	private static final String PAIR_WISE_COUNT = ColName.COL_PREFIX + "pair_wise_count";
	protected static String name = "zingg.ModelDocumenter";
	public static final Log LOG = LogFactory.getLog(ModelDocumenter.class);

	private final String MODEL_TEMPLATE = "model.ftlh";
	protected ModelColDocumenter<S,D,R,C,T> modelColDoc;
	protected  ZFrame<D,R,C>  markedRecords;
	protected  ZFrame<D,R,C>  unmarkedRecords;
	
	protected FieldDefUtil fieldDefUtil;

	public ModelDocumenter(Context<S,D,R,C,T> context, IArguments args) {
		super(context, args);
		markedRecords = getDSUtil().emptyDataFrame();
		fieldDefUtil = new FieldDefUtil();
	}

	public void process() throws ZinggClientException {
		createModelDocument();
		modelColDoc.process(markedRecords);
	}

	protected void createModelDocument() throws ZinggClientException {
		try {
			LOG.info("Model document generation starts");

			// drop columns which are don't use if show concise is true
			markedRecords = filterForConcise(getMarkedRecords().sortAscending(ColName.CLUSTER_COLUMN));
			unmarkedRecords = filterForConcise(getUnmarkedRecords().sortAscending(ColName.CLUSTER_COLUMN));
			Map<String, Object> root = populateTemplateData();
			writeModelDocument(root);

			LOG.info("Model document generation finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	private void writeModelDocument(Map<String, Object> root) throws ZinggClientException {
		checkAndCreateDir(args.getZinggDocDir());
		writeDocument(MODEL_TEMPLATE, root, args.getZinggModelDocFile());
	}

	protected Map<String, Object> populateTemplateData() {
		/* Create a data-model */
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.MODEL_ID, args.getModelId());
		
		if(!markedRecords.isEmpty()) {
			markedRecords = markedRecords.cache();
			
			root.put(TemplateFields.CLUSTERS, markedRecords.collectAsList());
			root.put(TemplateFields.NUM_COLUMNS, markedRecords.columns().length);
			root.put(TemplateFields.COLUMNS, markedRecords.columns());
			root.put(TemplateFields.ISMATCH_COLUMN_INDEX,
					markedRecords.fieldIndex(ColName.MATCH_FLAG_COL));
			root.put(TemplateFields.CLUSTER_COLUMN_INDEX,
					markedRecords.fieldIndex(ColName.CLUSTER_COLUMN));
			
			putSummaryCounts(root);

		} else {
			// fields required to generate basic document
			List<String> columnList = getColumnList();
			root.put(TemplateFields.NUM_COLUMNS, columnList.size());
			root.put(TemplateFields.COLUMNS, columnList.toArray());
			root.put(TemplateFields.CLUSTERS, Collections.emptyList());
			root.put(TemplateFields.ISMATCH_COLUMN_INDEX, 0);
			root.put(TemplateFields.CLUSTER_COLUMN_INDEX, 1);
		}
		
		return root;
	}

	protected ZFrame<D,R,C> filterForConcise(ZFrame<D,R,C> df) {
		if (args.getShowConcise()) {
			List<String> dontUseFields = getFieldNames(
					(List<? extends FieldDefinition>) fieldDefUtil.getFieldDefinitionDontUse(args.getFieldDefinition()));
			if(!dontUseFields.isEmpty()) {
				df = df.drop(dontUseFields.toArray(new String[dontUseFields.size()]));
			}
		}
		return df;
	}
	
	protected List<String> getColumnList() {	
		List<? extends FieldDefinition> fieldList = args.getFieldDefinition();
		//drop columns which are don't use if show concise is true
		if (args.getShowConcise()) {
			fieldList = fieldDefUtil.getFieldDefinitionToUse(args.getFieldDefinition());
		}	
		return getFieldNames(fieldList);
	}

	protected List<String> getFieldNames(List<? extends FieldDefinition> fieldList) {
		return fieldList.stream().map(fd -> fd.getFieldName())
				.collect(Collectors.toList());
	}

	private void putSummaryCounts(Map<String, Object> root) {
		// Get the count if not empty
		ZFrame<D,R,C>  markedRecordsPairSummary = markedRecords.groupByCount(ColName.MATCH_FLAG_COL, PAIR_WISE_COUNT);
		List<R> pairCountList = markedRecordsPairSummary.collectAsList();
		long totalPairs = 0;
		long matchPairs = 0;
		long nonMatchPairs = 0;
		long notSurePairs = 0;
		for (Iterator<R> iterator = pairCountList.iterator(); iterator.hasNext();) {
			R r = iterator.next();
			int z_isMatch = markedRecordsPairSummary.getAsInt(r, ColName.MATCH_FLAG_COL);
			long pairWiseCount = markedRecordsPairSummary.getAsLong(r, PAIR_WISE_COUNT);
			if (z_isMatch==ColValues.MATCH_TYPE_MATCH) {
				matchPairs = pairWiseCount/2;
			} else if (z_isMatch==ColValues.MATCH_TYPE_NOT_A_MATCH) {
				nonMatchPairs = pairWiseCount/2;
			} else if (z_isMatch==ColValues.MATCH_TYPE_NOT_SURE) {
				notSurePairs = pairWiseCount/2;
			}
		}
		totalPairs = matchPairs+nonMatchPairs+notSurePairs;
		
		root.put(TemplateFields.TOTAL_PAIRS, totalPairs);
		root.put(TemplateFields.MATCH_PAIRS, matchPairs);
		root.put(TemplateFields.NON_MATCH_PAIRS, nonMatchPairs);
		root.put(TemplateFields.NOT_SURE_PAIRS, notSurePairs);
		
		long markedPairs = markedRecords.count()/2;
		long unmarkedPairs = 0;
		
		if(unmarkedRecords!=null && !unmarkedRecords.isEmpty()) {
			unmarkedPairs = unmarkedRecords.count()/2;
		}
		
		long identifiedPairs = markedPairs+unmarkedPairs;

		root.put(TemplateFields.MARKED_PAIRS, markedPairs);
		root.put(TemplateFields.UNMARKED_PAIRS, unmarkedPairs);
		root.put(TemplateFields.IDENTIFIED_PAIRS, identifiedPairs);
		
		
	}
	
	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}
}
