package zingg.common.core.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public abstract class Model<S,D,R,C,T> implements Serializable {

	public static final Log LOG = LogFactory.getLog(Model.class);
	protected ModelColumnHelper columnHelper = new ModelColumnHelper();
	protected S session;

	public void setSession(S s){
		this.session = s;
	}

	public S getSession(){
		return session;
	}

	public Model(S s){
		this.session = s;
	}
	
	
	public abstract void register() ;

	

	protected String getColumnName(String fieldName, String fnName, int count) {
		return columnHelper.getColumnName(fieldName, fnName, count);
	}

	public List<String> getColumnsAdded() {
		return columnHelper.getColumnsAdded();
	}

	public void setColumnsAdded(List<String> columnsAdded) {
		columnHelper.setColumnsAdded(columnsAdded);
	}

	public abstract void fit(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg) throws ZinggClientException;

	public abstract void load(String path);

	protected abstract ZFrame<D,R,C> fitCore(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg);

	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data) throws ZinggClientException;

	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data, boolean isDrop) throws ZinggClientException;

	protected abstract ZFrame<D,R,C> predictCore(ZFrame<D,R,C> data);

	public abstract void save(String path) throws IOException;

	public abstract ZFrame<D,R,C> transform(ZFrame<D,R,C> input);

	public ZFrame<D, R, C> dropFeatureCols(ZFrame<D, R, C> predictWithFeatures, boolean isDrop) {
		if (isDrop) {
			List<String> cols = columnHelper.getColumnsAdded();
			return predictWithFeatures.drop(cols.toArray(new String[0]));
		}
		return predictWithFeatures;
	}
	
}
