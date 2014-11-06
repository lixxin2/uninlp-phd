package edu.hitsz.nlp.pinyin.pinyin4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jsoup.helper.StringUtil;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.corpus.ptb.PTB;
import edu.hitsz.nlp.pinyin.WordPyPair;
/**
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
*/

public class GetPinyin {
	
	
	//HanyuPinyinOutputFormat outputFormat;
	ArrayList<String> notChinese;
	
	/**
	public GetPinyin() {
		outputFormat = new HanyuPinyinOutputFormat();
	    // fix case type to lowercase firstly, change VChar and Tone
	    // combination
	    outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);	
	    // WITH_U_AND_COLON and WITH_TONE_NUMBER
	    outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	    outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	    
	    notChinese = new ArrayList<String>();
	    notChinese.addAll(ListEx.toList(CTB.punctuations));
	    notChinese.addAll(ListEx.toList(CTB.arabNumbers));
	    notChinese.addAll(ListEx.toList(CTB.letters));
	    notChinese.addAll(ListEx.toList(PTB.punctuations));
	}
	*/
		
	/**
	 * 获取句子的拼音
	 * @since Nov 10, 2012
	 * @param outputFormat
	 * @param sentence
	 * @return
	 */
	/**
	public String getSentencePinyin(String sentence){
		
		System.out.println(sentence);
		
		String sent = sentence; //sentence.replaceAll("\\s+", "");
		int sentenceLength = sent.length();
		String[] pinyins = new String[sentenceLength];
		for(int i=0; i<sentenceLength; i++) {
			char character = sent.charAt(i);
			if(notChinese.contains(String.valueOf(character))) {
				pinyins[i] = String.valueOf(character);
			}
			else if(String.valueOf(character).matches("[ \t]")) {
				pinyins[i] = "\t";
			}
			else {
				pinyins[i] = getCharacterPinyin(character);
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<sentenceLength; i++) {
			sb.append(pinyins[i]);
			if(!pinyins[i].equals("\t") && (i+1 < sentenceLength && !pinyins[i+1].equals("\t")))
				sb.append(" ");			
		}
		
		return sb.toString().trim();        
	}
	*/
	
	/**
	 * 通过pinyin4j获取字符的拼音
	 * 
	 * <p>有两个问题： u:->v, u:e->ve
	 * <p> 这：zhei->zhe
	 * @since Nov 10, 2012
	 * @param outputFormat
	 * @param character
	 * @return
	 */
	/**
	public String getCharacterPinyin4j(char character) {
				
		try {
			String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(character, outputFormat);
			return pinyins[0];
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("no pinyin for character " + character);
        return "";
        
	}
	
	public String getCharacterPinyin(char character) {
		
		try {
			String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(character, outputFormat);
			return pinyins[0];
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("no pinyin for character " + character);
        return "";
        
	}
	*/
	
	/**
	 * 获取文件的拼音
	 * @since Nov 10, 2012
	 * @param inputFileName
	 * @param outFileName
	 */
	/*
	public void getFilePinyin(String inputFileName, String outFileName) {
		
		String fileEncoding = FileEncoding.getCharset(inputFileName);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), fileEncoding));
			FileWriter writer = new FileWriter(outFileName);
			String line = "";
			while((line = reader.readLine()) != null) {
				String sentPinyin = getSentencePinyin(line.trim());
				writer.write(sentPinyin +"\n");
			}			
	        reader.close();
	        writer.close();	    	
		}
		catch (IOException e) {
			
		}	
	}
	
	
	public static void main(String[] args) {
		
		
		GetPinyin getPy = new GetPinyin();
		String sentPinyin = getPy.getSentencePinyin("这");
		System.out.println(sentPinyin);
		String inputFileName = "/home/tm/disk/disk1/pinyin2character/hanzi-UTF-8";
		String outFileName = "/home/tm/disk/disk1/pinyin2character/pinyin-4j";
		getPy.getFilePinyin(inputFileName, outFileName);
		

	}
	*/
	

}
