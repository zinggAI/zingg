package zingg.common.core;

public class ZinggBusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Constructor ZinggBusinessException creates a new ZinggBusinessException instance. */
    public ZinggBusinessException() {
    }

    /**
     * Constructor ZinggBusinessException creates a new ZinggBusinessException instance.
     *
     * @param string of type String
     */
    public ZinggBusinessException(String string) {
        super(string);
    }

    /**
     * Constructor ZinggBusinessException creates a new ZinggBusinessException instance.
     *
     * @param string of type String
     * @param throwable of type Throwable
     */
    public ZinggBusinessException(String string, Throwable throwable) {
        super(string, throwable);
    }

    /**
     * Constructor ZinggBusinessException creates a new ZinggBusinessException instance.
     *
     * @param throwable of type Throwable
     */
    public ZinggBusinessException(Throwable throwable) {
        super(throwable);
    }
}
