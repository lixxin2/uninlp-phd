package edu.hitsz.nlp.nermusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Stat {

	/**
	 * 统计前后出现词的次数
	 * @since May 15, 2012
	 * @param fileName
	 * @param statName
	 */
	public void statBeforeEnd(String fileName, String statName, String tagType) {
		
		ArrayList<String> characters = new ArrayList<String>();
		ArrayList<String> tags = new ArrayList<String>();
		
		BufferedReader reader = null;
		FileWriter writer = null;
		String line = null;		
		
		int range = 5;
		HashMap<String, Integer> map = new HashMap<String, Integer>();		
		
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				//空行，一个句子结束
				if(line.length() <= 0) {
					int length = characters.size();
					for(int i=0; i<length; i++) {
						String current = characters.get(i);
						String currentTag = tags.get(i);
						if(currentTag.startsWith("S-"+tagType) || currentTag.startsWith("B-"+tagType)) {
							int start = 0;
							if(i - range > 0) start = i-range;
							for(int j=start; j<i; j++) {
								StringBuffer sb = new StringBuffer();
								sb.append(characters.get(j));
								for(int k=j+1; k<i; k++) {
									sb.append(characters.get(k));									
								}
								if(map.containsKey("B---"+sb.toString())) {
									map.put("B---"+sb.toString(), map.get("B---"+sb.toString()) + 1);
								}
								else {
									map.put("B---"+sb.toString(), 1);
								}
							}							
						}	
						if(currentTag.startsWith("S-ARTIST") || currentTag.startsWith("E-ARTIST")) {
							int end = length-1;
							if(i + range < length) end = i+range;
							StringBuffer sb = new StringBuffer();
							for(int k=i+1; k<=end; k++) {
								sb.append(characters.get(k));
								if(map.containsKey("E---"+sb.toString())) {
									map.put("E---"+sb.toString(), map.get("E---"+sb.toString()) + 1);
								}
								else {
									map.put("E---"+sb.toString(), 1);
								}
							}				
						}
					}
					characters.clear();
					tags.clear();					
				}
				else {
					String[] parts = line.split("\t");
					if(parts.length == 2) {
						characters.add(parts[0]);
						tags.add(parts[1]);		
					}
					else
						System.out.println(line);
				}
			}
			reader.close();
			//
			writer = new FileWriter(new File(statName));
			Iterator<Map.Entry<String, Integer>> iter = map.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String, Integer> entry = iter.next();
				String word = entry.getKey();
				int number = entry.getValue();
				if(number > 10 && word.startsWith("B"))
					writer.write(word+"\t"+number+"\n");				
			}
			writer.close();
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**统计文件中实体的数目*/
	public void statNumber(String fileName) {
		BufferedReader reader = null;
		String line = null;		
		int artistNumber = 0;
		int aliasNumber = 0;
		int bandNumber = 0;
		int songNumber = 0;
		int albumNumber = 0;
		
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					String[] parts = line.split("\t");
					if(parts.length == 2) {
						String tag = parts[1];
						if(tag.equals("S-ARTIST") || tag.equals("B-ARTIST"))
							artistNumber += 1;
						else if(tag.equals("S-ALIAS") || tag.equals("B-ALIAS"))
							aliasNumber += 1;
						else if(tag.equals("S-BAND") || tag.equals("B-BAND"))
							bandNumber += 1;
						else if(tag.equals("S-SONG") || tag.equals("B-SONG"))
							songNumber += 1;
						else if(tag.equals("S-ALBUM") || tag.equals("B-ALBUM"))
							albumNumber += 1;
					}
					else
						System.out.println(line);
				}
			}
			reader.close();
			System.out.println("artistNumber+ aliasNumber+bandNumber+songNumber+albumNumber");
			System.out.println(artistNumber+ aliasNumber+bandNumber+songNumber+albumNumber);
			System.out.println(artistNumber+","+aliasNumber+","+bandNumber+","+songNumber+","+albumNumber);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void main(String[] args) {
		String fileName = "/home/tm/disk/disk1/nermusic/dev-tag";
		String statName = "/home/tm/disk/disk1/nermusic/ALIAS-stat-B";
		Stat stat = new Stat();
		//stat.statBeforeEnd(fileName, statName, "ALIAS");
		stat.statNumber(fileName);
	}
	
	
}
