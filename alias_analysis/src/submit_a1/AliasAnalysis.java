package submit_a1;

import java.util.*;

import com.google.common.collect.Sets;
import dont_submit.AliasQuery;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class AliasAnalysis extends BodyTransformer{

    //the out contains static info stack and heap
    public class Out {
		//Og indicates global reference
		//@ indicates any field
        HashMap<String, Set<String>> rho_data;
        HashMap<String, HashMap<String, Set<String>>> sigma_data;
		Out union(Out a) {
			//Union of rho_data
			for(String name: a.rho_data.keySet()) {
				Set<String> u_rho;
				if(!this.rho_data.containsKey(name)) {
					u_rho = a.rho(name);
				} else {
					u_rho = Sets.union(this.rho_data.get(name), a.rho(name));
				}
				this.rho_data.put(name, u_rho);
			}

			//Union of sigma_data

			return this;
		}
		Out() {
			rho_data = new HashMap<>();
			sigma_data = new HashMap<>();
		}
        public Set<String> rho(String a) {
            return rho_data.get(a);
        }
        public Set<String> sigma(String ref, String field) {
            return sigma_data.get(ref).get(field);
        }
    }
    HashMap<Unit, Out> get_out;
	Out union(List<Out> outs) {
		Out uni = outs.get(0);
		for(int i = 1; i < outs.size(); i++) {
			uni = uni.union(outs.get(i));
		}
		Out temp = new Out();
		return temp;
	}
    Out process_assign(Unit s, UnitGraph g) {
		AssignStmt a_s = (AssignStmt) s;
        //gets the out of predecessors and applies the flow functions to get the out of the current statement
		List<Unit> pred_s = g.getPredsOf(s);
		List<Out> pred_outs = new ArrayList<>();
		for(Unit pred: pred_s) {
			pred_outs.add(get_out.get(pred));
		}

		Out in = union(pred_outs);
        Value left = a_s.getLeftOp();
        Value right = a_s.getRightOp();

		//allocation type a = new b();
        if(left instanceof JimpleLocal && right instanceof JNewExpr) {
			int ln = ((SourceLnPosTag) s.getTag("SourceLnPosTag")).startLn();
			String ref = "O"+ln;
			Set<String> replace = new HashSet<>();
			replace.add(ref);
			in.rho_data.put(left.toString(), replace);
			return in;
        }
        Out temp = new Out();
        return temp;
    }

	Out process_invoke(Unit s, UnitGraph g) {
		InvokeExpr i_e = ((Stmt) s).getInvokeExpr();
		Out temp = new Out();
		return temp;
	}

	void debug(Object s) { System.out.println(s); }
	@Override
	protected synchronized void internalTransform(Body arg0, String arg1, Map<String, String> arg2) {
		/*
		 * Implement your alias analysis here. A1.answers should include the Yes/No answers for 
		 * the queries
		 */
		debug(arg0.getMethod().getName());
		debug(arg0.getMethod().getDeclaringClass());
		UnitGraph g = new BriefUnitGraph(arg0);

		for(Unit s : g) {
			if(s instanceof AssignStmt) {
				debug(s);
				Value l = ((AssignStmt) s).getLeftOp();
				Value r = ((AssignStmt) s).getRightOp();
				debug(l + ": " + l.getClass());
//				if(l instanceof JInstanceFieldRef) {
//					System.out.println(((JInstanceFieldRef) l).getBase());
//					System.out.println(((JInstanceFieldRef) l).getField().getName());
//				}
				System.out.println(r + ": " + r.getClass());
//				if(r instanceof JInstanceFieldRef) {
//					System.out.println(((JInstanceFieldRef) r).getBase());
//					System.out.println(((JInstanceFieldRef) r).getField().getName());
//				}
//				if(r instanceof JVirtualInvokeExpr) {
//					System.out.println(((JVirtualInvokeExpr) r).getBase());
//					System.out.println(((JVirtualInvokeExpr) r).getMethod().getName());
//					System.out.println(((JVirtualInvokeExpr) r).getArgs());
//				}
				System.out.println("-----");
			}
		}

		for(AliasQuery q: A1.queryList) {
			if(q.getClassName().equals(arg0.getMethod().getDeclaringClass().toString())) {
				if(q.getMethodName().equals(arg0.getMethod().getName())) {
					A1.answers[A1.queryList.indexOf(q)] = "Yes";
				}
			}
		}
	}
	
}
