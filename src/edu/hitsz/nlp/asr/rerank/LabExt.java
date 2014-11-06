package edu.hitsz.nlp.asr.rerank;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.chinese.ChineseWord;

/** 
 * 语音识别相关的文本抽取
 * @author Xinxin Li
 * @since Nov 15, 2013
 */
public class LabExt {
	
	
	/**
	 * 抽取lab3信息，即文本信息
	 * <p> 一般都是GBK格式，在linux下可能有问题，包括863方言
	 * @since Nov 15, 2013
	 * @param lab3FileName
	 * @return
	 * @throws IOException 
	 */
	public List<String> ext863Lab3Line(String lab3FileName) throws IOException {
		
		List<String> lines = new ArrayList<String>();
		
		String fileEncoding = FileEncoding.getCharset(lab3FileName);
		//System.out.println(lab3FileName + ":" + fileEncoding);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lab3FileName), fileEncoding));        
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.trim().length() == 0)
				lines.add(line.trim());
		}		
		reader.close();
		
		return lines;		
	}
	
	/**
	 * 从列表文件中抽取每个lab3文件名，然后再从lab3文件中抽取出文本
	 * @since Nov 15, 2013
	 * @param listFileName list文件
	 * @param dirName list中的文件所在的输入目录
	 * @param lab3FileName 
	 * @return
	 * @throws IOException 
	 */
	public void ext863Lab3FromList(String listFileName, 
			String dirName, 
			String lab3FileName) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(listFileName);
		BufferedReader listReader = new BufferedReader(new InputStreamReader(new FileInputStream(listFileName), fileEncoding));
		
        // read list
		List<String> nameList = new ArrayList<String>();
		String line = null;
		while((line = listReader.readLine()) != null) {
			if(line.trim().length() > 0)
				nameList.add(line.trim());
		}
		listReader.close();
		
		//clean list
		List<String> cleanNameList = new ArrayList<String>();
		for(String name : nameList) {
			String tmpName = new File("/a"+name).getName();
			String cleanName = tmpName.split("\\.")[0] + ".lab3";
			cleanNameList.add(cleanName);
		}
		
		//extract lab3 text, store into lab3File
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(lab3FileName)), "GBK");
		for(String name : cleanNameList) {
			String fileName = dirName + File.separator + name;
			List<String> texts = ext863Lab3Line(fileName);
			for(String text : texts)
				writer.write(text + "\n");
		}		
		writer.close();		
	}
	
	
	
	/**
	 * 处理
	 * @since Nov 15, 2013
	 * @param inputFileName
	 * @param outFileName
	 * @throws IOException 
	 */
	public static void filterLab3(String inputFileName, String outputFileName) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(inputFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), fileEncoding));
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(outputFileName)), "GBK");
		
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			int sentLength = line.length();
			if(sentLength > 0) {
				String partLine = line.split("（")[0];
				String newLine = delNoChinese(partLine);
				if(newLine.length() == 0)
					newLine = partLine;
				writer.write(newLine + "\n");
			}
		}
		
		reader.close();
		writer.close();		
	}
	
	
	
	
	
	
	
	

	public static String delNoChinese(String sequence) {
		return sequence.replaceAll("[^\u3007\u4e00-\u9fa5]", "");
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		//String listFileName = "/home/tm/disk/disk1/asr-rerank/data/4/cq-list";
		//String dirName = "/home/tm/disk/disk1/asr-rerank/data/4/cq-lab3";
		//String lab3FileName = "/home/tm/disk/disk1/asr-rerank/data/4/cq-lab3.gold";
		
		String oldLabFileName = "/home/tm/windows/text/xm-lab3.gold.utf8.txt";
		String newLabFileName = "/home/tm/windows/text/xm-lab3.gold.utf8.clean.txt";
		
		LabExt ext = new LabExt();
		ext.filterLab3(oldLabFileName, newLabFileName);
		
	}	

}







