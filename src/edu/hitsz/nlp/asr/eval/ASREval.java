package edu.hitsz.nlp.asr.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.language.english.Utility;
import edu.hitsz.nlp.sentence.Distance;
import edu.hitsz.nlp.sentence.LevenshteinDistance;
import edu.hitsz.nlp.util.Array;



/**
 * 评测语音识别的准确率
 * @author Xinxin Li
 * @since Aug 23, 2012
 */
public class ASREval {
	
	int gold = 0;
	int predict = 0;
	int correct = 0;
	int insert = 0;
	int delete = 0;
	int replace = 0;

	
	/**
	 * 更新评价结果
	 * @since Aug 13, 2012
	 * @param s1 predict 字或词的list
	 * @param s2 gold
	 */
	public void updateEval(ArrayList<String> s1, ArrayList<String> s2) {
		
		LevenshteinDistance med = new LevenshteinDistance();
		String[] ss1 = Array.toStringArray(s1);
		String[] ss2 = Array.toStringArray(s2);
		
		Distance dist = med.distance(ss1, ss2);

		gold += s1.size();
		predict += s2.size();
		correct += dist.correct;
		insert += dist.insertion;
		delete += dist.deletion;
		replace += dist.substitution;	
	}
	
	
	/**
	 * 根据音素序列获得字的注音序列,julius的输出结果
	 * @since Aug 13, 2012
	 * @param s
	 * @return
	 */
	public ArrayList<String> getZiYinsJulius(String s){
		String[] phone = s.split("[ \\|\t]");
		ArrayList<String> phoneList = new ArrayList<String>();
		for(String tmp: phone) {
			if(tmp.length() > 0)
				phoneList.add(tmp.trim());
		}		
		ArrayList<String> phones = new ArrayList<String>();
		if(phoneList.size() % 2 !=0) {
			System.out.println("phonome is wrong");
			System.exit(-1);
		}
		for(int i=0; i<phoneList.size(); i+=2) {
			phones.add(phoneList.get(i)+" "+phoneList.get(i+1));
		}
		return phones;
	}
	
	/**
	 * 根据音素序列获得词的注音序列,julius的输出结果
	 * @since Aug 13, 2012
	 * @param s
	 * @return
	 */
	public ArrayList<String> getWordYinsJulius(String s){
		String[] phone = s.split("\\|");
		ArrayList<String> phoneList = new ArrayList<String>();
		for(String tmp: phone) {
			if(tmp.length() > 0)
				phoneList.add(tmp.trim());
		}		
		ArrayList<String> phones = new ArrayList<String>();

		for(int i=0; i<phoneList.size(); i++) {
			phones.add(phoneList.get(i));
		}
		return phones;
	}
	
	
	/**
	 * 根据语句获得词序列,julius的输出结果
	 * @since Aug 13, 2012
	 * @param s 句子序列，词之间用空格或tab分割
	 * @return
	 */
	public ArrayList<String> getWords(String s){
		String[] word = s.split("[ \t]");
		ArrayList<String> words = new ArrayList<String>();
		for(int i=0; i<word.length; i++) {
			if(word[i].trim().length() > 0)
				words.add(word[i].trim());
		}
		return words;
	}
	
	
	/**
	 * 获得词对应的拼音序列
	 * @since Oct 3, 2013
	 * @param s
	 * @return
	 */
	public ArrayList<String> getWordYins(String s){
		
		String delim = "\t";
		if(!s.contains("\t") && s.contains(" "))
			delim = " ";		
		
		String[] wordYins = s.split(delim);
		ArrayList<String> wordYinList = new ArrayList<String>();
		for(int i=0; i<wordYins.length; i++) {
			if(wordYins[i].trim().length() > 0)
				wordYinList.add(wordYins[i].trim());
		}
		return wordYinList;
	}
	
	
	/**
	 * 根据语句获得字序列,julius的输出结果
	 * @since Aug 13, 2012
	 * @param s
	 * @return
	 */
	public ArrayList<String> getZis(String s){
		
		String[] word = s.split("\\s+");
		ArrayList<String> zis = new ArrayList<String>();
		//每个词中的每个字
		for(int i=0; i<word.length; i++) {
			String tmpWord = word[i].trim();
			if(tmpWord.length() > 0) {
				for(int j=0; j < tmpWord.length(); j++)
					zis.add(tmpWord.substring(j, j+1));
			}				
		}
		return zis;
	}
	
	/** 
	 * 获得拼音序列
	 * @since Oct 3, 2013
	 * @param s
	 * @return
	 */
	public ArrayList<String> getZiYins(String s){
		
		String[] yin = s.split("\\s+");
		ArrayList<String> yinList = new ArrayList<String>();
		//每个读音
		for(int i=0; i<yin.length; i++) {
			if(yin[i].trim().length() > 0)
				yinList.add(yin[i].trim());
		}
		return yinList;
	}
	

	
	
	/** 
	 * 评估语音识别文件，863数据集，julius的识别结果
	 * @since Aug 13, 2012
	 * @param goldFileName 带有句子标识的标准句子，比如“f01A1 上海的工人师傅克服困难”
	 * @param predictFileName
	 * @throws IOException
	 */
	public static void eval863Zi(String goldFileName, String predictFileName) throws IOException {
        
		//读取标准句子，用句子标识来索引句子
		String encoding = FileEncoding.getCharset(goldFileName);
        BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), encoding));
        HashMap<String, String> goldMap = new HashMap<String, String>();
        String line = "";
        while((line = goldReader.readLine()) != null) {
        	String[] parts = line.split(" ");
        	if(parts.length==2)
        		goldMap.put(Utility.upper2lower(parts[0].trim()).substring(3), parts[1].trim());
        	//else
        	//	goldMap.put(Utility.B2S(parts[0].trim()).substring(3), "");
        }
        goldReader.close();
		
        //读取julius识别结果，同样用句子标识来索引句子
		encoding = FileEncoding.getCharset(predictFileName);
        BufferedReader predictReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), encoding));
        HashMap<String, String> predictMap = new HashMap<String, String>();        
        while((line = predictReader.readLine()) != null) {
        	String[] parts = line.split("\t");
        	if(parts.length == 2)
        		predictMap.put(Utility.upper2lower(parts[0].trim()).substring(3), parts[1].trim());
        	else if(parts.length == 1)
        		predictMap.put(Utility.upper2lower(parts[0].trim()).substring(3), "");
        }
        predictReader.close();

        //对于每个预测的句子，计算其准确率
        ASREval eval = new ASREval();
        Iterator<Entry<String,String>> iter = predictMap.entrySet().iterator();
        while(iter.hasNext()) {
        	Entry<String, String> entry = iter.next();
        	String fileSeq = entry.getKey();
        	String predictLine = entry.getValue();
        	if(goldMap.containsKey(fileSeq)) {
        		String goldLine = goldMap.get(fileSeq);
        		ArrayList<String> goldZiList = eval.getZis(goldLine);
        		ArrayList<String> predictZiList = eval.getZis(predictLine);
        		eval.updateEval(goldZiList, predictZiList);
        	}
        	else {
        		System.out.println("no gold line for " + fileSeq);
        		System.exit(-1);
        	}
        	
        }
        eval.output();
		
	}
	
	
	/**
	 * 评估结果，字错误率
	 * @since Sep 21, 2013
	 * @param goldFile
	 * @param resultFile
	 * @throws IOException
	 */
	public static void evalZi(String goldFile, String resultFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(goldFile);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), fileEncoding));
	
		fileEncoding = FileEncoding.getCharset(resultFile);
		BufferedReader resultReader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), fileEncoding));

		ASREval eval = new ASREval();
		String goldLine = null;
		String resultLine = null;
		while((goldLine = goldReader.readLine()) != null
				&& (resultLine = resultReader.readLine()) != null) {			
			ArrayList<String> goldChars = eval.getZis(goldLine);
			ArrayList<String> resultChars = eval.getZis(resultLine);
			eval.updateEval(goldChars, resultChars);
			
		}
		
		eval.output();
		goldReader.close();
		resultReader.close();
	}
	
	
	
	public static void evalZiYin(String goldFile, String resultFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(goldFile);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFile), fileEncoding));
	
		fileEncoding = FileEncoding.getCharset(resultFile);
		BufferedReader resultReader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), fileEncoding));

		ASREval eval = new ASREval();
		String goldLine = null;
		String resultLine = null;
		while((goldLine = goldReader.readLine()) != null
				&& (resultLine = resultReader.readLine()) != null) {
			
			ArrayList<String> goldZiYins = eval.getZiYins(goldLine);
			ArrayList<String> resultZiYins = eval.getZiYins(resultLine);
			eval.updateEval(goldZiYins, resultZiYins);
		}
		eval.output();
		goldReader.close();
		resultReader.close();
	}
	
	
	public void output() {
		System.out.println(
				"gold: " + gold + "\t" + "predict: " + predict + "\t" + 
				"correct: "+ correct + "\n" + "insert: " + insert + "\t" + 
				"delete: " + delete + "\t" + "replace: " + replace);
		System.out.println("correct/gold: " + correct/(double)gold);//
		System.out.println("character error rate = (gold-insert-delete-replace)/gold = " + 
				String.valueOf((gold-insert-delete-replace)/(double)gold));		
	}
	
	public double getErr() {
		return (insert+delete+replace)/(double)gold;
	}
	
	
	public static void innerTest() {
		
		String goldWords = " 李 开 港 漫步 吉 奥 ";
		String predictWords = "李 开 香 漫不 奥 ";
		String goldYins = "| l i | k ai | g ang | m an b u | j i | _a ao |";
		String predictYins = "| l i | k an | g ang | m an b an | j iu | _a ao |";
		
		
		ASREval eval = new ASREval();
		ArrayList<String> goldYinList = eval.getZiYinsJulius(goldYins);
		ArrayList<String> predictYinList = eval.getZiYinsJulius(predictYins);
		eval.updateEval(goldYinList, predictYinList);
		eval.output();
		
		//eval.WordYinEval(goldYins, predictYins);
		//eval.output();
		//eval.WordEval(goldWords, predictWords);
		//eval.output();
		//eval.ZiEval(goldWords, predictWords);
		//eval.output();		
	}
		
	
	
	public static void main(String[] args) throws IOException {			
		
		ASREval eval = new ASREval();
		//ASREval.eval863Zi("/home/tm/windows/asr/eval/speechtext", "/home/tm/windows/asr/eval/result.pdgm.bigvocab.3f2");		
		ASREval.evalZi("/home/tm/disk/disk1/asr-rerank/mert/result/1560.gold", 
				//"/home/tm/disk/disk1/asr-rerank/mert/result/F01-06-00-500Best-by-F02-07-11-03-08-12-04-09-13-500Best.mert.result-10");
	            "/home/tm/disk/disk1/asr-rerank/perceptron/F01-06-00-500Best.result-1-1-4");
		
		//ASREval.evalZiYin("/home/tm/disk/disk1/asr-rerank/eval/1560.gold.pinyin", "/home/tm/disk/disk1/asr-rerank/error/F01-06-00.1Best.yin");
	
	}
	
	
	
}
