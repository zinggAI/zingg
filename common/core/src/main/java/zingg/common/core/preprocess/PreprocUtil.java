package zingg.common.core.preprocess;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.util.PipeUtilBase;

public abstract class PreprocUtil<S,D,R,C,T> {
	
	/**
	 * Need to be populated by concrete sub class
	 */
	protected PreprocFactory preprocFactory;
	
	protected S session;
	
	protected PipeUtilBase<S, D, R, C> pipeUtil;

    public PipeUtilBase<S, D, R, C> getPipeUtil() {
		return pipeUtil;
	}

	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}
	
	public ZFrame<D,R,C> preprocess(Arguments args, ZFrame<D,R,C> inpData) throws ZinggClientException{
		
		// go over all the preprocs and call factory and than preprocess one after the other
		String[] preprocessors = args.getPreprocessors();
		
		if (preprocessors==null || preprocessors.length==0) {
			return inpData;
		}
		
		ZFrame<D,R,C> outData = inpData;
		
		for (String preprocName : preprocessors) {
			//get the instance from factory
			IPreProc<S, D, R, C, T> preproc;
			try {
				preproc = preprocFactory.get(preprocName);
				//call preprocess
				outData = preproc.preprocess(session,pipeUtil,args,outData);
			} catch (Exception e) {
				throw new ZinggClientException("error occurred in preprocessing",e);
			}
		}
		
		return outData;
	}
}
