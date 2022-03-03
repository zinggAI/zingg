package zingg.client.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import com.snowflake.snowpark.Column;
import com.snowflake.snowpark.DataFrame;
import com.snowflake.snowpark.functions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import scala.collection.JavaConverters;

public class Util implements Serializable {

	public static final Log LOG = LogFactory.getLog(Util.class);
	public static final Log DbLOG = LogFactory.getLog("WEB");

	public static <T> T[] copy(T[] source) {
		if (source == null)
			return null;

		return Arrays.copyOf(source, source.length);
	}

	public static String unique(String value, String delim) {
		String[] split = value.split(delim);

		Set<String> values = new LinkedHashSet<String>();

		Collections.addAll(values, split);

		return join(values, delim);
	}

	/**
	 * This method joins the values in the given list with the delim String
	 * value.
	 *
	 * @param list
	 * @param delim
	 * @return String
	 */
	public static String join(int[] list, String delim) {
		return join(list, delim, false);
	}

	public static String join(int[] list, String delim, boolean printNull) {
		StringBuffer buffer = new StringBuffer();
		int count = 0;

		for (Object s : list) {
			if (count != 0)
				buffer.append(delim);

			if (printNull || s != null)
				buffer.append(s);

			count++;
		}

		return buffer.toString();
	}

	public static String join(String delim, String... strings) {
		return join(delim, false, strings);
	}

	public static String join(String delim, boolean printNull,
			String... strings) {
		return join(strings, delim, printNull);
	}

	/**
	 * This method joins the values in the given list with the delim String
	 * value.
	 *
	 * @param list
	 * @param delim
	 * @return a String
	 */
	public static String join(Object[] list, String delim) {
		return join(list, delim, false);
	}

	public static String join(Object[] list, String delim, boolean printNull) {
		return join(list, delim, printNull, 0);
	}

	public static String join(Object[] list, String delim, boolean printNull,
			int beginAt) {
		return join(list, delim, printNull, beginAt, list.length - beginAt);
	}

	public static String join(Object[] list, String delim, boolean printNull,
			int beginAt, int length) {
		StringBuffer buffer = new StringBuffer();
		int count = 0;

		for (int i = beginAt; i < beginAt + length; i++) {
			Object s = list[i];
			if (count != 0)
				buffer.append(delim);

			if (printNull || s != null)
				buffer.append(s);

			count++;
		}

		return buffer.toString();
	}

	/**
	 * This method joins each value in the collection with a tab character as
	 * the delimiter.
	 *
	 * @param collection
	 * @return a String
	 */
	public static String join(Collection collection) {
		return join(collection, "\t");
	}

	/**
	 * This method joins each valuein the collection with the given delimiter.
	 *
	 * @param collection
	 * @param delim
	 * @return a String
	 */
	public static String join(Collection collection, String delim) {
		return join(collection, delim, false);
	}

	public static String join(Collection collection, String delim,
			boolean printNull) {
		StringBuffer buffer = new StringBuffer();

		join(buffer, collection, delim, printNull);

		return buffer.toString();
	}

	/**
	 * This method joins each value in the collection with the given delimiter.
	 * All results are appended to the given {@link StringBuffer} instance.
	 *
	 * @param buffer
	 * @param collection
	 * @param delim
	 */
	public static void join(StringBuffer buffer, Collection collection,
			String delim) {
		join(buffer, collection, delim, false);
	}

	public static void join(StringBuffer buffer, Collection collection,
			String delim, boolean printNull) {
		int count = 0;

		for (Object s : collection) {
			if (count != 0)
				buffer.append(delim);

			if (printNull || s != null)
				buffer.append(s);

			count++;
		}
	}

	public static String[] removeNulls(String... strings) {
		List<String> list = new ArrayList<String>();

		for (String string : strings) {
			if (string != null)
				list.add(string);
		}

		return list.toArray(new String[list.size()]);
	}

	public static Collection<String> quote(Collection<String> collection,
			String quote) {
		List<String> list = new ArrayList<String>();

		for (String string : collection)
			list.add(quote + string + quote);

		return list;
	}

	public static String print(Collection collection, String delim) {
		StringBuffer buffer = new StringBuffer();

		print(buffer, collection, delim);

		return buffer.toString();
	}

	public static void print(StringBuffer buffer, Collection collection,
			String delim) {
		int count = 0;

		for (Object s : collection) {
			if (count != 0)
				buffer.append(delim);

			buffer.append("[");
			buffer.append(s);
			buffer.append("]");

			count++;
		}
	}

	public static String getTypeName(Type type) {
		if (type == null)
			return null;

		return type instanceof Class ? ((Class) type).getCanonicalName() : type
				.toString();
	}

	public static String getSimpleTypeName(Type type) {
		if (type == null)
			return null;

		return type instanceof Class ? ((Class) type).getSimpleName() : type
				.toString();
	}

	public static String[] typeNames(Type[] types) {
		String[] names = new String[types.length];

		for (int i = 0; i < types.length; i++)
			names[i] = getTypeName(types[i]);

		return names;
	}

	public static String[] simpleTypeNames(Type[] types) {
		String[] names = new String[types.length];

		for (int i = 0; i < types.length; i++)
			names[i] = getSimpleTypeName(types[i]);

		return names;
	}

	public static boolean containsNull(Object[] values) {
		for (Object value : values) {
			if (value == null)
				return true;
		}

		return false;
	}

	public static int[] getFields(String field) {
		String[] items = field.split(",");
		int[] results = new int[items.length];

		for (int i = 0; i < items.length; i++) {
			try {
				results[i] = Integer.parseInt(items[i]);
			} catch (NumberFormatException nfe) {
			}
			;
		}
		return results;
	}

	public static Class[] getClasses(String[] types)
			throws ClassNotFoundException {
		Class[] classes = new Class[types.length];
		int i = 0;
		for (String param : types) {
			classes[i++] = Class.forName(param);
		}
		return classes;
	}

	

	public static void writeToFile(Object o, String path) throws Exception {
		File f = new File(path);
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		if (!f.exists())
			f.createNewFile();
		FileOutputStream istream = new FileOutputStream(f);
		ObjectOutputStream q = new ObjectOutputStream(istream);

		q.writeObject(o);

		q.close();
		istream.close();
	}

	public static Object readfromFile(String filePath) throws Exception {
		FileInputStream istream = new FileInputStream(filePath);
		ObjectInputStream q = new ObjectInputStream(istream);

		Object o = q.readObject();

		q.close();
		istream.close();
		return o;
	}

	public static boolean isNumeric(final CharSequence cs) {
		if (cs == null)
			return false;
		if (isEmpty(cs)) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * Doesnt take care of trailing null columns
	 * 
	 * @param input
	 * @param delimiter
	 * @return
	 */
	public static List scanToList(String input, String delimiter) {

		Scanner scanner = new Scanner(input).useDelimiter(delimiter);
		List retList = new ArrayList();
		while (scanner.hasNext()) {
			String s = scanner.next();
			if (s != null)
				s = s.trim();
			retList.add(s);
		}
		scanner.close();
		return retList;
	}

	public static List parse(String input, String delimiter) {
		// /had to create new arraylist to support remove
		// dont kill this in perf
		// see
		// http://stackoverflow.com/questions/6260113/unsupportedoperationexception-in-abstractlist-remove-when-operating-on-arrayli
		//	LOG.info("line is " + input);
		return new ArrayList(Arrays.asList(input.split(
				Pattern.quote(delimiter), -1)));
	}
	
	
	
	public static String prettyPrintString(Object label, String delimiter,
			String t1) {
		StringBuffer buffer = new StringBuffer();
		
		//int count = 0;
		buffer.append(label);
		buffer.append(delimiter);
		/*for (Object s : t1.getElements()) {
			if (count != 0)
				buffer.append(delimiter);
			if (s == null) buffer.append(""); else buffer.append(s);
			count++;
		}*/
		buffer.append(t1);
		return buffer.toString();
	}


	/*

	public static ListMap<DataType, RFunction> getFunctionList(String fileName)
			throws Exception {
		ListMap<DataType, RFunction> functions = new ListMap<DataType, RFunction>();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		List<ScriptFunctionArgs> scriptArgs = mapper.readValue(
				zingg.Zingg.class.getResourceAsStream("/" + fileName),
				new TypeReference<List<ScriptFunctionArgs>>() {
				});
		for (ScriptFunctionArgs scriptArg : scriptArgs) {
			// LOG.info(" Args " + args);
			ScriptFunction function = new ScriptFunction(scriptArg);
			function.prepare();
			DataType cl = DataType.getFieldClass(scriptArg.getParameterTypes()[0]);
			LOG.debug("Adding func " + function);
			functions.add(cl, function);
		}
		return functions;
	}*/
	
	
	
		
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
	
	public static DataFrame addUniqueCol(DataFrame dupesActual, String colName) {
		String append = System.currentTimeMillis() + ":";
		dupesActual = dupesActual.withColumn(colName + "temp", 
				functions.lit(append));
				
		List<Column> cols = new ArrayList<Column>();
		cols.add(dupesActual.col(colName + "temp"));
		cols.add(dupesActual.col(colName));		 
		dupesActual = dupesActual.withColumn(colName,
				functions.concat(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq()));
		List<Column> dropCols = new ArrayList<Column>();
		dropCols.add(dupesActual.col(colName + "temp"));
		dupesActual = dupesActual.drop(dropCols.toArray(Column[]::new));
		return dupesActual;
	}
	
	public static byte[] convertObjectIntoByteArray(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedOutputStream bufferedOS = new BufferedOutputStream(bos);
		ObjectOutputStream oos = new ObjectOutputStream(bufferedOS);
		oos.writeObject(obj);
		oos.flush();
		return bos.toByteArray();
	}

	public static Object revertObjectFromByteArray(byte[] byteArray) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
		BufferedInputStream bufferedIS = new BufferedInputStream(bis);
		ObjectInputStream ois = new ObjectInputStream(bufferedIS);
		return ois.readObject(); 
	}
	
}
