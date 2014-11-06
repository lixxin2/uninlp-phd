package edu.hitsz.nlp.struct;

import java.util.ArrayList;

/**
 * 生句子，只包括句子中的词，没有其他标识
 * @author tm
 *
 */
public class RawSentence{

	ArrayList<String> tokens;

	public RawSentence(){
		tokens = new ArrayList<String>();
	}

	/**
	 * 解析,分割中文句子
	 * @param line
	 */
	public void parseChinese(String inputLine){
		for(int i=0; i<inputLine.length(); i++)
			tokens.add(inputLine.substring(i,i+1));
	}

	/**
	 * 解析,分割英文句子
	 * @param line
	 */
	public void parseEnglish(String inputLine){
		String[] words = inputLine.split(" ");
		for(int i=0; i<words.length; i++)
			tokens.add(words[i]);
	}

	/**
	 * 解析,分割英文句子
	 * @param line
	 */
	public void parseEnglishTab(String inputLine){
		String[] words = inputLine.split("\t");
		for(int i=0; i<words.length; i++)
			tokens.add(words[i]);
	}





}
