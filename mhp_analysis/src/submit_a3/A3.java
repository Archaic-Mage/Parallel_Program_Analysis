package submit_a3;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import dont_submit.MhpQuery;
import soot.PackManager;
import soot.Transform;


public class A3 {
	static ArrayList<MhpQuery> queryList;
	static String[] answers;
	static String testFilePath;
	public static void main(String args[]) throws IOException {
		
		String[] mainArgs = getOptions(args);

		populateQueries();
		
		PackManager.v().getPack("wjtp").add(new
        		Transform("wjtp.mymhp",
        		new MhpAnalysis()));
		
      
      
      
        soot.Main.main(mainArgs);
        
        System.out.println(answers);
	}
	

	static void populateQueries() throws IOException {
		
		queryList = new ArrayList<MhpQuery>();
			BufferedReader bufRdr = new BufferedReader(new FileReader(testFilePath));
			String line = bufRdr.readLine();
			while(line != null) {
				String[] tokens = line.split(",");
				if(tokens.length != 2) {
					throw new IllegalArgumentException("Please check the query format");
				}
				String[] leftTokens = tokens[0].split(":");
				String leftVar = leftTokens[0];
				String leftField = leftTokens[1];
				String[] rightTokens = tokens[1].split(":");
				String rightVar = rightTokens[0];
				String rightField = rightTokens[1];
				
				MhpQuery aq = new MhpQuery(leftVar, leftField, rightVar, rightField);
				System.out.println(aq);
				queryList.add(aq);	
				line = bufRdr.readLine();
			}
			
		
		answers = new String[queryList.size()];
		bufRdr.close();
		
	}
	
	static String[] getOptions(String args[]) {
		String classPath = "inputs";
		String argumentClass = "P1";
		if(System.getProperty("test.file") == null) {
			testFilePath = "queries/Q1.txt";
		}
		else
			testFilePath = System.getProperty("test.file");
		if(args.length != 0) {
			int i = 0;
			while(i < args.length) {
				if(args[i].equals("-cp")) {
					classPath = args[i+1];
					i += 2;
				} 
				else if(i == args.length - 1) {
					 //get the argument class to be processed
					argumentClass = args[i];
					i++;
					
				}
				else {
					i++;
				}
			}
		}
		 String[] mainArgs = { "-pp", 
	      		 "-cp" , classPath,
	      		 "-w",
	      		 "-app",
	      		"-p", "jb", "use-original-names:true",
	      		"-p", "cg.spark", "enabled:true,on-fly-cg:true,apponly:true",
	      		"-src-prec","java",
	      		 argumentClass
	    		  
	      };
		 return mainArgs;
	}
}

