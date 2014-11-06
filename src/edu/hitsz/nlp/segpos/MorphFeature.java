package edu.hitsz.nlp.segpos;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MorphFeature implements Serializable{

	private static final long serialVersionUID = 1L;

	HashMap<String, Integer> tagMap;
	
	transient HashMap<String, CTBMorph> wordCTBMorph;	
	
	HashMap<String, String> wordCTBMorphPre;
	
	HashMap<String, String> wordCTBMorphSuf;
	
	
	public MorphFeature() {
		
		tagMap = new HashMap<String, Integer>();
		wordCTBMorph = new HashMap<String, CTBMorph>();
		wordCTBMorphPre = new HashMap<String, String>();
		wordCTBMorphSuf = new HashMap<String, String>();
	}
	
	
	public void extractFromFile(String file, String[] types, Options options) throws IOException {
    	
		processTags(types);   	    	
		
    	System.out.print("Extract MorphFeatures from file " +file + "..."); 
    	Reader segposReader = new Reader();
		segposReader.startReading(file, options);
		Instance instance = segposReader.getNext();
		
		while(instance != null) {			
			processInstance(instance, types);		   
		   instance = segposReader.getNext();
		}
    	
		processFinal(types);

		System.out.println(" ...done\n");
    	
    }
	
	
	
	
	/** 预处理词性 */
	public void processTags(String[] types) {
		for(String tag : types)
			tagMap.put(tag, tagMap.size());
	}
	
	/** 处理每一个句子 
	 * 
	 * @since 2013-2-7
	 * @param instance
	 */
	public void processInstance(Instance instance, String[] types) {
				
		String[] words = instance.words;
		String[] tags = instance.tags;
		int wordSize = words.length;
		for(int i=0; i<wordSize; i++) {
			String word = words[i];
			int wordLength = word.length();
			String tag = tags[i];
			int posIndex = tagMap.get(tag);
			
			//前后字的tag统计
			 String preChar = word.substring(0,1);
			 String sufChar = word.substring(wordLength-1);
			 if(!wordCTBMorph.containsKey(preChar)){
				 CTBMorph newMorph = new CTBMorph(types.length);
				 newMorph.pre[posIndex] = 1;
				 wordCTBMorph.put(preChar, newMorph);
			 }
			 else{
				 CTBMorph newMorph = wordCTBMorph.get(preChar);
				 newMorph.pre[posIndex] = 1;
				 wordCTBMorph.put(preChar, newMorph);
			 }
			 if(!wordCTBMorph.containsKey(sufChar)){
				 CTBMorph newMorph = new CTBMorph(types.length);
				 newMorph.suf[posIndex] = 1;
				 wordCTBMorph.put(sufChar, newMorph);
			 }
			 else{
				 CTBMorph newMorph = wordCTBMorph.get(sufChar);
				 newMorph.suf[posIndex] = 1;
				 wordCTBMorph.put(sufChar, newMorph);
			 }
		}
		
	}
	
	
	public void processFinal(String[] tags) {
		
		Iterator<Map.Entry<String, CTBMorph>> iter= wordCTBMorph.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, CTBMorph> entry = iter.next();
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
		
	}
	
}
