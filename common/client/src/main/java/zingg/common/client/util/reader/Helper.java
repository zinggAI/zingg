package zingg.common.client.util.reader;

import zingg.common.client.pipe.Pipe;

import java.util.Map;

public class Helper {

    private static final String PATH = "path";
    private static final String LOCATION = "location";

    public static <D, R, C> IDFReader<D, R, C> initializeReaderForPipe(Pipe<D, R, C> pipe, IDFReader<D, R, C> dFReader) {
        IDFReader<D, R, C> reader = dFReader.format(pipe.getFormat());
        if (pipe.getSchema() != null) {
            reader = reader.setSchema(pipe.getSchema());
        }
        for (Map.Entry<String, String> entry : pipe.getProps().entrySet()) {
            //back compatibility
            if (LOCATION.equals(entry.getKey())) {
                reader = reader.option(PATH, entry.getValue());
            }   else {
                reader = reader.option(entry.getKey(), entry.getValue());
            }
        }
        return reader.option("mode", "PERMISSIVE");
    }
}
