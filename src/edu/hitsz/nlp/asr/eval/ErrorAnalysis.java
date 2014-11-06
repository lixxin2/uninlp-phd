package edu.hitsz.nlp.asr.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.erroranalysis.One2Multiple;
import edu.hitsz.nlp.erroranalysis.One2MultipleMap;
import edu.hitsz.nlp.sentence.Distance;
import edu.hitsz.nlp.sentence.LevenshteinDistance;
import edu.hitsz.nlp.util.Array;

/**
 * 对结果进行错误分析，包括对于单字（音），词（对应的多音节）进行比较分析
 * @author Xinxin Li
 * @since Sep 21, 2013
 */
public class ErrorAnalysis {

	
	/**
	 * 评价标准拼音，与两种不同拼音结果的比较
	 * @since Sep 8, 2013
	 * @param goldFile
	 * @param predFile1
	 * @param predFile2
	 * @throws IOException
	 */
	public void syllableFile(String goldFile, 
			String predFile1, 
			String predFile2, 
			String outFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(goldFile);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), fileEncoding));
		fileEncoding = FileEncoding.getCharset(predFile1);
		BufferedReader predReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile1), fileEncoding));
		fileEncoding = FileEncoding.getCharset(predFile2);
		BufferedReader predReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile2), fileEncoding));

		String goldLine = null;
		String predLine1 = null;
		String predLine2 = null;
		
		One2MultipleMap map1 = new One2MultipleMap();
		One2MultipleMap map2 = new One2MultipleMap();
		
		int sentNum = 0;
		
		while((goldLine = goldReader.readLine()) != null && 
				(predLine1 = predReader1.readLine()) != null && 
				(predLine2 = predReader2.readLine()) != null ) {
			
			System.out.println(sentNum);
			if(sentNum == 109)
				System.out.println();
			
			goldLine = goldLine.trim();
			if(goldLine.length() > 0) {				
				String[] golds = goldLine.split("\\s+");
				String[] preds1 = predLine1.split("\\s+");
				String[] preds2 = predLine2.split("\\s+");				
				singleSent(map1, golds, preds1);
				singleSent(map2, golds, preds2);			
			}
			else
				break;	
			
			sentNum++;
		}		
		
		goldReader.close();
    	predReader1.close();
    	predReader2.close();

    	map1.finishAdd();
    	map2.finishAdd();
    	
    	//compare pred1 and pred2
    	int yinSize = map1.size();
    	if(yinSize != map2.size()) {
    		System.out.println("reading process make mistakes");
    		System.exit(-1);
    	}
    	
    	ArrayList<One2Multiple> aList = map2.sortByAll();
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).output();

    	FileWriter writer = new FileWriter(outFile);
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).outputFile(writer);
    	writer.close();
	}
	
	
	public void charFile(String goldFile, 
			String predFile1, 
			String predFile2, 
			String outFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(goldFile);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), fileEncoding));
		fileEncoding = FileEncoding.getCharset(predFile1);
		BufferedReader predReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile1), fileEncoding));
		fileEncoding = FileEncoding.getCharset(predFile2);
		BufferedReader predReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile2), fileEncoding));

		String goldLine = null;
		String predLine1 = null;
		String predLine2 = null;
		
		One2MultipleMap map1 = new One2MultipleMap();
		One2MultipleMap map2 = new One2MultipleMap();
		
		int sentNum = 0;
		
		while((goldLine = goldReader.readLine()) != null && 
				(predLine1 = predReader1.readLine()) != null && 
				(predLine2 = predReader2.readLine()) != null ) {
			
			System.out.println(sentNum);
			if(sentNum == 109)
				System.out.println();
			
			goldLine = goldLine.trim();
			if(goldLine.length() > 0) {				
				String[] golds = getChars(goldLine);
				String[] preds1 = getChars(predLine1);
				String[] preds2 = getChars(predLine2);			
				singleSent(map1, golds, preds1);
				singleSent(map2, golds, preds2);			
			}
			else
				break;	
			
			sentNum++;
		}		
		
		goldReader.close();
    	predReader1.close();
    	predReader2.close();

    	map1.finishAdd();
    	map2.finishAdd();
    	
    	//compare pred1 and pred2
    	int yinSize = map1.size();
    	if(yinSize != map2.size()) {
    		System.out.println("reading process make mistakes");
    		System.exit(-1);
    	}
    	
    	ArrayList<One2Multiple> aList = map2.sortByAll();
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).output();
    	 
    	FileWriter writer = new FileWriter(outFile);
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).outputFile(writer);
    	writer.close();
	}
	
	/**
	 * 将句子
	 * @since Sep 23, 2013
	 * @param sentence
	 * @return
	 */
	public String[] getChars(String sentence) {
		ArrayList<String> chars = new ArrayList<String>();
		for(int i=0; i<sentence.length(); i++) {
			String character = sentence.substring(i, i+1);
			if(character.length() != 0)
				chars.add(character);
		}
		return Array.toStringArray(chars);
	}
	
	
	/** 
	 * 每个拼音句子的统计信息，只比较单个音的情况 
	 * @since Sep 12, 2013
	 * @param map
	 * @param goldLine
	 * @param predLine
	 */
	private void singleSent(One2MultipleMap o2map, String[] golds, String[] preds) {
				
		LevenshteinDistance dist = new LevenshteinDistance();
		Distance[][] distances = dist.distances(golds, preds);
		int row = distances.length;
		int column = distances[0].length;				
		Distance distance = distances[row-1][column-1];
		ArrayList<Distance> distList = distance.getDistances();
		
		//找到识别正确的位置
		Distance[] distanceRow = new Distance[row];
		for(int i=distList.size()-1; i>0; i--) {
			distance = distList.get(i);
			distanceRow[distance.row] = distance;
		}
		
		//对于每个正确的读音
		for(int i=1; i<row; i++) {		
			distance = distanceRow[i];
			int columni = distanceRow[i].column;
			System.out.println(columni);
			String pred = "EMPTY";
			if(columni > 0)
				pred = preds[columni-1];
			o2map.put(golds[i-1], pred);
		}
	}
	
	
	/** 
	 * 比较拼音对的信息
	 * @since Sep 23, 2013
	 * @param goldFile
	 * @param predFile1
	 * @param predFile2
	 * @param outFile
	 * @throws IOException
	 */
	public void syllablePairFile(String goldFile, 
			String predFile1, 
			String predFile2, 
			String outFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(goldFile);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), fileEncoding));
		String fileEncoding1 = FileEncoding.getCharset(predFile1);
		BufferedReader predReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile1), fileEncoding1));
		String fileEncoding2 = FileEncoding.getCharset(predFile2);
		BufferedReader predReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile2), fileEncoding2));

		String goldLine = null;
		String predLine1 = null;
		String predLine2 = null;
		
		One2MultipleMap map1 = new One2MultipleMap();
		One2MultipleMap map2 = new One2MultipleMap();
		
		int sentNum = 0;
		while((goldLine = goldReader.readLine()) != null 
				&& (predLine1 = predReader1.readLine()) != null
				&& (predLine2 = predReader2.readLine()) != null) {
			
			System.out.println(sentNum);
			
			goldLine = goldLine.trim();
			if(goldLine.length() > 0) {
				String[] golds = goldLine.split("\\s+");
				String[] preds1 = predLine1.split("\\s+");
				String[] preds2 = predLine2.split("\\s+");
				pairSent(map1, golds, preds1);	
				pairSent(map2, golds, preds2);			
			}
			else
				break;
			
			sentNum++;
		}		
		
		goldReader.close();
    	predReader1.close();
    	predReader2.close();
    	    	
    	map1.finishAdd();
    	map2.finishAdd();
    	
    	//compare pred1 and pred2
    	int yinSize = map1.size();
    	if(yinSize != map2.size()) {
    		System.out.println("reading process make mistakes");
    		System.exit(-1);
    	}
    	
    	ArrayList<One2Multiple> aList = map2.sortByAll();
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).output();		
    	
    	FileWriter writer = new FileWriter(outFile);
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).outputFile(writer);
    	writer.close();
	}
	
	
	public void charPairFile(String goldFile, 
			String predFile1, 
			String predFile2, 
			String outFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(goldFile);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), fileEncoding));
		String fileEncoding1 = FileEncoding.getCharset(predFile1);
		BufferedReader predReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile1), fileEncoding1));
		String fileEncoding2 = FileEncoding.getCharset(predFile2);
		BufferedReader predReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(predFile2), fileEncoding2));

		String goldLine = null;
		String predLine1 = null;
		String predLine2 = null;
		
		One2MultipleMap map1 = new One2MultipleMap();
		One2MultipleMap map2 = new One2MultipleMap();
		
		int sentNum = 0;
		while((goldLine = goldReader.readLine()) != null 
				&& (predLine1 = predReader1.readLine()) != null
				&& (predLine2 = predReader2.readLine()) != null) {
			
			System.out.println(sentNum);
			
			goldLine = goldLine.trim();
			if(goldLine.length() > 0) {
				String[] golds = getChars(goldLine);
				String[] preds1 = getChars(predLine1);
				String[] preds2 = getChars(predLine2);	
				pairSent(map1, golds, preds1);	
				pairSent(map2, golds, preds2);			
			}
			else
				break;
			
			sentNum++;
		}		
		
		goldReader.close();
    	predReader1.close();
    	predReader2.close();
    	    	
    	map1.finishAdd();
    	map2.finishAdd();
    	
    	//compare pred1 and pred2
    	int yinSize = map1.size();
    	if(yinSize != map2.size()) {
    		System.out.println("reading process make mistakes");
    		System.exit(-1);
    	}
    	
    	ArrayList<One2Multiple> aList = map2.sortByAll();
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).output();		
    	
    	FileWriter writer = new FileWriter(outFile);
    	for(int i=0; i<aList.size(); i++)
    		aList.get(i).outputFile(writer);
    	writer.close();
	}
	
	
	private void pairSent(One2MultipleMap o2map,
			String[] goldYins, String[] predYins) {
		
		LevenshteinDistance dist = new LevenshteinDistance();
		Distance[][] distances = dist.distances(goldYins, predYins);
		int row = distances.length;
		int column = distances[0].length;				
		Distance distance = distances[row-1][column-1];
		ArrayList<Distance> distList = distance.getDistances();
		
		//找到识别正确的位置
		Distance[] distanceRow = new Distance[row];
		for(int i=distList.size()-1; i>0; i--) {
			distance = distList.get(i);
			distanceRow[distance.row] = distance;
		}
		
		//对于每个正确的拼音对，gold拼音序列的
		for(int i=1; i<row-1; i++) {
			String goldPair = goldYins[i-1] + "" + goldYins[i];
			String predPair = "EMPTY";
			int columni = distanceRow[i].column;
			int columnj = distanceRow[i+1].column;
			if(columni > 0 && columnj > 0) {
				predPair = "";
				for(int j=columni; j<columnj+1; j++)
					predPair += predYins[j-1] + "";
				predPair = predPair.trim();
			}
			o2map.put(goldPair, predPair);
		}		
	}
	
	
	
	
	
	
	public static void test() {
		
		String goldYin = "a b c d b";
		String predYin1 = "a c d f";
		String predYin2 = "a b c e";
		String[] golds = goldYin.split("\\s+");
		String[] preds1 = predYin1.split("\\s+");
		String[] preds2 = predYin2.split("\\s+");
		
		ErrorAnalysis analy = new ErrorAnalysis();
		One2MultipleMap singleMap = new One2MultipleMap();
		analy.singleSent(singleMap, golds, preds1);
		singleMap.finishAdd();
		singleMap.output();
		
		One2MultipleMap pairMap = new One2MultipleMap();
		analy.pairSent(pairMap, golds, preds1);
		pairMap.finishAdd();
		pairMap.output();
		
	}
	
	/** 
	 * 评价拼音 
	 * @throws IOException 
	 */
	public static void evalSyllable() throws IOException {

		ErrorAnalysis analy = new ErrorAnalysis();
		String goldFile = "/home/tm/disk/disk1/asr-rerank/error/1560.gold.pinyin";
		String predFile1 = "/home/tm/disk/disk1/asr-rerank/error/F01-06-00.1Best.yin";
		String predFile2 = "/home/tm/disk/disk1/asr-rerank/error/F01-06-00.mert.yin";
		String compFile = "/home/tm/disk/disk1/asr-rerank/error/syllableSingle.comp";
		analy.syllableFile(goldFile, predFile1, predFile2, compFile);
		String pairFile = "/home/tm/disk/disk1/asr-rerank/error/syllablePair.comp";
		analy.syllablePairFile(goldFile, predFile1, predFile2, pairFile);
	}
	
	/**
	 * 评价字的错误
	 * @since Sep 23, 2013
	 * @throws IOException
	 */
	public static void evalCharacter() throws IOException {

		ErrorAnalysis analy = new ErrorAnalysis();
		String goldFile = "/home/tm/disk/disk1/asr-rerank/error/1560.gold";
		String predFile1 = "/home/tm/disk/disk1/asr-rerank/error/F01-06-00-500Best.mert.result";
		String predFile2 = "/home/tm/disk/disk1/asr-rerank/error/F01-06-00-500Best.mert.result";
		String compFile = "/home/tm/disk/disk1/asr-rerank/error/charSingle.comp";
		analy.charFile(goldFile, predFile1, predFile2, compFile);
		String pairFile = "/home/tm/disk/disk1/asr-rerank/error/charPair.comp";
		analy.charPairFile(goldFile, predFile1, predFile2, pairFile);
	}
	
	
	public static void main(String[] args) throws IOException {
		
		//test();
		//evalSyllable();
		evalCharacter();
		
	}
}


