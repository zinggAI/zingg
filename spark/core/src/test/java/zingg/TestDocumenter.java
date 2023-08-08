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

package zingg;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;

import zingg.common.client.Arguments;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestDocumenter extends ZinggSparkTester{
    
    @BeforeEach
    public void setUp(){

        try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testDocumenter/config.json").getFile());
           	//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
            e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
            fail(e.getMessage());
		}
    }

    /*
    @Test
    public void testOutput() throws Throwable{
        Documenter doc = new Documenter();
        doc.init(args, "");
        doc.setArgs(args);
        doc.execute();
    }
    */
}
