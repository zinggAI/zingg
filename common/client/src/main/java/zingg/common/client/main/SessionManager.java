package zingg.common.client.main;

public class SessionManager<S> {
    private S session;
    private final SessionFactory<S> sessionFactory;

    public SessionManager(SessionFactory<S> sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public S createSession() {
        if (session == null) {
            session = sessionFactory.createSession();
        }
        return session;
    }

    public void setSession(S session) {
        this.session = session;
    }

    public S getSession() {
        return session;
    }
}
