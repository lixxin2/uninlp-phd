package edu.hitsz.nlp.nermusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;

/**
 * 将标注好的类xml文本转换为ner处理的文件
 * <p> ARTIST(歌手), BAND（组合）, SONG（歌曲）, ALBUM（专辑）
 * @author Xinxin Li
 * @since May 5, 2012
 */
public class Xml2Column {
	
	/**
	 * 将标注好的类xml文本转换为ner处理的文件
	 * @since May 15, 2012
	 * @param xmlFileName
	 * @param charFileName
	 */
	public void xml2column(String xmlFileName, String charFileName) {
		
		BufferedReader reader = null;
		String oneline = "";
		
		try {
			String encoding = FileEncoding.getCharset(xmlFileName); 
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFileName), encoding)); 
			FileWriter writer = new FileWriter(new File(charFileName));
			int lineNumber = 0;
			while((oneline = reader.readLine()) != null) {
				if(oneline.equals("<DOCUMENT>") || oneline.equals("<\\DOCUMENT>"))
					continue;
				ArrayList<String> sentences = splitSentence(oneline.trim(),"[。？！]");
				//for(String sentence: sentences) {
					
					//ArrayList<String> lines = splitSentence(sentence.trim(),"[，：]");
					//如果没有，则不填加句子		
					boolean added = false;
					StringBuffer allSentence = new StringBuffer();
					for(String line : sentences) {
						line = line.trim();
						StringBuffer sb = new StringBuffer();
						int length = line.length();
						//如果标注没有错误，则不填加句子
						int i=0; 
						for(; i<length; i++) {
							String cur = line.substring(i, i+1);
							if(cur.matches(" "))
								continue;
							else if(cur.matches("[0-9a-zA-Z-]")) {
								i++;
								while(i<length && line.substring(i, i+1).matches("[0-9a-zA-Z-]")) {
									cur += line.substring(i,i+1);
									i++;
								}
								sb.append(cur + "\tO\n");
								i -= 1;						
							}
							else if(!cur.equals("<")) {
								sb.append(cur + "\tO\n");
							}
							else {
								if(i+1 < length) {
									String elstring = line.substring(i);
									
									
									if(elstring.startsWith("<ARTIST>")) {
										i = processSingleTag(sb, line, "<ARTIST>", i);	
										if(i == -1) break;
										added = true;
									}
									else if(elstring.startsWith("<ALIAS>")) {
										i = processSingleTag(sb, line, "<ARTIST>", i);	
										if(i == -1) break;
										added = true;
									}
									else if(elstring.startsWith("<BAND>")) {
										i = processSingleTag(sb, line, "<BAND>", i);
										if(i == -1) break;
										added = true;
									}
									else if(elstring.startsWith("<SONG>")) {
										i = processSingleTag(sb, line, "<SONG>", i);	
										if(i == -1) break;
										added = true;
									}
									else if(elstring.startsWith("<ALBUM>")) {
										i = processSingleTag(sb, line, "<ALBUM>", i);	
										if(i == -1) break;
										added = true;
									}
									else 
										continue;
								}
							}
						}	
						if(i != -1) {
							String s = sb.toString().trim();
							if(s.length() > 0)
								allSentence.append(s+"\n\n");
						}
					}
					if(added) {
						String s = allSentence.toString().trim();
						if(s.length() > 0)
							writer.write(s+"\n\n");
					}
				//}
			}
			reader.close();
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/** 根据[。？！]把句子分开*/
	private ArrayList<String> splitSentence(String line, String regex) {
		
		ArrayList<String> sentences = new ArrayList<String>();
		int number = line.length();
		for(int i=0; i<number; i++) {
			StringBuffer sb2 = new StringBuffer();
			String character = line.substring(i,i+1);
			while(!character.matches(regex)) {
				sb2.append(character);
				i++;
				if(i < number) {
					character = line.substring(i,i+1);
				}
				else {
					String s = sb2.toString().trim();
					if(s.length() > 0)
						sentences.add(s);
					break;
				}
			}
			if( i< number) {
				sb2.append(character);
				String s = sb2.toString().trim();
				if(s.length() > 0)
					sentences.add(s);
			}
		}
		
		return sentences;
	}
	
	/**
	 * 处理一句话中包含某个tag的情况
	 * @since May 13, 2012
	 * @param sb
	 * @param line
	 * @param tag
	 * @param start
	 * @return
	 */
	public int processSingleTag(StringBuffer sb, String line, String tag, int start) {
		int l = tag.length();
		String endTag = "</" + tag.substring(1);
		String nakTag = tag.substring(1, l-1);		
		
		String elstring  = line.substring(start+l);
		int index = elstring.indexOf(endTag);	
		if(index != -1) {
			String word = elstring.substring(0, index);			
			if(word.contains("<") || word.contains(">"))
				return -1;
			System.out.println("word: " + word);
			ArrayList<String> words = new ArrayList<String>();
			int length = word.length();
			for(int i=0; i<length; i++) {
				String cur = word.substring(i, i+1);
				if(cur.matches(" "))
					continue;
				else if(cur.matches("[0-9a-zA-Z-]")) {
					i++;
					while(i<length && word.substring(i, i+1).matches("[0-9a-zA-Z-]")) {
						cur += word.substring(i,i+1);
						i++;
					}
					words.add(cur);
					i -= 1;						
				}
				else {
					words.add(cur);
				}
			}
			if(words.size() < 1) {				
				System.out.println(words);
			}
			String[] tags = word2tags(words, nakTag);
			if(tags != null) {
				for(int j=0; j<words.size(); j++) {
					sb.append(words.get(j) + "\t" + tags[j] + "\n");
				}	
			}		
			start += 2*l + index;
		}
		else {
			index = elstring.indexOf(tag);
			if(index != -1) {
				String word = elstring.substring(0, index);				
				if(word.contains("<") || word.contains(">"))
					return -1;
				System.out.println("word: " + word);
				ArrayList<String> words = new ArrayList<String>();
				int length = word.length();
				for(int i=0; i<length; i++) {
					String cur = word.substring(i, i+1);
					if(cur.matches(" "))
						continue;
					else if(cur.matches("[0-9a-zA-Z-]")) {
						i++;
						while(i<length && word.substring(i, i+1).matches("[0-9a-zA-Z-]")) {
							cur += word.substring(i,i+1);
							i++;
						}
						words.add(cur);
						i -= 1;						
					}
					else {
						words.add(cur);
					}
				}
				if(words.size() < 1) {				
					System.out.println(words);
				}
				String[] tags = word2tags(words, nakTag);
				if(tags != null) {
					for(int j=0; j<words.size(); j++) {
						sb.append(words.get(j).trim() + "\t" + tags[j] + "\n");
					}	
				}	
				start += 2*l + index -1;
			}
			else {
				//System.out.println(line);
				return -1;
			}
		}
		
		
		return start;
	}
	
	
	
	public static String[] word2tags(String words, String tag) {
		ArrayList<String> wordList = new ArrayList<String>();
		for(int i=0; i<words.length(); i++)
			wordList.add(words.substring(i, i+1));
		return word2tags(wordList, tag);
	}
	
	
	/**
	 * 将一个词转换为多个字符的标示
	 * @since May 15, 2012
	 * @param words
	 * @param tag
	 * @return
	 */
	public static String[] word2tags(ArrayList<String> words, String tag) {
		int length = words.size();
		if(length > 0) {
			String[] tags = new String[length];
			if(length == 1) {
				tags[0] = "S-" + tag;
			}
			else {
				tags[0] = "B-" + tag;
				tags[length-1] = "E-" + tag;
				for(int i=1; i<length-1; i++)
					tags[i] = "I-" + tag;
			}
			return tags;		
		}
		else {
			System.out.println(words);
			return null;
		}
	}
	
	public static void main(String[] args) {
		Xml2Column xc = new Xml2Column();
		//xc.xml2column("/home/tm/disk/disk1/nermusic/dev", "/home/tm/disk/disk1/nermusic/dev-tag");
		xc.xml2column("/home/tm/disk/disk1/nermusic/test", "/home/tm/disk/disk1/nermusic/test-tag");
		//xc.xml2column("/home/tm/disk/disk1/nermusic/train", "/home/tm/disk/disk1/nermusic/train-tag");
	}
	
	
}
