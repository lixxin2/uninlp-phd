package edu.hitsz.nlp.language.chinese;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.ds.AdjustableSizeArray;
import edu.hitsz.java.file.local.FileEncoding;

/**
 * 简繁字转换
 * @author Xinxin Li
 * @since Nov 28, 2013
 */
public class SimpleTraditionalConversion {

	HashMap<String, String> trad2SimpMap;
	
	public SimpleTraditionalConversion() {
		
		trad2SimpMap = new HashMap<String, String>();
	}
	
	
	/**
	 * 读取繁简字转换map
	 * @since Nov 28, 2012
	 * @param mapFileName
	 */
	public void loadTrad2SimpMap(String mapFileName) {
		
		try {
			String fileEncoding = FileEncoding.getCharset(mapFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mapFileName)), fileEncoding));
			
			String line = null;
			while ( (line=reader.readLine()) != null ){
				
				line = line.trim();
				int length = line.length();
				if(length == 0 || line.startsWith("//"))
					continue;
				
				String[] subs = line.split("\t");
				String simpWord =subs[0];
				String[] tradWords = subs[1].split(" ");
				for(String tradWord : tradWords) {
					if(trad2SimpMap.containsKey(tradWord) && !simpWord.equals(trad2SimpMap.get(tradWord))) {						
						System.out.println(tradWord + "\t" + trad2SimpMap.get(tradWord) + " " + simpWord);
					}
					trad2SimpMap.put(tradWord, simpWord);
				}
			}
			
			reader.close();			
		}
		catch (IOException e) {
			
		}
		
	}
	
	
	/**
	 * 获得对应的简体字，如没有，返回源字
	 * @since Nov 28, 2012
	 * @param word
	 * @return
	 */
	public String getSimpWord(String word) {
		
		if(!trad2SimpMap.containsKey(word))
			return word;
		else {
			return trad2SimpMap.get(word);
		}
		
	}
	
	
	/**
	 * 获取当前词对应的所有可能简体词（比如多个繁体字对应多个简体字）
	 * @since Nov 28, 2012
	 * @param word
	 * @return
	 */
	public ArrayList<String> getSimpWords(String word) {
				
		int length = word.length();
		String[][] words = new String[length][2]; 
		int[] wordNumber = new int[length];
		for(int i=0; i<length; i++) {
			String singleWord = word.substring(i, i+1);
			words[i][0] = singleWord;
			String simpWord = getSimpWord(singleWord);
			if(!simpWord.equals(singleWord)) {
				words[i][1] = simpWord;			
				wordNumber[i] = 2;
			}
			else {
				wordNumber[i] = 1;
			}
		}
		
		ArrayList<String> allWords = new ArrayList<String>();
		
		AdjustableSizeArray bArr = new AdjustableSizeArray(wordNumber);
		int allNumber = bArr.getNumber();
		for(int i=0; i<allNumber; i++) {
			int[] cur = bArr.getNext();
			StringBuffer sbuf = new StringBuffer();
			for(int j=0; j<length; j++) {
				sbuf.append(words[j][cur[j]]);
			}
			allWords.add(sbuf.toString());			
		}
		
		return allWords;		
		
	}
	
	
	/**
	 * 预处理简体字总表，把它转换为我们适合读取的格式
	 * 
	 * @since Nov 28, 2012
	 */
	public static void processFile() {
		
		String inFileName = "/home/tm/windows/simtrad/简化字总表.txt";
		String outFileName = "/home/tm/windows/simtrad/simptrad.txt";
		
		try {
			
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFileName)), fileEncoding));
			FileWriter writer = new FileWriter(outFileName);
			
			String line = null;
			int count = 0;
			
			while ( (line=reader.readLine()) != null ){
				count++;
				if(count == 150)
					System.out.println();				
				
				line = line.trim();
				int length = line.length();
				if(length == 1)
					continue;
				
				int i=0;
				while(i<length) {
					while(i<length && line.substring(i, i+1).equals(" "))
						i++;
					if(i<length) {
						String simpWord = line.substring(i, i+1);
						i++;
						while(i<length && !line.substring(i, i+1).equals("〔")) //找到"〔"的位置
							i++;
						i++;
						ArrayList<String> tradWords = new ArrayList<String>();
						while(i<length && !line.substring(i, i+1).equals("〕")) { //递归繁体字，直到"〕"
							while(i<length && line.substring(i, i+1).equals(" "))
								i++;
							tradWords.add(line.substring(i, i+1));
							i++;
						}
						i++;
						if(tradWords.size() > 0) {
							StringBuffer sbuf= new StringBuffer(simpWord + "\t");
							for(String word : tradWords) 
								sbuf.append(word+" ");
							writer.write(sbuf.toString().trim()+"\n");
						}
						else {
							System.out.println("simplified Word: " + simpWord);
						}
						
					}
				}						
			}
			
			reader.close();
			writer.close();		
			
		}
		catch (IOException e) {
			
		}
		
	}
	
	
	public static void main(String[] args) {
		
		//SimpleTraditionalConversion.processFile();
		
		SimpleTraditionalConversion st = new SimpleTraditionalConversion();
		st.loadTrad2SimpMap("/home/tm/windows/simtrad/simptrad.txt");
		ArrayList<String> allWords = st.getSimpWords("乾隆");
		for(String word : allWords)
			System.out.println(word);
		
	}
	
}
