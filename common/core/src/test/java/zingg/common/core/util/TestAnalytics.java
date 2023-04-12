package zingg.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TestAnalytics {
	
	@Test
	public void testGetClientId() {
	    String clientId = Analytics.getClientId();
	    //should be not null
	    assertNotNull(clientId);
	    
	    //return same 2nd time
	    String clientId2 = Analytics.getClientId();
	    assertEquals(clientId,clientId2);
	    
	    // should not be default i.e. 555
	    assertNotEquals(Analytics.DEFAULT_CLIENT_ID, clientId2);
	}


}
