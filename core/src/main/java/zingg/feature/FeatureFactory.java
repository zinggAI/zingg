package zingg.feature;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class FeatureFactory<T> implements Serializable {

	public static final Log LOG = LogFactory.getLog(FeatureFactory.class);

	protected Map<T, Class> map;

	public abstract void init(); 

	public abstract T getDataTypeFromString(String t) ;

	public Object get(String dataType) throws Exception {
		if (map == null) {
			init();
		} 
		for (T v: map.keySet()) {
			System.out.println("______________" + v);
		}
		System.out.println("***********************getDataTypeFromString " + dataType);
		System.out.println("***********************and val is " + getDataTypeFromString(dataType));
		return map.get(getDataTypeFromString(dataType)).newInstance();
	}

}
