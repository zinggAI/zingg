package zingg.common.client;

/**
 * To be thrown in case of business scenario which needs graceful handling
 *
 */
public class ZinggBusinessException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ZinggBusinessException(String message) {
		super(message);
	}

	public ZinggBusinessException(String message, Throwable cause) {
		super(message, cause);
	}

}
