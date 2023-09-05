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

public abstract class Model<S,T,D,R,C> implements Serializable {
	
	public static final Log LOG = LogFactory.getLog(Model.class);
	private S session;
	


	public Model() {
	}
	
	
	public abstract void register(S spark) ;

	public void setSession(S s){
		this.session = s;
	}

	public S getSession(){
		return session;
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

	public abstract void fitCore(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg);

	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data) throws ZinggClientException;
	
	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data, boolean isDrop) throws ZinggClientException ;
	
	//this will do the prediction but not drop the columns
	public abstract ZFrame<D,R,C> predictCore(ZFrame<D,R,C> data);

	public abstract void save(String path) throws IOException;
	
	public abstract ZFrame<D,R,C> transform(ZFrame<D,R,C> input);

	public abstract ZFrame<D, R,C> dropFeatureCols(ZFrame<D, R,C> f, boolean isDrop);
	
}
