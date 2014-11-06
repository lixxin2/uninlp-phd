package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.mst.DependencyInstance;
import edu.hitsz.nlp.segpos.FeatureVector;
import edu.hitsz.nlp.segpos.Instance;

public class PyCharPipe {
	
	public PyWordPair pwPair;
	public FeatureMap dataMap;
	public PyCharReader reader;
	public PyCharWriter writer;
	public PyCharOptions options;
		
	NgramLanguageModel<String> wordLm;
	NgramLanguageModel<String> charLm;		
	CoOccurrence pinyinWordCo;
	CoOccurrence POSWordCo;
	CoOccurrence wordPOSCo;
	NgramLanguageModel<String> posLm;
	
	
	
	public PyCharPipe(PyCharOptions options) {
		pwPair = new PyWordPair();
		dataMap = new FeatureMap();
		reader = new PyCharReader();
		writer = new PyCharWriter();
		this.options = options;
	}
	
	
	
	
	/**
	 * 获取文件中的句子个数
	 * @since Oct 23, 2012
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public int getSentenceSize(String file) throws IOException{
		int sentenceSize = 0;
    	System.out.print("Read tags from file " +file + "...");
		reader.startReading(file);
		PyCharInstance instance = reader.getNext();
		while(instance != null) {
			sentenceSize++;
		    instance = reader.getNext();
		}

		return sentenceSize;
		
    }
	
	
	
	/**
     * 从依存实例创造特征向量,把其中的特征都生成出来
     * @param instance
     * @return
     */
    public FeatureVector createFeatureVector(PyCharInstance instance) {
		
		String[] pinyins = instance.wordYins;
		String[] words = instance.words;				
		
		FeatureVector fv = new FeatureVector();
		
		final int length = words.length;
		PyCharItem preItem = null;
		int start = 0;
		int end = 0;
		
		for(int i = 0; i < length; i++) {
			end = start + words[i].length();
			PyCharItem curItem = new PyCharItem(start, end, pinyins[i], words[i], preItem);
			addWordFeatures(instance, curItem, fv, true);
			start = end;
			preItem = curItem;
		}
		return fv;
		
    }
	
	
	
	
	
	
	
	
	/**
	 * 词的特征
	 * @since Oct 23, 2012
	 * @param inst
	 * @param item
	 * @param fv
	 * @param added
	 */
	public void addWordFeatures(PyCharInstance inst, PyCharItem item, 
    		FeatureVector fv, boolean added) {
    	
    	int MaxWordLength = 15;
    	int sentenceLength = inst.length;

    	String[] chars = inst.characterYins;
    	
    	PyCharItem preItem = null;
    	PyCharItem pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    			pre2Item = preItem.left;
    		}
    	}
    	
    	String curPinyin = item.pinyin; //当前拼音
    	String curWord = item.word;  //当前词
    	
    	String prePinyin = "pwy";
    	String preWord = "pw";
    	String pre2Pinyin = "p2wy";
    	String pre2Word = "p2w";
    	
    	if(preItem != null) {
    		prePinyin = preItem.pinyin;
    		preWord = preItem.word;
    		if(pre2Item != null) {
    			pre2Pinyin = pre2Item.pinyin;
    			pre2Word = pre2Item.word;
    		}
    	}
    	
    	int start = item.s;
    	int end = item.t;
    	
    	for(int i=start; i<end; i++) {
    		String character = curWord.substring(i-start,i-start+1);
    		addCharFeature(inst, item, fv, i, character, added);
    	}  
    	
    	//addSecondFeatures(pre2Pinyin, prePinyin, curPinyin, pre2Word, preWord, curWord, fv, added);
    	
    }
	
	
	
	
	/**
	 * 字的特征
	 * @since Oct 23, 2012
	 * @param inst
	 * @param item
	 * @param fv
	 * @param i
	 * @param character
	 * @param added
	 */
	public void addCharFeature(PyCharInstance inst, PyCharItem item, 
    		FeatureVector fv, int i, String character, boolean added) {
		
		String[] pinyin = inst.characterYins;
		int length = inst.length;
		
		String pre3Pinyin= "p3py";
		String pre2Pinyin= "p2py";
		String prePinyin= "ppy";
		String nextPinyin= "npy";
		String next2Pinyin= "n2py";
		String next3Pinyin= "n3py";
		
		String curPinyin = pinyin[i];
		if(i>0){
			prePinyin = pinyin[i-1];
			if(i>1) {
				pre2Pinyin = pinyin[i-2];
				if(i>2) {
					pre3Pinyin = pinyin[i-3];
				}
			}
		}
		
		if(i+1 < length) {
			nextPinyin = pinyin[i+1];
			if(i+2 < length) {
				next2Pinyin = pinyin[i+2];
				if(i+3 < length) {
					next3Pinyin = pinyin[i+3];
				}
			}
		}
		
		add("1="+pre2Pinyin+"_"+character, fv, added);
		add("2="+prePinyin+"_"+character, fv, added);
		add("3="+curPinyin+"_"+character, fv, added);
		add("4="+nextPinyin+"_"+character, fv, added);
		add("5="+next2Pinyin+"_"+character, fv, added);
		add("6="+prePinyin+"_"+curPinyin+"_"+character, fv, added);
		add("7="+curPinyin+"_"+nextPinyin+"_"+character, fv, added);
		add("8="+prePinyin+"_"+nextPinyin+"_"+character, fv, added);
		
		
	}
	
	/** 生成reranker的特征 
	 * @param featureSet 1：词特征；2：词和词性特征；5：dep特征
	 * */
	 public FeatureVector createFeatureVectorReranker(DependencyInstance instance, 
			 boolean added,
			 int featureSet) {

			FeatureVector fv = new FeatureVector();	
			
			String[] words = instance.forms;
			String[] tags = instance.postags;
			int[] heads = instance.heads;
			if(words == null) return fv;
			int length = words.length;								
									
			//word, pos pair
			for(int i=1; i<length; i++) {
				
				String curPos = tags[i];
				String curWord = words[i];
				
				if(featureSet == 1 || featureSet == 2 || featureSet == 5 ) add("1a:"+curWord, fv, added);
				if(featureSet == 2 || featureSet == 5) add("2a:"+curPos, fv, added);
				if(featureSet == 2 || featureSet == 5) add("2b:"+curPos+"_"+curWord, fv, added);
				
				if(i>1) {
					String prePos = tags[i-1];
					String preWord = words[i-1];
					
					if(featureSet == 1 || featureSet == 2 || featureSet == 5 ) add("1b:"+preWord+"_"+curWord, fv, added);
					if(featureSet == 2 || featureSet == 5) add("2c:"+prePos+"_"+curPos, fv, added);
					if(featureSet == 3) add("3a:"+prePos+"_"+curPos+"_"+curWord, fv, added);
					if(featureSet == 3) add("3b:"+prePos+"_"+preWord+"_"+curPos+"_"+curWord, fv, added);
					
					if(i>2) {
						String pre2Pos = tags[i-2];
						String pre2Word = words[i-2];
						
						if(featureSet == 1 || featureSet == 2 || featureSet == 5 ) add("1c:"+pre2Word+"_"+preWord+"_"+curWord, fv, added);						
						if(featureSet == 2 || featureSet == 5 ) add("2d:"+pre2Pos+"_"+prePos+"_"+curPos, fv, added);
						if(featureSet == 3) add("3c:"+pre2Pos+"_"+prePos+"_"+preWord+"_"+curPos+"_"+preWord, fv, added);
						if(featureSet == 3) add("3d:"+pre2Pos+"_"+pre2Word+"_"+prePos+"_"+preWord+"_"+curPos+"_"+preWord, fv, added);
						
					}
					
				}
			}			
				
			//dep				
			for(int i=1; i<length; i++) {
				String curPos = tags[i];
				String curWord = words[i];			
				//son-father
				int father = heads[i];
				String fatherPos = tags[father];
				String fatherWord = words[father];
				int direction = i < father ? 0 : 1;     //该节点在其父节点的左右位置： 0: left; 1:right;
				int adjacent = Math.abs(i-father) == 1 ? 1 : 0;  //1：邻近； 0：不邻近
				if(featureSet == 5) add("4a:"+curWord+"_"+direction+"_"+adjacent+"_"+fatherWord, fv, added);
				if(featureSet == 5) add("4b:"+curPos+"_"+direction+"_"+adjacent+"_"+fatherPos, fv, added);
				if(featureSet == 5) add("4c:"+curWord+"_"+curPos+"_"+direction+"_"+adjacent+"_"+fatherPos, fv, added);
				if(featureSet == 5) add("4d:"+curPos+"_"+direction+"_"+adjacent+"_"+fatherWord+"_"+fatherPos, fv, added);
				if(featureSet == 5) add("4e:"+curPos+"_"+curWord+"_"+direction+"_"+adjacent
						+"_"+fatherPos+"_"+fatherWord, fv, added);
				
				/*
				//son-father-grand				
				if(father != 0) {
					int grand = heads[father];
					String grandPos = tags[grand];
					String grandWord = words[grand];
					int grandDirection = father < grand ? 0 : 1;
					int grandAdjacent = Math.abs(father-grand) == 1 ? 1 : 0; 
					if(featureSet == 4 || featureSet == 5) add("14:"+curPos+"_"+direction+"_"+adjacent
							+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandPos, fv, added);
					if(featureSet == 4 || featureSet == 5) add("15:"+curWord+"_"+curPos+"_"+direction+"_"+adjacent							
							+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandPos, fv, added);
					if(featureSet == 4 || featureSet == 5) add("16:"+curPos+"_"+direction+"_"+adjacent							
							+"_"+fatherWord+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandPos, fv, added);
					if(featureSet == 4 || featureSet == 5) add("17:"+curPos+"_"+direction+"_"+adjacent							
							+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandWord+"_"+grandPos, fv, added);
					if(featureSet == 4 || featureSet == 5) add("18:"+curWord+"_"+curPos+"_"+direction+"_"+adjacent							
							+"_"+fatherWord+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandPos, fv, added);
					if(featureSet == 4 || featureSet == 5) add("19:"+curPos+"_"+direction+"_"+adjacent							
							+"_"+fatherWord+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandWord+"_"+grandPos, fv, added);
					if(featureSet == 4 || featureSet == 5) add("20:"+curPos+"_"+curWord+"_"+direction+"_"+adjacent							
							+"_"+fatherWord+"_"+fatherPos+"_"+grandDirection+"_"+grandAdjacent
							+"_"+grandWord+"_"+grandPos, fv, added);
					
				}
				*/
			}
			
			/*
			//dep sibling
			ArrayList<ArrayList<Integer>> sons = new ArrayList<ArrayList<Integer>>();
			for(int i=0; i<length; i++) 
				sons.add(new ArrayList<Integer>());
			for(int i=1; i<length; i++) {
				int father = heads[i];
				sons.get(father).add(i);
			}
			for(int i=0; i<length; i++) {
				ArrayList<Integer> son = sons.get(i);
				int sonSize = son.size();
				if(sonSize > 1) {
					String fatherPos = tags[i];
					String fatherWord = words[i];
					for(int j=0; j<sonSize-1; j++) {						
						int leftSonIndex = son.get(j);
						String leftSonPos = tags[leftSonIndex];
						String leftSonWord = words[leftSonIndex];	
						int leftSonDirection = leftSonIndex < i ? 0 : 1;
						int leftSonAdjacent = Math.abs(leftSonIndex-i) == 1 ? 1 : 0; 					
						int rightSonIndex = son.get(j+1);
						String rightSonPos = tags[rightSonIndex];
						String rightSonWord = words[rightSonIndex];		
						int rightSonDirection = rightSonIndex < i ? 0 : 1;
						int rightSonAdjacent = Math.abs(rightSonIndex-i) == 1 ? 1 : 0; 
						if(featureSet == 4 || featureSet == 5) add("21:"+leftSonPos+"_"+leftSonDirection+"_"+leftSonAdjacent
								+"_"+rightSonPos+"_"+rightSonDirection+"_"+rightSonAdjacent
								+"_"+fatherPos, fv, added);
						if(featureSet == 4 || featureSet == 5) add("22:"+leftSonWord+"_"+leftSonPos+"_"+leftSonDirection+"_"+leftSonAdjacent
								+"_"+rightSonWord+"_"+rightSonPos+"_"+rightSonDirection+"_"+rightSonAdjacent
								+"_"+fatherPos, fv, added);
						if(featureSet == 4 || featureSet == 5) add("23:"+leftSonPos+"_"+leftSonDirection+"_"+leftSonAdjacent
								+"_"+rightSonPos+"_"+rightSonDirection+"_"+rightSonAdjacent
								+"_"+fatherWord+"_"+fatherPos, fv, added);
						if(featureSet == 4 || featureSet == 5) add("24:"+leftSonPos+"_"+leftSonWord+"_"+leftSonDirection+"_"+leftSonAdjacent
								+"_"+rightSonPos+"_"+rightSonWord+"_"+rightSonDirection+"_"+rightSonAdjacent
								+"_"+fatherPos+"_"+fatherWord, fv, added);
					}
				}
			}	
			*/
			
			return fv;		
			
	 }
	 
	 
	
	
	
	
	/**
	 * 添加一个特征
	 * @since Oct 23, 2012
	 * @param feat
	 * @param fv
	 * @param added
	 */
	public void add(String feat, FeatureVector fv, boolean added) {    	
    	if(added) {
    		int num = dataMap.add(feat);
        	if(num >= 0)
        		fv.add(num);
    	}
    	else {
    		fv.addString(feat);
	    	int num = dataMap.get(feat);
	    	if(num >= 0)
	    		fv.add(num);
    	}    	
    }
		
	/** 载入词音词典*/
	public void loadWordPinyinPair(String wordPinyinFile) throws IOException {

		pwPair.loadWordPinyin(wordPinyinFile);		
		
	}
	
	public void initInputFile (String file) throws IOException {
    	reader.startReading(file);
    }
	
    public void initOutputFile (String file) throws IOException {
		writer.startWriting(file);
    }
	
    public PyCharInstance nextRawInstance() throws IOException {
    	PyCharInstance instance = reader.getNextPinyin();
    	return instance;
    }
    
    public void outputInstance(PyCharInstance instance) throws IOException {
    	writer.writeInstance(instance);    	
    }
    
    public void outputInstanceWords(PyCharInstance instance) throws IOException {
    	writer.writeInstanceWords(instance);    	
    }
    
    
    /**
     * Ngram输出的候选词序列 可能小于mertK， 该函数是为了补充完全mertK个最优的结果序列
     * @since Nov 12, 2012
     * @param instances
     * @param K
     * @throws IOException
     */
    public void renewInstances(List<PyCharInstance> instances, int K) {
    	
    	int instanceLength = instances.size(); //已有的实例数
    	PyCharInstance lastInstance = instances.get(instanceLength-1);

    	for(int i = instanceLength; i < K; i++) {
    		instances.add(lastInstance);
    	}
    }
    
    public void renewWeights(List<Double> weights, int K) {
    	
    	if(weights == null)
    		weights = new ArrayList<Double>();
    	if(weights.size() == 0)
    		weights.add(0.0d);
    	
    	int instanceLength = weights.size(); //已有的实例数
    	double lastWeight = weights.get(instanceLength-1);

    	for(int i = instanceLength; i < K; i++) {
    		weights.add(lastWeight);
    	}
    }
        
    
    /** 获得 char Discriminative Model 的概率 */
    public ArrayList<Double> getCharModelWeights(Parameters params, ArrayList<PyCharInstance> instances) {
    	
    	ArrayList<Double> weights = new ArrayList<Double>();
    	for(PyCharInstance instance : instances) {
    		weights.add(getCharModelWeight(params, instance));
    	}
    	return weights;
    	
    }
    
    
    /**
     * 获取句子的权重，根据char特征
     * @since Nov 12, 2012
     * @param params
     * @param instance
     * @return
     */
    private double getCharModelWeight(Parameters params, PyCharInstance instance) {
    	
    	String[] words = instance.words;
    	String[] wordYins = instance.wordYins;
    	PyCharItem preItem = null;
    	double preWeight = 0.0;
    	int s=0;
    	int t=0;
    	for(int i=0; i<words.length; i++) {
    		String word = words[i];
    		String wordYin = wordYins[i];   
    		t += word.length();
	    	
	    	FeatureVector fv = new FeatureVector();
			PyCharItem newItem = new PyCharItem(s, t, wordYin, word, preWeight, fv, preItem);
		
			addWordFeatures(instance, newItem, fv, false);
						
			double curWeight = fv.getScore(params);
			preWeight += curWeight;		
			newItem.prob = preWeight;
			
			preItem = newItem;
			s = t; 
    	}
    	return preItem.prob;    	
    	
    }
    
    /** 获得char Ngram的概率 */
    public ArrayList<Double> getCharNGramWeights(ArrayList<PyCharInstance> instances) {
    	
    	ArrayList<Double> weights = new ArrayList<Double>();
    	for(PyCharInstance instance : instances) {
    		weights.add(getCharNGramWeight(instance));
    	}
    	return weights;    	
    }
    
    private double getCharNGramWeight(PyCharInstance instance) {
    	
    	int charLmOrder = charLm.getLmOrder();
    	String[] words = instance.words;
    	ArrayList<String> chars = new ArrayList<String>();
    	    	
    	for(int i=0; i<words.length; i++) 
    		for(int j=0; j<words[i].length(); j++)
    			chars.add(words[i].substring(j, j+1));
    	int length = chars.size();
    	
    	double preWeight = 0.0;
    	PyCharItem preItem = null;
    	for(int i=0; i<length; i++) {
    		FeatureVector fv = new FeatureVector();	    	
			PyCharItem newItem = new PyCharItem(i, i+1, "", chars.get(i), preWeight, fv, preItem);
		
			List<String> ngram = newItem.getNgram(charLmOrder);							
			preWeight += charLm.getLogProb(ngram);
			//
			if(i==length-1) {
				ngram = newItem.getNgramEnd(charLmOrder);
				preWeight += charLm.getLogProb(ngram);
			}										
			newItem.prob = preWeight;	
			preItem = newItem;
    	}
    	return preWeight;    
    }
    
    /** 拼音-词 共现 的 概率 */
    public ArrayList<Double> getPinyinWordCoWeights(ArrayList<PyCharInstance> instances) {
    	
    	ArrayList<Double> weights = new ArrayList<Double>();
    	for(PyCharInstance instance : instances) {
    		weights.add(getPinyinWordCoWeight(instance));
    	}
    	return weights;
    	
    }
    
    
    private double getPinyinWordCoWeight(PyCharInstance instance) {
    	
    	String[] words = instance.words;
    	String[] pinyins = instance.wordYins;
    	
    	double weight = 0.0;
    	for(int i=0; i<words.length; i++) {
    		
    		double curWeight = pinyinWordCo.get(pinyins[i], words[i]);
    		weight += getLog(curWeight);    		
    	}
    	
    	return weight;
    	
    	
    }
    
    /**
     * log值，如果p<1e-10,返回-10
     * @since Dec 6, 2012
     * @param curWeight
     * @return
     */
    private double getLog(double curWeight) {
    	if(curWeight < 1e-10)
    		return -10.0f;
    	else
    		return Math.log(curWeight);
    }
    
    /** 获得 词性-词 共现 概率 */
    public ArrayList<Double> getPOSWordCoWeights(ArrayList<Instance> instances) {
    	
    	ArrayList<Double> weights = new ArrayList<Double>();
    	for(Instance instance : instances) {
    		weights.add(getPOSWordCoWeight(instance));
    	}
    	return weights;
    	
    }
    
    private double getPOSWordCoWeight(Instance instance) {
    	
    	String[] words = instance.words;
    	String[] tags = instance.tags;
    	
    	double weight = 0.0;
    	for(int i=0; i<words.length; i++) {
    		
    		double curWeight = POSWordCo.get(tags[i], words[i]);
    		weight += getLog(curWeight);    		
    	}
    	
    	return weight;
    	
    	
    }
    
    /** 获得 词-词性 共现 概率 */
    public ArrayList<Double> getWordPOSCoWeights(ArrayList<Instance> instances) {
    	
    	ArrayList<Double> weights = new ArrayList<Double>();
    	for(Instance instance : instances) {
    		weights.add(getWordPOSCoWeight(instance));
    	}
    	return weights;
    	
    }
    
    private double getWordPOSCoWeight(Instance instance) {
    	
    	String[] words = instance.words;
    	String[] tags = instance.tags;
    	
    	double weight = 0.0;
    	for(int i=0; i<words.length; i++) {
    		
    		double curWeight = wordPOSCo.get(words[i], tags[i]);
    		weight += getLog(curWeight);    		
    	}
    	
    	return weight;
    	
    	
    }
    
    
    /** 获得词性 Ngram 的概率 */
    public ArrayList<Double> getPOSNGramWeights(ArrayList<Instance> instances) {
    	
    	ArrayList<Double> weights = new ArrayList<Double>();
    	for(Instance instance : instances) {
    		weights.add(getPOSNGramWeight(instance));
    	}
    	return weights;
    	
    }
    
    private double getPOSNGramWeight(Instance instance) {
    	
    	int charLmOrder = charLm.getLmOrder();
    	String[] tags = instance.tags;
    	int length = tags.length;    	    	
    	
    	double preWeight = 0.0;
    	PyCharItem preItem = null;
    	for(int i=0; i<length; i++) {
    		FeatureVector fv = new FeatureVector();	    	
			PyCharItem newItem = new PyCharItem(i, i+1, "", tags[i], preWeight, fv, preItem);
		
			List<String> ngram = newItem.getNgram(charLmOrder);							
			preWeight += charLm.getLogProb(ngram);
			//
			if(i==length-1) {
				ngram = newItem.getNgramEnd(charLmOrder);
				preWeight += charLm.getLogProb(ngram);
			}										
			newItem.prob = preWeight;	
			preItem = newItem;
    	}
    	return preWeight;    
    }
    
    
    
    
    /**
     * 输出概率最大的结果序列
     * @since Nov 12, 2012
     * @param params
     * @param instances
     * @param ngramWeights
     * @param seq
     * @param mertK
     * @throws IOException
     */
    public void outputInstancesMertBest(Parameters params,
    		ArrayList<PyCharInstance> instances, 
    		ArrayList<Double> ngramWeights) throws IOException {
    	
    	int instanceLength = instances.size(); //已有的实例数
    	
    	int j=0; //已有的句子
    	double bestWeight = -1e5; //最后的权重值
    	int bestK = 0;
    	
    	double[] weights = options.mertWeights;
    	
    	while(j<instanceLength) {
    		    		
    		double charWeight = getCharModelWeight(params, instances.get(j));
    		double curWeight = weights[0] * ngramWeights.get(j) 
    				+ weights[1] * charWeight;
    		if(curWeight > bestWeight) {
    			bestWeight = curWeight;
    			bestK = j;
    		}
    		j++;
    	}
    	    	
    	writer.writeInstanceWords(instances.get(bestK));
    	
    }
    
    
    
    public void close () throws IOException {
		if (null != writer) {
		    writer.finishWriting();
		}
    }
    
   
    /** 载入berkeleyLM */
    public void loadBerkeleyLmBin(String binLmFile) {    	
    	System.out.print("load BerkeleyLM model: " + options.binLmFile + "... ");
    	wordLm = (ArrayEncodedNgramLanguageModel) LmReaders.readLmBinary(binLmFile);
    	wordLm.setOovWordLogProb(-10.0f);    	
    	System.out.println("done");
    }
    
    /*
    public void loadBerkeleyLm() {
    	System.out.print("load BerkeleyLM model: " + options.lmFile + "... ");
    	wordLm = LmReaders.readArrayEncodedLmFromArpa(options.lmFile, false);
    	wordLm.setOovWordLogProb(-10.0f);    	
    	System.out.println("done");
    }
    */

    public void loadCharLmBin(String charLmFile) {    	
    	
    	System.out.print("load Char LM model: " + options.charLmFile + "... ");
    	charLm = (ArrayEncodedNgramLanguageModel) LmReaders.readLmBinary(charLmFile);
    	charLm.setOovWordLogProb(-10.0f);    	
    	System.out.println("done");
    	
    }
    
    /** 拼音 词 共现概率*/
    public void loadPinyinWordCo(String pinyinWordCoFile) {
    	
    	System.out.print("load Pinyin Word CoOccurrence: " + pinyinWordCoFile + "... ");
    	pinyinWordCo = new CoOccurrence(0, 0);		
    	pinyinWordCo.read(options.pinyinWordCoFile);
    	
    }
    
    public void loadPOSWordCo(String POSWordCoFile) {
    	
    	System.out.print("load POS Word CoOccurrence: " + options.POSWordCoFile + "... ");
    	POSWordCo = new CoOccurrence(0, 0);		
    	POSWordCo.read(POSWordCoFile);
    	
    }
    
    public void loadWordPOSCo(String wordPOSCoFile) {
    	
    	System.out.print("load Word POS CoOccurrence: " + options.wordPOSCoFile + "... ");
    	wordPOSCo = new CoOccurrence(0, 0);		
    	wordPOSCo.read(wordPOSCoFile);
    	
    }
    
    
    /** 得到句子的数目 */
    public int getSentenceNumber(String fileName) {
    	
		int sentenceNumber = 0;
		
    	try {
    		String fileEncoding = FileEncoding.getCharset(fileName);
    		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
            
    		String line = null;
    		while( (line=reader.readLine()) != null) {
    			line = line.trim();
    			if(line.length() > 0) {
    				sentenceNumber++;
    			}
    		}
    		
    		reader.close();
    	}
    	catch (IOException e) {
    		
    	}
    	return sentenceNumber;    	
    	
    }

}
