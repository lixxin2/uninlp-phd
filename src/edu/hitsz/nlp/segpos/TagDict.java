package edu.hitsz.nlp.segpos;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.nlp.corpus.ctb.CTB;

/**
 * "A Fast Decoder for Joint Word Segmentation and POS-Tagging Using a Single Discriminative Model" by Yue Zhang & Stephen Clark 2010 
 * <p> 中采用的词典，包括：
 * <li> 每个词性对应的最长词长 posMaxWordLength
 * <li> 频率高的词 wordFreqPosMap
 * <li> 闭集词性对应的词  closedSetWordPosMap
 * <li> 闭集词性对应的词的首字  closedSetCharPosMap 
 * <li> 还有一个约束是相同p0p-1w-1的所有候选只保存一个，这个我们不在本类中处理
 * @author Xinxin Li
 * @since 2013-2-5
 */
public class TagDict implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/** 词性 */
	ArrayList<String> allSetPOS;
	HashMap<String, Integer> allSetPOSMap;
	/** 闭集词性 */
	ArrayList<String> closedSetPOS;
	HashMap<String, Integer> closedSetPOSMap;
	/** */
	ArrayList<String> openSetPOS;
	

	/** 每个词性对应的最长词长 */
	HashMap<String, Integer> posMaxWordLength;
	
	/** 每个词对应的词性和出现的次数 */
	HashMap<String, WordPosFreqMap> wordFreqPosMap;
	int maxWordFreq = 0;
	final int maxWordLength = 20;
	
	/** 闭集词性对应的词 */
	HashMap<String, HashMap<String, Integer>> closedSetWordPosMap;
	/** 闭集词性对应的首字 */
	HashMap<String, ArrayList<String>> closedSetCharPosMap;
		
	
	
	public TagDict() {
		
		allSetPOS = new ArrayList<String>();
		allSetPOSMap = new HashMap<String, Integer>();
		closedSetPOS = new ArrayList<String>();
		closedSetPOSMap = new HashMap<String, Integer>();		
		openSetPOS = new ArrayList<String>();
		
		posMaxWordLength = new HashMap<String, Integer>();
		
		wordFreqPosMap = new HashMap<String, WordPosFreqMap>();
		closedSetWordPosMap = new HashMap<String, HashMap<String, Integer>>();
		closedSetCharPosMap = new HashMap<String, ArrayList<String>>();
	}
		
	
	/**
	 * 初始化，保存闭集词性标示和开集词性标示，
	 * <p> 保存
	 * @param wordTrainName
	 * @param tags POS tags
	 * @throws IOException 
	 */
	public void initialize(String[] tags) throws IOException{
				
		/* 每个词性对应的最长词长 */
		for(String pos : tags) {
			posMaxWordLength.put(pos, 1);
		}
		
		//保存Closet set POS tags and open set
		//CTB中所有的闭集词性标示
		HashMap<String, Integer> ctbClosedSetPOSMap= new HashMap<String, Integer>();
		for(String pos : CTB.ctbClosedSetPosTags) {
			ctbClosedSetPOSMap.put(pos, 1);
		}
		//对于语料库中的每个词性
		for(String pos : tags) {
			allSetPOS.add(pos);
			allSetPOSMap.put(pos, allSetPOSMap.size());
			if(ctbClosedSetPOSMap.containsKey(pos)) {
				closedSetPOS.add(pos);
				closedSetPOSMap.put(pos, 1);
			}
			else
				openSetPOS.add(pos);
		}
						
	}
	
	
	/**
	 * 对于每个句子，在decoding之前要预处理，包括：
	 * <li> 保存每个词性的最大词长
	 * <li> 保存词的频度及其对应的词性
	 * <li> 保存
	 * @since 2013-2-7
	 * @param instance
	 */
	public void processInstance(Instance instance) {
			
		String[] words = instance.words;
		String[] poses = instance.tags;
		int sentenceLength = words.length;
		//每个字
		for(int j=0; j<sentenceLength; j++) {
			
			String word = words[j];
			int wordLength = word.length();
			String startChar = word.substring(0,1);

			String pos = poses[j];
			 
			 //对每个pos tag，保存其对应最大词长
			if(wordLength > posMaxWordLength.get(pos))
				posMaxWordLength.put(pos, wordLength);
			
			//对每个词建立tag频度统计
			int curWordFreq = 0;
			 if(!wordFreqPosMap.containsKey(word)){
				 WordPosFreqMap newPosFreq = new WordPosFreqMap();
				 newPosFreq.addFreq();
				 newPosFreq.addPos(pos);
				 curWordFreq = newPosFreq.getFreq();
				 wordFreqPosMap.put(word, newPosFreq);
			 }
			 else{
				 WordPosFreqMap newPosFreq = wordFreqPosMap.get(word);
				 newPosFreq.addFreq();
				 if(!newPosFreq.containsPos(pos))
					 newPosFreq.addPos(pos);
				 curWordFreq = newPosFreq.getFreq();
				 wordFreqPosMap.put(word, newPosFreq);
			 }
			 if(maxWordFreq < curWordFreq)         //统计词频最大的词
				 maxWordFreq = curWordFreq;
			
			 //closed set,保存词和首字的所有闭集词性
			 //对于每个闭集词性标示
			 if(closedSetPOSMap.containsKey(pos)) {
				 //如果不包含该词性
				 if(!closedSetWordPosMap.containsKey(word)) {
					 HashMap<String, Integer> wordPoses = new HashMap<String, Integer>();
					 wordPoses.put(pos, 1);
					 closedSetWordPosMap.put(word, wordPoses);
				 }
				 else {
					 HashMap<String, Integer> wordPoses = closedSetWordPosMap.get(word);
					 if(!wordPoses.containsKey(pos))
						 wordPoses.put(pos, 1);
				 }
				 //如果不包含该首字
				 if(!closedSetCharPosMap.containsKey(startChar)) {
					 ArrayList<String> wordPoses = new ArrayList<String>();
					 wordPoses.add(pos);
					 wordPoses.addAll(openSetPOS);        
					 closedSetCharPosMap.put(startChar, wordPoses);
				 }
				 else {
					 ArrayList<String> wordPoses = closedSetCharPosMap.get(startChar);
					 if(!wordPoses.contains(pos))
						 wordPoses.add(pos);
				 }
			 }			 		
		}
	}
	
	
	
	/**获取首字符对应的词性:
	 * <li> 如果closed set Pos tags 包含该字，则返回包含该字的closed set Pos tags和open set
	 * <li> 否则只返回open set
	 * @since 2013-2-7
	 * @param startChar
	 * @return
	 */
	public ArrayList<String> getCharPosTags(String startChar) {
		if(closedSetCharPosMap.containsKey(startChar))
			return closedSetCharPosMap.get(startChar);
		else
			return openSetPOS;
	}
	
	
	/** 判断该词和词性是否符合
	 * <li> 该词的长度要小于它的词性的最长词长
	 * <li> 如果该词是高频词，则词性应在其高频词性中
	 * <li> 如果该词位于闭集词中，则其词性为对应的闭集词性中
	 * @since 2013-2-7
	 * @param word
	 * @param pos
	 * @return
	 */
	public boolean isValidWordPOS(String word, String pos) {
		if(isValidWordLength(word, pos) && isValidFreqWord(word, pos) 
				& isValidClosedSetWord(word, pos))
			return true;
		else
			return false;
	}

	
	/** 判断一个词的长度
	 * <li> 是否超出最长词长，一般为20
	 * <li> 是否符合词性的最长词长  
	 * @since 2013-2-7
	 * @param word
	 * @param pos
	 * @return
	 */
	public boolean isValidWordLength(String word, String pos) {
		
		if(word.length() > maxWordLength)
			return false;
		int posMaxLength = posMaxWordLength.get(pos);
		if(word.length() <= posMaxLength)
			return true;
		else
			return false;
	}
	
	/**
	 * 判断该词是否为高频词
	 * <li> 是，如果高频词性中包含该词性，则返回true；否则返回false
	 * <li> 否，返回true
	 * @since 2013-2-7
	 * @param word
	 * @param pos
	 * @return
	 */
	public boolean isValidFreqWord(String word, String pos) {
		//如果是高频词
		if(wordFreqPosMap.containsKey(word) && wordFreqPosMap.get(word).getFreq() > maxWordFreq/5000 + 5) {
			if(wordFreqPosMap.get(word).containsPos(pos))
				return true;
			else
				return false;
		}
		else
			return true;		
	}
	
	/**
	 * 判断该词是否是闭集词
	 * <li> 是，则判断该词性是否位于词所属的闭集词性，是返回true;否返回false
	 * <li> 否
	 * @since 2013-2-7
	 * @param word
	 * @param pos
	 * @return
	 */
	public boolean isValidClosedSetWord(String word, String pos) {
		if(closedSetWordPosMap.containsKey(word)) {
			if(closedSetWordPosMap.get(word).containsKey(pos))
				return true;
			else
				return false;
		}
		else
			return true;
			
	}		
}



/**
 * 每个词对应的频率和对应的词性集合
 * @author Xinxin Li
 * @since 2013-2-7
 */
class WordPosFreqMap implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int freq = 0;
	private HashMap<String, Integer> posMap;
	
	WordPosFreqMap() {
		posMap = new HashMap<String, Integer>();
	}
	
	void addFreq() {
		freq += 1;
	}
	
	int getFreq() {
		return freq;
	}
	
	void addPos(String pos) {
		if(!posMap.containsKey(pos))
			posMap.put(pos, 1);
	}
	
	boolean containsPos(String pos) {
		return posMap.containsKey(pos);
	}
	
	
}
	
