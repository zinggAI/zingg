package zingg.common.client.util.reader;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface IDFReader<D, R, C> {

    IDFReader<D,R,C> getReader();

    IDFReader<D,R,C> format(String f);

    IDFReader<D,R,C> option(String k, String v);

    IDFReader<D,R,C> setSchema(String s);

    ZFrame<D,R,C> load() throws ZinggClientException;

    ZFrame<D,R,C> load(String location) throws ZinggClientException;
}
