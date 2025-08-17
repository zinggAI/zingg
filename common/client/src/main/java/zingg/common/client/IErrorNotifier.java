package zingg.common.client;

public interface IErrorNotifier {
    void notify(ClientOptions options, String subject, String message);
}
