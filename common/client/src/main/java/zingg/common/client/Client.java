package zingg.common.client;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.event.events.IEvent;
import zingg.common.client.event.events.ZinggStartEvent;
import zingg.common.client.event.events.ZinggStopEvent;
import zingg.common.client.event.listeners.EventsListener;
import zingg.common.client.event.listeners.IEventListener;
import zingg.common.client.event.listeners.ZinggStartListener;
import zingg.common.client.event.listeners.ZinggStopListener;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.Email;
import zingg.common.client.util.EmailBody;
import zingg.common.client.util.PipeUtilBase;

/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public abstract class Client<S,D,R,C,T> implements Serializable {
	private static final long serialVersionUID = 1L;
	protected IArguments arguments;
	protected ArgumentsUtil argsUtil;
	protected IZingg<S,D,R,C> zingg;
	protected ClientOptions options;
	protected S session;
	protected PipeUtilBase<S,D,R,C> pipeUtil;
	public static final Log LOG = LogFactory.getLog(Client.class);

	protected String zFactoryClassName;


	/**
	 * Construct a client to Zingg using provided arguments and spark master.
	 * If running locally, set the master to local.
	 * 
	 * @param args
	 *            - arguments for training and matching
	 * @throws ZinggClientException
	 *             if issue connecting to master
	 */
	
	public Client(String zFactory) {
		setZFactoryClassName(zFactory);
	}

	public Client(IArguments args, ClientOptions options, String zFactory) throws ZinggClientException {
		setZFactoryClassName(zFactory);
		this.options = options;
    	setOptions(options);
		try {
			buildAndSetArguments(args, options);
			printAnalyticsBanner(arguments.getCollectMetrics());
			setZingg(args, options);					
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException("An error has occured while setting up the client" + e.getMessage());
		}
	}


    public String getZFactoryClassName() {
        return zFactoryClassName;
    }

    public void setZFactoryClassName(String s) {
        this.zFactoryClassName = s;
    }

	public Client(IArguments args, ClientOptions options, S s, String zFactory) throws ZinggClientException {
		this(args, options, zFactory);
		this.session = s;
		LOG.debug("Session passed is " + s);
		if (session != null) zingg.setSession(session);
	}


	public IZinggFactory getZinggFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
        LOG.debug("z factory is " + getZFactoryClassName());
		return (IZinggFactory) Class.forName(getZFactoryClassName()).newInstance();
	}
	
	


	public void setZingg(IArguments args, ClientOptions options) throws Exception{
		IZinggFactory zf = getZinggFactory();
		try{
			setZingg(zf.get(ZinggOptions.getByValue(options.get(ClientOptions.PHASE).value.trim())));
		}
		catch(Exception e) {
			e.printStackTrace();
			//set default
			setZingg(zf.get(ZinggOptions.getByValue(ZinggOptions.PEEK_MODEL.getName())));
		}
	}
	

	public void setZingg(IZingg<S,D,R,C> zingg) {
		this.zingg = zingg; 
	}

	public void buildAndSetArguments(IArguments args, ClientOptions options) {
		setOptions(options);
		int jobId = new Long(System.currentTimeMillis()).intValue();
		if (options.get(options.JOBID)!= null) {
			LOG.info("Using job id from command line");
			String j = options.get(options.JOBID).value;
			jobId = Integer.parseInt(j);
			args.setJobId(jobId);
		}
		else if (args.getJobId() != -1) {
			jobId = args.getJobId();
		}
		
		//override value of zinggDir passed from command line
		if (options.get(options.ZINGG_DIR)!= null) {
			LOG.info("Using zingg dir from command line");
		
			String j = options.get(options.ZINGG_DIR).value;
			args.setZinggDir(j);
		}
		if (options.get(options.MODEL_ID)!= null) {
			LOG.info("Using model id from command line");
		
			String j = options.get(options.MODEL_ID).value;
			args.setModelId(j);
		}
		if (options.get(options.COLLECT_METRICS)!= null) {
			String j = options.get(options.COLLECT_METRICS).value;
			args.setCollectMetrics(Boolean.valueOf(j));
		}
		if (options.get(ClientOptions.SHOW_CONCISE)!= null) {
			String j = options.get(ClientOptions.SHOW_CONCISE).value;
			args.setShowConcise(Boolean.valueOf(j));
		}
		if (options.get(ClientOptions.COLUMN)!= null) {
			String j = options.get(ClientOptions.COLUMN).value;
			args.setColumn(j);
		}
		setArguments(args);
	}
	
	public void printBanner() {
		String versionStr = "0.4.1-SNAPSHOT";
		LOG.info("");
		LOG.info("********************************************************");
		LOG.info("*                    Zingg AI                          *");
		LOG.info("*               (C) 2021 Zingg.AI                      *");
		LOG.info("********************************************************");
		LOG.info("");
		LOG.info("using: Zingg v" + versionStr);
		LOG.info("");
	}
	
	public void printAnalyticsBanner(boolean collectMetrics) {
		if(collectMetrics) {
			LOG.info("");
			LOG.info("**************************************************************************");
			LOG.info("*            ** Note about analytics collection by Zingg AI **           *");
			LOG.info("*                                                                        *");
			LOG.info("*  Please note that Zingg captures a few metrics about application's     *");
			LOG.info("*  runtime parameters. However, no user's personal data or application   *");
			LOG.info("*  data is captured. If you want to switch off this feature, please      *");
			LOG.info("*  set the flag collectMetrics to false in config. For details, please   *");
			LOG.info("*  refer to the Zingg docs (https://docs.zingg.ai/docs/security.html)    *");
			LOG.info("**************************************************************************");
			LOG.info("");
		}
		else {
			LOG.info("");
			LOG.info("*************************************************************************************************************");
			LOG.info("*    Zingg is not collecting any analytics data and will only log a blank event with the name of the phase  *");
			LOG.info("*************************************************************************************************************");
			LOG.info("");
		}
	}

	public abstract Client<S,D,R,C,T> getClient(IArguments args, ClientOptions options) throws ZinggClientException;
	
	public void mainMethod(String... args) {
		printBanner();
		Client<S,D,R,C,T> client = null;
		ClientOptions options = null;
		try {
			
			for (String a: args) LOG.debug("args " + a);
			options = new ClientOptions(args);
			setOptions(options);

			if (options.has(options.HELP) || options.has(options.HELP1) || options.get(ClientOptions.PHASE) == null) {
				LOG.warn(options.getHelp());
				System.exit(0);
			}
			String phase = options.get(ClientOptions.PHASE).value.trim();
			ZinggOptions.verifyPhase(phase);
			IArguments arguments = null;
			if (options.get(ClientOptions.CONF).value.endsWith("json")) {
					arguments = getArgsUtil().createArgumentsFromJSON(options.get(ClientOptions.CONF).value, phase);
			}
			else if (options.get(ClientOptions.CONF).value.endsWith("env")) {
				arguments = getArgsUtil().createArgumentsFromJSONTemplate(options.get(ClientOptions.CONF).value, phase);
			}
			else {
				arguments = getArgsUtil().createArgumentsFromJSONString(options.get(ClientOptions.CONF).value, phase);
			}

			// after setting arguments as some of the listeners need arguments
			initializeListeners();
			EventsListener.getInstance().fireEvent(new ZinggStartEvent());
			
			client = getClient(arguments, options);
			client.init();
			client.execute();
			client.postMetrics();
			LOG.warn("Zingg processing has completed");				
		} 
		catch(ZinggClientException e) {
			if (options != null && options.get(ClientOptions.EMAIL) != null) {
				Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running Zingg job",
					"Zingg Error ",
					e.getMessage()));
			}
			LOG.warn("Apologies for this message. Zingg has encountered an error. "
					+ e.getMessage());
			e.printStackTrace();
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
		catch( Throwable e) {
			if (options != null && options.get(ClientOptions.EMAIL) != null) {
				Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running Zingg job",
					"Zingg Error ",
					e.getMessage()));
			}
			LOG.warn("Apologies for this message. Zingg has encountered an error. "
					+ e.getMessage());
					e.printStackTrace();
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
		finally {
			try {
				EventsListener.getInstance().fireEvent(new ZinggStopEvent());
				if (client != null) {
					//client.postMetrics();
					client.stop();
				}
			}
			catch(ZinggClientException e) {
				if (options != null && options.get(ClientOptions.EMAIL) != null) {
					Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running Zingg job",
						"Zingg Error ",
						e.getMessage()));
				}
			}
		}
	}

	public void init() throws ZinggClientException {
		zingg.init(getArguments(), getSession());
		if (session != null) zingg.setSession(session);
		
	}

	/**
	 * Stop the Spark job running context
	 */
	public void stop() throws ZinggClientException{
		zingg.cleanup();
	}

	public IArguments getArguments() {
		return arguments;
	}

	public void execute() throws ZinggClientException {
		zingg.execute();
 	}

	public void postMetrics() throws ZinggClientException {
		zingg.postMetrics();
	}

	public void setArguments(IArguments args) {
		this.arguments = args;				
	}

	public ClientOptions getOptions() {
		return options;
	}

	public void setOptions(ClientOptions options) {
		this.options = options;
	}

	public Long getMarkedRecordsStat(ZFrame<D,R,C>  markedRecords, long value) {
		return zingg.getMarkedRecordsStat(markedRecords, value);
	}

    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		return zingg.getMatchedMarkedRecordsStat(markedRecords);
	}

    public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		return zingg.getUnmatchedMarkedRecordsStat(markedRecords);
	}

    public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C>  markedRecords) {
		return zingg.getUnsureMarkedRecordsStat(markedRecords);
	}

	public ZFrame<D,R,C>  getMarkedRecords() {
		return zingg.getMarkedRecords();
	}

	public ZFrame<D,R,C>  getUnmarkedRecords() {
		return zingg.getUnmarkedRecords();
	}

    public ITrainingDataModel<S, D, R, C> getTrainingDataModel() throws UnsupportedOperationException {
    	return zingg.getTrainingDataModel();
    }    

    public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() throws UnsupportedOperationException {
    	return zingg.getLabelDataViewHelper();
    }

	protected ArgumentsUtil getArgsUtil() {	
		if (argsUtil==null) {
			argsUtil = new ArgumentsUtil();
		}
		return argsUtil;
	}    

	public void addListener(IEvent event, IEventListener listener) {
        EventsListener.getInstance().addListener(event.getClass(), listener);
    }

    public void initializeListeners() {
        addListener(new ZinggStartEvent(), new ZinggStartListener());
        addListener(new ZinggStopEvent(), new ZinggStopListener());
    }
    
    public abstract S getSession();
    
    public void setSession(S s) {
    	this.session = s;
    }

	public abstract PipeUtilBase<S, D, R, C> getPipeUtil();

	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}
    
}