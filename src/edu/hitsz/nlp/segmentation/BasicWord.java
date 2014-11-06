package edu.hitsz.nlp.segmentation;

import java.io.Serializable;

/**
 * 词的基本结构，包含词本身和它的位置（起始和结束位置）
 * @author tm
 * 2011.12.1
 */
public class BasicWord implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String word;
	private int start;
	private int end;

	public BasicWord(String tmpWord, int tmpStart, int tmpEnd){
		word = tmpWord;
		start = tmpStart;
		end = tmpEnd;
	}

	public BasicWord(){

	}

	public String getWord(){
		return this.word;
	}

	public int getStart(){
		return this.start;
	}

	public int getEnd(){
		return this.end;
	}

	public boolean equals(BasicWord newWord){
		return word.equals(newWord.getWord()) && start == newWord.getStart() && end ==newWord.getEnd();
	}

}
