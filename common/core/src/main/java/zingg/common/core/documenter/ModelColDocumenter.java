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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.Context;

public abstract class ModelColDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.ModelColDocumenter";
	public static final Log LOG = LogFactory.getLog(ModelColDocumenter.class);

	private final String COLUMN_DOC_TEMPLATE = "columnDocTemplate.ftlh";
	private final String Z_COLUMN_TEMPLATE = "zColumnTemplate.ftlh";

	public ModelColDocumenter(Context<S,D,R,C,T> context, Arguments args) {
		super(context, args);
	}

	public void process( ZFrame<D,R,C>  data) throws ZinggClientException {
		createColumnDocuments(data);
	}

	private void createColumnDocuments( ZFrame<D,R,C>  data) throws ZinggClientException {
		LOG.info("Column Documents generation starts");
		if (!data.isEmpty()) {
			String columnsDir = args.getZinggDocDir();
			checkAndCreateDir(columnsDir);
			/* 
			for (StructField field: data.schema().fields()) {
				prepareAndWriteColumnDocument(field.name(), columnsDir);
			}
			*/
		}
		LOG.info("Column Documents generation finishes");
	}

	private void prepareAndWriteColumnDocument(String fieldName, String columnsDir) throws ZinggClientException {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.TITLE, fieldName);
		root.put(TemplateFields.MODEL_ID, args.getModelId());

		String filenameHTML = columnsDir + fieldName + ".html";
		if (isZColumn(fieldName)) {
			writeDocument(Z_COLUMN_TEMPLATE, root, filenameHTML);
		} else {
			writeDocument(COLUMN_DOC_TEMPLATE, root, filenameHTML);
		}
	}

	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}
}
