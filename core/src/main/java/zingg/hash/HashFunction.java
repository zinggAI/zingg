package zingg.hash;

import java.io.Serializable;

import zingg.client.ZFrame;

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
}

