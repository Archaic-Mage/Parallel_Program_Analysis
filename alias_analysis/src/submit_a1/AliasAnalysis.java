package submit_a1;

import java.lang.reflect.Field;
import java.util.*;

import com.google.common.collect.Sets;
import dont_submit.AliasQuery;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
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
		//null indicates not yet initialized -> since grammar doesn't allow constructor definition
        HashMap<String, Set<String>> rho_data;									//contains stack info
        HashMap<String, HashMap<String, Set<String>>> sigma_data;				//contains heap info
		Out() {																	//initialization
			rho_data = new HashMap<>();
			sigma_data = new HashMap<>();
		}
        public Set<String> rho(String a) {
            return rho_data.get(a);
        }
        public Set<String> sigma(String ref, String field) {
			if(!sigma_data.get(ref).containsKey(field) && sigma_data.get(ref).containsKey("@")) return sigma_data.get(ref).get("@");
            return sigma_data.get(ref).get(field);
        }
		//union -> changes the object which calls it.
		Out union(Out a) {
			//Union of rho_data
			for(String name: a.rho_data.keySet()) {
				Set<String> u_rho;
				if(!this.rho_data.containsKey(name)) {
					u_rho = new HashSet<>(a.rho(name));
					if(u_rho.size() > 1 && u_rho.contains("null")) u_rho.remove("null");
				}
				else if(this.rho(name).contains("Og") || a.rho(name).contains("Og")) {
					u_rho = new HashSet<>();
					u_rho.add("Og");
				}
				else {
					u_rho = Sets.union(this.rho_data.get(name), a.rho(name));
					if(u_rho.size() > 1 && u_rho.contains("null")) u_rho.remove("null");
				}
				this.rho_data.put(name, u_rho);
			}

			//Union of sigma_data
			for(String ref: a.sigma_data.keySet()) {
				if(!this.sigma_data.containsKey(ref)) {
					HashMap<String, Set<String>> to_change = new HashMap<>(a.sigma_data.get(ref));
					for(String field: to_change.keySet()) {
						Set<String> temp = new HashSet<>(to_change.get(field));
						if(temp.size() > 1 && temp.contains("null")) temp.remove("null");
						to_change.put(field, temp);
					}
					this.sigma_data.put(ref, to_change);
				}
				else {
					for(String field: a.sigma_data.get(ref).keySet()) {
						Set<String> u_f_sig;
						if(!this.sigma_data.get(ref).containsKey(field)) {
							u_f_sig = new HashSet<>(a.sigma(ref, field));
							if(u_f_sig.size() > 1 && u_f_sig.contains("null")) u_f_sig.remove("null");
						}
						else if(this.sigma(ref, field).contains("Og") || a.sigma(ref, field).contains("Og")) {
							u_f_sig = new HashSet<>();
							u_f_sig.add("Og");
						}
						else {
							u_f_sig = Sets.union(this.sigma(ref, field), a.sigma(ref, field));
							if(u_f_sig.size() > 1 && u_f_sig.contains("null")) u_f_sig.remove("null");
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
					for(String field: a.sigma_data.get(ref).keySet()) {
						if(!this.sigma_data.get(ref).containsKey(field)) return false;
						else if(!a.sigma(ref, field).equals(this.sigma(ref, field))) return false;
					}
				}
			}

			return true;
		}
    }
    //HashMap<Unit, Out> get_out;
	Out union(List<Out> outs) {
		Out uni = new Out();
		for (Out out : outs) {
			uni = uni.union(out);
		}
		return uni;
	}
    Out process_assign(Unit s, UnitGraph g, HashMap<Unit, Out> get_out) {
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

		// a = static field
		if(left instanceof JimpleLocal && right instanceof StaticFieldRef) {
			Set<String> replace = new HashSet<>();
			replace.add("Og");
			in.rho_data.put(left.toString(), replace);
			return in;
		}
		//allocation type a = new b();
        if(left instanceof JimpleLocal && right instanceof JNewExpr) {
			int ln = ((SourceLnPosTag) s.getTag("SourceLnPosTag")).startLn();
			String ref = "O"+ln;
			Set<String> replace = new HashSet<>();
			HashMap<String, Set<String>> heap_init = new HashMap<>();
			replace.add(ref);
			in.rho_data.put(left.toString(), replace);
			in.sigma_data.put(ref, heap_init);
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
			if(in.rho_data.get(((JInstanceFieldRef) right).getBase().toString()).contains("Og")) {
				replace.add("Og");
			} else {
				for(String ref: in.rho(((JInstanceFieldRef) right).getBase().toString())) {
					replace = Sets.union(replace, in.sigma(ref, field));
				}
				if(replace.contains("Og")) {
					replace = new HashSet<>();
					replace.add("Og");
				}
			}
			in.rho_data.put(left.toString(), replace);
			return in;
		}
		//assign return of a call a = b.foo(args...)
		if(left instanceof JimpleLocal && right instanceof JVirtualInvokeExpr) {
			//a points to Og
			Set<String> replace = new HashSet<>();
			replace.add("Og");
			in.rho_data.put(left.toString(), replace);

			//all b.f points to Og
			for(String ref : in.rho(((JVirtualInvokeExpr) right).getBase().toString())) {
				HashMap<String, Set<String>> change_to = new HashMap<>();
				change_to.put("@", replace);
				in.sigma_data.put(ref, change_to);
			}

			//all args.f points to Og
			for(Value arg : ((JVirtualInvokeExpr) right).getArgs()) {
				for(String ref: in.rho(arg.toString())) {
					HashMap<String, Set<String>> change_to = new HashMap<>();
					change_to.put("@", replace);
					in.sigma_data.put(ref, change_to);
				}
			}
			return in;
		}
		//type; a.f = b
		if(left instanceof JInstanceFieldRef && right instanceof JimpleLocal) {
			if(in.rho(((JInstanceFieldRef) left).getBase().toString()).contains("Og")) {
				return in;
			}
			String field = ((JInstanceFieldRef) left).getField().getName();
			for(String ref : in.rho(((JInstanceFieldRef) left).getBase().toString())) {
				Set<String> replace = new HashSet<>();
				if(in.rho(((JInstanceFieldRef) left).getBase().toString()).size() == 1) {
					replace.addAll(in.rho(right.toString()));
				}
				else {
					if(in.sigma(ref, field) != null) replace.addAll(in.sigma(ref, field));
					replace = Sets.union(replace, in.rho(right.toString()));
				}
				HashMap<String, Set<String>> to_change = new HashMap<>(in.sigma_data.get(ref));
				to_change.put(field, replace);
				in.sigma_data.put(ref, to_change);
			}
			return in;
		}

        return in;
    }

	Out process_identity(Unit s, UnitGraph g, HashMap<Unit, Out> get_out) {
		JIdentityStmt a_s = (JIdentityStmt) s;
		List<Unit> pred_s = g.getPredsOf(s);
		List<Out> pred_outs = new ArrayList<>();
		for(Unit pred: pred_s) {
			pred_outs.add(get_out.get(pred));
		}

		Out in = union(pred_outs);
		Value left = a_s.getLeftOp();
		Value right = a_s.getRightOp();

		if(right instanceof ThisRef) {
			Set<String> replace = new HashSet<>();
			HashMap<String, Set<String>> to_change = new HashMap<>();
			replace.add(right.getType().toString());
			in.rho_data.put(left.toString(), replace);
			replace = new HashSet<>();
			replace.add("Og");
			to_change.put("@", replace);
			in.sigma_data.put(right.getType().toString(), to_change);
			return in;
		}

		if(right instanceof ParameterRef) {
			Set<String> replace = new HashSet<>();
			replace.add("Og");
			in.rho_data.put(left.toString(), replace);
			return in;
		}

		return in;
	}
	Out process_invoke(Unit s, UnitGraph g, HashMap<Unit, Out> get_out) {
		InvokeExpr right = ((Stmt) s).getInvokeExpr();

		List<Unit> pred_s = g.getPredsOf(s);
		List<Out> pred_outs = new ArrayList<>();
		for(Unit pred: pred_s) {
			pred_outs.add(get_out.get(pred));
		}

		Out in = union(pred_outs);

		Set<String> replace = new HashSet<>();
		replace.add("Og");
		//all b.f points to Og
		String to_search = "";
		if(right instanceof VirtualInvokeExpr) to_search = ((VirtualInvokeExpr) right).getBase().toString();
		else if(right instanceof SpecialInvokeExpr) {
			to_search = ((SpecialInvokeExpr) right).getBase().toString();
			replace = new HashSet<>();
			replace.add("null");
		}

		for(String ref : in.rho(to_search)) {
			HashMap<String, Set<String>> change_to = new HashMap<>();
			change_to.put("@", replace);
			in.sigma_data.put(ref, change_to);
		}

		//all args.f points to Og
		for(Value arg : right.getArgs()) {
			if(!(arg instanceof FieldRef)) continue;
			for(String ref: in.rho(arg.toString())) {
				HashMap<String, Set<String>> change_to = new HashMap<>();
				change_to.put("@", replace);
				in.sigma_data.put(ref, change_to);
			}
		}
		return in;
	}

	//debug information
	void debug(Object s) {
		//System.out.println(s);
	}

	void print_out(Out o) {
		debug("-------------------");
		debug("Stack Space Data");
		for(String name: o.rho_data.keySet()) {
			for(String ref: o.rho(name)) {
				debug(name + "->" + ref);
			}
		}
		debug("...................");
		debug("Heap Space Data");
		for(String ref: o.sigma_data.keySet()) {
			for(String field: o.sigma_data.get(ref).keySet()) {
				for(String ref1: o.sigma(ref, field)) {
					debug(ref + "-" + field + "->" + ref1);
				}
			}
		}
		debug("-------------------");
	}

	@Override
	protected void internalTransform(Body arg0, String arg1, Map<String, String> arg2) {
		/*
		 * Implement your alias analysis here. A1.answers should include the Yes/No answers for 
		 * the queries
		 */

		debug("******************");
		debug(arg0.getMethod().getDeclaringClass());
		debug(arg0.getMethod().getName());
		UnitGraph g = new BriefUnitGraph(arg0);

		HashMap<Unit, Out> get_out;
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

			if(to_process instanceof AssignStmt) {
				out = process_assign(to_process, g, get_out);
			}
			else if(to_process instanceof JIdentityStmt) {
				out = process_identity(to_process, g, get_out);
			}
			else if(((Stmt) to_process).containsInvokeExpr()) {
				out = process_invoke(to_process, g, get_out);
			}
			else {
				for(Unit pred: g.getPredsOf(to_process)) {
					out = out.union(get_out.get(pred));
				}
			}

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

		Out end_out = get_out.get(g.getTails().get(0));
		print_out(end_out);

		for(AliasQuery q: A1.queryList) {
			if(q.getClassName().equals(arg0.getMethod().getDeclaringClass().toString())) {
				if(q.getMethodName().equals(arg0.getMethod().getName())) {
					//may analysis so default case is taken to be yes
					String ans = "Yes";
					String left = q.getLeftVar();
					String right = q.getRightVar();
					Set<String> left_rho = new HashSet<>();
					Set<String> right_rho;

					if(end_out.rho_data.containsKey(left)) left_rho.addAll(end_out.rho(left));
					else left_rho.addAll(end_out.sigma(q.getClassName(), left));

					if(end_out.rho_data.containsKey(right)) right_rho = end_out.rho(right);
					else right_rho = end_out.sigma(q.getClassName(), right);

					//by checking our analysis we report no
					if(right_rho == null || left_rho.isEmpty()) ans = "No";
					else if(!left_rho.contains("Og") && !right_rho.contains("Og")) {
						left_rho.removeAll(right_rho);
						right_rho = left_rho; //A-B
						left_rho = new HashSet<>();

						if(end_out.rho_data.containsKey(left)) left_rho.addAll(end_out.rho(left));
						else left_rho.addAll(end_out.sigma(q.getClassName(), left));
						//A - (A-B) = A intersection B
						left_rho.removeAll(right_rho);

						if(left_rho.isEmpty() || (left_rho.size() == 1 && left_rho.contains("null"))) ans = "No";
					}
					A1.answers[A1.queryList.indexOf(q)] = ans;
				}
			}
		}

		debug("*******************");
	}
	
}
