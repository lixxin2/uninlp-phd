package edu.hitsz.nlp.pinyin2character;


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
import edu.hitsz.nlp.mst.DependencyInstance;
import edu.hitsz.nlp.mst.DependencyParser;
import edu.hitsz.nlp.mst.DependencyPipe;
import edu.hitsz.nlp.mst.DependencyPipe2O;
import edu.hitsz.nlp.mst.ParserOptions;
import edu.hitsz.nlp.mst.io.DependencyReader;
import edu.hitsz.nlp.mst.io.MSTReader;
import edu.hitsz.nlp.segpos.FeatureVector;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.segpos.Parser;
import edu.hitsz.nlp.segpos.WordPipe;


/**
 * 拼音转汉字程序
 * @author Xinxin Li
 * @since Oct 23, 2012
 */
public class PyCharParser {
	
	public Parameters baseParams;
	public PyCharDecoder decoder;
	public PyCharPipe pipe;
	public PyCharOptions options;
	public Parser posParser;
	public DependencyParser dependencyParser;
	
	public Parameters rerankerParams;
	
	
	public PyCharParser(PyCharPipe pipe, PyCharOptions options) {
		baseParams = new Parameters();
		decoder = new PyCharDecoder(pipe);
		this.pipe = pipe;
		this.options = options;
	}
	

    
    
    /** 按照ngram方法解析句子*/
    public void outputBestNgram(String testfile, String outputfile) throws IOException{
    	
    	long start = System.currentTimeMillis();
		
    	pipe.initInputFile(testfile);
    	pipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	PyCharInstance inst = pipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    if( cnt%100 == 0) {
    	    	System.out.print(cnt+" ");
    	    	if(cnt%1000 == 0)
    	    		System.out.println();
    	    }
    	        
		    PyCharInstance newInst = decoder.getNgramBest(inst, options.gramK);   	    
    	    
		    pipe.outputInstanceWords(newInst);
		    
    	    inst = pipe.nextRawInstance();     	    
    	    
    	}
    	
    	pipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
    
    
    
	/** 训练基于 字 的模型 */
	public void trainChar(String trainFile, String modelFile, int numIters) throws IOException {
		
		int numInstances = pipe.getSentenceSize(trainFile);
		int i = 0;
		for(i = 0; i < numIters; i++) {

		    System.out.print("\nIteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print(" [");

		    long start = System.currentTimeMillis();

		    trainingCharIter(trainFile, numInstances, i+1);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");		

			Parameters newParams = baseParams.copy();			
			baseParams.averagedAll(i, numInstances);			
			saveCharModel(modelFile+"-"+i);			
			baseParams = newParams;
			
			if(options.charDev) 
				outputBestChar(options.testFile, options.outputFile + "-" + i);
		
		}
	}
	
	
	private void trainingCharIter(String trainfile, int numInstances, int iter) throws IOException {

		PyCharReader reader = new PyCharReader(trainfile);
		PyCharInstance instance = reader.getNext();
		
		int sentenceNumber = 0;
		
		while(instance != null) {			

			if(sentenceNumber%100 == 0) {
				System.out.print(sentenceNumber + "...");
				if(sentenceNumber%1000 == 0) 
					System.out.println();
			}
		
			//int length = instance.length;
			
			PyCharInstance bestInstance = decoder.getBestBase(baseParams, instance, options.charK);
			
			FeatureVector bestFV = pipe.createFeatureVector(bestInstance);
			FeatureVector fv = pipe.createFeatureVector(instance);
			
			baseParams.update(fv.fv, bestFV.fv, numInstances, sentenceNumber);
			
			instance = reader.getNext();

			sentenceNumber++;
		}	
	
		System.out.println(numInstances);
	
	}
	
	
	public void saveCharModel(String file) throws IOException {
		
    	System.out.print("Saving model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    	out.writeObject(baseParams);
    	out.writeObject(pipe.pwPair);
    	out.writeObject(pipe.dataMap);
    	out.close();
    	System.out.println(" done.");

    }

    public void loadCharModel(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    	baseParams = (Parameters) in.readObject();
    	pipe.pwPair = (PyWordPair) in.readObject();
    	pipe.pwPair.pairs.clear();
    	pipe.dataMap = (FeatureMap) in.readObject();
    	in.close();
    	System.out.println("done");
    }
	
    
    public void outputBestChar(String testfile, String outputfile) throws IOException{
    	
    	long start = System.currentTimeMillis();

    	pipe.initInputFile(testfile);
    	pipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	PyCharInstance inst = pipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    if( cnt%100 == 0) {
    	    	System.out.print(cnt+" ");
    	    	if(cnt%1000 == 0)
    	    		System.out.println();
    	    }
    	    	    
		    PyCharInstance newInst = decoder.getBestBase(baseParams, inst, options.charK);   	    
    	    
		    pipe.outputInstanceWords(newInst);
		    
    	    inst = pipe.nextRawInstance();     	    
    	    
    	}
    	
    	pipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
    
    
    
    
    
    
    
    
    
    
    
    /**
     * 输出最优的K个结果，用于Mert学习，mert采用Zmert工具
     * 
     * @since Nov 6, 2012
     * @param testfile
     * @param outputWeightFile
     * @param outputInstanceFile
     * @throws IOException
     */
    public void outputMertNbest(String testfile, String outputWeightFile, 
    		String outputInstanceFile) throws IOException{
    	
    	long start = System.currentTimeMillis();
		
    	pipe.initInputFile(testfile);

    	System.out.print("Processing Sentence: ");
    	PyCharInstance inst = pipe.nextRawInstance();
    	int cnt = 0;
    	
    	FileWriter weightWriter = new FileWriter(outputWeightFile);
    	FileWriter instanceWriter = new FileWriter(outputInstanceFile);
    	
    	try {
	    	while(inst != null) {
	    	    int mertSeq = cnt;
	    		cnt++;
	    		System.out.print(cnt + "...");
	    	    if( cnt%10 == 0) {
	    	    	System.out.println();
	    	    }
	    	        		    
			    Object[] obj = decoder.getNgramKbest(inst, options.gramK);   	    
	    	    
			    ArrayList<PyCharInstance> instances = (ArrayList<PyCharInstance>) obj[0];
			    ArrayList<Double> ngramWeights = (ArrayList<Double>) obj[1]; // word n-gram
			    pipe.renewInstances(instances, options.mertK);
			    pipe.renewWeights(ngramWeights, options.mertK); 
			    
			    ArrayList<Double> charNGramWeight = pipe.getCharNGramWeights(instances);
			    ArrayList<Double> charModelWeights = pipe.getCharModelWeights(baseParams, instances);		    
			    ArrayList<Double> pinyinWordCoWeights = pipe.getPinyinWordCoWeights(instances);
			    
			    ArrayList<Object[]> POSModelObjs = getWordPOSInstances(posParser, instances);
			    ArrayList<Instance> POSModelInstances = new ArrayList<Instance>();
			    ArrayList<Double> POSModelWeights = new ArrayList<Double>();
			    for(Object[] POSModelObj : POSModelObjs) {
			    	POSModelInstances.add((Instance) POSModelObj[0]);
			    	POSModelWeights.add((Double) POSModelObj[1]);
			    }
			   
			    ArrayList<Double> POSWordCoWeights = pipe.getPOSWordCoWeights(POSModelInstances);
			    ArrayList<Double> wordPOSCoWeights = pipe.getWordPOSCoWeights(POSModelInstances);
			    ArrayList<Double> POSNGramWeights = pipe.getPOSNGramWeights(POSModelInstances);
			    
			    
			    Object[] depObj = getDependencyInstances(POSModelInstances);
			    ArrayList<DependencyInstance> depInstances = (ArrayList<DependencyInstance>) depObj[0];
			    ArrayList<Double> depWeights = (ArrayList<Double>) depObj[1];
			    
			    ArrayList<ArrayList<Double>> allWeights = new ArrayList<ArrayList<Double>>();
			    allWeights.add(ngramWeights); allWeights.add(charNGramWeight); 
			    allWeights.add(charModelWeights);
			    allWeights.add(pinyinWordCoWeights); 
			    allWeights.add(POSModelWeights); allWeights.add(POSWordCoWeights);
			    allWeights.add(wordPOSCoWeights); allWeights.add(POSNGramWeights); 
			    allWeights.add(depWeights);
			    
			    
			    outputInstancesWeights(weightWriter, cnt-1, instances, allWeights, options.mertK);
			    outputDependencyInstances(instanceWriter, depInstances, options.mertK);
			    
	    	    inst = pipe.nextRawInstance();     	    
	    	    
	    	}
	    	
	    	weightWriter.close();
	    	instanceWriter.close();
	    	
    	}
    	catch (IOException e) {    		
    	}    	
    	//pipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
       
    
    /**
     * 写入mert 权重文件 
     * @since Dec 7, 2012
     * @param writer
     * @param count
     * @param instances
     * @param allWeights
     * @param K
     * @throws IOException
     */
    public void outputInstancesWeights(FileWriter writer, int count, 
    		ArrayList<PyCharInstance> instances,
    		ArrayList<ArrayList<Double>> allWeights, int K) throws IOException {
    	
    	int weightNumber = allWeights.size();
		StringBuffer sbuf = new StringBuffer();
		
    	for(int i=0; i<K; i++) {
    		sbuf.append(count);sbuf.append(" ||| ");
    		String[] words = instances.get(i).words;
    		for(String word : words)
    			sbuf.append(word + " ");
    		sbuf.append("|||");
    		for(int j=0; j<weightNumber; j++)
    			sbuf.append(" " + allWeights.get(j).get(i));
    		sbuf.append("\n");    		
    	}
    	
    	writer.write(sbuf.toString());  	
    	
    }
    
    /** 写入依存结果 */
    public void outputDependencyInstances(FileWriter writer, 
    		ArrayList<DependencyInstance> depInstances, int K) throws IOException {
    	
    	StringBuffer sbuf = new StringBuffer();
    	for(int i=0; i<K; i++) {
    		DependencyInstance depInstance = depInstances.get(i);
    		String[] words = depInstance.forms;
    		String[] tags = depInstance.postags;
    		int[] heads = depInstance.heads;
    		for(String word : words)
    			sbuf.append(word + " ");
    		sbuf.append("\n");
    		for(String tag : tags)
    			sbuf.append(tag + " ");
    		sbuf.append("\n");
    		for(int head : heads)
    			sbuf.append(head + " ");
    		sbuf.append("\n");
    		sbuf.append("\n");    		
    	}    	
    	writer.write(sbuf.toString());    	
    	
    }
    
    
    /** 获得 词序列对应的词性序列 和 对应的概率 */
    public ArrayList<Object[]> getWordPOSInstances(Parser parser, ArrayList<PyCharInstance> instances) {
    	
    	ArrayList<Object[]> segposInstances = new ArrayList<Object[]>();
    	
    	for(PyCharInstance instance : instances) {
    		
    		Instance segposInstance = new Instance(instance.words, null);
    		Object[] obj = parser.getBestPOS(segposInstance);
    		segposInstances.add(obj);    		
    	}    	
    	
    	return segposInstances;
    	
    }
    
    /** 句法解析，得到依存实例 和 对应的概率 */
    public Object[] getDependencyInstances(ArrayList<Instance> instances) {
    	
    	ArrayList<DependencyInstance> depInstances = new ArrayList<DependencyInstance>();
    	ArrayList<Double> depWeights = new ArrayList<Double>();
    	
    	for(Instance instance : instances) {    		
    		DependencyInstance depInstance = new DependencyInstance(instance.words, instance.tags);
    		Object[] obj= dependencyParser.getBest(depInstance);
    		DependencyInstance newDepInstance = (DependencyInstance) obj[0]; 
    		double depWeight = (Double) obj[1];
    		depInstances.add(newDepInstance);   
    		depWeights.add(depWeight);
    	}    	
    	
    	Object[] obj = new Object[2];
    	obj[0] = depInstances;
    	obj[1] = depWeights;
    	return obj;
    	
    }
    
    
    
    
    
    /**
     * 从Mert已经产生的candidate file找到最优的结果，属于离线得到结果
     * <p> 这是我们已经确定了mert(linear reranking)模型的权重
     * @since Dec 18, 2012
     * @param testfile
     * @param outputfile
     * @throws IOException
     */
    public void outputMertBestOffline(String mertCandFile, String outputFile, double[] weights) throws IOException{
    	
    	long start = System.currentTimeMillis();
		String fileEncoding = FileEncoding.getCharset(mertCandFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFile), fileEncoding));
        
    	FileWriter writer = new FileWriter(outputFile);
    	
    	System.out.print("Processing Sentence: ");
    	int cnt = 0;
    	String line = null;
    	
    	String bestParse = "";         //最优的解析结果
    	double bestWeight = -1e20;     //最优的权重   	
    	int sentenceSeq = -1;          //第几个句子
    	int curSent = 0;
    	int bestSent = 0;              //每个句子最优候选项的位置 
    	
    	int weightSize = weights.length;
    	//0 ||| 万 鸟 的 ， ||| -18.97762495279312 -19.464714765548706 23.733055 -10.422477596392675 149.1898659596406 -28.97973908207265 -20.507673768583427 -41.096518993377686 -4.75718778595361
    	while( (line = reader.readLine()) != null ) {
    		
    		String[] subs = line.split("\\|\\|\\|");
    		if(subs.length != 3) {
    			System.out.println("sentence in file is wrong");
    			System.out.println(line);
    			System.exit(-1);
    		}
    		//分别为句子序号，句子结果，句子特征
    		int sentenceNumber = Integer.parseInt(subs[0].trim());
    		
    		//如果是新句子，则把上个结果的最优结果输出
    		if(sentenceNumber != sentenceSeq) {
    			if(sentenceSeq != -1) {
    				System.out.println(sentenceSeq + " " + bestSent);
	    			writer.write(bestParse+"\n");
	    			bestParse = "";
	    			bestWeight = -1e20;
	    			//cnt++;
	    			//if(cnt % 10 == 0) {
	    			//	System.out.print(cnt + "...");
	    			//	if(cnt != 10 && cnt % 100 == 0)
	    			//		System.out.println();
	    			//}
    			}
    			sentenceSeq = sentenceNumber;
    			curSent=0;
    		}
    		String result = subs[1].trim();
    		String[] featureStr = subs[2].trim().split(" ");
    		if(weightSize != featureStr.length) {
    			System.out.println("weight number is different");
    			System.exit(-1);
    		}
    		double[] features = new double[weightSize];
    		double tmpWeight = 0.0;
    		for(int i=0; i<weightSize; i++) {
    			features[i] = Double.parseDouble(featureStr[i]);
    			tmpWeight += features[i] * weights[i];
    		}
    		if(tmpWeight > bestWeight) {
    			bestWeight = tmpWeight;
    			bestParse = result;
    			bestSent = curSent;
    		}  
    		curSent++;
    	}
    	//最后一个句子
    	writer.write(bestParse+"\n");
		System.out.println(sentenceSeq + " " + bestSent);

		reader.close();
    	writer.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
    
    
    
    
    /**
     * 在线解析，速度太慢，一般不用，可能只用于测试可行性
     * 
     * <p> 从最后的k个最好的结果中，选择出最优的序列
     * <p> <@link #o
     * @since Nov 12, 2012
     * @param testfile
     * @param outputfile
     * @throws IOException
     */
    public void outputMertBestOnline(String testfile, String outputfile) throws IOException{
    	
    	long start = System.currentTimeMillis();
		
    	pipe.initInputFile(testfile);
    	pipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	PyCharInstance inst = pipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    if( cnt%100 == 0) {
    	    	System.out.print(cnt+" ");
    	    	if(cnt%1000 == 0)
    	    		System.out.println();
    	    }		    
		    Object[] obj = decoder.getNgramKbest(inst, options.gramK);   	    
    	    
		    ArrayList<PyCharInstance> instances = (ArrayList<PyCharInstance>) obj[0];
		    ArrayList<Double> ngramWeights = (ArrayList<Double>) obj[1];
		    
		    pipe.outputInstancesMertBest(baseParams, instances, ngramWeights);		    
    	    inst = pipe.nextRawInstance();    	    
    	}
    	
    	pipe.close();
    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));
	}
    
  
    
    
    /** 载入pos Model 
     * @throws Exception */
    public void loadPOSModel(String posModelFile) throws Exception {
    	Options posModelOptions = new Options();
    	posModelOptions.modelType = "pos";
    	posModelOptions.K = 16;
    	WordPipe pipe = new WordPipe(posModelOptions);
		posParser = new Parser(pipe, posModelOptions);
		posParser.loadWordModel(posModelFile);		
    }
    
    
    public void loadDependencyModel(String dependencyModelFile) throws Exception {
    	System.out.print("load Dependency model: " + dependencyModelFile + "... ");
    	ParserOptions dependencyModelOptions = new ParserOptions();
    	dependencyModelOptions.secondOrder = true;
    	dependencyModelOptions.decodeType = "proj";
    	DependencyPipe dependencyPipe = dependencyModelOptions.secondOrder ?
    			new DependencyPipe2O (dependencyModelOptions) : new DependencyPipe (dependencyModelOptions);
		dependencyParser = new DependencyParser(dependencyPipe, dependencyModelOptions);
		dependencyParser.loadModel(dependencyModelFile);	
		System.out.println("done");
    }
    
    
    /** 
     * Averaged Perceptron reranking
     * @since Sep 2, 2012
     * @param trainFile 训练文件，为正确的分词好的文件
     * @param mertCandFile mert文件，包括所有候选词序列的子模型概率
     * @param dependencyFileName 依存文件，包括所有候选词序列的分词，词性标注，和句法结构
     * @param initialWeight 要选择初始概率，1表示N-gram概率，2表示所有子模型的概率和（linear reranking的输出）
     * @param featureSet 不同的特征集，包括词，词性，和依存等不同
     * @param rerankModelName
     * @throws IOException
     */
    public void trainAPReranker(String trainFile,
    		String mertCandFile, 
    		String dependencyFileName,
    		int initialWeight,    		
    		int featureSet,
    		String rerankModelName) throws IOException {

		rerankerParams = new Parameters();
		
		int numInstances = pipe.getSentenceNumber(trainFile);
		
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print("\nIteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print(" [");

		    long start = System.currentTimeMillis();

		    trainAPRerankerIter(trainFile, mertCandFile, dependencyFileName, initialWeight, featureSet, numInstances);

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
    public int trainAPRerankerIter(String trainFile,
    		String mertCandFile, 
    		String dependencyFileName,
    		int initialWeight,
    		int featureSet,
    		int numInstances) {
    	
    	try {
    		
    		int candNumber = 500;           //候选格式
    		//正确文件
    		String goldFileEncoding = FileEncoding.getCharset(trainFile);
    		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(trainFile), goldFileEncoding));
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
    		double[] weights = options.mertWeights;
    		int weightLength = weights.length;
    		int sentenceNumber = 0;
    		
    		//读取gold文件    		
    		while( (goldLine = goldReader.readLine()) != null ){
    			
    			goldLine = goldLine.trim();
    			int goldWordNumber = goldLine.split("\\s+").length;
    			//对于每个句子
    			if(goldLine.length() > 0) { 
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
    				double bestAccuracy = Double.MIN_VALUE;  //添加的第一个权重
    				int bestIndex = 0;    	      //最优实例的索引   
    				int bestWordNumberDiff = 200;       //正确词序列的数目 和 预测词序列的数目 的 不同
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
    		    		int predictWordNumber = result.split("\\s+").length;
    		    		//子模型权重
    		    		String[] featureStr = subs[2].trim().split(" ");    		    		

    		    		//记录初始权重
    		    		double tmpWeight = 0.0;
    		    		if(initialWeight==1)
    		    			tmpWeight = Double.parseDouble(featureStr[0]);
    		    		else if(initialWeight==2) {
	    		    		for(int i=0; i<weightLength; i++) 
	    		    			tmpWeight += Double.parseDouble(featureStr[i]) * weights[i];
    		    		}
    		    		firstWeights[count] = tmpWeight;
    		    		
    		    		double tmpAccuracy = EvalOracle.evalSentence(goldLine, result);
		    			int wordNumberDiff = Math.abs(goldWordNumber - predictWordNumber);
    		    		//如果准确率相同
    		    		if(Math.abs(tmpAccuracy-bestAccuracy) < 1e-10) {
    		    			//句子中词的不同
    		    			if(wordNumberDiff < bestWordNumberDiff) {
    		    				bestWordNumberDiff = wordNumberDiff;
        		    			bestAccuracy = tmpAccuracy;
        		    			bestIndex = count;
    		    			}
    		    		}
    		    		//如果准确率增大
    		    		else if(tmpAccuracy > bestAccuracy) {
    		    			bestAccuracy = tmpAccuracy;
    		    			bestIndex = count;
    		    			bestWordNumberDiff = wordNumberDiff;
    		    		}
    		    		count++;
    				}
    				
    				//System.out.println(sentenceNumber+":"+index);
    				//读取dep文件
    				//记录最优准确性的实例bestIndex对应的bestInstance，和reranking模型给出的概率最大的实例predictInstance
    				count= 0;
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
    						tmpWeight = allWeight;
    						predictInstance = instance; 
    					}
    					count++;
    				}    				
    				//System.out.println(sentenceNumber+":"+predictIndex);
    				
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
    		
    		double[] weights = options.mertWeights;
    		int weightLength = weights.length;
    		int sentenceNumber = 0;
    		    		
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
		    			tmpWeight = Double.parseDouble(featureStr[0]);
		    		else if(k==2) {
    		    		for(int i=0; i<weightLength; i++) 
    		    			tmpWeight += Double.parseDouble(featureStr[i]) * weights[i];
		    		}
		    		firstWeights[count] = tmpWeight;
		    		
		    		count++;
				}
				
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
		
		PyCharOptions options = new PyCharOptions(args);		
		PyCharPipe pipe = new PyCharPipe(options);
		PyCharParser parser = new PyCharParser(pipe, options);
		
		//base表示基于字符的模型, options.numIters = 6
		if(options.charTrain) {
			//pipe.loadPinyinWordPair();
			pipe.loadWordPinyinPair(options.wordPinyinFile);
			parser.trainChar(options.trainFile, options.baseModelFile, options.numIters);
		}
		if(!options.charTrain && options.charDev) {
			for (int i=0; i<options.numIters; i++) {
				parser.loadCharModel(options.baseModelFile + "-" + i);
				pipe.loadWordPinyinPair(options.wordPinyinFile);
				parser.outputBestChar(options.testFile, options.outputFile + "-" + i);
			}
		}		
		if(options.charTest) {
			parser.loadCharModel(options.baseModelFile);
			pipe.loadWordPinyinPair(options.wordPinyinFile);
			parser.outputBestChar(options.testFile, options.outputFile);
		}		
		//ngram模型
		if(options.ngram) {
			//pipe.loadPinyinWordPair();
			pipe.loadWordPinyinPair(options.wordPinyinFile);
			pipe.loadBerkeleyLmBin(options.binLmFile);
			parser.outputBestNgram(options.testFile, options.outputFile);			
		}
		
		//linear reranking模型
		if(options.mertTrain) {		
			parser.loadCharModel(options.baseModelFile);			
			pipe.loadWordPinyinPair(options.wordPinyinFile);			
			pipe.loadBerkeleyLmBin(options.binLmFile);
			pipe.loadCharLmBin(options.charLmFile);
			pipe.loadPinyinWordCo(options.pinyinWordCoFile);
			parser.loadPOSModel(options.posModelFile);
			pipe.loadPOSWordCo(options.POSWordCoFile);
			pipe.loadWordPOSCo(options.wordPOSCoFile);
			parser.loadDependencyModel(options.dependencyModelFile);			
			parser.outputMertNbest(options.testFile, options.mertCandFile, options.mertDependencyFile);			
		}
		if(options.mertDevOffline) {					
			parser.outputMertBestOffline(options.mertCandFile, options.outputFile, options.mertWeights);			
		}		
		if(options.mertDevOnline) {
			parser.loadCharModel(options.baseModelFile);			
			pipe.loadWordPinyinPair(options.wordPinyinFile);
			pipe.loadBerkeleyLmBin(options.binLmFile);			
			parser.outputMertBestOnline(options.testFile, options.outputFile);			
		}
		
		if(options.rerankTrain) {
			parser.trainAPReranker(options.rerankerTrainFile, 
					options.mertCandTrainFile, 
					options.mertDependencyTrainFile, 
					options.initWeight, 
					options.featureSet, 
					options.rerankerModelFile + "-" + options.initWeight + "-" + options.featureSet);			
		}
		if(options.rerankDev) {			
			for(int i=1; i<options.numIters; i++) {
				parser.loadAPRerankerModel(options.rerankerModelFile+"-"+options.initWeight + "-" + options.featureSet+"-"+i);
				parser.testAPReranker(options.mertCandTestFile, 
						options.mertDependencyTestFile, 
						options.initWeight,
						options.featureSet,
						options.rerankTestOutputFile +"-"+options.initWeight + "-" + options.featureSet + "-"+i
						);
			}			
		}
		if(options.rerankTest) {				
			parser.loadAPRerankerModel(options.rerankerModelFile);
			parser.testAPReranker(options.mertCandTestFile, 
					options.mertDependencyTestFile, 
					options.initWeight,
					options.featureSet,
					options.rerankTestOutputFile);			
		}		
		
	}
	
	
	
	
	

}
