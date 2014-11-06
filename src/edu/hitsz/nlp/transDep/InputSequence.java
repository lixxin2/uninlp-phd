package edu.hitsz.nlp.transDep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.hitsz.algorithm.hash.HashCodeUtil;
import edu.hitsz.nlp.util.Array;
import edu.hitsz.nlp.segpos.Reader;

/**
 * 输入序列 
 * @author Xinxin Li
 * @since Feb 25, 2013
 */
public class InputSequence implements Serializable{
		
	private static final long serialVersionUID = 1L;
	
	public String sentence;
	public String[] words;
	public String[] postags;
	public int wordSize;
	
	//only use for character parser
	public String[] chars;
	public int[] isWords; //与chars相同，通常为-1；只是在每个词的位置对应与词的
	public int charSize;
	public boolean isChar;
	
	//
	public int[] heads;
	public String[] deprels;	
	
	/** 
	 * 创造新
	 * @since Sep 4, 2012
	 * @param words
	 * @param postags
	 * @param heads
	 * @param deprels
	 * @param isChar 是否需要字符处理
	 * @param deleteRoot 是否要删除root
	 * @return
	 */
	public static InputSequence getInputSequence(String[] words, 
			String[] postags, 
			int[] heads,
			String[] deprels,
			String decodingMethod,
			boolean deleteRoot) {
		
		assert(words != null);
		int length = words.length;
		if(deleteRoot)
			length -= 1;
		
		String[] newWords = new String[length];
		System.arraycopy(words, words.length-length, newWords, 0, length);
		
		String[] newPostags = new String[length];
		if(postags != null) 
			System.arraycopy(postags, words.length-length, newPostags, 0, length);
		
		int[] newHeads = new int[length];
		if(heads != null) 
			System.arraycopy(heads, words.length-length, newHeads, 0, length);
		
		String[] newDeprels = new String[length];
		if(deprels != null)
			System.arraycopy(deprels, words.length-length, newDeprels, 0, length);
		
		boolean isChar = false;
		if(decodingMethod.contains("char"))
			isChar = true;
		return new InputSequence(newWords, newPostags, newHeads, newDeprels, isChar);	
		
	}	
	
	
	/**
	 * 新实例，头结点-1
	 * @since Jun 7, 2013
	 * @param sequence
	 * @return
	 */
	public static InputSequence getRawInputSequence(InputSequence sequence) {
		
		int length = sequence.wordSize;
		int[] newHeads = new int[length];
		for(int i=0; i<length; i++)
			newHeads[i] = -2;
		InputSequence newSequence = new InputSequence(sequence.words, sequence.postags, newHeads, null, sequence.isChar);
		return newSequence;		
	}
	
	
	
	public static InputSequence getCharInputSequence(String sequence) {
		return new InputSequence(sequence);
	}
	
	

	public InputSequence(String[] words, String[] postags, int[] heads, String[] deprels, boolean isChar) {
		this.words = words;
		this.postags = postags;
		wordSize = words.length ;
		this.heads = heads;
		this.deprels = deprels;
		//
		this.isChar = isChar;
		if(isChar) {
			ArrayList<String> charList = new ArrayList<String>();
			ArrayList<Integer> isWordsList = new ArrayList<Integer>();
			charList.add(words[0]);
			isWordsList.add(0);
			for(int i=1; i<wordSize; i++) {
				String[] oneChars = Reader.getChars(words[i]);
				for(int j=0; j<oneChars.length; j++) {
					charList.add(oneChars[j]);
					if(j == 0)
						isWordsList.add(i);
					else
						isWordsList.add(-1);
				}
			}
			chars = Array.toStringArray(charList);
			charSize = chars.length;
			isWords = new int[charSize];
			for(int i=0; i<charSize; i++)
				isWords[i] = isWordsList.get(i);
		}
	}
	
	
	/** 根据句子生成所有的词 */
	public InputSequence(String sequence) {
		chars = Reader.getChars(sequence);
		charSize = chars.length;	
	}
		
	
	
	/**
	 * 判断当前字位置是否是词的首字
	 * @since Mar 10, 2013
	 * @param charSeq
	 * @return
	 */
	public boolean isWord(int charSeq) {
		if(isWords[charSeq] != -1)
			return true;
		else
			return false;
	}
	
	@Override
	public String toString() {
		if(wordSize > 0) {
			StringBuffer strbuf = new StringBuffer();
			for(int i=0; i<wordSize; i++) {
				if(words[i] != null)
					strbuf.append(words[i]);
				if(postags[i] != null)
					strbuf.append("/"+postags[i]+" ");
			}
			return strbuf.toString().trim();
		}
		else if(sentence != null)
			return sentence;
		else
			return "";
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof InputSequence)) return false;
		InputSequence sequence = (InputSequence) obj;
		if(!sequence.sentence.equals(sentence)) return false;
		if(!Arrays.equals(sequence.words, words)) return false;
		if(!Arrays.equals(sequence.postags, postags)) return false;
		if(sequence.wordSize != wordSize) return false;
		if(!Arrays.equals(chars, chars)) return false;
		if(!Arrays.equals(isWords, isWords)) return false;
		if(sequence.charSize != charSize) return false;
		if(!Arrays.equals(heads, heads)) return false;
		if(!Arrays.equals(deprels, deprels)) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int hashCode = HashCodeUtil.SEED;
	    hashCode = HashCodeUtil.hash(hashCode, sentence);
	    hashCode = HashCodeUtil.hash(hashCode, words);
	    hashCode = HashCodeUtil.hash(hashCode, postags);
	    hashCode = HashCodeUtil.hash(hashCode, wordSize);
	    hashCode = HashCodeUtil.hash(hashCode, chars);
	    hashCode = HashCodeUtil.hash(hashCode, isWords);
	    hashCode = HashCodeUtil.hash(hashCode, charSize);
	    hashCode = HashCodeUtil.hash(hashCode, heads);
	    hashCode = HashCodeUtil.hash(hashCode, deprels);
        return hashCode;		
	}
	
}
