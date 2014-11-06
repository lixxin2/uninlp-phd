package edu.hitsz.nlp.pinyin2character;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;

/**
 * 共现
 * @author Xinxin Li
 * @since Dec 4, 2012
 */
public class CoOccurrence {

	HashMap<String, Integer> fathers;
	HashMap<String, Double> sons;
	int fatherLength = 10000; //不设制>wordLength的词
	int sonLength = 10000;
	String delim = "-@%-";
	
	public CoOccurrence(int fatherLength, int sonLength) {
		this();
		if(fatherLength > 0)
			this.fatherLength = fatherLength;
		if(sonLength > 0)
			this.sonLength = sonLength;
	}
	
	public CoOccurrence() {
		fathers = new HashMap<String, Integer>();
		sons = new HashMap<String, Double>();
	}
	
	
	/** 读取句子和对应的拼音，其中句子已分词，拼音也都分开了*/
	public void readPinyinWord(String wordFileName, String pinyinFileName) {
		
		try {
			String wordFileEncoding = FileEncoding.getCharset(wordFileName);
			BufferedReader wordReader = new BufferedReader(new InputStreamReader(new FileInputStream(wordFileName), wordFileEncoding));
			String pinyinFileEncoding = FileEncoding.getCharset(pinyinFileName);
			BufferedReader pinyinReader = new BufferedReader(new InputStreamReader(new FileInputStream(pinyinFileName), pinyinFileEncoding));
			
			String wordLine = null;
			String pinyinLine = null;
			
			int count = 0;
			while ( (wordLine = wordReader.readLine()) != null 
					&& (pinyinLine = pinyinReader.readLine()) != null ) {
				
				count++;
				if(count % 10000 == 0) {
					System.out.print(count + "...");
					if(count % 100000 == 0)
						System.out.println();
				}
				
				String[] words = wordLine.split("\\s+");
				String[] pinyins = pinyinLine.split("\t");
				
				processPairs(pinyins, words);					
			}
			
			postProcess();
			
			wordReader.close();
			pinyinReader.close();
		}
		catch (IOException e) {
			
		}		
	}
	
		
	
	/** 读取句子和对应的拼音，其中句子已分词，拼音也都分开了*/
	public void readWordPOSLine(String wordPOSFileName, boolean bForward) {
		
		try {
			String wordPOSFileEncoding = FileEncoding.getCharset(wordPOSFileName);
			BufferedReader wordPOSReader = new BufferedReader(new InputStreamReader(new FileInputStream(wordPOSFileName), wordPOSFileEncoding));
			
			String wordLine = null;
			String POSLine = null;
			
			int count = 0;
			while ( (wordLine = wordPOSReader.readLine()) != null  
				&& (POSLine = wordPOSReader.readLine()) != null) {
				String blackLine = wordPOSReader.readLine();
				
				count++;
				if(count % 10000 == 0) {
					System.out.print(count + "...");
					if(count % 100000 == 0)
						System.out.println();
				}
				
				String[] words = wordLine.split("\\s+");
				String[] POSes = POSLine.split("\\s+");
				
				if(bForward)
					processPairs(words, POSes);
				else
					processPairs(POSes, words);
			}
			
			postProcess();
			
			wordPOSReader.close();
		}
		catch (IOException e) {
			
		}		
	}
	
	
	/** 读取词性和组块文件 */
	public void readPOSChunkColumn(String posChunkFileName, int first, int second) {		
					
		ConllFile trainFile=new ConllFile();
		trainFile.readFrom(posChunkFileName, 0);
		
		int sentenceNum=trainFile.getSentenceNumber();
		System.out.print("Sentence ");
		for(int j=0;j<sentenceNum;j++){
			if(j%100==0){
				System.out.print(Integer.toString(j)+"...");
				if(j%1000==0)
					System.out.println();
			}
			ConllSentence sentence = trainFile.getSentence(j);
			ArrayList<String> firstWords = sentence.getWords(first);
			ArrayList<String> secondWords = sentence.getWords(second);
		
			processPairs(firstWords, secondWords);
		}
		
		postProcess();
	}
	
	
	/**处理对应的很多对*/
	public void processPairs(String[] father, String[] son) {
		
		int length = father.length;
		if(length != son.length) {
			System.out.println("length differnt:\n" + father[0] + "\n" + son[0]);
			
		}
		else {
			for(int i=0; i<length; i++)
				processPair(father[i], son[i]);
		}		
		
	}
	
	
	/**处理对应的很多对*/
	public void processPairs(ArrayList<String> father, ArrayList<String> son) {
		
		int length = father.size();
		if(length != son.size()) {
			System.out.println("length differnt:\n" + father.get(0) + "\n" + son.get(0));			
		}
		else {
			for(int i=0; i<length; i++)
				processPair(father.get(i), son.get(i));
		}		
		
	}
	
	/**处理一对*/
	public void processPair(String father, String son) {
		
		if(father.length() <= fatherLength && son.length() <= sonLength) {
		
			//father
			if(!fathers.containsKey(father)) {
				fathers.put(father, 1);
			}
			else {
				fathers.put(father, fathers.get(father)+1);
			}
			
			//
			String fatherSonPair = father + delim + son;
			if(!sons.containsKey(fatherSonPair)) {
				sons.put(fatherSonPair, 1.0);
			}
			else {
				sons.put(fatherSonPair, sons.get(fatherSonPair)+1.0);
			}		
		}		
	}
	
	
	
	/** 后处理，统计频率 */
	public void postProcess() {
		
		HashMap<String, Double> newSons = new HashMap<String, Double>();
		
		Iterator<Entry<String, Double>> iter = sons.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Double> entry = iter.next();
			String fatherSonPair = entry.getKey();
			//
			double sonNumber = entry.getValue();
			if(sonNumber > 1e-5) {
				String[] subs = fatherSonPair.split(delim);
				if(subs.length != 2){
					System.out.println("no such " + fatherSonPair);
					//System.exit(-1);
					continue;
				}
				String father = subs[0];
				double fatherNumber = fathers.get(father);
				sonNumber = sonNumber / fatherNumber;
			}
			newSons.put(fatherSonPair, sonNumber);			
		}
		sons = newSons;		
		
	}
	
	public void store(String statFileName) {
		
		try {
			System.out.print("Saving state File: "+statFileName+ " ... ");
	    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(statFileName));
	    	out.writeInt(-1);
	    	out.writeObject(fathers);
	    	out.writeInt(-2);
	    	out.writeObject(sons);
	    	out.writeInt(fatherLength);
	    	out.writeInt(sonLength);
	    	out.writeUTF(delim);
	    	out.close();
	    	System.out.println("done.");
		}
		catch (IOException e) {
			
		}
	}
	
	public void read(String statFileName) {
		
		try {
			System.out.print("Loading state File: "+statFileName+ " ... ");
	    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(statFileName));
	    	int a = (Integer) in.readInt();
	    	if(a != -1)
	    		System.out.println("read wrong");
	    	fathers = (HashMap<String, Integer>) in.readObject();
	    	a = (Integer) in.readInt();
	    	if(a != -2)
	    		System.out.println("read wrong");
	    	sons = (HashMap<String, Double>) in.readObject();
	    	fatherLength = (Integer) in.readInt();
	    	sonLength = (Integer) in.readInt();
	    	delim = (String) in.readUTF();
	    	in.close();
	    	System.out.println("done");
		}
		catch (ClassNotFoundException e) {
			
		}
		catch (IOException e) {
			
		}
	}
	
	
	public double get(String father, String son) {
		
		if(father.length() <= fatherLength && son.length() <= sonLength) {
						
			String fatherSonPair = father + delim + son;
			if(sons.containsKey(fatherSonPair))
				return sons.get(fatherSonPair);
			else
				return 0.0;	
		}
		else
			return 1.0;
	}
	
	
	
	public static void main(String[] args) {
		/*
		String wordFileName = "/home/tm/disk/disk1/lm/pdgm.minwordnum.seg5s";
		String pinyinFileName = "/home/tm/disk/disk1/lm/pdgm.minwordnum.seg5s.pinyin";	
		String pinyinWordStatFileName = "/home/tm/disk/disk1/lm/stat.pinyinWord.0.0";
		
		CoOccurrence occur = new CoOccurrence(0, 0);		
		occur.readWordPinyin(wordFileName, pinyinFileName);
		occur.store(pinyinWordStatFileName);
		*/

		/*
		String wordPosFileName = "/home/tm/disk/disk1/lm/pdgm.minwordnum.seg5s.wp";
		String wordPosStatFileName = "/home/tm/disk/disk1/lm/stat.wordPOS.0.0";
		CoOccurrence occur2 = new CoOccurrence(0, 0);		
		occur2.readWordPOSLine(wordPosFileName, true);
		occur2.store(wordPosStatFileName);	
		*/
		
		String posChunkFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.wpc";
		CoOccurrence occur = new CoOccurrence(0, 0);
		//String posChunkStatFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.poschunk.stat";
		//occur.readPOSChunkColumn(posChunkFileName, 1, 2);
		//occur.store(posChunkStatFileName);
		//String chunkPOSStatFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.chunkpos.stat";
		//occur.readPOSChunkColumn(posChunkFileName, 2, 1);
		//occur.store(chunkPOSStatFileName);
		//String wordPOSStatFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.wordpos.stat";
		//occur.readPOSChunkColumn(posChunkFileName, 0, 1);
		//occur.store(wordPOSStatFileName);
		//String posWordStatFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.posword.stat";
		//occur.readPOSChunkColumn(posChunkFileName, 1, 0);
		//occur.store(posWordStatFileName);
		//String wordChunkFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.wordchunk.stat";
		//occur.readPOSChunkColumn(posChunkFileName, 0, 2);
		//occur.store(wordChunkFileName);
		String chunkWordFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.chunkword.stat";
		occur.readPOSChunkColumn(posChunkFileName, 2, 0);
		occur.store(chunkWordFileName);
		
	}
	
	
	
}

