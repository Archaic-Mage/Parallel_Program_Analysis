package submit_a2;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SceneTransformer;

public class EscapeAnalysis extends SceneTransformer{

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		/*
		 * Implement your escape analysis here
		 */
		System.out.println("Hello");
	}
	
}
