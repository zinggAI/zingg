package zingg.common.client.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

import java.util.List;

public class ValidatorService<A extends IZArgs>{

    private static final Log LOG = LogFactory.getLog(ValidatorService.class);
    protected PhaseValidatorFactory factory;

    public ValidatorService() {
        this.factory = new PhaseValidatorFactory();
    }
    public ValidatorService(PhaseValidator<A> validator, String phase) {
        this.factory = new PhaseValidatorFactory();
        this.factory.registerValidator(validator, phase);
    }

    public void validate(A arguments, String phase) throws ZinggClientException {
        PhaseValidator<A> validator = factory.getValidator(phase);

        if (validator == null) {
            LOG.info("No argument validation needed for phase: " + phase);
            return;
        }

        List<String> errors = validator.validate(arguments);

        if (!errors.isEmpty()) {
            String errorMessage = "Invalid arguments for phase '" + phase + "': " +
                    String.join(", ", errors);
            throw new ZinggClientException(errorMessage);
        }

        LOG.info("Validation successful for phase: " + phase);
    }
}

