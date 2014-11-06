package edu.hitsz.nlp.corpus.ptb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.java.math.NumbUtils;
import edu.hitsz.nlp.struct.CfgTree;

public class ExtractPTB {
	
	
	/**
	 * Duan 2007
	 * @since Dec 25, 2012
	 * @param dir
	 * @param outdir
	 */
	public static void extractPTBParse(String dir, String outdir) {
				
		try {			
			FileWriter trainWriter = new FileWriter(new File(outdir+"/ptb-train-parse"));
			FileWriter trainWordPosWriter = new FileWriter(new File(outdir+"/ptb-train-parse-wp"));
			FileWriter trainWordWriter = new FileWriter(new File(outdir+"/ptb-train-parse-w"));

			FileWriter devWriter22 = new FileWriter(new File(outdir+"/ptb-dev-parse22"));
			FileWriter devWordPosWriter22 = new FileWriter(new File(outdir+"/ptb-dev-parse-wp22"));	
			FileWriter devWordWriter22 = new FileWriter(new File(outdir+"/ptb-dev-parse-w22"));	

			FileWriter devWriter24 = new FileWriter(new File(outdir+"/ptb-dev-parse24"));
			FileWriter devWordPosWriter24 = new FileWriter(new File(outdir+"/ptb-dev-parse-wp24"));	
			FileWriter devWordWriter24 = new FileWriter(new File(outdir+"/ptb-dev-parse-w24"));	
			
			FileWriter testWriter = new FileWriter(new File(outdir+"/ptb-test-parse23"));
			FileWriter testWordPosWriter = new FileWriter(new File(outdir+"/ptb-test-parse-wp23"));
			FileWriter testWordWriter = new FileWriter(new File(outdir+"/ptb-test-parse-w23"));
			
			//train: 2-21
			for(int i=2; i<=21; i++) {				
				//String subdir = dir + File.separator + NumbUtils.addPrefixZeros(Integer.toString(i), 2);
				String subdir = dir + File.separator + NumbUtils.elimPrefixZeroInt(Integer.toString(i));	
				FileTree newTree = new FileTree();
		    	newTree.generateFrom(subdir);
		    	ArrayList<String> fileNames = newTree.getFileNames();
		    	for(String fileName : fileNames)
		    		extractParse(fileName, trainWriter, trainWordPosWriter, trainWordWriter);
			}
			
			//dev:22
			String subdir = dir + File.separator + Integer.toString(22);
			FileTree newTree = new FileTree();
	    	newTree.generateFrom(subdir);
	    	ArrayList<String> fileNames = newTree.getFileNames();
	    	for(String fileName : fileNames)
	    		extractParse(fileName, devWriter22, devWordPosWriter22, devWordWriter22);
	    	
			//dev: 24
	    	subdir = dir + File.separator + Integer.toString(24);
			newTree = new FileTree();
	    	newTree.generateFrom(subdir);
	    	fileNames = newTree.getFileNames();
	    	for(String fileName : fileNames)
	    		extractParse(fileName, devWriter24, devWordPosWriter24, devWordWriter24);

			//test: 23 
	    	subdir = dir + File.separator + Integer.toString(23);
			newTree = new FileTree();
	    	newTree.generateFrom(subdir);
	    	fileNames = newTree.getFileNames();
	    	for(String fileName : fileNames)
	    		extractParse(fileName, testWriter, testWordPosWriter, testWordWriter);
			
			trainWriter.close();
			trainWordPosWriter.close();
			trainWordWriter.close();
			
			devWriter22.close();
			devWordPosWriter22.close();
			devWordWriter22.close();
			
			devWriter24.close();
			devWordPosWriter24.close();
			devWordWriter24.close();
			
			testWriter.close();
			testWordPosWriter.close();
			testWordWriter.close();
				
		}
		catch(IOException e) {
			
		}
	}
	
	/**
	 * 从目录中抽取所有
	 * @since Aug 14, 2014
	 * @param pathName
	 * @param outFileName
	 */
	public static void extractPTBParseAll(String pathName, String outFileName) {
		try {
			FileWriter trainWriter = new FileWriter(new File(outFileName+"-ptb-parse"));
			FileWriter trainWordPosWriter = new FileWriter(new File(outFileName+"-ptb-wp"));
			FileWriter trainWordWriter = new FileWriter(new File(outFileName+"-ptb-w"));
			FileTree newTree = new FileTree();
	    	newTree.generateFrom(pathName);
	    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix("mrg");
	    	System.out.println("Sentence Number: " + fileNames.size());
	    	for(String fileName : fileNames) {
	    		System.out.println(fileName);
	    		if(!fileName.contains("readme"))
	    			extractParse(fileName, trainWriter, trainWordPosWriter, trainWordWriter);
	    	}
			trainWriter.close();
			trainWordPosWriter.close();
			trainWordWriter.close();
		}
		catch(IOException e) {
			
		}
	}
	
	
	
	
	/** 从文件中提取句法树，以Zhang and Clark 2008为规则修建句法树*/
	public static void extractParse(String fileName, FileWriter writer, FileWriter wordPosWriter, FileWriter wordWriter) {
		
		try {
			StringBuffer bf = new StringBuffer();
			String encoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			String line = null;
			while(true) {
				line = reader.readLine();
				//如果bf有句子，
				if(bf.toString().trim().length() > 0 && (line == null || line.trim().length() ==0 || line.startsWith("( (S"))) {
					String s = bf.toString().trim();
					if(s.length() > 0) {
						//System.out.print(number+++".");
						CfgTree tree = new CfgTree();
						tree.load(s, 0, s.length(), false);
						//tree.output();
						tree.cleanEmptyTerminal();
						//tree.output();
						tree.cleanNonTerminals();
						//tree.output();
						tree.collapseUnaryRule();
						//tree.output();	
						tree.store(writer);
						tree.storeWPColumn(wordPosWriter);
						tree.storeWordRow(wordWriter, true);
					}
					bf = new StringBuffer();
					if(line == null)
						break;
					bf.append(line.trim()+" ");
				}				
				else {
					bf.append(line.trim()+" ");
				}
			}
			reader.close();					
		}
		catch(IOException e) {			
		}		
	}
	
	
	public static void main(String[] args) {
		String ptbDir = "/home/tm/disk/disk1/pos-chunk-rerank/raw/wsj";		
		String ptbParseDir="/home/tm/disk/disk1/pos-chunk-rerank/raw";		
		//ExtractPTB.extractPTBParse(ptbDir, ptbParseDir);
		ptbDir= "/home/tm/disk/disk1/pos-chunk-rerank/raw/wsj";
		String ptbFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/ptb.wp";
		ExtractPTB.extractPTBParseAll(ptbDir, ptbFileName);
	}

}
