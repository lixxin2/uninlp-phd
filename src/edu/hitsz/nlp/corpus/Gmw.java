package edu.hitsz.nlp.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.sentence.SimpleChineseSegmentation;

/**
 * 光明日报
 * @author Xinxin Li
 * @since Aug 20, 2012
 */
public class Gmw {
	
	/**
	 * 从网页中抽取文本，这只是一种网页格式，格式很可能大不同
	 * 适合2006，2010
	 * @since Aug 20, 2012
	 * @param inFileName
	 * @param outFileName
	 * @throws IOException
	 */
	public static void extFile(String inFileName, String outFileName) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(inFileName);
		
		File htmlFile = new File(inFileName);
		Document doc = Jsoup.parse(htmlFile, fileEncoding);
		
		String titleString = "";
		String contentString = "";
		//获取标题和内容
		Elements contentWrapper = doc.select("div.contentWrapper");
		if(contentWrapper != null) {
			Elements contentLeft = contentWrapper.select("div.contentLeft");
			Elements articleTitle = contentLeft.select("#articleTitle");
			if(articleTitle != null) {
				Element title = articleTitle.first();
				if(title != null && title.hasText())
					titleString = title.text();
			}
			Elements contentMain = contentLeft.select("#contentMain");
			if(contentMain != null) {
				Element content = contentMain.first();
				if(content != null && content.hasText())
					contentString = content.text();
			}
			//分句
			ArrayList<String> allContents = new ArrayList<String>();
			SimpleChineseSegmentation scs = new SimpleChineseSegmentation();
			if(titleString.length() > 0)
				allContents.addAll(scs.getSegs(titleString));
			if(contentString.length() > 0)
				allContents.addAll(scs.getSegs(contentString));
			//存储
			FileWriter writer = new FileWriter(outFileName, true);
			for(String s : allContents) {
				writer.write(s+"\n");
			}		
			writer.close();
		}
		
	}
	
	
	/**
//	 * 抽取整个目录的文件，按月份存储/data3/web/gmw/www.gmw.cn/01gmrb/2004-01/23/content_1031302.htm
	 * @since Aug 20, 2012
	 * @param inFileDir
	 * @param outFileDir
	 * @throws IOException
	 */
	public static void extDir(String inFileDir, String outFileDir) throws IOException {
		
		FileTree ft = new FileTree(inFileDir);
		ArrayList<String> fileNames = ft.getFileNamesWithSuffix("htm");
		for(String fileName : fileNames) {
			if(new File(fileName).getName().startsWith("content")) {
				String[] fullFileName = fileName.split(File.separator);
				String outFileName = outFileDir + File.separator + fullFileName[fullFileName.length-3];
				//System.out.println(outFileName);
				extFile(fileName, outFileName);
			}
		}		
	}
	
	public static void usage() {
		System.out.println("-f|d infileName|infileDir outfileName|outfileDir");
		System.exit(-1);
	}
	
	public static void run(String[] args) throws IOException {
		if(args.length != 3) {
			usage();		
		}
		
		if(args[0].equals("-f")) {
			Gmw.extFile(args[1], args[2]);
		}
		else if (args[0].equals("-d")) {
			Gmw.extDir(args[1], args[2]);
		}
		else {
			usage();
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		
		Gmw.run(args);
		
		
	}
	
	

}
