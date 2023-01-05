package zingg.client;

import java.io.Serializable;

/**
 * Base class for all Zingg Exceptions
 * 
 * @author sgoyal
 *
 */

public class ZinggClientException extends Throwable implements Serializable {

	public String message;

	public ZinggClientException(String m) {
		super(m);
		this.message = m;
	}

	public ZinggClientException(String m, Throwable cause) {
		super(m, cause);
		this.message = m;
	}

	public ZinggClientException(Throwable cause) {
		super(cause);
		this.message = cause.getMessage();
	}

}
