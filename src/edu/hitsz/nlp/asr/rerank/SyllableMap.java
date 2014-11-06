package edu.hitsz.nlp.asr.rerank;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.hitsz.java.file.local.FileEncoding;

/**
 * 音节 和 元音辅音 对应的Map
 * @author Xinxin Li
 * @since Sep 21, 2013
 */
public class SyllableMap {
	
	public HashMap<String, String> syllable2Parts;
	public HashMap<String, String> parts2Syllable;
	
	public SyllableMap() {
		syllable2Parts = new HashMap<String, String>();
		parts2Syllable = new HashMap<String, String>();
	}
	
	/** 从文件中读取音节 和 对应的分开格式。例如
	 *  <p> an	_a an
	 * @throws IOException 
	 */
	public void readFrom(String fileName) {
		
		try {
			String fileEncoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
	        
			String line = null;
			while((line = reader.readLine()) != null ) {
				line = line.trim();
				if(line.length() == 0)
					break;
				
				String[] subs = line.split("\t");
				syllable2Parts.put(subs[0], subs[1]);
				parts2Syllable.put(subs[1], subs[0]);
			}
			reader.close();		
		}
		catch (IOException e) {
			
		}
	}
	

}
