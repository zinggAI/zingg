package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckNullFunctionInt extends SimFunction<Integer> {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(CheckNullFunctionInt.class);

	public CheckNullFunctionInt() {
		super("CheckNullFunctionInt");
	}

	public CheckNullFunctionInt(String name) {
		super(name);
	}

	@Override
	public Double call(Integer first, Integer second) {
		if (first != null && second != null) {
			return 1d;
		}
		return 0d;		
	}

	

}
