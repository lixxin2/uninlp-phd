package edu.hitsz.nlp.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.hitsz.nlp.language.english.Utility;

public class NGramEntropy {
	
	private NgramProb ngram;
	private boolean bParam = false;
	public int n = 1; //ngram length
	public String fileType = "conll"; //conll, row
	public int column = 0;
	public int secondColumn = -1;
	public BufferedReader reader;
	
	public NGramEntropy() {
		ngram = new NgramProb();
	}
	
	public void setParameter(int n, String fileType, int column) {
		this.n = n;
		this.fileType = fileType;
		this.column = column;	
		this.bParam = true;
	}
	
	public void setParameter(int n, String fileType, int column, int secondColumn) {
		this.setParameter(n, fileType, column);
		this.secondColumn = secondColumn;
	}
	
	public double calcAllLetterEntropy(String inFileName) throws IOException {
		assert(!bParam);
		//stat letter freq
		startReading(inFileName);		
		ArrayList<String> words = getWords().get(0);
		int count = 0;
		while(words != null) {
			if(words.size() > 0) {
				ArrayList<String> letters = Utility.words2letters(words);
				ArrayList<String> lowerLetters = new ArrayList<String>();
				for(String letter : letters)
					lowerLetters.add(letter.toLowerCase());
				for(int i=0; i<letters.size(); i++) {
					List<String> prevs = null;
					if(i > 0 && n > 1) {
						prevs = lowerLetters.subList(Math.max(i-n+1, 0), i);
					}
					ngram.addNgram(prevs, n-1, lowerLetters.get(i));					
				}
			}
			System.out.println(count + ",");
			count += 1;
			words = getWords().get(0);
		}		
		assert(ngram.checkNumber());
		//calc entropy
		return ngram.calcEntropy();
	}
	
	/** 词里面的character ngram */
	public double calcWordLetterEntropy(String inFileName) throws IOException {
		assert(!bParam);
		//stat letter freq
		startReading(inFileName);		
		ArrayList<String> words = getWords().get(0);
		int count = 0;
		while(words != null) {
			if(words.size() > 0) {
				for(int j=0; j<words.size(); j++) {
					ArrayList<String> letters = Utility.word2letters(words.get(j));
					ArrayList<String> lowerLetters = new ArrayList<String>();
					for(String letter : letters)
						lowerLetters.add(letter.toLowerCase());
					for(int i=0; i<letters.size(); i++) {
						List<String> prevs = null;
						if(i > 0 && n > 1) {
							prevs = lowerLetters.subList(Math.max(i-n+1, 0), i);
						}
						ngram.addNgram(prevs, n-1, lowerLetters.get(i));					
					}
				}
			}
			System.out.println(count + ",");
			count += 1;
			words = getWords().get(0);
		}		
		//calc entropy
		assert(ngram.checkNumber());
		return ngram.calcEntropy();
	}
	
	
	public void calcWordEntropy(String inFileName) throws IOException {
		assert(!bParam);
		//stat letter freq
		startReading(inFileName);		
		ArrayList<ArrayList<String>> words = getWords();
		int count = 0;
		while(words != null) {
			if(words.size() > 0) {
				for(int i=0; i<words.get(0).size(); i++) {
					List<String> firstPrevs = null;
					List<String> secondPrevs = null;
					if(i > 0 && n > 1) {
						firstPrevs = words.get(0).subList(Math.max(i-n+1, 0), i);
						if(secondColumn > 0)
							secondPrevs = words.get(1).subList(Math.max(i-n+1, 0), i);
					}
					if(secondColumn > 0)				
						ngram.addNgram(firstPrevs, secondPrevs, n-1, words.get(0).get(i), words.get(1).get(i));
					else
						ngram.addNgram(firstPrevs, n-1, words.get(0).get(i));
				}
			}
			System.out.println(count + ",");
			count += 1;
			words = getWords();
		}		
		//calc entropy
		assert(ngram.checkNumber());
		double entropy =  ngram.calcEntropy();
		System.out.println(entropy);
		if(secondColumn > 0) {
			double conditionalEntropy = ngram.calcConditionalEntropy();
			System.out.println(conditionalEntropy);
		}
	}
	
	

	public void startReading(String inFileName) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(inFileName));
	}
	
	
	public ArrayList<ArrayList<String>> getWords() throws IOException {
		
		if(fileType.equals("row")) {
			String line = reader.readLine();
			if(line != null) {
				line = line.trim();
				if(line.length() == 0)
					return getWords();
				else {
					String[] words = line.split("[ \t]");
					ArrayList<ArrayList<String>> wordLists = new ArrayList<ArrayList<String>>();
					ArrayList<String> wordList = new ArrayList<String>();
					for(String word : words)
						wordList.add(word);
					wordLists.add(wordList);
					return wordLists;
				}
			}
			else {
				reader.close();
				return null;
			}
		}
		else if(fileType.equals("conll")){
			ArrayList<ArrayList<String>> wordLists = new ArrayList<ArrayList<String>>();
			String line = reader.readLine();
			int count = 0;
			while(line != null) {
				line = line.trim();
				if(line.length() == 0)
					return wordLists;
				else {
					String[] words = line.split("[ \t]");
					if(count == 0) {
						wordLists.add(new ArrayList<String>());
						if(secondColumn > 0)
							wordLists.add(new ArrayList<String>());
					}
					String word = words[column];
					wordLists.get(0).add(word);
					if(secondColumn > 0) {
						word = words[secondColumn];
						wordLists.get(1).add(word);
					}
				}
				line = reader.readLine();
			}
			if(line == null) {
				reader.close();
				return null;
			}
		}
		return null;
	}
	
	
	public static void main(String[] args) throws IOException {
		String inWordFileName = "/home/tm/disk/disk1/entropy/train_char.segpos.need";
		//String outWordFileName = "/home/tm/disk/disk1/entropy/brownptb.entropy";
		NGramEntropy entropy = new NGramEntropy();
		entropy.setParameter(1, "conll", 2, 2);
		entropy.calcWordEntropy(inWordFileName);
	}
	
}


class NgramProb{
	public HashMap<String, Integer> prevWords;
	public HashMap<String, Integer> entireWords;
	public int entireNumber;
	public HashMap<String, Integer> firstPrevWords;
	public HashMap<String, Integer> firstEntireWords;
	public int firstEntireNumber;
	public HashMap<String, Integer> secondPrevWords;
	public HashMap<String, Integer> secondEntireWords;
	public int secondEntireNumber;
	
	public NgramProb() {
		prevWords = new HashMap<String, Integer>();
		entireWords = new HashMap<String, Integer>(); 
		entireNumber = 0;
		firstPrevWords = new HashMap<String, Integer>();
		firstEntireWords = new HashMap<String, Integer>(); 
		firstEntireNumber = 0;
		secondPrevWords = new HashMap<String, Integer>();
		secondEntireWords = new HashMap<String, Integer>(); 
		secondEntireNumber = 0;
	}
	
	public void addNgram(String prev, String cur, HashMap<String, Integer> prevWords, HashMap<String, Integer> entireWords) {
		String entire = prev + "|||" + cur;
		if(prevWords.containsKey(prev)) 
			prevWords.put(prev, prevWords.get(prev)+1);
		else 
			prevWords.put(prev, 1);
		if(entireWords.containsKey(entire))
			entireWords.put(entire, entireWords.get(entire)+1);
		else
			entireWords.put(entire, 1);
	}
	
	public void addNgram(List<String> prevs, int prevNumber, String cur) {
		StringBuffer strbuf = new StringBuffer();
		String prev = "EMPTY";
		strbuf.append(prev);
		int length = prevs == null ? 0 : prevs.size();
		for(int i=0; i<prevNumber-length; i++)
			strbuf.append("&&&START" );
		if(prevs != null && prevs.size() > 0) {
			for(String p : prevs)
				strbuf.append("&&&" + p);
		}
		prev = strbuf.toString();
		addNgram(prev, cur, prevWords, entireWords);		
		entireNumber += 1;
	}
	
	public void addNgram(List<String> firstPrevs, List<String> secondPrevs, int prevNumber, 
			String firstCur, String secondCur) {
		//
		StringBuffer firstStrbuf = new StringBuffer();
		String firstPrev = "EMPTY";
		firstStrbuf.append(firstPrev);
		int firstLength = firstPrevs == null ? 0 : firstPrevs.size();
		for(int i=0; i<prevNumber-firstLength; i++)
			firstStrbuf.append("&&&START" );
		if(firstPrevs != null && firstPrevs.size() > 0) {
			for(String p : firstPrevs)
				firstStrbuf.append("&&&" + p);
		}
		firstPrev = firstStrbuf.toString();
		addNgram(firstPrev, firstCur, firstPrevWords, firstEntireWords);	
		firstEntireNumber += 1;
		//second
		StringBuffer secondStrbuf = new StringBuffer();
		String secondPrev = "EMPTY";
		secondStrbuf.append(secondPrev);
		int secondLength = secondPrevs == null ? 0 : secondPrevs.size();
		for(int i=0; i<prevNumber-secondLength; i++)
			secondStrbuf.append("&&&START" );
		if(secondPrevs != null && secondPrevs.size() > 0) {
			for(String p : secondPrevs)
				secondStrbuf.append("&&&" + p);
		}
		secondPrev = secondStrbuf.toString();
		addNgram(secondPrev, secondCur, secondPrevWords, secondEntireWords);
		secondEntireNumber += 1;
		//
		String prev = firstPrev + "&&&&" + secondPrev;
		String cur = firstCur + "&&&&" + secondCur;
		addNgram(prev, cur, prevWords, entireWords);	
		entireNumber += 1;
	}
	
	/**
	 * 计算熵
	 * @return
	 */
	public double calcEntropy() {
		double entropy = 0.0;
		Iterator<Entry<String, Integer>> iter = entireWords.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String entire = entry.getKey();
			int entireWordNumber = entry.getValue();
			String prev = entire.split("\\|\\|\\|")[0];
			int prevWordNumber = prevWords.get(prev);
			double curProb = entireWordNumber/(double)entireNumber 
					* Math.log(entireWordNumber/(double)prevWordNumber) * 1/Math.log(2);
			entropy += -curProb;			
		}
		return entropy;
	}
	
	public double calcConditionalEntropy() {
		double entropy = 0.0;
		Iterator<Entry<String, Integer>> iter = entireWords.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String entire = entry.getKey();
			int entireWordNumber = entry.getValue();
			String prev = entire.split("\\|\\|\\|")[0];
			String cur = entire.split("\\|\\|\\|")[1];
			int prevWordNumber = prevWords.get(prev);
			double curProb = entireWordNumber/(double)entireNumber 
					* Math.log(entireWordNumber/(double)prevWordNumber) * 1/Math.log(2);
			String firstPrev = prev.split("&&&&")[0];
			String firstCur = cur.split("&&&&")[0];
			int firstPrevWordNumber = firstPrevWords.get(firstPrev);
			String firstEntireWord = firstPrev + "|||" + firstCur;
			int firstEntireWordNumber = firstEntireWords.get(firstEntireWord);
			double firstCurProb = entireWordNumber/(double)entireNumber 
					* Math.log(firstEntireWordNumber/(double)firstPrevWordNumber) * 1/Math.log(2);
			entropy += firstCurProb - curProb;			
		}
		return entropy;
	}
	
	/**
	 * 验证统计数目是否正确
	 * @return
	 */
	public boolean checkNumber() {
		int number = 0;
		Iterator<Entry<String, Integer>> iter = entireWords.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String entire = entry.getKey();
			int entireWordNumber = entry.getValue();
			number += entireWordNumber;			
		}
		
		int prevNumber = 0;
		iter = prevWords.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String prev = entry.getKey();
			int prevWordNumber = entry.getValue();
			prevNumber += prevWordNumber;			
		}
		
		if(entireNumber == number && entireNumber == prevNumber)
			return true;
		else
			return false;
	}
	
	
}
