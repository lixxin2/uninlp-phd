package edu.hitsz.nlp.sentence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;



public class ChineseSegmentationPuncts {

	/**
	 * 根据标点符号分割句子，用于中文
	 * @param pharagraphString 段落的字符串
	 */
	public static ArrayList<String> pharagraph2sentence(String pharagraphString){
		
		ArrayList<String> pucts = new ArrayList<String>();
		pucts.add("，");pucts.add("。");pucts.add("；");
		pucts.add("？");pucts.add("！");pucts.add("：");

		//ArrayList<String> deleteSymbols= new ArrayList<String>();
		//deleteSymbols.add(" ");deleteSymbols.add("　");deleteSymbols.add("\n");deleteSymbols.add("\t");

		ArrayList<String> sentences = new ArrayList<String>();
		StringBuffer newBuffer = new StringBuffer();
		for(int i=0; i<pharagraphString.length();i++){

			String singleChar = pharagraphString.substring(i,i+1);
			//if(deleteSymbols.contains(singleChar)){
			//	continue;
			//}
			if(!pucts.contains(singleChar)){
				newBuffer.append(singleChar);
			}
			else{
				newBuffer.append(singleChar);
				sentences.add(newBuffer.toString().trim());
				newBuffer = new StringBuffer();
			}
		}
		String lastSentence = newBuffer.toString().trim();
		if(lastSentence.length() > 0)
			sentences.add(lastSentence);
		return sentences;
	}


	/**
	 * 根据标点符号分割句子
	 */
	public void pharagraph2sentence(String inputFileName, String outFileName){
		
		StringBuffer allString = new StringBuffer();
		//读取文件
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(inputFileName)));
			FileWriter writer = new FileWriter(outFileName);
			String tempString = null;
			while ((tempString = reader.readLine())!= null){
				ArrayList<String> splitLines =  pharagraph2sentence(tempString.trim());
				outputSentences(writer, splitLines);
			}
			reader.close();
			writer.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		//输出

	}

	/**
	 * 将多行字符串输出到文件
	 * @param sentence
	 * @param outFileName
	 */
	public void outputSentences(FileWriter writer, ArrayList<String> sentences){
		try{
			for(int i=0; i<sentences.size(); i++)
				writer.write(sentences.get(i) + "\n");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 源文件按每行一字排列，然后分割
	 * <li> 根据 
	 * <li>
	 * @since 2013-2-9
	 * @param inFile
	 * @param outFile
	 */
	public static void segFile(String inFile, String outFile) {
		ArrayList<String> puncts = new ArrayList<String>();
		puncts.add("。");puncts.add("？");puncts.add("！");
		
		try {
			String fileEncoding = FileEncoding.getCharset(inFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), fileEncoding));
			FileWriter writer = new FileWriter(outFile);
			String line = null;
			boolean preEnd = false;
			int count = 0;
			
			while( (line = reader.readLine()) != null) {
				if(count % 10000 == 0) {
					System.out.print(count+"...");
					if(count % 100000 == 0)
						System.out.println();
				}
				count++;
				
				line = line.trim();
				if(line.length() == 0) {       //if blank line
					if(!preEnd) {               //if pre line is not blank
						writer.write("\n");
						preEnd = true;
					}
				}
				else if(puncts.contains(line)) {   //if line contains end puncts
					writer.write(line+"\n\n");
					preEnd = true;
				}
				else {
					writer.write(line+"\n");
					preEnd = false;
				}
			}			
			reader.close();
			writer.close();
		}
		catch (IOException e) {
			
		}
		
		
	}
	
	public static void main(String[] args) {
		
		ChineseSegmentationPuncts chn = new ChineseSegmentationPuncts();
		//chn.pharagraph2sentence("/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/test-1", 
		//		"/home/tm/disk/disk1/pinyin2character/Lcmc/data/p2c-data/test.seg1");
		chn.segFile("/disk1/segpossemi/data/19980106-c", "/disk1/segpossemi/data/19980106-c-seg");
	}



}
