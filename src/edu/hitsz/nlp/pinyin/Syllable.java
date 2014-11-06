package edu.hitsz.nlp.pinyin;

/**
 * 音节
 * @author Xinxin Li
 * @since Nov 9, 2012
 */
public class Syllable {
		
	private String syllable;
	private String initial;
	private String finall;
	private String tone;
	
	
	/**
	 * 
	 * @since Nov 9, 2012
	 * @param yin a4, ai1
	 */
	public Syllable(String yin) {
		int length = yin.length();
		char tmpTone = yin.charAt(length-1);
		if(tmpTone >= '0' && tmpTone <= '9') {
			syllable = yin.substring(0,length-1).toLowerCase();
			tone = yin.substring(length-1);
		}
		else {
			syllable = yin.toLowerCase();
			tone = "5";
		}
	}	
	
	public String get(boolean withTone) {
		if(withTone)
			return syllable + tone;
		else
			return syllable;
	}
	
	@Override
	public int hashCode() {
		return syllable.hashCode();
	}	
	

}
