package edu.hitsz.nlp.asr.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;

public class Oov {

	HashMap<String, Integer> goldWords;
	HashMap<String, Integer> testWords;
	HashMap<String, Integer> oovWords;
	int allWordNumber;
	int alloovWordNumber;
	
	public Oov() {
		
		goldWords = new HashMap<String, Integer>();
		testWords = new HashMap<String, Integer>();
		oovWords = new HashMap<String, Integer>();
		allWordNumber = 0;
		alloovWordNumber = 0;
		
	}
	
	
	public void readDict(String fileName) {
		String encoding = FileEncoding.getCharset(fileName);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
	        String line = null;
	        while((line = reader.readLine()) != null) {
	        	line = line.trim();
	        	if(line.length()>0)
	        		goldWords.put(line, 1);	        	
	        }
	        reader.close();
			
		}
		catch (IOException e) {
			
		}
		
	}
	
	
	
	
	public void evalText(String textFileName) {
		String encoding = FileEncoding.getCharset(textFileName);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFileName), encoding));
	        String line = null;
	        while((line = reader.readLine()) != null) {
	        	String[] words = line.trim().split("[ \t]");
	        	if(words.length>0) {
	        		
	        		allWordNumber += words.length;
	        		for(String word : words) {
	        			if(!testWords.containsKey(word))
	        				testWords.put(word, 1);
	        			if(!goldWords.containsKey(word)){
	        				alloovWordNumber+=1;
	        				if(!oovWords.containsKey(word))
	        					oovWords.put(word, 1);
	        				
	        			}	        			
	        		}
	        	}
	        }
	        reader.close();
			
		}
		catch (IOException e) {
			
		}
	}
	
	
	public void output() {
		
		System.out.println("allWordNumber: "+allWordNumber);
		System.out.println("alloovWordNumber: "+alloovWordNumber);
		System.out.println("oov rate1: " + alloovWordNumber/(double)allWordNumber);
		System.out.println("goldWords: " +goldWords.size());
		System.out.println("testWords: "+testWords.size());
		System.out.println("oovWords: "+oovWords.size());
		System.out.println("oov rate2: " + oovWords.size()/(double)testWords.size());
	}
	
	
	
	
	public static void main(String[] args) {
		
		Oov oov = new Oov();
		oov.readDict("/home/tm/windows/asr/eval/oov/100k.vocab");
		oov.evalText("/home/tm/windows/asr/eval/speechtext.text");
		oov.output();
		
		
	}
	
	
	
	
}
