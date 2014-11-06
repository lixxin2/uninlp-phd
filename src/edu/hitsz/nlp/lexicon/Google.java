package edu.hitsz.nlp.lexicon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.chinese.ChineseWord;
import edu.hitsz.nlp.language.chinese.SimpleTraditionalConversion;

public class Google {
	
	private HashMap<String, Integer> otherSimpWords;
	
	
	public Google() {
		otherSimpWords = new HashMap<String, Integer>(); 
	}
	
	
	/**
	 * 读取已有的简体词表
	 * @since Nov 28, 2012
	 * @param inFileName
	 */
	public void readSimpWords(String inFileName) {
		
		try {
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
	        
			String line = null;
			while ( (line = reader.readLine()) != null ) {
				line = line.trim();
				if(line.length() > 0) {
					String[] subs = line.split("\\s+");
					otherSimpWords.put(subs[0], 1);				
				}
			}
			
			reader.close();
		}
		catch (IOException e) {
			
		}
	}
	
	
	/**
	 * 检测google 1gram中的繁体词，如果已有词中已有,则删除
	 * @since Nov 28, 2012
	 * @param googleFileName
	 * @param simpTradMapFile
	 * @param outFileName
	 */
	public void convertTraditionalWords(String googleFileName,
			String simpTradMapFile, String outFileName) {
		
		//读取繁简字map
		SimpleTraditionalConversion st = new SimpleTraditionalConversion();
		st.loadTrad2SimpMap(simpTradMapFile);
				
		HashMap<String, Integer> simpWords = new HashMap<String, Integer>();
		HashMap<String, Integer> probTradWords = new HashMap<String, Integer>();
		HashMap<String, Integer> tradWords = new HashMap<String, Integer>();
		
		try {
			String fileEncoding = FileEncoding.getCharset(googleFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(googleFileName), fileEncoding));
	        //读取simpWords和probTrabWords
			String line = null;
			while ( (line = reader.readLine()) != null ) {
				line = line.trim();
				if(line.length() > 0) {
					String[] wordNumber = line.split("\t");
					ArrayList<String> words = st.getSimpWords(wordNumber[0]);
					if(words.size() == 1) //如果是简体词
						simpWords.put(wordNumber[0], Integer.parseInt(wordNumber[1]));
					else //如果可能是繁体词
						probTradWords.put(wordNumber[0], Integer.parseInt(wordNumber[1]));
				}
			}
			//查找probTrabWords是否有对应的简体字，如果没有，则存储tradWords
			Iterator<Entry<String, Integer>> iter = probTradWords.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				String tradWord = entry.getKey();
				int number = entry.getValue();
				ArrayList<String> words = st.getSimpWords(tradWord);
				boolean isSimp = false;  //是不是有对应的简体词
				for(String word : words) {
					if(simpWords.containsKey(word) || otherSimpWords.containsKey(word)) {
						isSimp = true;
						break;
					}					
				}
				if(!isSimp)
					tradWords.put(tradWord, number);				
			}			
			reader.close();
			
			//存储所有simpWords和tradWords
			FileWriter writer = new FileWriter(outFileName);
			Iterator<Entry<String, Integer>> iter2 = simpWords.entrySet().iterator();
			int count = 0;
			while(iter2.hasNext()) {
				count++;
				Entry<String, Integer> entry = iter2.next();
				String simpWord = entry.getKey();
				int number = entry.getValue();
				writer.write(simpWord + "\t" + number + "\n");
			}
			System.out.println(count);
			Iterator<Entry<String, Integer>> iter3 = tradWords.entrySet().iterator();
			while(iter3.hasNext()) {
				Entry<String, Integer> entry = iter3.next();
				String tradWord = entry.getKey();
				int number = entry.getValue();
				writer.write(tradWord + "\t" + number + "\n");
			}
			writer.close();
			
		}
		catch (IOException e) {
			
		}		
		
	}
	
	
	
	
	public static void main() {
		
		
		
		
	}
	
	
	public static void main(String[] args) {
		
		String simpleFileName = "/home/tm/Documents/lm/wordlist2";
		String simpleFileName2 = "/home/tm/disk/disk1/pinyin2character/dict/fctix.word.pinyin";
		
		String googleFileName = "/home/tm/Documents/lm/ngrams.2"; 
		String simpTradMapFile = "/home/tm/Documents/lm/simptrad.txt";
		String outFileName = "/home/tm/Documents/lm/google.simple.all";
		 
		Google g = new Google();
		g.readSimpWords(simpleFileName);
		g.readSimpWords(simpleFileName2);
		g.convertTraditionalWords(googleFileName, simpTradMapFile, outFileName);
		
		//System.out.println("號".matches(ChineseWord.hanziRegex));
		
		
	}

}
