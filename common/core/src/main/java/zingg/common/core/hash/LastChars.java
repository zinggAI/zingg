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

package zingg.common.core.hash;


public class LastChars extends BaseHash<String,String>{
	private int numChars;
	
	public LastChars(int endIndex) {
	    setName("last" + endIndex + "Chars");
		this.numChars = endIndex;
	} 
	
	public String call(String field) {
		String r = null;
		if (field == null ) {
			r = field;
		}
		else {
			field = field.trim().toLowerCase();
			r= field.trim().toLowerCase().substring(Math.max(field.length() - numChars, 0));
		}
		return r;
	}

    public int getNumChars() {
        return numChars;
    }

}
