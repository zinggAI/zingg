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

package zingg.common.client.util;

public interface ColValues {

    public static final int MATCH_TYPE_UNKNOWN = -1;
    public static final int MATCH_TYPE_NOT_A_MATCH = 0;
    public static final int MATCH_TYPE_MATCH = 1;
    public static final int MATCH_TYPE_NOT_SURE = 2;
    
    public static final double IS_MATCH_PREDICTION = 1.0;
    public static final double IS_NOT_A_MATCH_PREDICTION = 0.0;
    public static final double IS_NOT_KNOWN_PREDICTION = -1.0;
    public static final double IS_NOT_SURE_PREDICTION = 2.0;
    
    public static final double ZERO_SCORE = 0.0;
    public static final double FULL_MATCH_SCORE = 1.0;
    
}
