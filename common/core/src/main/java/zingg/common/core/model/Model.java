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


	public abstract ZFrame<D,R,C> fit(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg) throws ZinggClientException;
        
	public abstract void load(String path);

	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data) throws ZinggClientException;

	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data, boolean isDrop) throws ZinggClientException;

	public abstract void save(String path) throws IOException;

	public ZFrame<D, R, C> dropFeatureCols(ZFrame<D, R, C> predictWithFeatures, boolean isDrop) {
		if (isDrop) {
			List<String> cols = columnHelper.getColumnsAdded();
			return predictWithFeatures.drop(cols.toArray(new String[0]));
		}
		return predictWithFeatures;
	}
	
}
