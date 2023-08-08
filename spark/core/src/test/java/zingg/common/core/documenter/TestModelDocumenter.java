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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.util.ColName;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.documenter.SparkModelDocumenter;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestModelDocumenter extends ZinggSparkTester {
	public static final Log LOG = LogFactory.getLog(TestModelDocumenter.class);

	Arguments docArguments = new Arguments();
	
	@BeforeEach
	public void setUp(){

		try {
			String configPath = getClass().getResource("../../../../documenter/config.json").getFile();
			docArguments = Arguments.createArgumentsFromJSON(configPath);
			String zinggDirPath = getClass().getResource("../../../../"+docArguments.getZinggDir()).getFile();
			docArguments.setZinggDir(zinggDirPath);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@Test
	public void testIfModelDocumenterGeneratedDocFile() throws Throwable {
		
		try {
			Files.deleteIfExists(Paths.get(docArguments.getZinggModelDocFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ModelDocumenter modelDoc = new SparkModelDocumenter(zsCTX, docArguments);
		modelDoc.createModelDocument();

		assertTrue(Files.exists(Paths.get(docArguments.getZinggModelDocFile())), "Model documentation file is not generated");
	}

	@Test
	public void testPopulateTemplateDataWhenMarkedRecordsAreAvailable() throws Throwable {
		
		ModelDocumenter modelDoc = new SparkModelDocumenter(zsCTX, docArguments);
		modelDoc.markedRecords = zsCTX.getPipeUtil().read(false, false, zsCTX.getPipeUtil().getTrainingDataMarkedPipe(docArguments));

		Map<String, Object> root =  modelDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(modelDoc.markedRecords.columns().length, root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(modelDoc.markedRecords.collectAsList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(modelDoc.markedRecords.fieldIndex(ColName.MATCH_FLAG_COL), root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(modelDoc.markedRecords.fieldIndex(ColName.CLUSTER_COLUMN), root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}

	@Test
	public void testPopulateTemplateDataWhenMarkedRecordsAreNone() throws Throwable {
		
		ModelDocumenter modelDoc = new SparkModelDocumenter(zsCTX, docArguments);
		modelDoc.markedRecords = new SparkFrame(spark.emptyDataFrame());

		Map<String, Object> root =  modelDoc.populateTemplateData();
		assertTrue(root.containsKey(TemplateFields.MODEL_ID), "The field does not exist - " + TemplateFields.MODEL_ID);
		assertEquals(docArguments.getModelId(), root.get(TemplateFields.MODEL_ID));

		assertEquals(docArguments.getFieldDefinition().size(), root.get(TemplateFields.NUM_COLUMNS));
		assertEquals(Collections.emptyList(), root.get(TemplateFields.CLUSTERS));
		assertEquals(0, root.get(TemplateFields.ISMATCH_COLUMN_INDEX));
		assertEquals(1, root.get(TemplateFields.CLUSTER_COLUMN_INDEX));
	}
}
