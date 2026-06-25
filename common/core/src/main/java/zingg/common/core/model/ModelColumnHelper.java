package zingg.common.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import zingg.common.client.util.ColName;

public class ModelColumnHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> columnsAdded = new ArrayList<>();

	public String getColumnName(String fieldName, String fnName, int count) {
		return ColName.SIM_COL + count;
	}

	public List<String> getColumnsAdded() {
		return columnsAdded;
	}

	public void setColumnsAdded(List<String> columnsAdded) {
		this.columnsAdded = columnsAdded;
	}
}
