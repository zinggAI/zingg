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

package zingg.spark.core.documenter;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import freemarker.template.Version;
import zingg.common.client.Arguments;
import zingg.common.core.Context;
import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.RowWrapper;
import zingg.spark.client.ZSparkSession;

/**
 * Spark specific implementation of DataDocumenter
 *
 */
public class SparkDataDocumenter extends DataDocumenter<ZSparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;

	public SparkDataDocumenter(Context<ZSparkSession, Dataset<Row>, Row, Column,DataType> context, Arguments args) {
		super(context, args);
	}

	@Override
	public RowWrapper<Row> getRowWrapper(Version v) {
		return new SparkRowWrapper(v);
	}

}
