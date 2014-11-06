package edu.hitsz.nlp.segpos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.language.chinese.ChineseWord;

public class Statistics {

	
	
	
	public static void statNoChinese() {
		/*
		String fileName = "/home/tm/disk/disk1/segpos/large-train-wp";
		String fileEncoding = FileEncoding.getCharset(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
		
		ArrayList<String> puncts = new ArrayList<String>();
		for(String s : CTB.punctuations)
			puncts.add(s);
		
		
		String line = null;
		while((line= reader.readLine()) != null) {
			
			if(line.trim().length() > 0) {
				String word = line.split("[ \t]")[0];
				if(!word.matches(ChineseWord.hanziRegex) && !puncts.contains(word))
					System.out.println(word);
			}
			
			
			
		}
		reader.close();
		*/
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("1.2".matches("[0-9]+\\.?[0-9]?"));
		
		
		
		
	}
	
	
}
