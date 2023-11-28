package zingg.common.client;

import java.io.Serializable;

/**
 * To be thrown in case of business scenario which needs graceful handling
 *
 */
public class ZinggBusinessException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public ZinggBusinessException(String message) {
		super(message);
	}

	public ZinggBusinessException(String message, Throwable cause) {
		super(message, cause);
	}

}
