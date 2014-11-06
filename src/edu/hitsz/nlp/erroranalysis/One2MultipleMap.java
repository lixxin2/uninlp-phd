package edu.hitsz.nlp.erroranalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class One2MultipleMap {
	
	HashMap<String, One2Multiple> map;
	
	public One2MultipleMap() {
		map = new HashMap<String, One2Multiple>(); 
	}
	
	public int size() {
		return map.size();
	}
	
	/**  
	 * 添加新实例
	 * @since Sep 23, 2013
	 * @param gold
	 * @param pred
	 */
	public void put(String gold, String pred) {
		if(map.containsKey(gold)) {
			One2Multiple oneMap = map.get(gold);
			oneMap.put(pred);
		}
		else {
			One2Multiple oneMap = new One2Multiple(gold, pred);
			map.put(gold, oneMap);
		}			
	}
	
	/** 
	 * 停止添加新的实例，并对每个实例都进行处理
	 * @since Sep 23, 2013
	 */
	public void finishAdd() {
		Iterator<Map.Entry<String, One2Multiple>> iter = map.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, One2Multiple> entry = iter.next();
			One2Multiple one  = entry.getValue();
			one.finishAdd();
		}
	}
	
	public void output() {
		Iterator<Map.Entry<String, One2Multiple>> iter = map.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, One2Multiple> entry = iter.next();
			One2Multiple one  = entry.getValue();
			one.output();
		}
	}
	
	/** 
	 * 根据每个实例的出现次数
	 * @since Sep 23, 2013
	 * @return
	 */
	public ArrayList<One2Multiple> sortByAll() {
		TreeMap<Integer, ArrayList<One2Multiple>> treeMap = new TreeMap<Integer, ArrayList<One2Multiple>>();
		//
		Iterator<Map.Entry<String, One2Multiple>> iter = map.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, One2Multiple> entry = iter.next();
			One2Multiple one  = entry.getValue();
			if(!treeMap.containsKey(one.all)) {
				ArrayList<One2Multiple> list = new ArrayList<One2Multiple>();
				list.add(one);
				treeMap.put(one.all, list);
			}
			else {
				ArrayList<One2Multiple> list = treeMap.get(one.all);
				list.add(one);
			}
				
		}
		
		ArrayList<One2Multiple> aList = new ArrayList<One2Multiple>();
		Iterator<Map.Entry<Integer, ArrayList<One2Multiple>>> iter2 = treeMap.entrySet().iterator();
		while(iter2.hasNext()) {
			Map.Entry<Integer, ArrayList<One2Multiple>> entry = iter2.next();
			ArrayList<One2Multiple> ones  = entry.getValue();
			aList.addAll(ones);
		}
		return aList;
	}
	
	
	/**
	 * 根据每个实例的准确率
	 * @since Sep 23, 2013
	 * @return
	 */
	public ArrayList<One2Multiple> sortByAcc() {
		TreeMap<Double, One2Multiple> treeMap = new TreeMap<Double, One2Multiple>();
		Iterator<Map.Entry<String, One2Multiple>> iter = map.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, One2Multiple> entry = iter.next();
			One2Multiple one  = entry.getValue();
			treeMap.put(one.acc, one);
		}
		
		ArrayList<One2Multiple> aList = new ArrayList<One2Multiple>();
		Iterator<Map.Entry<Double, One2Multiple>> iter2 = treeMap.entrySet().iterator();
		while(iter2.hasNext()) {
			Map.Entry<Double, One2Multiple> entry = iter2.next();
			One2Multiple one  = entry.getValue();
			aList.add(one);
		}
		return aList;
	}
	
	
	public static void main(String[] args) {
		
		String predYin1 = "a c d d c a a";
		String predYin2 = "a b d c e b b";
		String[] preds1 = predYin1.split("\\s+");
		String[] preds2 = predYin2.split("\\s+");
		
		One2MultipleMap map1 = new One2MultipleMap();
		for(int i = 0; i < preds1.length; i++)
			map1.put(preds1[i], preds2[i]);	
		map1.finishAdd();
		map1.output();
		
		One2MultipleMap map2 = new One2MultipleMap();
		for(int i = 0; i < preds1.length-1; i++)
			map2.put(preds1[i]+preds1[i+1], preds2[i]+preds2[i+1]);	
		map2.finishAdd();
		map2.output();
	}
	
	
	

}
