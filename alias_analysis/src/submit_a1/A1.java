package submit_a1;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import dont_submit.AliasQuery;
import soot.PackManager;
import soot.Transform;


public class A1 {
	static ArrayList<AliasQuery> queryList;
	static String[] answers;
	static String testFilePath;
	public static void main(String args[]) throws IOException {
		
		String[] mainArgs = getOptions(args);

		populateQueries();
		
		PackManager.v().getPack("jtp").add(new
        		Transform("jtp.ala",
        		new AliasAnalysis()));
		
      
      
      
        soot.Main.main(mainArgs);

		for(String answer: answers) {
			System.out.println(answer);
		}
	}
	

	static void populateQueries() throws IOException {
		//String testFilePath = System.getProperty("test.file");
		queryList = new ArrayList<AliasQuery>();
			BufferedReader bufRdr = new BufferedReader(new FileReader(testFilePath));
			String line = bufRdr.readLine();
			while(line != null) {
				String[] tokens = line.split(":");
				if(tokens.length != 3) {
					throw new IllegalArgumentException("Please check the query format");
				}
				String className = tokens[0];
				String methName = tokens[1];
				String[] query = tokens[2].split(" ");
				
				AliasQuery aq = new AliasQuery(className, methName, query[0], query[2]);
				//System.out.println(aq);
				queryList.add(aq);	
				line = bufRdr.readLine();
			}
			
		
		answers = new String[queryList.size()];
		bufRdr.close();
		
	}
	
	static String[] getOptions(String args[]) {
		String classPath = "inputs";
		String argumentClass = "P9";
		if(System.getProperty("test.file") == null) {
			testFilePath = "queries/Q9.txt";
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
	      		 "-p", "jb", "use-original-names:true",
	      		 "-app",
	      		 "-src-prec","java",
	      		 argumentClass
	    		  
	      };
		 return mainArgs;
	}
}

