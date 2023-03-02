package zingg.spark.core.similarity;

import org.apache.spark.sql.api.java.UDF2;

import zingg.similarity.function.SimFunction;

public class SparkSimFunction<T> implements UDF2<T, T, Double> {

    SimFunction<T> function;

    public SparkSimFunction(SimFunction<T> fn) {
        this.function = fn;
    }

    public String getName() {
        return function.getName();
    }

    @Override
    public Double call(T t1, T t2) throws Exception {
        return function.call(t1,t2);
    }
    
}


