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

public interface HashFunctionRegistry<D,R,C,T>  {

	//public static HashMap<String, HashFunction<D,R,C,T,T1> > fns = new  HashMap<String,HashFunction<D,R,C,T,T1>>();
		/*
		init(new IdentityString());
		init(new IdentityInteger());
		init(new First1Chars());
		init(new First2Chars());
		init(new First3Chars());
		init(new First4Chars());
		init(new Last1Chars());
		init(new Last2Chars());
		init(new Last3Chars());
		init(new Round());
		init(new TruncateDoubleTo1Place());
		init(new TruncateDoubleTo2Places());
		init(new TruncateDoubleTo3Places());
		init(new LastWord());
		init(new First2CharsBox());
		init(new First3CharsBox());
		init(new IsNullOrEmpty());
		init(new LessThanZeroDbl());
		init(new LessThanZeroInt());
		init(new TrimLast1DigitDbl());
		init(new TrimLast2DigitsDbl());
		init(new TrimLast3DigitsDbl());
		init(new TrimLast1DigitInt());
		init(new TrimLast2DigitsInt());
		init(new TrimLast3DigitsInt());
		init(new RangeBetween0And10Int());
		init(new RangeBetween10And100Int());
		init(new RangeBetween100And1000Int());
		init(new RangeBetween1000And10000Int());
		init(new RangeBetween0And10Dbl());
		init(new RangeBetween10And100Dbl());
		init(new RangeBetween100And1000Dbl());
		init(new RangeBetween1000And10000Dbl());
	*/
	
	public void init(HashFunction<D,R,C,T> fn);
	
}
