package edu.hitsz.nlp.nermusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 提取歌手名，组合名
 */
public class ExtName {

	/**
	 * 从文本文件中提取歌手名，组合名
	 * @since May 6, 2012
	 * @param subFileNames
	 * @param combFileName
	 * @throws IOException
	 */
	public static void ext2Name(ArrayList<String> subFileNames, String combFileName){
		
		int number = subFileNames.size();
		
		HashMap<String, Integer> names = new HashMap<String, Integer>();
		
		if(number > 0) {
			for(int i=0; i<number; i++) {
				try{
					BufferedReader reader = new BufferedReader(new FileReader(new File(subFileNames.get(i))));
					String line = null;
					while((line = reader.readLine()) != null) {
						String[] name = line.trim().split("\t");
						for(String s : name) {
							if(s.trim().length() > 0) {
								if(s.contains("(")) {								
									String[] t = s.trim().split("\\(");
									if(t[0].trim().length() > 0)
										names.put(t[0].trim(), 1);
									if(t[1].length() > 0) {
										String[] r = t[1].split("\\)");
										if(r.length > 0) {
											if(r[0].trim().length() > 0) {
												names.put(r[0].trim(), 1);
											}
										}			
									}
								}
								else
									names.put(s.trim(), 1);
							}
						}					
					}	
					reader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(names.size() > 0) {
				try {
					FileWriter writer = new FileWriter(new File(combFileName));
					Iterator<Entry<String, Integer>> iter = names.entrySet().iterator();
					while(iter.hasNext()) {
						Entry<String, Integer> entry = iter.next();
						writer.write(entry.getKey()+ "\n");					
					}
					writer.close();		
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 把音译英文名中间空格换为点
	 * @since May 23, 2012
	 */
	public static void proChingEnglishName() {
		String filename = "/home/tm/disk/disk1/nermusic/dict/band";
		String outname = "/home/tm/disk/disk1/nermusic/dict/bands2";
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			FileWriter writer = new FileWriter(new File(outname));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 1)
					continue;
				if(line.contains(" ") && !line.matches("[0-9a-zA-Z].*")) 
					line = line.replaceAll(" ", "·");
				writer.write(line + "\n");	
			}
			reader.close();
			writer.close();		
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void extEnglishName( ) {
		String filename = "/home/tm/Downloads/dist.male.first";
		String outname = "/home/tm/Downloads/EnglishFirstNameMale";
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			FileWriter writer = new FileWriter(new File(outname));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				String name = line.split("[ ,\t]")[0];
				name = name.substring(0,1) + name.substring(1).toLowerCase();
				writer.write(name + "\n");	
			}
			reader.close();
			writer.close();		
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	
	
	public static void ext2Name(String dir) {
		ArrayList<String> artistFileNames = new ArrayList<String>();
		artistFileNames.add(dir + "/" + "China man 1ting");
		artistFileNames.add(dir + "/" + "China man google");
		artistFileNames.add(dir + "/" + "China woman 1ting");
		artistFileNames.add(dir + "/" + "China woman google");
		artistFileNames.add(dir + "/" + "Europe man 1ting");
		artistFileNames.add(dir + "/" + "Europe man google");
		artistFileNames.add(dir + "/" + "Europe woman 1ting");
		artistFileNames.add(dir + "/" + "Europe woman google");
		artistFileNames.add(dir + "/" + "JapanKorea man google");
		artistFileNames.add(dir + "/" + "JapanKorea woman google");
		artistFileNames.add(dir + "/" + "Other man google");
		artistFileNames.add(dir + "/" + "Other woman google");
		
		String artistFileName = dir + "/"+ "artist";
		ext2Name(artistFileNames, artistFileName);
		
		ArrayList<String> bandFileNames = new ArrayList<String>();
		bandFileNames.add(dir + "/" + "China band 1ting");
		bandFileNames.add(dir + "/" + "China band google");
		bandFileNames.add(dir + "/" + "Europe band 1ting");
		bandFileNames.add(dir + "/" + "Europe band google");
		bandFileNames.add(dir + "/" + "JapanKorea band google");
		bandFileNames.add(dir + "/" + "Other band google");
		
		String bandFileName = dir + "/"+ "band";
		ext2Name(bandFileNames, bandFileName);
				
	}
	
	
	public static void main(String[] args) {

		//ExtName.ext2Name("/home/tm/disk/disk1/dict/artists");
		//ExtName.proChingEnglishName();
		ExtName.extEnglishName();
	}
	
}
