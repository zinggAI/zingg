package zingg.client;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class TestClient {

	@Test
	public void testInvalidPhaseArg() {
		String s = null;
		int exitStatusExpected = 127; // error code for an illegal command e.g. typo
		try {
			String zinggInvalidPhaseCmd = "scripts/zingg.sh --phase findTrainingDat --conf examples/febrl/config.json";
			Process p = Runtime.getRuntime().exec(zinggInvalidPhaseCmd, null, new File(System.getenv("ZINGG_HOME")));
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
			p.waitFor();
			assertTrue(exitStatusExpected == p.exitValue());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testValidPhaseArg() {
		String s = null;
		int exitStatusExpected = 0; //default SUCCESS code
		try {
			String zinggHome = System.getenv("ZINGG_HOME");
			String zinggCmd = "scripts/zingg.sh --phase findTrainingData --conf  examples/febrl/config.json";
			Process p = Runtime.getRuntime().exec(zinggCmd, null, new File(zinggHome));
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
			p.waitFor();
			assertTrue(exitStatusExpected == p.exitValue());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}