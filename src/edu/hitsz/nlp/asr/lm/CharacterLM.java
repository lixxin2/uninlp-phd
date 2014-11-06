package edu.hitsz.nlp.asr.lm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.language.chinese.ChineseWord;

public class CharacterLM {
	
	ArrayList<String> punts;
			
	public CharacterLM() {
		punts = new ArrayList<String>();
		for(String s : CTB.punctuations) {
			punts.add(s);
		}
	}
	
	/**
	 * 给文本每句话都按字分开，
	 * <p> old: 我们 安排Lida Li来 上课
	 * <p> new: 我 们 安 排 Lida Li 来 上 课
	 * @since Dec 2, 2012
	 * @param inFileName
	 * @param outFileName
	 */
	public void getCharacterFile(String inFileName, String outFileName) {
		
		try{
			System.out.println("inFile: " + inFileName);
			System.out.println("outFile: " + outFileName);
			String encoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), encoding));
			FileWriter writer = new FileWriter(outFileName);
			
			String line = null;
			
			while( (line=reader.readLine()) != null ) {
				
				line = line.trim();
				if(line.length() > 0) {
					
					String newLine = getCharacterLine(line);	
					
					writer.write(newLine+"\n");
				}							
			
			}

			reader.close();
			writer.close();	
		}
		
		catch (IOException e) {
			
		}		
		
	}
	
	
	public String getCharacterLine(String line) {
		
		StringBuffer sbuf = new StringBuffer();
		String[] subs = line.split("\\s+");
		boolean notHanzi = false;
		for(String sub : subs) {
			int i=0;
			int length = sub.length();						
			while(i<length) {
				String curChar = sub.substring(i, i+1);
				if(curChar.matches(ChineseWord.hanzisRegex) || punts.contains(curChar)) {
					if(notHanzi){
						sbuf.append(" " + curChar + " ");
						notHanzi = false;
					}
					else
						sbuf.append(curChar + " ");
				}
				else {
					sbuf.append(curChar);
					notHanzi = true;
				}
				i++;
			}
			if(notHanzi) {
				sbuf.append(" ");
				notHanzi = false;
			}
		}
						
		
		return sbuf.toString().trim();
		
	}
	
	public static void main(String[] args) {
		
		CharacterLM lm = new CharacterLM();
		String rawTextFile = "/home/tm/disk/disk1/lm/pdgm.minwordnum.seg5";
		String charTextFile = "/home/tm/disk/disk1/lm/pdgm.char.5";
		lm.getCharacterFile(rawTextFile, charTextFile);
		
		String rawText = "old： 我们 安排Lida Li来 上课";
		System.out.println(lm.getCharacterLine(rawText));
		
	}
	
	

}
