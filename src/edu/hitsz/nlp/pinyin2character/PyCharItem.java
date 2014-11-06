package edu.hitsz.nlp.pinyin2character;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.nlp.segpos.FeatureVector;


public class PyCharItem {

	public int s,t;
	public String pinyin, word;
	public double prob; //所有的权重和
	public double curProb; //当前词的权重和
	public FeatureVector fv;
	public PyCharItem left;
	
	
	public PyCharItem(int s, int t, String pinyin, String word,
			double prob,
			FeatureVector fv,
			PyCharItem left){
		this.s = s;
		this.t = t;
		this.pinyin = pinyin;
		this.word = word;
		this.prob = prob;
		this.fv = fv;
		this.left = left;
	}
	
	
	public PyCharItem(int s, int t, String pinyin, String word, PyCharItem left) {
		this.s = s;
		this.t = t;
		this.pinyin = pinyin;
		this.word = word;
		this.left = left;
	}
	
	/** 得到ngram的串 */
	public List<String> getNgram(int count) {
		PyCharItem item = this;
		ArrayList<String> ngram = new ArrayList<String>();
		int i=0;
		while (i<count && item != null) {
			ngram.add(0, item.word);
			item = item.left;
			i++;
		}		
		if(i<count)
			ngram.add(0, "<s>");
		
		return ngram;	
		
	}
	
	
	/** 最后一个词，加</s> */
	public List<String> getNgramEnd(int count) {
		PyCharItem item = this;
		List<String> ngram = getNgram(count -1);
		ngram.add("</s>");
		return ngram;		
	}
	
	
	
}
