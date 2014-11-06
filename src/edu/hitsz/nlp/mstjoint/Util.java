package edu.hitsz.nlp.mstjoint;


/**
 * Utility methods that may be generally useful.
 *
 * @author     Jason Baldridge
 * @created    August 27, 2006
 */
public class Util {


	/**
	 * Assumes input is a String[] containing integers as strings.
	 * <p> 将数字型字符串转换为数字
	 * @param stringreps
	 * @return
	 */
    public static int[] stringsToInts(String[] stringreps) {
		int[] nums = new int[stringreps.length];
		for(int i = 0; i < stringreps.length; i++)
		    nums[i] = Integer.parseInt(stringreps[i]);
		return nums;
    }

    /**
     * 用一个字符连接字符串数组，返回一个字符串
     * @param a
     * @param sep
     * @return
     */
    public static String join (String[] a, char sep) {
		StringBuffer sb = new StringBuffer();
		sb.append(a[0]);
		for (int i=1; i<a.length; i++)
		    sb.append(sep).append(a[i]);
		return sb.toString();
    }

    /**
     * 用一个字符连接数字数组，返回一个字符串
     * @param a
     * @param sep
     * @return
     */
    public static String join (int[] a, char sep) {
		StringBuffer sb = new StringBuffer();
		sb.append(a[0]);
		for (int i=1; i<a.length; i++)
		    sb.append(sep).append(a[i]);
		return sb.toString();
    }


}
