package edu.hitsz.nlp.subWordSegPOS;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;


import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.segpos.Reader;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;

/**
 * 根据(Sun,2011)提取特征
 * @author Xinxin Li
 * @since 2013-5-1
 */
public class GenerateFeatures {

	//特征和类型Map
	HashMap<String, Integer> typeMap;
	HashMap<String, Integer> featureMap;
	
	public GenerateFeatures() {
		typeMap = new HashMap<String, Integer>();
		featureMap = new HashMap<String, Integer>();
	}
	
	/** 提取特征，转换为svm-hmm格式 
	 * 
	 * @since 2013-5-2
	 * @param trainFileName
	 * @param trainFeaFileName
	 * @param isTrain 表示为训练文件提取特征，还是测试文件提取特征。两者不同在于训练文件生成特征map，而测试文件使用特征map
	 * @throws IOException
	 */
	public void generateFea(String trainFileName, 
			String trainFeaFileName,
			boolean isTrain) throws IOException {
		
		ConllFile trainFile = new ConllFile();
		trainFile.readFrom(trainFileName, -1);
		int sentenceNumber = trainFile.getSentenceNumber();
		//
		FileWriter writer = new FileWriter(trainFeaFileName);
		//每个句子
		for(int i=0; i<sentenceNumber; i++) {
			
			if(i % 100 == 0) {
				System.out.print(i+",");
				if(i % 1000 == 0)
					System.out.println();
			}
			
			ConllSentence sentence = trainFile.getSentence(i);
			int sentenceLength = sentence.getSentenceLength();
			ArrayList<String> words = sentence.getWords(0);
			ArrayList<String> subTags = sentence.getWords(1);
			ArrayList<String> goldTags = sentence.getWords(2);
			StringBuffer strbuf = new StringBuffer();
			//每个subWord
			for(int j=0; j<sentenceLength; j++) {
				String goldTag = goldTags.get(j);
				ArrayList<String> features = getFeatures(words, subTags, sentenceLength, j);
				int tagId = getId(goldTag, typeMap);
				TreeSet<Integer> featuresIds = new TreeSet<Integer>();
				for(int k=0; k<features.size(); k++) {
					int featureId = getId(features.get(k), featureMap, isTrain);
					featuresIds.add(featureId);
				}
				ArrayList<Integer> featuresList = new ArrayList<Integer>();
				Iterator<Integer> iter = featuresIds.iterator();
				while(iter.hasNext()) {
					featuresList.add(iter.next());
				}
				if(isTrain)
					strbuf.append(tagId);
				else
					strbuf.append(0);
				strbuf.append(" qid:"); strbuf.append(i+1);
				for(int featureId : featuresList) {
					if(featureId != 0) {
						strbuf.append(" "); 
						strbuf.append(featureId);
						strbuf.append(":1");
					}
				}
				strbuf.append("\n");
			}		
			writer.write(strbuf.toString());
		}		
		writer.close();
	}
	
	
	
    public void saveMap(String file) throws IOException {
    	System.out.print("Saving model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    	out.writeObject(typeMap);
    	out.writeObject(featureMap);
    	out.close();
    	System.out.println(" done.");
    }

    public void loadMap(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    	typeMap =  (HashMap<String, Integer>) in.readObject();
    	featureMap =  (HashMap<String, Integer>) in.readObject();
    	in.close();
    	System.out.println("done");
    }
    
    public void loadTypeMap(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    	typeMap =  (HashMap<String, Integer>) in.readObject();
    	in.close();
    	System.out.println("done");
    }
    
     
	
	public int getId(String idStr, HashMap<String, Integer> idMap) {
		return getId(idStr, idMap, true);
	}
	
	
	/**
	 * 得到在Map中的序列，从1开始
	 * <p> 如果有，则返回对于
	 * <p> 如没有，则加入
	 * @since Feb 25, 2013
	 * @param idStr
	 * @param idMap
	 * @param isTrain
	 * @return
	 */
	public int getId(String idStr, HashMap<String, Integer> idMap, boolean isTrain) {
		
		if(idMap.containsKey(idStr))
			return idMap.get(idStr);
		else {
			if(isTrain) {
				int id = idMap.size()+1;
				idMap.put(idStr, id);
				return id;
			}
			else
				return 0;
		}	
	}
	
	
	/**
	 * 得到sub word的特征，参考（Sun, 2011)
	 * @since Feb 25, 2013
	 * @param words
	 * @param subTags
	 * @param length
	 * @param i
	 * @return
	 */
	public ArrayList<String> getFeatures(ArrayList<String> words,
			ArrayList<String> subTags,
			int length,
			int i) {
	
		ArrayList<String> features = new ArrayList<String>();
		
		String curWord = words.get(i);
		String curTag = subTags.get(i);
		
		String preWord = "PW";
		String preTag = "PT";
		String pre2Word = "P2W";
		String pre2Tag = "P2T";
		String pre3Word = "P3W";
		String pre3Tag = "P3T";
		
		if(i>0) {			
			preWord = words.get(i-1);
			preTag = subTags.get(i-1);
			if(i>1) {
				pre2Word = words.get(i-2);
				pre2Tag = subTags.get(i-2);
			}if(i>2) {
				pre3Word = words.get(i-3);
				pre3Tag = subTags.get(i-3);
			}			
		}
		
		String nextWord = "NW";
		String nextTag = "NT";
		String next2Word = "N2W";
		String next2Tag = "N2T";
		String next3Word = "N3W";
		String next3Tag = "N3T";
		
		if(i < length-1) {			
			nextWord = words.get(i+1);
			nextTag = subTags.get(i+1);
			if(i < length-2) {
				next2Word = words.get(i+2);
				next2Tag = subTags.get(i+2);
			}if(i < length-3) {
				next3Word = words.get(i+3);
				next3Tag = subTags.get(i+3);
			}			
		}
		
		String[] curChars = Reader.getChars(curWord);
		int charLength = curChars.length;
		String prefix1 = "PF1";
		String prefix2 = "PF2";
		String prefix3 = "PF3";
		String suffix1 = "SF1";
		String suffix2 = "SF2";
		String suffix3 = "SF3";
		if(charLength > 0) {
			prefix1 = curChars[0];
			suffix1 = curChars[charLength-1];
			if(charLength > 1) {
				prefix2 = curChars[1];
				suffix2 = curChars[charLength-2];
				if(charLength > 2) {
					prefix3 = curChars[2];
					suffix3 = curChars[charLength-3];
				}
			}
		}
		
		features.add("1:"+pre3Word);
		features.add("2:"+pre2Word);
		features.add("3:"+preWord);
		features.add("4:"+curWord);
		features.add("5:"+nextWord);
		features.add("6:"+next2Word);
		features.add("7:"+next3Word);
		
		features.add("8:"+preTag);
		features.add("9:"+curTag);
		features.add("10:"+nextTag);
		
		features.add("11:"+pre3Word+"_"+pre2Word);
		features.add("12:"+pre2Word+"_"+preWord);
		features.add("13:"+preWord+"_"+curWord);
		features.add("14:"+curWord+"_"+nextWord);
		features.add("15:"+nextWord+"_"+next2Word);
		features.add("16:"+next2Word+"_"+next3Word);
		
		features.add("17:"+preTag+"_"+curTag);
		features.add("18:"+curTag+"_"+nextTag);

		features.add("19:"+preWord+"_"+nextWord);
		features.add("20:"+preTag+"_"+nextTag);
		
		features.add("21:"+prefix1);
		features.add("22:"+prefix2);
		features.add("23:"+prefix3);
		features.add("24:"+suffix1);
		features.add("25:"+suffix2);
		features.add("26:"+suffix3);
		
		return features;
	}
	
	
	/** 将svm-hmm的结果number文件转换为tag文件 
	 * 
	 * @since 2013-5-2
	 * @param numberTypeFile
	 * @param strTypeFile
	 * @throws IOException
	 */
	public void convertType(String numberTypeFile, String strTypeFile) throws IOException {
		
		HashMap<Integer, String> typeConvertMap = new HashMap<Integer, String>();
		Iterator<Entry<String, Integer>> iter = typeMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			typeConvertMap.put(entry.getValue(), entry.getKey());
		}
		
		FileWriter writer = new FileWriter(strTypeFile);
		
		String fileEncoding = FileEncoding.getCharset(numberTypeFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(numberTypeFile), fileEncoding));
		String line = null;
		int count = 0;
		while((line = reader.readLine()) != null) {
			count++;
			line = line.trim();
			if(line.length() > 0) {
				int number = Integer.parseInt(line);
				if(typeConvertMap.containsKey(number))
					writer.write(typeConvertMap.get(number)+"\n");
				else {
					writer.write("NONE\n");
					System.out.println(count);
				}
			}
			
			
		}
		reader.close();
		writer.close();		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String trainFileName = "/disk1/subWordSegPos/subWords/trainSubWordFile";
		String trainFeaFileName = "/disk1/subWordSegPos/subWords/trainSubWordFile-fea";
		String feaTypeMapFileName = "/disk1/subWordSegPos/subWords/trainSubWordFile-featureMap";
		GenerateFeatures genFea = new GenerateFeatures();
		/*
		genFea.generateFea(trainFileName, trainFeaFileName, true);
		genFea.saveMap(feaTypeMapFileName);
		
		String testFileName = "/disk1/subWordSegPos/subWords/devSubWordFile";
		String testFeaFileName = "/disk1/subWordSegPos/subWords/devSubWordFile-fea";
		genFea.loadMap(feaTypeMapFileName);
		genFea.generateFea(testFileName, testFeaFileName, false);
		*/
		
		String numberTypeFileName = "/disk1/subWordSegPos/subWords/devSubWordFile.tags";
		String strTypeFileName = "/disk1/subWordSegPos/subWords/devSubWordFile.strtags";
		genFea.loadTypeMap(feaTypeMapFileName);
		genFea.convertType(numberTypeFileName, strTypeFileName);
	}
	
	
	
	
	
	

}
