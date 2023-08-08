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

package zingg.common.client;

import zingg.common.client.util.Util;

public enum ZinggOptions {
    
    TRAIN("train"), 
    MATCH("match"), 
    TRAIN_MATCH("trainMatch"), 
    FIND_TRAINING_DATA("findTrainingData"), 
    LABEL("label"),
    LINK("link"),
    GENERATE_DOCS("generateDocs"),
    RECOMMEND("recommend"),
    UPDATE_LABEL("updateLabel"),
    FIND_AND_LABEL("findAndLabel"),
    ASSESS_MODEL("assessModel"),
    PEEK_MODEL("peekModel"),
    EXPORT_MODEL("exportModel"),
	RESOLVE("resolve");

    private String value;

    ZinggOptions(String s) {
        this.value = s;
    }

    public static String[] getAllZinggOptions() {
        ZinggOptions[] zo = ZinggOptions.values();
        int i = 0;
        String[] s = new String[zo.length];
        for (ZinggOptions z: zo) {
            s[i++] = z.getValue();
        }
        return s;
    }

    public String getValue() { 
        return value;
    }

    public static final ZinggOptions getByValue(String value){
        for (ZinggOptions zo: ZinggOptions.values()) {
            if (zo.value.equals(value)) return zo;
        }
        return null;
    }

	public static void verifyPhase(String phase) throws ZinggClientException {
		if (getByValue(phase) == null) {	
			String message = "'" + phase + "' is not a valid phase. "
			               + "Valid phases are: " + Util.join(getAllZinggOptions(), "|");
			throw new ZinggClientException(message);
		}
	}
}