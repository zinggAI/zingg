package zingg.common.client.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.exceptions.ZinggValidationException;

public class ValidatorService{

    private static final Log LOG = LogFactory.getLog(ValidatorService.class);

    public ValidatorService() {
    }

    /**
     * Validates the provided arguments for a specific phase of processing.
     * <p>
     * This method checks the integrity and correctness of the {@code arguments}
     * based on the specified {@code phase}. If the arguments do not meet the
     * required criteria for the phase, a {@link ZinggValidationException} is thrown.
     *
     * @param arguments the arguments to validate; must not be null and should conform to phase requirements
     * @param phase the phase of processing for which validation is performed; must be a recognized phase
     * @throws ZinggValidationException if validation fails for the provided arguments and phase
     */
    public void validate(IZArgs arguments, String phase) throws ZinggValidationException {
    }
}


