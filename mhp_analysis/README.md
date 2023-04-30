# a36235

## Instructions for Assignment 3:

Mhp Analysis assignment is given below.

http://www.cse.iitm.ac.in/~krishna/cs6235/a3.html

You need to implement the *internalTransform* method in *MhpAnalysis* class under *submit_a3* package.

 *A3* is the main class. It takes the input file from *inputs* folder and queries file from *queries* folder.
 
 ### Spark call graph
 You can get the Spark points-to analysis by 
 ````
 PointsToAnalysis pta = Scene.v().getPointsToAnalysis()
 ````
 And you can get the points-to sets of locals, fields by using *reachingObjects* method of *PointsToAnalysis*. It is overloaded method taking different kinds of arguments. It will return a *PointsToSet*. You can check the aliases by using *hasNonEmptyIntersection* method. 
 The types of objects in the *PointsToSet* can be obtained by using its *possibleTypes* method. 
 
 You can iterate over the *PointsToSet* using *P2SetVisitor*.You will actually get *PointsToSetInternal* type of points-to set (it is an implementation of  *PointsToSet* interface).
 ````

 P2SetVisitor vis = new P2SetVisitor() {

	            @Override
	            public void visit(Node n) {
	             /* Do something with node n*/
		     
	            }
	          };
	          pti.forall(vis); //pti is the *PointsToSetInternal* set
````
 

 
 ### Running from command-line:
 
 From the project folder (a36235) run the following command in command-line.
 
 java -classpath "bin/:lib/soot-4.3.0-jar-with-dependencies.jar:" -Dtest.file="queries/Q1.txt" submit_a3.A3 -pp -p jb use-original-names:true 
          -p  cg.spark enabled:true,on-fly-cg:true,apponly:true -src-prec java P1
 
 *Note* : You can set the classpath appropriately if you have different file structure. Remember to add path of soot jar to the classpath while running from command line.
 
- The queries file path is given by -Dtest.file, which can be accessed using System.getProperty("test.file")
- **-pp** : indicates to prepend java class path to soot classpath. Initially soot classpath is empty
- **-cp "inputs"** : indicates to include "inputs"(where P1.java is placed) in soot classpath 
- **-w** : indicates to run in whole program mode.
- **-app** : indicates to run in application mode. Whatever classes referred from argument classes (here P1) will also be application classes(except library classes). Application classes are the ones that will be processed by soot.
- **-p cg.spark enabled:true,on-fly-cg:true,apponly:true** : enables building Spark call graph with only application classes
- **src-prec java** : indicates the source precision to be java file instead of .class file. This is to retain original names of variables. If source format is .class, the bytecode variable names will appear in jimple format.

All these options are encoded in the getOptions method of A3 class.

If you want to give any further options or change the existing options, you can try them by passing command line arguments. For normal functioning required for assignment, you need not change any options. 
 
 
      
### Query form      

Each query is of the form
*&lt;leftVar&gt;:&lt;leftField&gt;,&lt;rightVar&gt;:&lt;rightField&gt;*
      
It represents, "Can the accesses to fields *&lt;leftVar&gt;.&lt;leftField&gt;* and *&lt;rightVar&gt;.&lt;rightField&gt;* lead to data race? "
      
Your analysis should answer either "yes" or "No" for this.

The answers should follow the same order as of queries in the query file.
      
Your answers should be present in static String array *answers* in *A3* class which can be accessed as *A3.answers*.

### Example

Consider the below example,
      
```

class P1 {
	public static void main(String[] args) {
		try {
			 A x;
			 A y;
			 P1 z;
			 
			 x = new A();
			 y = new A(); 
			 z = new P1(); 
			 x.start();
			 x.f1 = z;
			 y.start();
			 y.join();
			 x.join();
		
			}catch (Exception e) {
					
			} 
	}
}
	 
class A extends Thread{
		P1 f1;
		
		public void run() {
			try {
				A a;
				P1 b;
				a = this;
				b = new P1();
				a.f1 = b;
			}catch(Exception e) {
				
			}
		}
	}

	

```
      
The Query
      
```
x:f1,y:f1
```

should answer
```      
No
```
It means, A3.answers[0] should contain No, and so on..
