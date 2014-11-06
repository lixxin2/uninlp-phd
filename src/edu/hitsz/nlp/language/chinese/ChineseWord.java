package edu.hitsz.nlp.language.chinese;

/**
 * 匹配中文，英文字母和数字
 * @author Xinxin Li
 * @since Jul 31, 2012
 */
public class ChineseWord {

	
	public static String hanziRegex = "[\u3007\u4e00-\u9fa5]";
	public static String hanzisRegex= "[\u3007\u4e00-\u9fa5]+";
	
	/**
	 * 
	 * @since Aug 14, 2012
	 * @param word
	 * @return
	 */
	public static boolean isHanzi(String word) {		
		if(word.matches(hanzisRegex))
			return true;
		else
			return false;
	}
	
	/**
	 * delete the characters which are not Chinese characters
	 * @since Nov 15, 2013
	 * @param sequence
	 * @return
	 */
	public static String delNoChinese(String sequence) {
		return sequence.replaceAll("[^\u3007\u4e00-\u9fa5]", "");
	}
	
	public static void main(String[] args) {
		System.out.println(ChineseWord.isHanzi("5；6"));
		String a = "1中国，测试[a]明白";
		System.out.println(ChineseWord.delNoChinese(a));
	}
	
	
}
