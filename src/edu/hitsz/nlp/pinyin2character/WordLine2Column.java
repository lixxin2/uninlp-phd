package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.hitsz.java.file.local.FileEncoding;



/**
 * 将一行的句子中的词按照conll类型分开
 * @author Xinxin Li
 * @since Dec 19, 2012
 */
public class WordLine2Column {
	
	
	/**
	 * 通过gold文件中的词，将test文件中分词，并全部转换为column格式
	 * @since Dec 19, 2012
	 * @param goldFileName
	 * @param testFileName
	 * @param goldOutFileName
	 * @param testOutFileName
	 */
	public static void convert(String goldFileName, 
			String testFileName,
			String goldOutFileName, 
			String testOutFileName) {
		
		try {
			
			String goldFileEncoding = FileEncoding.getCharset(goldFileName);
			BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), goldFileEncoding));
	        
			String testFileEncoding = FileEncoding.getCharset(testFileName);
			BufferedReader testReader = new BufferedReader(new InputStreamReader(new FileInputStream(testFileName), testFileEncoding));
	        
			FileWriter goldWriter = new FileWriter(goldOutFileName);
			FileWriter testWriter = new FileWriter(testOutFileName);
			
			String goldLine = null;
			String testLine = null;
			
			while( (goldLine = goldReader.readLine()) != null
					&& (testLine = testReader.readLine()) != null) {
				
				goldLine = goldLine.trim();
				testLine = testLine.trim();
				
				if(goldLine.length() > 0) {
					
					String[] goldWords = goldLine.split("\\s+");
					
					String[] testWords = getTestWords(goldWords, testLine);
					
					String goldSentence = combine(goldWords, "\n");
					String testSentence = combine(testWords, "\n");
					
					goldWriter.write(goldSentence + "\n\n");
					testWriter.write(testSentence + "\n\n");					
					
				}				
			}			
			
			goldReader.close();
			testReader.close();
			goldWriter.close();
			testWriter.close();
	        
		}
		catch (IOException e) {
			
		}		
	}
	
	
	public static String[] getTestWords(String[] goldWords, String testLine) {
		
		int length = goldWords.length;
		
		testLine = delim(testLine);
		String[] testWords = new String[length];
		
		int start = 0;
		int end = 0;
		
		for(int i=0; i<length; i++) {
			String goldWord = goldWords[i];
			end += goldWord.length();
			String testWord = testLine.substring(start, end);
			testWords[i] = testWord;
			start = end;						
		}
		
		return testWords;
	}
	
	
	public static String combine(String[] subWords, String split) {
		
		StringBuffer sbuf = new StringBuffer();
		int length = subWords.length;
		
		if(length > 0) {
			sbuf.append(subWords[0]);
			for(int i=1; i<length; i++) {
				sbuf.append(split);
				sbuf.append(subWords[i]);
			}		
		}
		
		return sbuf.toString();
		
	}
	
	
	public static String delim(String line) {
		
		StringBuffer sbuf = new StringBuffer();
		String[] words = line.split("\\s+");
		for(String word : words) 
			sbuf.append(word);
		return sbuf.toString();
		
	}
	
	
	public static void main(String[] args) {
		
		String goldFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/dev.seg.words";
		String testFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/dev.seg.pinyins-result-lm.seg.5.bin";
		String goldOutFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/dev.seg.words.column";
		String testOutFileName = "/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/dev.seg.pinyins-result-lm.seg.5.bin.column";
		
		WordLine2Column.convert(goldFileName, testFileName, goldOutFileName, testOutFileName);
		
		
	}
	

}
