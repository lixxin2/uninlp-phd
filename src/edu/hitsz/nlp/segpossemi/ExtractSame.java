package edu.hitsz.nlp.segpossemi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.nlp.segpos.BasicWordPos;
import edu.hitsz.nlp.segpos.Evaluation;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.segpos.Reader;
import edu.hitsz.nlp.segpos.Writer;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;


/**
 * 从两个文件中抽取一一对应的相同的句子，用作semi-supervised learning
 * @author Xinxin Li
 * @since 2013-2-9
 */
public class ExtractSame {
	
	public double minProb = 0.999; //0.999 最小的相同概率，只有大于这个最小值
	public double minOovRate = 0.20;   //-0.1 OOV词占句子的比例，只有大于这个最小值才能选取
	public double minMinLength = 0;      //0 最小句子长度，只有大于这个最小值才能选取
	public HashMap<String, Integer> vTags;    //词典，判断是否是IV words
	public Evaluation eval = new Evaluation();
	public boolean isSatisfied = false;      //是否满足设定的条件
	
	/** 
	 * 载入词典 
	 * @since Oct 20, 2013
	 * @param fileName
	 * @throws IOException
	 */
	public void loadInWords(String fileName) {
		eval.loadIvWords(fileName, "seg");
		vTags = eval.vTags;
	}
	
	/**
	 * 
	 * @since Oct 20, 2013
	 * @param wordpos
	 * @return
	 */
	public int getInWordNumber(BasicWordPos[] wordposes) {
		
		int size = wordposes.length;
		int ivNumb = 0;
		for(int i = 0; i< size; i++) {
			String word = wordposes[i].getWord();
			if(vTags.containsKey(word))
				ivNumb++;
		}		
		return ivNumb;
	}
	
	
	/** 
	 * Oov词的比例 > minOovRate
	 * @since Oct 20, 2013
	 * @param wordWordPoses
	 * @return
	 */
	public boolean satisfyingOOVRate(BasicWordPos[] wordWordPoses) {
		
		int numb = wordWordPoses.length;
		int inNumb = getInWordNumber(wordWordPoses);
		if( (numb - inNumb)/(double)numb > minOovRate)
			return true;
		else
			return false;		
	}
	
	
	/**
	 * 如果满足条件
	 * @since Oct 20, 2013
	 * @return
	 */
	public boolean satisfyingMinProb(BasicWordPos[] charWordPoses, 
			BasicWordPos[] wordWordPoses) {

		int numb1 = charWordPoses.length;
		int numb2 = wordWordPoses.length;
		int sameNumb = eval.wordCorr(charWordPoses, wordWordPoses);
		double prob1 = (double)sameNumb/numb1;
		double prob2 = (double)sameNumb/numb2;
		
		if(prob1 > minProb && prob2 > minProb)
			return true;
		else
			return false;
	}
	
	/**
	 * 句子长度是否大于设定值
	 * @since Oct 20, 2013
	 * @param wordWordPoses
	 * @return
	 */
	public boolean satisfyingMinLength(BasicWordPos[] wordWordPoses) {
		
		if(wordWordPoses.length > minMinLength)
			return true;
		else
			return false;				
	}
	
	
	/**
	 * 是否满足所有条件
	 * @since Oct 20, 2013
	 * @param charWordPoses
	 * @param wordWordPoses
	 * @return
	 */
	public boolean satisfied(BasicWordPos[] charWordPoses, 
			BasicWordPos[] wordWordPoses) {
		
		if(
			satisfyingMinProb(charWordPoses, wordWordPoses) &&
			satisfyingOOVRate(wordWordPoses) &&
			satisfyingMinLength(wordWordPoses)
			)
			return true;
		else
			return false;
	}
	
	/**
	 * 从两个文件charFile和wordFile中抽取一一对应的相同的句子，
	 * 并同时抽取goldFile中对应的句子，用作以后比较
	 * @since 2013-2-13
	 * @param charFile 第一个文件
	 * @param wordFile 第二个文件
	 * @param sameFile 两个文件中相同句子
	 * @param goldFile gold文件
	 * @param goldSameFile gold文件中与sameFile对应的句子
	 */
	public void extractSentences(String predFile1, String predFile2, 
			String predSameFile, 
			String goldFile, String goldSameFile) {
		
		try {
			
			Options options = new Options();
			
			Reader charReader = new Reader();
			charReader.startReading(predFile1, options);
			Instance charInstance = charReader.getNext();
			
			Reader wordReader = new Reader();
			wordReader.startReading(predFile2, options);
			Instance wordInstance = wordReader.getNext();
			
			Writer sameWriter = new Writer();
			sameWriter.startWriting(predSameFile, options);
						
			Reader goldReader = new Reader();
			Instance goldInstance = null;
			if(goldFile != null) {
				goldReader.startReading(goldFile, options);
				goldInstance = goldReader.getNext();
			}
			
			Writer goldSameWriter = new Writer();
			if(goldSameFile != null)
				goldSameWriter.startWriting(goldSameFile, options);
						
			int count = 0;
			int satisfiedNumber = 0;
			
			while(charInstance != null && wordInstance != null) {
				
				if(count % 100 == 0) {
					System.out.print(count + ".");
					if(count % 1000 == 0)
						System.out.println();
				}
				
				BasicWordPos[] charWordPoses = eval.getWordPoses(
						charInstance.words, charInstance.tags);
				
				BasicWordPos[] wordWordPoses = eval.getWordPoses(
						wordInstance.words, wordInstance.tags);				
				
				if(satisfied(charWordPoses, wordWordPoses)) {
					sameWriter.write(charInstance);
					if(goldSameFile != null && goldInstance != null)
						goldSameWriter.write(goldInstance);
					satisfiedNumber++;
				}
				
				charInstance = charReader.getNext();
				wordInstance = wordReader.getNext();
				
				if(goldFile != null) {
					goldInstance = goldReader.getNext();
				}
				count++;				
			}
			
			sameWriter.finishWriting();
			goldSameWriter.finishWriting();
			
			System.out.println("totally " + count +" sentences, " +
					"with " + satisfiedNumber + " same sentences\n");
		}
		catch (IOException e) {
			
		}
	}
	
	
	/**
	 * 去掉predictFile中相同的句子，同时去掉goldFile中对应的句子
	 * @since 2013-2-13
	 * @param predictFile
	 * @param goldFile
	 * @param predictOutputFile
	 * @param goldOutputFile
	 */
	public void removeReduplicate(String predictFile, String goldFile, 
			String predictOutputFile, String goldOutputFile) {
		
		try {
			Reader predictReader = new Reader();
			Options options = new Options();
			predictReader.startReading(predictFile, options);
			Instance predictInstance = predictReader.getNext();
			
			Reader goldReader = new Reader();
			goldReader.startReading(goldFile, options);
			Instance goldInstance = goldReader.getNext();
			
			Writer predictOutputWriter = new Writer();
			predictOutputWriter.startWriting(predictOutputFile, options);

			Writer goldOutputWriter = new Writer();
			goldOutputWriter.startWriting(goldOutputFile, options);
			int count = 0;
			
			HashMap<String, Integer> wordPos = new HashMap<String, Integer>();
			
			while(predictInstance != null && goldInstance != null) {
				String[] words = predictInstance.words;
				String[] tags = predictInstance.tags;
				StringBuffer strbuf = new StringBuffer();
				for(String word : words)
					strbuf.append(word+"+");
				for(String tag : tags)
					strbuf.append(tag+"+");
				
				if(!wordPos.containsKey(strbuf)) {
					predictOutputWriter.write(predictInstance);
					goldOutputWriter.write(goldInstance);
					count++;
				}
				
				predictInstance = predictReader.getNext();
				goldInstance = goldReader.getNext();
			}
			predictOutputWriter.finishWriting();
			goldOutputWriter.finishWriting();
			
			System.out.println("totally " + count +" same sentences");
		}
		catch (IOException e) {
			
		}		
	}
		
	
	public static void main(String[] args) {
		String dictFile = "/home/tm/disk/disk1/segpossemi/error-driven/ctb5-train-parse-wp";
		
		// -95minprob-20oovrate-5minlength
		String limitcond = "-20oovrate";
		
		String charFile = "/home/tm/disk/disk1/segpossemi/error-driven/train-wp-test-wp-charesult";
		String wordFile = "/home/tm/disk/disk1/segpossemi/error-driven/train-wp-test-wp-wordresult";
		String sameFile = "/home/tm/disk/disk1/segpossemi/error-driven/train-wp-test-wp-same" + limitcond;
		
		String goldFile = "/home/tm/disk/disk1/segpossemi/error-driven/ctb5-train-parse-wp";
		String goldSameFile = "/home/tm/disk/disk1/segpossemi/error-driven/ctb5-train-parse-wp-same" + limitcond;
		
		ExtractSame ext = new ExtractSame();
		ext.loadInWords(dictFile);
		ext.extractSentences(charFile, wordFile, sameFile, goldFile, goldSameFile);
		
		
		
	}
	
}

