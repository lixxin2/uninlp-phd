package edu.hitsz.nlp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;

/**
 * 字典类
 * @author Xinxin Li
 * @since May 15, 2012
 */
public class Dict extends HashMap<String, Integer>{
	
	/**
	 * 从文件读取字典
	 * @since May 15, 2012
	 * @param fileName
	 */
	public static Dict read(String fileName) {
		
		Dict dict = new Dict();
		
		BufferedReader reader = null;
		String line = null;
		int number = 0;
		
		try {
			String encoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					if(!dict.containsKey(line))
						dict.put(line, number++);
				}
			}			
			reader.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return dict;
	}
	
	public int find(String key) {
		if(this.containsKey(key))
			return 1;
		else
			return 0;
	}
	
	
}
