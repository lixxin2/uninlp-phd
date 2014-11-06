package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.hitsz.java.file.local.FileEncoding;


public class PyCharReader {

	public BufferedReader reader;
	
	public PyCharReader() {
		
	}
	
	/**
	 * 初始化，包括设置读取器
	 * @since 2012-2-29
	 * @param file
	 */
	public PyCharReader(String file){
		if (file == null || file.isEmpty()){
			System.out.println("no read pychar file in "+this.getClass().getCanonicalName());
			System.exit(1);
		}
		startReading(file);
	}
	
	/**
	 * 设置读取器
	 * @since 2012-2-29
	 * @param file
	 */
	public void startReading(String file) {
		try {
			if(!new File(file).exists()) {
				System.out.println("file: " + file + " doesn't exist");
				System.exit(-1);
			}
			String fileEncoding = FileEncoding.getCharset(file);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), fileEncoding));
		}
		catch (IOException e) {
			System.out.println(e.getStackTrace());			
		}
	}
	
	/**
	 * 读取一个完整的pinyin和句子的实例
	 * @since 2012-2-29
	 * @return
	 */
	public PyCharInstance getNext() {
		
		try{
			String pinyin_line = null;
			if((pinyin_line = reader.readLine()) != null) {
			
				pinyin_line = pinyin_line.trim();
				
				if(pinyin_line.length() == 0 ) {
				    reader.close();
				    return null;
				}
				String form_line = reader.readLine().trim();

				String[] pinyin = pinyin_line.split("\t");
				String[] words = form_line.split("\t");
				int wordLength = pinyin.length;
				if(wordLength != pinyin.length) {
					System.out.println("word number is different from pinyin number");
					System.exit(-1);
				}
				
				int characterLength = 0;
				for(int i=0; i<wordLength; i++)
					characterLength += words[i].length();
				
				int position=0;				
				String[] characterYins = new String[characterLength];
				String[] characters = new String[characterLength];

				for(int i=0; i<wordLength; i++) {
					String[] yins = pinyin[i].split(" ");
					int yinLength = yins.length;
					if(yinLength != words[i].length()) {
						System.out.println("word is different from pinyin");
						System.exit(-1);
					}
					for(int j=0; j<yinLength; j++) {
						characterYins[position] = yins[j];
						characters[position] = words[i].substring(j,j+1);
						position++;
					}
				}
				
				PyCharInstance instance = new PyCharInstance(characterYins, characters,
						pinyin, words);
				
				return instance;
			}
			else
				reader.close();
			
			return null;
			
		}
		catch(IOException e){
			System.out.println(e.getStackTrace());
		}	
		return null;
		
	}
	
	/**
	 * 读取一个拼音实例，一句话
	 * @since 2012-2-29
	 * @return
	 */
	public PyCharInstance getNextPinyin() {
		
		try{
			String pinyin_line = null;
			if((pinyin_line = reader.readLine()) != null) {	
				if(pinyin_line.length() == 0 ) {
				    reader.close();
				    return null;
				}
				
				String[] pinyin = pinyin_line.split("\\s+");
				int length = pinyin.length;
				
				PyCharInstance instance = new PyCharInstance(pinyin, length);
				
				return instance;
			}
			else 
				reader.close();
			return  null;
			
		}
		catch(IOException e){
			System.out.println(e.getStackTrace());
		}	
		
		return null;
		
	}
	
	
}
