package zingg.common.core.similarity.function;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNumbersJaccardFunction {
	
	
	@Test
	public void testFirstHas1NumSecondHas0Num() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have 1 number", "I have no number"));
	}

	@Test
	public void testFirstHas0NumSecondHas1Num() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have no number", "I have 1 number"));
	}
	
	@Test
	public void testFirstHas0NumSecondHas0Num() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have no number", "I have no number"));
	}

	@Test
	public void testFirstHas1NumSecondHas1NumMatch() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1d, sim.call("I have 1 number", "I have 1 number"));
	}

	@Test
	public void testFirstHas1NumSecondHas1NumNoMatch() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have 1 number", "I have 2 number"));
	}

	@Test
	public void testFirstHas2NumSecondHas2NumNoMatch() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(0d, sim.call("I have 1 number 2 ", "I have 3 4 number"));
	}

	@Test
	public void testFirstHas2NumSecondHas2Num1Match() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1.0d/3, sim.call("I have 1 number 2 ", "I have 3 1 number"));
	}
	
	@Test
	public void testFirstHas2NumSecondHas2Num2Match() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1.0d, sim.call("I have 1 number 2 ", "I have 2 number 1"));
	}

	@Test
	public void testFirstHas2NumSecondHas2Num2ButNotClean() {
		NumbersJaccardFunction sim = new NumbersJaccardFunction();
		assertEquals(1d, sim.call("I have1 number2 ", "I have 2number 1unclean"));
	}
}
