package edu.hitsz.nlp.language.english;

import java.util.ArrayList;

public class Utility {

	
	/**
	 * 大写转小写
	 * @since Aug 13, 2012
	 * @param word
	 * @return
	 */
	public static String upper2lower(String word) {
		int length = word.length();
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<length; i++) {
			char c = word.charAt(i);
			if(c>='A' && c<='Z')
				c +=32;
			sb.append(c);	
		}
		return sb.toString();		
	}
	

	
	/** 
	 * 词转字母串
	 */
	public static String word2letter(String word) {
		int length = word.length();
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<length; i++) {
			String c = word.substring(i,i+1);
			if(c.matches("[a-zA-Z]"))
				sb.append(c);	
		}
		return sb.toString();		
	}
	
	/**
	 * 词转字母数组
	 */
	public static ArrayList<String> word2letters(String word) {
		ArrayList<String> letters = new ArrayList<String>();
		int length = word.length();
		for(int i=0; i<length; i++) {
			String c = word.substring(i,i+1);
			if(c.matches("[a-zA-Z]"))
				letters.add(c);	
		}
		return letters;		
	}
	
	/**
	 * 词数组转字母数组
	 * @param sent
	 * @return
	 */
	public static ArrayList<String> words2letters(ArrayList<String> words) {
		ArrayList<String> letters = new ArrayList<String>();
		for(String word : words)
			letters.addAll(word2letters(word));
		return letters;
	}
	
	
}
