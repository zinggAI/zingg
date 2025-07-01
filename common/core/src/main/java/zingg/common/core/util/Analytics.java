package zingg.common.core.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;

public class Analytics {

	private static final String HOST = "www.google-analytics.com";
	private static final String PATH = "/mp/collect";
	//private static final String PATH = "/debug/mp/collect"; //set the path to validate the POST request
	private static final String API_SECRET = "IYJgNn8KR2Cr0yZIvrMvng";
	private static final String API_VERSION = "2";
	private static final String MEASUREMENT_ID = "G-MWRMGB9652";
	private static final String ZINGG_USER_ENV = "ZINGG_USER";

	private static Map<String, String> metrics;
	public static final Log LOG = LogFactory.getLog(Analytics.class); 

	private static Map<String, String> getMetrics() {
		if(metrics == null) {
			metrics =  new HashMap<String, String>();
		}
		return metrics;
	}

	public static void track(String metricName, String metricValue, boolean collectMetrics) {
		if (collectMetrics) {
			String metricNameToSend = metricName.replace(".", "_");
			if (metricValue == null) metricValue = "";
			getMetrics().put(metricNameToSend, metricValue);
		}
	}

	public static void trackEnv(String metricName, boolean collectMetrics) {
		track(metricName, String.valueOf(System.getenv(metricName) != null), collectMetrics);
	}

	public static void trackProp(String metricName, boolean collectMetrics) {
		track(metricName, String.valueOf(System.getProperty(metricName) != null), collectMetrics);
	}

	public static void trackEnvValue(String metricName, boolean collectMetrics) {
		track(metricName, System.getenv(metricName), collectMetrics);
	}

	public static void trackPropValue(String metricName, boolean collectMetrics) {
		track(metricName, System.getProperty(metricName), collectMetrics);
	}

	public static void track(String metricName, double metricValue, boolean collectMetrics) {
		track(metricName, String.valueOf(metricValue), collectMetrics);
	}

	public static String getDomain(){
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			return "CouldntGetCanicalHostName";
		}
	}

	public static String getHost(){

		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "CouldntGetHost";
		}

	}
	public static void trackDomain(String metricName, boolean collectMetrics){
		track(metricName, getDomain(), collectMetrics);
	}

	public static String getUserId() {
		String userId = System.getenv(ZINGG_USER_ENV); // Fetch the environment variable
		if (userId == null || userId.isEmpty()) {
			userId = getHost(); // Default to domain if environment variable is not set
		}
		return userId;
	}


	public static void postEvent(String phase, boolean collectMetrics) {
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();

		rootNode.put("client_id", getDomain());

		ObjectNode eventNode = mapper.createObjectNode();
		eventNode.put("name", phase);

		if (collectMetrics != false) {
			ObjectNode paramNode = mapper.createObjectNode();
			for (Map.Entry<String, String> entry : metrics.entrySet()) {
				paramNode.put(entry.getKey(), entry.getValue());
			}
			eventNode.set("params", paramNode); 
		}

		ArrayNode eventList;
		eventList = mapper.createArrayNode();
		eventList.add(eventNode);
		rootNode.set("events", eventList);
		rootNode.put("user_id", getUserId());
		String metricEvent = rootNode.toString();
		LOG.debug("event is " + metricEvent);
		Analytics.sendEvents(metricEvent);
	}

	private static void sendEvents(String param) {
 		URIBuilder builder = new URIBuilder();
		builder
				.setScheme("https")
				.setHost(HOST)
				.setPath(PATH)
				.addParameter("api_secret", API_SECRET)
				.addParameter("v", API_VERSION)
				.addParameter("measurement_id", MEASUREMENT_ID); // Tracking ID

		URI uri = null;
		try {
			uri = builder.build();
			URL url = uri.toURL();
   			String response = executePostRequest(url.toString(), param);
			LOG.debug("Analytics event " + response);
    		} catch (IOException | URISyntaxException e) {
			if(LOG.isDebugEnabled()) e.printStackTrace();
		}
		LOG.warn("Event tracked.");
	}

	private static String executePostRequest(String targetURL, String urlParameters) {
		HttpURLConnection connection = null;
		try {
			//Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", 
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", 
					Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");  
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			//Send request
			DataOutputStream os = new DataOutputStream (
					connection.getOutputStream());
			os.writeBytes(urlParameters);
			os.close();

			//Get Response
			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuffer response = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			reader.close();
			return response.toString();
		} catch (Exception e) {
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
