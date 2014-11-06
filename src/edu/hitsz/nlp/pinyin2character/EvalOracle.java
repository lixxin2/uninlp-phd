package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;



public class EvalOracle {
	
	/**
	 * 从candfile中选择与gold文件中每个句子中最相近的句子
	 * @since Dec 22, 2012
	 * @param goldFileName gold文件
	 * @param mertCandFileName mert格式的nbest文件
	 * @param n nbest的n，默认500
	 * @param bestOutFileName 从每个gold句子找到nbest中oracle最高的句子保存
	 */
	public static void select(String goldFileName, String mertCandFileName, int n, String bestOutFileName) {
				
		try {
			
			String goldFileEncoding = FileEncoding.getCharset(goldFileName);
			BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), goldFileEncoding));
	        
			String candFileEncoding = FileEncoding.getCharset(mertCandFileName);
			BufferedReader candReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertCandFileName), candFileEncoding));
	        
			FileWriter bestOutFileWriter = new FileWriter(bestOutFileName);
			
			int count = 8623;			

			String goldLine = null;
			String candLine = null;
			
			candLine = candReader.readLine();			
			
			while((goldLine = goldReader.readLine()) != null) {
								
				if(count % 100 == 0) {
					System.out.print(count+"...");
					if(count % 1000 == 0)
						System.out.println("\n");
				}
				
				String[] goldWords = goldLine.split("\\s+");
				String[] corCand = new String[goldWords.length];	
				double corProb = -1.0;		
				int curCount = 0; //现在k-best的数目
				
				while (candLine != null &&  
						Integer.parseInt(candLine.split("\\|\\|\\|")[0].trim()) == count) {
					String cand = candLine.split("\\|\\|\\|")[1].trim();				
					String[] candWords = WordLine2Column.getTestWords(goldWords, cand);

					//最优候选
					double tmpProb = evalSentence(goldWords, candWords);
					if(curCount < n && tmpProb > corProb) {
						corProb = tmpProb;
						corCand = candWords;
					}
					
					candLine = candReader.readLine();
					curCount++;		
				}
				count++;
				
				//bestOutFileWriter.write(WordLine2Column.combine(corCand, "\n") + "\n\n");
				bestOutFileWriter.write(WordLine2Column.combine(corCand, "\t") + "\n");			
			}
			
			goldReader.close();
			candReader.close();
			bestOutFileWriter.close();
		
		}
		catch (IOException e) {
			
		}
		
		
		
	}
	
	/**
	 *  计算两个句子相同词的数目
	 * @since Jan 28, 2013
	 * @param goldWords
	 * @param candWords
	 * @return
	 */
	public static double evalSentence(String[] goldWords, String[] candWords) {
		
		int length = goldWords.length;
		if(candWords.length != length) {
			System.out.println("length is different");
			System.exit(-1);
		}
		
		double cor = 0.0;
		
		for(int i=0; i<length; i++) {
			if(goldWords[i].equals(candWords[i]))
				cor += 1.0;
		}
		
		return cor/length;		
		
		
	}
	
	
	/**
	 * 评价句子
	 * @since Dec 22, 2012
	 * @param goldSentence
	 * @param candSentence
	 * @return
	 */
	public static double evalSentence(String goldSentence, String candSentence) {
		
		String[] goldWords = goldSentence.split("\\s+");
		String[] candWords = candSentence.split("\\s+");
		
		StringBuffer goldBuf = new StringBuffer();
		for(String word : goldWords)
			goldBuf.append(word);
		goldSentence = goldBuf.toString();
		StringBuffer candBuf = new StringBuffer();
		for(String word : candWords)
			candBuf.append(word);
		candSentence = candBuf.toString();
		
		int length = goldSentence.length();
		if(length != candSentence.length()) {
			
		}
		
		goldWords = new String[length];
		candWords = new String[length];
		
		for(int i=0; i<length; i++) {
			goldWords[i] = goldSentence.substring(i,i+1);
			candWords[i] = candSentence.substring(i,i+1);
		}
		
		return evalSentence(goldWords, candWords);
		
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		String goldFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/dev.seg.words";
		String mertCandFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/mert/cand_database_lcmcdev.txt";
		String nBestOutFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/result/dev.500best";
		
		EvalOracle oracle = new EvalOracle();
		
		oracle.select(goldFileName, mertCandFileName, 500, nBestOutFileName);
		
	}
	
	
	

}
