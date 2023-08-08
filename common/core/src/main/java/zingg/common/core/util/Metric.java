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

package zingg.common.core.util;


public class Metric {
    public static final String DATA_FORMAT = "dataFormat";
    public static final String OUTPUT_FORMAT = "outputFormat";
    public static final String TOTAL_FIELDS_COUNT = "numTotalFields";
    public static final String MATCH_FIELDS_COUNT = "numMatchFields";
    public static final String EXEC_TIME = "executionTime";
    public static final String TRAINING_MATCHES = "trainingDataMatches";
    public static final String TRAINING_NONMATCHES = "trainingDataNonmatches";
    public static final String DATA_COUNT = "dataCount";

    public static final long timeout = 1200L;
    public static final double confidence = 0.95; // default value

    /* 
    public static double approxCount(Dataset<Row> data) {
        return data.rdd().countApprox(timeout, confidence).initialValue().mean();
    }
    */
}
