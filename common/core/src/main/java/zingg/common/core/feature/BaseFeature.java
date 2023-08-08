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

package zingg.common.core.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.core.similarity.function.SimFunction;

public abstract class BaseFeature<T> implements Feature<T> {

	public static final Log LOG = LogFactory.getLog(BaseFeature.class);

	List<SimFunction<T>> simFunctions;
	FieldDefinition fieldDefinition;
	
	public FieldDefinition getFieldDefinition() {
		return fieldDefinition;
	}

	public BaseFeature() {
		simFunctions = new ArrayList<SimFunction<T>>();
	}

	public BaseFeature(FieldDefinition fieldDefinition) {
		this();
		this.fieldDefinition = fieldDefinition;

	}

	/**
	 * @return the fieldType
	 */
	public List<MatchType> getMatchType() {
		return fieldDefinition.getMatchType();
	}

	/**
	 * @param fieldType
	 *            the fieldType to set
	 */
	public void setFieldDefinition(FieldDefinition fieldType) {
		this.fieldDefinition = fieldType;
	}

	

	public SimFunction<T> getSimFunction(int i) {
		return simFunctions.get(i);
	}

	/**
	 * @return the simFunctions
	 */
	public List<SimFunction<T>> getSimFunctions() {
		return simFunctions;
	}

	/**
	 * @param simFunctions
	 *            the simFunctions to set
	 */
	public void setSimFunctions(List<SimFunction<T>> simFunctions) {
		this.simFunctions = simFunctions;
	}

	@Override
	public void addSimFunction(SimFunction<T> b) {
		//LOG.debug("Adding " + b + " with " + b.getNumFeatures() + " and "
		//		+ b.getNorm());
		this.simFunctions.add(b);
	}

	

	
}
