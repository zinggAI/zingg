package zingg.common.client.util.reader;

import zingg.common.client.pipe.Pipe;

import java.util.Map;

public class Helper {

    public static <D, R, C> DFReader<D, R, C> initializeReaderForPipe(Pipe<D, R, C> pipe, DFReader<D, R, C> dfReader) {
        DFReader<D, R, C> reader = dfReader.format(pipe.getFormat());
        if (pipe.getSchema() != null) {
            reader = reader.setSchema(pipe.getSchema());
        }
        for (Map.Entry<String, String> entry : pipe.getProps().entrySet()) {
            reader = reader.option(entry.getKey(), entry.getValue());
        }
        return reader.option("mode", "PERMISSIVE");
    }
}
