package submit_a2;

import java.util.*;

import com.google.common.collect.Sets;
import dont_submit.EscapeQuery;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import static submit_a2.A2.answers;
import static submit_a2.A2.queryList;

public class EscapeAnalysis extends SceneTransformer{

	void debug(Object s) {
		//System.out.println(s);
	}

	//helper function to print the outs
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

	//helper function to print escape references
	void print_escape(Out o) {
		debug(":------ESCAPED------:");
		for(String s: o.ES) {
			debug(s);
		}
		debug(":------END-ESC------:");
	}

	//Worklist for the Escape Analysis part
	Stack<SootMethod> esc_worklist = new Stack<>();

	//Call-Graph Declaration
	CallGraph cg;

	//out contains stack, heap and ES info
	//the heap remains same
	//the ES remains same
	//The
	public class Out {
		//Og indicates global reference
		//@ indicates any field
		//null indicates not yet initialized -> since grammar doesn't allow constructor definition
		HashMap<String, Set<String>> rho_data;                                    //contains stack info
		HashMap<String, HashMap<String, Set<String>>> sigma_data;                //contains heap info
		Set<String> ES;

		Out() {                                                                    //initialization
			rho_data = new HashMap<>();
			sigma_data = new HashMap<>();
			ES = new HashSet<>();
		}

		public Set<String> rho(String a) {
			return rho_data.get(a);
		}

		public Set<String> sigma(String ref, String field) {
			if(!sigma_data.get(ref).containsKey(field) && sigma_data.get(ref).containsKey("@")) return sigma_data.get(ref).get("@");
			return sigma_data.get(ref).get(field);
		}

		//gets all reachable ref
		public Set<String> get_reachable(String a) {
			Set<String> reachable = new HashSet<>();
			Stack<String> g = new Stack<>();
			g.addAll(rho(a));
			while(!g.empty()) {
				String curr = g.pop();
				if(reachable.contains(curr)) continue;
				reachable.add(curr);
				if(!sigma_data.containsKey(curr)) continue;
				for(String field: sigma_data.get(curr).keySet()) {
					g.addAll(sigma(curr, field));
				}
			}
			return reachable;
		}

		Out get_follow(String a) {
			Out follow = new Out();
			//setting the stack data
			follow.rho_data.put("this", rho(a));

			//setting the heap data
			Set<String> reachable = get_reachable(a);
			for(String ref : reachable) {
				if(sigma_data.containsKey(ref)) {
					HashMap<String, Set<String>> to_change = new HashMap<>(sigma_data.get(ref));
					follow.sigma_data.put(ref, to_change);
				}
			}

			//setting the escape set
			for(String ref: reachable) {
				if(ES.contains(ref)) {
					follow.ES.add(ref);
				}
			}

			return follow;
		}

		//gives if a particular reference escapes
		public boolean is_escape(String ref) {
			if(ES.contains(ref)) return true;
			else return false;
		}

		//stack union
		Out stack_union(Out a) {
			//Union of rho_data
			for(String name: a.rho_data.keySet()) {
				Set<String> u_rho;
				if(!this.rho_data.containsKey(name)) {
					u_rho = new HashSet<>(a.rho(name));
					if(u_rho.size() > 1 && u_rho.contains("null")) u_rho.remove("null");
				} else if(this.rho(name).contains("Og") || a.rho(name).contains("Og")) {
					u_rho = new HashSet<>();
					u_rho.add("Og");
				} else {
					u_rho = Sets.union(this.rho_data.get(name), a.rho(name));
					if(u_rho.size() > 1 && u_rho.contains("null")) u_rho.remove("null");
				}
				this.rho_data.put(name, u_rho);
			}
			return this;
		}

		//heap union
		Out heap_union(Out a) {
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
				} else {
					for(String field: a.sigma_data.get(ref).keySet()) {
						Set<String> u_f_sig;
						if(!this.sigma_data.get(ref).containsKey(field)) {
							u_f_sig = new HashSet<>(a.sigma(ref, field));
							if(u_f_sig.size() > 1 && u_f_sig.contains("null")) u_f_sig.remove("null");
						} else if(this.sigma(ref, field).contains("Og") || a.sigma(ref, field).contains("Og")) {
							u_f_sig = new HashSet<>();
							u_f_sig.add("Og");
						} else {
							u_f_sig = Sets.union(this.sigma(ref, field), a.sigma(ref, field));
							if(u_f_sig.size() > 1 && u_f_sig.contains("null")) u_f_sig.remove("null");
						}
						this.sigma_data.get(ref).put(field, u_f_sig);
					}
				}
			}
			return this;
		}

		//ES union
		Out es_union(Out a) {
			//Union of ES data
			ES.addAll(a.ES);
			return this;
		}

		//union -> changes the object which calls it.
		Out union(Out a) {

			this.stack_union(a);
			this.heap_union(a);
			this.es_union(a);

			return this;
		}

		Boolean is_stack_equal(Out a) {
			//comparing stack space
			if(a.rho_data.size() != this.rho_data.size()) {
				return false;
			}
			for(String name: a.rho_data.keySet()) {
				if(!this.rho_data.containsKey(name)) return false;
				else if(!a.rho(name).equals(this.rho(name))) return false;
			}

			return true;
		}

		Boolean is_heap_equal(Out a) {
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

		Boolean is_ES_equal(Out a) {
			//comparing ES
			if(this.ES.size() != a.ES.size()) return false;
			for(String ref: a.ES) {
				if(!this.ES.contains(ref)) return false;
			}

			return true;
		}

		Boolean isEqual(Out a) {
			return is_stack_equal(a) && is_heap_equal(a) && is_ES_equal(a);
		}
	}

	//INFO
	//this contains the initial start environment for the function
	//this also contains the information of the flow sensitive stack, heap and EM data
	public class INFO {
		Out env;
		Out at_syn;
		Out ret;

		Boolean can_remove = true;

		INFO() {
			env = new Out();
			at_syn = new Out();
			ret = new Out();
		}
	}

	//this map gives the INFO of particular method
	HashMap<SootMethod, INFO> get_INFO = new HashMap<>();

	//helper function to take union of outs
	Out union(List<Out> outs) {
		Out uni = new Out();
		for (Out out : outs) {
			uni = uni.union(out);
		}
		return uni;
	}

	//check if the given sootClass extends Thread
	Boolean is_Thread(SootClass s) {
		while(s.hasSuperclass()) {
			if(s.getName().equals("java.lang.Thread")) return true;
			s = s.getSuperclass();
		}
		return false;
	}

	//helper function to analyse the every relevant in a function (assignment stmt)
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
		// static field ref = a
		if(left instanceof StaticFieldRef && right instanceof JimpleLocal) {
			//add ref all reachable from 'a' to escape set
			in.ES.addAll(in.get_reachable(right.toString()));
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
			//checking if the class extends Thread
			SootClass cls = Scene.v().getSootClass(right.getType().toString());
			if(is_Thread(cls)) {
				in.ES.add(ref);
			}
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
					if(in.is_escape(ref)) {
						replace.add("Og");
						break;
					}
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
		//type; a.f = b
		if(left instanceof JInstanceFieldRef && right instanceof JimpleLocal) {
//			if(((JInstanceFieldRef) left).getBase().toString().equals("this")) {
//				//all reachable from b escapes
//				in.ES.addAll(in.get_reachable(right.toString()));
//				return in;
//			}
			String field = ((JInstanceFieldRef) left).getField().getName();
			for(String ref : in.rho(((JInstanceFieldRef) left).getBase().toString())) {
				if(in.is_escape(ref)) {
					in.ES.addAll(in.get_reachable((right.toString())));
					return in;
				}
				Set<String> replace = new HashSet<>();
				if(in.sigma(ref, field) != null) replace.addAll(in.sigma(ref, field));
				replace = Sets.union(replace, in.rho(right.toString()));
				HashMap<String, Set<String>> to_change = new HashMap<>(in.sigma_data.get(ref));
				to_change.put(field, replace);
				in.sigma_data.put(ref, to_change);
			}
			return in;
		}

		return in;
	}

	//helper function to process the identity stmt
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

//		if(right instanceof ThisRef) {
//			Set<String> replace = new HashSet<>();
//			HashMap<String, Set<String>> to_change = new HashMap<>();
//			replace.add(right.getType().toString());
//			in.rho_data.put(left.toString(), replace);
//			replace = new HashSet<>();
//			replace.add("Og");
//			to_change.put("@", replace);
//			in.sigma_data.put(right.getType().toString(), to_change);
//			return in;
//		}

//		if(right instanceof ParameterRef) {
//			Set<String> replace = new HashSet<>();
//			replace.add("Og");
//			in.rho_data.put(left.toString(), replace);
//			return in;
//		}

		return in;
	}

	//helper function to process invoke stmt
	Out process_invoke(Unit s, UnitGraph g, HashMap<Unit, Out> get_out) {
		InvokeExpr right = ((Stmt) s).getInvokeExpr();

		List<Unit> pred_s = g.getPredsOf(s);
		List<Out> pred_outs = new ArrayList<>();
		for(Unit pred: pred_s) {
			pred_outs.add(get_out.get(pred));
		}

		Out in = union(pred_outs);

		if(right instanceof VirtualInvokeExpr) {
			String to_search = ((VirtualInvokeExpr) right).getBase().toString();
			Iterator<Edge> eIter = cg.edgesOutOf(s);

			while(eIter.hasNext()) {
				SootMethod called = eIter.next().tgt();
				//taking care of start function of a thread class
				String name = called.getName();
				SootClass called_class = called.getDeclaringClass();
				if(name.equals("start") && is_Thread(called_class)) continue;

				INFO foo = get_INFO.get(called);
				Out env = in.get_follow(to_search);
				if(foo == null) continue;
				if(!foo.env.isEqual(env)) {
					foo.env = env;
					esc_worklist.add(called);
				}
				in.heap_union(foo.ret);
				in.es_union(foo.ret);
			}
		} else if(right instanceof SpecialInvokeExpr) {
			String to_search = ((SpecialInvokeExpr) right).getBase().toString();
			Set<String> replace = new HashSet<>();
			replace.add("null");

			for(String ref : in.rho(to_search)) {
				HashMap<String, Set<String>> change_to = new HashMap<>();
				change_to.put("@", replace);
				in.sigma_data.put(ref, change_to);
			}
		}
		return in;
	}


	//this analyses the function, with given stack, heap and ES
	Out analyse_func(SootMethod foo) {
		Body arg0 = foo.getActiveBody();
		INFO info = get_INFO.get(foo);
		UnitGraph g = new BriefUnitGraph(arg0);

		HashMap<Unit, Out> get_out;
		//initialize everything with top
		get_out = new HashMap<>();

		for(Unit s: g) {
			Out out = new Out();
			get_out.put(s, out);
		}

		Out temp = new Out();
		temp.union(info.env);
		get_out.put(g.getHeads().get(0), temp);

		Stack<Unit> work_list = new Stack<>();
		Set<Unit> visited = new HashSet<>();
		work_list.push(g.getHeads().get(0));

		while(!work_list.isEmpty()) {
			Unit to_process = work_list.pop();
			visited.add(to_process);
			Out out = new Out();

			if(to_process instanceof AssignStmt) {
				out = process_assign(to_process, g, get_out);
			} else if(to_process instanceof JIdentityStmt) {
				out = process_identity(to_process, g, get_out);
			} else if(((Stmt) to_process).containsInvokeExpr()) {
				out = process_invoke(to_process, g, get_out);
			} else {
				for(Unit pred: g.getPredsOf(to_process)) {
					out = out.union(get_out.get(pred));
				}
			}

			out.union(get_out.get(to_process));

			if(!get_out.get(to_process).isEqual(out)) {
				get_out.put(to_process, out);
				if(to_process instanceof JEnterMonitorStmt) {
					info.at_syn = out;
					for(String ref: out.rho(((JEnterMonitorStmt) to_process).getOp().toString())) {
						if(ref.equals("null")) continue;
						if(ref.equals("Og") || out.is_escape(ref)) info.can_remove = false;
						else info.can_remove = true;
					}
				}
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
		debug(foo.toString());
		print_out(end_out);
		print_escape(end_out);
		print_escape(info.at_syn);
		Out follow = end_out.get_follow("this");
		//adding callers if follow heap or escape map changes
		if(!follow.isEqual(info.ret)) {
			info.ret = follow;
			//debug(foo.getDeclaringClass().getMethodByName("start"));
			Iterator<Edge> eIter = cg.edgesInto(foo);
			while(eIter.hasNext()) {
				SootMethod m = eIter.next().src();
				if(m.getName().equals("start") && is_Thread(m.getDeclaringClass())) continue;
				esc_worklist.add(m);
			}
		}
		return end_out;
	}

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		/*
		 * Implement your escape analysis here
		 */

		cg = Scene.v().getCallGraph();

		List<SootMethod> methods = new ArrayList<>();
		for (SootClass clazz : Scene.v().getApplicationClasses()) {
			methods.addAll(clazz.getMethods());
		}

		// Initialization
		for (SootMethod method : methods) {
			INFO info = new INFO();
			get_INFO.put(method, info);
		}

		//main method's Class object should be created
		SootClass _main_class = Scene.v().getMainClass();
		for(SootMethod foo : _main_class.getMethods()) {
			Set<String> to_set = new HashSet<>();
			to_set.add("O0");
			get_INFO.get(foo).env.rho_data.put("this", to_set);
		}

		//start from main method
		esc_worklist.add(Scene.v().getMainMethod());

		//work-list algorithm implementation
		while(!esc_worklist.empty()) {
			SootMethod foo = esc_worklist.pop();
			analyse_func(foo);
		}

		//answering the questions
		int i = 0;
		for(EscapeQuery query: queryList) {
			SootClass cls;
			for (SootClass c : Scene.v().getApplicationClasses()) {
				if(c.getName().equals(query.getClassName())) {
					cls = c;
					SootMethod m = cls.getMethodByName(query.getMethodName());
					INFO info  = get_INFO.get(m);
					if(info.can_remove) answers[i] = "Yes";
					else answers[i] = "No";
					break;
				}
			}
			i++;
		}
	}

}