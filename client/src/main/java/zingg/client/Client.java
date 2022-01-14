package zingg.client;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;

import zingg.client.util.Email;
import zingg.client.util.EmailBody;

/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public class Client implements Serializable {
	private Arguments arguments;
	private IZingg zingg;
	private ClientOptions options;

	public static final Log LOG = LogFactory.getLog(Client.class);


	/**
	 * Construct a client to Zingg using provided arguments and spark master.
	 * If running locally, set the master to local.
	 * 
	 * @param args
	 *            - arguments for training and matching
	 * @throws ZinggClientException
	 *             if issue connecting to master
	 */
	
	
	public Client(Arguments args, ClientOptions options) throws ZinggClientException {
		this.options = options;
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

	public void setZingg(Arguments args, ClientOptions options) throws Exception{
		JavaSparkContext.jarOfClass(IZinggFactory.class);
		IZinggFactory zf = (IZinggFactory) Class.forName("zingg.ZFactory").newInstance();
		setZingg(zf.get(ZinggOptions.getByValue(options.get(ClientOptions.PHASE).value.trim())));
	}

	public void setZingg(IZingg zingg) {
		this.zingg = zingg; 
	}

	public void buildAndSetArguments(Arguments args, ClientOptions options) {
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
		setArguments(args);
	}
	
	public static void printBanner() {
		String versionStr = "0.3.2";
		LOG.info("");
		LOG.info("********************************************************");
		LOG.info("*                    Zingg AI                          *");
		LOG.info("*               (C) 2021 Zingg.AI                      *");
		LOG.info("********************************************************");
		LOG.info("");
		LOG.info("using: Zingg v" + versionStr);
		LOG.info("");
	}
	
	public static void printAnalyticsBanner(boolean collectMetrics) {
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
			LOG.info("********************************************************");
			LOG.info("*    Zingg is not collecting any analytics data        *");
			LOG.info("********************************************************");
			LOG.info("");
		}
	}

	public static void main(String... args) {
		printBanner();
		Client client = null;
		ClientOptions options = null;
		try {
			for (String a: args) LOG.debug("args " + a);
			options = new ClientOptions(args);
		
			if (options.has(options.HELP) || options.has(options.HELP1) || options.get(ClientOptions.PHASE) == null) {
				LOG.warn(options.getHelp());
				System.exit(0);
			}
			String phase = options.get(ClientOptions.PHASE).value.trim();
			ZinggOptions.verifyPhase(phase);
			Arguments arguments = null;
			if (options.get(ClientOptions.CONF).value.endsWith("json")) {
					arguments = Arguments.createArgumentsFromJSON(options.get(ClientOptions.CONF).value, phase);
			}
			else if (options.get(ClientOptions.CONF).value.endsWith("env")) {
				arguments = Arguments.createArgumentsFromJSONTemplate(options.get(ClientOptions.CONF).value, phase);
			}
			else {
				arguments = Arguments.createArgumentsFromJSONString(options.get(ClientOptions.CONF).value, phase);
			}

			client = new Client(arguments, options);	
			client.init();
			client.execute();
			client.postMetrics();
			LOG.warn("Zingg processing has completed");				
		} 
		catch(ZinggClientException e) {
			if (options != null) {
				Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running Zingg job",
					"Zingg Error ",
					e.getMessage()));
			}
			LOG.warn("Apologies for this message. Zingg has encountered an error. "
					+ e.getMessage());
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
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
		finally {
			try {
				if (client != null) {
					client.stop();
				}
			}
			catch(ZinggClientException e) {
				if (options != null) {
					Email.email(options.get(ClientOptions.EMAIL).value, new EmailBody("Error running Zingg job",
						"Zingg Error ",
						e.getMessage()));
				}
			}
		}
	}

	public void init() throws ZinggClientException {
		zingg.init(getArguments(), "");
	}
	
	
	/**
	 * Stop the Spark job running context
	 */
	public void stop() throws ZinggClientException{
		zingg.cleanup();
	}

	public Arguments getArguments() {
		return arguments;
	}

	public void execute() throws ZinggClientException {
		zingg.execute();
 	}

	public void postMetrics() throws ZinggClientException {
		zingg.postMetrics();
	}

	public void setArguments(Arguments args) {
		this.arguments = args;				
	}

	public ClientOptions getOptions() {
		return options;
	}

	public void setOptions(ClientOptions options) {
		this.options = options;
	}
}