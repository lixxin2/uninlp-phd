package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;


/**
 * 音到字的映射
 * @author Xinxin Li
 * @since Nov 9, 2012
 */
public class PyWordPair implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	HashMap<String, ArrayList<String>> pairs;
	
	public PyWordPair() {
		pairs = new HashMap<String, ArrayList<String>>();
	}
	
	public HashMap<String, ArrayList<String>> getPair() {
		return pairs;
	}
		
	
	/**
	 * 返回拼音对应的词
	 * @since Oct 23, 2012
	 * @param pinyin
	 * @return
	 */
	public ArrayList<String> getWords(String pinyin) {
		if(pairs.containsKey(pinyin)) {
			return pairs.get(pinyin);
		}			
		else if(pinyin.split("\\s+").length == 1) { //单字
			if(pinyin.matches("[a-zA-Z]+"))
				System.out.println("no words for pinyin: " + pinyin);
			ArrayList<String> yins = new ArrayList<String>();
			yins.add(pinyin);
			return yins;			
		}		
		else {
			//System.out.println("no words for pinyin: " + pinyin);
			return null;
		}
	}
	
	/**
	 * 从训练文件中统计
	 * @since Oct 23, 2012
	 * @param trainFile
	 */
	public void read(String trainFile) {
		
		PyCharReader reader = new PyCharReader();
		int sentenceSize = 0;
    	System.out.print("Read tags from file " +trainFile + "...");
		reader.startReading(trainFile);
		PyCharInstance instance = reader.getNext();
		while(instance != null) {
			sentenceSize++;
			int length = instance.length;
			String[] pinyin = instance.characterYins;
			String[] words = instance.characters;
			for(int i=0; i<words.length; i++)
				putYinWord(pinyin[i], words[i]);
			
		    instance = reader.getNext();
		}
		System.out.println("done");

	}
	
	/**存入新词和词音*/
	public void putYinWord(String yin, String word) {
		if(!pairs.containsKey(yin)) {
			ArrayList<String> words = new ArrayList<String>();
			words.add(word);
			pairs.put(yin, words);
		}
		else {
			ArrayList<String> words = pairs.get(yin);
			if(!words.contains(word)) {
				words.add(word);				
			}
		}
	}
	
	/** 读取单字词典
	 * 
	 * <p> a	啊 阿
	 * <p> ai 	艾 爱
	 * @since Nov 3, 2012
	 * @param pinyinWordFile
	 * @throws IOException
	 */
	public void loadPinyinWord(String pinyinWordFile) throws IOException {
		System.out.print("load pinyin2word file: " + pinyinWordFile + "... ");
		String fileEncoding = FileEncoding.getCharset(pinyinWordFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pinyinWordFile), fileEncoding));
        String line = null;
        while((line=reader.readLine()) != null) {
        	line = line.trim();
        	String[] yinwords = line.split("\t");
        	String yin = yinwords[0];
        	String[] wordsPart = yinwords[1].split(" ");
        	ArrayList<String> words = new ArrayList<String>(); 
        	for(String word : wordsPart) {
        		words.add(word);
        	}
        	pairs.put(yin, words);        	
        }
		reader.close();
		System.out.println("done");
	}
	
	
	/**
	 * 读取词音词典
	 * 
	 * <p> 啊 a
	 * <p> 测试 ce shi
	 * @since Nov 3, 2012
	 * @param pinyinWordFile
	 * @throws IOException
	 */
	public void loadWordPinyin(String wordPinyinFile) throws IOException {
		System.out.print("load word2pinyin file: " + wordPinyinFile + "... ");
		String fileEncoding = FileEncoding.getCharset(wordPinyinFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordPinyinFile), fileEncoding));
        String line = null;
        while((line=reader.readLine()) != null) {
        	line = line.trim();
        	String[] wordYins = line.split("\t");
        	String word = wordYins[0];
        	String yin = wordYins[1];
    		ArrayList<String> words = new ArrayList<String>();
        	if(pairs.containsKey(yin)) 
        		words = pairs.get(yin);
        	if(!words.contains(word)) {
        		words.add(word);
        		pairs.put(yin, words);     
        	}
        }
		reader.close();
		System.out.println("done");
	}
	
	
	
	
	
	
	
	public void save(String pinyinWordFile) throws IOException {
		FileWriter writer = new FileWriter(pinyinWordFile);
		
		Iterator<Entry<String, ArrayList<String>>> iter = pairs.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = iter.next();
			StringBuffer sb = new StringBuffer();
			String yin = entry.getKey();
			sb.append(yin+"\t");
			ArrayList<String> words = entry.getValue();
			for(String word : words)
				sb.append(word + " ");
			String line = sb.toString().trim();
			writer.write(line + "\n");
		}
		writer.close();
		
	}
	
	
	/** 获取语音对应的词的数目 */
	public int getMaximumWordNumber() {
		
		Iterator<Map.Entry<String, ArrayList<String>>> iter = pairs.entrySet().iterator();
		
		int number = 0;
		String yin = "";
		
		while(iter.hasNext()) {
			
			Map.Entry<String, ArrayList<String>> entry = iter.next();
			
			String tmpYin = entry.getKey();
			ArrayList<String> words = entry.getValue();
			
			if(words.size() > number) { 
				number = words.size();
				yin = tmpYin;
			}
			
		}
		
		System.out.println(yin);
		
		return number;
		
	}
	
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		PyWordPair pair = new PyWordPair();
		String file = "/home/tm/disk/disk1/pinyin2character/train-UTF-8-2";
		String pinyinWordFile = "/home/tm/3500.pinyin";//disk/disk1/pinyin2character/train-pinyinWord";
		//pair.read(file);
		//pair.save(pinyinWordFile);
		pair.loadWordPinyin(pinyinWordFile);
		int number = pair.getMaximumWordNumber();
		System.out.println(number);
		
	}
	
	

}
