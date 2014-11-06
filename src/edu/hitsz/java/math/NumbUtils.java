package edu.hitsz.java.math;

public class NumbUtils {

	
	/** 
	 * 去掉字符串 前面的0，例如"0001"为1
	 */ 
	public static int elimPrefixZeroInt(String s) {
		
		String tmp = elimPrefixZero(s);
		return Integer.parseInt(tmp);		
	}
	
	
	
	/** 
	 * 去掉字符串 前面的0，例如"0001"为1，“01.2”为“1.2”
	 * @since Aug 16, 2013
	 * @param s
	 * @return
	 */
	private static String elimPrefixZero(String s) {
	
		int length = s.length();
		//找到前一个不为0的位置
		int start = 0;
		for(; start < length; start++) 
			if(!s.substring(start, start).equals("0"))
				break;
		
		return start < length ? s.substring(start, length) : "";
	}
	
	
}
