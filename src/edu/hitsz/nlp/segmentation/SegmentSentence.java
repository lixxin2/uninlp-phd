package edu.hitsz.nlp.segmentation;

import java.util.ArrayList;

/**
 * 分词句子
 * @author Xinxin Li
 * Dec 3, 2011
 */
public class SegmentSentence extends ArrayList<BasicWord> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	int wordsNumber;
	String characters;
	int length;

	/**
	 * 处理分词句子，格式如下：
	 * 人们  常  说  生活  是  一  部  教科书。
	 * @param tempSentence
	 */
	public void processTrainLine(ArrayList<String> tempSentence){
		String[] allWords = tempSentence.get(0).split("  ");
		wordsNumber = allWords.length;
		length = 0;
		for(int i=0; i<wordsNumber; i++){
			int start = length;
			length += allWords[i].length();
			BasicWord newWord = new BasicWord(allWords[i],start,length);
			this.add(newWord);
			characters += allWords[i];
		}
	}

	/**
	 * 处理分词句子，格式如下：
	 * 人们常说生活是一部教科书。
	 * @param tempSentence
	 */
	public void processRaw(ArrayList<String> tempSentence){
		characters = tempSentence.get(0);
		length = characters.length();
	}



}
