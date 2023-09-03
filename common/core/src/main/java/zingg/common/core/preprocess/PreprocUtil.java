package zingg.common.core.preprocess;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public class PreprocUtil<S,D,R,C,T> {
	
	public ZFrame<D,R,C> preprocess(S session, Arguments args, ZFrame<D,R,C> inpData) throws ZinggClientException{
		
		// go over all the preprocs and call factory and than preprocess one after the other
		String[] preprocessors = args.getPreprocessors();
		
		if (preprocessors==null || preprocessors.length==0) {
			return inpData;
		}
		
		PreprocFactory preprocFactory = new PreprocFactory();
		ZFrame<D,R,C> outData = inpData;
		
		for (String preprocName : preprocessors) {
			//get the instance from factory
			IPreProc<S, D, R, C, T> preproc;
			try {
				preproc = preprocFactory.get(preprocName);
				//call preprocess
				outData = preproc.preprocess(session,args,outData);
			} catch (Exception e) {
				throw new ZinggClientException("error occurred in preprocessing",e);
			}
		}
		
		return outData;
	}
}
