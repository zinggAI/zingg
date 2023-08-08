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

package zingg.spark.core.model;

import java.io.IOException;
import java.util.Map;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.Column;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.feature.Feature;

public class SparkLabelModel extends SparkModel{
	
	public SparkLabelModel(Map<FieldDefinition, Feature<DataType>> f) {
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override	
	public void fit(ZFrame<Dataset<Row>,Row,Column> pos, ZFrame<Dataset<Row>,Row,Column> neg) {
		//create features
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[pipelineStage.size()]));
		PipelineModel pm = pipeline.fit(transform(pos.union(neg)).df().coalesce(1).cache());
		transformer = pm;	
	}
	
	
	@Override
	public void save(String path) throws IOException{
		((PipelineModel) transformer).write().overwrite().save(path);
	}


}
