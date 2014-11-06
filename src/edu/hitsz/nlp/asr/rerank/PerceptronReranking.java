package edu.hitsz.nlp.asr.rerank;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.asr.eval.ASREval;
import edu.hitsz.nlp.mst.DependencyInstance;
import edu.hitsz.nlp.mst.io.DependencyReader;
import edu.hitsz.nlp.mst.io.MSTReader;
import edu.hitsz.nlp.pinyin2character.EvalOracle;
import edu.hitsz.nlp.pinyin2character.PyCharOptions;
import edu.hitsz.nlp.pinyin2character.PyCharParser;
import edu.hitsz.nlp.pinyin2character.PyCharPipe;
import edu.hitsz.nlp.segpos.FeatureVector;

public class PerceptronReranking extends PyCharParser{

	public PerceptronReranking(PyCharPipe pipe, PyCharOptions options) {
		super(pipe, options);
		// TODO Auto-generated constructor stub
	}

	   /** 
     * Averaged Perceptron reranking
     * @since Sep 2, 2012
     * @param goldFile 训练文件，为正确的分词好的文件
     * @param mertCandFile mert文件，包括所有候选词序列的子模型概率
     * @param dependencyFileName 依存文件，包括所有候选词序列的分词，词性标注，和句法结构
     * @param initialWeight 要选择初始概率，1表示N-gram概率，2表示所有子模型的概率和（linear reranking的输出）
     * @param featureSet 不同的特征集，包括词，词性，和依存等不同
     * @param rerankModelName
     * @throws IOException
     */
    public void trainAPReranker(String goldFile,
    		String mertCandFile, 
    		String dependencyFileName,
    		int initialWeight,    		
    		int featureSet,
    		String rerankModelName) throws IOException {

		rerankerParams = new Parameters();
		
		int numInstances = pipe.getSentenceNumber(goldFile);
		
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print("\nIteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print(" [");

		    long start = System.currentTimeMillis();

		    trainAPRerankerIter(goldFile, mertCandFile, dependencyFileName, initialWeight, featureSet, numInstances);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");		

			Parameters newParams = rerankerParams.copy();			
			rerankerParams.averagedAll(i, numInstances);			
			saveAPRerankerModel(rerankModelName+"-"+i);			
			rerankerParams = newParams;
		
		}
	}
    
    /** 
     * @param k
     * @param featureSet
     */
    public int trainAPRerankerIter(String goldFile,
    		String mertCandFile, 
    		String dependencyFileName,
    		int initialWeight,
    		int featureSet,
    		int numInstances) {
    	
    	try {    		
    		int candNumber = 500;           //候选格式
    		//正确文件
    		String goldFileEncoding = FileEncoding.getCharset(goldFile);
    		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), goldFileEncoding));
            String goldLine = null;
            //mert格式的候选文件
    		String candFileEncoding = FileEncoding.getCharset(mertCandFile);
    		BufferedReader candReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFile), candFileEncoding));
            String candLine = null;
    		//候选dep文件
    		MSTReader depReader = (MSTReader) DependencyReader.createDependencyReader("MST", false);
    		boolean labeled = depReader.startReading(dependencyFileName);
    		DependencyInstance instance = null;
    		//子模型的权重    		
    		double[] weights = ((AsrRerankOptions)options).mertWeights;
    		int weightLength = weights.length;
    		int sentenceNumber = 0;
    		
    		int mid = 500;
    		
    		//读取gold文件，每次读取一行		
    		while( (goldLine = goldReader.readLine()) != null ){
    			
    			goldLine = goldLine.trim();
    			int goldNumber = goldLine.length();
    			//对于每个句子
    			if(goldLine.length() > 0) { 
    				
    				//System.out.println(sentenceNumber);
    				if(sentenceNumber%100 == 0) {
    					System.out.print(sentenceNumber+"...");
    					if(sentenceNumber%1000 == 0) {
    						System.out.println();
    					}
    				}    				    				
    				int count = 0;
    				
    				//读取mert cand文件，
    				//获得所有候选词序列中最优词序列的准确性 和 索引index
    				//获得所有候选词序列的权重，可为n-gram的概率，或者linear reranking的概率
    				double worstErr = 1.1;  //添加的第一个权重
    				int bestIndex = 0;    	      //最优实例的索引   
        			int shortestNumber = goldNumber;
    				
    				double[] firstWeights = new double[candNumber]; 				
    				while(count < candNumber) {    					
    					candLine = candReader.readLine();
    					String[] subs = candLine.split("\\|\\|\\|");
    		    		if(subs.length != 3) {
    		    			System.out.println("sentence in file is wrong");
    		    			System.out.println(candLine);
    		    			System.exit(-1);
    		    		}    		    		
    		    		String result = subs[1].trim();
    		    		//子模型权重
    		    		String[] featureStr = subs[2].trim().split(" ");    		    		

    		    		//记录初始权重
    		    		double tmpWeight = 0.0;
    		    		if(initialWeight == 1)
    		    			tmpWeight = Double.parseDouble(featureStr[0]);// +Double.parseDouble(featureStr[1]);
    		    		else if(initialWeight == 2) {
	    		    		for(int i=0; i<weightLength; i++) 
	    		    			tmpWeight += Double.parseDouble(featureStr[i]) * weights[i];
    		    		}
    		    		firstWeights[count] = tmpWeight;
    		    		
    		    		ASREval eval = new ASREval();
    		    		ArrayList<String> goldChars = eval.getZis(goldLine);
    					ArrayList<String> resultChars = eval.getZis(result);
    					eval.updateEval(goldChars, resultChars);
    		    		double tmpErr = eval.getErr();    		    				
    		    		int tmpNumber = result.split("\\s+").length;
    		    		
    		    		//如果准确率相同
    		    		if(Math.abs(tmpErr-worstErr) < 1e-10) {
    		    			//句子中词的不同
    		    			if(tmpNumber < shortestNumber) {
    		    				worstErr = tmpErr;
        		    			bestIndex = count;
        		    			shortestNumber = tmpNumber;
    		    			}
    		    		}
    		    		//如果错误率减少
    		    		else if(tmpErr < worstErr) {
    		    			worstErr = tmpErr;
    		    			bestIndex = count;
    		    			shortestNumber = tmpNumber;
    		    		}
    		    		count++;
    				}
    				
    				firstWeights = this.balanceWeights(firstWeights, mid);
    				
    				//System.out.println(sentenceNumber+":"+index);
    				//读取dep文件
    				//记录最优准确性的实例bestIndex对应的bestInstance，和reranking模型给出的概率最大的实例predictInstance
    				count= 0;
    				int predictIndex = -1;
    				double tmpWeight = -1e20;
    				DependencyInstance bestInstance = new DependencyInstance();
    				DependencyInstance predictInstance = new DependencyInstance();
    				while(count < candNumber) {
    					instance = depReader.getNextBlank();
    					if(count == bestIndex)
    						bestInstance = instance;
    					FeatureVector fv = pipe.createFeatureVectorReranker(instance, false, featureSet); 
    					double secondWeight = fv.getScore(rerankerParams);
    					double allWeight = firstWeights[count] + secondWeight;
    					if(allWeight > tmpWeight) {
    						predictIndex = count;
    						tmpWeight = allWeight;
    						predictInstance = instance;
    					}
    					count++;
    				}    				
    				
    				//System.out.println("bestIndex: "+bestIndex+", predictIndex: " + predictIndex);
    				//if(sentenceNumber == 63)
    				//	System.out.println();
    				
    				FeatureVector bestFV = pipe.createFeatureVectorReranker(bestInstance, true, featureSet); 
    				FeatureVector predictFV = pipe.createFeatureVectorReranker(predictInstance, true, featureSet); 
    				rerankerParams.update(bestFV.fv, predictFV.fv, numInstances, sentenceNumber);
        			
        			sentenceNumber++;   
    			}    			
    		}   		
    		
    		goldReader.close();
    		candReader.close();
    		
    		return sentenceNumber;
    		
    	}
    	catch (IOException e) {
    		
    	}
    	
    	return 0;
    	
    }
    
    
    /**  */
    public int testAPReranker(String mertCandFile, 
    		String dependencyFileName,
    		int k,
    		int featureSet,
    		String outputFileName) {
    	
    	try {
    		
    		System.out.print("output: "+outputFileName+ " ");
    		int candNumber = 500;           //候选格式
            //mert格式的候选文件
    		String candFileEncoding = FileEncoding.getCharset(mertCandFile);
    		BufferedReader candReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFile), candFileEncoding));
            String candLine = null;
    		//候选dep文件
    		MSTReader depReader = (MSTReader) DependencyReader.createDependencyReader("MST", false);
    		boolean labeled = depReader.startReading(dependencyFileName);
    		DependencyInstance instance = null;
    		    		
    		FileWriter writer = new FileWriter(outputFileName);
    		
    		double[] weights = ((AsrRerankOptions)options).mertWeights;
    		int weightLength = weights.length;
    		int sentenceNumber = 0;
    		
    		double max = 18.72247177362442;
    		double min = 1.1996433772146702;
    		double mid = 10;
    		    		
    		boolean end = true;
    		//读取gold文件    		
    		while( end ){
    			    			
    			if(sentenceNumber % 10 == 0){
    				System.out.print(sentenceNumber+"...");
    				if(sentenceNumber % 100 == 0)
    					System.out.println();
    			}
    			
				int count = 0;
				
				//读取mert cand文件，读取第一项概率，选择最优结果的index 
				double[] firstWeights = new double[candNumber]; 
				String[] sentences = new String[candNumber];
				while(count < candNumber) {    					
					candLine = candReader.readLine();
					if(candLine == null) {
						end = false;
						break;
					}
					String[] subs = candLine.split("\\|\\|\\|");
		    		if(subs.length != 3) {
		    			System.out.println("sentence in file is wrong");
		    			System.out.println(candLine);
		    			System.exit(-1);
		    		}    		    		
		    		String result = subs[1].trim();
		    		sentences[count] = result;
		    		
		    		String[] featureStr = subs[2].trim().split(" ");	    		
		    		double tmpWeight = 0.0;
		    		if(k==1)
		    			tmpWeight = Double.parseDouble(featureStr[0]) + Double.parseDouble(featureStr[1]);
		    		else if(k==2) {
    		    		for(int i=0; i<weightLength; i++) 
    		    			tmpWeight += Double.parseDouble(featureStr[i]) * weights[i];
		    		}
		    		firstWeights[count] = tmpWeight;
		    		
		    		count++;
				}
				
				//firstWeights = balanceWeights(firstWeights, mid);
				
				//读取dep文件
				if(count != candNumber) {
					end = false;
					break;
				}
				count= 0;
				double tmpWeight = -1e20;
				String sentence = null;
								
				while(count < 500) {
					instance = depReader.getNextBlank();
					FeatureVector fv = pipe.createFeatureVectorReranker(instance, false, featureSet); 
					double secondWeight = fv.getScore(rerankerParams);
					double allWeight = firstWeights[count] + secondWeight;
					if(allWeight > tmpWeight) {
						tmpWeight = allWeight;
						sentence = sentences[count]; 
					}
					count++;
				}    				
				
				writer.write(sentence+"\n");
    			sentenceNumber++;   
			}    			
    						
    				
    		candReader.close();
    		writer.close();
    		System.out.println("done");
    		
    		return sentenceNumber;
    		
    	}
    	catch (IOException e) {
    		
    	}
    	
    	return 0;
    	
    }
    
    /** 根据length 来修改weights */
    public double[] balanceWeights(double[] weights, double length) {
    	    	
    	int weightSize = weights.length;
    	double tmpMax = -1e10;
    	double tmpMin = 1e10;
    	for(int i=0; i<weightSize; i++) {
    		if(weights[i] > tmpMax) tmpMax = weights[i];
    		if(weights[i] < tmpMin) tmpMin = weights[i];
    	}    		
    	
    	double weightLength = tmpMax - tmpMin;
    	
    	double[] newWeights = new double[weightSize];
    	for(int i=0; i<weightSize; i++) {
    		newWeights[i] = (weights[i] - tmpMin)/weightLength * length;
    	}
    	
    	return newWeights;    	
    }
    
    
    
    /** 统计discriminative model的概率情况 */
    public int statDisProb(String mertCandFile, 
    		String dependencyFileName,
    		int k,
    		int featureSet) {
    	
    	try {
    		
    		int candNumber = 500;           //候选格式
            //mert格式的候选文件
    		String candFileEncoding = FileEncoding.getCharset(mertCandFile);
    		BufferedReader candReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFile), candFileEncoding));
            String candLine = null;
    		//候选dep文件
    		MSTReader depReader = (MSTReader) DependencyReader.createDependencyReader("MST", false);
    		boolean labeled = depReader.startReading(dependencyFileName);
    		DependencyInstance instance = null;    		    		
    		
    		double[] weights = ((AsrRerankOptions)options).mertWeights;
    		int weightLength = weights.length;
    		int sentenceNumber = 0;
    		    		
    		boolean end = true;
    		double allfirstMax = -1e10;
    		double allfirstMin = 1e10;
    		double allsecondMax = -1e10;
    		double allsecondMin = 1e10;
    		double allMax = -1e10;
    		double allMin = 1e10;
    		double max = -1e10;
    		double min = 1e10;
    		//读取gold文件    		
    		while( end ){    			    			

        		double firstMax = -1e10;
        		double firstMin = 1e10;
        		double secondMax = -1e10;
        		double secondMin = 1e10;
    			
    			if(sentenceNumber % 10 == 0){
    				System.out.print(sentenceNumber+"...");
    				if(sentenceNumber % 100 == 0)
    					System.out.println();
    			}
    			
				int count = 0;
				
				//读取mert cand文件，读取第一项概率，选择最优结果的index 
				double[] firstWeights = new double[candNumber]; 
				String[] sentences = new String[candNumber];
				while(count < candNumber) {    					
					candLine = candReader.readLine();
					if(candLine == null) {
						end = false;
						break;
					}
					String[] subs = candLine.split("\\|\\|\\|");
		    		if(subs.length != 3) {
		    			System.out.println("sentence in file is wrong");
		    			System.out.println(candLine);
		    			System.exit(-1);
		    		}    		    		
		    		String result = subs[1].trim();
		    		sentences[count] = result;
		    		
		    		String[] featureStr = subs[2].trim().split(" ");	    		
		    		double tmpWeight = 0.0;
		    		if(k==1)
		    			tmpWeight = Double.parseDouble(featureStr[0]);// +Double.parseDouble(featureStr[1]);
		    		else if(k==2) {
    		    		for(int i=0; i<weightLength; i++) 
    		    			tmpWeight += Double.parseDouble(featureStr[i]) * weights[i];
		    		}
		    		firstWeights[count] = tmpWeight;
		    		
		    		if(tmpWeight > firstMax) firstMax = tmpWeight;
		    		if(tmpWeight < firstMin) firstMin = tmpWeight;
		    		if(firstMax == -1E10 || firstMin == 1E10)
		    			System.out.println();    		
		    		
		    		count++;
				}
				System.out.print("firstMax: " + firstMax + "\tfirstMin: " + firstMin);
	    		double length = firstMax - firstMin;
	    		if(length > max) max = length;
	    		if(length < min) min = length;		
	    		
				if(firstMax > allfirstMax) allfirstMax = firstMax;
				if(firstMin > allfirstMin) allfirstMin = firstMin;
				
				//读取dep文件
				if(count != candNumber) {
					end = false;
					break;
				}
				count= 0;
				double tmpWeight = -1e20;
				String sentence = null;
				
				double alpha = 1;
				double beta = 0.5;
				
				while(count < 500) {
					instance = depReader.getNextBlank();
					FeatureVector fv = pipe.createFeatureVectorReranker(instance, false, featureSet); 
					double secondWeight = fv.getScore(rerankerParams);
					if(secondWeight > secondMax) secondMax = secondWeight;
					if(secondWeight < secondMin) secondMin = secondWeight;
					
					double allWeight = alpha * firstWeights[count] + beta * secondWeight;
					if(allWeight > tmpWeight) {
						tmpWeight = allWeight;
						sentence = sentences[count]; 
					}
					
					if(allWeight > allMax) allMax = allWeight;
					if(allWeight < allMin) allMin = allWeight;
					
					count++;
				}    				
				
				System.out.println("\tsecondMax: " + secondMax + "\tsecondMin: " + secondMin);
				if(secondMax > allsecondMax) allsecondMax = secondMax;
				if(secondMin < allsecondMin) allsecondMin = secondMin;
				
    			sentenceNumber++;   

        		System.out.println("max: " + max + "\tmin: " + min);
			}    	
    		
    		System.out.println("allfirstMax: " + allfirstMax + "\tallfirstMin: " + allfirstMin);
    		System.out.println("allsecondMax: " + allsecondMax + "\tallsecondMin: " + allsecondMin);
    		
    		System.out.println("max: " + max + "\tmin: " + min);
    		
    		System.out.println("allMax: " + allMax + "\tallMin:" + allMin);
    				
    		candReader.close();
    		System.out.println("done");
    		
    		return sentenceNumber;
    		
    	}
    	catch (IOException e) {
    		
    	}
    	
    	return 0;
    	
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
	
    
    
    public static void main(String[] args) throws Exception {
    	
    	AsrRerankOptions options = new AsrRerankOptions(args);		
		PyCharPipe pipe = new PyCharPipe(options);
		PerceptronReranking parser = new PerceptronReranking(pipe, options);
		
		options.rerankerTrainFile = "/media/main/experiments/pinyin2character/Lcmc/mert/ref.train";
		options.mertCandTrainFile = "/media/main/experiments/pinyin2character/Lcmc/mert/cand_database_train.txt";
		options.mertDependencyTrainFile = "/media/main/experiments/pinyin2character/Lcmc/mert/cand_dependency_train";
		options.initWeight = 1;
		options.featureSet = 1;
		options.rerankerModelFile = "/home/tm/disk/disk1/asr-rerank/perceptron/Lcmc/lcmc.model";
		
		options.rerankTrain = false;
		if(options.rerankTrain) {
			parser.trainAPReranker(options.rerankerTrainFile, 
					options.mertCandTrainFile, 
					options.mertDependencyTrainFile, 
					options.initWeight, 
					options.featureSet, 
					options.rerankerModelFile + "-" + options.initWeight + "-" + options.featureSet);			
		}

		options.rerankerModelFile = "/home/tm/disk/disk1/asr-rerank/perceptron/PD/pd.model";		
		options.initWeight = 1;
		options.featureSet = 1;
		options.mertCandTestFile = "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.mert";
		options.mertDependencyTestFile = "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.dep";
		options.rerankTestOutputFile = "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.result";
		
		options.rerankDev = false;
		if(options.rerankDev) {			
			for(int i=0; i<2; i++) {//options.numIters
				parser.loadAPRerankerModel(options.rerankerModelFile+"-"+options.initWeight + "-" + options.featureSet+"-"+i);
				parser.testAPReranker(options.mertCandTestFile, 
						options.mertDependencyTestFile, 
						options.initWeight,
						options.featureSet,
						options.rerankTestOutputFile +"-"+options.initWeight + "-" + options.featureSet + "-"+i
						);
			}			
		}
		
		options.rerankTest = true;
		options.initWeight = 1;
		options.featureSet = 1;
		options.rerankerModelFile = "/home/tm/disk/disk1/asr-rerank/perceptron/Lcmc/lcmc.model-1-1-4";
		options.mertCandTestFile = "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.mert";
		options.mertDependencyTestFile = "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.dep";
		options.rerankTestOutputFile = "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.result-1-1-4";
		
		if(options.rerankTest) {				
			parser.loadAPRerankerModel(options.rerankerModelFile);
			parser.testAPReranker(options.mertCandTestFile, 
					options.mertDependencyTestFile, 
					options.initWeight,
					options.featureSet,
					options.rerankTestOutputFile);			
		}			
		
		if(false) {				
			parser.loadAPRerankerModel(options.rerankerModelFile);
			parser.statDisProb(options.mertCandTestFile, 
					options.mertDependencyTestFile, 
					options.initWeight,
					options.featureSet);			
		}	
    }
    
}
