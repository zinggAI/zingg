package zingg.common.client.main;

import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.model.IZArgs;

import java.rmi.NoSuchObjectException;

public class ConfigurationManager {
    private final IArgumentService argumentService;

    public ConfigurationManager(IArgumentService argumentService) {
        this.argumentService = argumentService;
    }

    public IZArgs loadArguments(String configPath) throws ZinggClientException, NoSuchObjectException {
        return argumentService.loadArguments(configPath);
    }

    public IZArgs buildArguments(IZArgs baseArgs, ClientOptions options) {
        // Extract argument building logic here
        return enhanceArgumentsWithOptions(baseArgs, options);
    }

    private IZArgs enhanceArgumentsWithOptions(IZArgs args, ClientOptions options) {
        // Move buildAndSetArguments logic here
        setJobId(args, options);
        setZinggDir(args, options);
        setModelId(args, options);
        setMetricsCollection(args, options);
        setConciseDisplay(args, options);
        setColumn(args, options);
        return args;
    }
}
