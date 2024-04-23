package zingg.common.core.similarity.function;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckNullFunctionDate extends SimFunction<Date> {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory
			.getLog(CheckNullFunctionDate.class);

	public CheckNullFunctionDate() {
		super("CheckNullFunctionDate");
	}

	public CheckNullFunctionDate(String name) {
		super(name);
	}

	@Override
	public Double call(Date first, Date second) {
		if (first != null && second != null) {
			return 1d;
		}
		return 0d;		
	}

}
