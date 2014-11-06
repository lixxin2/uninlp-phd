package edu.hitsz.nlp.subWordSegPOS;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.util.Array;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.segpos.Reader;
import edu.hitsz.nlp.segpos.Writer;

public class GenerateResults {
	
	/**
	 * 
	 * 合并subword文件中的第一列（子词序列）和subtag文件的第一列（tag序列），得到最后的sub word结果文件
	 * @since 2013-5-1
	 * @param subWordFileName subword词
	 * @param subTagFileName subword tag的
	 * 	 * @param resultFileName subword tag结果
	 * @throws IOException
	 */
	public void align(String subWordFileName, 
			String subTagFileName, 
			String resultFileName) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(subWordFileName);
		BufferedReader subWordReader = new BufferedReader(new InputStreamReader(new FileInputStream(subWordFileName), fileEncoding));
        
		fileEncoding = FileEncoding.getCharset(subTagFileName);
		BufferedReader subTagReader = new BufferedReader(new InputStreamReader(new FileInputStream(subTagFileName), fileEncoding));
        
		FileWriter writer = new FileWriter(resultFileName);
		
		
		String subWords = subWordReader.readLine();
		String subTag = subTagReader.readLine();
		
		boolean isSentenceEnd = false;
		while(subWords != null && subTag != null) {
			//每一行
			subWords = subWords.trim();
			subTag = subTag.trim();
			if(subWords.length() == 0) {
				isSentenceEnd = true;
				subWords = subWordReader.readLine().trim();
			}
			else
				isSentenceEnd = false;
			//
			String subWord = subWords.split("\\s+")[0];
			StringBuffer strbuf = new StringBuffer();
			if(isSentenceEnd)
				strbuf.append("\n");
			strbuf.append(subWord); strbuf.append("\t"); 
			strbuf.append(subTag);strbuf.append("\n");
			writer.write(strbuf.toString());	
			
			subWords = subWordReader.readLine();
			subTag = subTagReader.readLine();
		}
		writer.write("\n");
		
		subWordReader.close();
		subTagReader.close();
		writer.close();		
		
	}
	
	/**
	 * 将subword + subtag文件转换为word+tag文件
	 * @since Feb 25, 2013
	 * @param resultFileName
	 * @param finalFileName
	 * @throws IOException
	 */
	public void convert(String resultFileName, String finalFileName) throws IOException {
		
		Options options = new Options();
		//reader
		Reader reader = new Reader();
		reader.startReading(resultFileName, options);
		Instance instance = reader.getNext();
		//writer
		Writer writer = new Writer();
		writer.startWriting(finalFileName, options);
		
		//
		int count = 0;
		while(instance != null) {
			if(count % 100 == 0) {
				System.out.print(count+",");
				if(count % 1000 == 0)
					System.out.println();
			}
			count++;
			
			String[] subWords = instance.words;
			String[] subTags = instance.tags;
			
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<String> tags = new ArrayList<String>();
			
			for(int i=0; i<subTags.length; i++) {
				if(subTags[i].startsWith("B-")) {
					words.add(subWords[i]);
					tags.add(subTags[i].substring(2));
				}
				else if(subTags[i].startsWith("I-")) {
					int wordSize = words.size()-1;
					if(wordSize >= 0) {
						String preWord = words.get(wordSize);
						String preTag = tags.get(wordSize);
						//if(subTags[i].substring(2).matches(preTag)) {
							words.set(wordSize, preWord+subWords[i]);
						//}
					}
					else {
						words.add(subWords[i]);
						tags.add(subTags[i].substring(2));
					}
				}
				else {
					words.add(subWords[i]);
					tags.add(subTags[i]);
				}					
			}
			
			String[] subWordsVec = Array.toStringArray(words);
			String[] subTagsVec = Array.toStringArray(tags);
			
			Instance newInstance = new Instance(subWordsVec, subTagsVec);
			
			writer.write(newInstance);
			instance = reader.getNext();
		}
		
		writer.finishWriting();
		
	}
	
	
	public static void main(String[] args) throws IOException {
		
		String devCharSubWordFile = "/disk1/subWordSegPos/subWords/devSubWordFile";
		String strTypeFileName = "/disk1/subWordSegPos/subWords/devSubWordFile.strtags";
		String partResultFileName = "/disk1/subWordSegPos/subWords/devSubWordFile.subresult";
	
		GenerateResults genResults = new GenerateResults();
		genResults.align(devCharSubWordFile, strTypeFileName, partResultFileName);
		
		String finalResultFileName = "/disk1/subWordSegPos/subWords/devSubWordFile.result";
		genResults.convert(partResultFileName, finalResultFileName);
		
	}
	
	
	

}
