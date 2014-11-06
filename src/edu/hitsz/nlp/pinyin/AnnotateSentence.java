package edu.hitsz.nlp.pinyin;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.segmentation.Matching;
import edu.hitsz.nlp.segmentation.MinimumWordNumber;
import edu.hitsz.nlp.segmentation.BackwardMaximumMatching;


/**
 * 根据词音词典，用最少分词数、后向匹配等方法来标注语音。最少分词数的方法注音最准
 * @author Xinxin Li
 * @since Nov 12, 2012
 */
public class AnnotateSentence {
	
	public WordPyPair wp; //词音map
	
	boolean outputWord; //输出词
	boolean wordAnnotate; //按词标注（先分词，然后对每个词再标注拼音，适用于以分词的语料）
	boolean characterMultiple; //单字词给多注音
	boolean wordMultiple; //多字词给多注音
		
	public AnnotateSentence() {
		wp = new WordPyPair();	

		loadNotChinese();
		setOptions(true, true, false, false);
	}
	
	/**
	 * 读取标点符号和数字
	 * @since Nov 21, 2012
	 */
	public void loadNotChinese() {
		
	}
	
	/**
	 * 载入词典
	 * @since Nov 21, 2012
	 */
	public void loadDict(String un2yin, String wordYinFile) {
		wp.loadUC2PY(un2yin);
		//wp.loadWordPinyinDict(wordYinFile);	
	}
	
	/**
	 * 
	 * @since Nov 21, 2012
	 * @param outputWord 是否输出词
	 * @param wordAnnotate 是否按词标注（先分词，然后对每个词再标注拼音，适用于以分词的语料）
	 * @param characterMultiple 对单字进行多音标注
	 * @param wordMultiple 对多字词进行多音标注
	 */
	public void setOptions(boolean outputWord, boolean wordAnnotate, 
			boolean characterMultiple, boolean wordMultiple) {
		this.outputWord = outputWord; 
		this.wordAnnotate = wordAnnotate; 
		this.characterMultiple = characterMultiple; //单字词给多注音
		this.wordMultiple = wordMultiple; 
	}
	
		
	/** 最少词分词后，注音 */
	public void MinimumWordNumber(String inFileName, String outFileName) {
		
		Matching newMatch = new MinimumWordNumber();
		newMatch.setDict(wp.wpPair);
		try {
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
	        String line = null;
	        FileWriter writer = new FileWriter(outFileName);

	        int count = 0;
	        while((line = reader.readLine()) != null) {
	        	count++;
	        	if(count%1000 == 0) {
	        		System.out.println(count + " ");
	        		if(count %10000 == 0)
	        			System.out.println();
	        	}
	        	ArrayList<String> yins = getSentencePinyins(newMatch, line);
	        	for(String yin : yins) {
	        		if(outputWord)
	        			writer.write(line.trim() + "\t" + yin + "\n");
	        		else
	        			writer.write(yin+"\n");
	        	}
	        }
			
			reader.close();
			writer.close();
		}
		catch (IOException e) {
			
		}		
	}

	
	/** 后向最大匹配 */
	public void BackwardMaximumMatching(String inFileName, String outFileName) {
		
		Matching newMatch = new BackwardMaximumMatching();
		newMatch.setDict(wp.wpPair);
		try {
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
	        String line = null;
	        FileWriter writer = new FileWriter(outFileName);
	        
	        int count = 0;
	        
	        while((line = reader.readLine()) != null) {
	        	count++;
	        	if(count%1000 == 0) {
	        		System.out.println(count + " ");
	        		if(count %10000 == 0)
	        			System.out.println();
	        	}
	        	ArrayList<String> yins = getSentencePinyins(newMatch, line);
	        	for(String yin : yins)
	        		writer.write(yin + "\n");
	        }
			
			reader.close();
			writer.close();
		}
		catch (IOException e) {
			
		}		
	}
	
	
	/**
	 * 去掉句子中空格，tab等标示，合为一个完整的句子
	 * @since Nov 21, 2012
	 * @param line
	 * @return
	 */
	public String trimLine(String line) {
		
		String[] strs = line.split("\\s+");
		StringBuffer sbuf = new StringBuffer();
		for(String str: strs)
			sbuf.append(str);
		return sbuf.toString();
		
	}
	
	
	/**
	 * 去掉句子中空格，tab等标示，将词放入list中
	 * @since Nov 21, 2012
	 * @param line
	 * @return
	 */
	public ArrayList<String> splitLine(String line) {
		
		ArrayList<String> strList = new ArrayList<String>();
		String[] strs = line.split("\\s+");
		for(String str: strs)
			strList.add(str);
		return strList;
		
	}
	
	/**
	 * 根据得到的句子和匹配方法, 分割词
	 * @since Nov 21, 2012
	 * @param newMatch
	 * @param line
	 * @return
	 */
	public ArrayList<String> getSentencePinyins(Matching newMatch, String line) {
		
		ArrayList<String> words = new ArrayList<String>(); //所有的词
    	if(!wordAnnotate) {
    		String sentence = trimLine(line);
    		words.add(sentence);
    	}
    	else {
    		words = splitLine(line);
    	}
    	
    	return getWordsPinyins(newMatch, words);
  
	}
	
	/** 
	 * 根据输入词序列，得到对应的拼音，然后再把拼音合并，得到词序列的拼音
	 * @since Nov 21, 2012
	 * @param subWords
	 * @param words
	 * @return
	 */	 
	public ArrayList<String> getWordsPinyins(Matching newMatch, ArrayList<String> words) {
		
		ArrayList<StringBuffer> sbufList = new ArrayList<StringBuffer>();
		sbufList.add(new StringBuffer());
		//每个词
		for(String word : words) {
			//每个子词
			ArrayList<String> yins = getWordPinyins(newMatch, word);
			int currentYinSize = yins.size();
			int currentSize = sbufList.size();
			for(int i=1; i<currentYinSize; i++) {
				for(int j=0; j<currentSize; j++) {
					sbufList.add(new StringBuffer(sbufList.get(j).toString()));
				}
			}
			for(int i=0; i<currentYinSize; i++) {
				for(int j=0; j<currentSize; j++) {
					int currentPosition = i*currentSize + j;
					sbufList.get(currentPosition).append(yins.get(i) + "\t");
				}
			}
		}
		 
		ArrayList<String> allYins = new ArrayList<String>();
		for(StringBuffer sbuf : sbufList) {
			allYins.add(sbuf.toString().trim());
		}
		return allYins;
		
	}
	
	
	public ArrayList<String> getWordPinyins(Matching newMatch, String word) {
		
		ArrayList<String> subWords = newMatch.segment(word);
		
		ArrayList<StringBuffer> sbufList = new ArrayList<StringBuffer>();
		sbufList.add(new StringBuffer());
		int subWordSize = subWords.size();
		for(int n=0; n<subWordSize; n++) {
			String subWord = subWords.get(n);
			List<String> yins = wp.getYins(subWord);
			if(!characterMultiple && word.length()==1) { //单字，并且单字
				yins = yins.subList(0, 1);
			}
			if(!wordMultiple && word.length() > 1) {
				yins = yins.subList(0, 1);
			}
			int currentYinSize = yins.size();
			int currentSize = sbufList.size();
			for(int i=1; i<currentYinSize; i++) {
				for(int j=0; j<currentSize; j++) {
					sbufList.add(new StringBuffer(sbufList.get(j).toString()));
				}
			}
			for(int i=0; i<currentYinSize; i++) {
				for(int j=0; j<currentSize; j++) {
					int currentPosition = i*currentSize + j;
					sbufList.get(currentPosition).append(yins.get(i) + " ");
				}
			}
		}
		ArrayList<String> allYins = new ArrayList<String>();
		for(StringBuffer sbuf : sbufList) {
			allYins.add(sbuf.toString().trim());
		}		
		return allYins;
		
	}
	
		
	
	public static void main(String[] args) {
		
		String un2yin = "/home/tm/disk/disk1/pinyin/unicode_to_hanyu_pinyin.txt";
		String wordYinFile = "/home/tm/disk/disk1/pinyin/fctix.word.pinyin";
		
		
		String inFileName = "/home/tm/3500.word";//disk/disk1/pinyin2character/Lcmc/data/p2c-data/train.seg.words";//lm/dict/3500";
		String outFileName = "/home/tm/3500.pinyin";//disk/disk1/pinyin2character/Lcmc/data/p2c-data/train.seg.pinyins";
		AnnotateSentence at = new AnnotateSentence();
		at.setOptions(true, true, true, false);
		at.loadDict(un2yin, wordYinFile);
		at.MinimumWordNumber(inFileName, outFileName);
	}
	

}
