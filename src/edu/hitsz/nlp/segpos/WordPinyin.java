package edu.hitsz.nlp.segpos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.java.file.local.FileTree;
import edu.hitsz.nlp.language.chinese.ChineseWord;

public class WordPinyin {

	
	/**
	 * 从文件夹的所有文件中抽取
	 * @since Sep 27, 2012
	 * @param dir
	 * @throws IOException 
	 */
	public static void extractCharacter(String inDir, String outFile) throws IOException {
		
		FileTree tree = new FileTree();
		tree.generateFrom(inDir);
		
		HashMap<String, Integer> characters = new HashMap<String, Integer>();
		
		for(String file : tree.getFileNames()) {
			
			Options option = new Options();
			option.trainFile = file;
			Reader segposReader = new Reader();
			segposReader.startReading(file, option);
			Instance instance = null;
			instance = segposReader.getNext();
			int count = 0;

			while(instance != null) {
				if(count %100 == 0) {	
					System.out.print(count+"...");
					if(count %1000 == 0) {
						System.out.println();
					}
				}
				String[] chars = instance.chars;
				for(String character : chars)
					characters.put(character, 1);
				count++;
				instance = segposReader.getNext();
			}				
		}
		
		FileWriter writer = new FileWriter(outFile);		
		Iterator<Entry<String, Integer>> iter = characters.entrySet().iterator();
		
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String character = entry.getKey();
			if(character.matches(ChineseWord.hanzisRegex))
				writer.write(character+"\n");
			else
				System.out.println(character);
		}
		writer.close();	
		
	}
	
	
	
	
	
	
	
	
	/**
	 * 对于给定文件中的每个字，根据所有字音文件抽取其注音，放入另一文件
	 * @since Sep 27, 2012
	 * @param characterFile
	 * @param characterYinsFile
	 * @param outCharacterYinFile
	 * @throws IOException 
	 */
	public static void extractCharacterYin(String characterYinsFile, String characterFile, 
			String outCharacterYinFile) throws IOException {
		ArrayList<String> vowels = new ArrayList<String>();
		vowels.add("a");vowels.add("e");vowels.add("i");vowels.add("o");vowels.add("u");vowels.add("v");
		//read character yins
		HashMap<String, String> characterYins= new HashMap<String, String>();
		String fileEncoding = FileEncoding.getCharset(characterYinsFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(characterYinsFile), fileEncoding));
	    String line = null;
		while((line = reader.readLine()) != null) {
			String[] tmps = line.split(" ");
			if(tmps.length > 1)
				characterYins.put(tmps[0], tmps[1]);
		}
		reader.close();
		//
		String fileEncoding2 = FileEncoding.getCharset(characterFile);
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(characterFile), fileEncoding2));
		FileWriter writer = new FileWriter(outCharacterYinFile);
	    String line2 = null;
		while((line2 = reader2.readLine()) != null) {
			String character = line2.trim();
			if(characterYins.containsKey(character)) {
				String yin = characterYins.get(character);
				int length = yin.length();
				int numberPosition = length -1;
				int vowelPosition = 0;
				for(; vowelPosition<numberPosition; vowelPosition++) {
					if(vowels.contains(yin.substring(vowelPosition, vowelPosition+1))) {
						break;
					}
				}
				String shengmu = null;
				if(vowelPosition == 0)
					shengmu = "SM";
				else
					shengmu = yin.substring(0,vowelPosition);
				String yunmu = yin.substring(vowelPosition, numberPosition);
				String shengdiao = yin.substring(numberPosition);
				writer.write(character+" "+yin+" "+shengmu+" "+yunmu+ " "+shengdiao+"\n");				
			}
			else
				System.out.println(character);				
		}
		reader2.close();
		writer.close();
		
		
	}
	
	
	
	public static void statCharacterPinyins(String fileName) {
		
		
		
		
	}
	
	/**
	 * 统计分词的词的读音情况
	 * @since Sep 27, 2012
	 * @param fileName
	 * @throws IOException 
	 */
	public static void statWordPinyins(String fileName, String characterYinsFile) throws IOException {
		//
		HashMap<String, Yin> characterYins = readCharacterYin(characterYinsFile);
		//
		Options option = new Options();
		option.trainFile = fileName;
		Reader segposReader = new Reader();
		segposReader.startReading(fileName, option);
		Instance instance = null;
		instance = segposReader.getNext();
		int count = 0;
		//
		HashMap<String,HashMap<String,Integer>> shengdiaoMap = new HashMap<String,HashMap<String,Integer>>();
		
		while(instance != null) {
			if(count %100 == 0) {	
				System.out.print(count+"...");
				if(count %1000 == 0) {
					System.out.println();
				}
			}
			String[] words = instance.words;
			String[] tags = instance.tags;
			int length = words.length;			
			
			for(int i=0; i<length; i++) {
				if(ChineseWord.isHanzi(words[i])) {
					String wordShengdiao = "";
					for(int j=0; j<words[i].length(); j++) {
						String character = words[i].substring(j, j+1);
						if(characterYins.containsKey(character)) {
							Yin yin = characterYins.get(character);
							wordShengdiao += yin.shengdiao;
						}
					}
					if(!shengdiaoMap.containsKey(tags[i])) {
						HashMap<String,Integer> tagNumberMap = new HashMap<String,Integer>();
						tagNumberMap.put(wordShengdiao, 1);
						shengdiaoMap.put(tags[i], tagNumberMap);
					}
					else {
						HashMap<String,Integer> tagNumberMap = shengdiaoMap.get(tags[i]);
						if(!tagNumberMap.containsKey(wordShengdiao)) {
							tagNumberMap.put(wordShengdiao, 1);
							shengdiaoMap.put(tags[i], tagNumberMap);
						}
						else {
							tagNumberMap.put(wordShengdiao, tagNumberMap.get(wordShengdiao)+1);
							shengdiaoMap.put(tags[i], tagNumberMap);
						}
						
					}					
				}					
			}			
			count++;
			instance = segposReader.getNext();
		}			
		
		System.out.println();
		
		
	}
	
	/**
	 * 从文件中读取字读音
	 * @since Sep 28, 2012
	 * @param characterYinsFile
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Yin> readCharacterYin(String characterYinsFile) throws IOException {
		System.out.println("reading characteryin: " + characterYinsFile + " done.");
		HashMap<String, Yin> characterYins = new HashMap<String, Yin>();
		String fileEncoding = FileEncoding.getCharset(characterYinsFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(characterYinsFile), fileEncoding));
	    String line = null;
		while((line = reader.readLine()) != null) {
			String[] tmps = line.split(" ");
			if(tmps.length == 5) {
				characterYins.put(tmps[0], new Yin(tmps[1], tmps[2], tmps[3], tmps[4]));
			}
		}		
		reader.close();
		return characterYins;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		//WordPinyin.extractCharacter("/home/tm/disk/disk1/segpospinyin", "/home/tm/disk/disk1/character");
		WordPinyin.extractCharacterYin("/home/tm/disk/disk1/segpospinyin/characterpinyins", 
				"/home/tm/disk/disk1/segpospinyin/character", "/home/tm/disk/disk1/segpospinyin/characterpinyin2");
		//WordPinyin.statWordPinyins("/home/tm/disk/disk1/segpospinyin/large-train-wp", "/home/tm/disk/disk1/segpospinyin/characterpinyin");
	}
	
	
	
}

class Yin {
	String all;
	String shengmu;
	String yunmu;
	String shengdiao;
	
	public Yin(String all, String shengmu, String yunmu, String shengdiao) {
		this.all = all;
		this.shengmu = shengmu;
		this.yunmu = yunmu;
		this.shengdiao = shengdiao;
	}
	
}
