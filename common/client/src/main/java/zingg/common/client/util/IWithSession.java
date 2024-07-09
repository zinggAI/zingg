package zingg.common.client.util;

public interface IWithSession<S> {

    public void setSession(S s);

    public S getSession();

}
