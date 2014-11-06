package edu.hitsz.nlp.sharedtask.i2b2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;

public class TextPreprocess {
	
	/**
	 * extract text and tag from xml file
	 * @param inFile
	 * @param outSentFile
	 * @param outTagFile
	 */
	public static void xml2raw(String inFile, String outSentFile, String outTagFile) {
		long lasting =System.currentTimeMillis();
		
		try {		
			System.out.println(inFile);
	 		File f=new File(inFile);
	 		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	 		DocumentBuilder builder=factory.newDocumentBuilder();
	 		Document doc = builder.parse(f);
	 		
	 		NodeList deIdi2b2_node = doc.getElementsByTagName("deIdi2b2");
	 		Element deIdi2b2_element = (Element) deIdi2b2_node.item(0);
	 		
	 		// text
	 		NodeList text_node = deIdi2b2_element.getElementsByTagName("TEXT");
	 		String all_text =  text_node.item(0).getTextContent();    //包含所有字符的字符串
	 		int length = all_text.length();
	 		int i=0; 
	 		int j=0;
	 		ArrayList<String> sentences = new ArrayList<String>();
	 		HashMap<Integer, Integer> int2int = new HashMap<Integer, Integer>();
	 		String pre = "";
	 		StringBuffer strbuf = new StringBuffer();
	 		StringBuffer allstrbuf = new StringBuffer();
	 		//每个字符
	 		while(i<length) {
	 			String cur = all_text.substring(i, i+1);
	 			//如果\n,则换新句子
	 			if(cur.equals("\n")) {
	 				String curSentence = strbuf.toString().trim();
	 				if(curSentence.length() > 0) {
	 					if(i+2 < length && all_text.substring(i+1, i+2).equals("\n")
	 							&& all_text.substring(i+2, i+3).equals("\n"))
	 						sentences.add(curSentence + "\n");
	 					else
	 						sentences.add(curSentence);	 					
	 				}
	 				strbuf = new StringBuffer();
	 			}
	 			//其他字符,则句子中添加
	 			else {
	 				//
	 				if(!(cur.equals(" ") && cur.equals(pre)))	 					
	 					strbuf.append(cur);
	 			}
 				int2int.put(i, j);
	 			if(cur.trim().length() > 0) {
	 				j += 1;
	 			}
	 			i += 1;	 
	 			pre = cur;
	 			//所有非空字符
	 			if(cur.trim().length() > 0)
	 				allstrbuf.append(cur);
	 		}
	 		String curSentence = strbuf.toString().trim();
			if(curSentence.length() > 0) 
				sentences.add(curSentence);
			for(int k=0; k<sentences.size(); k++) {
				String sentence = sentences.get(k);
				//sentence = process(sentence);
				sentences.set(k, sentence);
			}
			String allString = allstrbuf.toString();        //去掉空格后的字符串
			
			// 写入句子
			FileWriter sentWriter = new FileWriter(outSentFile);	
			sentWriter.write("Begin.\n\n");       //for splitta to detect sentence boundary
			int sent_size = sentences.size();
			for(i=0; i<sent_size; i++) {
				//int sent_length = sentences.get(i).length();
				String sentence = sentences.get(i);
				if(sentence.endsWith("\n"))
					sentWriter.write(sentence);
				else
					sentWriter.write(sentence.trim() + " ");
			}				
			sentWriter.close();
	 
			//
			FileWriter tagWriter = new FileWriter(outTagFile);
	 		// tags
	 		NodeList tag_node = deIdi2b2_element.getElementsByTagName("TAGS");
	 		Element tag = (Element) tag_node.item(0);
 			NodeList type_node = tag.getElementsByTagName("*");
 			int tag_number = type_node.getLength();
 			for(int k=0; k<tag_number; k++) {
 				Element type_element = (Element) type_node.item(k);
 				String id = type_element.getAttribute("id");
 				String startString = type_element.getAttribute("start");
 				int start = Integer.parseInt(startString);
 				String endString = type_element.getAttribute("end");
 				int end = Integer.parseInt(endString);
 				String text = type_element.getAttribute("text");   //标注的tag
 				String type = type_element.getAttribute("TYPE");
 				String init_text = "";
 				String[] textsubs = text.split("[ \t\n]");
 				for(String textsub : textsubs)                     //标注的tag去掉\t\n
 					init_text += textsub; 				
 				
 				String cur_text = all_text.substring(start, end);  //原始文本的tag
 				int startAfter = int2int.get(start);
 				int endAfter = int2int.get(end);
 				String after_text = allString.substring(startAfter, endAfter); //原始文本去掉\t\n的tag
 				
 				if(!init_text.equals(after_text)) {
 					System.out.println("text: " + text + ", init_text: " + init_text + 
 							", cur_text: " + cur_text + ", after_text: " + after_text);
 					//System.exit(-1);
 				} 				
 				tagWriter.write(int2int.get(start) + "\t" + int2int.get(end) + "\t" + type + "\n"); 				
 			}
 			tagWriter.close();
	 		
		}catch(Exception e){
	 		e.printStackTrace();
		}
		System.out.println("运行时间："+(System.currentTimeMillis() - lasting)+" 毫秒");
	
	}
	
	public static void xml2rawDir(String inDir, String outDir) {
		FileTree newTree = new FileTree();
    	newTree.generateFrom(inDir);
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix("xml");
    	for(String fileName : fileNames) {
    		String outFileName = outDir + File.separator + new File(fileName).getName();
    		String outRawFileName = outFileName + ".raw";
    		String outTagFileName = outFileName + ".tag";
    		xml2raw(fileName, outRawFileName, outTagFileName);    		
    	}		
	}
	
	public static void raw2conll(String inSegFileName, String inTagFileName, String outFileName) throws IOException {
		
		FileWriter writer = new FileWriter(new File(outFileName));
		
		//read tag: start, end, tag
		Map<Integer, Integer> startList = new HashMap<Integer, Integer>();
		Map<Integer, Integer> midList = new HashMap<Integer, Integer>();
		Map<Integer, Integer> endList = new HashMap<Integer, Integer>();
		ArrayList<String> tagList = new ArrayList<String>();
		if(inTagFileName != null) {
			String fileEncoding = FileEncoding.getCharset(inTagFileName);		
			BufferedReader inTagFile = new BufferedReader(new InputStreamReader(new FileInputStream(inTagFileName), fileEncoding));
			String line = null;
			int count = 0;
			while((line = inTagFile.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					String[] subs = line.split("\t");
					int start = Integer.parseInt(subs[0]);
					int end = Integer.parseInt(subs[1]);
					String tag = subs[2];
					//if(!tag.equals("DATE") && !tag.equals("AGE")) {
						startList.put(start, count);
						for(int i=start+1; i<end; i++)
							midList.put(i, count);
						endList.put(end, count);
						tagList.add(tag);
						count += 1;
					//}
				}
			}
			inTagFile.close();
		}
		
		//
		String fileEncoding = FileEncoding.getCharset(inSegFileName);		
		BufferedReader inSegFile = new BufferedReader(new InputStreamReader(new FileInputStream(inSegFileName), fileEncoding));
		String line = inSegFile.readLine();
		line = inSegFile.readLine();
		int count = 0;
		while((line = inSegFile.readLine()) != null) {
			line = line.trim();
			if(line.length() > 0) {
				ArrayList<String> wordList1 = new ArrayList<String>();
				boolean bNewSent = true;
				int sent_position = count;
				while(line != null && bNewSent) {
					line = line.trim();
					if(line.length() > 0) {
						String[] words = line.split(" ");
						//read sentence length
						for(String word : words)
							sent_position += word.length();
						if(midList.containsKey(sent_position)) {
							line = inSegFile.readLine();
							System.out.println("read a new line: " + line);
							bNewSent = true;
						}
						else
							bNewSent = false;
						//store all words
						for(int i=0; i< words.length-1; i++) 
							wordList1.add(words[i]);
						String lastWord = words[words.length-1];
						if(lastWord.substring(lastWord.length()-1).equals(".") && 
								lastWord.length() > 1) {							
							wordList1.add(lastWord.substring(0,lastWord.length()-1));
							wordList1.add(lastWord.substring(lastWord.length()-1));
						}
						else
							wordList1.add(lastWord);
					}
					else{
						line = inSegFile.readLine();
						System.out.println("read a new line");
					}
				}
				
				//
				ArrayList<String> wordList = new ArrayList<String>();
				for(String word : wordList1) {
					ArrayList<String> newWords = processWord(word);
					for(String newWord : newWords) {
						newWord = newWord.trim();
						if(newWord.length() > 0)
							wordList.add(newWord.trim());
					}
				}				
				
				if(inTagFileName != null) {
					//detect word boundary
					for(int i=0; i< wordList.size(); i++) {
						String word = wordList.get(i);
						int current = count + word.length();
						//System.out.println(word + "," + count + "," + current);
						String isTagStart = "O"; 
						String isTagEnd = "O";
						if(startList.containsKey(count)) 
							isTagStart = "S";
						else if(midList.containsKey(count))
							isTagStart = "M";
						if(midList.containsKey(current))
							isTagEnd = "M";
						else if(endList.containsKey(current))
							isTagEnd = "E";
						//
						String tag = "O";
						if(isTagStart.equals("S")) {
							int index = startList.get(count);
							tag = tagList.get(index);
							if(isTagEnd.equals("M")) 
								tag = "B-" + tag;
							else if(isTagEnd.equals("E"))
								tag = "S-" + tag;							
							else {
								System.out.println("Wrong 1");
								System.out.println(word + "," + count + "," + current);
								//System.exit(-1);
							}
						}
						else if(isTagStart.equals("M")) {
							int index = midList.get(count);
							tag = tagList.get(index);
							if(isTagEnd.equals("M")) 
								tag = "M-" + tag;
							else if(isTagEnd.equals("E"))
								tag = "E-" + tag;							
							else {
								System.out.println("Wrong 2");
								System.out.println(word + "," + count + "," + current);
								//System.exit(-1);
							}
						}
						writer.write(word + "\t" + tag + "\n");
						count = current;
					}
				}
				else {
					for(int i=0; i< wordList.size(); i++) 
						writer.write(wordList.get(i) + "\n");
				}
				writer.write("\n");
			}
		}
		inSegFile.close();
		writer.close();			
	}
	
	/**
	 * process each word
	 * @param word
	 * @return
	 */
	public static ArrayList<String> processWord(String word) {
		ArrayList<String> prevs = new ArrayList<String> ();
		ArrayList<String> words = new ArrayList<String> ();
		words.add(word);
		boolean bChange = true;
		while(bChange) {
			bChange = false;
			prevs = words;
			words = new ArrayList<String> ();
			for(String sWord : prevs) {
				boolean bbChange = false;
				if(sWord.contains("#") && !sWord.equals("#")) {
					int index = sWord.indexOf("#");
					if(index > 0)
						words.add(sWord.substring(0, index));
					words.add("#");
					if(index + 1 < sWord.length()) 
						words.add(sWord.substring(index + 1));
					bChange = true; bbChange = true;
					continue;
				}
				if(sWord.contains(",") && !sWord.equals(",")){
					int index = sWord.indexOf(",");
					if(index > 0)
						words.add(sWord.substring(0, index));
					words.add(",");
					if(index + 1 < sWord.length()) 
						words.add(sWord.substring(index + 1));
					bChange = true; bbChange = true;
					continue;
				}
				if(sWord.contains("=") && !sWord.equals("=")){
					int index = sWord.indexOf("=");
					if(index > 0)
						words.add(sWord.substring(0, index));
					words.add("=");
					if(index + 1 < sWord.length()) 
						words.add(sWord.substring(index + 1));
					bChange = true; bbChange = true;
					continue;
				}
				if(sWord.contains("/") && !sWord.equals("/")) {
					int index = sWord.indexOf("/");
					if((index > 0 && sWord.substring(index-1,index).matches("[a-zA-Z]*"))
						|| (index + 1 < sWord.length() && sWord.substring(index + 1, index+2).matches("[a-zA-Z]*"))) {
						if(index > 0)
							words.add(sWord.substring(0, index));
						words.add("/");
						if(index + 1 < sWord.length()) 
							words.add(sWord.substring(index + 1));
						bChange = true; bbChange = true;
						continue;
					}	
				}
				if(sWord.contains("-") && !sWord.equals("-")) {
					int index = sWord.indexOf("-");
					if((index > 0 && sWord.substring(index-1,index).matches("[a-zA-Z]*"))
						|| (index + 1 < sWord.length() && sWord.substring(index + 1, index+2).matches("[a-zA-Z]*"))) {
						if(index > 0)
							words.add(sWord.substring(0, index));
						words.add("-");
						if(index + 1 < sWord.length()) 
							words.add(sWord.substring(index + 1));
						bChange = true; bbChange = true;
						continue;
					}					
				}
				if(sWord.contains("&") && !sWord.equals("&")) {
					int index = sWord.indexOf("&");
					if((index > 0 && sWord.substring(index-1,index).matches("[a-zA-Z]*"))
						|| (index + 1 < sWord.length() && sWord.substring(index + 1, index+2).matches("[a-zA-Z]*"))) {
						if(index > 0)
							words.add(sWord.substring(0, index));
						words.add("&");
						if(index + 1 < sWord.length()) 
							words.add(sWord.substring(index + 1));
						bChange = true; bbChange = true;
						continue;
					}					
				}
				if(sWord.contains(".") && !sWord.equals(".")) {
					int index = sWord.indexOf(".");
					if((index > 0 && sWord.substring(index-1,index).matches("[a-z]*"))
						|| (index + 1 < sWord.length() && sWord.substring(index + 1, index+2).matches("[a-z]*"))) {
						if(index > 0)
							words.add(sWord.substring(0, index));
						words.add(".");
						if(index + 1 < sWord.length()) 
							words.add(sWord.substring(index + 1));
						bChange = true; bbChange = true;
						continue;
					}					
				}
				if(sWord.matches("[A-Z]+[a-z]+[A-Z]+.*")) {
					Pattern p = Pattern.compile("([A-Z]+[a-z]+)([A-Z]+.*)");
					Matcher m = p.matcher(sWord);
					if(m.find()){
						int gc = m.groupCount();
						for(int i = 1; i <= gc; i++)
							words.add(m.group(i));
					}
					bChange = true; bbChange = true;
					continue;
				}				
				if(sWord.matches(".*[0-9][a-zA-Z]+.*")) {
					Pattern p = Pattern.compile("(.*[0-9])([a-zA-Z]+.*)");
					Matcher m = p.matcher(sWord);
					if(m.find()){
						int gc = m.groupCount();
						for(int i = 1; i <= gc; i++)
							words.add(m.group(i));
					}
					bChange = true; bbChange = true;
					continue;
				}
				if(sWord.length() > 1 && sWord.substring(sWord.length()-2,sWord.length()-1).matches("[0-9]")
						&& sWord.substring(sWord.length()-1).matches("[-&.]")) {
					words.add(sWord.substring(0, sWord.length()-1));
					words.add(sWord.substring(sWord.length()-1));
					bChange = true; bbChange = true;
				}
				if(!bbChange)
					words.add(sWord);				
				//else if()
			}
		}
		int count = 0;
		for(String sWord: words)
			count += sWord.length();
		if(count != word.length()) {
			System.out.println(word + ": length is different");
		}
		return words;
	}
	
	public static void raw2conllDir(String inDir, String outDir, boolean bWithTag) throws IOException {
		FileTree newTree = new FileTree();
    	newTree.generateFrom(inDir);
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix("raw.seg");
    	for(String fileName : fileNames) {
    		String name = new File(fileName).getName();
    		String prefix = name.substring(0, name.length()-8);
    		System.out.println(prefix);
    		String inSegName = inDir + File.separator + prefix + ".raw.seg";
    		String inTagName = null;
    		if(bWithTag)
    			inTagName = inDir + File.separator + prefix + ".tag";
    		String outName = outDir + File.separator + prefix + ".conll";
    		raw2conll(inSegName , inTagName, outName);    		
    	}		
	}
	
	
	public static void statTag(String inDir) {
		FileTree newTree = new FileTree();
    	newTree.generateFrom(inDir);
    	HashMap<String, Integer> tags = new HashMap<String, Integer>();
    	HashMap<String, Integer> names = new HashMap<String, Integer>();
    	HashMap<String, String> nameTags = new HashMap<String, String>();
    	
    	ArrayList<String> fileNames = newTree.getFileNamesWithSuffix("xml");
    	for(String fileName : fileNames) {
    		try {
    			System.out.println(fileName);
	    		File f=new File(fileName);
		 		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		 		DocumentBuilder builder=factory.newDocumentBuilder();
		 		Document doc = builder.parse(f);
		 		
		 		NodeList deIdi2b2_node = doc.getElementsByTagName("deIdi2b2");
		 		Element deIdi2b2_element = (Element) deIdi2b2_node.item(0);
		 			 		
		 		// tags
		 		NodeList tag_node = deIdi2b2_element.getElementsByTagName("TAGS");
		 		Element tag = (Element) tag_node.item(0);
	 			NodeList type_node = tag.getElementsByTagName("*");
	 			int tag_number = type_node.getLength();
	 			for(int k=0; k<tag_number; k++) {
	 				Element type_element = (Element) type_node.item(k);
	 				String id = type_element.getAttribute("id");
	 				String startString = type_element.getAttribute("start");
	 				int start = Integer.parseInt(startString);
	 				String endString = type_element.getAttribute("end");
	 				int end = Integer.parseInt(endString);
	 				String text = type_element.getAttribute("text");   //标注的tag
	 				String type = type_element.getAttribute("TYPE");
	 				if(!names.containsKey(text))
	 					names.put(text, 1);
	 				else
	 					names.put(text, names.get(text)+1);
	 				if(!tags.containsKey(type))
	 					tags.put(type, 1);
	 				else
	 					tags.put(type, tags.get(type)+1); 
	 				nameTags.put(text, type);
	 			}
	    	}	
        	catch(Exception e){
    	 		e.printStackTrace();
    		}	
    	}
    	Iterator<Entry<String, Integer>> iter = tags.entrySet().iterator();
    	while(iter.hasNext()) {
    		Entry<String, Integer> entry = iter.next();
    		System.out.println(entry.getKey() + "\t" + entry.getValue());
    	}
    	iter = names.entrySet().iterator();
    	while(iter.hasNext()) {
    		Entry<String, Integer> entry = iter.next();
    		System.out.println(entry.getKey() + "\t" + entry.getValue());
    	}
    	Iterator<Entry<String, String>> iter2 = nameTags.entrySet().iterator();
    	while(iter.hasNext()) {
    		Entry<String, String> entry = iter2.next();
    		System.out.println(entry.getKey() + "\t" + entry.getValue());
    	}
	}
	
	public static void main(String[] args) throws IOException {
		String inFile = "/home/tm/disk/disk1/i2b2/220-01.xml";
		String outRawFile = "/home/tm/disk/disk1/i2b2/220-01.raw";
		String outTagFile = "/home/tm/disk/disk1/i2b2/220-01.tag";
		//TextPreprocess.xml2raw(inFile, outRawFile, outTagFile);
		String inDir = "/home/tm/disk/disk1/i2b2/training-PHI-Gold-Set1";		
		//TextPreprocess.statTag(inDir);
		String outDir = "/home/tm/disk/disk1/i2b2/testing-PHI-noTags";
		//TextPreprocess.xml2rawDir(inDir, outDir);
		
		String inSegFileName = "/home/tm/disk/disk1/i2b2/training-PHI-Gold-Set1/220-02.xml.raw.seg";
		String inTagFileName = "/home/tm/disk/disk1/i2b2/training-PHI-Gold-Set1/220-02.xml.tag";
		String outFileName = "/home/tm/disk/disk1/i2b2/training-PHI-Gold-Set1/220-02.xml.conll";
		//TextPreprocess.raw2conll(inSegFileName, inTagFileName, outFileName);
		inDir = "/home/tm/disk/disk1/i2b2/testing-PHI-Gold-fixed";
		outDir = "/home/tm/disk/disk1/i2b2/testing-PHI-Gold-fixed";
		TextPreprocess.raw2conllDir(inDir, outDir, false);
		String word = "8a";
		//process(word);
		//System.out.println(word.substring(word.length()-1).matches("[-&.]"));
	}

}
