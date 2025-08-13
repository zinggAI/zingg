package zingg.common.client.util.writer;

public interface IDFWriter<D,R,C> {
    void setMode(String s);
    IDFWriter<D,R,C> format(String f);
    IDFWriter<D,R,C> option(String k, String v);
    void save();
}
