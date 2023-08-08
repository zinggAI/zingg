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
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.model.Model;
import zingg.common.core.util.ModelUtil;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.feature.SparkFeatureFactory;
import zingg.spark.core.model.SparkLabelModel;
import zingg.spark.core.model.SparkModel;


public class SparkModelUtil extends ModelUtil<ZSparkSession,DataType,Dataset<Row>, Row, Column> {

    public static final Log LOG = LogFactory.getLog(SparkModelUtil.class);
    

    public SparkModelUtil(ZSparkSession s) {
        this.session = s;
    }

	public Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> getModel(boolean isLabel, Arguments args) throws ZinggClientException{
        Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> model = null;
        if (isLabel) {
            model = new SparkLabelModel(getFeaturers(args));
        }
        else {
            model = new SparkModel(getFeaturers(args));            
        }
        return model;
    }

    @Override
    public Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> loadModel(boolean isLabel,
        Arguments args) throws ZinggClientException    {
        Model<ZSparkSession,DataType,Dataset<Row>, Row, Column> model = getModel(isLabel, args);
        model.load(args.getModel());
        return model;

     }

    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }

}
