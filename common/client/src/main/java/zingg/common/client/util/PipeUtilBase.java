package zingg.common.client.util;

import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.IPipeUtilReader;
import zingg.common.client.util.writer.IPipeUtilWriter;

public interface PipeUtilBase<S, D, R, C> extends IPipeUtilReader<D, R, C>, IPipeUtilWriter<D, R, C> {

	String getPipesAsString(Pipe<D, R, C>[] pipes);

	S getSession();

	void setSession(S session);
	
}