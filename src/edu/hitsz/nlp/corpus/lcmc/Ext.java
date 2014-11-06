package edu.hitsz.nlp.corpus.lcmc;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;

import javax.xml.parsers.*;

public class Ext{

	
	/**
	 * 从原始xml文件中抽取句子，拼音和汉字都可
	 * @since Nov 21, 2012
	 * @param inFile
	 * @param outFile
	 */
	public static void extFile(String inFile, String outFile) {
		
		long lasting =System.currentTimeMillis();
		
		try {
			FileWriter writer = new FileWriter(outFile);			
			
	 		File f=new File(inFile);
	 		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	 		DocumentBuilder builder=factory.newDocumentBuilder();
	 		Document doc = builder.parse(f);
	 		
	 		NodeList Lcmc = doc.getElementsByTagName("LCMC");
	 		Element lcmcText = (Element) Lcmc.item(0);
	 		
	 		//解析找到text
	 		NodeList texts = lcmcText.getElementsByTagName("text");
	 		for(int i=0; i<texts.getLength(); i++){	 			
	 			Element text = (Element) texts.item(i);
	 			String textID = text.getAttribute("ID");
	 			String textType = text.getAttribute("TYPE");
	 			//找到file
	 			NodeList files = text.getElementsByTagName("file");
	 			for(int j=0; j<files.getLength(); j++) {
	 				Element file = (Element) files.item(j);
	 				String fileID = file.getAttribute("ID");
	 				//找到段落p, paragraph
	 				NodeList paragraphs = file.getElementsByTagName("p");
	 				for(int k=0; k<paragraphs.getLength(); k++) {
	 					Element paragraph = (Element) paragraphs.item(k);
	 					//找到句子s, sentence
	 					NodeList sentences = paragraph.getElementsByTagName("s");
	 					for(int o=0; o<sentences.getLength(); o++) {
	 						Element sentence = (Element) sentences.item(o);
	 						String sentenceN = sentence.getAttribute("n");
	 						
	 						ArrayList<String> words = new ArrayList<String>();
	 						//每个词
	 						NodeList childNodes = sentence.getChildNodes();
	 						for(int p=0; p<childNodes.getLength(); p++) {
	 							Node childNode = childNodes.item(p); 
	 							if(childNode.getNodeType()==Node.ELEMENT_NODE)
	 			                {   
	 			                    String wc = childNode.getNodeName();
	 			                    String pos = ((Element) childNode).getAttribute("POS");
	 			                    String word = childNode.getFirstChild().getNodeValue();
	 			                    words.add(word);
	 			                }   
	 						}
	 						
	 						writer.write(combWords(words)+"\n");
	 						
	 					}
	 				}
	 			}
	 		}
	 		
	 		writer.close();
	 		
		}catch(Exception e){
	 		e.printStackTrace();
		}
		System.out.println("运行时间："+(System.currentTimeMillis() - lasting)+" 毫秒");
	
	}
	
	
	/**
	 * 合并词
	 * @since Oct 21, 2012
	 * @param words
	 * @return
	 */
	public static String combWords(ArrayList<String> words) {
		
		StringBuffer bf = new StringBuffer();
		for(String word : words)
			bf.append(word+"\t");
		return bf.toString().trim();
		
	}

	public static void extDir(String inDir, String outDir) {
		FileTree ftree = new FileTree();
		ftree.generateFrom(inDir);
		ArrayList<String> infileNames = ftree.getFileNames();
		for(String infileName : infileNames) {
			String outFileName = new File(outDir, new File(infileName).getName()).getPath();
			
			extFile(infileName, outFileName);
			
			
		}		
	}
	
	
	/**
	 * 对行进行过滤，如果一句中有英文，则删掉该行（音字转换时，通常是行根据标点先分行）
	 * @since Dec 3, 2012
	 * @param inFileName
	 * @param outFileName
	 */
	public static void delEnglish(String inFileName, String outFileName) {
		
		try {
			String encoding = FileEncoding.getCharset(inFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), encoding));
			FileWriter writer = new FileWriter(outFileName);
			
			String line = null;
			
			while ( (line = reader.readLine()) != null ) {
				
				line = line.trim();
				
				if(line.length() > 0 ) {
					if(!line.matches(".*[a-zA-Z].*"))
						writer.write(line+"\n");
				}
					
				
			}
			reader.close();
			writer.close();
		}
		catch (IOException e) {
			
		}
			
		
		
	}
	
	
	
	
	
	public static void main(String[] args){
		String inFile = "/home/tm/disk/disk1/i2b2/LCMC_C.XML";
		String outFile = "/home/tm/disk/disk1/i2b2/LCMC_C.txt";
		Ext.extFile(inFile, outFile);
	
		
	}
	
	
} 