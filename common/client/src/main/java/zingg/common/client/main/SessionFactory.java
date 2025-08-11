package zingg.common.client.main;

public interface SessionFactory<S> {
    S createSession();
}
