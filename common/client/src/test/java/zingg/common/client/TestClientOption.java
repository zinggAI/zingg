/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestClientOption {

	
	@Test
	public void testParseArguments() {
		String[] args = {"--phase", "train", 
				"--conf", "conf.json", 
				"--zinggDir", "/tmp/z_main", 
				"--email", "zingg@zingg.ai", 
				"--license", "zinggLicense.txt"};
		
		ClientOptions co = new ClientOptions(args);
		assertEquals("conf.json", co.get(ClientOptions.CONF).value);
		assertEquals("train", co.get(ClientOptions.PHASE).value);
		assertEquals("/tmp/z_main", co.get(ClientOptions.ZINGG_DIR).value);
		assertNull(co.get(ClientOptions.HELP));
	}
	
	@Test
	public void testParseUnsupportedArgumentsConf() {
		try {
			String[] args = {"--phase", "train", 
					"--conf1", "conf.json", 
					"--zinggDir", "/tmp/z_main", 
					"--email", "zingg@zingg.ai", 
					"--license", "zinggLicense.txt"};
			
			ClientOptions co = new ClientOptions(args);
			fail("exception should have been raised due to invalid conf option");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testParseUnsupportedArgumentsPhase() {
		try {
			String[] args = {"--phase1", "train", 
					"--conf1", "conf.json", 
					"--zinggDir", "/tmp/z_main", 
					"--email", "zingg@zingg.ai", 
					"--license", "zinggLicense.txt"};
			
			ClientOptions co = new ClientOptions(args);
			fail("exception should have been raised due to invalid phase option");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	
}
