package zingg.common.core.data.df.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.SelectedCols;
import zingg.common.client.cols.ZidAndFieldDefSelector;

public class FieldDefDataController<D, R, C> implements IDataController<D, R, C> {
	@Override
	public ZFrame<D, R, C> process(ZFrame<D,R,C> originalDF) throws ZinggClientException {
		return originalDF.select(selectedCols.getCols());
	}
	
	protected SelectedCols selectedCols;
	
	protected List<? extends FieldDefinition> fieldDefinition;
	
	public static final Log LOG = LogFactory.getLog(FieldDefDataController.class);   
	
	public FieldDefDataController(List<? extends FieldDefinition> fieldDefinition) {
		this(fieldDefinition,new ZidAndFieldDefSelector(fieldDefinition));
	}

	public FieldDefDataController(List<? extends FieldDefinition> fieldDefinition,
			SelectedCols selectedCols) {
		this.fieldDefinition = fieldDefinition;
		this.selectedCols = selectedCols;
	}

}
