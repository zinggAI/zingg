package zingg.common.client.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ZinggValidationException extends Exception {
    private static final long serialVersionUID = 1L;
    private final List<String> errors = new ArrayList<>();

    public void addError(String msg) {
        errors.add(msg);
    }

    public void throwIfErrors() throws ZinggValidationException {
        if (!errors.isEmpty()) {
            throw this;
        }
    }

    @Override
    public String getMessage() {
        return String.join("; ", errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}

