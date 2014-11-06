package edu.hitsz.nlp.lexicon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.chinese.ChineseWord;

public class Xdhycd {
	
	public static void extract() {
		
		String inFileName = "/media/study/corpora/Lexicon/现代汉语词典/《现代汉语词典》商务印书馆-UTF8.txt";
		String outFileName = "/media/study/corpora/Lexicon/现代汉语词典/wordlist2";
		
		try {
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
	        FileWriter writer = new FileWriter(outFileName);
			
	        ArrayList<String> words = new ArrayList<String>();
	        
	        String line = null;
	        while( (line = reader.readLine()) != null ) {
	        	line = line.trim();
	        	if(line.length() > 0) {
	        		while(line.length() > 0 && line.startsWith("　"))
	        			line = line.substring(1);
	        		if(line.startsWith("*")) {
	        			String singleWord = line.substring(1,2);
	        			words.add(singleWord);
	        		}
	        		else if(line.startsWith("【")) {
	        			int first = line.indexOf("【");
	        			int last = line.indexOf("】");
	        			if(first >= 0 && first < last) {
	        				String word = line.substring(first+1, last);
	        				if(!word.matches(ChineseWord.hanzisRegex))
	        					System.out.println(word);
	        				else
	        					words.add(word);
	        			}
	        		}        		
	        	}	        	
	        }
	        
	        for(String word : words) {
	        	writer.write(word + "\n");
	        }
			
			reader.close();
			writer.close();
			
			
		}
		catch (IOException e) {
			
		}
		
		
		
	}
	
	public static void main(String[] args) {
		Xdhycd.extract();
	}

}
