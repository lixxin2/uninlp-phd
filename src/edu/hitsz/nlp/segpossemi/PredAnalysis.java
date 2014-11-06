package edu.hitsz.nlp.segpossemi;

import java.io.IOException;
import java.util.HashMap;

import edu.hitsz.nlp.segmentation.BasicWord;
import edu.hitsz.nlp.segpos.BasicWordPos;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.segpos.Reader;
import edu.hitsz.nlp.segpos.Evaluation;


public class PredAnalysis {
	
	Evaluation eval = new Evaluation();

	/** 
	 * 返回gold文件中的所有词的数目，predict文件中的所有词的数目，和预测正确的词的数目
	 * @since Oct 11, 2013
	 * @param goldFile
	 * @param predictFile
	 */
	public void evalFile(String goldFile, String predictFile) {
		
		try {
			Options options = new Options();

			Reader goldReader = new Reader();
			goldReader.startReading(goldFile, options);
			Instance goldInstance = goldReader.getNext();
			Reader predictReader = new Reader();
			predictReader.startReading(predictFile, options);
			Instance predictInstance = predictReader.getNext();			
			
			int allGoldNum = 0;
			int allPredNum = 0;
			int allCorrNum = 0;
			int count = 0;			
			
			while(goldInstance != null && predictInstance != null) {
				count++;
				System.out.println(count);
				
				String[] goldWords = goldInstance.words;
				int goldNumber = goldWords.length;
				BasicWord[] goldWordPoses = eval.getWordPoses(goldWords);	
				
				String[] predictWords = predictInstance.words;
				int predictNumber = predictWords.length;
				BasicWord[] predictWordPoses = eval.getWordPoses(predictWords);
				
				int corrNumber = new Evaluation().wordCorr(goldWordPoses, predictWordPoses);
				
				allGoldNum += goldNumber;
				allPredNum += predictNumber;
				allCorrNum += corrNumber;
				
				goldInstance = goldReader.getNext();
				predictInstance = predictReader.getNext();				
			}
			
			System.out.println("allGoldNum: " + allGoldNum + "\n");
			System.out.println("allPredNum: " + allPredNum + "\n");
			System.out.println("allCorrNum: " + allCorrNum + "\n");
			System.out.println("precison:   " + (double)allCorrNum/allGoldNum + "\n");
			System.out.println("recall:     " + (double)allCorrNum/allPredNum + "\n");
			System.out.println("F-1 value:  " + (double)2*allCorrNum/(allGoldNum+allPredNum) + "\n");
			
		}
		catch (IOException e) {
			
		}
		
	}
	
	
	/**
	 * 比较两个预测的文件在不同长度上的准确性
	 * @since 2013-2-14
	 * @param goldFile
	 * @param predict1File
	 * @param predict2File
	 */
	public void evalByWordNumInSent(String goldFile, String predict1File, String predict2File) {
		
		try {
			Options options = new Options();

			Reader goldReader = new Reader();
			goldReader.startReading(goldFile, options);
			Instance goldInstance = goldReader.getNext();

			Reader predictReader1 = new Reader();
			predictReader1.startReading(predict1File, options);
			Instance predictInstance1 = predictReader1.getNext();		

			Reader predictReader2 = new Reader();
			predictReader2.startReading(predict2File, options);
			Instance predictInstance2 = predictReader2.getNext();		
			
			int count = 0;
			
			int[][] results = new int[10][3]; //句子数目，正确性（相同，大于，小于）
			
			while(goldInstance != null && predictInstance1 != null && predictInstance2 != null) {
				count++;
				System.out.println(count);
				
				String[] goldWords = goldInstance.words;
				int goldNumber = goldWords.length;
				BasicWord[] goldWordPoses = eval.getWordPoses(goldWords);				
				String[] predictWords1 = predictInstance1.words;
				int predictNumber1 = predictWords1.length;
				BasicWord[] predictWordPoses1 = eval.getWordPoses(predictWords1);
				
				int corrNum1 = new Evaluation().wordCorr(goldWordPoses, predictWordPoses1);
				double f1 = 2 * corrNum1 / (double)(goldNumber + predictNumber1) ;
				
				if(goldNumber >= 100)
					goldNumber = 99;
				if(goldNumber%10 == 0)
					goldNumber -= 1;
				
				int numberSeq = goldNumber/10;
				
				String[] predictWords2 = predictInstance2.words;
				BasicWord[] predictWordPoses2 = new Evaluation().getWordPoses(predictWords2);
				
				int corrNum2 = new Evaluation().wordCorr(goldWordPoses, predictWordPoses2);
				int predictNumber2 = predictWords2.length;
				double f2 = 2 * corrNum2/(double)(goldNumber + predictNumber2) ;
				
				if(Math.abs(f1-f2) < 1e-8)
					results[numberSeq][0] += 1;
				else if(f1 > f2)
					results[numberSeq][1] += 1;
				else 
					results[numberSeq][2] += 1;
				
				goldInstance = goldReader.getNext();
				predictInstance1 = predictReader1.getNext();
				predictInstance2 = predictReader2.getNext();
			}
			
			System.out.println();
		}
		catch (IOException e) {
			
		}			
	}
	

	
	
	
	public static void main(String[] args) {
		
		String goldFile = "/home/tm/disk/disk1/segpossemi/error-driven/ctb5-dev-wp";
		String predictFile = "/home/tm/disk/disk1/segpossemi/error-driven/ctb5-dev-result-6";
		String predictFile2 = "/disk1/segpossemi/joint-first/dev-wordResult-3";
		
		PredAnalysis eval = new PredAnalysis();
		eval.evalFile(goldFile, predictFile);
		//eval.evalFileInSentences(goldFile, predictFile, predictFile2);
		
	}
	
	

}
