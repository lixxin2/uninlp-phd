package edu.hitsz.nlp.segpos;

import java.io.Serializable;
import java.util.ArrayList;

import edu.hitsz.nlp.mstjoint.WordPos;
import edu.hitsz.nlp.util.Array;



public class Instance implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	FeatureVector fv;

	public String[] words;
	public String[] tags;
	public String[] chars;
	public String[] chartags;
	public String[] puncts; //是否标点符号
	public String[] ngLowClasses;  //类别

	
	
	public String[] getChars() {
		return this.chars;
	}
	
	
	/**
	 * 通过word，pos生成实例，包括所有的char，chartags，puncts，nglowclasses
	 * @since Jan 15, 2013
	 * @param words
	 * @param tags
	 */
	public Instance(String[] words, String[] tags){
		int length = words.length;
		this.words = new String[length];
		System.arraycopy(words, 0, this.words, 0, length);
		if(tags != null) {
			this.tags = new String[length];
			System.arraycopy(tags, 0, this.tags, 0, length);
			String[][] obj = Reader.getCharPOS(this.words, this.tags);
			this.chartags = obj[1];
		}
		String[] obj = Reader.getChars(this.words);
		this.chars = obj;
		this.puncts = Reader.isPunc(chars);
		this.ngLowClasses = Reader.ngLowClasses(chars);
	}
	
	
	/**
	 * 从依存实例中获得分词和词性实例，需要去掉root节点
	 * <p> 单纯的分词和词性标注不用这个初始化
	 * @since 2012-3-15
	 * @param words
	 * @param tags
	 * @param added 添加
	 */
	public Instance(String[] words, String[] tags, boolean added){
		
		int length = words.length - 1;
		this.words = new String[length];
		System.arraycopy(words, 1, this.words, 0, length);
		if(tags != null) {
			this.tags = new String[length];
			System.arraycopy(tags, 1, this.tags, 0, length);
			String[][] obj = Reader.getCharPOS(this.words, this.tags);
			this.chartags = obj[1];
		}
		String[] obj = Reader.getChars(this.words);
		this.chars = obj;
		this.puncts = Reader.isPunc(chars);
		this.ngLowClasses = Reader.ngLowClasses(chars);
	}
	
	
	
	/** 直接赋值 */
	public Instance(String[] chars, String[] chartags, String[] words, String[] tags){
		this.chars = chars;
		this.chartags = chartags;
		this.words = words;
		this.tags = tags;
	}
	
	/** 直接赋值 */
	public Instance(String[] chars, String[] chartags, String[] words, String[] tags,
			String[] puncts, String[] ngLowClasses){
		this.chars = chars;
		this.chartags = chartags;
		this.words = words;
		this.tags = tags;
		this.puncts = puncts;
		this.ngLowClasses = ngLowClasses;
	}
		
	
	/**
	 * 获得从头结点到第k个位置组成的实例
	 * @since Jan 15, 2013
	 * @param k
	 * @return
	 */
	public Instance getInstance(int k) {
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
		return new Instance(newWordVec, newTagVec);
	}


	public int charLength () {
    	return chars.length;
    }

	public void setFeatureVector(FeatureVector fv){
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
	 * 输入词和词性，返回字和字标识,实现与Reader.getWordPOS不同
	 * @since 2012-3-10
	 * @param words
	 * @param tags
	 * @return
	 */
	public Object[] word2char(String[] words, String[] tags){
		Object[] newObj = new Object[2];
		
		int length = 0;
		int wordSize = words.length;
		for(int i=0; i < wordSize; i++){
			length += words[i].length();
		}
		String[] chars = new String[length];
		String[] charPos = new String[length];

		int k=0;//第一个字
		for(int i = 0; i < wordSize; i++) {
		    int wordLength = words[i].length();
		    String[] wordCharPos = Word2Char.generateTag(wordLength, tags[i]);
		    for(int j=0; j<wordLength; j++){
		    	chars[k] = words[i].substring(j,j+1);
		    	charPos[k] = wordCharPos[j];
		    	k++;
		    }
		}
		
		newObj[0] = chars;
		newObj[1] = charPos;
		
		return newObj;
	}
	
	
	/**
	 * 翻转词和词性
	 * @since Sep 6, 2012
	 * @param inst
	 * @return
	 */
	public Instance reverse(){
		
		String[] newWords = null;
		String[] newTags = null;
		
		if(words != null)
			newWords = Reader.reverseWords(words);
		if(tags != null)
			newTags = Reader.reverseArray(tags);
		
		return new Instance(newWords, newTags);			
	}	

	
	/**
	 * 使两个实例的字符对齐
	 * @since Sep 26, 2012
	 * @param chars
	 */
	public void fitWordPos(String[] chars) {
		int curCharsLength = this.chars.length;
		int charsLength = chars.length;
		String[] newCharPos = new String[charsLength];
		int[] charPosition = new int[charsLength];
		int j=0;
		//if(chars.length != this.chars.length) {
			for(int i=0; i< charsLength; i++) {
				String sChar = chars[i];
				for(int k=j; k<Math.min(j+3, curCharsLength); k++)
					if(this.chars[k].equals(sChar)) {
						newCharPos[i] = this.chartags[k];
						charPosition[i] = k; 
						j = k+1;
						break;
					}				
			}
			for(int i=0; i<charsLength; i++) {
				if(newCharPos[i]==null || newCharPos[i].length()==0) {
					if(i == 0)
						newCharPos[0] = chartags[0];
					else {
						charPosition[i] = charPosition[i-1]+1;
						if(charPosition[i] < chartags.length)
							newCharPos[i] = chartags[charPosition[i]];
						else
							newCharPos[i] = chartags[chartags.length-1];
					}
				}
			}
			
			
		//}
		
	}
	
	
	
	/** 
	 * 解析，得到wordpos lattice需要的结构(将所有词和词性对，放到lattice中)，
	 * 具体参考DependencyPipeJoint的WriteInstanceJoint方法 (测试用)
	 */
	public Object[] decompose2222() {
		int length = chars.length+1;
		//WordPos[][][] allWordPos = new WordPos[length][length][8];
		WordPos[][] leftWordPoses = new WordPos[length][1];
		WordPos[][] rightWordPoses = new WordPos[length][1];
		WordPos[] leftWordPos = new WordPos[length];
		WordPos[] rightWordPos = new WordPos[length];
		
		WordPos first= new WordPos("<root>","<root-POS>", 0, 0, 0.0);
    	//allWordPos[0][0][0] = first; 
    	leftWordPos[0] = first;
    	leftWordPoses[0][0] = first;
    	rightWordPos[0] = first;
    	rightWordPoses[0][0] = first;
		    	
		int start = 1;
		int end = 1;
		for(int i=0; i<words.length; i++) {
			end = start + words[i].length() - 1;
			WordPos wp = new WordPos(words[i], tags[i], start, end, 0.0);
			//找到左右索引，index
			wp.leftIndex = 0;
			wp.rightIndex = 0;
			
			//allWordPos[start][end][0] = wp;
			leftWordPoses[end][0] = wp;
			rightWordPoses[start][0] = wp;
			leftWordPos[end] = wp;
			rightWordPos[start] = wp;
					
			start = end + 1;
		}
		Object[] obj = new Object[5];

    	
    	//obj[0] = allWordPos;
    	obj[1] = leftWordPoses;
    	obj[2] = rightWordPoses;
    	//
    	for(int i=length-1; i>=0; i--){
    		if(leftWordPos[i] == null) {
    			int j=i;
    			while(j<length && leftWordPos[j] == null)
    				j--;    			
    			String form = Array.toWord(chars, j, i);
    			leftWordPos[i] = new WordPos(form, "<l-pos>", j+1, i, 0.0);
    		}
    	}    	
    	obj[3] = leftWordPos;
    	//
    	//怎样处理那些没有的词
    	//策略1：生成一个<e-pos>词性
    	for(int i=0; i<length; i++){
    		if(rightWordPos[i] == null) {
    			int j=i;
    			while(j<length && rightWordPos[j] == null)
    				j++;    			
    			String form = Array.toWord(chars, i-1, j-1);
    			rightWordPos[i] = new WordPos(form, "<r-pos>", i, j-1, 0.0);
    		}
    	}
    	//策略2： 直接在lattice中生成，使其没有空余    	
    	obj[4] = rightWordPos; 		
    	
    	return obj;		
		
	}
	
	
	/**
	 * 将Instance转换为Item[]
	 * @since Sep 6, 2012
	 * @return
	 */
	public Item[] getItems() {

		int wordSize = words.length;
		Item[] items = new Item[wordSize];
		
		Item preItem = null;
		int start = 0;
		int end = 0;
		for(int i = 0; i < wordSize; i++) {
			end = start + words[i].length();
			Item curItem = new Item(start, end, words[i], tags[i], preItem);
			items[i]=curItem;
			start = end;
			preItem = curItem;
		}		
		return items;
	}
	
	
	
	/** 匹配当前生成的部分predict instance和现有gold instance进行比较
	 */
	public boolean matches(Instance predict) {		
		
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
		System.out.print("chars: ");
		if(chars!=null && chars.length >0){
			for(int i=0; i< chars.length; i++)
				System.out.print(chars[i]+"\t");			
		}
		System.out.println();
	}
	
	
	
	
	



}
