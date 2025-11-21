package zingg.common.client.validator;

import zingg.common.client.arguments.model.IZArgs;

import java.util.HashMap;
import java.util.Map;

public class PhaseValidatorFactory<A extends IZArgs> {
    protected final Map<String, PhaseValidator<A>> validators = new HashMap<>();

    public PhaseValidatorFactory() {
        // Add more validators as needed
    }

    public void registerValidator(PhaseValidator<A> validator, String phase) {
        validators.put(phase, validator);
    }

    public PhaseValidator<A> getValidator(String phase) {
        return validators.get(phase);
    }
}
