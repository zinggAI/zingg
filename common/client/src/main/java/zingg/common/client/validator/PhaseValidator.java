package zingg.common.client.validator;

import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IZArgs;

import java.util.List;

public interface PhaseValidator<A extends IZArgs> {
    List<String> validate(A args) throws ZinggClientException;
}
