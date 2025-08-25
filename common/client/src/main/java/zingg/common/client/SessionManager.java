package zingg.common.client;

import java.util.function.Supplier;

public class SessionManager<S> {

    private S session;
    private final Supplier<S> sessionSupplier;

    public SessionManager(Supplier<S> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }

    public S get() {
        if (session == null) {
            session = sessionSupplier.get();
        }
        return session;
    }

    public void set(S session) {
        this.session = session;
    }
}
