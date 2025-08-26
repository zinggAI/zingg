package zingg.common.client.util.writer;

import zingg.common.client.pipe.Pipe;

public class Helper {

    private static final String PATH = "path";
    private static final String LOCATION = "location";

    public static <D, R, C> IDFWriter<D, R, C> initializeWriterForPipe(Pipe<D, R, C> pipe, IDFWriter<D, R, C> dfWriter) {
        IDFWriter<D, R, C> writer = dfWriter
                .format(pipe.getFormat());
        writer.setMode(pipe.getMode() != null ? pipe.getMode() : "Append");
        for (String key : pipe.getProps().keySet()) {
            //back compatibility
            if (LOCATION.equals(key)) {
                writer = writer.option(PATH, pipe.get(key));
            } else {
                writer = writer.option(key, pipe.get(key));
            }
        }
        return writer;
    }
}
