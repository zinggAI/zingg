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

package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.executor.Linker;
import zingg.common.core.model.Model;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.preprocess.SparkStopWordsRemover;


public class SparkLinker extends Linker<ZSparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkLinker";
	public static final Log LOG = LogFactory.getLog(SparkLinker.class);

	public SparkLinker() {
		setZinggOptions(ZinggOptions.LINK);
		setContext(new ZinggSparkContext());
	}

    @Override
    public void init(Arguments args, IZinggLicense license)  throws ZinggClientException {
        super.init(args, license);
        getContext().init(license);
    }
	
	@Override
	protected Model getModel() throws ZinggClientException {
		Model model = getModelUtil().loadModel(false, args);
		model.register(getContext().getSession());
		return model;
	}

	@Override
	protected StopWordsRemover<ZSparkSession, Dataset<Row>, Row, Column, DataType> getStopWords() {
		return new SparkStopWordsRemover(getContext(),getArgs());
	}
	
}
