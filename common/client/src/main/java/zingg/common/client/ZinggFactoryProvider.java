package zingg.common.client;

import zingg.common.client.options.ZinggOptions;

public class ZinggFactoryProvider<S,D,R,C> {

    private final String factoryClass;

    public ZinggFactoryProvider(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    @SuppressWarnings("unchecked")
    public IZingg<S,D,R,C> create(String phase) throws ZinggClientException {
        try {
            IZinggFactory factory = (IZinggFactory) Class.forName(factoryClass).getDeclaredConstructor().newInstance();
            return factory.get(ZinggOptions.getByValue(phase));
        } catch (Exception e) {
            throw new ZinggClientException("Failed to instantiate Zingg for phase " + phase, e);
        }
    }
}
