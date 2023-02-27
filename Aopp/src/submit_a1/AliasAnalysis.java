package submit_a1;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class AliasAnalysis extends BodyTransformer{

	@Override
	protected synchronized void internalTransform(Body arg0, String arg1, Map<String, String> arg2) {
		/*
		 * Implement your alias analysis here. A1.answers should include the Yes/No answers for 
		 * the queries
		 */
		UnitGraph g = new BriefUnitGraph(arg0);
		System.out.println(arg0.getMethod());
		for(Unit s : g) {
			if(s instanceof AssignStmt) {
				System.out.println(s);
				String l = ((AssignStmt) s).getLeftOp().toString();
				String r = ((AssignStmt) s).getRightOp().toString();
				String r_split[] = r.split(" ");
				if(r_split[0].equals("new")) {
					System.out.println(l + " : " + r);
					System.out.println("Create box");
				}
				System.out.println("-----");
			}
		}
	}
	
}
