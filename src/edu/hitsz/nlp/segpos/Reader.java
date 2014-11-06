package edu.hitsz.nlp.segpos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.corpus.ptb.PTB;
import edu.hitsz.nlp.util.Array;

public class Reader {

	protected BufferedReader inputReader;
	private String inputFileType;
	private String charFeatureType;
	private boolean trainReverse = false;
	private boolean devReverse = false;
	private boolean normalized = true;

    /**
     * 设置文件读取器
     * @param file
     * @return
     * @throws IOException
     */
    public void startReading (String file, Options options) throws IOException {
		inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		inputFileType = options.inputFileType;
		charFeatureType = options.charFeatureType;
		trainReverse = options.trainReverse;
		devReverse = options.devReverse;
    }

    /** 读取文件
     *  <p> column
     */   
    public Instance getNext() throws IOException {
    	return getNextColumn();
    }
        
    /**
     * 获取下一个实例，每行一个词
     * <li> 
     */
    public Instance getNextColumn() throws IOException {

		ArrayList<String[]> lineList = new ArrayList<String[]>();
		String line = inputReader.readLine();
		while (line != null && !line.trim().equals("")) {
			String[] subs = line.trim().split("\\s+");
		    line = inputReader.readLine();
			if(subs.length < 1) {
				continue;
			}
		    lineList.add(subs);
		    //System.out.println("## "+line);
		}
		int wordSize = lineList.size();
		if(wordSize == 0) {
		    inputReader.close();
		    return null;
		}
		//分配词和词性
		String[] words = new String[wordSize];
		String[] poses = new String[wordSize];	
		for(int i = 0; i < wordSize; i++) {
		    String[] info = lineList.get(i);
		    words[i] = info[0];
		    if(info.length > 1)
		    	poses[i] = info[1];
		}
		
		return newInstance(words, poses);
    }
    
    
    
    /** 读取空白实例，只有字符串输入*/
    public Instance getNextRaw( ) throws IOException {
    	if(inputFileType.equals("row"))
    		return getNextRawRow();
    	return getNextRawColumn();
    }
    
    
    /** 按行读取原始文件，一行一个字，字可以是中文，也可以是英文单词 */
    public Instance getNextRawColumn( ) throws IOException {

		ArrayList<String[]> lineList = new ArrayList<String[]>();

		String line = inputReader.readLine();
		while (line != null && !line.equals("")) {
		    lineList.add(line.split("\\s+"));
		    line = inputReader.readLine();
		    //System.out.println("## "+line);
		}

		int wordSize = lineList.size();

		if(wordSize == 0) {
		    inputReader.close();
		    return null;
		}
		//赋值words
		String[] words = new String[wordSize];
		for(int i=0; i < wordSize; i++){
			String[] info = lineList.get(i);
		    words[i] = info[0];
		}
		
		return newInstance(words, null);
    }
    
    
    /** 原始文件按行读取*/
    public Instance getNextRawRow( ) throws IOException {

		String line = inputReader.readLine();
		if (line != null && line.trim().length() > 0) {
		    			
		    //System.out.println("## "+line);
			line = line.trim();
		    String[] words = line.split("\\s+");
		    
		    return newInstance(words, null);
		    			
		}
		else {
			inputReader.close();
			return null;
		}

    }


    /** 新实例 */
    public Instance newInstance(String[] words, String[] tags) {
		
    	int length = words.length;
    	String[] newWords = new String[length];    	
    	String[] newTags = null;
    	if(tags != null && tags.length >0 && tags[0] != null)
    		newTags = new String[length];
    	
    	for(int i=0; i<length; i++) {
    		newWords[i] = words[i];
    		if(normalized)
    			newWords[i] = Normalizer.normalize(words[i], Normalizer.Form.NFKC);//words[i];//
    		if(tags != null && tags[0] != null)
    			newTags[i] = tags[i];
    	}
    	
		//如果需要反向字符串
		if(trainReverse || devReverse) {
			newWords = reverseWords(newWords);
    		if(tags != null)
    			newTags = reverseArray(newTags);
		}		
		
		String[] chars = null;
		String[] charPos = null;
		//创建字符和字符标示
		if(tags != null && tags[0] != null) {
			String[][] charposes = getCharPOS(newWords, newTags);
			chars = charposes[0];
			charPos = charposes[1];
		}
		else {
			chars = getChars(newWords);
		}
		
		//		
		if(charFeatureType.equals("nglow")) {
			String[] puncts = isPunc(chars);
			String[] cs = ngLowClasses(chars);
			return new Instance(chars, charPos, newWords, newTags, puncts, cs);
		}

		return new Instance(chars, charPos, newWords, newTags);
    }

    /** 如果是数字或者字母*/
    //public static boolean isAlphaNumeric(String s){
   // 	if(s.matches("[a-zA-Z\\.]+") || s.matches("[0-9]+|[0-9]+[\\.][0-9]+|[0-9,]+[0-9]+")) {
    //		return true;
    //	}    	
    //	return false;
    //}
    
    
    public static String strInverse(String s) {
    	StringBuffer bf = new StringBuffer(s);
    	return bf.reverse().toString();
    }
    
	
    /** 获取所有字符是否是符号*/
    public static String[] isPunc(String[] characters) {
    	HashMap<String, Integer> puncts = new HashMap<String, Integer>();
    	for(String t : CTB.punctuations) {
			puncts.put(t, 1);
		}
    	for(String t : PTB.punctuations)
    		puncts.put(t, 1);
    	int length = characters.length;
    	if(length > 0) {
	    	String[] isPunc = new String[characters.length];
	    	for(int i=0; i<characters.length; i++) {
	    		if(puncts.containsKey(characters[i]))
	    			isPunc[i] = "1";
	    		else
	    			isPunc[i] = "0";
	    	}
	    	return isPunc;
    	}
    	return null;
    	
    }
    
    /** 得到所有字符的类型 Ng and Low 2004 */
    public static String[] ngLowClasses(String[] characters) {
    	//numbers
    	HashMap<String, Integer> numbers = new HashMap<String, Integer>();
    	for(String t : CTB.arabNumbers) {
			numbers.put(t, 1);
		}
    	for(String t : CTB.hanNumbers) {
			numbers.put(t, 1);
		}
    	//dates
    	HashMap<String, Integer> dates = new HashMap<String, Integer>();
    	for(String t : CTB.dates) {
			dates.put(t, 1);
		}
    	//English letters
    	HashMap<String, Integer> letters = new HashMap<String, Integer>();
    	for(String t : CTB.letters) {
			letters.put(t, 1);
		}    	
    	//
    	int length = characters.length;
    	if(length > 0) {
	    	String[] classes = new String[characters.length];
	    	for(int i=0; i<length; i++) {
		    	for(String s : characters) {
		    		if(numbers.containsKey(s) || s.matches("[0-9]+(\\.[0-9]+)?"))
		    			classes[i] = "1";
		    		else if(dates.containsKey(s))
		    			classes[i] = "2";
		    		else if(letters.containsKey(s) || s.matches("[a-zA-Z\\.]*"))
		    			classes[i] = "3";
		    		else
		    			classes[i] = "4";    			
		    	}
	    	}
	    	return classes;
    	}
    	return null;
    	
    }
    
    /** 得到每个字的序列，注意英文的分开方式 */
    public static String[] getChars(String[] words) {
    	
    	int wordSize = words.length;  
		ArrayList<String> chars = new ArrayList<String>();
		for(int i=0; i < wordSize; i++){
			String[] wordChars = getChars(words[i]);
			for(String wordChar : wordChars) 
				chars.add(wordChar);
		}
    			
		return Array.toStringArray(chars);
    }
    
    /** 
     * 通过判断每个字是否是中文字，获得词中包含的所有字
     * @since Feb 23, 2013
     * @param word
     * @return
     */
    public static String[] getChars(String word) {
    	
    	return getCharsEntire(word);
    	/*
    	int allLength = word.length();
    	ArrayList<Integer> charIndex = new ArrayList<Integer>();
    	int type = 0; //0:中文；1:英文；2：数字
    	for(int i=0; i<allLength; i++) {
    		int newType = 0;
    		String curChar = word.substring(i, i+1);
    		if(ChineseWord.isHanzi(curChar))
    			newType = 0;
    		else if(curChar.matches("[a-zA-Z]+"))
    			newType = 1;
    		else if(curChar.matches("[0-9]+"))
    			newType = 2;
    		else
    			newType = 3;
    		if(newType != type || newType == 0 || newType == 3) { 
    				charIndex.add(i);
    				type = newType;
    		}
    	}
    	if(!charIndex.contains(0))
    		charIndex.add(0, 0);
    	charIndex.add(allLength);
    	//
    	int charSize = charIndex.size()-1;
    	String[] chars = new String[charSize];
    	for(int i=0; i<charSize; i++)
    		chars[i] = word.substring(charIndex.get(i), charIndex.get(i+1));
    	
		return chars;
		*/
    }
    
    /**
     * 获得词中的所有字符，包括中文和英文，不包括空格符等
     * @since 2013-5-2
     * @param word
     * @return
     */
    public static String[] getCharsEntire(String word) {
    	int allLength = word.length();
    	String[] chars = new String[allLength];     	
    	for(int i=0; i<allLength; i++) {
    		String curChar = word.substring(i,i+1);
    		if(!curChar.matches("\\s+"))
    			chars[i] = word.substring(i, i+1);
    	}    	
		return chars;
    }
    
    
    
    /**
     * 根据词和词性获得字符和字符标示
     * @since Aug 27, 2012
     * @param words
     * @param poses
     * @return
     */
    public static String[][] getCharPOS(String[] words, String[] poses) {
    	String[][] charposes = new String[2][];
    	int wordSize = words.length;
    	  
		ArrayList<String> chars = new ArrayList<String>();
		ArrayList<String> charPos = new ArrayList<String>();
		for(int i=0; i < wordSize; i++){
			String[] wordChars = getChars(words[i]);
			for(String wordChar : wordChars) 
				chars.add(wordChar);
			String[] wordCharPoses = Word2Char.generateTag(wordChars.length, poses[i]);
			for(String wordCharPos : wordCharPoses) 
				charPos.add(wordCharPos);			
		}
    	if(chars.size() != charPos.size()) {
    		System.out.println("different between chars and charposes");
    		System.exit(-1);
    	}
		
		charposes[0] = Array.toStringArray(chars);
		charposes[1] = Array.toStringArray(charPos);
			
		return charposes;
				
    }
        
    
    /**
     * 反向字符串数组,并把每个字符串的字符也都翻转
     * @since Sep 6, 2012
     * @param s
     * @return
     */
    public static String[] reverseWords(String[] s) {
    
    	if(s == null || s.length == 0)
    		return new String[0];
    	
    	int length = s.length;
    	String[] rs = new String[length];
    	for(int i=0; i<length; i++) {
    		String word = s[i];
    		rs[length-1-i] = reverseWord(word);
    	}
    	return rs;	
    	
    }
    
    /** 翻转单个词 */
    public static String reverseWord(String s) {
    	
    	String rs;
    	String word = s;
		
		StringBuffer sb = new StringBuffer();	
		int wordLength = word.length();
		for(int j=0; j<wordLength; j++)
			sb.append(word.substring(wordLength-1-j, wordLength-j));   		
		rs = sb.toString();
		
		return rs;
    }
    
    
    /**
     * 反向字符串数组
     * @since Sep 6, 2012
     * @param s
     * @return
     */
    public static String[] reverseArray(String[] s) {
        
    	if(s == null || s.length == 0)
    		return new String[0];
    	
    	int length = s.length;
    	String[] rs = new String[length];
    	for(int i=0; i<length; i++) {    		
    		rs[length-1-i] = s[i];
    	}
    	return rs;	
    	
    }
   
    
    public static void main(String[] args) {
    	
    	Reader reader = new Reader();
    	String[] s = {"中国","人民","银行","1986","年","在","Beijing","成立"};
    	String[] s1 = reader.reverseArray(s);
    	String[] s2 = reader.reverseWords(s);
    	String[] chars = reader.getChars(s);
    	String[] chars2 = reader.getChars(s2);
    	System.out.println();
    	
    }




}
