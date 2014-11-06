package edu.hitsz.nlp.segmentation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 后向最大匹配，用于中文分词。需要首先设置词典
 * @author Xinxin Li
 * Dec 1, 2011
 */
public class BackwardMaximumMatching extends Matching{

	private static HashMap dict;
	private static int maximumLength = 20;

	public BackwardMaximumMatching(){
		dict = new HashMap();
	}

	/**
	 * 设置词典
	 * @param tmpDict
	 */
	public void setDict(HashMap tmpDict){
		dict = tmpDict;
	}

	public void setMaximumLength(int length){
		maximumLength = length;
	}

	private ArrayList<BasicWord> segmentSentence(String sentence){
		int sentenceLength = sentence.length();
		return segmentSentence(sentence, 0, sentenceLength);
	}

	private ArrayList<BasicWord> segmentSentence(String sentence, int sentenceStart, int sentenceEnd){
		ArrayList<BasicWord> words = new ArrayList<BasicWord>();
		int end = sentenceEnd;
		while(end > 0 ){
			int start = Math.max(sentenceStart, end-maximumLength);
			boolean isWord = false;
			for(; start < end; start++){
				if(dict.containsKey(sentence.substring(start, end))){
					BasicWord word = new BasicWord(sentence.substring(start, end), start, end);
					words.add(word);
					isWord = true;
					break;
				}
			}
			if(isWord == false){
				start = end - 1;
				BasicWord word = new BasicWord(sentence.substring(start, end), start, end);
				words.add(word);
			}
			end = start;
		}
		return words;
	}

	public ArrayList<String> segment(String sentence){
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<BasicWord> basicWords = segmentSentence(sentence);
		for(int i=basicWords.size()-1; i>=0; i--){
			words.add(basicWords.get(i).getWord());
		}
		return words;
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> newDict = new HashMap<String, Integer>();
		newDict.put("我们", 1);
		newDict.put("中国", 1);
		BackwardMaximumMatching newMatch = new BackwardMaximumMatching();
		newMatch.setDict(newDict);
		ArrayList<String> words = newMatch.segment("我们爱中国");
		System.out.println(words);
	}

}
