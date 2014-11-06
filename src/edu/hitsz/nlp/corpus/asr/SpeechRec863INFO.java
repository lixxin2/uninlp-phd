package edu.hitsz.nlp.corpus.asr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.pinyin.Syllable;

public class SpeechRec863INFO {
	
	String backgroundNoise;
	String recordDate;
	String micphoneLowFreguency;
	String micphoneHighFrequency;
	String micphoneImpedence;
	String micphoneSensity;
	String micphoneOthers;

	String SampleFrequency;
	String sampleBits;
	String channelNumber;
	String channelNoise;
	String ADinputOthers;
	String speakerName;
	String speakerNameSpell;
	String speakerSex; 
	String speakerHomeland;
	 
	String SpeakerKnowledge;
	String SpeakerAge;
	
	
	/**
	 * 从INFO.TXT文件中提取出文本和语音信息
	 * @since Sep 8, 2013
	 * @param infoFile
	 * @param pinyinFile
	 * @throws IOException 
	 */
	public static void extractTextPinyin(String infoFile, 
			String sentenceFile,
			String pinyinFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(infoFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile), fileEncoding));
		
		FileWriter sentWriter = null;
		if(sentenceFile != null)
			sentWriter = new FileWriter(sentenceFile);
		FileWriter pinyinWriter = null;
		if(pinyinFile != null)
			pinyinWriter = new FileWriter(pinyinFile);
		
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0)
				continue;
			
			String sentence = "";
			String pinyin = "";

			boolean pair = false;
			if(line.startsWith("Sentence") && !line.startsWith("Sentence spell")) {
				int start = line.indexOf(":");
				sentence = line.substring(start+1).trim();
				while((line = reader.readLine()) != null) {
					if(line.startsWith("Sentence spell")) {
						pair = true;
						start = line.indexOf(":");
						pinyin = line.substring(start+1).trim();	
						String[] origYins = pinyin.split("\\s+");
						StringBuffer pinyinBuf = new StringBuffer();
						for(int i=0; i < origYins.length; i++) {
							Syllable syl = new Syllable(origYins[i]);
							pinyinBuf.append(syl.get(false) + " ");
						}
						pinyin = pinyinBuf.toString().trim();
						break;
					}
				}
				if(!pair) {
					System.out.println("no corresponding pinyin for sentence " + sentence);
					System.exit(-1);
				}				
			}
			
			if(pair) {
				System.out.println(sentence);
				System.out.println(pinyin);	
				sentWriter.write(sentence + "\n");
				pinyinWriter.write(pinyin + "\n");
			}
			
		}
		
		reader.close();
		sentWriter.close();
		pinyinWriter.close();		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		
		String infoFile = "/home/tm/disk/disk1/asr-rerank/INFO/F39INFO.TXT";
		String textFile = "/home/tm/disk/disk1/asr-rerank/INFO/F39INFO-text.txt";
		String pinyinFile = "/home/tm/disk/disk1/asr-rerank/INFO/F39INFO-pinyin.txt";
		
		SpeechRec863INFO.extractTextPinyin(infoFile, textFile, pinyinFile);	
		
	}

}
