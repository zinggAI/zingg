package zingg.common.client.util;

public class WithSession<S> implements IWithSession<S> {

    S session;
    @Override
    public void setSession(S session) {
        this.session = session;
    }

    @Override
    public S getSession() {
        return session;
    }
}
