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

package zingg.common.core.documenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.spark.core.documenter.SparkDataDocumenter;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestDataDocumenter extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestDataDocumenter.class);

	private Arguments docArguments = new Arguments();
	@BeforeEach
	public void setUp(){
		try {
			String configPath = getClass().getResource("../../../../documenter/config.json").getFile();
			docArguments = Arguments.createArgumentsFromJSON(configPath);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@Test
	public void testPopulateTemplateData() throws Throwable {
		
		DataDocumenter dataDoc = new SparkDataDocumenter(zsCTX, docArguments);
		Pipe[] dataPipeArr = docArguments.getData();
		
		for (int i = 0; i < dataPipeArr.length; i++) {
			String file = getClass().getResource("../../../../documenter/test.csv").getFile();
			dataPipeArr[i].setProp(FilePipe.LOCATION, file);
		}
				
		dataDoc.data = zsCTX.getPipeUtil().read(false, false, dataPipeArr);
		
		

		Map<String, Object> root =  dataDoc.populateTemplateData();
		
		assertTrue(root.containsKey(TemplateFields.TITLE), "The field does not exist - " + TemplateFields.TITLE);
		assertEquals(DataDocumenter.TEMPLATE_TITLE, root.get(TemplateFields.TITLE));

		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertTrue(root.containsKey(TemplateFields.DATA_FIELDS_LIST), "The field does not exist. - " + TemplateFields.DATA_FIELDS_LIST);
	}
}
