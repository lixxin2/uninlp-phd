package edu.hitsz.nlp.struct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * 用HashMap存放字典
 * @author tm
 *
 */
public class DictionaryHashMap extends HashMap<String, Integer>{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public DictionaryHashMap(String fileName){
		readFrom(fileName);
	}

	/**从文件中读取字典，每一行一个词
	 *
	 * @param fileName
	 */
	public void readFrom(String fileName){
		try{
			BufferedReader newReader = new BufferedReader(new FileReader(new File(fileName)));
			String tmpString = null;
			while((tmpString = newReader.readLine())!=null){
				this.put(tmpString.trim(), 1);
			}
		}
		catch(IOException e){

		}
	}

	public void add(String newString){
		this.put(newString, 1);
	}

	public boolean isExist(String newString){
		return this.containsKey(newString);
	}

}
