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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import zingg.common.client.ZFrame;

public abstract class HashFunction<D,R,C,T> implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected T dataType;
		protected String name;
		protected boolean isUdf = true;
		protected T returnType;
		
        public HashFunction() {           
        }

		public HashFunction(String name) {
			this.name = name;
		}
		
		public HashFunction(String name, T cl, T returnType) {
			this.name = name;
			this.dataType = cl;
			this.returnType = returnType;
		}
	
		public HashFunction(String name, T cl, T returnType, boolean isUdf) {
			this(name, cl, returnType);
			this.isUdf = isUdf;
		}
		
		public T getDataType() {
			return dataType;
		}
		
		
	
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isUdf() {
			return isUdf;
		}

		public void setUdf(boolean isUdf) {
			this.isUdf = isUdf;
		}

		public T getReturnType() {
			return returnType;
		}

		public void setReturnType(T returnType) {
			this.returnType = returnType;
		}

		public void setDataType(T dataType) {
			this.dataType = dataType;
		}

		
		public abstract ZFrame<D,R,C> apply(ZFrame<D,R,C> ds, String column, String newColumn) ;

		public abstract Object getAs(R r, String column);

		public abstract Object getAs(D df, R r, String column); // added for SnowFrame getAsString method 
		
		public abstract Object apply(R r, String column);

		public abstract Object apply(D df, R r, String column); // added for SnowFrame getAsString method

		/* 
		public abstract void writeCustomObject(ObjectOutputStream out) throws IOException;
		public abstract void readCustomObject(ObjectInputStream ois) throws ClassNotFoundException, IOException;
		
		
		private void writeObject(ObjectOutputStream out) throws IOException{
			out.writeUTF(name);
			out.writeBoolean(isUdf);
			writeCustomObject(out);
			//out.close();
			
		}
	
		private void readObject(ObjectInputStream ois) 
			throws ClassNotFoundException, IOException {
				setName(ois.readUTF());
				setUdf(ois.readBoolean());
				readCustomObject(ois);
			}
			*/
	
}

