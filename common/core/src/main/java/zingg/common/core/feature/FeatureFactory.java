package zingg.common.core.feature;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.core.ZinggException;

public abstract class FeatureFactory<T> implements Serializable {

	public static final Log LOG = LogFactory.getLog(FeatureFactory.class);

	protected Map<T, Class> map;

	public abstract void init(); 

	public abstract T getDataTypeFromString(String t) ;

	public Object get(String dataType) throws Exception {
		try {
			if (map == null) {
				init();
			}
			return map.get(getDataTypeFromString(dataType)).newInstance();
		} catch (Throwable exception) {
			LOG.error("can not get feature for given data type, " + exception.getMessage());
			throw new ZinggException("DataType not supported, please check!");
		}
	}

}
