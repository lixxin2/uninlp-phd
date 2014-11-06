package edu.hitsz.nlp.segpos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 统计语料库中的词长和
 * @author Xinxin Li
 * @since 2013-2-5
 */
public class WordPosFreq implements Serializable{

	private static final long serialVersionUID = 3375778105940315964L;
	
	/** 每个词对应的词性和出现的次数 */
	HashMap<String, PosFreq> wordFreqMap;
	
	transient HashMap<String, CTBMorph> wordCTBMorph;
	
	HashMap<String, String> wordCTBMorphPre;
	
	HashMap<String, String> wordCTBMorphSuf;
	
	/** 每个词性对应的最长词长，表示为每个词长对应的所有词性 */
	HashMap<Integer, ArrayList<String>> lengthPosMap;


	public WordPosFreq(){
		wordFreqMap = new HashMap<String, PosFreq>();
		wordCTBMorph = new HashMap<String, CTBMorph>();
		wordCTBMorphPre = new HashMap<String, String>();
		wordCTBMorphSuf = new HashMap<String, String>();
		lengthPosMap = new HashMap<Integer, ArrayList<String>>();
	}

	public WordPosFreq(String wordFreqName){
		this();
		read(new File(wordFreqName));
	}

	/**获得词对应的词性,经过两个规则，*/
	public ArrayList<String> getPos(String word){
		if(wordFreqMap.containsKey(word))
			return wordFreqMap.get(word).pos;
		else {
			int wordLength = word.length();
			if(wordLength <= lengthPosMap.size())
				return lengthPosMap.get(wordLength);
			else 
				return new ArrayList<String>();
		}
	}


	/**
	 * 读取word的频度
	 * @param wordFreqName
	 */
	public void read(File wordFreqFile){
		
		BufferedReader reader = null;
		int stopsignal=0;
		wordFreqMap.clear();
		wordCTBMorph.clear();
		wordCTBMorphPre.clear();
		wordCTBMorphSuf.clear();
		
		try{
			reader = new BufferedReader(new FileReader(wordFreqFile));			
			String tempString=null;
			while ((tempString = reader.readLine())!= null && stopsignal==0){
				if (!tempString.trim().equals("")){
					String[] words = tempString.trim().split("\t");
					String word = words[0];
					PosFreq newPosFreq = new PosFreq();
					newPosFreq.freq = Integer.parseInt(words[1]);
					for(int i=2; i<words.length; i++)
						newPosFreq.pos.add(words[i]);
					wordFreqMap.put(word, newPosFreq);
				}
				else
					break;
			}
			while ((tempString = reader.readLine())!= null && stopsignal==0){
				if (!tempString.trim().equals("")){
					String[] words = tempString.trim().split("\t");
					String word = words[0];
					String preFreq = words[1];
					wordCTBMorphPre.put(word, preFreq);
				}
				else
					break;
			}
			while ((tempString = reader.readLine())!= null && stopsignal==0){
				if (!tempString.trim().equals("")){
					String[] words = tempString.trim().split("\t");
					String word = words[0];
					String sufFreq = words[1];
					wordCTBMorphSuf.put(word, sufFreq);
				}
			}
			reader.close();
			System.out.println("Read wordFreq file successed!\n");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	/**
	 * 将训练文件中的词的频度和pos提取出来
	 * <p> 保存每个tag的最大词长，
	 * <p> 频度超过5的词，只采用出现的pos tag;其他的全部采用
	 * @param wordTrainName
	 * @param tags POS tags
	 * @throws IOException 
	 */
	public void extract(String wordTrainName, String[] tags, Options options) throws IOException{
		
		System.out.println("Extract tag frequency from file: " + wordTrainName + "...");
		int limitedNumber = 5; //设置词出现次数大于limitedNumber的只用已有的POS
		int wordMaxLength = 20;
		
		ArrayList<String> tagVec = new ArrayList<String>();
		ArrayList<Integer> tagMaxLengthVec = new ArrayList<Integer>();
		int tagSize = tags.length;
		for(int i=0; i<tagSize; i++) {
			tagVec.add(tags[i]);
			tagMaxLengthVec.add(0);
		}
		
		Reader segposReader = new Reader();
		segposReader.startReading(wordTrainName, options);
		Instance instance = segposReader.getNext();
		int count = 0;
		while(instance != null) {
			if(count % 1000 == 0) {
				System.out.print(count+"...");
				if(count % 10000 == 0) 
					System.out.println();
			}
			count++;
			//依存关系
			//System.out.println(count++);
			int sentenceLength = instance.words.length;
			String[] words = instance.words;
			String[] poses = instance.tags;
			for(int j=0; j<sentenceLength; j++) {
				String word = words[j];
				 int wordLength = word.length();
				 String pos = poses[j];
				 int posIndex = tagVec.indexOf(pos);
				 //对每个词建立tag频度统计
				 if(!wordFreqMap.containsKey(word)){
					 PosFreq newPosFreq = new PosFreq();
					 newPosFreq.freq = 1;
					 newPosFreq.pos.add(pos);
					 wordFreqMap.put(word, newPosFreq);
				 }
				 else{
					 PosFreq newPosFreq = wordFreqMap.get(word);
					 newPosFreq.freq += 1;
					 if(!newPosFreq.pos.contains(pos))
						 newPosFreq.pos.add(pos);
					 wordFreqMap.put(word, newPosFreq);
				 }
				 //对每个tag，保存其对应最大词长
				 if(tagMaxLengthVec.get(posIndex) < wordLength)
					 tagMaxLengthVec.set(posIndex, wordLength);
				 //前后字的tag统计
				 String preChar = word.substring(0,1);
				 String sufChar = word.substring(wordLength-1);
				 if(!wordCTBMorph.containsKey(preChar)){
					 CTBMorph newMorph = new CTBMorph(tags.length);
					 newMorph.pre[posIndex] = 1;
					 wordCTBMorph.put(preChar, newMorph);
				 }
				 else{
					 CTBMorph newMorph = wordCTBMorph.get(preChar);
					 newMorph.pre[posIndex] = 1;
					 wordCTBMorph.put(preChar, newMorph);
				 }
				 if(!wordCTBMorph.containsKey(sufChar)){
					 CTBMorph newMorph = new CTBMorph(tags.length);
					 newMorph.suf[posIndex] = 1;
					 wordCTBMorph.put(sufChar, newMorph);
				 }
				 else{
					 CTBMorph newMorph = wordCTBMorph.get(sufChar);
					 newMorph.suf[posIndex] = 1;
					 wordCTBMorph.put(sufChar, newMorph);
				 }		
			}
			instance = segposReader.getNext();
		}
		
		//排除出现次数小于limitedNumber的词
		Iterator<Map.Entry<String, PosFreq>>iter= wordFreqMap.entrySet().iterator();
		ArrayList<String> deleteWords = new ArrayList<String>();
		while (iter.hasNext()) {
			Map.Entry<String, PosFreq> entry = iter.next();
		    String word = entry.getKey().toString();
		    PosFreq newPosFreq = (PosFreq) entry.getValue();
		    if(newPosFreq.freq < limitedNumber){
		    	deleteWords.add(word);
		    }
		}
		for(String word : deleteWords)
			wordFreqMap.remove(word);
				
		//保存每个tag的最大词长，
		for(int i=0; i<wordMaxLength; i++) {
			ArrayList<String> posArr = new ArrayList<String>();
			for(int j=0; j<tagSize; j++) {
				String tag = tagVec.get(j);
				int tagMaxLength = tagMaxLengthVec.get(j);
				if(tagMaxLength > i) {
					posArr.add(tag);
				}					
			}
			if(posArr.size() > 0)
				lengthPosMap.put(i+1, posArr);
		}
				
		//
		Iterator<Map.Entry<String, CTBMorph>> iter2= wordCTBMorph.entrySet().iterator();
		while (iter2.hasNext()) {
			Map.Entry<String, CTBMorph> entry = iter2.next();
		    String word = entry.getKey().toString();
		    CTBMorph newMorph = (CTBMorph) entry.getValue();
		    StringBuffer preString = new StringBuffer();
		    StringBuffer sufString = new StringBuffer();
		    for(int i=0; i<tags.length; i++){
		    	preString.append(newMorph.pre[i]);
		    	sufString.append(newMorph.suf[i]);
		    }
		    wordCTBMorphPre.put(word,preString.toString());
		    wordCTBMorphSuf.put(word,sufString.toString());
		}
		
		System.out.println(" ...done");

	}

}

class PosFreq implements Serializable{
	
	int freq=0;
	ArrayList<String> pos = new ArrayList<String>();
}

class CTBMorph{
	
	int[] pre;
	int[] suf;
	
	public CTBMorph(String[] tags) {
		pre = new int[tags.length];
		suf = new int[tags.length];
	}
	
	public CTBMorph(int length) {
		pre = new int[length];
		suf = new int[length];
	}
}
