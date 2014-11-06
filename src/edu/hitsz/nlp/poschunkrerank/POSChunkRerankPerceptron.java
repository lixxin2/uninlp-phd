package edu.hitsz.nlp.poschunkrerank;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.segpos.FeatureVector;
import edu.hitsz.nlp.sentence.Distance;
import edu.hitsz.nlp.sentence.LevenshteinDistance;

public class POSChunkRerankPerceptron {

	public Parameters rerankerParams;
	public POSChunkRerankPipe pipe;
	
	public POSChunkRerankPerceptron() {
		rerankerParams = new Parameters();
		pipe = new POSChunkRerankPipe();
	}
	
	
	/** 
     * Averaged Perceptron reranking
     * @since Sep 2, 2012
     * @param goldFileName 训练文件，为正确的文件
     * @param kbestFileName kbest文件
     * @param mertCandFile mert文件，包括所有候选序列的子模型概率
     * @param weights 要选择初始概率
     * @param featureSet 不同的特征集，包括词，词性，和依存等不同
     * @param rerankModelName
     * @throws IOException
     */
    public void trainAPReranker(String goldFileName,
    		String kbestFileName,
    		String mertCandFile, 
    		int sentenceNumber,
    		int candNumber,
    		double[] weights,    		
    		int featureSet,
    		int numIters,
    		String rerankModelName) throws IOException {

		rerankerParams = new Parameters();
		
		//int numInstances = pipe.getSentenceNumber(trainFile);
		
		int i = 0;
		for(i = 0; i < numIters; i++) {

		    System.out.print("\nIteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print(" [");

		    long start = System.currentTimeMillis();

		    trainAPRerankerIter(goldFileName, kbestFileName, mertCandFile, 
		    		sentenceNumber, candNumber, weights, featureSet);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");		

			Parameters newParams = rerankerParams.copy();			
			rerankerParams.averagedAll(i+1, sentenceNumber);			
			saveAPRerankerModel(rerankModelName+"-"+i);			
			rerankerParams = newParams;
		
		}
	}
    
    /** 
     * @param k
     * @param featureSet
     */
    public int trainAPRerankerIter(String goldFileName,
    		String kbestFileName,
    		String mertCandFile, 
    		int sentenceNumber,
    		int candNumber,
    		double[] weights,
    		int featureSet) {
    	
    	try {
    		//正确文件
    		BufferedReader goldFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName)));
            ArrayList<String[] > goldWords = null;
    		//候选文件
            BufferedReader nbestFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(kbestFileName)));
            ArrayList<String[] > candWords = null;
            //mert格式的候选文件
    		BufferedReader candReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFile)));
            String candLine = null;
            
    		//子模型的权重    		
    		int weightLength = weights.length;
    		
    		//读取gold文件    		
    		for(int i=0; i<sentenceNumber; i++) {    			
    			goldWords = getNextColumn(goldFileReader);
    			//对于每个句子
				if(i%100 == 0) {
					System.out.print(i+"...");
					if(i%1000 == 0) {
						System.out.println();
					}
				}    						
				
				//读取mert cand文件，
				//获得所有候选词序列中最优词序列的准确性 和 索引index
				//获得所有候选词序列的权重，可为n-gram的概率，或者linear reranking的概率
				double bestAccuracy = Double.NEGATIVE_INFINITY;  
				double predictAccuracy = Double.NEGATIVE_INFINITY;	
				ArrayList<String[] > bestWords = null;
				ArrayList<String[] > predictWords = null;				
				
				for(int j=0; j<candNumber; j++) {    	
					//
					candWords = getNextColumn(nbestFileReader);
					candLine = candReader.readLine();
					String[] subs = candLine.split("\\|\\|\\|");
		    		if(subs.length != 3) {
		    			System.out.println("sentence in file is wrong");
		    			System.out.println(candLine);
		    			System.exit(-1);
		    		}    	
		    		
		    		//子模型权重
		    		String[] featureStr = subs[2].trim().split(" ");     
		    		//记录初始权重
		    		double firstWeight = 0.0;
		    		for(int k=0; k<weightLength; k++) 
    		    		firstWeight += Double.parseDouble(featureStr[k]) * weights[k];
		    		//
		    		double tmpAccuracy = evalResult(goldWords, candWords);
		    		if(tmpAccuracy > bestAccuracy) {
		    			bestAccuracy = tmpAccuracy;
		    			bestWords = candWords;
		    		}
					//记录最优准确性的实例bestIndex对应的bestInstance，和reranking模型给出的概率最大的实例predictInstance
					FeatureVector fv = pipe.createFeatureVectorReranker(candWords, false, featureSet); 
					double secondWeight = fv.getScore(rerankerParams);
					double allWeight = firstWeight + secondWeight;
					if(allWeight > predictAccuracy) {
						predictAccuracy = allWeight;
						predictWords = candWords; 
					}				
					//System.out.println(sentenceNumber+":"+predictIndex);
				}
				FeatureVector bestFV = pipe.createFeatureVectorReranker(bestWords, true, featureSet); 
				FeatureVector predictFV = pipe.createFeatureVectorReranker(predictWords, true, featureSet); 
				rerankerParams.update(bestFV.fv, predictFV.fv, sentenceNumber, i);			
    		}   		
    		
			goldFileReader.close();
			nbestFileReader.close();
			candReader.close();  		
    	}
    	catch (IOException e) {
    		
    	}    	
    	return 0;    	
    }
    
    
    public void testAPReranker(String kbestFileName,
    		String mertCandFile, 
    		int sentenceNumber,
    		int candNumber,
    		double[] weights,
    		int featureSet,
    		String outputFileName) {
    	
    	try {    		
    		System.out.print("output: "+outputFileName+ " ");
    		//候选文件
            BufferedReader nbestFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(kbestFileName)));
            ArrayList<String[] > candWords = null;            
            //mert格式的候选文件
    		BufferedReader candReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFile)));
            String candLine = null;
            
    		int weightLength = weights.length;
    		    		
    		FileWriter writer = new FileWriter(outputFileName);
    				
    		for(int i=0; i<sentenceNumber; i++){ 
    			//System.out.print(i+"...");
    			if(i % 10 == 0){
    				System.out.print(i+"...");
    				if(i % 100 == 0)
    					System.out.println();
    			}
    			double bestAccuracy = Double.NEGATIVE_INFINITY; 
    			ArrayList<String[]> bestWords = new ArrayList<String[]> ();    			
				for(int j=0; j<candNumber; j++) {
					candWords = getNextColumn(nbestFileReader);
					candLine = candReader.readLine();
					String[] subs = candLine.split("\\|\\|\\|");
		    		if(subs.length != 3) {
		    			System.out.println("sentence in file is wrong");
		    			System.out.println(candLine);
		    			System.exit(-1);
		    		}    		    		
		    		String[] featureStr = subs[2].trim().split(" ");	    		
		    		double firstWeight = 0.0;
		    		for(int k=0; k<weightLength; k++) 
    		    			firstWeight += Double.parseDouble(featureStr[k]) * weights[k];
		    				    		
					FeatureVector fv = pipe.createFeatureVectorReranker(candWords, false, featureSet); 
					double secondWeight = fv.getScore(rerankerParams);
					double allWeight = firstWeight + secondWeight;
					if(allWeight > bestAccuracy) {
						bestAccuracy = allWeight;
						bestWords = candWords; 
					}
				}    			
				StringBuffer sbuf = new StringBuffer();
				String[] words = bestWords.get(0);
				String[] poses = bestWords.get(1);
				String[] chunks = bestWords.get(2);
				for(int j=0; j<words.length; j++) {
					sbuf.append(words[j]); sbuf.append("\t");
					sbuf.append(poses[j]); sbuf.append("\t");
					sbuf.append(chunks[j]); sbuf.append("\n");
				}
				writer.write(sbuf.toString()+"\n");
			}    			    						
    		nbestFileReader.close();		
    		candReader.close();
    		writer.close();
    		System.out.println("done");
    		
    	}
    	catch (IOException e) {
    		
    	}    	
    }
    
    
 
	public void saveAPRerankerModel(String file) throws IOException {
		
    	System.out.print("Saving model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    	out.writeObject(rerankerParams);
    	out.writeObject(pipe.dataMap);
    	out.close();
    	System.out.println(" done.");

    }
    
	
    public void loadAPRerankerModel(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    	rerankerParams = (Parameters) in.readObject();
    	pipe.dataMap = (FeatureMap) in.readObject();
    	in.close();
    	System.out.println("done");
    }
    
	private ArrayList<String []> getNextColumn(BufferedReader inputReader) throws IOException {

		ArrayList<String[]> lineList = new ArrayList<String[]>();
		String line = inputReader.readLine();
		while (line != null && !line.trim().equals("")) {
			String[] subs = line.trim().split("\\s+");
		    line = inputReader.readLine();
			if(subs.length < 1) {
				continue;
			}
		    lineList.add(subs);
		    //System.out.println("## "+line);
		}
		int wordSize = lineList.size();
		if(wordSize == 0) {
		    inputReader.close();
		    return null;
		}
		//分配词和词性
		String[] words = new String[wordSize];
		String[] poses = new String[wordSize];
		String[] chunks = new String[wordSize];
		for(int i = 0; i < wordSize; i++) {
		    String[] info = lineList.get(i);
		    words[i] = info[0];
		    if(info.length > 1) {
		    	poses[i] = info[1];
		    	if(info.length > 2)
		    		chunks[i] = info[2];
		    }
		}
		ArrayList<String []> allWords = new ArrayList<String []>();
		allWords.add(words); allWords.add(poses); allWords.add(chunks);
		
		return allWords;
    }
	
	
	public double evalResult(ArrayList<String[]> goldWords, ArrayList<String[]> predictWords) {
		
		double prob = 0.33;
		
		String[] goldPoses = goldWords.get(1);
		String[] goldChunks = goldWords.get(2);
		String[] predictPoses = predictWords.get(1);
		String[] predictChunks = predictWords.get(2);
		
		double[] stats = new double[2];
		
		int length = goldPoses.length;
        int corr = 0;
        for(int j=0; j<length; j++) {
        	if(goldPoses[j].equals(predictPoses[j]))
        		corr++;
        }
        stats[0] = prob * corr;
        stats[1] = prob * length;	 
	    		    	
	    ArrayList<String> goldChunkTags = getBIOTags(goldChunks);
	    ArrayList<String> predictChunkTags = getBIOTags(predictChunks);
	    length = goldChunkTags.size();       
	    LevenshteinDistance dist = new LevenshteinDistance();
	    Distance newDist = dist.distance(goldChunkTags, predictChunkTags);	        
        stats[0] += (1-prob) * Math.max(length - (newDist.deletion + newDist.insertion + newDist.substitution), 0);
        stats[1] += (1-prob) * length;	
	    
        return stats[0]/(double)stats[1];
	}

	private ArrayList<String> getBIOTags(String[] tags) {
		ArrayList<String> BIOTags = new ArrayList<String>();
		int length = tags.length;
		for(int i=0; i<length; i++) {
			String tag = tags[i];
			if(tag.startsWith("O"))
				//BIOTag = tag + "-" + i + "-" + i;
				continue;
			else if(tag.startsWith("B")) {
				String BIOTag = tag.substring(2) + "-" + i;
				int j = i+1;
				for(; j<length; j++) {
					tag = tags[j];
					if(tag.startsWith("O") || tag.startsWith("B"))
						break;
				}
				i = j - 1;
				BIOTag += "-" + i;
				BIOTags.add(BIOTag);
			}			
		}		
		return BIOTags;
	}
	
	
	public static void main(String[] args) throws Exception {
		int candNumber = 500;
		//double[] weights = {0.5515203410322929, 0.12487968421343158, 3.5242006198382176, -0.658863852949126, 10.905384417981839, 6.459495074838543, 5.834626890585073, -1.5851103080805429, -0.3111953525066167, -3.434201255444795};   		
		double[] weights = {1.0, 0.0, 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0 };   		
		
		int featureSet = 2;
		int numIters = 10;
		
		String dir = "/home/tm/disk/disk1/pos-chunk-rerank/8.perceptron/";
		String rerankModelName = dir + "15-18.perceptronmodel.pos.f2";
		/*
		String trainGoldFileName = dir + "15-18wpc";
		String trainKbestFileName = dir + "15-18.500best";
		String trainMertCandFile = dir + "15-18.mert";
		int trainSentenceNumber = 8936;// 2012
		POSChunkRerankPerceptron perceptron = new POSChunkRerankPerceptron();
		perceptron.trainAPReranker(trainGoldFileName, trainKbestFileName, trainMertCandFile, 
	    		trainSentenceNumber, candNumber, weights,  featureSet, numIters,rerankModelName);
		*/
		String testKbestFileName = dir + "20.500best";
		String testMertCandFile = dir + "20.mert";
		int testSentenceNumber = 2012;
		String outputFileName = dir + "20.percetron.result.pos.f2";
		for(int i=0; i<numIters; i++) {
			POSChunkRerankPerceptron perceptron2 = new POSChunkRerankPerceptron();
			perceptron2.loadAPRerankerModel(rerankModelName +"-" + i);
			perceptron2.testAPReranker(testKbestFileName, testMertCandFile, 
		    		testSentenceNumber, candNumber, weights, featureSet, outputFileName+"-"+i);
		}
	    
	}
	

}
