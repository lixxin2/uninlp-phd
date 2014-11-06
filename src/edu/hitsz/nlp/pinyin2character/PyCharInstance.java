package edu.hitsz.nlp.pinyin2character;

import java.io.Serializable;
import java.util.ArrayList;

import edu.hitsz.nlp.segpos.FeatureVector;
import edu.hitsz.nlp.util.Array;

/**
 * 拼音-词序列实例，通常是指一个句子。其中包括字音，字，词音，词，句长
 * @author Xinxin Li
 * @since Sep 21, 2013
 */
public class PyCharInstance implements Serializable {

	private static final long serialVersionUID = 1L;

	FeatureVector fv;

	public String[] characterYins;
	public String[] characters;
	public String[] wordYins;
	public String[] words;
	public int length;
	
	public PyCharInstance() {
		
	}
	
	public PyCharInstance(String[] characterYins, String[] characters){
		this.characterYins = characterYins;
		this.characters = characters;
		this.length = characterYins.length;
	}
	
	public PyCharInstance(String[] characterYins, String[] characters, 
			String[] wordYins, String[] words){
		this.characterYins = characterYins;
		this.characters = characters;
		this.wordYins = wordYins;
		this.words = words;
		this.length = characterYins.length;
	}
	
	public PyCharInstance(String[] pinyin, int length){
		this.characterYins = pinyin;
		this.length = length;
	}

	public PyCharInstance(String[] words){
		this.words = words;
	}
	
	/**
	 * get the features of every pinyin
	 * @since 2012-2-29
	 * @param i
	 * @return
	 */
	public ArrayList<String> getFeature(int i){
		ArrayList<String> features = new ArrayList<String>();
		int featureNumber=1;
		features.add(featureNumber+++":"+characterYins[i]);
		
		for(int k=1; k<4; k++) {
			if(i-k>=0) {
				features.add(featureNumber+++":"+characterYins[i-k]);
            }
			if((i+k<length)) {
				features.add(featureNumber+++":"+characterYins[i+k]);
            }
        }		
		return features;
	}
	
	
	public static PyCharInstance getInstance(PyCharInstance inst, PyCharItem cur) {
		String[] characterYins = inst.characterYins;
		String[] characters = inst.characters;
		
		ArrayList<String> word = new ArrayList<String>();
		ArrayList<String> wordYin = new ArrayList<String>();
		while(cur != null){
			word.add(0, cur.word);
			wordYin.add(0, cur.pinyin);
			cur = cur.left;
		}
		String[] words = Array.toStringArrayReverse(word);
		String[] wordYins = Array.toStringArrayReverse(wordYin);
				
		PyCharInstance instance = new PyCharInstance(characterYins, characters, wordYins, words);
		return instance;   
	}
	
	/** 获得句子序列 */
	public String getSentence() {
		StringBuffer sb = new StringBuffer();
		for(String word : words)
			sb.append(word);
		return sb.toString();
	}
	
	/** 得到所有PyCharItem */
	public PyCharItem[] getItems() {
		int length = words.length;
		PyCharItem[] items = new PyCharItem[length];
		int start = 0;
		for(int i=0; i<length; i++) {
			PyCharItem item = new PyCharItem(start, start+words[i].length(), wordYins[i], words[i], null);
			if(i>0)
				item.left =  items[i-1];
			start += words[i].length();
			items[i] = item;
		}
		return items;
	}
	
}
