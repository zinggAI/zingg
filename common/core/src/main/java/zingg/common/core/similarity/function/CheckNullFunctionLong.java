package zingg.common.core.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckNullFunctionLong extends SimFunction<Long> {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(CheckNullFunctionLong.class);

	public CheckNullFunctionLong() {
		super("CheckNullFunctionLong");
	}

	public CheckNullFunctionLong(String name) {
		super(name);
	}

	@Override
	public Double call(Long first, Long second) {
		if (first != null && second != null) {
			return 1d;
		}
		return 0d;		
	}

	

}
