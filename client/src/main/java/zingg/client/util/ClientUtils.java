package zingg.client.util;

import java.net.URL;

public class ClientUtils {
	
	public static URL getImage(final String pathAndFileName) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
	    return url; //Toolkit.getDefaultToolkit().getImage(url);
	}

}
