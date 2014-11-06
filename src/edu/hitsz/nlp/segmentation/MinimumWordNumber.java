package edu.hitsz.nlp.segmentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.corpus.ctb.CTB;

/**
 * 最少分词，最少切分
 * @author Xinxin Li
 * Dec 2, 2011
 */
public class MinimumWordNumber extends Matching{

	private HashMap dict;
	private int maximumLength = 20;


	public MinimumWordNumber(){
		dict = new HashMap();
	}

	/**
	 * 设置词典
	 * @param tmpDict
	 */
	public void setDict(HashMap tmpDict){
		dict = tmpDict;
	}

	public void setMaximumLength(int length){
		maximumLength = length;
	}

	/**
	 * 分词，最少分词，即分词的个数最少
	 * @param sentence
	 * @return
	 */
	private ArrayList<ArrayList<BasicWord>> segmentSentence(String sentence){
		return segmentSentence(sentence, 0, sentence.length());
	}

	/**
	 * 分词，最少分词，即分词的个数最少
	 * @param sentence
	 * @param sentenceStart
	 * @param sentenceEnd
	 * @return
	 */
	private ArrayList<ArrayList<BasicWord>> segmentSentence(String sentence, int sentenceStart, int sentenceEnd){
		//所有可能词序列
		ArrayList<ArrayList<ArrayList<BasicWord>>> allWords = new ArrayList<ArrayList<ArrayList<BasicWord>>>();
		//每个字对应的词序列长度
		ArrayList<Integer> shortestLength = new ArrayList<Integer>();
		//第一个字前
		for(int i=0; i<=sentenceStart; i++){
			ArrayList<BasicWord> emptyWords = new ArrayList<BasicWord>();
			ArrayList<ArrayList<BasicWord>> emptyWordsList = new ArrayList<ArrayList<BasicWord>>();
			emptyWordsList.add(emptyWords);
			allWords.add(emptyWordsList);
			shortestLength.add(0);
			continue;
		}
		//递归每个字,对于每个字
		for(int i=sentenceStart+1; i<=sentenceEnd; i++){
			//可能的前一个起始字，判断是否有一个词从这个字到当前字
			int j=Math.max(i-maximumLength, sentenceStart);
			//假设最长长度
			int maxLength = i-sentenceStart;
			//记录前面所有可能最少分词的前一个词的位置
			//ArrayList<Integer> beforeWordPosition = new ArrayList<Integer>();
			//测试每一个从当前字到前
			for(int k=j; k<i; k++){
				//前一个词保存的所有词的个数
				int beforeLength = shortestLength.get(k);
				String currentWord = sentence.substring(k,i);
				if((k==i-1 || dict.containsKey(currentWord)) && beforeLength+1<=maxLength){
					maxLength = beforeLength+1;
					//beforeWordPosition.add(k);
				}
			}
			shortestLength.add(maxLength);
			ArrayList<ArrayList<BasicWord>> currentWordsList = new ArrayList<ArrayList<BasicWord>>();
			for(int k=j; k<i; k++){
				//前一个词保存的所有词的个数
				int beforeLength = shortestLength.get(k);
				String word = sentence.substring(k,i);
				if((k==i-1 || dict.containsKey(word)) && beforeLength+1==maxLength){
					ArrayList<ArrayList<BasicWord>> beforeWordsList = allWords.get(k);
					for(ArrayList<BasicWord> beforeWords : beforeWordsList){
						ArrayList<BasicWord> currentWords = new ArrayList<BasicWord>();
						currentWords.addAll(beforeWords);
						BasicWord currentWord = new BasicWord(word,k,i);
						currentWords.add(currentWord);
						currentWordsList.add(currentWords);
					}
				}
			}
			/*
			for(int k: beforeWordPosition){
				String word = sentence.substring(k,i);
				ArrayList<ArrayList<BasicWord>> beforeWordsList = allWords.get(k);
				for(ArrayList<BasicWord> beforeWords : beforeWordsList){
					ArrayList<BasicWord> currentWords = new ArrayList<BasicWord>();
					currentWords.addAll(beforeWords);
					BasicWord currentWord = new BasicWord(word,k,i);
					currentWords.add(currentWord);
					currentWordsList.add(currentWords);
				}
			}
			*/
			allWords.add(currentWordsList);
		}
		return allWords.get(sentenceEnd);
	}

	
	/**
	 * 最少分词
	 * @param sentence
	 * @return 多个分词序列
	 */
	public ArrayList<ArrayList<String>> segmentAll(String sentence){
		ArrayList<ArrayList<BasicWord>> allWords = segmentSentence(sentence);
		ArrayList<ArrayList<String>> allWordString = new ArrayList<ArrayList<String>>();
		for(ArrayList<BasicWord> basicWords : allWords){
			ArrayList<String> wordString = new ArrayList<String>();
			for(BasicWord basicWord : basicWords){
				wordString.add(basicWord.getWord());
			}
			allWordString.add(wordString);
		}
		return allWordString;
	}
	
	
	public ArrayList<String> segment(String sentence){
		ArrayList<ArrayList<BasicWord>> allWords = segmentSentence(sentence);
		ArrayList<ArrayList<String>> allWordString = new ArrayList<ArrayList<String>>();
		for(ArrayList<BasicWord> basicWords : allWords){
			ArrayList<String> wordString = new ArrayList<String>();
			for(BasicWord basicWord : basicWords){
				wordString.add(basicWord.getWord());
			}
			allWordString.add(wordString);
		}
		return allWordString.get(0);
	}

	
	/** 读取字典 */
	public void loadDict(String inFileName) {
		
		try {
			String encoding = FileEncoding.getCharset(inFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFileName)), encoding)); 
			
			String line = null;
			
			while( (line = reader.readLine()) != null ) {
				
				line = line.trim();
				if(line.length() > 0 ) {
					String[] subs = line.split("\\s+");
					dict.put(subs[0], 1);					
				}
				
			}
			reader.close();
			
		}
		catch (IOException e) {
			
		}		
		
	}
	
	
	/**
	 * 分割文件
	 * @since Nov 29, 2012
	 * @param inFileName
	 * @param outFileName
	 */
	public void segmentFile(String inFileName, String outFileName) {
		
		try {
			String encoding = FileEncoding.getCharset(inFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFileName)), encoding)); 
			FileWriter writer = new FileWriter(outFileName);
			
			String line = null;
			int count = 0;
			
			while( (line = reader.readLine()) != null ) {
				
				count++;
				if(count % 100000 == 0)
					System.out.println(count+"...");
				
				line = line.trim();
				if(line.length() > 0 ) {
					String[] lines = line.split("[。？！，；：]");
					for(String oneLine : lines) {		
						do {
							String firstLine = oneLine;
							if(oneLine.length() > 100) {
								firstLine = oneLine.substring(0, 100);
								oneLine = oneLine.substring(100);
							}
							else
								oneLine = "";
							ArrayList<String> words = segment(firstLine);
							StringBuffer sbuf = new StringBuffer();
							for(String word : words)
								sbuf.append(word + " ");
							writer.write(sbuf.toString().trim() + "\n");	
						}
						while (oneLine.length() > 0);
					}
				}
				
			}
			reader.close();
			writer.close();
			
		}
		catch (IOException e) {
			
		}	
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Long a = System.nanoTime();
		MinimumWordNumber newMatch = new MinimumWordNumber();
		/*
		HashMap<String, Integer> newDict = new HashMap<String, Integer>();
		newDict.put("我们", 1);
		newDict.put("中国", 1);
		newDict.put("国家", 1);
		newDict.put("家庭", 1);		
		newMatch.setDict(newDict);
		String sentence = "我们爱中国家庭,我们国家庭好";
		ArrayList<ArrayList<String>> words = newMatch.segmentAll(sentence);
		System.out.println(words);
		*/
		newMatch.loadDict("./words");
		newMatch.segmentFile("./raw", "./seg");
		Long b = System.nanoTime();
		System.out.println(b-a);
	}

}
