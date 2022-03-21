package zingg.similarity.function;

import zingg.common.ZinggException;

public class FunctionException extends ZinggException {
	/** Constructor ZinggException creates a new ZinggException instance. */
	public FunctionException() {
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param string
	 *            of type String
	 */
	public FunctionException(String string) {
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
	public FunctionException(String string, Throwable throwable) {
		super(string, throwable);
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param throwable
	 *            of type Throwable
	 */
	public FunctionException(Throwable throwable) {
		super(throwable);
	}
}
