package edu.hitsz.nlp.segpos;

import java.io.Serializable;
import java.util.ArrayList;

import edu.hitsz.nlp.segmentation.BasicWord;

/**
 * 词的基本结构，包含词、词性和它的位置（起始和结束位置）
 * @author Xinxin Li
 * Dec 2, 2011
 */
public class BasicWordPos extends BasicWord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String pos;
	private double prob;
	private BasicWordPos next;
	private BasicWordPos prev;

	public BasicWordPos(String tmpWord, String tmpPos, int tmpStart, int tmpEnd) {
		super(tmpWord, tmpStart, tmpEnd);
		pos = tmpPos;
		prev = null;
		next = null;
	}
	
	public BasicWordPos(String tmpWord, String tmpPos, int tmpStart, int tmpEnd, double prob) {
		this(tmpWord, tmpPos, tmpStart, tmpEnd);		
		this.prob = prob;		
	}

	public BasicWordPos(){
		super();
	}

	public String getPos(){
		return pos;
	}
	
	public double getProb(){
		return prob;
	}

	public void setPrev(BasicWordPos prev){
		this.prev = prev;
	}

	public void setNext(BasicWordPos next){
		this.next = next;
	}

	public BasicWordPos getPrev(){
		return this.prev;
	}

	public BasicWordPos getNext(){
		return this.next;
	}


	public boolean equals(BasicWordPos newWordPos){
		return super.equals(newWordPos) && pos.equals(newWordPos.pos);
	}

	public boolean wordEquals(BasicWordPos newWordPos){
		return super.equals(newWordPos);
	}

	public boolean isIn(ArrayList<BasicWordPos> allWordPos){
		for(BasicWordPos wordPos : allWordPos){
			if(equals(wordPos)){
				return true;
			}
		}
		return false;
	}

	public boolean isWordIn(ArrayList<BasicWordPos> allWordPos){
		for(BasicWordPos wordPos : allWordPos){
			if(wordEquals(wordPos)){
				return true;
			}
		}
		return false;
	}




}
