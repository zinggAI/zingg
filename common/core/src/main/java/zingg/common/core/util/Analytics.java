package zingg.common.core.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Analytics {

	private static final String ZINGG_HOME = "ZINGG_HOME";
	private static final String CLIENT_ID_FILE_NAME = "clientId.txt";
	protected static final String DEFAULT_CLIENT_ID = "555";
	private static final String HOST = "www.google-analytics.com";
	private static final String PATH = "/mp/collect";
	//private static final String PATH = "/debug/mp/collect"; //set the path to validate the POST request
	private static final String API_SECRET = "IYJgNn8KR2Cr0yZIvrMvng";
	private static final String API_VERSION = "2";
	private static final String MEASUREMENT_ID = "G- 	";

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
			getMetrics().put(metricName, metricValue);
		}
	}

	public static void track(String metricName, double metricValue, boolean collectMetrics) {
		track(metricName, String.valueOf(metricValue), collectMetrics);
	}

	public static void postEvent(String phase, boolean collectMetrics) {
		if (collectMetrics == false) {
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();

		rootNode.put("client_id", getClientId());

		ObjectNode eventNode = mapper.createObjectNode();
		eventNode.put("name", phase);

		ObjectNode paramNode = mapper.createObjectNode();
		for (Map.Entry<String, String> entry : metrics.entrySet()) {
			paramNode.put(entry.getKey(), entry.getValue());
		}
		eventNode.set("params", paramNode); 

		ArrayNode eventList;
		eventList = mapper.createArrayNode();
		eventList.add(eventNode);
		rootNode.set("events", eventList);
		
		Analytics.sendEvents(rootNode.toString());
	}

	protected static String getClientId() {
		
		String clientId = DEFAULT_CLIENT_ID;
		String zinggHomeVal = System.getenv(ZINGG_HOME);
		
		if (zinggHomeVal!=null) {
			try {
				//check if the file clientId.txt exists at ZINGG_HOME
				String filePath = zinggHomeVal + "/" + CLIENT_ID_FILE_NAME;
				File clientIdFile = new File(filePath);
				if (clientIdFile.exists()) {
					// read the clientId
					FileReader fr = new FileReader(clientIdFile);
					BufferedReader br = new BufferedReader(fr);
					// client id would be in the first line
					String line = br.readLine();
					fr.close();
					// if clientId not null return
					if (line != null && !(line.trim().isEmpty())) {
						clientId = line;
						return clientId;
					}
				} else {
					clientIdFile.createNewFile();
				}

				// generate and write client id
				clientId = String.valueOf(System.currentTimeMillis()+Math.round(Math.random()*1000000000));
				FileWriter filewriter = new FileWriter(clientIdFile);
				filewriter.write(clientId);
				filewriter.close();
				
				// return generated value
				return clientId;

			} catch (Exception e) {
				LOG.error("Exception occurred in reading or writing client id", e);
			} 
		}
		
		return clientId;
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
    	} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		LOG.debug("Event tracked.");
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
