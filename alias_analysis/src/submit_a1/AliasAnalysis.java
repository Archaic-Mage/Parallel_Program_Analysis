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
			for(String ref: a.sigma_data.keySet()) {
				if(!this.sigma_data.containsKey(ref)) {
					this.sigma_data.put(ref, a.sigma_data.get(ref));
				} else {
					for(String field: a.sigma_data.get(ref).keySet()) {
						Set<String> u_f_sig;
						if(!this.sigma_data.get(ref).containsKey(field)) {
							u_f_sig = a.sigma_data.get(ref).get(field);
						} else {
							u_f_sig = Sets.union(this.sigma(ref, field), a.sigma(ref, field));
						}
						this.sigma_data.get(ref).put(field, u_f_sig);
					}
				}
			}
			return this;
		}
		Boolean isEqual(Out a) {
			//comparing stack space
			if(a.rho_data.size() != this.rho_data.size()) {
				return false;
			}
			for(String name: a.rho_data.keySet()) {
				if(!this.rho_data.containsKey(name)) return false;
				else if(!a.rho(name).equals(this.rho(name))) return false;
			}

			//comparing heap space
			if(a.sigma_data.size() != this.sigma_data.size()) return false;
			for(String ref: a.sigma_data.keySet()) {
				if(!this.sigma_data.containsKey(ref)) return false;
				else {
					if(a.sigma_data.get(ref).size() != this.sigma_data.get(ref).size()) return false;
					for(String field: a.sigma_data.keySet()) {
						if(!this.sigma_data.get(ref).containsKey(field)) return false;
						else if(!a.sigma(ref, field).equals(this.sigma(ref, field))) return false;
					}
				}
			}

			return true;
		}
    }
    HashMap<Unit, Out> get_out;
	Out union(List<Out> outs) {
		Out uni = new Out();
		for(int i = 0; i < outs.size(); i++) {
			uni = uni.union(outs.get(i));
		}
		return uni;
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
		//copy type a = b
		if(left instanceof JimpleLocal && right instanceof JimpleLocal) {
			Set<String> replace = new HashSet<>();
			if(in.rho(right.toString()) != null) replace.addAll(in.rho(right.toString()));
			in.rho_data.put(left.toString(), replace);
			return in;
		}
		//field copy a = b.f
		if(left instanceof JimpleLocal && right instanceof JInstanceFieldRef) {
			Set<String> replace = new HashSet<>();
			String field = ((JInstanceFieldRef) right).getField().getName();
			for(String ref: in.rho_data.get(right.toString())) {
				replace = Sets.union(replace, in.sigma(ref, field));
			}
			in.rho_data.put(left.toString(), replace);
			return in;
		}
		//assign return of a call a = b.foo(args...)
		if(left instanceof JimpleLocal && right instanceof InvokeExpr) {
			//a points to Og
			Set<String> replace = new HashSet<>();

		}

        return in;
    }

	Out process_invoke(Unit s, UnitGraph g) {
		InvokeExpr i_e = ((Stmt) s).getInvokeExpr();
		Out temp = new Out();
		return temp;
	}

	//debug information
	void debug(Object s) { System.out.println(s); }

	void print_out(Out o) {
		debug("------------------");
		debug("Stack Space Data");
		for(String name: o.rho_data.keySet()) {
			for(String ref: o.rho(name)) {
				debug(name + "->" + ref);
			}
		}

		debug("Heap Space Data");
		for(String ref: o.sigma_data.keySet()) {
			for(String field: o.sigma_data.get(ref).keySet()) {
				for(String ref1: o.sigma(ref, field)) {
					debug(ref + "-" + field + "->" + ref1);
				}
			}
		}
		debug("------------------");
	}
	//create two out and checks union
	void test() {
		Out a = new Out();
		Out b = new Out();

		//defining the stack space
		Set<String> temp = new HashSet<>();
		temp.add("O1");
		temp.add("O2");
		a.rho_data.put("a1", temp);
		temp = new HashSet<>();
		temp.add("O1");
		a.rho_data.put("b1", temp);
		b.rho_data.put("a1", temp);
		temp = new HashSet<>();
		temp.add("O2");
		b.rho_data.put("b1", temp);

		//defining the heap space
		temp = new HashSet<>();
		temp.add("O1");
		temp.add("O2");
		HashMap<String, Set<String>> fields = new HashMap<>();
		fields.put("test", temp);
		temp = new HashSet<>();
		temp.add("O3");
		fields.put("test2", temp);
		a.sigma_data.put("O5", fields);
		fields = new HashMap<>();
		temp = new HashSet<>();
		temp.add("O2");
		fields.put("test", temp);
		b.sigma_data.put("O6", fields);

		debug("a before union");
		print_out(a);
		debug("b before union");
		print_out(b);
		a = b.union(a);
		debug("after union");
		print_out(a);
	}

	@Override
	protected synchronized void internalTransform(Body arg0, String arg1, Map<String, String> arg2) {
		/*
		 * Implement your alias analysis here. A1.answers should include the Yes/No answers for 
		 * the queries
		 */

		debug(arg0.getMethod().getName());
		debug(arg0.getMethod().getDeclaringClass());
		UnitGraph g = new BriefUnitGraph(arg0);

		//initialize everything with top
		get_out = new HashMap<>();
		for(Unit s: g) {
			Out out = new Out();
			get_out.put(s, out);
		}

		Stack<Unit> work_list = new Stack<>();
		Set<Unit> visited = new HashSet<>();
		work_list.push(g.getHeads().get(0));

		while(!work_list.isEmpty()) {
			Unit to_process = work_list.pop();
			visited.add(to_process);
			Out out = new Out();
			//debug(to_process);
			if(to_process instanceof AssignStmt) {
				out = process_assign(to_process, g);
			} /*else if(((Stmt) to_process).containsInvokeExpr()) {
				out = process_invoke(to_process, g);
			} */else {
				for(Unit pred: g.getPredsOf(to_process)) {
					out = out.union(get_out.get(pred));
				}
			}

			//print_out(out);

			if(!get_out.get(to_process).isEqual(out)) {
				get_out.put(to_process, out);
				for(Unit suc: g.getSuccsOf(to_process)) {
					work_list.push(suc);
				}
			} else {
				for(Unit suc: g.getSuccsOf(to_process)) {
					if(!visited.contains(suc)) work_list.push(suc);
				}
			}
		}

		print_out(get_out.get(g.getTails().get(0)));

//		for(Unit s: get_out.keySet()) {
//			print_out(get_out.get(s));
//		}

//		for(Unit s : g) {
//			if(s instanceof AssignStmt) {
//				debug(s);
//				Value l = ((AssignStmt) s).getLeftOp();
//				Value r = ((AssignStmt) s).getRightOp();
//				debug(l + ": " + l.getClass());
////				if(l instanceof JInstanceFieldRef) {
////					System.out.println(((JInstanceFieldRef) l).getBase());
////					System.out.println(((JInstanceFieldRef) l).getField().getName());
////				}
//				System.out.println(r + ": " + r.getClass());
////				if(r instanceof JInstanceFieldRef) {
////					System.out.println(((JInstanceFieldRef) r).getBase());
////					System.out.println(((JInstanceFieldRef) r).getField().getName());
////				}
////				if(r instanceof JVirtualInvokeExpr) {
////					System.out.println(((JVirtualInvokeExpr) r).getBase());
////					System.out.println(((JVirtualInvokeExpr) r).getMethod().getName());
////					System.out.println(((JVirtualInvokeExpr) r).getArgs());
////				}
//				System.out.println("-----");
//			}
//		}

		for(AliasQuery q: A1.queryList) {
			if(q.getClassName().equals(arg0.getMethod().getDeclaringClass().toString())) {
				if(q.getMethodName().equals(arg0.getMethod().getName())) {
					A1.answers[A1.queryList.indexOf(q)] = "Yes";
				}
			}
		}
	}
	
}
