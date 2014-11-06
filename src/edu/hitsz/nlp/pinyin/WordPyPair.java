package edu.hitsz.nlp.pinyin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.chinese.ChineseWord;

/**
 * 词-音节对
 * 
 * <p> 相似的可参考{@link edu.hitsz.nlp.pinyin2character.PyWordPair}
 * @author Xinxin Li
 * @since Nov 9, 2012
 */
public class WordPyPair {
	
	public HashMap<String, ArrayList<String>> wpPair;
	
	public WordPyPair() {
		wpPair = new HashMap<String, ArrayList<String>>();
	}
	
	/**
	 * 载入unicode to pinyin词典
	 * @since Nov 11, 2012
	 * @param fileName
	 */
	public void loadUC2PY(String fileName) {
		
		String fileEncoding = FileEncoding.getCharset(fileName);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
			String line = null;
			int count = 0;
			
			while((line = reader.readLine()) != null) {
				if(count++ % 10000 == 0)
					System.out.println(count);
				String[] charyins = line.trim().split("\\s+");
				if(charyins.length >= 2) {
					String character = unicodeToString("\\" + "u" + charyins[0]);
					String yinSent = charyins[1];
					if(yinSent.startsWith("("))
						yinSent = yinSent.substring(1);
					if(yinSent.endsWith(")"))
						yinSent = yinSent.substring(0, yinSent.length()-1);
					String[] yins = yinSent.split(",");
					for(String yin : yins) {
						String syllableNoTone = new Syllable(yin).get(false);
						addCharYin(character, syllableNoTone);
					}
				}
				else {
					System.out.println("format is wrong in line: " + line);
				}
			}
			reader.close();
		}
		catch (IOException e){
			
		}
		
	}
	
	/**
	 * 载入sogou辞典
	 * @since Nov 11, 2012
	 * @param fileName
	 */
	public void loadWordPinyinDict(String fileName) {
		
		try {
			String fileEncoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
			String line = null;
			int count = 0;
			
			while((line = reader.readLine()) != null) {
				if(count++ % 10000 == 0)
					System.out.println(count);
				String[] charyins = line.trim().split("\t");
				if(charyins.length == 2) {
					String word = charyins[0];
					String yin = charyins[1];
					addCharYin(word, yin);
				}
				else {
					System.out.println("format is wrong in line: " + line);
				}
			}
			reader.close();
		}
		catch (IOException e){
			
		}
		
	}
	
	/** 添加字音到map*/
	private void addCharYin(String character, String yin) {
		
		yin = formatPinyin(yin);
		
		if(wpPair.containsKey(character)) {
			ArrayList<String> yins = wpPair.get(character);
			if(!yins.contains(yin)) {
				yins.add(yin);
				//wpPair.put(character, yins);
			}		
		}
		else {
			ArrayList<String> yins = new ArrayList<String>();
			yins.add(yin);
			wpPair.put(character, yins);
		}
		
	}
	
	/**  把十六进制Unicode编码字符串转换为中文字符串   */   
	public static String unicodeToString(String str) {    
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");       
		Matcher matcher = pattern.matcher(str);    
		char ch;    
		while (matcher.find()) {     
			ch = (char) Integer.parseInt(matcher.group(2), 16);     
			str = str.replace(matcher.group(1), ch + "");       
		}    
		return str;   
	}  
	
	
	/*   *  把中文字符串转换为十六进制Unicode编码字符串   */   
	public static String stringToUnicode(String s) {    
		String str = "";    
		for (int i = 0; i < s.length(); i++) {     
			int ch = (int) s.charAt(i);     
			if (ch > 255)      
				str += "\\u" + Integer.toHexString(ch);     
			else      
				str += "\\" + Integer.toHexString(ch);    
		}    
		return str;   
	}
	
	/** 
	 * 获得词的所有拼音:(1)如果词在词表中，则输出其拼音；（2）如果词不是汉字，则原样输出；
	 * （3）如果是汉字，但是词表中没有，则打印错误，退出
	 * @since Nov 21, 2012
	 * @param word
	 * @return
	 */
	public ArrayList<String> getYins(String word) {
		if(wpPair.containsKey(word))
			return wpPair.get(word);
		else if(!word.matches(ChineseWord.hanzisRegex)) {
			//System.out.println("word: " + word + " is not simplified hanzi in [\u3007\u4e00-\u9fa5]+");
			ArrayList<String> yins = new ArrayList<String>();
			yins.add(word);
			return yins;
		}
		else {
			System.out.println("there is no pinyin for word: " + word);
			System.exit(-1);
		}
		return null;
	}
	
	/** 获得词的第一拼音*/
	public String getYin(String word) {
		if(wpPair.containsKey(word))
			return wpPair.get(word).get(0);
		else
			return null;
	}
	

	public String formatPinyin(String yin) {
		return yin.replaceAll("u:e", "ue").replaceAll("u:", "v");
	}
	
	

	
	
	
	
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		String un2yin = "/home/tm/disk/disk1/pinyin/unicode_to_hanyu_pinyin.txt";
		String wordYinFile = "/home/tm/disk/disk1/pinyin/fctix.word.pinyin";
		WordPyPair wp = new WordPyPair();
		wp.loadUC2PY(un2yin);
		//wp.loadWordPinyinDict(wordYinFile);
		System.out.println();
		System.out.println(wp.stringToUnicode("哦"));
		System.out.println(wp.unicodeToString("\\u4E87"));
		
		
	}
	
	
	
	
	
	
	

}
