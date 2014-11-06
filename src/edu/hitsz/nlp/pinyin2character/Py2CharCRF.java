package edu.hitsz.nlp.pinyin2character;

import java.util.ArrayList;

public class Py2CharCRF {
	
	
	
	/**
	 * 转化训练语料为列排列，用于CRF
	 * @since Oct 22, 2012
	 * @param inFileName
	 * @param outFileName
	 */
	public static void trainConvert(String inFileName, String outFileName) {
		
		PyCharReader reader= new PyCharReader(inFileName);
		PyCharInstance instance = reader.getNext();
		PyCharWriter writer = new PyCharWriter(outFileName);
		
		int sentenceNumber = 0;
		
		while(instance != null) {
			
			sentenceNumber++;

			if(sentenceNumber%100 == 0) {
				System.out.println(sentenceNumber + "...");
			}
			
			writer.writeInstanceColumn(instance);
						
			instance = reader.getNext();
		}
		
		writer.finishWriting();
		
	}
	
	
	
	public static void main(String[] args) {
		
		String trainFile = "/home/tm/disk/disk1/pinyin2character/train-UTF-8";
		String trainCRFFile = "/home/tm/disk/disk1/pinyin2character/train-CRF";
		
		Py2CharCRF.trainConvert(trainFile, trainCRFFile);
		
	}
	

}
