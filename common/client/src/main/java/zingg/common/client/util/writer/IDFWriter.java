package zingg.common.client.util.writer;

import zingg.common.client.pipe.Pipe;

public interface IDFWriter<D,R,C> {
    void setMode(String s);
    IDFWriter<D,R,C> format(String f);
    IDFWriter<D,R,C> option(String k, String v);
    void save();
    void write(Pipe<D, R, C> pipe) throws Exception;
}
