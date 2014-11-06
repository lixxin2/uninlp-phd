package edu.hitsz.nlp.asr.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.corpus.ptb.PTB;


/**
 * 将文本转换为Htk,srilm训练的格式
 * @author Xinxin Li
 * @since Aug 12, 2012
 */
public class TextProcess {

	ArrayList<String> punts;
	ArrayList<String> endpunts;
		
	
	public TextProcess() {
		punts = new ArrayList<String>();
		for(String s : CTB.punctuations) {
			punts.add(s);
		}
		
		endpunts = new ArrayList<String>();
		for(String s : CTB.endPunctuations) {
			endpunts.add(s);
		}
		for(String s : PTB.endPunctuations) {
			endpunts.add(s);
		}
	}
	
	
	/**
	 * 将原始文本转换为Cmu, Srilm训练时需要的文本格式
	 * @since Aug 12, 2012
	 * @param inFileName
	 * @param outFileName
	 * @param forward 是否正向输出
	 * @param sentenceSignal 是否添加 \<\s\> ,\<\/s\>表示，因为CMU需要，而srilm不需要
	 * @param deleteEnglish 是否删除英文
	 * @param changeNumber 是否转换数字
	 */
	public void raw2LMText(String inFileName, String outFileName, boolean forward, boolean sentenceSignal,
			boolean deleteEnglish, boolean changeNumber){		
		
		//设置起始点，因为有的文件中句子的第一个词是该句子的属性		
		
		try{
			System.out.println("inFile: " + inFileName);
			System.out.println("outFile: " + outFileName);
			String encoding = FileEncoding.getCharset(inFileName);
			BufferedReader newReader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), encoding));
			FileWriter newWriter = new FileWriter(outFileName);
			String line = null;
			int lineNumber = 0;
			while((line = newReader.readLine()) != null){
				if(lineNumber%10000 == 0)
					System.out.print("line:"+lineNumber+",");
				//System.out.println(lineNumber);
				//if(lineNumber == 48410)
				//	System.out.println();
				lineNumber++;
				if(line.trim().length() == 0)
					continue;
				//上面得到了整个句子，接下来需要进行很多处理，包括：
				
				//这是人民日报的处理方式
				//ArrayList<String> sentences = segSentence(delPRCPOSTags(line));
				
				//其他常用文本的处理
				ArrayList<ArrayList<String>> words = segSentence(splitWords(line));
				
				int count=0;
				//递归每个分句
				for(ArrayList<String> singleWords : words) {		
					//System.out.println(count);
					//if(count == 401)
					//	System.out.println();
					count++;
					
					if(deleteEnglish)
						singleWords = deleteEnglish(singleWords);
					if(changeNumber) {
						singleWords = changeNumber(singleWords);
					}
					if(!forward)
						singleWords = backwardSentence(singleWords);
					
					singleWords = keepChinese(singleWords);
					
					String s = getSingleSentence(singleWords);
					
					if(s.trim().length() > 0) {
						if(sentenceSignal)
							newWriter.write("<s> " + s.trim() + " </s>\n");
						else
							newWriter.write(s.trim() + "\n");
					}					
				}
			}
			System.out.println("line:"+lineNumber);
			newReader.close();
			newWriter.close();
		}
		catch (IOException e){
			System.out.println("IOException" + e);
		}		
	}
	

	
	/**
	 * 人民日报有词性标示的话，则去掉标示
	 * @since Jul 30, 2012
	 * @param line
	 */
	private ArrayList<String> delPRCPOSTags(String line) {

		boolean delBracket = true;	//删除[标示，因为他可能和词在一起出现，比如“[出现/V”
		String delimiter = "/";		//词和词性分隔符
		
		int start = 0;
		ArrayList<String> words = new ArrayList<String>();		
		//replace("//O  o/O  ]/O  nz/O", "]/O")处理2to6中的问题
		String newLine = line.trim().replace("//O  o/O  ]/O  nz/O", "]/O");
		String[] wordpos = newLine.trim().split(" ");				
		int wordNumber = wordpos.length;
		for(int i=start; i<wordNumber; i++){
			//如果是199801.txt文件，则去掉注释，因为他有一个词是
			if(i == 0)
				continue;			
			
			if(wordpos[i].trim().length()>0){						
				String[] parts = wordpos[i].split(delimiter);
				
				String word = parts[0];
				
				//处理199801
				if(delBracket && parts[0].contains("["))
					word = word.substring(1);
				
				words.add(word);
			}
		}
		return words;
	}
	
	
	/**
	 * 分句，将整个句子分开
	 * @since Jul 30, 2012
	 * @param line
	 */
	private ArrayList<String> splitWords(String line) {

		ArrayList<String> allWords = new ArrayList<String>();
		
		String[] words = line.trim().split("[ \t]");				
		for(String s : words){
			if(s.trim().length() > 0)
				allWords.add(s);
		}		
		return allWords;
	}
	
	
	
	
	/** 
	 * 按照标点符号来分割句子
	 * @since Jul 30, 2012
	 * @param words
	 * @param punts
	 * @param endpunts
	 * @return
	 */
	private ArrayList<ArrayList<String>> segSentence(ArrayList<String> words) {
				
		ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
		ArrayList<String> firstline = new ArrayList<String>();
		for(String word : words) {
			if(word.trim().length()>0){											
				if(endpunts.contains(word)) {
					if(firstline.size() > 0)
						lines.add(firstline);
					firstline = new ArrayList<String>();
				}
				else if(!endpunts.contains(word) && punts.contains(word)) {
					continue;
				}
				else {
					if(!punts.contains(word)) 
					firstline.add(word);
				}
			}
		}
		if(firstline.size() > 0)
			lines.add(firstline);		
		return lines;
	}
	
	
	/**
	 * 处理数字
	 * @since Jul 31, 2012
	 * @param words
	 * @return
	 */
	public ArrayList<String> changeNumber(ArrayList<String> words) {
		
		ArrayList<String> newWords = new ArrayList<String>();
		DigitNumber dn = new DigitNumber(" ");
		for(String word : words) {
			//如果匹配数字0
			if(word.matches(dn.numberRegex)) {				
				int length = word.length();
				for(int i=0; i<length-1; i++){
					if(length > 1 && word.substring(0,1).equals("0") && word.substring(1,2).matches("[0-9]"))
						word = word.substring(1);
					else
						break;
				}
				newWords.add(word);				
			}
			else
				newWords.add(word);
		}
		int length = newWords.size();
		//
		for(int i=0; i<length; i++) {
			newWords.set(i, dn.getWord(newWords, i));
		}
		
		return newWords;
	}
	
	
	/**
	 * 删除英文单词
	 * @since Jul 31, 2012
	 * @param words
	 * @return
	 */
	public ArrayList<String> deleteEnglish(ArrayList<String> words) {
		ArrayList<String> newWords = new ArrayList<String>();
		for(String word : words) {
			if(!word.matches("[a-zA-Z]*"))
				newWords.add(word);				
		}
		return newWords;
	}
	
	/**
	 * 只保留中文
	 * @since Jul 31, 2012
	 * @param words
	 * @return
	 */
	public ArrayList<String> keepChinese(ArrayList<String> words) {
		ArrayList<String> newWords = new ArrayList<String>();
		for(String word : words) {
			if(word.matches("[\u4e00-\u9fa5 ]+"))
				newWords.add(word);				
		}
		return newWords;
	}
	
	
	
	
	/**
	 * 反转句子
	 * @param words
	 * @return
	 */
	public ArrayList<String> backwardSentence(ArrayList<String> words){
		ArrayList<String> newWords= new ArrayList<String>();
		int wordSize = words.size();
		for(int i=wordSize-1; i>=0; i--)
			newWords.add(words.get(i));		
		return newWords;		
	}

	/**
	 * 根据词得到对应的句子
	 * @since Jul 31, 2012
	 * @param words
	 * @return
	 */
	public String getSingleSentence(ArrayList<String> words) {

		StringBuffer sb = new StringBuffer();
		int wordSize = words.size();
		if(wordSize > 0) {
			sb.append(words.get(0));
			for(int i=1; i<wordSize; i++)
				sb.append(" "+words.get(i));	
		}
		return sb.toString();
	}
	
	
	
	public static void v22() {
		long before = System.currentTimeMillis();
		System.out.println("start: " + (new Date()));
		TextProcess tp = new TextProcess();
		int count = 0;
		
		for(int i=0; i<11660000; i+=10000) {
			System.out.println(i+": ");
			String input = "/media/main/v22/"+Integer.toString(i);
			String out = "/media/main/v22Raw/"+Integer.toString(i);
			if(new File(input).exists())
				tp.raw2LMText(input, out, true, false, true, true);
			//if(count == 0)
			//	break;
			count++;
		}
		/*
		String input = "/media/学习资料/corpora/sogou/SogouCS.seg";
		String out = "/media/学习资料/corpora/sogou/SogouCS.text";
		tp.raw2LMText(input, out, true, false);
		
		input = "/media/学习资料/corpora/sogou/SogouCA.seg";
		out = "/media/学习资料/corpora/sogou/SogouCA.text";
		tp.raw2LMText(input, out, true, false);
		*/
		long now = System.currentTimeMillis();
		System.out.println("time: " + (now - before) / 1000.0 + " s");
		
	}
	
	
	/** 
	 * 执行命令，
	 * @since Aug 19, 2012
	 * @param args
	 */
	public static void excute(String[] args) {
		if(args.length != 3) {
			System.out.println("-f|-d inFile|inDir outFile|outDir");
			System.exit(-1);
		}
		
		if(args[0].equals("-f")) {
			TextProcess r2t = new TextProcess();
			r2t.raw2LMText(args[1], args[2], true, false, true, true);				
		}
		else if(args[0].equals("-d")) {
			FileTree ft =new FileTree(args[1]);
			ArrayList<String> fileNames = ft.getFileNames();
			TextProcess r2t = new TextProcess();
			for(String fileName : fileNames) {
				String outFileName = args[2] + File.separator + new File(fileName).getName();
				r2t.raw2LMText(fileName, outFileName, true, false, true, true);			
			}		
		}
		else {
			System.out.println("-f|-d inFile|inDir outFile|outDir");
			System.exit(-1);
		}
		
		
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		excute(args);
		
		
		
	}
	
	
	
}
