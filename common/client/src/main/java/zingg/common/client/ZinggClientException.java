package zingg.common.client;

/**
 * Base class for all Zingg Exceptions
 * 
 * @author sgoyal
 *
 */

public class ZinggClientException extends Exception {

	private static final long serialVersionUID = 1L;

	public ZinggClientException(String m) {
		super(m);
	}

	public ZinggClientException(String m, Throwable cause) {
		super(m, cause);
	}

	public ZinggClientException(Throwable cause) {
		super(cause);
	}

}
