package zingg.client.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
	private static final String API_SECRET = "LWZHm7tASgOl2VHpy_LR8A";
	private static final String API_VERSION = "2";
	private static final String MEASUREMENT_ID = "G-VFQXB5JFC1";

	private static ArrayNode eventList;
	private static Map<String, String> specificParams;

	public static ArrayNode getEventList() {
		if(eventList == null) {
			ObjectMapper mapper = new ObjectMapper();
			eventList = mapper.createArrayNode();
		}
		return eventList;
	}

	public static Map<String, String> getSpecificParams() {
		if(specificParams == null) {
			specificParams =  new HashMap<String, String>();
		}
		return specificParams;
	}

	public static void addEvent(String name, String value) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode eventNode = mapper.createObjectNode();
		eventNode.put("name",  name);
		ObjectNode paramNode = mapper.createObjectNode();
		paramNode.put(name, value);
 		eventNode.set("params", paramNode);

		getEventList().add(eventNode);
	}

	public static String trackParameters(ObjectNode eventNode) {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();

		rootNode.put("client_id", "555");
		ObjectNode paramNode = (ObjectNode) eventNode.get("params");
		for (Map.Entry<String, String> entry : specificParams.entrySet()) {
			paramNode.put(entry.getKey(), entry.getValue());
		}

		getEventList().add(eventNode);
		rootNode.set("events", getEventList());

		return rootNode.toString();
	}

	public static final Log LOG = LogFactory.getLog(Analytics.class); 

	public static void track(String param) {
		LOG.warn("Entering Analytics.track()");
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
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		try {
			URL url = uri.toURL();
			LOG.warn("URL: " + url.toString());
			LOG.warn("param: " + param);
 			String response = executePostRequest(url.toString(), param);
			LOG.warn("Response: " + response);
  		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.debug("Event tracked.");
	}

	public static String executePostRequest(String targetURL, String urlParameters) {
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
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}