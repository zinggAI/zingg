package zingg.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.client.FieldDefinition;
import zingg.client.ZFrame;
import zingg.feature.Feature;

public abstract class Model<S,T,D,R,C> implements Serializable {
	
	public static final Log LOG = LogFactory.getLog(Model.class);
	//private Map<FieldDefinition, Feature> featurers;

	public Model() {
	}
	
	
	public abstract void register(S spark) ;
	
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
	
	public abstract void fit(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg);
	
	
	public abstract void load(String path);
	
	
	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data);
	
	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data, boolean isDrop) ;

	public abstract void save(String path) throws IOException;
	
	public abstract ZFrame<D,R,C> transform(ZFrame<D,R,C> input);
	
}
