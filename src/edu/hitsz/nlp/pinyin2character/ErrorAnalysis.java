package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.chinese.ChineseWord;
import edu.hitsz.nlp.segpos.BasicWordPos;
import edu.hitsz.nlp.segpos.Evaluation;
import edu.hitsz.nlp.util.Array;

public class ErrorAnalysis {

	/**
	 * 评价不同长度的词的正确性
	 * @since Apr 13, 2013
	 * @param wordFileName
	 * @param goldFileName
	 * @param predictFileName
	 * @param evalFileName
	 */
	public static void wordAcc(
			String wordFileName,
			String goldFileName, 
			String predictFileName, 
			String evalFileName) {
				
		//读取已有词表
		HashMap<String, Integer> words = new HashMap<String, Integer>();
		String wordFileEncoding = FileEncoding.getCharset(wordFileName);
		
		try {
			BufferedReader wordReader = new BufferedReader(new InputStreamReader(new FileInputStream(wordFileName), wordFileEncoding));
			String line = null;
			while((line = wordReader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					String[] subs = line.split("\\s+");
					words.put(subs[0], 1);
				}			
			}
			wordReader.close();
		}
		catch (IOException e) {
			
		}
		
		
		Evaluation sEval = new Evaluation();
		
		String goldFileEncoding = FileEncoding.getCharset(goldFileName);
		String predictFileEncoding = FileEncoding.getCharset(predictFileName);
		
		//分别表示词的长度（0, 1，2，3，>=4，>=1),
		//是否为已有词（IV，OOV），
		//是否正确（）		
		int[][][] goldNumber = new int[6][2][2]; 
		
		try {
			BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), goldFileEncoding));
			BufferedReader predictReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), predictFileEncoding));
			
			//int count = 0;			
			String goldLine = null;
			String predictLine = null;
			
			while((goldLine = goldReader.readLine()) != null
				&& (predictLine = predictReader.readLine()) != null) {
					
				String[] goldWords = goldLine.split("\\s+");
				//String[] predictWords = predictLine.split("\\s+");
				
				StringBuffer sb = new StringBuffer();
				for(String word : predictLine.split("\\s+"))
					sb.append(word.trim());
				predictLine = sb.toString();       //预测的词
				
				BasicWordPos[] goldWordPoses = sEval.getWordPoses(goldWords);
				//BasicWordPos[] predictWordPoses = sEval.getWordPoses(predictWords);
								
				//对于每个gold的词
				for(int i = 0; i < goldWordPoses.length; i++) {
					String word = goldWordPoses[i].getWord();
					if(word.matches(ChineseWord.hanzisRegex)) {
						int s = goldWordPoses[i].getStart();
						int t = goldWordPoses[i].getEnd();
						int wordLength = t-s+1;
						int cor = 1;   //是否正确,1表示不正确
						if(predictLine.substring(s, t+1).equals(word)) 
							cor = 0;
						
						int iv = words.containsKey(word) ? 0 : 1; //0表示IV， 1表示OOV
						
						if(wordLength < 4)
							goldNumber[wordLength][iv][cor] += 1;
						if(wordLength >= 4) 
							goldNumber[4][iv][cor] += 1;
						goldNumber[5][iv][cor] += 1;
					}
				}
			}
					
	        goldReader.close();
	        predictReader.close();
	        
	        //分别表示词的长度（0, 1，2，3，4，2-5，6-, 2-,1-),是否为已有词（IV，OOV），是否正确（）
	        System.out.println("Length\tiv\tcor\tnumber\tallnum\tacc");
	        //FileWriter writer = new FileWriter(evalFileName);
	        for(int i=1; i<6; i++) {
	        	for(int j=0; j<2; j++) {
	        		for(int k=0; k<2; k++) {
	        			System.out.print(i+"\t"+j+"\t"+k+"\t"+goldNumber[i][j][k]+"\t");
	        			System.out.print(goldNumber[i][j][0]+goldNumber[i][j][1]);
	        			System.out.print("\t");
	        		}	  
	        		System.out.println(goldNumber[i][j][0]/(double)(goldNumber[i][j][0]+goldNumber[i][j][1]));
	        	}
	        }
	        //writer.close();
			
		}
		catch (IOException e) {
			
		}		
	}
	
	/** 统计错误的词 */
	public static void wordErrStat(
			String wordFileName,
			String goldFileName, 
			String predictFileName,
			String predict2FileName, 
			String evalFileName) {
				
		//读取已有词表
		HashMap<String, Integer> words = new HashMap<String, Integer>();
		String wordFileEncoding = FileEncoding.getCharset(wordFileName);
		
		try {
			BufferedReader wordReader = new BufferedReader(new InputStreamReader(new FileInputStream(wordFileName), wordFileEncoding));
			String line = null;
			while((line = wordReader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					String[] subs = line.split("\\s+");
					words.put(subs[0], 1);
				}			
			}
			wordReader.close();
		}
		catch (IOException e) {
			
		}

		HashMap<String, HashMap<String, Integer>> predAllNumMap = new HashMap<String, HashMap<String, Integer>>();		
		
		Evaluation sEval = new Evaluation();
		
		String goldFileEncoding = FileEncoding.getCharset(goldFileName);
		String predictFileEncoding = FileEncoding.getCharset(predictFileName);
		String predict2FileEncoding = FileEncoding.getCharset(predict2FileName);
		
		try {
			BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), goldFileEncoding));
			BufferedReader predictReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), predictFileEncoding));
			BufferedReader predict2Reader = new BufferedReader(new InputStreamReader(new FileInputStream(predict2FileName), predict2FileEncoding));
			FileWriter writer = new FileWriter(evalFileName);
			
			//int count = 0;			
			String goldLine = null;
			String predictLine = null;
			String predict2Line = null;
			
			while((goldLine = goldReader.readLine()) != null
				&& (predictLine = predictReader.readLine()) != null
				&& (predict2Line = predict2Reader.readLine()) != null) {
					
				String[] goldWords = goldLine.split("\\s+");
				ArrayList<ArrayList<String>> predictWord = new ArrayList<ArrayList<String>>();
				predictWord.add(Array.toArrayList(predictLine.split("\\s+")));
				ArrayList<ArrayList<String>> predict2Word = new ArrayList<ArrayList<String>>();
				predict2Word.add(Array.toArrayList(predict2Line.split("\\s+")));
				
				StringBuffer sb = new StringBuffer();
				for(String word : predictLine.split("\\s+"))
					sb.append(word.trim());
				predictLine = sb.toString();       //预测的词
				StringBuffer sb2 = new StringBuffer();
				for(String word : predict2Line.split("\\s+"))
					sb2.append(word.trim());
				predict2Line = sb2.toString();       //预测的词
				
				BasicWordPos[] goldWordPoses = sEval.getWordPoses(goldWords);
				
				//对于每个gold的词
				for(int i = 0; i < goldWordPoses.length; i++) {
					String word = goldWordPoses[i].getWord();
					if(word.matches(ChineseWord.hanzisRegex)) {
						int s = goldWordPoses[i].getStart();
						int t = goldWordPoses[i].getEnd();
						String pred1Word = predictLine.substring(s, t+1);
						String pred2Word = predict2Line.substring(s, t+1);
						//第一个预测是否正确
						int cor1 = 1;   //是否正确,1表示不正确
						if(pred1Word.equals(word)) 
							cor1 = 0;
						//第二个预测是否正确
						int cor2=1;
						if(pred2Word.equals(word))
							cor2 = 0;
						
						//第一个预测不正确，第二个预测正确
						if(cor1 != cor2) {
							writer.write(
									word + "\t" + goldLine + "\n" 
									+ pred1Word + "\t" + predictLine + "\n"
									+ pred2Word + "\t" + predict2Line + "\n\n");													
						}
						//如果不包含该词
						if(!predAllNumMap.containsKey(word)) {
							predAllNumMap.put(word, new HashMap<String, Integer>());
						}
						else {
							//
							if(!(word.equals(pred1Word) && word.equals(pred2Word))) {
								HashMap<String, Integer> wordNumMap = predAllNumMap.get(word);
								
								String wordNum = pred1Word + ":" + pred2Word;
								if(!wordNumMap.containsKey(wordNum)) {
									wordNumMap.put(wordNum, 1);
								}
								else {
									wordNumMap.put(wordNum, wordNumMap.get(wordNum)+1);
								}	
							}
						}					
					}
				}
			}
					
			Iterator<Entry<String, HashMap<String, Integer>>> iter2 = predAllNumMap.entrySet().iterator();
			while(iter2.hasNext()) {
				Entry<String, HashMap<String, Integer>> wordNum = iter2.next();
				String word = wordNum.getKey();
				HashMap<String, Integer> num = wordNum.getValue();
				Iterator<Entry<String, Integer>> iter3 = num.entrySet().iterator();
				while(iter3.hasNext()) {
					Entry<String, Integer> freq = iter3.next();
					String freqWord = freq.getKey();
					int freqNum = freq.getValue();
					if(freqNum >= 5)
						writer.write(word+":"+freqWord+":"+freqNum+"\n");
				}
			}
						
	        goldReader.close();
	        predictReader.close();
	        predict2Reader.close();
	        writer.close();			
		}
		catch (IOException e) {
			
		}		
	}
	
	
	
	/**
	 * 根据每句话中词的数目来分割句子 
	 * @since Aug 26, 2013
	 * @param infile
	 * @throws IOException 
	 */
	public static void splitByWordNumber(String fileName1, String fileName2) throws IOException {
		
		String fileEncoding1 = FileEncoding.getCharset(fileName1);
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(fileName1), fileEncoding1));
		
		String fileEncoding2 = FileEncoding.getCharset(fileName2);
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(fileName2), fileEncoding2));
        
		String line1 = null;
		String line2 = null;
		        
        FileWriter writer101 = new FileWriter(fileName1+"-1");
        FileWriter writer102 = new FileWriter(fileName1+"-2");
        FileWriter writer103 = new FileWriter(fileName1+"-3");
        FileWriter writer104 = new FileWriter(fileName1+"-4");
        FileWriter writer105 = new FileWriter(fileName1+"-5");
        FileWriter writer106 = new FileWriter(fileName1+"-6");
        
        FileWriter writer201 = new FileWriter(fileName2+"-1");
        FileWriter writer202 = new FileWriter(fileName2+"-2");
        FileWriter writer203 = new FileWriter(fileName2+"-3");
        FileWriter writer204 = new FileWriter(fileName2+"-4");
        FileWriter writer205 = new FileWriter(fileName2+"-5");
        FileWriter writer206 = new FileWriter(fileName2+"-6");
        
        while((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
        	line1 = line1.trim();
        	line2 = line2.trim();
        	if(line1.length() > 0 && line2.length() > 0) {
        		String[] words = line1.split("\\s+");
        		if(words.length == 1) {
        			writer101.write(line1+"\n");
        			writer201.write(line2+"\n");
        		}
        		else if(words.length == 2) {
        			writer102.write(line1+"\n");
        			writer202.write(line2+"\n");
        		}
        		else if(words.length == 3) {
        			writer103.write(line1+"\n");
        			writer203.write(line2+"\n");
        		}
        		else if(words.length == 4) {
        			writer104.write(line1+"\n");
        			writer204.write(line2+"\n");
        		}
        		else if(words.length == 5) {
        			writer105.write(line1+"\n");
        			writer205.write(line2+"\n");
        		}
        		else {
        			writer106.write(line1+"\n");
        			writer206.write(line2+"\n");
        		}
        	}
        }
		
		reader1.close();
		reader2.close();
		writer101.close();
		writer102.close();
		writer103.close();
		writer104.close();
		writer105.close();
		writer106.close();
		writer201.close();
		writer202.close();
		writer203.close();
		writer204.close();
		writer205.close();
		writer206.close();		
	}
	
	
	
	
	public static void main(String[] args) throws IOException {

		String wordFileName = "/home/tm/disk/disk1/lm/dict/wordlist5";
		String goldFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/dev.seg.words";
		String predictFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/result/dev.seg.pinyins-result-mert.9-";
		String predict2FileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/rerank/lcmcdev_result-2-9-7";
		
		// "/home/tm/disk/disk1/pinyin2character/Lcmc/result/dev.seg.pinyins-result-ngram";
		// "/home/tm/disk/disk1/pinyin2character/Lcmc/result/dev.seg.pinyins-result-mert.9-";
		// "/home/tm/disk/disk1/pinyin2character/Lcmc/rerank/lcmcdev_result-2-9-7";
		//String evalFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/result/dev.erroranalysis.mert";
		
		
		//ErrorAnalysis.wordAcc(wordFileName, goldFileName, predictFileName, evalFileName);
		//ErrorAnalysis.wordErrStat(wordFileName, goldFileName, predictFileName, predict2FileName, evalFileName);
		
		String fileName1 = "/home/tm/disk/disk1/dev.gold.2";
		String fileName2 = "/home/tm/disk/disk1/dev.rerank";
		ErrorAnalysis.splitByWordNumber(fileName1, fileName2);
		
	}	
	
}
