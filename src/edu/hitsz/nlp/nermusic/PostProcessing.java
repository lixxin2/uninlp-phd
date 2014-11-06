package edu.hitsz.nlp.nermusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.nermusic.Eval.NER;
import edu.hitsz.nlp.util.TypeDict;

public class PostProcessing {

	TypeDict songAlbumPrefixList;
	TypeDict artistBandDict;
	
	
	/**根据 歌曲，专辑名前缀词典匹配 匹配*/
	public void matchSongAlbumPrefix(String fileName, String resultFileName) {
		
		String songAlbumPrefixFileName = "/home/tm/disk/disk1/nermusic/dict1/prefixmatch";
		songAlbumPrefixList = TypeDict.read(songAlbumPrefixFileName);
		
		ArrayList<String> characters = new ArrayList<String>();
		ArrayList<String> goldtags = new ArrayList<String>();
		ArrayList<String> predicttags = new ArrayList<String>();
		
		int sentenceNumber = 0;
		
		BufferedReader reader = null;
		FileWriter writer = null;
		String line = null;
		try {
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			writer = new FileWriter(new File(resultFileName));
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					sentenceNumber++;
					if(sentenceNumber % 100 == 0)
						System.out.println(sentenceNumber);
					int length = characters.size();
					matchSongAlbumPrefix(characters, predicttags);						
					for(int i=0; i<length; i++)
						writer.write(characters.get(i)+"\t"+goldtags.get(i)+"\t"+predicttags.get(i)+"\n");
					writer.write("\n");
					characters.clear();
					goldtags.clear();
					predicttags.clear();
					
				}
				else {					
					String[] parts = line.split("[ \t]");
					int length = parts.length;
					if(parts.length > 2) {
						characters.add(parts[0]);
						goldtags.add(parts[length-2]);
						predicttags.add(parts[length-1]);
					}				
				}				
			}
			System.out.println(sentenceNumber);
			reader.close();
			writer.close();
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void matchSongAlbumPrefix(ArrayList<String> characters, ArrayList<String> predicttags) {
		int length = characters.size();
				
		for(int i=length-1; i >= 0; i--) {
			for(int j=Math.max(0, i-2); j< Math.max(1, i); j++) {
				StringBuffer sb = new StringBuffer();
				sb.append(characters.get(j));
				for(int m=j+1; m<=i; m++) {
					sb.append(characters.get(m));					
				}
				String tag = songAlbumPrefixList.getKeyValue(sb.toString());
				if(!tag.equals("O")) {
					boolean sig = false;
					int k=i+1;
					for(; k<length; k++) {
						if(characters.get(k).equals("》")) {
							sig = true;
							break;
						}
					}
					if( sig) {
						ArrayList<String> words = new ArrayList<String>();				
						for(int m = i+1; m < k; m++)
							words.add(characters.get(m));
						String[] subtags = Xml2Column.word2tags(words, tag);
						for(int m = i+1; m < k; m++) {
							predicttags.set(m, subtags[m-i-1]);
						}
					}					
					i = j-1;
					break;
				}		
			}
		}
	}
	
	
	/**匹配歌手名，乐队名词典匹配*/
	public void matchArtistBand(String fileName, String resultFileName) {
		String artistFileName = "/home/tm/disk/disk1/nermusic/dict1/artists";
		String bandFileName = "/home/tm/disk/disk1/nermusic/dict1/bands";
		
		//artistBandDict = TypeDict.read(artistFileName, "ARTIST");
		//artistBandDict.add(bandFileName, "BAND");
		//artistBandDict = TypeDict.read(bandFileName, "BAND");
		
		ArrayList<String> characters = new ArrayList<String>();
		ArrayList<String> goldtags = new ArrayList<String>();
		ArrayList<String> predicttags = new ArrayList<String>();
		
		int sentenceNumber = 0;
		
		BufferedReader reader = null;
		FileWriter writer = null;
		String line = null;
		try {
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			writer = new FileWriter(new File(resultFileName));
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					sentenceNumber++;
					if(sentenceNumber % 100 == 0)
						System.out.println(sentenceNumber);
					int length = characters.size();
					matchArtistBand(characters, predicttags);						
					for(int i=0; i<length; i++)
						writer.write(characters.get(i)+"\t"+goldtags.get(i)+"\t"+predicttags.get(i)+"\n");
					writer.write("\n");
					characters.clear();
					goldtags.clear();
					predicttags.clear();
					
				}
				else {					
					String[] parts = line.split("[ \t]");
					int length = parts.length;
					if(parts.length > 2) {
						characters.add(parts[0]);
						goldtags.add(parts[length-2]);
						predicttags.add(parts[length-1]);
					}				
				}				
			}
			System.out.println(sentenceNumber);
			reader.close();
			writer.close();
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	private void matchArtistBand(ArrayList<String> characters, ArrayList<String> predicttags) {
		int length = characters.size();
				
		for(int i=length-1; i >= 0; i--) {
			for(int j=Math.max(0, i-8); j< Math.max(1, i); j++) {
				StringBuffer sb = new StringBuffer();
				sb.append(characters.get(j));				
				for(int m=j+1; m<=i; m++) {
					if(characters.get(m).matches("[0-9a-zA-Z].*"))
						sb.append(" "+characters.get(m));
					else
						sb.append(characters.get(m));			
				}
				String tag = artistBandDict.getKeyValue(sb.toString());
				if(!tag.equals("O")) {
					ArrayList<String> words = new ArrayList<String>();
					for(int k=j; k<i+1; k++) {
						words.add(characters.get(k));
					}
					String[] subtags = Xml2Column.word2tags(words, tag);
					for(int k=j; k<i+1; k++) {
						predicttags.set(k, subtags[k-j]);
					}
					i = j;
					break;
				}		
			}
		}
	}
	
	/** 根据结果里统计出来的*/
	public void matchResult(String fileName, String resultFileName) {
		
		Eval eval = new Eval();
		
		ArrayList<HashMap<String,Integer>> words = new ArrayList<HashMap<String, Integer>>();
		for(int i=0; i<4; i++)
			words.add(new HashMap<String, Integer>());
		
		BufferedReader reader = null;
		FileWriter writer = null;
		String line = null;
		try {
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			
			ArrayList<String> characters = new ArrayList<String>();
			ArrayList<String> goldTags = new ArrayList<String>();
			ArrayList<String> predictTags = new ArrayList<String>();
			int sentenceNumber = 0;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					if(sentenceNumber++ == 48)
						System.out.println();
					System.out.println(sentenceNumber);
					ArrayList<NER> predictNers = eval.getNER(characters, predictTags);
					for(NER ner : predictNers) {
						String label = ner.tag;
						String word = ner.word;
						int number = label.equals("ARTIST")?0:
							(label.equals("BAND")?1:
								(label.equals("SONG")?2:3));
						HashMap<String, Integer> wordNumber = words.get(number);
						if(wordNumber.containsKey(word)) {
							wordNumber.put(word, wordNumber.get(word)+1);
						}
						else
							wordNumber.put(word, 1);				
					}
					characters.clear();
					predictTags.clear();
				}
				else {					
					String[] parts = line.split("[ \t]");
					int length = parts.length;
					if(parts.length > 2) {
						characters.add(parts[0]);
						predictTags.add(parts[length-1]);
					}				
				}				
			}
			characters.clear();
			predictTags.clear();
			reader.close();
			//
			sentenceNumber = 0;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			writer = new FileWriter(new File(resultFileName));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					System.out.println(sentenceNumber++);
					int length = characters.size();
					matchResult(words, characters, predictTags);						
					for(int i=0; i<length; i++)
						writer.write(characters.get(i)+"\t"+goldTags.get(i)+"\t"+predictTags.get(i)+"\n");
					writer.write("\n");
					characters.clear();
					goldTags.clear();
					predictTags.clear();
				}
				else {					
					String[] parts = line.split("[ \t]");
					int length = parts.length;
					if(parts.length > 2) {
						characters.add(parts[0]);
						goldTags.add(parts[length-2]);
						predictTags.add(parts[length-1]);
					}				
				}				
			}
			reader.close();
			writer.close();
			
		}
		catch(IOException e) {
			
		}
	}
	
	
	private void matchResult(ArrayList<HashMap<String, Integer>> wordTags,
			ArrayList<String> characters, ArrayList<String> predicttags) {
		int length = characters.size();
				
		for(int i=length-1; i >= 0; i--) {
			for(int j=Math.max(0, i-8); j< Math.max(1, i); j++) {
				StringBuffer sb = new StringBuffer();
				sb.append(characters.get(j));				
				for(int m=j+1; m<=i; m++) {
					if(characters.get(m).matches("[0-9a-zA-Z].*"))
						sb.append(" "+characters.get(m));
					else
						sb.append(characters.get(m));			
				}
				HashMap<String, Integer> artistMap = wordTags.get(0);
				
				if(artistMap.containsKey(sb.toString())) {
					ArrayList<String> words = new ArrayList<String>();
					for(int k=j; k<i+1; k++) {
						words.add(characters.get(k));
					}
					String[] subtags = Xml2Column.word2tags(words, "ARTIST");
					for(int k=j; k<i+1; k++) {
						predicttags.set(k, subtags[k-j]);
					}
					i = j;
					break;
				}		
			}
		}
	}
	
	
	public static void main(String[] args) {
		
		String fileName = "/home/tm/disk/disk1/nermusic/test-liblinear-final-result-end";
		String resultFileName = "/home/tm/disk/disk1/nermusic/test-liblinear-final-result-end-mid";
		String finalFileName = "/home/tm/disk/disk1/nermusic/test-liblinear-final-result-end-post";
		PostProcessing p = new PostProcessing();
		p.matchSongAlbumPrefix(fileName, resultFileName);
		//p.matchArtistBand(fileName, resultFileName);
		p.matchResult(resultFileName, finalFileName);
		
		
	}
	
	
}
