package edu.hitsz;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.struct.CfgTree;

public class Test {
	
	/** 从文件中提取句法树，以Zhang and Clark 2008为规则修建句法树*/
	public static String extractParse(String fileName, FileWriter writer, FileWriter wordPosWriter, FileWriter wordWriter) {
		try {
			StringBuffer allBf = new StringBuffer();
			StringBuffer bf = new StringBuffer();
			String encoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			String line = null;
			int number=0;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.startsWith("<") && !line.endsWith(">")) {
					bf.append(line+" ");
				}				
				else {
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
				}
			}
			reader.close();
			
			return bf.toString();
			
		}
		catch(IOException e) {
			
		}		
		return "";
	}
	
	public static void main(String[] args) throws IOException {
		String inputFileName = "/home/tm/disk/disk1/nnparse/ctb/results/";
		String outParseFileName = "/home/tm/disk/disk1/nnparse/ctb/results/train-parse";
		String outWPFileName = "/home/tm/disk/disk1/nnparse/ctb/results/train-wp";
		String outWFileName = "/home/tm/disk/disk1/nnparse/ctb/results/train-w";
		FileWriter outParserFile = new FileWriter(outParseFileName);
		FileWriter outWPFile = new FileWriter(outWPFileName);
		FileWriter outWFile = new FileWriter(outWFileName);
		Test.extractParse(inputFileName, outParserFile, outWPFile, outWFile);
		outParserFile.close();
		outWPFile.close();
		outWFile.close();
		
		
		
		
	}

}
