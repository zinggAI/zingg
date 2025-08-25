package zingg.common.client;

public class ZinggFactoryProvider {

    public static IZinggFactory getZinggFactory(String factoryClass) throws ZinggClientException {
        try {
            return (IZinggFactory) Class.forName(factoryClass).getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            throw new ZinggClientException("Failed to get Zingg factory for " + factoryClass);
        }
    }
}
