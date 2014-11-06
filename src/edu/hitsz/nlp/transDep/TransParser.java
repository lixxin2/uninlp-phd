package edu.hitsz.nlp.transDep;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.mstjoint.DependencyEvaluatorJoint;
import edu.hitsz.nlp.mstjoint.DependencyInstanceJoint;

public class TransParser {
	
	private TransPipe pipe;
	private TransOptions options;

    
    public TransParser(TransOptions options) throws IOException {
    	this.options = options;
    	pipe = TransPipe.getPipe(options);
    }
	
    /** 
     * 训练
     * @since Feb 25, 2013
     * @param trainFileName
     * @param modelFileName
     * @throws Exception 
     */
    public void trainWord(String trainFileName, String modelFileName, int numIters) throws Exception {
		   	
    	pipe.getPosDep(trainFileName);
    	int[] iter = new int[1];
    	iter[0] = 0;
    	    	
		for(int i = 0; i < numIters; i++) {
			
			System.out.println("Iteration "+i);
			long start = System.currentTimeMillis();			
			trainWordIter(trainFileName, iter);				
			long end = System.currentTimeMillis();
		    System.out.println("Training iter took: " + (end-start));
		    System.out.println("Feature Number: " + pipe.dataMap.size());
		    
		    //for many iteration training
		    Parameters newParams = pipe.params.copy();	
		    pipe.params.updateAll(iter[0]);
		    pipe.params.averagedAll(iter[0]);							
			saveModel(modelFileName+"-"+i);
			
			//每个都评估
			if(options.dev) {
				outputBestParse(options.testFileName, options.resultFileName + "-" +i);
				if(options.eval) {
					System.out.println("\nEVALUATION PERFORMANCE:");
					DependencyEvaluatorJoint.evaluateJoint(options.goldFileName,
							 options.resultFileName + "-" +i,
							 "CONLL");
				}
			}
			pipe.params = newParams;
		}
	}
    
    
	public void trainWordIter(String trainFileName, int[] iter) throws IOException {
		
		pipe.initInputFile(trainFileName);		
		DependencyInstanceJoint instance = pipe.depReader.getNext();   
		int count = 0;
		System.out.print("sentences: ");
		while(instance != null) {
			System.out.print(count+".");
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count > 0 && count % 1000 == 0) {
					System.out.println();					
				}
			}
			if(count == 10) 
				System.out.println();
			count++;
			if(!options.isLabeled)
				instance.deprels = null;
			InputSequence goldSequence = InputSequence.getInputSequence(instance.forms, 
					instance.postags, instance.heads, null, 
					options.decodingMethod, true);
			
			pipe.decoding(goldSequence, options.K, true, iter);			
			instance = pipe.depReader.getNext();			
		}	
		System.out.println();
	}
	
	
	
	
	public void outputBestParse(String testFileName,
			String resultFileName) throws Exception {
		
		long start = System.currentTimeMillis();
		pipe.initInputFile(testFileName);
		pipe.initOutputFile(resultFileName);
	
		System.out.print("Parsing Sentence: ");
		DependencyInstanceJoint instance = pipe.depReader.getNext();
		int count = 0;
		while(instance != null) {
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count > 0 && count % 1000 == 0) {
					System.out.println();					
				}
			}
			//System.out.print(count+"...");
			if(options.isSegPos)
				instance.postags = null;
			InputSequence sequence = InputSequence.getInputSequence(instance.forms, 
					instance.postags, null, null, options.decodingMethod, true);
		    DependencyInstanceJoint newInstance = pipe.decoding(sequence, options.K, false, null);			
			pipe.outputInstance(newInstance);
		    instance = pipe.depReader.getNext();
			count++;
		}
		pipe.close();
	
		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end-start));
	}
	
	
    ///////////////////////////////////////////////////////
    // Saving and loading models
    ///////////////////////////////////////////////////////
	public void saveModel(String file) throws IOException {
		System.out.print("Saving model...");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(pipe.params);
		out.writeObject(pipe.postags);
		out.writeObject(pipe.deprels);
		out.writeObject(pipe.dataMap);
		out.close();
		System.out.println("done.");
	}

	
	/**
	 * 导入模型文件，用于测试
	 * @since Mar 30, 2012
	 * @param file
	 * @throws Exception
	 */
	public void loadModel(String file) throws Exception {
		System.out.print("Loading model..."+file);   
	    
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		pipe.params = (Parameters)in.readObject();
		pipe.postags = (ArrayList<String>) in.readObject();
		pipe.deprels = (ArrayList<String>) in.readObject();
		pipe.dataMap = (FeatureMap) in.readObject();
		in.close();
		System.out.println(" done.");
	}
	
	/**
	 * 检测动作解析步骤是否正确
	 * @since Feb 28, 2013
	 * @param trainFileName
	 * @throws IOException
	 */
	public void checkActionDecoder(String trainFileName) throws IOException {
		
		System.out.println("Simulating gold parsing: ");
		pipe.initInputFile(trainFileName);		
		DependencyInstanceJoint instance = pipe.depReader.getNext();   
		int count = 0;
		while(instance != null) {
			if(count % 100 == 0) {
				System.out.print(count+".");
				if(count !=0 && count % 1000 == 0) {
					System.out.println();					
				}
			}
			count++;
			int length = instance.length();
						
			InputSequence sequence = InputSequence.getInputSequence(instance.forms, 
					instance.postags, instance.heads, instance.deprels, 
					options.decodingMethod, true);
			int[] heads = instance.heads;	
			
			List<Pair<TransAction, DepState>> golds = pipe.simulateGold(sequence);
			DepState goldState = golds.get(golds.size()-1).second;
			int[] goldHeads = goldState.heads;			
			
			DepState finalState = pipe.moveSequentActions(sequence, goldState.actions, false);			
			int[] finalHeads = finalState.heads;
			
			for(int i=0; i<length; i++) {
				if(heads[i] != goldHeads[i] && heads[i] != finalHeads[i] ) {
					System.out.println("Action decoding algorithm is wrong: " + count);
					System.out.println(heads);
					System.out.println(goldHeads);
					System.out.println(finalHeads);
					System.exit(-1);
				}
			}
			instance = pipe.depReader.getNext();			
		}
		System.out.println("done");
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		TransOptions options = new TransOptions();
		TransParser parser = new TransParser(options);
		if(options.train) {
			parser.trainWord(options.trainFileName, options.modelFileName, options.numIters);	
		}
		if(!options.train && options.dev) {
			//每个都评估
			for(int i=0; i<options.numIters; i++) {
				parser.loadModel(options.modelFileName+"-"+i);
				parser.outputBestParse(options.testFileName, options.resultFileName + "-" +i);
				if(options.eval) {
					DependencyEvaluatorJoint eval = new DependencyEvaluatorJoint();
					eval.eval(options.goldFileName,
							 options.resultFileName + "-" +i,
							 "CONLL",
							 false);
				}
			}
		}
		if(!options.train && !options.dev && options.eval) {
			for(int i=0; i<options.numIters; i++) {			
				DependencyEvaluatorJoint eval = new DependencyEvaluatorJoint();
				eval.eval(options.goldFileName,
						 options.resultFileName + "-" +i,
						 "CONLL",
						 false);
			}
		}
		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
		if(options.actionCheck) {
			parser.checkActionDecoder(options.trainFileName);
		}
			
		
	}

}
