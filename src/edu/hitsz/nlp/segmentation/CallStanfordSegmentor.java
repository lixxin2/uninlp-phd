package edu.hitsz.nlp.segmentation;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class CallStanfordSegmentor {

	/**
	 *
	 * @param corpus ctb or pku
	 * @param inFileName
	 * @param encodingType UTF-8 GBK
	 * @param outFileName
	 * @param kbest
	 */
	public static void segment(String corpus, String inFileName, String encodingType, String outFileName, int kbest){
		String baseDir = "/home/tm/disk/d1/seg/StanfordSegmentor/";
		String args="-keepAllWhitespaces false";
		String dataDir = baseDir + "data/";
		String dicts = dataDir + "dict-chris6.ser.gz";
		String javaCmd = "java -mx2g -cp "+baseDir+"seg.jar edu.stanford.nlp.ie.crf.CRFClassifier " +
			"-sighanCorporaDict "+dataDir+" -testFile "+inFileName+" -inputEncoding "+encodingType+
			"-sighanPostProcessing true "+args;
		String segCmd = null;
		if(corpus.equals("ctb")){
			segCmd = javaCmd+" -loadClassifier "+dataDir+"ctb.gz -serDictionary "+dicts+" "+kbest;
		}
		else if (corpus.equals("pku")){
			segCmd = javaCmd+" -loadClassifier "+dataDir+"pku.gz -serDictionary "+dicts+" "+kbest;
		}
		else{
			System.out.println("no such corpus");
			System.exit(-1);
		}

		Process process;
		try{
		    System.out.println("exec: "+segCmd);
		    process = Runtime.getRuntime().exec(segCmd);
		    BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    String str = null;
		    FileWriter outWriter = new FileWriter(outFileName);
		    while ((str = read.readLine()) != null) {
		    	//outWriter.write(str+"\n");
		    	StringBuffer newBuffer = new StringBuffer();
		    	for(int i=0; i<str.length();i++){
		    		String singleChar = str.substring(i,i+1);
		    		if(!singleChar.equals(" ")){
		    			newBuffer.append(singleChar);
		    		}
		    		else{
		    			newBuffer.append("\n");
		    		}
		    	}
		    	outWriter.write(newBuffer+"\n\n");
		    }
		    outWriter.close();
		}
		catch(Exception e){
		    e.printStackTrace();
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CallStanfordSegmentor.segment("pku", "/home/tm/disk/d1/seg/StanfordSegmentor/test.simp.utf8",
				"UTF-8", "/home/tm/disk/d1/seg/StanfordSegmentor/output2",0);

	}

}
