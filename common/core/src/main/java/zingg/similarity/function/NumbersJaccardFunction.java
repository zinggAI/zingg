package zingg.similarity.function;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NumbersJaccardFunction extends StringSimilarityDistanceFunction {

	public static final Log LOG = LogFactory
			.getLog(NumbersJaccardFunction.class);


	public NumbersJaccardFunction() {
		super("JaccSimFunction");
		gap = new SJacc();
	}

	@Override
	public Double call(String first, String second) {
		double score1 = 0.0;
		double score2 = 0.0;
		double score = 0.0;

		try {
			if (!(first == null || first.equals(""))) {
				score1 = 1.0d;
			}
			if (!(second == null || second.equals(""))) {
				score2 = 1.0d;
			}
			Set<String> num1 = new HashSet<String>();
			Set<String> num2 = new HashSet<String>();
			//Pattern p = Pattern.compile("(\\s?([a-z0-9A-Z]*\\d+[a-z0-9A-Z]*)\\s?)");
			Pattern p = Pattern.compile("\\d+");
			
			if (score1 == 1.0d) {
				// get numbers
				Matcher m = p.matcher(first);
				while (m.find()) {
					num1.add(m.group().toLowerCase());
					score1 ++;
				}
			}
			if (score2 == 1.0d) {
				Matcher m = p.matcher(second);
				while (m.find()) {
					num2.add(m.group().toLowerCase());
					score2 ++;
				}
			}
			if (num1.size() > 0 && num2.size() > 0) {
				LOG.debug("Found codes " + num1 + " in " + first);
				LOG.debug("Found codes " + num2 + " in " + second);
				int union = num1.size() + num2.size();
				num1.retainAll(num2);
				int intersection = num1.size();

				score = intersection * 1.0d / (union - intersection);
				LOG.debug("Score explanation for : " + first + " and :" + second + "is - intersection: " + intersection + " and union: " + union + " and score: " + score);
				LOG.debug("Score for : " + first + " and :" + second + "is - score1: " + score1 + " and score2: " + score2);
				
			}
			else {
				LOG.debug("No data found");
			}

			
		}  catch (Exception e) {
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			if (Double.isNaN(score))
				score = 0.0;
			//scores stand for number of digits found + 1, so we normalize
			if (score1 > 1) score1 = 1; else score = 0;
			if (score2 > 1) score2 = 1;  else score = 0;
			LOG.debug("Now Score for : " + first + " and :" + second + "is - score1: " + score1 + " and score2: " + score2);
			//context.addToResult(score2);
			
			// LOG.debug("Jaccard Num" + first + " and " + second + " is " +
			// num1Numbers + "," + num2Numbers + ", " + score);
		}
		return score;
	}
}
