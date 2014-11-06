package edu.hitsz.nlp.corpus.pd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.corpus.ctb.CTB;

public class PeopleDaily {

	
	/**
	 * 从原来的文件转换为Conll格式的文件，需要设置句子的起始词位置
	 * <p> 1998 01 02-06
	 * @param inFileName
	 * @param outFileName
	 */
	public static void line2Conll(String inFileName, String outFileName){		
		String delimiter = "/";
		boolean delBracket = true;
		//设置起始点，因为有的文件中句子的第一个词是该句子的属性
		int start = 0;
		try{
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader newReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFileName)), fileEncoding));
			FileWriter newWriter = new FileWriter(outFileName);
			String line = null;
			int lineNumber = 0;
			while((line = newReader.readLine()) != null){
				if(lineNumber%1000 == 0)
					System.out.println("line:"+lineNumber);
				lineNumber++;
				if(line.trim().length() == 0)
					continue;
				//replace("//O  o/O  ]/O  nz/O", "]/O")处理2to6中的问题
				String newLine = line.trim().replace("//O  o/O  ]/O  nz/O", "]/O");
				String[] words = newLine.trim().split("\\s+");				
				int wordNumber = words.length;				
				StringBuffer newBuffer = new StringBuffer();
				for(int i=start; i<wordNumber; i++){
					if(words[i].trim().length()>0){						
						String[] parts = words[i].split(delimiter);
						//处理2to6
						if(delBracket && ( parts[0].equals("[") || parts[0].equals("]")))
							continue;
						//处理199801
						if(delBracket && parts[0].contains("["))
							newBuffer.append(parts[0].substring(1)+"\t");
						else
							newBuffer.append(parts[0]+"\t");
						//处理199801.txt中“电台/n]nt”的问题
						if(parts[1].contains("]")){
							int position = parts[1].indexOf("]");
							newBuffer.append(parts[1].substring(position+1)+"\n");
							if(!delBracket)
								newBuffer.append("]\t"+parts[1].substring(0, position)+"\n");							
						}
						else{
							newBuffer.append(parts[1]+"\n");
						}
					}
				}
				newBuffer.append("\n");
				newWriter.write(newBuffer.toString());
			}
			newReader.close();
			newWriter.close();
		}
		catch (IOException e){
			System.out.println("IOException" + e);
		}		
	}
	
	

	
	
	
	
	/**
	 * 从2007年网页中提取文本
	 * @since Jun 13, 2012
	 * @param inFileName
	 * @param outFileName
	 * @param style
	 * @throws IOException 
	 */
	public void html2txtPeoplesDaily2007(String htmlDir, String txtDir, String style) throws IOException {
		FileTree newTree = new FileTree();
    	newTree.generateFrom(htmlDir);
    	ArrayList<String> fileNames = newTree.getFileNames();	
    	
    	int number = 0;
    	
    	for(String htmlFileName : fileNames) {
    		
    		if(number % 1000 == 0) {
    			System.out.println(number + "\n");
    		}
    		else if(number % 100 == 0) {
    			System.out.print(number +".");
    		}    		
    		number++;    	
    		
    		String[] dirs = htmlFileName.split(File.separator);
    		int dirsLength = dirs.length;		
    		
    		//String textPath = txtDir + File.separator + dirs[dirsLength-2];
    		
    		//if(!(new File(textPath).isDirectory()))
    		//	new File(textPath).mkdir();
    		
    		//String textFileName = textPath + File.separator + dirs[dirsLength-1];
    		
    		String textFileName = txtDir + File.separator + dirs[dirsLength-2]+".txt";
    		
    		File htmlFile = new File(htmlFileName);
    		Document doc = Jsoup.parse(htmlFile, "gb2312");
    		String text = doc.body().text();
    		
    		ArrayList<String> sentences = preprocess(text);
    		if(sentences.size() > 0) {
	    		FileWriter writer = new FileWriter(new File(textFileName), true);	    		
	    		for(String s : sentences) {
	    			if(style.equals("column")) {
		    			StringBuffer sbuf = new StringBuffer();
		    			for(int i=0; i<s.length(); i++) {
		    				sbuf.append(s.substring(i, i+1)+"\n");
		    			}
		    			writer.write(sbuf.toString()+"\n");
	    			}
	    			else if(style.equals("row"))
	    				writer.write(s+"\n");
	    		}	    			
	    		writer.close();	    		
    		}   		
    		
    	}
    	
    	
	}
	
	
	
	/**
	 * 预处理，将原文本处理为一句话一行的文本
	 * @since May 5, 2012
	 * @param text
	 * @return
	 */
	private ArrayList<String> preprocess(String text) {
		
		ArrayList<String> sentences = new ArrayList<String>();
		String[] sents = text.split("[ \\t]");		
		int sentSize = sents.length;
				
		for(int i=0; i<sentSize; i++) {
			if(sents[i].length()>10 && !sents[i].equals("【人民日报】") && !sents[i].equals("人民日报社新闻信息中心")) {
				String senttmp = sents[i].replace("。", "。\t").replace("？", "？\t").replace("！", "！\t");
				String sent = replaceEnglishBlank(senttmp);
				String[] subsent = sent.split("[ \t]");
				for(String s : subsent) {
					sentences.add(backEnglishBlank(s));	
				}
			}		
		}
		
		return sentences;
		
	}
	
	
	/**
	 * 保存英文单词之间的空格，用____替换
	 * @since May 5, 2012
	 * @param text
	 * @return
	 */
	private String replaceEnglishBlank(String text) {
		StringBuffer sb = new StringBuffer();
		int textLength = text.length();
		for(int i=0; i<textLength; i++) {
			String pre = "Null";
			if(i-1>=0)
				pre = text.substring(i-1, i);
			String cur = text.substring(i, i+1);
			String next = "Null";
			if(i+1 < textLength)
				next = text.substring(i+1, i+2);
			if(cur.matches("[  ]")) {
				//如果前面的词是标点符号，则不处理
				if(pre.matches("[、，：；。？！]"))
					continue;
				if(!pre.matches("[。？！]") && next.matches("[[(（《]]"))
					continue;
				//左右词都为非中文
				if(pre.matches("[0-9a-zA-Z]") || next.matches("[0-9a-zA-Z]")) {
					sb.append("____");
					continue;
				}				
			}
			sb.append(cur);
		}
		return sb.toString();
		
	}
	
	/**
	 * 将英文词之间的"____"替换为"_"
	 * @since May 5, 2012
	 * @param s
	 * @return
	 */
	private String backEnglishBlank(String s) {
		return s.replace("____", " ").trim();
	}
	
	
	/**
	 * 将列的句子（一列一词，句间有空行）改为一句一行
	 * @since Nov 29, 2012
	 * @param inFileName
	 * @param outFileName
	 */
	public static void combSentence(String inFileName, String outFileName) {
		
		try{
			String fileEncoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFileName)), fileEncoding));
			FileWriter writer = new FileWriter(outFileName);
			String line = null;
			int lineNumber = 0;
			
			while ( (line=reader.readLine()) != null ) {
				
				line = line.trim();
				if(line.length() == 0) 
					writer.write("\n");
				else
					writer.write(line);
			}
			
			reader.close();	
			writer.close();
				
		}
		catch (IOException e) {
			
		}
		
	}
	
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//line2Conll("/media/study/corpora/treebank/peoplesDaily/199801.txt","/media/study/corpora/treebank/peoplesDaily/199801.text");
		//PeopleDaily pd = new PeopleDaily();
		//pd.html2txtPeoplesDaily2007("/home/tm/disk/disk1/segpossemi/人民日报下载版", "/home/tm/disk/disk1/segpossemi/peopledaily2007", "column");
		//pd.html2txtPeoplesDaily2007("/home/tm/disk/disk1/人民日报下载版", "/home/tm/windows/asr/dict/lm/pd2007", "row");
		
		combSentence("/media/study/corpora/treebank/peoplesDaily/19980206-w","/media/study/corpora/treebank/peoplesDaily/19980206-s");
		
	}

}
