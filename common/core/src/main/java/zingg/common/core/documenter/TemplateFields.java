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

package zingg.common.core.documenter;

public interface TemplateFields {

	public static final String TITLE = "title";
	public static final String MODEL_ID= "modelId";
	public static final String CLUSTERS = "clusters";
	public static final String NUM_COLUMNS= "numColumns";
    public static final String COLUMNS = "columns";
	public static final String FIELD_DEFINITION_COUNT= "fieldDefinitionCount";
	public static final String ISMATCH_COLUMN_INDEX = "isMatchColumnIndex";
	public static final String CLUSTER_COLUMN_INDEX= "clusterColumnIndex";
	public static final String PARENT_LINK= "parentLink";
	public static final String DATA_FIELDS_LIST= "dataFieldsList";
	public static final String TOTAL_PAIRS = "totalPairs";
	public static final String MATCH_PAIRS = "matchPairs";
	public static final String NON_MATCH_PAIRS = "nonMatchPairs";
	public static final String NOT_SURE_PAIRS = "notSurePairs";
	public static final String IDENTIFIED_PAIRS = "identifiedPairs";
	public static final String MARKED_PAIRS = "markedPairs";
	public static final String UNMARKED_PAIRS = "unmarkedPairs";

}
