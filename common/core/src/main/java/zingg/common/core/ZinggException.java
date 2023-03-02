package zingg.common.core;

import java.io.Serializable;

public class ZinggException extends RuntimeException implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Constructor ZinggException creates a new ZinggException instance. */
	public ZinggException() {
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param string
	 *            of type String
	 */
	public ZinggException(String string) {
		super(string);
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param string
	 *            of type String
	 * @param throwable
	 *            of type Throwable
	 */
	public ZinggException(String string, Throwable throwable) {
		super(string, throwable);
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param throwable
	 *            of type Throwable
	 */
	public ZinggException(Throwable throwable) {
		super(throwable);
	}
}
