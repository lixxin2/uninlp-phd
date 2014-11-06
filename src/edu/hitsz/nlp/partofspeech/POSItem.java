package edu.hitsz.nlp.partofspeech;

import java.util.ArrayList;

import edu.hitsz.nlp.util.Array;


public class POSItem {

	public int s;
	public String form, pos;
	public double prob; //所有的权重和
	public double curProb; //当前词的权重和
	public POSFeatureVector fv;
	public POSItem left;
	public POSItem right;

	/**
	 * 
	 * @since 2012-3-2
	 * @param s 新词位置
	 * @param form
	 * @param pos 新词词性
	 * @param score 概率
	 * @param fv 特征
	 * @param left 前一个词
	 */
	public POSItem(int s, String form, String pos,
			double prob,
			POSFeatureVector fv,
			POSItem left){
		this.s = s;
		this.form = form;
		this.pos = pos;
		this.prob = prob;
		this.fv = fv;
		this.left = left;
		this.curProb = left != null ? prob - left.prob : prob;
	}	

	/**
	 * 
	 * @since Apr 17, 2012
	 * @param s 新词开始位置
	 * @param form
	 * @param pos 新词词性
	 * @param left 前一个词
	 */
	public POSItem(int s, String form, String pos, POSItem left) {
		this.s = s;
		this.form = form;
		this.pos = pos;
		this.left = left;
	}
	
	/** 同时也设置每个item的后向item, POS use*/
	public POSItem(int s, String form, String pos, POSItem left, boolean addNext) {
		this.s = s;
		this.form = form;
		this.pos = pos;
		this.left = left;
		if(left != null)
			this.left.right = this;
	}
	
	/** 同时也设置每个item的后向item, POS 为空*/
	public POSItem(int s, String form, POSItem left, boolean addNext) {
		this.s = s;
		this.form = form;
		this.left = left;
		if(left != null)
			this.left.right = this;
	}
	
	/** 复制Item */
	public POSItem copy() {
		POSItem newItem = new POSItem(this.s, this.form, this.pos, this.left, true);
		return newItem;
	}
	
	
	/**
	 * 查找一个Item的word是否在Item[]
	 * @since Sep 6, 2012
	 * @param items
	 * @return
	 */
	public int inWord(POSItem[] items) {
    	int length = items.length;
    	for(int i=0; i<length; i++) {
    		POSItem curItem = items[i];
    		if(curItem.s == this.s && curItem.form.equals(this.form)) {
    			return 2;
    		}    			
    	}
    	return 1;    	
    }
	
	/**
	 * 查找一个Item的word pos是否在Item[]
	 * @since Sep 6, 2012
	 * @param items
	 * @return
	 */
	public int inWordPos(POSItem[] items) {
    	int length = items.length;
    	for(int i=0; i<length; i++) {
    		POSItem curItem = items[i];
    		if(curItem.s == this.s && curItem.form.equals(this.form) && curItem.pos.equals(this.pos)) {
    			return 2;
    		}    			
    	}
    	return 1;    	
    }
	
	
	/**
	 * 翻转Item，主要是翻转其中的词，也可以同时它前面的词
	 * @since Jan 8, 2013
	 * @param reverseLeft 把其前面的Item转为后面的Item
	 * @return
	 */
	public POSItem reverse(boolean reverseLeft, int length) {
		
		POSItem newRight = null;
		if(reverseLeft) {
		  if(left != null)
			newRight = left.reverse(reverseLeft, length);
		  left = null;
		}
		int newS = length - s;
				
		POSItem newItem = new POSItem(newS, form, pos, curProb, fv, left);
		if(reverseLeft) {
			newItem.right = newRight;
		}
		
		return newItem;
		
	}


	/** 通过Item得到它的对应的整个实例 */
	public POSInstance getInstance() {		
		
		ArrayList<String> wordList = new ArrayList<String>();
    	ArrayList<String> posList = new ArrayList<String>();
    	wordList.add(form);
    	posList.add(pos);
    	POSItem cur = left;
		while(cur != null){
			wordList.add(0, cur.form);
			posList.add(0, cur.pos);
			cur = cur.left;
		}
		String[] words = Array.toStringArrayReverse(wordList);
		String[] poses = Array.toStringArrayReverse(posList);
		POSInstance inst = new POSInstance(words, poses);
		return inst;
		
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof POSItem)) return false;
		POSItem objItem = (POSItem) obj;
		if(this.s != objItem.s || !this.pos.equals(objItem.pos))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return new String(s+":"+form+":"+pos);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}

