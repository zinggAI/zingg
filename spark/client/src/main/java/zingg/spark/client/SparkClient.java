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

package zingg.spark.client;

import java.io.Serializable;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.Client;
import zingg.common.client.ClientOptions;
import zingg.common.client.IZinggFactory;
import zingg.common.client.ZinggClientException;
import zingg.common.client.license.IZinggLicense;

/**
 * This is the main point of interface with the Zingg matching product.
 * 
 * @author sgoyal
 *
 */
public class SparkClient extends Client<ZSparkSession, Dataset<Row>, Row, Column, DataType>  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public SparkClient(Arguments args, ClientOptions options) throws ZinggClientException {
		super(args, options);
		
	}

	public SparkClient(Arguments args, ClientOptions options, ZSparkSession s) throws ZinggClientException {
		super(args, options, s);
	}

	public SparkClient() {
		/*SparkSession session = SparkSession
                .builder()
                .appName("Zingg")
                .getOrCreate();
		JavaSparkContext ctx = JavaSparkContext.fromSparkContext(session.sparkContext());
        JavaSparkContext.jarOfClass(IZingg.class);
		*/

	}

	@Override
	public IZinggFactory getZinggFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (IZinggFactory) Class.forName("zingg.spark.core.executor.SparkZFactory").newInstance();
	}
	

	@Override
	public Client<ZSparkSession, Dataset<Row>, Row, Column, DataType> getClient(Arguments args, 
		ClientOptions options) throws ZinggClientException {
		// TODO Auto-generated method stub
		SparkClient client = null;
		if ((session != null)) {
			LOG.debug("Creating client with existing session");
			client = new SparkClient(args, options, session);
		}
		else {
			client = new SparkClient(args, options);
		}
		
		return client;
	}

	public static void main(String... args) {
		SparkClient client = new SparkClient();
		client.mainMethod(args);
	}

	@Override
	protected IZinggLicense getLicense(String license) throws ZinggClientException {
		return null;
	}
	
	
}