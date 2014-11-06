package edu.hitsz.nlp.mstjoint;


import java.io.*;

import edu.hitsz.nlp.mstjoint.io.*;

/**
 * 依存句法树评测器
 * @author tm
 *
 */
public class DependencyEvaluatorJoint {
	
	int wordGold = 0; 
	int wordCorr = 0;
	int wordPred = 0;
	
	int posGold = 0;
	int posCorr = 0;
	int posPred = 0;
	
	int sentGold = 0; 
	int sentCorr = 0; 
	
	//所有词，去掉标点（真正评测）
	int headNoPunctGold = 0; 
	int headNoPunctCorr = 0; 
	int headNoPunctPred = 0;
	//root
	int headRootGold = 0; 
	int headRootCorr = 0; 	
	
	
	/**
	 * 依存句法评测函数
	 * @param act_file
	 * @param pred_file
	 * @param format
	 * @throws IOException
	 */
    public void eval (String act_file,
				 String pred_file,
				 String format,
				 boolean isLabelled) throws IOException {

		DependencyReader goldReader = DependencyReader.createDependencyReader(format);
		boolean labeled = goldReader.startReading(act_file);

		DependencyReader predictedReader = DependencyReader.createDependencyReader(format);
		boolean predLabeled = predictedReader.startReading(pred_file);

		if (labeled != predLabeled)
		    System.out.println("Gold file and predicted file appear to differ on whether or not they are labeled. Expect problems!!!");

		DependencyInstanceJoint goldInstance = goldReader.getNext();
		DependencyInstanceJoint predInstance = predictedReader.getNext();

		int sentencenumber=0;
		
		while(goldInstance != null && predInstance != null) {

			System.out.println(sentencenumber+++1);
			
			boolean sentWhole = evalSent(goldInstance, predInstance, isLabelled);
			
			sentGold += 1;
		    if(sentWhole) sentCorr++;

		    goldInstance = goldReader.getNext();
		    predInstance = predictedReader.getNext();
		}

		System.out.println("Tokens: " + headNoPunctGold + ", Correct: " + headNoPunctCorr + ", Accuracy: " + ((double)headNoPunctCorr/headNoPunctGold));
		System.out.println("Root: " + headRootGold + ", Correct: " + headRootCorr + ", Accuracy" + ((double)headRootCorr/headRootGold));
		System.out.println("Sentence:" + sentGold + ", Correct: " + sentCorr + ", Accuracy: " + ((double)sentCorr/sentGold));
		
    }
    
    
    public boolean evalSent(DependencyInstanceJoint goldInstance, DependencyInstanceJoint predInstance, boolean isLabelled) {
    	
	    int instanceLength = goldInstance.length();
	    if (instanceLength != predInstance.length())
	    	System.out.println("Lengths do not match on sentence ");

	    String[] goldTags = goldInstance.postags;
	    int[] goldHeads = goldInstance.heads;
	    String[] goldLabels = goldInstance.deprels;
	    int[] predHeads = predInstance.heads;
	    String[] predLabels = predInstance.deprels;

	    boolean whole = true;

	    if(isLabelled)
	    	assert(goldLabels != null && predLabels != null);
	    
	    for (int i = 1; i < instanceLength; i++) {
	    	
	    	String goldHead = Integer.toString(goldHeads[i]);
	    	String predHead = Integer.toString(predHeads[i]);
	    	if(isLabelled) {
	    		goldHead += "$" + goldLabels[i];
	    		predHead += "$" + predLabels[i];
	    	}	    	
	    	
			if (!goldTags.equals("PU")) {
				headNoPunctGold += 1;
				if(goldHead.equals(predHead)) {
					headNoPunctCorr += 1;
				}	
				else
					whole = false;
			}
			if(goldHead.startsWith("0")) {
				headRootGold += 1;
				if(goldHead.equals(predHead))
					headRootCorr += 1;
			}
	    }
				
		return whole;	
    }
    
    
    
    public static void evaluateJoint (String act_file,
			 String pred_file,
			 String format) throws IOException {

		DependencyReader goldReader = DependencyReader.createDependencyReader(format);
		boolean labeled = goldReader.startReading(act_file);
	
		DependencyReader predictedReader = DependencyReader.createDependencyReader(format);
		boolean predLabeled = predictedReader.startReading(pred_file);
	
		if (labeled != predLabeled)
		    System.out.println("Gold file and predicted file appear to differ on whether or not they are labeled. Expect problems!!!");
	
		int goldNumber = 0; 
		int predNumber = 0;
		int goldNumberSent = 0;
		int predNumberSent = 0;
		//dep
		int corr = 0; 
		int corrsent = 0; 	
		//seg
		int corrSeg = 0;
		int corrsentSeg = 0;		
		//segpos
		int corrST = 0;
		int corrsentST = 0;
		
		DependencyInstanceJoint goldInstance = goldReader.getNext();
		DependencyInstanceJoint predInstance = predictedReader.getNext();
	
		int goldSentenceNumber = 0;
		int predSentenceNumber = 0;
		
		while(goldInstance != null || predInstance != null) {
			goldSentenceNumber++;
			predSentenceNumber ++;
			//System.out.println(goldSentenceNumber);
			//System.out.println(predSentenceNumber );
				
		    int goldLength = goldInstance.length();
		    int predLength = predInstance.length();
		    
		    String[] goldForms = goldInstance.forms;
		    String[] goldPoses = goldInstance.forms;
		    String[] predForms = predInstance.forms;
		    String[] predPoses = predInstance.forms;
		    
		    int[] goldHeads = goldInstance.heads;
		    int[] predHeads = predInstance.heads;
	
		    boolean whole = true;
		    boolean wholeSeg = true;
		    boolean wholeST = true;
	
		    // NOTE: the first item is the root info added during nextInstance(), so we skip it.
	
		    int i = 1;
		    int j = 1;
		    int goldSize = 0;
		    int predSize = 0;
		    while(i < goldLength && j < predLength) {
		    	if(goldForms[i].equals(predForms[j])) {
		    		corrSeg++;
		    		if(goldPoses[i].equals(predPoses[j])) {
		    			corrST++;
		    		}
		    		else
		    			wholeST = false;
		    		if((goldHeads[i] == 0 && predHeads[j] == 0) || 
		    				(goldHeads[i] != 0 && predHeads[j] != 0 
		    				&& goldForms[goldHeads[i]-1].equals(predForms[predHeads[j]-1]))) {
		    			corr++;
		    		}
		    		else
		    			whole = false;
		    	}
		    	else {
		    		wholeSeg = false;
		    		wholeST = false;
		    		whole = false;
		    	}
		    	
		    	
		    	if(goldSize == predSize) {
		    		goldSize += goldForms[i].length();
			    	predSize += predForms[j].length();
		    		i++; j++;
		    	}
		    	else if(goldSize > predSize) {
		    		predSize += predForms[j].length();
		    		j++;
		    	}
		    	else { 
		    		goldSize += goldForms[i].length();
		    		i++;
		    	}
		    }		    		
		    
		    	    
		    goldNumber += goldLength - 1; // Subtract one to not score fake root token
		    predNumber += predLength - 1;
		    		    
		    if(whole) corrsent++;
		    if(wholeSeg) corrsentSeg++;
		    if(wholeST) corrsentST++;
		    
		    goldNumberSent++;
		    predNumberSent++;
		    
		    goldInstance = goldReader.getNext();
		    predInstance = predictedReader.getNext();
		}

		System.out.println("\nEVALUATION PERFORMANCE:");
		if(goldInstance != null || predInstance != null) {
			System.out.println("gold file and pred file have different sentence numbers. gold:"+goldNumberSent+",pred:"+predNumberSent);
			System.exit(-1);
		}
		System.out.println("Sentence Number: " + goldSentenceNumber);
		System.out.println("Tokens: " + "gold," + goldNumber + " pred:" + predNumber);
		
		System.out.println("\nSegmentation:");
		System.out.println("Correct: " + corrSeg);
		double segPrecision = (double)corrSeg/goldNumber; double segRecall = (double)corrSeg/predNumber;
		double segFMeasure =  2 * segPrecision * segRecall/(segPrecision + segRecall);
		System.out.println("precison: " + segPrecision + ", recall: " + segRecall + ", F-1: " + segFMeasure);
		System.out.println("complete correct: " + (double)corrsentSeg/goldNumberSent);
		
		System.out.println("\nSegmentation and POS Tagging:");
		System.out.println("Correct: " + corrST);
		double stPrecision = (double)corrST/goldNumber; double stRecall = (double)corrST/predNumber;
		double stFMeasure =  2 * stPrecision * stRecall/(stPrecision + stRecall);
		System.out.println("precison: " + stPrecision + ", recall: " + stRecall + ", F-1: " + stFMeasure);
		System.out.println("complete correct: " + (double)corrsentST/goldNumberSent);
		
		System.out.println("\nCorrect: " + corr);
		double depPrecision = (double)corr/goldNumber; double depRecall = (double)corr/predNumber;
		double depFMeasure =  2 * depPrecision * depRecall/(depPrecision + depRecall);
		System.out.println("precison: " + depPrecision + ", recall: " + depRecall + ", F-1: " + depFMeasure);
		System.out.println("complete correct: " + ((double)corrsent/goldNumberSent));
	
	}

    public static void main (String[] args) throws IOException {
		String format = "CONLL";
		if (args.length > 2)
		    format = args[2];

		DependencyEvaluatorJoint eval = new DependencyEvaluatorJoint();
		eval.eval(args[0], args[1], format, false);
    }

}

