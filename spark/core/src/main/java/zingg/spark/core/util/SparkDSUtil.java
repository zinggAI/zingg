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

package zingg.spark.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import zingg.common.client.ZFrame;
import zingg.common.core.util.DSUtil;
import zingg.scala.DFUtil;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;

public class SparkDSUtil extends DSUtil<ZSparkSession, Dataset<Row>, Row, Column>{

    public SparkDSUtil(ZSparkSession s) {
        super(s);
        //TODO Auto-generated constructor stub
    }



    public static final Log LOG = LogFactory.getLog(SparkDSUtil.class);	

    

    @Override
    public ZFrame<Dataset<Row>, Row, Column> addClusterRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {

        ZSparkSession zSparkSession = getSession();
		return new SparkFrame(DFUtil.addClusterRowNumber(((Dataset<Row>)ds.df()), zSparkSession.getSession()));
    }



    @Override
    public ZFrame<Dataset<Row>, Row, Column> addRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {
    	ZSparkSession zSparkSession = getSession();
        return new SparkFrame(DFUtil.addRowNumber(((Dataset<Row>)ds.df()), zSparkSession.getSession()));
    }

	

	
	

	
}
