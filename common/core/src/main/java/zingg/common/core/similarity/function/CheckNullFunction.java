package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckNullFunction<T> extends SimFunction<T> {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(CheckNullFunction.class);

	public CheckNullFunction(String name) {
		super(name);
	}

	@Override
	public Double call(T first, T second) {
		if (first != null && second != null) {
			return 1d;
		}
		return 0d;		
	}

	

}
