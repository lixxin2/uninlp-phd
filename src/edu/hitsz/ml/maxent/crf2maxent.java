package edu.hitsz.ml.maxent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.ml.crf.Template;

/** 最大熵文件相关的操作*/
public class crf2maxent {

	
	
	/**
	 * 将crf训练文件和特征模板提取为maxent模型格式
	 * @since May 22, 2012
	 * @param oriFileName
	 * @param templFileName
	 * @param feaFileName
	 * @param B crf模板是否包含B，是指包含前一个状态
	 */
	public static void crf2maxentTrain(String oriFileName, String templFileName, String feaFileName, boolean B) {
		
		Template templ = new Template();
		templ.read(templFileName);
						
		try {
			String encoding = FileEncoding.getCharset(oriFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(oriFileName), encoding));
					
			FileWriter writer = new FileWriter(new File(feaFileName));
			
			int sentenceNumber = 0;
			
			ArrayList<String> lines = new ArrayList<String>();
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() <= 0) {
					sentenceNumber++ ;
					if(sentenceNumber % 10 == 1)
						System.out.print(sentenceNumber + "...");
					if(sentenceNumber % 100 == 1)
						System.out.println();
					
					int row = lines.size();
					int column = lines.get(lines.size()-1).split("[ \t]").length;
					
					ArrayList<ArrayList<String>> words = new ArrayList<ArrayList<String>>();
					for(int i=0; i<column; i++)
						words.add(new ArrayList<String>());
					
					for(int i=0; i<row; i++) {
						String[] parts = lines.get(i).split("[ \t]");
						int j=0;
						for(; j<parts.length && j<column; j++)
							words.get(j).add(parts[j]);
						if(j<column) {
							for(int k=j; k<column; k++)
								words.get(j).add("NONE");
						}
					}
										
					if(row > 0 && column > 0) {
						for(int j=0; j<row; j++) {
							String label = words.get(column-1).get(j);
							String preLabel = "NONE";
							if(j > 0) preLabel = words.get(column-1).get(j-1);
							ArrayList<String> features = templ.getFea(words, row, column, j);
							StringBuffer sb = new StringBuffer();
							for(String s : features) {
								sb.append(" ");sb.append(s);
							}
							if(B) {
								for(String s : features) {
									sb.append(" B");sb.append(preLabel);;sb.append(s);
								}
							}
							writer.write(label + sb.toString() + "\n");				
						}
					}
					lines.clear();
				}
				else {
					lines.add(line);
				}
			}			
			System.out.print(sentenceNumber);
			writer.close();		
			
		}
		catch (IOException e) {
			
		}
		
	}
	
	/**将crf训练文件和特征模板提取为maxent模型格式*/
	public static void crf2maxentTrain(String oriFileName, String templFileName, String feaFileName) {
		crf2maxentTrain(oriFileName, templFileName, feaFileName, false);
	}
	
	
	/**将crf测试文件和特征模板提取为maxent模型格式*/
	public static void crf2maxentTest(String oriFileName, String templFileName, String feaFileName) {
		Template templ = new Template();
		templ.read(templFileName);
						
		try {
			String encoding = FileEncoding.getCharset(oriFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(oriFileName), encoding));
					
			FileWriter writer = new FileWriter(new File(feaFileName));
			
			int sentenceNumber = 0;
			
			ArrayList<String> lines = new ArrayList<String>();
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() <= 0) {
					sentenceNumber++ ;
					if(sentenceNumber % 10 == 1)
						System.out.print(sentenceNumber + "...");
					if(sentenceNumber % 100 == 1)
						System.out.println();
					
					int row = lines.size();
					int column = lines.get(lines.size()-1).split("[ \t]").length;
					
					ArrayList<ArrayList<String>> words = new ArrayList<ArrayList<String>>();
					for(int i=0; i<column; i++)
						words.add(new ArrayList<String>());
					
					for(int i=0; i<row; i++) {
						String[] parts = lines.get(i).split("[ \t]");
						int j=0;
						for(; j<parts.length && j<column; j++)
							words.get(j).add(parts[j]);
						if(j<column) {
							for(int k=j; k<column; k++)
								words.get(j).add("NONE");
						}
					}
										
					if(row > 0 && column > 0) {
						for(int j=0; j<row; j++) {
							ArrayList<String> features = templ.getFea(words, row, column, j);
							StringBuffer sb = new StringBuffer();
							for(String s : features) {
								sb.append(s);sb.append(" ");
							}
							writer.write(sb.toString().trim() + "\n");				
						}
					}
					writer.write("\n");
					lines.clear();
				}
				else {
					lines.add(line);
				}
			}			
			System.out.print(sentenceNumber);
			writer.close();		
			
		}
		catch (IOException e) {
			
		}
	}
	
	public static void main(String[] args) {
		
		String trainFileName = "/home/tm/disk/disk1/nermusic/test-fea";
		String templFileName = "/home/tm/disk/disk1/nermusic/template.txt";
		String trainFeaFileName = "/home/tm/disk/disk1/nermusic/test-maxent-fea";
		trainFileName = "/home/tm/disk/disk1/nermusic/train-fea";
		templFileName = "/home/tm/disk/disk1/nermusic/template9.txt";
		trainFeaFileName = "/home/tm/disk/disk1/nermusic/train-maxent-fea";
		/*
		oriFileName = args[0];
		templFileName = args[1];
		feaFileName = args[2];
		*/
		crf2maxent.crf2maxentTrain(trainFileName, templFileName, trainFeaFileName);
		
		String testFileName = "/home/tm/disk/disk1/nermusic/test-fea";
		String testFeaFileName = "/home/tm/disk/disk1/nermusic/test-maxent-fea";
		//crf2maxent.crf2maxentTrain(testFileName, templFileName, testFeaFileName);//, true);
		
	}
	
	
	
	
}


