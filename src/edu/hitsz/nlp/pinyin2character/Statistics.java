package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.chinese.ChineseWord;


/** 统计语料的句子数，词数，字数，词频数 */
public class Statistics {
	
	HashMap<String, Integer> wordMap;
	
	public Statistics() {
		wordMap = new HashMap<String, Integer>();
	}
	
	public void loadWordMap(String fileName) {
		
		try {
			String fileEncoding = FileEncoding.getCharset(fileName);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
	        
	        String line = null;
	        while( (line=reader.readLine()) != null) {
	        	
	        	if(line.length() > 0) {
	        		
	        		String[] words = line.split("\\s+");
	        		wordMap.put(words[0], 1);	        		
	        	}
	        	
	        }
	        reader.close();
	    }
		catch (IOException e) {
			
		}
	}
	
	
	
	public void stat(String fileName) {
		
		int sentenceNumber = 0;
		int allWordNumber = 0;
		int chnWordNumber = 0;
		int charWordNumber = 0;	
		int OOVWordNumber = 0;
		
		try {
			String fileEncoding = FileEncoding.getCharset(fileName);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
	        
	        String line = null;
	        
	        while( (line=reader.readLine()) != null) {
	        	
	        	if(line.length() > 0) {
	        		sentenceNumber++;
	        		
	        		String[] words = line.split("\\s+");
	        		for(String word : words) {
	        			allWordNumber++;
	        			if(word.matches(ChineseWord.hanzisRegex)) {
	        				chnWordNumber++;
	        				charWordNumber+=word.length();
	        				if(!wordMap.containsKey(word))
	        					OOVWordNumber++;
	        			}	        			
	        		}	        		
	        	}
	        	
	        }
	        
	        reader.close();
	        
	        System.out.println("sentenceNumber:"+sentenceNumber);
	        System.out.println("allWordNumber:"+allWordNumber);
	        System.out.println("chnWordNumber:"+chnWordNumber);
	        System.out.println("charWordNumber:"+charWordNumber);		
	        System.out.println("OOVWordNumber:"+OOVWordNumber+","+OOVWordNumber/(double)allWordNumber);
		}
		catch (IOException e) {
			
		}
	}
	
	
	public static void main(String[] args) {
		
		String wordFileName = "/home/tm/disk/disk1/lm/dict/wordlist5";
		String fileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/test.seg";
		Statistics s = new Statistics();
		s.loadWordMap(wordFileName);
		s.stat(fileName);
		
	}

}
