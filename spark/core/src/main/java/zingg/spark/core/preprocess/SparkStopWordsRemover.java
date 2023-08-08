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

package zingg.spark.core.preprocess;

import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.lit;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.core.Context;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;

public class SparkStopWordsRemover extends StopWordsRemover<ZSparkSession,Dataset<Row>,Row,Column,DataType>  implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.preprocess.SparkStopWordsRemover";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRemover.class);
	
	private String udfName;
	
	public SparkStopWordsRemover(Context<ZSparkSession, Dataset<Row>, Row, Column,DataType> context,Arguments args) {
		super(context,args);
		this.udfName = registerUDF();
	}
	
 	@Override
	protected ZFrame<Dataset<Row>, Row, Column> removeStopWordsFromDF(ZFrame<Dataset<Row>, Row, Column> ds,
			String fieldName, String pattern) {
 		Dataset<Row> dfAfterRemoval = ds.df().withColumn(fieldName,callUDF(udfName, ds.df().col(fieldName),lit(pattern)));
		return new SparkFrame(dfAfterRemoval);
	}

	protected String registerUDF() {
		RemoveStopWordsUDF removeStopWordsUDF = new RemoveStopWordsUDF();
		// Each field will have different pattern
		String udfName = removeStopWordsUDF.getName();
		// register the UDF
		ZSparkSession zSession = getContext().getSession();
		zSession.getSession().udf().register(udfName, removeStopWordsUDF, DataTypes.StringType);
		return udfName;
	}

}
