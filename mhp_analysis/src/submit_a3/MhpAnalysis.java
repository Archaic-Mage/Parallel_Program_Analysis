package submit_a3;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JEnterMonitorStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.toolkits.callgraph.CallGraph;

import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.util.Chain;

public class MhpAnalysis extends SceneTransformer{

	//debug function
	//prints the given parameter
	public void debug(Object o) {
		System.out.println(o);
	}

	public void printSet(Set<String> s) {
		for(String str : s) {
			debug(str);
		}
	}

	//Call Graph
	CallGraph cg;

	//PEG node
	public class PEGNode {
		//unit is null for special node
		Unit u;
		//class:method for normal nodes
		//obj,stmt,class:method for special nodes
		String thread;
		//true for special node, false for normal node
		boolean type;
		public PEGNode(Unit u, String thread, boolean type) {
			this.u = u;
			this.thread = thread;
			this.type = type;
		}

		public String getSpecial() {
			if(!type) return null;
			String[] token = thread.split(",");
			return token[1];
		}

		public String toString() {
			if(type) {
				return thread;
			}
			else {
				//line no of the unit
				int line = ((SourceLnPosTag) u.getTag("SourceLnPosTag")).startLn();
				// unit,thread
				return u.toString()+ ":" + line + "," + thread;
			}
		}
	}

	//compute the PEG for the program
	public class PEG {
		HashMap<String, Set<String>> peg = new HashMap<>();
		//String to PEGNode
		HashMap<String, PEGNode> node_map = new HashMap<>();

		//Local to thread id
		HashMap<Local, String> thread_id_map = new HashMap<>();

		//N(thread name) -> Set of Nodes
		HashMap<String, Set<String>> thread_map = new HashMap<>();
		//add a node to the thread map
		void addThread(String thread, String node) {
			if(!thread_map.containsKey(thread)) {
				thread_map.put(thread, new HashSet<>());
			}
			thread_map.get(thread).add(node);
		}

		//get the set of node for the given thread
		Set<String> N(String thread) {
			return thread_map.get(thread);
		}

		//start pred node
		HashMap<String, Set<String>> start_pred = new HashMap<>();
		//add a start pred node
		void addStartPred(String begin, String node) {
			if(!start_pred.containsKey(begin)) {
				start_pred.put(begin, new HashSet<>());
			}
			start_pred.get(begin).add(node);
		}
		Set<String> startPred(String begin) {
			return start_pred.get(begin);
		}

		//local pred node
		Set<String> localPred(String node) {
			Set<String> ret = new HashSet<>();
			for(String s: peg.keySet()) {
				if(peg.get(s).contains(node)) {
					ret.add(s);
				}
			}
			return ret;
		}

		//waiting pred node
		HashMap<String, String> waiting_pred = new HashMap<>();
		//add a waiting pred node
		void addWaitingPred(String node, String pred) {
			waiting_pred.put(node, pred);
		}
		String waitingPred(String node) {
			return waiting_pred.get(node);
		}

		//Monitor
		HashMap<Local, Set<String>> _monitor = new HashMap<>();
		//add a monitor node
		void addMonitor(Local l, String node) {
			if(!_monitor.containsKey(l)) {
				_monitor.put(l, new HashSet<>());
			}
			_monitor.get(l).add(node);
		}

		//get the monitor stmt by the given enter monitor unit
		void getMointor(Unit enter_monitor, UnitGraph g) {
			//obj on which the monitor is entered
		}

		/* printing functions */

		//print the graph for the given method
		void printGraph(SootMethod m, int thread_id) {
			//thread name
			String thread = m.getDeclaringClass().getName() + ":" + m.getName() + ":" + thread_id;
			//begin node
			String begin_name = "*,begin,"+thread;
			Stack<String> s = new Stack<>();
			s.push(begin_name);
			Set<String> visited = new HashSet<>();
			while(!s.isEmpty()) {
				String cur = s.pop();
				if(visited.contains(cur)) {
					continue;
				}
				visited.add(cur);
				//add edges if present
				if(peg.containsKey(cur)) {
					for(String next: peg.get(cur)) {
						System.out.println(cur + " -> " + next);
						s.push(next);
					}
				}
			}
		}

		//print all the nodes in the graph
		void printNodes() {
			for(String s: node_map.keySet()) {
				debug(s);
			}
		}

		//add edge between a and b
		void addEdge(String a, String b) {
			if(!peg.containsKey(a)) {
				peg.put(a, new HashSet<>());
			}
			peg.get(a).add(b);
		}
		//construct PEG for the given method
		public void constructPEG(SootMethod m, int thread_count) {
			int thread_id = thread_count;
			UnitGraph g = new BriefUnitGraph(m.getActiveBody());
			//get the thread name - class:method
			String thread = m.getDeclaringClass().getName() + ":" + m.getName() + ":" + thread_count;

			//begin node - special node
			String begin_name = "*,begin,"+thread;
			PEGNode begin_node = new PEGNode(null, begin_name, true);
			//begin to node map
			node_map.put(begin_node.toString(), begin_node);
			addThread(thread, begin_node.toString());

			Unit head = g.getHeads().get(0);
			//make PEGNode for head
			PEGNode head_node = new PEGNode(head, thread, false);
			//head to node map
			node_map.put(head_node.toString(), head_node);
			addThread(thread, begin_node.toString());

			//add begin node -> head
			addEdge(begin_node.toString(), head_node.toString());

			//iterate over the graph
			Stack<Unit> s = new Stack<>();
			Set<Unit> visited = new HashSet<>();
			s.push(head);
			if(g.getHeads().size() > 2) s.push(g.getHeads().get(2));
			while(!s.isEmpty()) {
				Unit u = s.pop();
				//if already visited, continue
				if(visited.contains(u)) {
					continue;
				}
				visited.add(u);
				//make PEGNode for u
				PEGNode u_node = new PEGNode(u, thread, false);
				//add u_node to node map
				node_map.put(u_node.toString(), u_node);
				addThread(thread, u_node.toString());

				//if u is exitmonitor, then add edge to node of getHeads().get(2)
				if(u instanceof ExitMonitorStmt) {
					//PEG node for getHeads().get(2)
					PEGNode next_exit_node = new PEGNode(g.getHeads().get(2), thread, false);
					//add next_exit_node to node map
					node_map.put(next_exit_node.toString(), next_exit_node);
					//add edge from u -> next_exit_node
					addEdge(u_node.toString(), next_exit_node.toString());
				}

				//if u is start(), then add edge to begin node of the next thread
				if(u instanceof InvokeStmt) {
					InvokeExpr expr = ((InvokeStmt) u).getInvokeExpr();
					if(expr.getMethod().getName().equals("start")) {

						//get the object on which start is called
						Local obj = (Local) ((InstanceInvokeExpr) expr).getBase();

						//get the methods called by start from the call graph
						Iterator<Edge> edges = cg.edgesOutOf(u);
						while(edges.hasNext()) {
							Edge e = edges.next();
							//get the method called by start
							SootMethod called_method = e.tgt();
							//print if the method name is run()
							if(called_method.getName().equals("run"))
							{
								//get the next thread name
								String next_thread = called_method.getDeclaringClass().getName() + ":" + called_method.getName() + ":" + (thread_count+1);
								//PEG node for begin node of next thread
								PEGNode next_begin_node = new PEGNode(null, "*,begin,"+next_thread, true);
								//add next_begin_node to node map
								node_map.put(next_begin_node.toString(), next_begin_node);
								//add edge from u -> next_begin_node
								addEdge(u_node.toString(), next_begin_node.toString());
								//add the next thread to the thread_id map
								thread_id_map.put(obj, next_thread);

								//add to start_pred
								addStartPred(next_begin_node.toString(), u_node.toString());

								constructPEG(called_method, thread_count+1);
								thread_count++;
							}
						}
					}
					//else if - wait call
					//add special edges to the graph
					// wait -> waiting -> notified-entry
					else if(expr.getMethod().getName().equals("wait")) {
						//obj of the wait call
						Local obj = (Local) ((InstanceInvokeExpr) expr).getBase();
						//PEG node for waiting
						PEGNode waiting_node = new PEGNode(u, obj.toString()+",waiting,"+thread, true);
						//add waiting_node to node map
						node_map.put(waiting_node.toString(), waiting_node);
						//add waiting node in thread map
						addThread(thread, waiting_node.toString());
						//add edge from u -> waiting_node
						addEdge(u_node.toString(), waiting_node.toString());

						//PEG node for notified-entry
						PEGNode notified_entry_node = new PEGNode(u, obj.toString()+",notified-entry,"+thread, true);
						//add notified_entry_node to node map
						node_map.put(notified_entry_node.toString(), notified_entry_node);
						//add notified-entry node in thread map
						addThread(thread, notified_entry_node.toString());
						//add edge from waiting_node -> notified_entry_node
						addEdge(waiting_node.toString(), notified_entry_node.toString());
						//add to waiting_pred
						addWaitingPred(notified_entry_node.toString(), waiting_node.toString());
						//u_node change to notified-entry
						u_node = notified_entry_node;
					}
				}

				//get the successors
				List<Unit> succs = g.getSuccsOf(u);
				for(Unit succ: succs) {
					//make PEGNode for succ
					PEGNode succ_node = new PEGNode(succ, thread, false);
					//add succ_node to node map
					node_map.put(succ_node.toString(), succ_node);
					//add edge from u -> succ
					addEdge(u_node.toString(), succ_node.toString());
					s.push(succ);
				}
			}
		}
	}

	//M set
	HashMap<String, Set<String>> m_set;

	//Kill set
	HashMap<String, Set<String>> kill_set;

	//Gen set
	HashMap<String, Set<String>> gen_set;

	//Out set
	HashMap<String, Set<String>> out_set;

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		/*
		 * Implement your mhp analysis here
		 */

		//get points to analysis
		PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
		//get the call graph
		cg = Scene.v().getCallGraph();
		//get the main method
		SootMethod mainMethod = Scene.v().getMainMethod();
		PEG peg = new PEG();
		peg.constructPEG(mainMethod, 0);
		peg.printGraph(mainMethod, 0);

		/* Main Algorithm */

		//Initialization - Gen = Kill = M = Out = {}
		m_set = new HashMap<String, Set<String>>();
		kill_set = new HashMap<String, Set<String>>();
		gen_set = new HashMap<String, Set<String>>();
		out_set = new HashMap<String, Set<String>>();

		//Stage one - Initialize the Kill set and Gen set
		for(String s: peg.node_map.keySet()) {
			PEGNode node = peg.node_map.get(s);
			//join node
			if(node.u instanceof JInvokeStmt) {
				//get the invoke expression
				InvokeExpr expr = ((JInvokeStmt) node.u).getInvokeExpr();
				//get the method called
				SootMethod called_method = expr.getMethod();
				if(called_method.getName().equals("join")) {
					//get the object on which join is called
					Local obj = (Local) ((InstanceInvokeExpr) expr).getBase();
					//get the thread id of the object
					String thread = peg.thread_id_map.get(obj);
					kill_set.put(s, peg.N(thread));
					continue;
				}
			}
			//entry node
			if(node.u instanceof JEnterMonitorStmt) {
				//get the object of the enter monitor statement
				Local obj = (Local) ((JEnterMonitorStmt) node.u).getOp();
				kill_set.put(s, moniter(obj));
				continue;
			}


		}
 	}
}

	

