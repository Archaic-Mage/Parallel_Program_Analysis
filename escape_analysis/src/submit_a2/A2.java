package submit_a2;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import dont_submit.EscapeQuery;
import soot.PackManager;
import soot.Transform;


public class A2 {
	static ArrayList<EscapeQuery> queryList;
	static String[] answers;
	static String testFilePath;
	public static void main(String args[]) throws IOException {

		String[] mainArgs = getOptions(args);

		populateQueries();

		PackManager.v().getPack("wjtp").add(new
				Transform("wjtp.eca",
				new EscapeAnalysis()));




		soot.Main.main(mainArgs);

		for(String ans: answers) {
			System.out.println(ans);
		}
	}


	static void populateQueries() throws IOException {

		queryList = new ArrayList<EscapeQuery>();
		BufferedReader bufRdr = new BufferedReader(new FileReader(testFilePath));
		String line = bufRdr.readLine();
		while(line != null) {
			String[] tokens = line.split(":");
			if(tokens.length != 2) {
				throw new IllegalArgumentException("Please check the query format");
			}
			String className = tokens[0];
			String methName = tokens[1];

			EscapeQuery aq = new EscapeQuery(className, methName);
			queryList.add(aq);
			line = bufRdr.readLine();
		}


		answers = new String[queryList.size()];
		bufRdr.close();

	}

	static String[] getOptions(String args[]) {
		String classPath = "inputs";
		String argumentClass = "P5";
		if(System.getProperty("test.file") == null) {
			testFilePath = "queries/Q5.txt";
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
				"-app","-x","jdk.*",
				"-p", "cg.cha", "enabled:true,apponly:true",
				"-p", "cg.spark", "enabled:false",
				argumentClass

		};
		return mainArgs;
	}
}
