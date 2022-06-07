package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

public class PeekModel extends ZinggBase{

	protected static String name = "zingg.PeekModel";
	public static final Log LOG = LogFactory.getLog(PeekModel.class); 

    public PeekModel() {
        setZinggOptions(ZinggOptions.PEEK_MODEL);
    }

	@Override
	public void execute() throws ZinggClientException {
		try {
			LOG.info("PeekModel starts");
			
			//do something

			LOG.info("PeekModel finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

}
