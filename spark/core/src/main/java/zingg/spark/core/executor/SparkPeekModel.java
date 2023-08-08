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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.PythonRunner;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.executor.ZinggBase;
import zingg.spark.client.ZSparkSession;

public class SparkPeekModel extends ZinggBase<ZSparkSession, Dataset<Row>, Row, Column, DataType>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.core.executor.SparkPeekModel";
	public static final Log LOG = LogFactory.getLog(SparkPeekModel.class); 
	
	public SparkPeekModel() {
		setZinggOptions(ZinggOptions.PEEK_MODEL);
		setContext(new ZinggSparkContext());
		
	}

	@Override
    public void init(Arguments args, IZinggLicense license)
        throws ZinggClientException {
		super.init(args, license);
		getContext().setUtils();
		//we wil not init here as we wnt py to drive
		//the spark session etc
		//getContext().init(license);
    }

	@Override
	public void execute() throws ZinggClientException {
		try {
			LOG.info("Generic Python phase starts");
			//LOG.info(this.getClass().getClassLoader().getResource("python/phases/assessModel.py").getFile());
			List<String> pyArgs = new ArrayList<String>();
			pyArgs.add("python/phases/"+clientOptions.get(ClientOptions.PHASE).getValue() + ".py");
			pyArgs.add("");
			for (String c: clientOptions.getCommandLineArgs()) {
				pyArgs.add(c);
			}
			PythonRunner.main(pyArgs.toArray(new String[pyArgs.size()]));

			LOG.info("Generic Python phase ends");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}


} 
