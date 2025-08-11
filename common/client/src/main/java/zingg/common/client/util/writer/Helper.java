package zingg.common.client.util.writer;

import zingg.common.client.pipe.Pipe;

public class Helper {

    public static <D, R, C> IDFWriter<D, R, C> initializeWriterForPipe(Pipe<D, R, C> pipe, IDFWriter<D, R, C> dfWriter) {
        IDFWriter<D, R, C> writer = dfWriter
                .format(pipe.getFormat());
        writer.setMode(pipe.getMode() != null ? pipe.getMode() : "Append");
        for (String key : pipe.getProps().keySet()) {
            writer = writer.option(key, pipe.get(key));
        }
        return writer;
    }
}
