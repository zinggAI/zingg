package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SetMembershipFunction { /*extends SimFunction<String>{
	
	public static final Log LOG = LogFactory
			.getLog(SetMembershipFunction.class);

	public SetMembershipFunction() {
		super("SetMembershipFunction");
		// TODO Auto-generated constructor stub
	}

	public SetMembershipFunction(String name) {
		super(name);
	}

	public void prepare() {
		// TODO Auto-generated method stub

	}

	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	public void operate(SimFunctionContext<String> context) {
		String first = context.getFirstOperand();
		String second = context.getSecondOperand();
		// LOG.debug("Operate on " + first +", and sec " + second);

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
			if (score1 == 1.0d && score2 == 1.0d) {
				if (first.length() > second.length()) {
					score = first.contains(second)?1.0:0.0;
				}
				else if (second.length() > first.length()){
					score = second.contains(first)?1.0:0.0;
				}
				else {
					score = first.equals(second)?1.0:0.0;
				}
			}
			else {
				score1 = 0.0d;
			}
		} catch (Exception e) {
			LOG.warn("Error processing differences for " + first + "," + second);
		} finally {
			//context.addToResult(score1);
			//context.addToResult(score2);
			context.addToResult(score);
			LOG.debug("Result for " + first + ", " + second + " is " + score);
		}
	}

	@Override
	public int getNumFeatures() {
		return 1;
	}

	@Override
	public void setNorm() {
		//this.norm.add(false);
		this.norm.add(false);
		//this.norm.add(true);
	}
	*/

}
