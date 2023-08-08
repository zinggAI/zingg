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

package zingg.common.core.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.feature.Feature;

public abstract class Model<S,T,D,R,C> implements Serializable {
	
	public static final Log LOG = LogFactory.getLog(Model.class);
	//private Map<FieldDefinition, Feature> featurers;

	public Model() {
	}
	
	
	public abstract void register(S spark) ;
	
	public static double[] getGrid(double begin, double end, double jump, boolean isMultiple) {
		List<Double> alphaList = new ArrayList<Double>();
		if (isMultiple) {
			for (double alpha =begin; alpha <= end; alpha *= jump) {
				alphaList.add(alpha);
			}
		}
		else {
			for (double alpha =begin; alpha <= end; alpha += jump) {
				alphaList.add(alpha);
			}
		}
		double[] retArr = new double[alphaList.size()];
		for (int i=0; i < alphaList.size(); ++i) {
			retArr[i] = alphaList.get(i);
		}
		return retArr;
	}
	
	public abstract void fit(ZFrame<D,R,C> pos, ZFrame<D,R,C> neg);
	
	
	public abstract void load(String path);
	
	
	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data);
	
	public abstract ZFrame<D,R,C> predict(ZFrame<D,R,C> data, boolean isDrop) ;

	public abstract void save(String path) throws IOException;
	
	public abstract ZFrame<D,R,C> transform(ZFrame<D,R,C> input);
	
}
