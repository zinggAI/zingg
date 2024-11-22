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
	protected IZArgs arguments;
	protected ArgumentsUtil<?> argsUtil;
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

	public Client(IZArgs args, ClientOptions options, String zFactory) throws ZinggClientException {
		setZFactoryClassName(zFactory);
		this.options = options;
    	setOptions(options);
		try {
			buildAndSetArguments(args, options);
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

	public Client(IZArgs args, ClientOptions options, S s, String zFactory) throws ZinggClientException {
		this(args, options, zFactory);
		this.session = s;
		LOG.debug("Session passed is " + s);
		if (session != null) zingg.setSession(session);
	}


	public IZinggFactory getZinggFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
        LOG.debug("z factory is " + getZFactoryClassName());
		return (IZinggFactory) Class.forName(getZFactoryClassName()).newInstance();
	}
	
	


	public void setZingg(IZArgs args, ClientOptions options) throws Exception{
		IZinggFactory zf = getZinggFactory();
		try{
			setZingg(zf.get(ZinggOptions.getByValue(options.get(ClientOptions.PHASE).value.trim())));
		}
		catch(Exception e) {
			//set default
			setZingg(zf.get(ZinggOptions.getByValue(ZinggOptions.PEEK_MODEL.getName())));
		}
	}
	

	public void setZingg(IZingg<S,D,R,C> zingg) {
		this.zingg = zingg; 
	}

	public void buildAndSetArguments(IZArgs args, ClientOptions options) {
		setOptions(options);
		int jobId = new Long(System.currentTimeMillis()).intValue();
		if (options.get(options.JOBID)!= null) {
			LOG.info("Using job id from command line");
			String j = options.get(options.JOBID).value;
			jobId = Integer.parseInt(j);
			args.setJobId(jobId);
		}
		else if (args.getJobId() != -1) {
			jobId = (args).getJobId();
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
			((IArguments) args).setShowConcise(Boolean.valueOf(j));
		}
		if (options.get(ClientOptions.COLUMN)!= null) {
			String j = options.get(ClientOptions.COLUMN).value;
			((IArguments) args).setColumn(j);
		}
		setArguments(args);
	}
	
	public void printBanner(boolean collectMetrics) {
		LOG.info("");
		LOG.info("**************************************************************************");
		LOG.info("*                                                                        *");
		LOG.info("*                                "+getProductName()+"                                *");
		LOG.info("*                        (C) 2021 Zingg Labs, Inc.                       *");
		LOG.info("*                                                                        *");
		LOG.info("*                          https://www.zingg.ai/                         *");
		LOG.info("*                                                                        *");
		LOG.info("*                        using: Zingg v"+getProductVersion()+"                    *");
		LOG.info("*                                                                        *");
		if(collectMetrics) {
			LOG.info("*            ** Note about analytics collection by Zingg AI **           *");
			LOG.info("*                                                                        *");
			LOG.info("*  Please note that Zingg captures a few metrics about application's     *");
			LOG.info("*  runtime parameters. However, no personal data or application data     *");
			LOG.info("*  is captured. If you want to switch off this feature, please set the   *");
			LOG.info("*  flag collectMetrics to false in config. For details, please refer to  *");
			LOG.info("*  the Zingg docs (https://docs.zingg.ai/zingg/security).                *");
			LOG.info("*                                                                        *");
			LOG.info("**************************************************************************");
			LOG.info("");
		}
		else {
			LOG.info("*  Zingg is not collecting any analytics data and will only log a blank  *");
			LOG.info("*                    event with name of the phase.                       *");
			LOG.info("*                                                                        *");
			LOG.info("**************************************************************************");
			LOG.info("");
		}
	}
	
	
	public ClientOptions getClientOptions(String ... args){
		return new ClientOptions(args);
	}

	public abstract Client<S,D,R,C,T> getClient(IZArgs args, ClientOptions options) throws ZinggClientException;
	
	public void mainMethod(String... args) {
		Client<S,D,R,C,T> client = null;
		ClientOptions options = null;
		
		try {
			
			for (String a: args) LOG.debug("args " + a);
			options = getClientOptions(args);
			setOptions(options);

			if (options.has(options.HELP) || options.has(options.HELP1) || options.get(ClientOptions.PHASE) == null) {
				LOG.warn(options.getHelp());
				System.exit(0);
			}
			String phase = options.get(ClientOptions.PHASE).value.trim();
			ZinggOptions.verifyPhase(phase);
			if (options.get(ClientOptions.CONF).value.endsWith("json")) {
					arguments = getArgsUtil(phase).createArgumentsFromJSON(options.get(ClientOptions.CONF).value, phase);
			}
			else if (options.get(ClientOptions.CONF).value.endsWith("env")) {
				arguments = getArgsUtil(phase).createArgumentsFromJSONTemplate(options.get(ClientOptions.CONF).value, phase);
			}
			else {
				arguments = getArgsUtil(phase).createArgumentsFromJSONString(options.get(ClientOptions.CONF).value, phase);
			}
			printBanner(arguments.getCollectMetrics());
			
			client = getClient(arguments, options);
			client.init();
			// after setting arguments etc. as some of the listeners need it
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
					+ e.getMessage());;
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
		zingg.setClientOptions(getOptions());
		zingg.init(getArguments(), getSession(),getOptions());
		if (session != null) zingg.setSession(session);
		initializeListeners();
		EventsListener.getInstance().fireEvent(new ZinggStartEvent());					
	}

	/**
	 * Stop the Spark job running context
	 */
	public void stop() throws ZinggClientException{
		zingg.cleanup();
	}

	public IZArgs getArguments() {
		return arguments;
	}

	
	public void execute() throws ZinggClientException {
		zingg.execute();
 	}

	public void postMetrics() throws ZinggClientException {
		zingg.postMetrics();
	}

	public void setArguments(IZArgs args) {
		this.arguments = args;				
	}

	public ClientOptions getOptions() {
		return options;
	}

	public void setOptions(ClientOptions options) {
		this.options = options;
	}


	protected ArgumentsUtil<?> getArgsUtil(String phase) {	
		if (argsUtil==null) {
			argsUtil = new ArgumentsUtil(Arguments.class);
		}
		return argsUtil;
	}    

	public void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        EventsListener.getInstance().addListener(eventClass, listener);
    }

    public void initializeListeners() {
        addListener(ZinggStartEvent.class, new ZinggStartListener());
        addListener(ZinggStopEvent.class, new ZinggStopListener());
    }
    
    public abstract S getSession();
    
    public void setSession(S s) {
    	this.session = s;
    }

	public abstract PipeUtilBase<S, D, R, C> getPipeUtil();

	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}

	public String getProductName(){
		return "Zingg AI";
	}

	public String getProductVersion(){
		return "0.4.1-SNAPSHOT";
	}
    
}