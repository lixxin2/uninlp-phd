package edu.hitsz.nlp.partofspeech;

import java.io.Serializable;
import java.util.ArrayList;

import edu.hitsz.nlp.segpos.BasicWordPos;
import edu.hitsz.nlp.util.Array;



public class POSInstance implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	POSFeatureVector fv;

	public String[] words;
	public String[] tags;
		
	
	/**
	 * 通过word，pos生成实例，包括所有的char，chartags，puncts，nglowclasses
	 * @since Jan 15, 2013
	 * @param words
	 * @param tags
	 */
	public POSInstance(String[] words, String[] tags){
		int length = words.length;
		this.words = new String[length];
		System.arraycopy(words, 0, this.words, 0, length);
		if(tags != null) {
			this.tags = new String[length];
			System.arraycopy(tags, 0, this.tags, 0, length);
		}
	}
	
	
	/**
	 * 从依存实例中获得分词和词性实例，需要去掉root节点
	 * <p> 单纯的分词和词性标注不用这个初始化
	 * @since 2012-3-15
	 * @param words
	 * @param tags
	 * @param added 添加
	 */
	public POSInstance(String[] words, String[] tags, boolean added){
		
		int length = words.length - 1;
		this.words = new String[length];
		System.arraycopy(words, 1, this.words, 0, length);
		if(tags != null) {
			this.tags = new String[length];
			System.arraycopy(tags, 1, this.tags, 0, length);
		}
	}
	
	public int getSentenceLength() {
		if(words == null)
			return 0;
		else
			return words.length;
	}
	
	/**
	 * 获得从头结点到第k个位置组成的实例
	 * @since Jan 15, 2013
	 * @param k
	 * @return
	 */
	public POSInstance getInstance(int k) {
		int i = -1;
		ArrayList<String> newWords = new ArrayList<String>();
		ArrayList<String> newTags = new ArrayList<String>();
		int j = 0;
		//前面完整的词
		for(; j<words.length; j++) {
			if(i + words[j].length() <= k) {
				newWords.add(words[j]);
				newTags.add(tags[j]);	
				i += words[j].length();
			}		
			else
				break;
		}
		//最后一个不完整的词
		if(i+1 <= k && j < words.length) {
			newWords.add(words[j].substring(0, k-i));
			newTags.add(tags[j]);
		}		
		String[] newWordVec = Array.toStringArray(newWords);
		String[] newTagVec = Array.toStringArray(newTags);	
		return new POSInstance(newWordVec, newTagVec);
	}
	
	public void setFeatureVector(POSFeatureVector fv){
		this.fv = fv;
	}
	
	/**
	 * 返回所有的BasicWordPos类，
	 * @since 2012-3-11
	 * @return
	 */
	public ArrayList<BasicWordPos> getWordPos() {
		ArrayList<BasicWordPos> wordpos = new ArrayList<BasicWordPos>();
		int length = words.length;
		int start = 0;
		int end = 0;
		for(int i=0; i<length; i++) {
			int wordsize = words[i].length();
			end += wordsize;
			wordpos.add(new BasicWordPos(words[i], tags[i], start, end-1));
			start = end;
		}
		return wordpos;
	}
	
	

	
	
	/**
	 * 翻转词和词性
	 * @since Sep 6, 2012
	 * @param inst
	 * @return
	 */
	public POSInstance reverse(){
		
		String[] newWords = null;
		String[] newTags = null;
		
		if(words != null)
			newWords = POSReader.reverseWords(words);
		if(tags != null)
			newTags = POSReader.reverseArray(tags);
		
		return new POSInstance(newWords, newTags);			
	}	

	
	
	
	/**
	 * 将Instance转换为Item[]
	 * @since Sep 6, 2012
	 * @return
	 */
	public POSItem[] getItems() {

		int wordSize = words.length;
		POSItem[] items = new POSItem[wordSize];
		
		POSItem preItem = null;
		int start = 0;
		int end = 0;
		for(int i = 0; i < wordSize; i++) {
			POSItem curItem = new POSItem(start, words[i], tags[i], preItem);
			items[i]=curItem;
			start = end;
			preItem = curItem;
		}		
		return items;
	}
	
	
	
	/** 匹配当前生成的部分predict instance和现有gold instance进行比较
	 */
	public boolean matches(POSInstance predict) {		
		
		if(predict != null) {	
			
			String[] predictWords = predict.words;
			String[] predictTags = predict.tags;
			
			if(words.length != predictWords.length) {
				return false;
			}
			else {
				for(int i=0; i<words.length; i++) {
					if(!words[i].equals(predictWords[i]) || !tags[i].equals(predictTags[i]))
						return false;
				}
				return true;
			}
		}		
		return false;		
		
	}
	

	
	
	
	
	
	
	
	
	
	
	public void output() {
		System.out.print("words: ");
		if(words!=null && words.length >0){
			for(int i=0; i< words.length; i++)
				System.out.print(words[i]+"\t");			
		}
		System.out.println();
	}
	
	
	
	
	



}

