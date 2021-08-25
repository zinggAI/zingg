package zingg.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import zingg.client.util.ListMap;

public class FileUtil implements Serializable {

	public static final Log LOG = LogFactory.getLog(FileUtil.class);
	
		
	public static String getNextLabelPath(String path) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path fsPath = new Path(path);
		String pathPrefix = "";
		if (!fs.exists(fsPath)) {
			pathPrefix += "0";
		}
		else {
			FileStatus[] fileStatus = fs.listStatus(fsPath);
			pathPrefix += fileStatus.length;
		}
		return pathPrefix;
		
	}
	
	public static String getCurrentLabelPath(String path) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path fsPath = new Path(path);
		int pathPrefix = 0;
		FileStatus[] fileStatus = fs.listStatus(fsPath);
		pathPrefix = fileStatus.length -1;
		return new Integer(pathPrefix).toString();
		
	}
	
	public static Dataset<Row> addUniqueCol(Dataset<Row> dupesActual, String colName) {
		String append = System.currentTimeMillis() + ":";
		dupesActual = dupesActual.withColumn(colName + "temp", 
				functions.lit(append));
		dupesActual = dupesActual.withColumn(colName,
				functions.concat(dupesActual.col(colName + "temp"),
						dupesActual.col(colName)));
		dupesActual = dupesActual.drop(dupesActual.col(colName + "temp"));
		return dupesActual;
	}
	
	
	
	
}
