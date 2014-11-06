package edu.hitsz.nlp.struct;

import java.util.ArrayList;

public class RawText {


	/**
	 * 中文句子分割
	 * @param text 输入文本
	 * @return
	 */
	public static String[] ChineseSentenceSegmentation(String text){
		String stopPuncts="[。？！]";
		String[] sentences=text.split(stopPuncts);
		return sentences;
	}





}
