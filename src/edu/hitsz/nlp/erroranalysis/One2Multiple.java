package edu.hitsz.nlp.erroranalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/** 
 * 一个正确结果对应 多个识别结果， 分析每个结果的可能性
 * @author Xinxin Li
 * @since Sep 22, 2013
 */
public class One2Multiple {

	String gold;
	int all;
	int corr;
	double acc;
	int[] mics;
	HashMap<String, Integer> wrongHashMap;
	TreeMap<Integer, ArrayList<String>> wrongTreeMap;
	
	public One2Multiple() {
		all = 0;
		corr = 0;
		acc = 0.0;
		wrongHashMap = new HashMap<String, Integer>();
		wrongTreeMap = new TreeMap<Integer, ArrayList<String>>();
	}
	
	
	public One2Multiple(String gold, String pred) {
		this();
		this.gold = gold;
		put(pred);
	}
	
	/** 
	 * 放入 预测值（其中gold已经确定） 
	 * @since Sep 23, 2013
	 * @param pred
	 */
	public void put(String pred) {
		all++;
		if(gold.equals(pred))
			corr++;
		else {
			if(!wrongHashMap.containsKey(pred))
				wrongHashMap.put(pred, 1);
			else
				wrongHashMap.put(pred, wrongHashMap.get(pred)+1);
		}
	}
	
	/** 
	 * 最后统计，根据错误次数放入TreeMap
	 * @since Sep 23, 2013
	 */
	public void finishAdd() {
		
		acc = corr/(double)all;
		Iterator<Map.Entry<String, Integer>> iter = wrongHashMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			String pred = entry.getKey();
			int number = entry.getValue();
			if(!wrongTreeMap.containsKey(number)) {
				ArrayList<String> preds = new ArrayList<String>();
				preds.add(pred);
				wrongTreeMap.put(number, preds);
			}
			else {
				ArrayList<String> preds = wrongTreeMap.get(number);
				preds.add(pred);
			}
		}
	}
	
	/**  
	 * 输出到cmd
	 * @since Sep 23, 2013
	 */
	public void output() {	
		System.out.print(gold + "\t" + all + "\t" + corr + "\t" + acc + "\t");
		Iterator<Map.Entry<Integer, ArrayList<String>>> iter = wrongTreeMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, ArrayList<String>> entry = iter.next();
			int number = entry.getKey();
			ArrayList<String> strs = entry.getValue();
			System.out.print(number);
			for(String str : strs)
				System.out.print(":" + str);
			System.out.print("\t");
		}
		System.out.println();
	}
	
	/**
	 * 输出到FileWriter
	 * @since Sep 23, 2013
	 * @param writer
	 * @throws IOException
	 */
	public void outputFile(FileWriter writer) throws IOException {
		
		writer.write(gold + "\t" + all + "\t" + corr + "\t" + acc + "\t");
		Iterator<Map.Entry<Integer, ArrayList<String>>> iter = wrongTreeMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, ArrayList<String>> entry = iter.next();
			int number = entry.getKey();
			ArrayList<String> strs = entry.getValue();
			writer.write(number);
			for(String str : strs)
				writer.write(":" + str);
			writer.write("\t");
		}
		writer.write("\n");
	}
	
	
	
}
