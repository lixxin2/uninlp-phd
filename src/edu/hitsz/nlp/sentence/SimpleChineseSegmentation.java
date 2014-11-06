package edu.hitsz.nlp.sentence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.util.Array;

/**
 * 简单的分割句子，只根据"。？！"三种标点符号进行分割 
 * @author Xinxin Li
 * @since Aug 18, 2012
 */
public class SimpleChineseSegmentation {
	
	public ArrayList<String> sentenceSegPuncts;
	
	public SimpleChineseSegmentation() {
		sentenceSegPuncts = new ArrayList<String>();
		sentenceSegPuncts.addAll(Array.toArrayList(CTB.endPunctuations));
		
	}
	
	/**
	 * 分割句子
	 * @since Aug 18, 2012
	 * @param inFileName
	 * @param outFileName
	 */
	public void sentenceSeg(String inFileName, String outFileName) {
		System.out.println("input file: "+inFileName);
		System.out.println("output file: "+outFileName);
		
		String fileEncoding  = FileEncoding.getCharset(inFileName);
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), fileEncoding));
			FileWriter writer = new FileWriter(outFileName);
			//int count = 0;
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				int length = line.length();
				if(length < 1)
					continue;
				ArrayList<String> sentences = getSegs(line);
				for(String sentence : sentences) {
					writer.write(sentence + "\n");
				}					
			}
			
			reader.close();
			writer.close();
			
		}
		catch (IOException e) {
			
		}
		
	}
	
	public ArrayList<String> getSegs(String line) {
		int length = line.length();
		//标点符号索引
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		indexes.add(-1);
		for(int i=0; i<length; i++) {
			if(sentenceSegPuncts.contains(line.substring(i, i+1)))
				indexes.add(i);						
		}
		indexes.add(length-1);
		//存储子句
		ArrayList<String> sentences = new ArrayList<String>();
		for(int j=0; j<indexes.size()-1; j++) {
			String sentence = line.substring(indexes.get(j)+1, indexes.get(j+1)+1);
			if(sentence.length() > 0) {
				sentences.add(sentence.trim());
			}
		}
		
		return sentences;
	}
	
	
	public String getSeg(String line) {
		int length = line.length();
		//标点符号索引
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		indexes.add(-1);
		for(int i=0; i<length; i++) {
			if(sentenceSegPuncts.contains(line.substring(i, i+1)))
				indexes.add(i);						
		}
		indexes.add(length-1);
		//存储子句
		StringBuffer sb = new StringBuffer();
		for(int j=0; j<indexes.size()-1; j++) {
			String sentence = line.substring(indexes.get(j)+1, indexes.get(j+1)+1);
			if(sentence.length() > 0) {
				sb.append(sentence+"\n");
			}
		}		
		return sb.toString();
	}
	
	
	/**
	 * 对文件夹中的
	 * @since Aug 18, 2012
	 * @param inFileDir
	 * @param outFileDir
	 */
	public void sentencesSeg(String inFileDir, String outFileDir) {
		
		FileTree ftree = new FileTree(inFileDir);
		ArrayList<String> fileNames = ftree.getFileNames();
		
		for(String fileName : fileNames) {
			String outFileName = outFileDir + File.separator + new File(fileName).getName();
			sentenceSeg(fileName, outFileName);			
		}		
	}
	
	
	public static void usage() {
		System.out.println("-f|d infileName|infileDir outfileName|outfileDir");
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		
		if(args.length != 3) {
			usage();		
		}
		
		if(args[0].equals("-f")) {
			SimpleChineseSegmentation cs = new SimpleChineseSegmentation();
			cs.sentenceSeg(args[1], args[2]);
		}
		else if (args[0].equals("-d")) {
			SimpleChineseSegmentation cs = new SimpleChineseSegmentation();
			cs.sentencesSeg(args[1], args[2]);
		}
		else {
			usage();
		}		
		
		
	}
	
	
	
	

}
