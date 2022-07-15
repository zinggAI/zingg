package zingg.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.util.constraint.PlacementConstraintParser.SourceTags;

import zingg.client.util.Util;

public class ClientOptions {
	
	public static final String CONF = "--conf";
	public static final String PHASE = "--phase";
	public static final String LICENSE = "--license";
	public static final String PREPROCESS = "--preprocess";
	public static final String JOBID = "--jobId";
	public static final String EMAIL = "--email";
	public static final String FORMAT = "--format";
	public static final String ZINGG_DIR = "--zinggDir";
	public static final String MODEL_ID = "--modelId";
	public static final String COLLECT_METRICS = "--collectMetrics";
	public static final String SHOW_CONCISE = "--showConcise";
	public static final String LOCATION = "--location";
	public static final String COLUMN = "--column";
	
	 // Options that do not take arguments.
	public static final String HELP = "--help";
	public static final String HELP1 = "--h";
	public static final String VERBOSE = "--verbose";
	public static final String VERSION = "--version";
	
	public static final Log LOG = LogFactory.getLog(ClientOptions.class);
	
	protected static Map<String, Option> optionMaster = new HashMap<String, Option>();
	/*
	 * String optionName;
		  //String alias;
		  boolean hasArg;
		  String desc;
		  boolean isExit;
		  boolean isMandatory;
	 */
	static {	//This is the canonical list of Zingg options.
		optionMaster.put(CONF, new Option(CONF, true, "JSON configuration with data input output locations and field definitions", false, true));
		optionMaster.put(PHASE, new Option(PHASE, true, Util.join(ZinggOptions.getAllZinggOptions(), "|"), false, true, ZinggOptions.getAllZinggOptions()));
		optionMaster.put(LICENSE, new Option(LICENSE, true, "location of license file", false, true));
		optionMaster.put(JOBID, new Option(JOBID, true, "database job id for logging", false, false));
		optionMaster.put(EMAIL, new Option(EMAIL, true, "notification email id. Can be an alias", false, false));
		optionMaster.put(FORMAT, new Option(FORMAT, true, "format of the data", false, false));
		optionMaster.put(ZINGG_DIR, new Option(ZINGG_DIR, true, "location of Zingg models, defaults to /tmp/zingg", false, false));
		optionMaster.put(MODEL_ID, new Option(MODEL_ID, true, "model identifier, can be a number ", false, false));
		optionMaster.put(COLLECT_METRICS, new Option(COLLECT_METRICS, true, "collect analytics, true/false  ", false, false));
		optionMaster.put(SHOW_CONCISE, new Option(SHOW_CONCISE, true, "Display only fields that are used to make model, true/false  ", false, false));
		optionMaster.put(LOCATION, new Option(LOCATION, true, "location of CSV file for exported data ", false, false));
		optionMaster.put(COLUMN, new Option(COLUMN, true, "name of the column", false, false));
		
		//no args
		optionMaster.put(HELP,new Option(HELP,  false, "print usage information", true, false));
		optionMaster.put(HELP1,new Option(HELP1,  false, "print usage information", true, false));
		optionMaster.put(PREPROCESS, new Option(PREPROCESS, false, "convert files to unix format", false, false));
		///optionMaster.add(new Option(VERBOSE, false, "verbose logging", true, false));
		//optionMaster.add(new Option(VERSION,  false, "version information", true, false));
	
	}
	
	protected Map<String, OptionWithVal> options = new HashMap<String, OptionWithVal> ();
	
	public ClientOptions(String... args) {
		parse(Arrays.asList(args));
	}
	
	public ClientOptions(List<String> args) {
		parse(args);
	}
	
	
	  /**
	   * Parse a list of Zingg command line options.
	   * <p>
	   * 
	   * @throws IllegalArgumentException If an error is found during parsing.
	   */
	  public final Option parse(List<String> args) {
	    Pattern eqSeparatedOpt = Pattern.compile("(--[^=]+)=(.+)");

	    int idx = 0;
	    for (idx = 0; idx < args.size(); idx++) {
	      String arg = args.get(idx);
	      String value = null;
 
	      Matcher m = eqSeparatedOpt.matcher(arg);
	      if (m.matches()) {
	        arg = m.group(1);			
	        value = m.group(2);	
	      }

	      // Look for options with a value.
	      Option opt = findCliOption(arg);
	      if (opt != null) {
	    	if (opt.isExit) return opt;
	        if (value == null && opt.hasArg) {
	          if (idx == args.size() - 1) {
	            throw new IllegalArgumentException(
	                String.format("Missing argument for option '%s'.", arg));
	          }
	          idx++;
	          value = args.get(idx);
	        }
	        //put null or actual value
			options.put(opt.optionName, new OptionWithVal(opt, value));	        
	      }
	      else {   
			  if (!handleUnknown(arg)) {
	    		  break;
	    	  }
	      }
	    }

	    /*if (idx < args.size()) {
	      idx++;
	    }
	    handleExtraArgs(args.subList(idx, args.size()));
	    */
	    for (Option o: optionMaster.values()) {
	    	if (o.isMandatory && !options.containsKey(o.optionName)) {
	    		LOG.warn("Missing required option " + o.optionName);
	    		LOG.warn(getHelp());
	    		throw new IllegalArgumentException("Missing mandatory option " + o.optionName);
	    	}
	    }
	    return null;
	  }

	 

	  /**
	   * Callback for when an unrecognized option is parsed.
	   *
	   * @param opt Unrecognized option from the command line.
	   * @return Whether to continue parsing the argument list.
	   */
	  protected boolean handleUnknown(String opt) {
		  LOG.warn("Could not recognize argument " + opt);
		  LOG.warn(getHelp());
	    throw new UnsupportedOperationException("Do not recognize option " + opt);
	  }

	  /**
	   * Callback for remaining command line arguments after either {@link #handle(String, String)} or
	   * {@link #handleUnknown(String)} return "false". This will be called at the end of parsing even
	   * when there are no remaining arguments.
	   *
	   * @param extra List of remaining arguments.
	   */
	  protected void handleExtraArgs(List<String> extra) {
		LOG.warn(getHelp());
	    throw new UnsupportedOperationException();
	  }

	  private Option findCliOption(String name) {
	    return optionMaster.get(name);
	  }
	  
	  public static class Option {
		  String optionName;
		  //String alias;
		  boolean hasArg;
		  String desc;
		  boolean isExit;
		  boolean isMandatory;
		  Set<String> options;
		  
		  protected Option(){
			  
		  }
		  
		  public Option(String o, boolean isArg, String d, boolean e, boolean m) {
			  this.optionName = o;
			  //this.alias = a;
			  this.hasArg = isArg;
			  this.desc = d;
			  this.isExit = e;
			  this.isMandatory = m;
		  }	  
		  

		  public Option(String o, boolean isArg, String d, boolean e, boolean m, String... options) {
			  this.optionName = o;
			  //this.alias = a;
			  this.hasArg = isArg;
			  this.desc = d;
			  this.isExit = e;
			  this.isMandatory = m;
			  this.options = new TreeSet<String>();
			  for (String op: options) this.options.add(op);
		  }	  
	  }
	  
	  public final static class OptionWithVal extends Option{
		  Option option;
		  String value;
		  
		  public OptionWithVal(Option o, String v) {
			  this.option = o;
			  this.value = v;
			  if (hasArg && v != null) {
				  if (!this.options.contains(value.trim())) throw new IllegalArgumentException("Unexpected argument " + value + " for " + o);
			  }
		  }

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		  
		  
		  
	  }
	  
	  public final static String getHelp() {
		StringBuffer s = new StringBuffer();
		s.append("zingg.sh [options]\n");
		s.append("zingg.sh --phase train --conf config.json\n");
		s.append("zingg.sh --phase findTrainingData --conf config.json --zinggDir /location \n");
		s.append("zingg.sh --phase label --conf config.json --zinggDir /location \n");
		s.append("zingg.sh --phase trainMatch --conf config.json --email sendMe@email.com\n");
		s.append("zingg.sh --phase findAndLabel --conf config.json --zinggDir /location\n");
		s.append("options\n");

		int maxlo = 0;
		for (Option o: optionMaster.values()){
			maxlo=Math.max(maxlo,o.optionName.length());
		}

		int maxld = 0;
		for (Option o: optionMaster.values()){
			maxld=Math.max(maxld,o.desc.length());
		}
		
		StringBuilder formatBuilder = new StringBuilder();
		formatBuilder.append("\t").append("%-").append(maxlo + 5).append("s").append(": ").append("%-").append(maxld + 5).append("s").append("\n");
		String format = formatBuilder.toString();
		
		for (Option o: optionMaster.values()) {
			s.append(String.format(format,o.optionName, o.desc));
		}
		return s.toString();
	  }
	  
	  public OptionWithVal get(String a) {
		  //if (optionMaster.containsKey(a)) 
		  return options.get(a);
		  //throw new IllegalArgumentException("Wrong argument");
	  }

	  public boolean has(String key) {
		  return options.containsKey(key);
				  
	  }
	  
	  public String getOptionValue(String a) {
		  //if (optionMaster.containsKey(a)) 
		  return get(a).getValue();
		  //throw new IllegalArgumentException("Wrong argument");
	  }
	  




}