package edu.hitsz.nlp.util;

import java.util.ArrayList;

public class Array {


	/**
	 * 判断当前字符串是否位于数组中
	 * @since Jan 9, 2012
	 * @param one
	 * @param array
	 * @return
	 */
	public static boolean isIn(String one, String[] array){
		for(int i=0; i<array.length; i++){
			if (one.equals(array[i]))
				return true;
		}
		return false;
	}

	public static boolean isAllIn(String one, String[] array){
		for(int i=0; i<one.length(); i++){
			if(!isIn(one.substring(i, i+1), array))
				return false;
		}
		return true;
	}

	public static int[] toArray(ArrayList<Integer> lists){
		int length = lists.size();
		int[] ints = new int[length];
		for(int i=0; i<length; i++)
			ints[i] = lists.get(i).intValue();
		return ints;
	}
	
	public static int[] toArray(Integer[] lists){
		int length = lists.length;
		int[] ints = new int[length];
		for(int i=0; i<length; i++)
			ints[i] = lists[i].intValue();
		return ints;
	}
	
	public static String[] toStringArray(ArrayList<String> lists){
		int length = lists.size();
		String[] strings = new String[length];
		for(int i=0; i<length; i++)
			strings[i] = lists.get(i);
		return strings;
	}
	
	public static String[] toStringArrayReverse(ArrayList<String> lists){
		int length = lists.size();
		String[] strings = new String[length];
		for(int i=length-1; i>-1; i--)
			strings[i] = lists.get(i);
		return strings;
	}

	/**
	 * 连接字符串数组中从i到j之间的元素
	 * @since 2012-3-1
	 * @param character
	 * @param i
	 * @param j
	 * @return
	 */
	public static String toWord(String[] character, int i, int j){
		StringBuffer newbf = new StringBuffer();
		for(int k=i; k<j; k++){
			newbf.append(character[k]);
		}
		return newbf.toString();
	}

	public static String toPinyin(String[] character, int i, int j){
		StringBuffer newbf = new StringBuffer();
		newbf.append(character[i]);
		for(int k=i+1; k<j; k++){
			newbf.append(" " + character[k]);
		}
		return newbf.toString();
	}
	
	public static ArrayList<String> toArrayList(String[] vec){
		ArrayList<String> ints = new ArrayList<String>();
		for(String i : vec)
			ints.add(i);
		return ints;
	}


}
