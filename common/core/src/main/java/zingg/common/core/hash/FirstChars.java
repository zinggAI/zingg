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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FirstChars extends BaseHash<String,String>{
	
	public static final Log LOG = LogFactory.getLog(FirstChars.class);

	private int endIndex;

	public FirstChars(int endIndex) {
	    setName("first" + endIndex + "Chars");
		this.endIndex = endIndex;
	}
	
	 
	 public String call(String field) {
		 
		 String r = null;
			if (field == null ) {
				r = field;
			}
			else{
				field = field.trim().toLowerCase();
				if (field.length() <= (endIndex)) {
			
				r = field; 
			}
			else {
				r = field.trim().substring(0, endIndex);
			}
			}
			LOG.debug("Applying " + this.getName() + " on " + field + " and returning " + r);
			return r;
	 }


    public int getEndIndex() {
        return endIndex;
    }

}
