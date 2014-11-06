package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.corpus.ptb.PTB;
import edu.hitsz.nlp.language.chinese.ChineseWord;

public class Py2CharEval {

	int sentNumber;
	int sentCorr;
	int all;
	int corr;
	int chnAll;
	int chnCorr;
	
	public Py2CharEval() {
	
	}
	
	/**
	 * 比较两个句子，统计正确字数
	 * @since Oct 21, 2012
	 * @param goldSentence
	 * @param predictSentence
	 */
	public void evalSentence(ArrayList<String> goldSentence, ArrayList<String> predictSentence) {
		
		int goldLength = goldSentence.size();
		int predictLength = predictSentence.size();
		if(goldLength != predictLength) {
			System.out.println("sentence length is different:\n " 
					+ "goldSentence:    " + goldSentence 
					+ "predictSentence: " + predictSentence);
			System.exit(-1);
		}
		
		if(goldLength > 0 ) {
		
			sentNumber++;
			boolean corrSig = true;
			
			for(int i=0; i<goldLength; i++) {
				String goldCharacter = goldSentence.get(i);
				all++;
				if(goldSentence.get(i).equals(predictSentence.get(i)))
					corr++;			
				else
					corrSig = false;
				if(goldCharacter.matches(ChineseWord.hanzisRegex)) {
					chnAll++;
					if(goldSentence.get(i).equals(predictSentence.get(i)))
						chnCorr++;		
				}
			}
			
			if(corrSig)
				sentCorr++;
		}
		
	}
	
	
	public void evalSentence(String goldSentence, String predictSentence) {
		
		ArrayList<String> goldWords = new ArrayList<String>();
		ArrayList<String> predictWords = new ArrayList<String>();
		for(int i=0; i<goldSentence.length(); i++) {
			String curChar = goldSentence.substring(i, i+1);
			if(curChar.trim().length() == 1)
				goldWords.add(curChar);
		}
			
		for(int i=0; i<predictSentence.length(); i++) {
			String predictChar = predictSentence.substring(i, i+1);
			if(predictChar.trim().length() == 1)
				predictWords.add(predictChar);
		}
		
		evalSentence(goldWords, predictWords);
		
	}
		
	
	
	public void evalOneFile(String inFile) {
		
	}

	
	/**
	 * 评价测试文件的字识别率
	 * @since Oct 21, 2012
	 * @param goldFileName
	 * @param predictFileName
	 */
	public void evalFiles(String goldFileName, String predictFileName) {
		
		try {
		
			String goldFileEncoding = FileEncoding.getCharset(goldFileName);
	        BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), goldFileEncoding));
	        String predictFileEncoding = FileEncoding.getCharset(predictFileName);
	        BufferedReader predictReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), predictFileEncoding));
	        
	        String goldLine = null;
	        String predictLine = null;
	        
	        while( (goldLine=goldReader.readLine()) != null && (predictLine = predictReader.readLine()) != null) {
	        	evalSentence(goldLine, predictLine);
	        }
	        	        
	        goldReader.close();
	        predictReader.close();
		
		}
		catch(IOException e) {
			
		}		
		
	}
	
	
	/**
	 * 判断两个句子是否相同
	 * @since Dec 23, 2012
	 * @param goldWords
	 * @param predictWords
	 * @return
	 */
	public static boolean isSameRawSentence(String[] goldWords, String[] predictWords) {
		
		StringBuffer goldBuf = new StringBuffer();
		for(String word : goldWords)
			goldBuf.append(word);
		String goldSentence = goldBuf.toString();
		StringBuffer predictBuf = new StringBuffer();
		for(String word : predictWords)
			predictBuf.append(word);
		String predictSentence = predictBuf.toString();
		
		return goldSentence.equals(predictSentence);
		
	}
	
	
	
	
	
	
	public void output() {
		
		System.out.println("all characters:         " + all);
		System.out.println("correct characters:     " + corr);
		System.out.println("precents:               " + corr/(double) all);

		System.out.println("all chn characters:     " + chnAll);
		System.out.println("correct chn characters: " + chnCorr);
		System.out.println("precents:               " + chnCorr/(double) chnAll);
		
		System.out.println("all sentences:          " + sentNumber);
		System.out.println("correct sentences:      " + sentCorr);
		System.out.println("precents:               " + sentCorr/(double) sentNumber);
	}
	
	
	
	public static void main(String[] args) {
		
		//String goldFile = "/home/tm/disk/disk1/pinyin2character/PD/data/dev";
		//String predictFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/dev.pinyins.result.mert";
		
		String goldFile = "/home/tm/disk/disk1/dev.gold-6";
		String predictFile = "/home/tm/disk/disk1/dev.rerank-6";
		// "/home/tm/disk/disk1/dev.seg.pinyins-result-mert.9-";
		// "/home/tm/disk/disk1/pinyin2character/Lcmc/rerank/lcmcdev_result-2-9-7";
		// "/home/tm/disk/disk1/pinyin2character/Lcmc/result/test.seg.pinyins-result-ngram";
		// "/home/tm/disk/disk1/pinyin2character/Lcmc/result/test.seg.pinyins-result-mert.9"
		//
		Py2CharEval eval = new Py2CharEval();
		eval.evalFiles(goldFile, predictFile);
		eval.evalSentence("", "");
		eval.output();
		
	}	
	
}
