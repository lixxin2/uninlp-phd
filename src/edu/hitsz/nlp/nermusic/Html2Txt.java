package edu.hitsz.nlp.nermusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;

/**
 * 从下载的网页中中抽取出新闻文本
 * <p> 首先html2textSohu从html中将文本提取出来
 * <p> comb将标注好的文本合成一个文件
 * @author Xinxin Li
 * @since May 5, 2012
 */
public class Html2Txt {

	/**
	 * 将音乐新闻网页中将文本抽取出来
	 * @since May 4, 2012
	 * @param htmlDir
	 * @param textDir
	 * @throws IOException
	 */
	public void html2textSohuMusic(String htmlDir, String txtDir) throws IOException {
		
		FileTree newTree = new FileTree();
    	newTree.generateFrom(htmlDir);
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix("html");    	
    	
    	for(String htmlFileName : fileNames) {
    		
    		String[] dirs = htmlFileName.split("/");
    		int dirsLength = dirs.length;		
    		
    		String textFileName = txtDir + "/" + dirs[dirsLength-2] + ".txt"; //+ "-"+ dirs[dirsLength-1] + ".txt";
    		
    		File htmlFile = new File(htmlFileName);
    		Document doc = Jsoup.parse(htmlFile, "GB18030");
    		String text = doc.body().text();
    		
    		ArrayList<String> sentences = preprocess(text);
    		if(sentences.size() > 0) {
	    		FileWriter writer = new FileWriter(new File(textFileName), true);
	    		writer.write("<DOCUMENT>\n");
	    		for(String s : sentences)
	    			writer.write(s+"\n");
	    		writer.write("<\\DOCUMENT>\n");
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
		
		String nText = replaceEnglishBlank(text.replace("。", "。\t").replace("？", "？\t").replace("！", "！\t"));
		
		
		ArrayList<String> sentences = new ArrayList<String>();
		String[] sents = nText.split("[ \\t]");
		
		int sentSize = sents.length;
		
		String pre = "";
		
		for(int i=0; i<sentSize; i++) {
			String sent = sents[i];
			if(sent.matches(".+[。？！]") && sent.length() > 30 && !sent.matches(".+语言文明。")) {// && !sent.matches("[^\u4e00-\u9fa5]*")) {
				String sentence = pre + sent;
				String[] sub = sentence.split("\t");
				for(String s : sub) {
					sentences.add(backEnglishBlank(s));				   
				}
				pre = "";
			}
			else {
				if(sent.matches(".+[、,，:：；。？！]")) {
					pre += sent;
					continue;
				}
				else {
					pre = "";
					continue;
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
	 * 将多个文件合并为一个文件，并把其中的断句（没有以。？！结束，并且以>结束的）合并
	 * <p> 主要是解决标注中的问题
	 * @since May 8, 2012
	 * @param dir
	 * @param fileName
	 */
	public static void comb(String dir, String outFileName) {
		FileTree newTree = new FileTree();
    	newTree.generateFrom(dir);
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix("txt");
    	boolean entire = false;
    	String sentence = "";
    	try {
    		FileWriter writer = new FileWriter(new File(outFileName));
    		
	    	for(String fileName : fileNames) {
	    		String encoding = FileEncoding.getCharset(fileName); 
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding)); 
				//writer.write(fileName + "\n");
				String line = null;
				while((line = reader.readLine()) != null) {
					if(line.equals("<DOCUMENT>") || line.equals("<\\DOCUMENT>"))
						continue;
					if(line.trim().matches(".*[。？！]")) {
						entire = true;
						sentence += line.trim();
					}
					else {
						sentence += line.trim();
					}
					
					if(entire) {						
						//String[] sub = sentence.split("[。？！]");
						//for(String s : sub)
						//	writer.write(s + "\n");			
						writer.write(sentence + "\n");
						entire = false;
						sentence = "";
					}
				}				
				reader.close();	
	    		
	    	}
	    	writer.close();
    	}
    	catch(IOException e) {
    		
    	}
		
	}
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		Html2Txt h2t = new Html2Txt();
		/*
    	String htmlDir = "/home/tm/Downloads/site/music.yule.sohu.com";
    	String txtDir = "/home/tm/disk/disk1/nermusic/music.yule.sohu.com";    	
    	h2t.html2textSohu(htmlDir, txtDir);
    	*/
		//h2t.comb("/home/tm/disk/disk1/nermusic/data/annotated-2011-20120504/train","/home/tm/disk/disk1/nermusic/train");
		h2t.comb("/home/tm/disk/disk1/nermusic/data/annotated-2011-20120504/test","/home/tm/disk/disk1/nermusic/test");
    	
    	
	}
	
	
	
	
	
	
	
	
}
