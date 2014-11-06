package edu.hitsz.nlp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;

public class TypeDict extends HashMap<String, String>{	
	
	/**
	 * 从单列文件中读取词典，词类型为输入值
	 * @since May 16, 2012
	 * @param fileName
	 * @param type
	 * @return
	 */
	public static TypeDict read(String fileName, String type) {
		
		TypeDict dict = new TypeDict();
		BufferedReader reader = null;
		String line = null;		
		try {
			String fileEncoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 1 && !line.matches("[0-9a-zA-Z].*"))
					continue;
				if(line.length() > 0) {
					if(!dict.containsKey(line))
						dict.put(line, type);
				}
			}			
			reader.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return dict;
	}
	
	/**
	 * 添加新词典，词类型为输入参数
	 * @since May 16, 2012
	 * @param fileName
	 * @param type
	 */
	public void add(String fileName, String type) {
		BufferedReader reader = null;
		String line = null;
		
		try {
			String fileEncoding = FileEncoding.getCharset(fileName);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 1 && !line.matches("[0-9a-zA-Z].*"))
					continue;
				if(line.length() > 0) {
					if(!containsKey(line))
						put(line, type);
				}
			}			
			reader.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**返回需要的值，不需要的返回"O"*/
	public String getKeyValue(String key) {
		if(containsKey(key)) {
			return get(key);
		}
		else
			return "O";
	}
	
	public static TypeDict read(String fileName) {
		
		TypeDict dict = new TypeDict();
		BufferedReader reader = null;
		String line = null;		
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					String[] parts = line.split("[ \t]");
					if(parts.length == 2) {
						if(!dict.containsKey(parts[0]))
							dict.put(parts[0], parts[1]);
					}
				}
			}			
			reader.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return dict;
	}
	

}
