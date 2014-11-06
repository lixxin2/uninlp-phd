package edu.hitsz.nlp.segpos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.hitsz.java.file.local.FileEncoding;

public class StatDict{
	
	public static String delimiter = "&";
	
	private HashMap<String, Integer> oneWords;
	private int oneWordsNumber;
	private HashMap<String, Integer> twoWords;
	private int twoWordsNumber;
	private HashMap<String, Integer> threeWords;
	private int threeWordsNumber;
	
	private HashMap<String, Integer> oneTags;
	private int oneTagsNumber;
	private HashMap<String, Integer> twoTags;
	private int twoTagsNumber;
	private HashMap<String, Integer> threeTags;
	private int threeTagsNumber;
	
	private HashMap<String, Integer> oneWordTags;
	private int oneWordTagsNumber;
	private HashMap<String, Integer> twoWordTags;
	private int twoWordTagsNumber;
	
	
	public StatDict() {
		oneWords = new HashMap<String, Integer>();
		oneWordsNumber = 0;
		twoWords = new HashMap<String, Integer>();
		twoWordsNumber = 0;
		threeWords = new HashMap<String, Integer>();
		threeWordsNumber = 0;
		oneTags = new HashMap<String, Integer>();
		oneTagsNumber = 0;
		twoTags = new HashMap<String, Integer>();
		twoTagsNumber = 0;
		threeTags = new HashMap<String, Integer>();
		threeTagsNumber = 0;
		oneWordTags = new HashMap<String, Integer>();
		oneWordTagsNumber = 0;
		twoWordTags = new HashMap<String, Integer>();
		twoWordTagsNumber = 0;
	}
	
	/**
	 * 读取文件，统计文件中的词和词性
	 * @since Jun 15, 2012
	 * @param fileName
	 */
	public void statFile(String fileName) {		

		ArrayList<String> characters = new ArrayList<String>();
		ArrayList<String> tags = new ArrayList<String>();

		int sentenceNumber = 0;
		
		BufferedReader reader = null;
		String line = null;
		try {
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					sentenceNumber++;
					if(sentenceNumber % 100 == 0)
						System.out.println(sentenceNumber);
					
					statSentence(characters, tags);
					
					characters.clear();
					tags.clear();					
				}
				else {					
					String[] parts = line.split("[ \t]");
					if(parts.length == 2) {
						characters.add(parts[0]);
						tags.add(parts[1]);
					}				
				}				
			}

			System.out.println(sentenceNumber);
			reader.close();
			oneWordsNumber = oneWords.size();
			twoWordsNumber = twoWords.size();
			threeWordsNumber = threeWords.size();
			oneTagsNumber = oneTags.size();
			twoTagsNumber = twoTags.size();
			threeTagsNumber = threeTags.size();
			oneWordTagsNumber = oneWordTags.size();
			twoWordTagsNumber = twoWordTags.size();		
			
		}
		
		catch(IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * 将一句话中的所有词和词性放入map
	 * @since Jun 15, 2012
	 * @param words
	 * @param tags
	 */
	public void statSentence(ArrayList<String> words, ArrayList<String> tags) {
		int length = words.size();
		if(length != tags.size()) {
			System.out.println("The number of characters and tags are different");
			System.exit(-1);
		}
		for(int i=0; i<length; i++) {
			String curWord = words.get(i);
			String curTag = tags.get(i);
			String curWordTag = curWord + delimiter + curTag;
			putIntoMap(oneWords, curWord);
			putIntoMap(oneTags, curTag);
			putIntoMap(oneWordTags, curWordTag);
			
			//
			String preWord = "PWord";
			String preTag = "PTag";
			String preWordTag = preWord + delimiter + preTag;
			int j=i-1;
			if(j >= 0) {
				preWord = words.get(j);
				preTag = tags.get(j);
				preWordTag = preWord + delimiter + preTag;
			}
			
			String preCurWord = preWord + delimiter + curWord;
			String preCurTag = preTag + delimiter + curTag;
			putIntoMap(twoWords, preCurWord);
			putIntoMap(twoTags, preCurTag);
			String preCurWordTag = preWordTag + delimiter + curWordTag;
			putIntoMap(twoWordTags, preCurWordTag);		
			
			//
			String pre2Word = "P2Word";
			String pre2Tag = "P2Tag";
			
			int k= j-1;
			if(k >= 0) {
				pre2Word = words.get(k);
				pre2Tag = tags.get(k);				
			}
			
			String pre2PreCurWord = pre2Word + delimiter + preCurWord;
			String pre2PreCurTag = pre2Tag + delimiter + preCurTag;
			putIntoMap(threeWords, pre2PreCurWord);
			putIntoMap(threeTags, pre2PreCurTag);		
				
		}		
		
	}
	
	
	/**将字符串放入map,map中统计字符串的出现次数*/
	public void putIntoMap(HashMap<String, Integer> map, String s) {
		if(map.containsKey(s))
			map.put(s, map.get(s)+1);
		else
			map.put(s, 1);	
	}
	
	/**
	 * 得到map中的所有出现次数
	 * @since Jun 15, 2012
	 * @param map
	 * @return
	 */
	public int getNumber(HashMap<String, Integer> map) {
		int number = 0;
		Iterator<Map.Entry<String, Integer>> iter = map.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			int subNumber = entry.getValue();
			number += subNumber;			
		}
		return number;	
	}
	
	/**存储统计信息*/
	public void store(String fileName) {		
		try {
			System.out.print("store stat dict ... ");
			FileOutputStream f = new FileOutputStream(fileName);  
		    ObjectOutputStream s = new ObjectOutputStream(f);  
		    s.writeObject(oneWords);  
		    s.writeObject(oneWordsNumber);  
		    s.writeObject(twoWords);
		    s.writeObject(twoWordsNumber);
		    s.writeObject(threeWords);
		    s.writeObject(threeWordsNumber);
		    s.writeObject(oneTags);
		    s.writeObject(oneTagsNumber);
		    s.writeObject(twoTags);
		    s.writeObject(twoTagsNumber);
		    s.writeObject(threeTags);
		    s.writeObject(threeTagsNumber);
		    s.writeObject(oneWordTags);
		    s.writeObject(oneWordTagsNumber);
		    s.writeObject(twoWordTags);
		    s.writeObject(twoWordTagsNumber);
		    s.close();
		    System.out.println("done.");
		}
		catch (IOException e) {
		}
		    
		
	}
	
	
	public void load(String fileName) {
		
		try {
			System.out.print("load stat dict ... ");
			FileInputStream in = new FileInputStream(fileName);  
		    ObjectInputStream s = new ObjectInputStream(in);  
		    oneWords = (HashMap<String, Integer>) s.readObject();
			oneWordsNumber = (Integer) s.readObject();
			twoWords = (HashMap<String, Integer>) s.readObject();
			twoWordsNumber = (Integer) s.readObject();
			threeWords = (HashMap<String, Integer>) s.readObject();
			threeWordsNumber = (Integer) s.readObject();
			oneTags = (HashMap<String, Integer>) s.readObject();
			oneTagsNumber = (Integer) s.readObject();
			twoTags = (HashMap<String, Integer>) s.readObject();
			twoTagsNumber = (Integer) s.readObject();
			threeTags = (HashMap<String, Integer>) s.readObject();
			threeTagsNumber = (Integer) s.readObject();
			oneWordTags = (HashMap<String, Integer>) s.readObject();
			oneWordTagsNumber = (Integer) s.readObject();
			twoWordTags = (HashMap<String, Integer>) s.readObject();
			twoWordTagsNumber = (Integer) s.readObject();
			s.close();
			System.out.println("done");
		}
		catch (IOException e) {
			
		}
		catch (ClassNotFoundException e) {
			
		}
		
	}
	
	
	
	public static void main(String[] args) {
		
		StatDict dict = new StatDict();
		//dict.statFile("/home/tm/disk/disk1/segpossemi/large-train-wp");
		//dict.store("/home/tm/disk/disk1/segpossemi/large-train-stat");
		dict.load("/home/tm/disk/disk1/segpossemi/large-train-stat");
		System.out.println(dict.oneTags.get("NR"));
		
	}
	
}
