package zingg.common.core.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;

public abstract class Model<S,T,D,R,C> implements Serializable {
	
	public static final Log LOG = LogFactory.getLog(Model.class);
	private S session;
	protected 
	List<String> columnsAdded = new ArrayList<String>();
	


	public Model() {
	}
	
	
	public abstract void register(S spark) ;

	public void setSession(S s){
		this.session = s;
	}

	public S getSession(){
		return session;
	}

	protected String getColumnName(String fieldName, String fnName, int count) {
		return ColName.SIM_COL + count;
	}
	

	public List<String> getColumnsAdded() {
		return columnsAdded;
	}


	public void setColumnsAdded(List<String> columnsAdded) {
		this.columnsAdded = columnsAdded;
	}

	public static double[] getGrid(double begin, double end, double jump, boolean isMultiple) {
		List<Double> alphaList = new ArrayList<Double>();
		if (isMultiple) {
			for (double alpha =begin; alpha <= end; alpha *= jump) {
				alphaList.add(alpha);
			}

		}
		else {
			for (double alpha =begin; alpha <= end; alpha += jump) {
				alphaList.add(alpha);
			}
		}
		double[] retArr = new double[alphaList.size()];
		for (int i=0; i < alphaList.size(); ++i) {
			retArr[i] = alphaList.get(i);
		}
		return retArr;
	}
	
	public abstract void fit(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg) throws ZinggClientException;
	
	
	public abstract void load(String path);

	public abstract ZFrame<D,R,C> fitCore(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg);

	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data) throws ZinggClientException;
	
	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data, boolean isDrop) throws ZinggClientException ;
	
	//this will do the prediction but not drop the columns
	public abstract ZFrame<D,R,C> predictCore(ZFrame<D,R,C> data);

	public abstract void save(String path) throws IOException;
	
	public abstract ZFrame<D,R,C> transform(ZFrame<D,R,C> input);

	public ZFrame<D, R, C> dropFeatureCols(ZFrame<D, R, C> predictWithFeatures, boolean isDrop){
		if (isDrop) {
			ZFrame<D, R, C> returnDS = predictWithFeatures.drop(columnsAdded.toArray(new String[columnsAdded.size()]));
			//LOG.debug("Return schema after dropping additional columns is " + returnDS.schema());
			return returnDS; //new SparkFrame(returnDS);
		}
		return predictWithFeatures;
	}
	
}
