package edu.hitsz.nlp.asr.rerank;

import edu.hitsz.nlp.pinyin2character.PyCharInstance;
import edu.hitsz.nlp.pinyin2character.PyCharItem;

/**
 * Julius结果的每个候选项，包括{@link PyCharInstance}，声学模型，语言模型和结合后给该候选项的赋值
 * @author Xinxin Li
 * @since Sep 21, 2013
 */
public class JuliusResultInstance extends PyCharInstance{

	private static final long serialVersionUID = 1L;
	
	public double totalWeight;
	public double amWeight;
	public double lmWeight;
	
	public JuliusResultInstance() {
		super();
	}
	
	/** 
	 * 生成一个空实例，内容为"空"("kong")
	 * @since Sep 21, 2013
	 * @return
	 */
	public static JuliusResultInstance emptyInstance() {
		
		JuliusResultInstance instance = new JuliusResultInstance();
		
		String[] emptyWords = new String[1]; 
		emptyWords[0] = "空";
		instance.words = emptyWords;
		instance.characters = emptyWords;
		
		String[] emptyYins = new String[1];
		emptyYins[0] = "kong";
		instance.wordYins = emptyYins;
		instance.characterYins = emptyYins;
		
		return instance;		
	}
	
	/**
	 * 从给定的结果中抽取出实例
	 * @since Sep 21, 2013
	 * @param sentencen
	 * @param wseqn
	 * @param phseqn
	 * @param cmscoren
	 * @param scoren
	 * @param sylMap
	 * @return
	 */
	public static JuliusResultInstance getInstance(final String sentencen, final String wseqn,
			final String phseqn, final String cmscoren, final String scoren, final SyllableMap sylMap) {
		
		JuliusResultInstance instance = new JuliusResultInstance();
		int characterLength = instance.extractWords(sentencen);
		instance.extractPinyins(phseqn, characterLength, sylMap);
		instance.extractWeight(scoren);	
		if(instance.words == null || instance.words.length == 0 ||
				instance.characters == null || instance.characters.length == 0 ||
				instance.wordYins == null || instance.wordYins.length == 0 ||
				instance.characterYins == null || instance.characterYins.length == 0)
			instance = null;
		
		return instance;		
	}
	
	
	/** 
	 * 从句子中抽取其中的词 
	 * @since Sep 21, 2013
	 * @param sentencen
	 * @return
	 */
	public int extractWords(String sentencen) {
		int start = sentencen.indexOf("<s>");
		int end = sentencen.indexOf("</s>");
		if(start+3 > end) {
			words = null;
			characters = null;
			return -1;
		}
		
		String sentence = sentencen.substring(start+3, end);
		if(sentence.trim().length() == 0) {
			words = null;
			characters = null;
			return 0;
		}			
		
		words = sentence.trim().split(" ");	
		
		int length = 0;
		for(String word : words)
			length += word.length();
		characters = new String[length];
		length =0;
		for(String word : words) {
			for(int i=0; i<word.length(); i++) {
				characters[length] = word.substring(i,i+1);
				length++;
			}
		}
		return length;
	}
	
	/** 
	 * 抽取拼音
	 *  <p> phseq1: sil | sh ang h ai | d e | g ong r en | sh i2 f u | k e f u | k un n an | sil 
	 * @since Sep 21, 2013
	 * @param phseqn
	 * @param characterLength
	 * @param sylMap
	 * @return
	 */
	public boolean extractPinyins(String phseqn, int characterLength, SyllableMap sylMap) {
		//解析
		int start = phseqn.indexOf("sil |");
		int end = phseqn.indexOf("| sil");
		if(start+5 >= end) {			
			wordYins = null;
			characterYins = null;
			return false;
		}
		
		String sentence = phseqn.substring(start+5, end);
		if(sentence.trim().length() == 0) {
			wordYins = null;
			characterYins = null;
			return false;
		}
		
		//把词分开
		String[] origWordYins = sentence.trim().split("\\|");		
		int wordSize = origWordYins.length;
		wordYins = new String[wordSize];
		characterYins = new String[characterLength];
		int charSize =0;
		
		//对于每个词音
		for(int i = 0; i < wordSize; i++) {
			String origWordYin = origWordYins[i];
			String wordYin = "";
			String[] charSubYins = origWordYin.trim().split("\\s+");
			int charSubYinLength = charSubYins.length;
			for(int s = 0; s < charSubYinLength; s += 2) {
				String characterYin = charSubYins[s] + " " +charSubYins[s+1];
				//characterYin = getCharacterYin(charSubYins[s], charSubYins[s+1], characters[length]);
				if(!sylMap.parts2Syllable.containsKey(characterYin)) {
					System.out.println(characterYin + "\t" + characters[charSize]);
					System.exit(-1);
				}					
				characterYins[charSize] = sylMap.parts2Syllable.get(characterYin);
				wordYin += characterYins[charSize] + " ";
				charSize++;
			}
			wordYins[i] = wordYin.trim();
		}
		
		if(words.length != wordYins.length && charSize != characterLength) {
			System.out.println("words and pinyins length are different");
			System.exit(-1);
		}

		return true;
	}
	
	
	/**
	 * 抽取出声学模型和语言模型的权重
	 * @since Sep 21, 2013
	 * @param scoren
	 */
	public void extractWeight(String scoren) {
		int start = scoren.indexOf(":");
		int mid1 = scoren.indexOf("(AM:");
		int mid2 = scoren.indexOf("LM:");
		totalWeight = Double.parseDouble(scoren.substring(start+1, mid1).trim());
		amWeight = Double.parseDouble(scoren.substring(mid1+4, mid2).trim());
		lmWeight = Double.parseDouble(scoren.substring(mid2+3, scoren.length()-1).trim());		
	}
	
	
	
	@Deprecated
	/**
	 * 人工设定的转换规则，后由于
	 * @since Sep 21, 2013
	 * @param pre
	 * @param end
	 * @param character
	 * @return
	 */
	public String getCharacterYin(String pre, String end, String character) {
		
		String newEnd = "";
		
		if(pre.equals("_a")) {
			if (end.equals("a") || end.equals("ai") || end.equals("an") || end.equals("ang") || end.equals("ao"))
				return end;
			else 
				System.out.println(pre+"\t"+end+"\t"+character);
		}
		else if(pre.equals("_e")) {
			if(end.equals("e") || end.equals("en") || end.equals("er"))
				return end;
			else
				System.out.println(pre+"\t"+end+"\t"+character);
		}
		else if(pre.equals("_i")) {
			if(end.length() == 1)
				return "yi";
			else if(end.equals("iu"))
				return "you";
			else if(end.equals("in") || end.equals("ing"))
				return "y"+end;
			else if(end.substring(1, 2).matches("[aeiou]"))
				return "y"+end.substring(1);
			else
				System.out.println(pre+"\t"+end+"\t"+character);
		}
		else if(pre.equals("_o")) {
			if(character.equals("区"))
				return "qu";
			else if(end.equals("ou"))
				return end;
			else
				System.out.println(pre+"\t"+end+"\t"+character);
		}
		else if(pre.equals("_u")) {
			if(end.equals("u") || end.equals("eng"))
				return "wu";
			else if(end.equals("uo") || end.equals("uang") || end.equals("uai") 
					|| end.equals("uan") || end.equals("ua"))
				return "w" + end.substring(1);
			else if(end.equals("ui") || end.equals("un"))
				return "we" + end.substring(1);
			else
				System.out.println(pre+"\t"+end+"\t"+character);
		}
		else if(pre.equals("_v")) {
			if(end.equals("v") )
				return "yu";
			else if(end.equals("van") || end.equals("ve") || end.equals("vn"))
				return "yu"+end.substring(1);
			else
				System.out.println(pre+"\t"+end+"\t"+character);
		}
		
		return pre+end;
	}
	
	
}
