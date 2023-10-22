package zingg.common.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CliUtils {
	/*
	 * leftJustifiedRows - If true, it will add "-" as a flag to format string to
	 * make it left justified. Otherwise right justified.
	 */
	private static boolean leftJustifiedRows = false;
	private static int maxColumnWidth = 50;

	public static void formatIntoTable(List<String[]> table) {

		List<String[]> finalTable = prepareColumnTexts(table, maxColumnWidth);
		//Sritng[][] finalTable = prepareColumnTexts(table, maxColumnWidth);
		Map<Integer, Integer> columnLengths = calculateColumnsLength(finalTable);
		final StringBuilder formatString = buildFormatString(columnLengths);
		String line = buildDemarcationLine(columnLengths);

		/*
		 * Print data table on screen
		 */
		System.out.print(line);
		//Arrays.stream(finalTable).limit(1).forEach(a -> System.out.printf(formatString.toString(), a));
		table.stream().limit(1).forEach(a -> System.out.printf(formatString.toString(), a));
		System.out.print(line);
		/*

		IntStream.iterate(1, (i -> i < finalTable.size()), (i -> ++i))
				.forEach(a -> System.out.printf(formatString.toString(), finalTable.get(a)));
				
		for (String[] t: finalTable) {
			for (String a: t) {
				System.out.printf(formatString.toString(), t);
			}
		}
		*/

		finalTable.stream().forEach(a -> System.out.printf(formatString.toString(), a));
		System.out.print(line);
	}

	private static List<String[]> prepareColumnTexts(List<String[]> table, int maxColumnWidth) {
		List<String[]> finalTableList = new ArrayList<String[]>();

		for (String[] row : table) {
			// If any cell data is more than max width, then it will need extra row.
			boolean needExtraRow = false;
			// Count of extra split row.
			int splitRow = 0;

			do {
				needExtraRow = false;
				String[] newRow = new String[row.length];
				for (int i = 0; i < row.length; i++) {
					// If data is less than max width, use that as it is.
					if (row[i] != null) {
						if (row[i].length() < maxColumnWidth) {
							newRow[i] = splitRow == 0 ? row[i] : "";
						} else if ((row[i].length() > (splitRow * maxColumnWidth))) {
							// If data is more than max width, then crop data at maxColumnWidth.
							// Remaining cropped data will be part of next row.
							int end = row[i].length() > ((splitRow * maxColumnWidth) + maxColumnWidth)
									? (splitRow * maxColumnWidth) + maxColumnWidth
									: row[i].length();
							newRow[i] = row[i].substring((splitRow * maxColumnWidth), end);
							needExtraRow = true;
						} else {
							newRow[i] = "";
						}
					}
					else {
						newRow[i] = row[i];
					}
				}

				finalTableList.add(newRow);
				if (needExtraRow) {
					splitRow++;
				}
			} while (needExtraRow);
		}
		String[][] finalTable = new String[finalTableList.size()][finalTableList.get(0).length];
		for (int i = 0; i < finalTable.length; i++) {
			finalTable[i] = finalTableList.get(i);
		}
		return finalTableList;
	}

	private static String buildDemarcationLine(Map<Integer, Integer> columnLengths) {
		/*
		 * Prepare line for top, bottom & below header row.
		 
		StringBuffer line = new StringBuffer(columnLengths.entrySet().stream().reduce("", (ln, b) -> {
			StringBuffer templn = new StringBuffer("+-");
			templn.append(Stream.iterate(0, (i -> i < b.getValue()), (i -> ++i)).reduce("", (ln1, b1) -> ln1 + "-",
					(a1, b1) -> a1 + b1));
			templn.append("-");
			return ln + templn;
		}, (a, b) -> a + b));
		line.append("+\n");
		return line.toString();
		*/
		StringBuffer templn = new StringBuffer("+-");
		for (Integer e: columnLengths.keySet()) {
			Integer length = columnLengths.get(e);
			for (int i = 0; i < length; ++i) {
				templn.append("-");
			}
			templn.append("+-");
		}
		templn.append("\n");

		return templn.toString();
	}

	private static Map<Integer, Integer> calculateColumnsLength(List<String[]> table) {
		/*
		 * Calculate appropriate Length of each column by looking at width of data in
		 * each column.
		 * 
		 * Map columnLengths is <column_number, column_length>
		 */
		Map<Integer, Integer> columnLengths = new HashMap<Integer, Integer>();
		for (String[] row: table) {
			int i = 0;
			for (String col : row) {
				if (columnLengths.get(i) == null) {
					columnLengths.put(i, 0);
				}
				if (col != null) {
					if (columnLengths.get(i) < col.length()) {
						columnLengths.put(i, col.length());
					}
				}
				i++;
			}		
		}
			/*
		table.forEach(a -> Stream.iterate(0, (i -> i < a.length), (i -> ++i)).forEach(i -> {
			if (columnLengths.get(i) == null) {
				columnLengths.put(i, 0);
			}
			if (a[i] != null) {
				if (columnLengths.get(i) < a[i].length()) {
					columnLengths.put(i, a[i].length());
				}
			}
		}));
		*/

		return columnLengths;
	}

	private static StringBuilder buildFormatString(Map<Integer, Integer> columnLengths) {
		/*
		 * Prepare format String
		 */
		final StringBuilder formatString = new StringBuilder("");
		String flag = leftJustifiedRows ? "-" : "";

		columnLengths.entrySet().stream().forEach(e -> formatString.append("| %" + flag + e.getValue() + "s "));
		formatString.append("|\n");
		return formatString;
	}

	public static void testFunc() {
		/*
		 * Sample Table to print in console in 2-dimensional array. Each sub-array is a
		 * row.
		 */
		String[][] table = new String[][] { { "id", "First Name", "Last Name", "Age", "Profile" },
				{ "1", "John", "Johnson", "45", "My name is John Johnson. My id is 1. My age is 45." },
				{ "2", "Tom", "", "35", "My name is Tom. My id is 2. My age is 35." },
				{ "3", "Rose", "Johnson Johnson Johnson Johnson Johnson Johnson Johnson Johnson Johnson Johnson", "22",
						"My name is Rose Johnson. My id is 3. My age is 22." },
				{ "4", "Jimmy", "Kimmel", "", "My name is Jimmy Kimmel. My id is 4. My age is not specified. "
						+ "I am the host of the late night show. I am not fan of Matt Damon. " } };
		/*
		 * Create new table array with wrapped rows
		 */
		ArrayList<String[]> tableList = new ArrayList<String[]>(Arrays.asList(table)); // Input
		formatIntoTable(tableList);
	}
}