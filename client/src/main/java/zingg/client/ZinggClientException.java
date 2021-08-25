/*
 * Copyright Nube Technologies 2014
 */
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

}
