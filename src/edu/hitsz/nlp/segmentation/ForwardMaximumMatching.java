package edu.hitsz.nlp.segmentation;

import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.nlp.struct.DictionaryHashMap;

/**
 * 前向最大匹配，用于中文分词。需要首先设置词典
 * @author Xinxin Li
 * Dec 1, 2011
 */
public class ForwardMaximumMatching extends Matching{

	private static HashMap dict;
	private static int maximumLength=20;

	public ForwardMaximumMatching(){
		dict = new HashMap();
	}

	/**
	 * 初始化，读入词典和最长字符
	 * @param tmpDict
	 */
	public void setDict(HashMap tmpDict){
		dict = tmpDict;
	}

	public void setMaximumLength(int length){
		maximumLength = length;
	}

	/**
	 * 分词 对句子分词
	 * @param sentence
	 * @return
	 */
	public static ArrayList<BasicWord> segmentSentence(String sentence){
		int sentenceLength = sentence.length();
		return segmentSentence(sentence, 0, sentenceLength);
	}

	/**
	 * 对句子的一部分分词
	 * @param sentence
	 * @param sentenceStart
	 * @param sentenceLength
	 * @return
	 */
	public static ArrayList<BasicWord> segmentSentence(String sentence, int sentenceStart, int sentenceEnd){
		ArrayList<BasicWord> words = new ArrayList<BasicWord>();
		int start = sentenceStart;
		while(start < sentenceEnd){
			int end = Math.min(start+maximumLength, sentenceEnd);
			boolean isWord = false;
			for(; end > start; end--){
				if(dict.containsKey(sentence.substring(start, end))){
					BasicWord word = new BasicWord(sentence.substring(start, end), start, end);
					words.add(word);
					isWord = true;
					break;
				}
			}
			if(isWord == false){
				end = start + 1;
				BasicWord word = new BasicWord(sentence.substring(start, end), start, end);
				words.add(word);
			}
			start = end;
		}
		return words;
	}

	/**
	 * 分词
	 * @param sentence
	 * @return
	 */
	public ArrayList<String> segment(String sentence){
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<BasicWord> basicWords = segmentSentence(sentence);
		for(BasicWord basicWord : basicWords){
			words.add(basicWord.getWord());
		}
		return words;
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		HashMap<String, Integer> newDict = new HashMap<String, Integer>();
		newDict.put("我们", 1);
		newDict.put("中国", 1);
		ForwardMaximumMatching newMatch = new ForwardMaximumMatching();
		newMatch.setDict(newDict);
		ArrayList<String> words = newMatch.segment("我们爱中国");
		System.out.println(words);

	}

}
