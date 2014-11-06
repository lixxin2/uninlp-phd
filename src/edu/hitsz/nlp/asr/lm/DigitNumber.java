package edu.hitsz.nlp.asr.lm;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将阿拉伯数字转换为汉语数字
 * 
 * @author Xinxin Li
 * @since Jul 30, 2012
 */
public class DigitNumber {

	/** */
	public String numberRegex = "-?[０１２３４５６７８９0-9]+([\\.．·][０１２３４５６７８９0-9]+)?[%％]?(/／[０１２３４５６７８９0-9]+)?";
	/** */
	private String digitRegex = "[０１２３４５６７８９0-9]+";
	/** */
	private String traditionalNumberRegex = "[〇零壹贰兩叁肆伍陆柒捌玖拾佰仟万亿兆点萬億點]+";
	/** */
	private String standardNumberRegex = "[零一二两三四五六七八九十百千万亿兆点年]+";

	private String segSignal = "";
	
	
	public DigitNumber(String str) {
		segSignal = str;
	}
	
	/**
	 * 处理带有数字的词，程序主入口
	 * @since Jul 30, 2012
	 * @param word
	 * @param preWord
	 * @param nextWord
	 * @return
	 */
	public String getWord(String word, String preWord, String nextWord) {
		
		
		if(!word.matches(numberRegex)) {
			Pattern pattern=Pattern.compile(numberRegex);
			Matcher m=pattern.matcher(word);		
			//使用find()方法查找第一个匹配的对象 
			boolean result = m.find(); 
			//使用循环将句子里所有的kelvin找出并替换再将内容加到sb里 
			if(result) { 		
				StringBuffer before = new StringBuffer();
				StringBuffer end = new StringBuffer();
				m.appendReplacement( before, ""); 
				////最后调用appendTail()方法将最后一次匹配后的剩余字符串加到sb里； 
				m.appendTail(end);
				String beforeWord = before.toString();
				String endWord = end.toString();
				
				word = word.substring(beforeWord.length(), word.length()-endWord.length());
				
				if(beforeWord.length() > 0) {
					if(endWord.length() > 0)
						return beforeWord + getWord(word, beforeWord, endWord) + endWord;
					else
						return beforeWord + getWord(word, beforeWord, nextWord);
				}
				else {
					if(endWord.length() > 0)
						return getWord(word, preWord, endWord) + endWord;
					else {
						//System.out.println("word: " + word);
						return getWord(word, preWord, nextWord);
					}
				}
			} 
		}
		
		//匹配中文
		if(word.matches(standardNumberRegex))
			return addSegSignal(word);
		
		//匹配传统中文
		if(word.matches(traditionalNumberRegex))
			return traditionalWord(word);
		
		//如果不匹配		
		if(!word.matches(numberRegex))
			return word;
		
		//如果是负数
		if(word.startsWith("-") && word.length()>1) {
			return "负" + segSignal + getWord(word.substring(1), preWord, nextWord);
		}
		
		//如果是百分数
		if((word.endsWith("%") || word.endsWith("％")) && word.length()>1) {
			return "百分之" + segSignal + getWord(word.substring(0,word.length()-1), preWord, nextWord);					
		}
		
		
		//如果是小数，带小数点
		if(word.contains(".") || word.contains("．") ||word.contains("·")) {
			String[] subs = word.split("[\\.．·]");
			String s = "";
			if(subs.length > 0 && subs[0].length() > 0) {
				subs[0] = sequence(subs[0], true);
				s += subs[0];
			}
			if(subs.length > 1 && subs[1].length() > 0) {
				subs[1] = direct(subs[1], true);
				s += segSignal + "点" + segSignal + subs[1];
			}
			return s;
		}
		
		//如果是分数
		if(word.contains("/") || word.contains("／")) {
			String[] subs = word.split("[/／]");
			String s = "";
			if(subs.length > 0 && subs[0].length() > 0) {
				subs[0] = sequence(subs[0], true);
				s += subs[0];
			}
			if(subs.length > 1 && subs[1].length() > 0) {
				subs[1] = sequence(subs[1], true);
				s = subs[1] + segSignal + "分之" + segSignal + s;
			}
			return s;
		}
		
		if(word.length() > 17)
			direct(word, true);
		
		//如果前面词是
		if( (preWord.equals("公元") || preWord.equals("公元前")) && word.length() <=4 )
			return sequence(word, true);
		
		//如果是年结尾直接读	
		if(nextWord.equals("年") )
			return direct(word, false);
		
		//如果数字小于8位
		if(word.length() < 7)
			return sequence(word, false);
		
		return direct(word, true);		
		
		
	}
	
	public String getWord(ArrayList<String> words, int i) {
		int length = words.size();
		String cur = "NONE";
		String pre = "NONE";
		String next = "NONE";
		
		if(i>=0 && i<length)
			cur = words.get(i);
		if(i-1>=0 && i-1<length)
			pre = words.get(i-1);
		if(i+1>=0 && i+1<length)
			next = words.get(i+1);
		return getWord(cur, pre, next);
		
	}
	

	/**
	 * 大写数字：伍拾壹万零三佰
	 * @since Jul 31, 2012
	 * @param word
	 * @return
	 */
	public String traditionalWord(String word) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<word.length(); i++) {
			String s = singleDigit(word.substring(i,i+1), true);
			sb.append(s);
		}		
		return addSegSignal(sb.toString());
	}
	
	
	
	
	
	/**
	 * 读时直接读出每个数目字，如“1997年”读作“一九九七年”。
	 * @since Jul 30, 2012
	 * @param digit
	 * @param isSeg 是否分开
	 * @return
	 */
	public String direct(String digit, boolean isSeg) {
		
		if(digit.matches(digitRegex)) {
			StringBuffer sb = new StringBuffer();
			int length = digit.length();
			for(int i=0; i<length; i++) {
				sb.append(singleDigit(digit.substring(i,i+1), true));
			}
			digit = sb.toString();
		}
		
		if(isSeg)
			return addSegSignal(digit);
		else
			return digit;
	}
	
	
	/**
	 * 读时可以将年序数字按基数整个读出，如 “公元1990年”，也可以读作“公元一千九百九十年”。
	 * @since Jul 30, 2012
	 * @param digit
	 * @param erLiang true:er false:liang
	 * @return
	 */
	public String sequence(String digit, boolean erLiang) {
		
		String s = "";
		
		if(digit.matches(digitRegex)) {
			StringBuffer sb = new StringBuffer();
			int length = digit.length();
			if(length > 16) {
				return direct(digit, true);
			}
			if(length > 12) {
				s += sequenceQian(digit.substring(0, length-12), erLiang) + "兆" + segSignal + sequence(digit.substring(length-12), erLiang);
			}
			else if(length > 8) {
				s += sequenceQian(digit.substring(0, length-8), erLiang) + "亿" + segSignal + sequence(digit.substring(length-8), erLiang);				
			}
			else if(length > 4) {
				s += sequenceQian(digit.substring(0, length-4), erLiang) + "万" + segSignal + sequence(digit.substring(length-4), erLiang);				
			}
			else {
				s += sequenceQian(digit, erLiang);
			}
		}
		
		if(s.length() == 0)
			s = "零";
		else
			s = s.replaceAll("[零]+", "零");
		
		return s;
	
	}
	
	
	/**
	 * 在词的字符之间插入分割符
	 * @since Aug 20, 2012
	 * @param s
	 * @return
	 */
	public String addSegSignal(String s) {
		StringBuffer sb = new StringBuffer();
		if(s.length() > 0) {
			for(int i=0; i<s.length(); i++) {
				String str = s.substring(i, i+1);
				if(str.trim().length() != 0)
					sb.append(segSignal + str);
			}				
		}
		return sb.toString().trim();
	}
	
	
	/** 
	 * 读一万以内的数字，按基数整个读出，如 “公元1990年”，也可以读作“公元一千九百九十年”。
	 * @since Jul 30, 2012
	 * @param digit
	 * @param erLiang true:er false:liang
	 * @return
	 */
	public String sequenceQian(String digit, boolean erLiang) {
		
		String tmp = digit;
		//数字
		String[] digits = new String[4];
		//单位：千" ，"百" ， "十"
		String[] fours = new String[4];
		int length = digit.length();
		//
		StringBuffer sb = new StringBuffer();
		//如果只有一位数字
		if(length == 1)
			sb.append(singleDigit(digit,erLiang));	
		//如果两位数
		else if(length == 2) {
			String ten = singleDigit(digit.substring(0, 1), true);
			String one = singleDigit(digit.substring(1, 2), true);
			if(!ten.equals("一"))
				sb.append(ten);
			sb.append("十"); 
			sb.append(one);
		}			
		//多位数字
		else for(int i=0; i<length; i++) {
			int j = length-i;
			if(j==4) {
				digits[i] = singleDigit(digit.substring(i,i+1), false);
			}
			else {
				digits[i] = singleDigit(digit.substring(i,i+1), true);
			}
			
			//千百十
			fours[i] = j==4 ? "千" : (j==3 ? "百" : (j==2 ? "十" : ""));
			if(digits[i].equals("零"))
				sb.append(digits[i] + segSignal);		
			else
				sb.append(digits[i]+fours[i]+ segSignal);			
		}
		length = sb.length();
		tmp = sb.toString();
		tmp = tmp.replaceAll("(零 ?)+", "零");
		
		if(tmp.length() > 0 && tmp.endsWith("零"))
			tmp = tmp.substring(0, tmp.length()-1);			
		return tmp;	
		
	}
	
	
	/**
	 * 单个数字的转换
	 * @since Jul 30, 2012
	 * @param digit
	 * @param direct 如果为true，则二；false为两
	 * @return
	 */
	public String singleDigit(String digit, boolean direct) {
		if(digit.equals("0") || digit.equals("０")|| digit.equals("〇")) 
			return "零";
		else if(digit.equals("1") || digit.equals("１")|| digit.equals("壹")) 
			return "一";
		else if(digit.equals("2") || digit.equals("２")|| digit.equals("贰")) {
			if(direct)
				return "二";
			else
				return "两";
		}			
		else if(digit.equals("3") || digit.equals("３")|| digit.equals("叁")) 
			return "三";
		else if(digit.equals("4") || digit.equals("４")|| digit.equals("肆")) 
			return "四";
		else if(digit.equals("5") || digit.equals("５")|| digit.equals("伍")) 
			return "五";
		else if(digit.equals("6") || digit.equals("６")|| digit.equals("陆")) 
			return "六";
		else if(digit.equals("7") || digit.equals("７")|| digit.equals("柒")) 
			return "七";
		else if(digit.equals("8") || digit.equals("８")|| digit.equals("捌")) 
			return "八";
		else if(digit.equals("9") || digit.equals("９")|| digit.equals("玖")) 
			return "九";
		else if(digit.equals("拾")) 
			return "十";
		else if(digit.equals("佰")) 
			return "百";
		else if(digit.equals("仟")) 
			return "千";
		else if(digit.equals("萬"))
			return "万";
		else if(digit.equals("億"))
			return "亿";
		else if(digit.equals("點"))
			return "点";
		else if(digit.equals("兩"))
			return "两";
		
		return digit;
			
	}
	
	
	public void test() {		

		DigitNumber dn = new DigitNumber(" ");	
		
		System.out.println(dn.getWord("15年", "公元", "世纪"));

		System.out.println(dn.getWord("98%", "公元", "年"));

		System.out.println(dn.getWord("-55", "", ""));
	}
	

	
	public static void main(String[] args) {
		DigitNumber dn = new DigitNumber(" ");
		dn.test();
	}
	
	
	
	
	
}
