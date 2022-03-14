package zingg.util;

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
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataType;

import zingg.client.util.ListMap;

public class FileUtil implements Serializable {

	public static final Log LOG = LogFactory.getLog(FileUtil.class);
	
	public static DataFrame addUniqueCol(DataFrame dupesActual, String colName) {
		String append = System.currentTimeMillis() + ":";
		dupesActual = dupesActual.withColumn(colName + "temp", 
				Functions.lit(append));
		dupesActual = dupesActual.withColumn(colName,
				Functions.concat(dupesActual.col(colName + "temp"),
						dupesActual.col(colName)));
		dupesActual = dupesActual.drop(dupesActual.col(colName + "temp"));
		return dupesActual;
	}
	
	
	
	
}
