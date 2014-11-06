package edu.hitsz.nlp.pinyin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import edu.hitsz.java.file.local.FileEncoding;

public class PyAlign {

	/** 对齐
	 * @since Aug 31, 2012
	 * @param wordFileName 分词好的句子： 中国 人民
	 * @param origPyFileName 原始的语音句子，没分词的： zhong guo ren min 
	 * @param outPyFileName 输出的语音句子，分词好的： zhong guo    ren min
	 * @param withWord true表示两行，false表示一行
	 */
	public void fileAlign(String wordFileName, String origPyFileName, String outPyFileName, boolean withWord) {
		
		try {
			
			String wordFileEncoding = FileEncoding.getCharset(wordFileName);
			BufferedReader wordReader = new BufferedReader(new InputStreamReader(new FileInputStream(wordFileName), wordFileEncoding));
	        
			String origPyFileEncoding = FileEncoding.getCharset(origPyFileName);
			BufferedReader origPyReader = new BufferedReader(new InputStreamReader(new FileInputStream(origPyFileName), origPyFileEncoding));
	        
			FileWriter outPyWriter = new FileWriter(outPyFileName);
			
			String wordLine = null;
			String origPyLine = null;
			
			int count = 0;
			while( (wordLine = wordReader.readLine()) != null
					&& (origPyLine = origPyReader.readLine()) != null) {
				
				count++;
				wordLine = wordLine.trim();
				origPyLine = origPyLine.trim();
				
				if(wordLine.length() > 0) {							
					String alignedPyLine = sentenceAlign(wordLine, origPyLine, count);	
					if(withWord) {
						String newWordLine = wordLine.replace(" ", "\t");
						outPyWriter.write(alignedPyLine + "\n" + newWordLine + "\n");
					}
					else
						outPyWriter.write(alignedPyLine + "\n");
				}				
			}			
			
			wordReader.close();
			origPyReader.close();
			outPyWriter.close();
	        
		}
		catch (IOException e) {
			
		}		
	}
		
	
	/** 对齐
	 * 
	 * @since Aug 31, 2012
	 * @param wordLine 分词好的句子： 中国 人民
	 * @param origPyLine 原始的语音句子，没分词的： zhong guo ren min 
	 * @return 输出的语音句子，分词好的： zhong guo    ren min 
	 */
	public String sentenceAlign(String wordLine, String origPyLine, int count) {
		
		StringBuffer strbuf = new StringBuffer();
		
		String[] words = wordLine.split("\\s+");
		String[] pinyins = origPyLine.split("\\s+");
		
		int length = 0;
		Map<Integer, Integer> wordEnd = new HashMap<Integer, Integer>();
		
		for(String word : words) {
			length += word.length();
			wordEnd.put(length-1, 1);
		}
		if(length != pinyins.length) {
			System.out.println("length is different in the " + count + " sentence.");
		}
		
		for(int i=0; i<length; i++) {
			strbuf.append(pinyins[i]);
			if(wordEnd.containsKey(i)) {
				strbuf.append("\t");
			}
			else
				strbuf.append(" ");
		}		
		return strbuf.toString().trim();
		
	}		
	
	
	
	public static void main(String[] args) {
		
		String wordFileName = "/home/tm/disk/disk1/pinyin2character/PD/data/train.nodict";
		String origPyFileName = "/home/tm/disk/disk1/pinyin2character/PD/data/train.pinyins";
		String outPyFileName = "/home/tm/disk/disk1/pinyin2character/PD/data/train.pinyinsword.align";
		PyAlign align = new PyAlign();
		align.fileAlign(wordFileName, origPyFileName, outPyFileName, true);
		
	}
	
	
	
}
