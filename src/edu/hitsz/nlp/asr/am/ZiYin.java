package edu.hitsz.nlp.asr.am;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.pinyin2character.PyWordPair;

public class ZiYin {
	
	
	
	/**
	 * 检测两个不同字典中字音的标注有何不同
	 * 
	 * <p> 比如： lu: lv 
	 * @since Nov 4, 2012
	 * @param ziYinFile
	 * @param wordPinyinFile
	 * @throws IOException
	 */
	public static void checkYinDiff(String ziYinFile, String wordPinyinFile) throws IOException {
		PyWordPair ziYinDict = new PyWordPair();
		ziYinDict.loadPinyinWord(ziYinFile);
		PyWordPair wordYinDict = new PyWordPair();
		wordYinDict.loadWordPinyin(wordPinyinFile);
		
		HashMap<String, ArrayList<String>> ziYinMap = ziYinDict.getPair();
		HashMap<String, ArrayList<String>> wordYinMap = wordYinDict.getPair();
		
		HashMap<String, String> newWordYinMap = new HashMap<String, String>();
		
		System.out.println("yin in wordyin file, but not in ziyin file");
		Iterator<Entry<String, ArrayList<String>>> iter2 = wordYinMap.entrySet().iterator();
		while(iter2.hasNext()) {
			Entry<String, ArrayList<String>> entry = iter2.next();
			String yin = entry.getKey();
			String[] yins = yin.split(" ");
			for(String yinTmp: yins) {
				if(!ziYinMap.containsKey(yinTmp))
					System.out.println(yinTmp);
				if(!newWordYinMap.containsKey(yinTmp))
					newWordYinMap.put(yinTmp, "");
			}
		}
		
		System.out.println("yin in ziyin file, but not in wordyin file");
		Iterator<Entry<String, ArrayList<String>>> iter = ziYinMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, ArrayList<String>> entry = iter.next();
			String yin = entry.getKey();
			if(!wordYinMap.containsKey(yin))
				System.out.println(yin);
		}
		
		
		
		
		
		
	}
	
	public static void main(String[] args) throws IOException {
		String ziYinFile = "/home/tm/disk/disk1/pinyin2character/train-pinyinWord";
		String wordPinyinFile = "/home/tm/disk/disk1/pinyin2character/lm/all.ict.150k.vocab.yin00";
		ZiYin.checkYinDiff(ziYinFile, wordPinyinFile);
	}
	
	
	
	

}
