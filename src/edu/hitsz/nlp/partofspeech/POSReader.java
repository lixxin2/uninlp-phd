package edu.hitsz.nlp.partofspeech;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.corpus.ptb.PTB;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.util.Array;

public class POSReader {

	protected BufferedReader inputReader;
	private String inputFileType;
	private boolean trainReverse = false;
	private boolean devReverse = false;
	private boolean normalized = true;

    /**
     * 设置文件读取器
     * @param file
     * @return
     * @throws IOException
     */
    public void startReading (String file, POSOptions options) throws IOException {
		inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		inputFileType = options.inputFileType;
		trainReverse = options.trainReverse;
		devReverse = options.devReverse;
    }

    /** 读取文件
     *  <p> column
     */   
    public POSInstance getNext() throws IOException {
    	return getNextColumn();
    }
    
    /**
     * 获取下一个实例，每行一个词
     * <li> 
     */
    public POSInstance getNextColumn() throws IOException {

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
    public POSInstance getNextRaw( ) throws IOException {
    	if(inputFileType.equals("row"))
    		return getNextRawRow();
    	return getNextRawColumn();
    }
    
    
    /** 按行读取原始文件，一行一个字，字可以是中文，也可以是英文单词 */
    public POSInstance getNextRawColumn( ) throws IOException {

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
    public POSInstance getNextRawRow( ) throws IOException {

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
    public POSInstance newInstance(String[] words, String[] tags) {
		
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
		
		return new POSInstance(newWords, newTags);
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
    


        
    
    /**
     * 反向字符串数组
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
    		rs[length-1-i] = word;
    	}
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
    	
    	POSReader reader = new POSReader();
    	String[] s = {"中国","人民","银行","1986","年","在","Beijing","成立"};
    	//String[] s1 = reader.reverseArray(s);
    	//String[] s2 = reader.reverseWords(s);
    	System.out.println();    	
    }




}

