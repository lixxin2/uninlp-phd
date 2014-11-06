package edu.hitsz.nlp.mstjoint;

import edu.hitsz.nlp.segpos.BasicWordPos;

public class WordPos extends BasicWordPos {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int leftIndex;
	public int rightIndex;
	public int index;
	
	public WordPos(String tmpWord, String tmpPos, int tmpStart, int tmpEnd){
		super(tmpWord, tmpPos, tmpStart, tmpEnd);
	}
	
	public WordPos(String tmpWord, String tmpPos, int tmpStart, int tmpEnd, double prob){
		super(tmpWord, tmpPos, tmpStart, tmpEnd, prob);
	}
	
	public WordPos(String tmpWord, String tmpPos, int tmpStart, int tmpEnd, double prob, 
			int leftIndex, int rightIndex, int index){
		super(tmpWord, tmpPos, tmpStart, tmpEnd, prob);
		this.leftIndex = leftIndex;
		this.rightIndex = rightIndex;
		this.index = index;
	}

}
