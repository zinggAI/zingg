package zingg.common.client.util;

public interface WithSession<S> {

    public void setSession(S s);

    public S getSession();

}
