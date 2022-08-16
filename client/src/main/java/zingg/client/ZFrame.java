package zingg.client;

import java.util.List;
//Dataset, Row, column
public interface ZFrame<T, R, C> {
    
    public ZFrame<T, R, C> cache();
    public ZFrame<T, R, C> as(String s);
    public String[] columns();
    public ZFrame<T, R, C> select(C... cols);
    public ZFrame<T, R, C> select(List<C> cols);
    public ZFrame<T, R, C> select(String col, String... cols);
    public ZFrame<T, R, C> select(String col);
    public ZFrame<T, R, C> selectExpr(String... col);
    public ZFrame <T, R, C> distinct();
    public List<R> collectAsList();

    public ZFrame<T,R,C> toDF(String[] cols);
    public ZFrame<T,R,C> toDF(String col1, String col2);

    public ZFrame<T,R,C> join(ZFrame<T,R,C> lines1, String joinColumn);

    public ZFrame<T,R,C> joinRight(ZFrame<T,R,C> lines1, String joinColumn);

    public ZFrame<T,R,C> join(ZFrame<T,R,C> lines1, String joinColumn, boolean addPrefixToCol, String jointype);    

    public C col(String colname);
    
    public long count();

    public ZFrame<T,R,C> filter(C col);

    public T df();

    public ZFrame<T,R,C> withColumnRenamed(String s, String t);

    public ZFrame<T,R,C> dropDuplicates(String c, String... d);

    public ZFrame<T,R,C> dropDuplicates(String[] c);

    public ZFrame<T,R,C> drop(String c);
    public ZFrame<T,R,C> drop(String... c);
    public ZFrame<T,R,C> except(ZFrame<T,R,C> c);

    public ZFrame<T,R,C> groupByMinMax(C c);

    public ZFrame<T,R,C> union(ZFrame<T,R,C> other);

    public ZFrame<T,R,C> unionByName(ZFrame<T,R,C> other, boolean flag);

    public ZFrame<T,R,C> withColumn(String s, int c);
    public ZFrame<T,R,C> withColumn(String s, String c);
    public ZFrame<T,R,C> withColumn(String s, double c);
    public ZFrame<T,R,C> withColumn(String s, C c);

    
    public ZFrame<T,R,C> repartition(int num);
    public ZFrame<T,R,C> repartition(int num, C c);

    public ZFrame<T,R,C> sample(boolean repartition, float num);
    public ZFrame<T,R,C> sample(boolean repartition, double num);


    public ZFrame<T,R,C> coalesce(int num);

    public C gt(String c);

	public C equalTo(String c, String e);

	public C notEqual(String c, String e);
    
    public C equalTo(String c, int e);
    public C equalTo(String c, double e);
    public C concat(C a, C b);

	public C notEqual(String c, int e);


    public void show(int num);
    public void show();

    public String showSchema();
    public ZFrame<T,R,C> orderBy(String c);
    public ZFrame<T,R,C> sortAscending(String c);
    public ZFrame<T,R,C> sortDescending(String c);


    public ZFrame<T,R,C> limit(int l);

    public String getAsString(R r, String colName);

    public double getAsDouble(R r, String colName);

    public int getAsInt(R r, String colName);

    public R head();

    public void show(boolean a);

    public boolean isEmpty();
    
}