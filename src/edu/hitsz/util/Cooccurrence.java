package edu.hitsz.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cooccurrence implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -750036375637876649L;
	
	private HashMap<String, Integer> first;
	private int firstSize;
	private HashMap<String, Integer> second;
	private int secondSize;
	private HashMap<String, Integer> pair;
	private int pairSize;
	private int allSize;
	
	public Cooccurrence() {
		first = new HashMap<String, Integer>();
		firstSize = 0;
		second = new HashMap<String, Integer>();
		secondSize  = 0;
		pair = new HashMap<String, Integer>();
		pairSize = 0;
		allSize = 0;
	}
	
	public String getPair(String firstItem, String secondItem) {
		return firstItem + "&$&" + secondItem;
	}
	
	public void add(String firstItem, String secondItem) {
		if(!first.containsKey(firstItem))
			first.put(firstItem, 1);
		else
			first.put(firstItem, first.get(firstItem) + 1);
		
		if(!second.containsKey(secondItem))
			second.put(secondItem, 1);
		else
			second.put(secondItem, second.get(secondItem) + 1);
		
		String pairItem = getPair(firstItem, secondItem);
		if(!pair.containsKey(pairItem))
			pair.put(pairItem, 1);
		else
			pair.put(pairItem, pair.get(pairItem) + 1);
	}
	
	public void finishAdd() {
		firstSize = first.size();
		Iterator<Map.Entry<String, Integer>> iter = first.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			allSize += entry.getValue();
		}
		secondSize = second.size();
		pairSize = pair.size();
	}
	
	public int getNumber(String item, HashMap<String, Integer> map) {
		if(map.containsKey(item))
			return map.get(item);
		else
			return 0;
	}
	
	/**
	 * second/first
	 * @since Aug 14, 2014
	 * @param firstItem
	 * @param secondItem
	 * @return
	 */
	public double getFirstSecondOccurrence(String firstItem, String secondItem) {
		int firstNumber = getNumber(firstItem, first);
		int pairNumber = getNumber(getPair(firstItem, secondItem), pair);
		if(firstNumber == 0 || pairNumber == 0)
			return 0.0;
		else 
			return pairNumber/(double)firstNumber;
	}
	
	/**
	 * first/second
	 * @since Aug 14, 2014
	 * @param firstItem
	 * @param secondItem
	 * @return
	 */
	public double getSecondfirstOccurrence(String firstItem, String secondItem) {
		int secondNumber = getNumber(secondItem, second);
		int pairNumber = getNumber(getPair(firstItem, secondItem), pair);
		if(secondNumber == 0 || pairNumber == 0)
			return 0.0;
		else 
			return pairNumber/(double)secondNumber;
	}
	
	
	

}
