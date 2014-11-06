package edu.hitsz.nlp.struct;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import edu.hitsz.nlp.util.FeatureTemplate;


/**
 * a conll-like sentence,
 * in which sentences are seperated by a blank line,
 * and one sentence contains many lines, usually one token one line.
 * @author tm
 *
 */
public class ConllSentence {

	private int sentenceLength;
	/** correct tag */
	private ArrayList<String> resultSignal;
	/** predicted tags */
	public ArrayList<String> predictSignal;
	/** To store all the information
	 *  word1 pos1 ...
	 *  word2 pos2 ...
	 */
	private ArrayList<ArrayList<String>> words;


	public ConllSentence(int number){
		sentenceLength = 0;
		words = new ArrayList<ArrayList<String>>();
		predictSignal = new ArrayList<String>();
	}

	public ConllSentence(){
		sentenceLength = 0;
		words = new ArrayList<ArrayList<String>>();
		predictSignal = new ArrayList<String>();
	}

	public int getSentenceLength(){
		return this.sentenceLength;
	}
	public void setSentenceLength(int newLength){
		this.sentenceLength = newLength;
	}

	public ArrayList<ArrayList<String>> getWords(){
		return words;
	}

	public ArrayList<String> getResultSignal(){
		return resultSignal;
	}

	/**
	 * 获得第i列数据，比如所有词，所有词性等
	 * @since Dec 14, 2011
	 * @param i
	 * @return
	 */
	public ArrayList<String> getWords(int i){
		if(i >= words.size()){
			System.out.println("Error: required columns in words of sentence is exceed the size of words");
			System.exit(-1);
		}
		return words.get(i);
	}

	public void setWords(ArrayList<ArrayList<String>> x){
		words = new ArrayList<ArrayList<String>>();
		words = x;
	}

	public void clearWords(){
		words = null;
	}


	/**
	 * read data
	 * @param tempSentence Sentence structured as a ArrayList of strings
	 * @param len Length of the ArrayList
	 */
	public void process(ArrayList<String> tempSentence, int len){
		sentenceLength=len;
		resultSignal = new ArrayList<String>();
		int columnNumber = 0;
		for(int i=0;i<sentenceLength;i++){
			String[] allWords = null;
			allWords=tempSentence.get(i).split("\\s+");
			columnNumber = allWords.length;
			if(i==0){
				for(int j=0; j<columnNumber; j++){
					ArrayList<String> newVec = new ArrayList<String>();
					words.add(newVec);
				}
			}
			resultSignal.add(allWords[columnNumber-1]);
			if(columnNumber != words.size()){
				System.out.println(words.size()+":"+columnNumber+":"+tempSentence.get(i)+":"+tempSentence.get(i)+":"+allWords[0]);
				this.output();
				System.out.println(this.getClass().getName()+" Error: Line "+i+1+" is not equal to line above of sentence ");
				System.exit(-1);
			}
			int j=0;
			while(j<columnNumber){
				words.get(j).add(allWords[j]);
				j++;
			}
		}
	}



	/**
	 * select columns for sentence, and delete the others
	 * @param columns
	 */
	public void selectColumns(ArrayList<Integer> columns){
		int columnNumber = words.size();
		if(columns.get(columns.size()-1) >= columnNumber){
			System.out.println("the input column exceed the column number "+columnNumber+" in file");
			System.exit(-1);
		}
		for(int i=columnNumber-1; i>=0; i--)
			if(!columns.contains(i))
				words.remove(i);
	}

	/**
	 * add another sentence with given columns, 在一个句子上增加另一个句子的某些列
	 * @param addSentence
	 * @param addColumns
	 */
	public void addSentenceColumns(ConllSentence addSentence, ArrayList<Integer> addColumns){
		if(addSentence.sentenceLength != this.sentenceLength){
			System.out.println(this.getClass().getName()+" Error: the two sentence have different lengths");
			System.exit(-1);
		}
		addSentence.selectColumns(addColumns);
		ArrayList<ArrayList<String>> addWords = addSentence.words;
		words.addAll(addWords);
	}

	/**
	 * add another sentence， 在句子后面增加另一个句子
	 * @param addSentence
	 * @param addColumns
	 */
	public void addSentence(ConllSentence addSentence){
		if(addSentence.words.size() != this.words.size()){
			System.out.println(this.getClass().getName()+" Error: the two sentence have different column number");
			System.exit(-1);
		}
		this.sentenceLength += addSentence.sentenceLength;
		for(int i=0; i<words.size(); i++)
			words.get(i).addAll(addSentence.words.get(i));
		this.predictSignal.addAll(addSentence.predictSignal);
		this.resultSignal.addAll(addSentence.resultSignal);
	}


	/**
	 *  overturn the matrix of words in sentence, convert
	 *  1 a    1 2 3
	 *  2 b -> a b c
	 *  3 c
	 */
	public void overturn(){
		ArrayList<ArrayList<String>> newWords = new ArrayList<ArrayList<String>>();
		int column = words.size();
		int row = words.get(0).size();
		for(int i=0; i<row; i++){
			ArrayList<String> newColumn = new ArrayList<String>();
			for(int j=0; j<column; j++)
				newColumn.add(words.get(j).get(i));
			newWords.add(newColumn);
		}
		words = newWords;
		sentenceLength = column;
	}

	/**
	 * 	reverse the order of columns in sentence，翻转句子的列次序
	 *  1 a    a 1
	 *  2 b -> b 2
	 *  3 c    c 3
	 */
	public void reverseColumn(){
		ArrayList<ArrayList<String>> newWords = new ArrayList<ArrayList<String>>();
		int columnNumber = words.size();
		for(int i=columnNumber-1; i>=0; i--)
			newWords.add(words.get(i));
		words = newWords;
	}

	/**
	 * 	reverse the order of rows in sentence，翻转句子的列次序
	 *  1 a    3 c
	 *  2 b -> 2 b
	 *  3 c    1 a
	 */
	public void reverseRow(){
		ArrayList<ArrayList<String>> newWords = new ArrayList<ArrayList<String>>();
		int columnNumber = words.size();
		int rowNumber = words.get(0).size();
		for(int i=0; i<columnNumber; i++){
			ArrayList<String> newColumn = new ArrayList<String>();
			for(int j=rowNumber-1; j>=0; j--)
				newColumn.add(words.get(i).get(j));
			newWords.add(newColumn);
		}
		words = newWords;
	}

	/**
	 * 根据标点符号来分开句子
	 * @param pucts
	 * @return
	 */
	public ArrayList<ConllSentence> splitPuncts(ArrayList<String> pucts){
		ArrayList<ConllSentence> newSentences = new ArrayList<ConllSentence>();
		ArrayList<Integer> puctPosition = new ArrayList<Integer> ();
		for(int i=0; i<sentenceLength; i++){
			if(pucts.contains(words.get(0).get(i)))
				puctPosition.add(i);
		}
		if(puctPosition.size() == 0){
			newSentences.add(this);
		}
		else{
			//第一个符号之前的句子
			ConllSentence newSentence = new ConllSentence();
			for(int j=0; j<words.size(); j++){
				ArrayList<String> tmpList = new ArrayList<String>();
				for(int k=0; k<puctPosition.get(0)+1; k++)
					tmpList.add(words.get(j).get(k));
				newSentence.words.add(tmpList);
				newSentence.sentenceLength = puctPosition.get(0)+1;
			}
			newSentences.add(newSentence);
			//最后一个符号之后的句子
			//if(puctPosition.get(puctPosition.size()-1)<sentenceLength-1)
			for(int i=0; i<puctPosition.size() && puctPosition.get(i)<sentenceLength-1; i++){
				int curPosition = puctPosition.get(i);
				if(curPosition<sentenceLength-1){
					int start = puctPosition.get(i)+1;
					int end = sentenceLength;
					if(i+1<puctPosition.size()){
						end = puctPosition.get(i+1)+1;
					}
					newSentence = new ConllSentence();
					for(int j=0; j<words.size(); j++){
						ArrayList<String> tmpList = new ArrayList<String>();
						for(int k=start; k<end; k++)
							tmpList.add(words.get(j).get(k));
						newSentence.words.add(tmpList);
						newSentence.sentenceLength = end - start;
					}
					newSentences.add(newSentence);
				}
			}
		}
		return newSentences;
	}

	
	/**
	 * 根据标点符号来分开句子
	 * @param pucts
	 * @return
	 */
	public ArrayList<ConllSentence> splitPuncts(ArrayList<String> pucts, ConllSentence secondSentence){
		ArrayList<ConllSentence> newSentences = new ArrayList<ConllSentence>();
		ArrayList<Integer> puctPosition = new ArrayList<Integer> ();
		ArrayList<Integer> secondPuctPosition = new ArrayList<Integer> ();
		for(int i=0; i<sentenceLength; i++){
			if(pucts.contains(words.get(0).get(i)))
				puctPosition.add(i);
		}
		for(int i=0; i<secondSentence.sentenceLength; i++){
			if(pucts.contains(secondSentence.words.get(0).get(i)))
				secondPuctPosition.add(i);
		}
		if(puctPosition.size() == 0 || puctPosition.size() != secondPuctPosition.size()){
			newSentences.add(this);
		}
		else{
			//第一个符号之前的句子
			ConllSentence newSentence = new ConllSentence();
			for(int j=0; j<words.size(); j++){
				ArrayList<String> tmpList = new ArrayList<String>();
				for(int k=0; k<puctPosition.get(0)+1; k++)
					tmpList.add(words.get(j).get(k));
				newSentence.words.add(tmpList);
				newSentence.sentenceLength = puctPosition.get(0)+1;
			}
			newSentences.add(newSentence);
			//最后一个符号之后的句子
			//if(puctPosition.get(puctPosition.size()-1)<sentenceLength-1)
			for(int i=0; i<puctPosition.size() && puctPosition.get(i)<sentenceLength-1; i++){
				int curPosition = puctPosition.get(i);
				if(curPosition<sentenceLength-1){
					int start = puctPosition.get(i)+1;
					int end = sentenceLength;
					if(i+1<puctPosition.size()){
						end = puctPosition.get(i+1)+1;
					}
					newSentence = new ConllSentence();
					for(int j=0; j<words.size(); j++){
						ArrayList<String> tmpList = new ArrayList<String>();
						for(int k=start; k<end; k++)
							tmpList.add(words.get(j).get(k));
						newSentence.words.add(tmpList);
						newSentence.sentenceLength = end - start;
					}
					newSentences.add(newSentence);
				}
			}
		}
		return newSentences;
	}




	/**
	 * 判断两个句子是否相同
	 * @param secondSentence
	 * @return
	 */
	public boolean isSameSentence(ConllSentence secondSentence){
		if(this.words.equals(secondSentence.words))
			return true;
		return false;
	}

	/**
	 * 检测句子在某列中是否包含某个tag
	 * @param column
	 * @param tag
	 * @return
	 */
	public boolean containsTag(int column, String tag){
		if(column >= this.words.size()){
			System.out.println(this.getClass().getName()+": input column larger than sentence column");
			System.exit(-1);
		}
		if(this.words.get(column).contains(tag))
			return true;
		else
			return false;
	}


	/**
	 * store sentence into file
	 * @param outWriter
	 * @param isWordTabed Type of separator between words: true for Tab, false for blank
	 * @throws IOException
	 */
	public void store(FileWriter outWriter, boolean isWordTabed) throws IOException{
		if(sentenceLength>0){
			int componentLen=words.size();
			for(int i=0; i<sentenceLength; i++){
				String tmpString="";
				for(int j=0;j<componentLen;j++){
					if(isWordTabed)
						tmpString+=words.get(j).get(i)+"\t";
					else
						tmpString+=words.get(j).get(i)+" ";
				}
				outWriter.write(tmpString.trim()+"\n");
			}
		}
	}


	/**
	 * Store sentence in which the tokens are in reverse order.
	 * @throws IOException
	 *
	 */
	public void storeReverseRow(FileWriter newWriter) throws IOException{
		int i=sentenceLength-1;
		if(sentenceLength>0){
			int componentLen=words.get(0).size();
			for(;i>=0;i--){
				String tmpString="";
				for(int j=0;j<componentLen;j++)
					tmpString+="\t"+words.get(i).get(j);
				newWriter.write(tmpString.trim()+"\n");
			}
		}
		newWriter.write("\n");
	}




	/**
	 *  display sentence
	 */
	public void output(){
		if(sentenceLength>0){
			int componentLen=words.size();
			for(int i=0; i<sentenceLength; i++){
				String tmpString="";
				for(int j=0;j<componentLen;j++)
					tmpString+=words.get(j).get(i)+"\t";
				System.out.println(tmpString.trim());
			}
		}
	}



	/**
	 * extract feature from paper (Collins, 2002; Spoustova, 2009) for POS tagging
	 * @param features Feature ArrayList extracted from the sentence
	 * @param tokenNum The tokenNum of the sentence we want to extract
	 */
	public void extractTokenFeaturesPOS(ArrayList<String> features, int tokenNum){
		//determine the POS position in the ArrayList word
		int feaSeq=1;

		/*
		//Previous tag
		String previousTag="";
		if(tokenNum<=0)
			previousTag="NULL";
		else
			previousTag=(String)predictSignal.get(0).get(tokenNum-1);
		features.add(Integer.toString(feaSeq++)+":"+previousTag);
		//Previous two tags
		String previousSecondTag="";
		if(tokenNum<=1)
			previousSecondTag="NULL";
		else
			previousSecondTag=(String)predictSignal.get(0).get(tokenNum-2);
		String previousTwoTags=previousSecondTag+"+"+previousTag;
		features.add(Integer.toString(feaSeq++)+":"+previousTwoTags);
		*/

		/*
		//First letter of the previous tag
		String firstLetterOfPreviousTag=previousTag.substring(0, 1);
		features.add(Integer.toString(feaSeq++)+":"+firstLetterOfPreviousTag);
		*/
		//current word form
		String currentWord=(String)words.get(0).get(tokenNum);
		features.add(Integer.toString(feaSeq++)+":"+currentWord);
		//previous word form
		String previousWord="";
		if(tokenNum<=0)
			previousWord="NULL";
		else
			previousWord=(String)words.get(0).get(tokenNum-1);
		features.add(Integer.toString(feaSeq++)+":"+previousWord);
		//previous two word forms
		String previousSecondWord="";
		if(tokenNum<=1)
			previousSecondWord="NULL";
		else
			previousSecondWord=(String)words.get(0).get(tokenNum-2);
		features.add(Integer.toString(feaSeq++)+":"+previousSecondWord);
		//String previousTwoWordForms=previousSecondWord+"+"+previousWord;
		//features.add(Integer.toString(feaSeq++)+":"+previousTwoWordForms);

		//Following word form
		String followingWord="";
		if(tokenNum+1>=sentenceLength)
			followingWord="NULL";
		else
			followingWord=(String)words.get(0).get(tokenNum+1);
		features.add(Integer.toString(feaSeq++)+":"+followingWord);
		//Following two word forms
		String followingSecondWord="";
		if(tokenNum+2>=sentenceLength)
			followingSecondWord="NULL";
		else
			followingSecondWord=(String)words.get(0).get(tokenNum+2);
		features.add(Integer.toString(feaSeq++)+":"+followingSecondWord);
		//String followingTwoWords=followingWord+"+"+followingSecondWord;
		//features.add(Integer.toString(feaSeq++)+":"+followingTwoWords);
		/*
		//Last but one word form
		String lastButOneWord="";
		if(sentenceLength==1)
			lastButOneWord="NULL";
		else
			lastButOneWord=(String)word.get(sentenceLength-2).get(0);
		features.add(Integer.toString(feaSeq++)+":"+lastButOneWord);
		*/
		//prefixes of length 1-9
		int prefixNumber=4;
		int currentWordLength=currentWord.length();
		for(int j=0;j<prefixNumber;j++){
			String prefixWord="";
			if(currentWordLength>j)
				prefixWord=currentWord.substring(0,j+1);
			else
				prefixWord=currentWord;
			features.add(Integer.toString(feaSeq++)+":"+prefixWord);
		}

		//suffixes of length 1-9
		int suffixNumber=4;
		currentWordLength=currentWord.length();
		for(int j=0;j<suffixNumber;j++){
			String suffixWord="";
			if(currentWordLength>j)
				suffixWord=currentWord.substring(currentWordLength-1-j);
			else
				suffixWord=currentWord;
			features.add(Integer.toString(feaSeq++)+":"+suffixWord);
		}
		//Contains number
		boolean containNumber=false;
		for(int j=0;j<currentWordLength;j++){
			if(Character.isDigit(currentWord.charAt(j))){
				containNumber=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containNumber);
		//contains dash(hyphen)
		boolean containDash=false;
		for(int j=0;j<currentWordLength;j++){
			if(currentWord.charAt(j)=='-'){
				containDash=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containDash);
		//contains upper case letter
		boolean containUpper=false;
		for(int j=0;j<currentWordLength;j++){
			if(Character.isUpperCase(currentWord.charAt(j))){
				containUpper=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containUpper);
	}


	/**
	 * extract features for Chunking,只采用词的信息
	 * @param features Feature ArrayList extracted from the sentence
	 * @param tokenNum The tokenNum of the sentence we want to extract
	*/
	public void extractTokenFeaturesChunk(ArrayList<String> features, int tokenNum){
		//determine the POS position in the ArrayList word
		int feaSeq=1;

		//current word form
		String currentWord=(String)words.get(0).get(tokenNum);
		features.add(Integer.toString(feaSeq++)+":"+currentWord);
		//previous word form
		String previousWord="";
		if(tokenNum<=0)
			previousWord="NULL";
		else
			previousWord=(String)words.get(0).get(tokenNum-1);
		features.add(Integer.toString(feaSeq++)+":"+previousWord);
		//previous two word forms
		String previousSecondWord="";
		if(tokenNum<=1)
			previousSecondWord="NULL";
		else
			previousSecondWord=(String)words.get(0).get(tokenNum-2);
		features.add(Integer.toString(feaSeq++)+":"+previousSecondWord);
		//Following word form
		String followingWord="";
		if(tokenNum+1>=sentenceLength)
			followingWord="NULL";
		else
			followingWord=(String)words.get(0).get(tokenNum+1);
		features.add(Integer.toString(feaSeq++)+":"+followingWord);
		//Following two word forms
		String followingSecondWord="";
		if(tokenNum+2>=sentenceLength)
			followingSecondWord="NULL";
		else
			followingSecondWord=(String)words.get(0).get(tokenNum+2);
		features.add(Integer.toString(feaSeq++)+":"+followingSecondWord);

		//two word feature combination:6－9
		String previousTwoWords=previousSecondWord+"+"+previousWord;
		features.add(Integer.toString(feaSeq++)+":"+previousTwoWords);
		String preciousCurrentWords=previousWord+"+"+currentWord;
		features.add(Integer.toString(feaSeq++)+":"+preciousCurrentWords);
		String currentFollowingWords=currentWord+"+"+followingWord;
		features.add(Integer.toString(feaSeq++)+":"+currentFollowingWords);
		String followingTwoWords=followingWord+"+"+followingSecondWord;
		features.add(Integer.toString(feaSeq++)+":"+followingTwoWords);

		//three word combination:10-12

		features.add(Integer.toString(feaSeq++)+":"+previousTwoWords+"+"+currentWord);
		features.add(Integer.toString(feaSeq++)+":"+preciousCurrentWords+"+"+followingWord);
		features.add(Integer.toString(feaSeq++)+":"+currentWord+"+"+followingTwoWords);


		/*
		//Last but one word form
		String lastButOneWord="";
		if(sentenceLength==1)
			lastButOneWord="NULL";
		else
			lastButOneWord=(String)word.get(sentenceLength-2).get(0);
		features.add(Integer.toString(feaSeq++)+":"+lastButOneWord);
		*/
		//prefixes of length 1-4
		int prefixNumber=4;
		int currentWordLength=currentWord.length();
		for(int j=0;j<prefixNumber;j++){
			String prefixWord="";
			if(currentWordLength>j)
				prefixWord=currentWord.substring(0,j+1);
			else
				prefixWord=currentWord;
			features.add(Integer.toString(feaSeq++)+":"+prefixWord);
		}

		//suffixes of length 1-4
		int suffixNumber=4;
		currentWordLength=currentWord.length();
		for(int j=0;j<suffixNumber;j++){
			String suffixWord="";
			if(currentWordLength>j)
				suffixWord=currentWord.substring(currentWordLength-1-j);
			else
				suffixWord=currentWord;
			features.add(Integer.toString(feaSeq++)+":"+suffixWord);
		}

		//Contains number
		boolean containNumber=false;
		for(int j=0;j<currentWordLength;j++){
			if(Character.isDigit(currentWord.charAt(j))){
				containNumber=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containNumber);
		//contains dash(hyphen)
		boolean containDash=false;
		for(int j=0;j<currentWordLength;j++){
			if(currentWord.charAt(j)=='-'){
				containDash=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containDash);
		//contains upper case letter
		boolean containUpper=false;
		for(int j=0;j<currentWordLength;j++){
			if(Character.isUpperCase(currentWord.charAt(j))){
				containUpper=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containUpper);
	}




	/**
	 * extract feature POS tagging from word and chunk features
	 * 从词和chunk中提取特征用于POS tagging
	 * @param features Feature ArrayList extracted from the sentence
	 * @param tokenNum The tokenNum of the sentence we want to extract
	 */
	public void extractTokenFeaturesChunkPOS(ArrayList<String> features, int tokenNum){
		//determine the POS position in the ArrayList word
		int feaSeq=1;

		//current word form
		String currentWord=(String)words.get(0).get(tokenNum);
		features.add(Integer.toString(feaSeq++)+":"+currentWord);
		//previous word form
		String previousWord="";
		if(tokenNum<=0)
			previousWord="NULL";
		else
			previousWord=(String)words.get(0).get(tokenNum-1);
		features.add(Integer.toString(feaSeq++)+":"+previousWord);
		//previous two word forms
		String previousSecondWord="";
		if(tokenNum<=1)
			previousSecondWord="NULL";
		else
			previousSecondWord=(String)words.get(0).get(tokenNum-2);
		features.add(Integer.toString(feaSeq++)+":"+previousSecondWord);
		//Following word form
		String followingWord="";
		if(tokenNum+1>=sentenceLength)
			followingWord="NULL";
		else
			followingWord=(String)words.get(0).get(tokenNum+1);
		features.add(Integer.toString(feaSeq++)+":"+followingWord);
		//Following two word forms
		String followingSecondWord="";
		if(tokenNum+2>=sentenceLength)
			followingSecondWord="NULL";
		else
			followingSecondWord=(String)words.get(0).get(tokenNum+2);
		features.add(Integer.toString(feaSeq++)+":"+followingSecondWord);

		//two word feature combination
		String previousTwoWords=previousSecondWord+"+"+previousWord;
		features.add(Integer.toString(feaSeq++)+":"+previousTwoWords);
		String preciousCurrentWords=previousWord+"+"+currentWord;
		features.add(Integer.toString(feaSeq++)+":"+preciousCurrentWords);
		String currentFollowingWords=currentWord+"+"+followingWord;
		features.add(Integer.toString(feaSeq++)+":"+currentFollowingWords);
		String followingTwoWords=followingWord+"+"+followingSecondWord;
		features.add(Integer.toString(feaSeq++)+":"+followingTwoWords);




		/*
		//Last but one word form
		String lastButOneWord="";
		if(sentenceLength==1)
			lastButOneWord="NULL";
		else
			lastButOneWord=(String)word.get(sentenceLength-2).get(0);
		features.add(Integer.toString(feaSeq++)+":"+lastButOneWord);
		*/
		//prefixes of length 1-9
		int prefixNumber=4;
		int currentWordLength=currentWord.length();
		for(int j=0;j<prefixNumber;j++){
			String prefixWord="";
			if(currentWordLength>j)
				prefixWord=currentWord.substring(0,j+1);
			else
				prefixWord=currentWord;
			features.add(Integer.toString(feaSeq++)+":"+prefixWord);
		}

		//suffixes of length 1-9
		int suffixNumber=4;
		currentWordLength=currentWord.length();
		for(int j=0;j<suffixNumber;j++){
			String suffixWord="";
			if(currentWordLength>j)
				suffixWord=currentWord.substring(currentWordLength-1-j);
			else
				suffixWord=currentWord;
			features.add(Integer.toString(feaSeq++)+":"+suffixWord);
		}
		//Contains number
		boolean containNumber=false;
		for(int j=0;j<currentWordLength;j++){
			if(Character.isDigit(currentWord.charAt(j))){
				containNumber=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containNumber);
		//contains dash(hyphen)
		boolean containDash=false;
		for(int j=0;j<currentWordLength;j++){
			if(currentWord.charAt(j)=='-'){
				containDash=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containDash);
		//contains upper case letter
		boolean containUpper=false;
		for(int j=0;j<currentWordLength;j++){
			if(Character.isUpperCase(currentWord.charAt(j))){
				containUpper=true;
				break;
			}
		}
		features.add(Integer.toString(feaSeq++)+":"+containUpper);



		//current Chunk form
		String currentChunk=(String)words.get(1).get(tokenNum);
		features.add(Integer.toString(feaSeq++)+":"+currentChunk);
		//previous Chunk form
		String previousChunk="";
		if(tokenNum<=0)
			previousChunk="NULL";
		else
			previousChunk=(String)words.get(1).get(tokenNum-1);
		features.add(Integer.toString(feaSeq++)+":"+previousChunk);
		//previous two Chunk forms
		String previousSecondChunk="";
		if(tokenNum<=1)
			previousSecondChunk="NULL";
		else
			previousSecondChunk=(String)words.get(1).get(tokenNum-2);
		features.add(Integer.toString(feaSeq++)+":"+previousSecondChunk);
		//Following Chunk form
		String followingChunk="";
		if(tokenNum+1>=sentenceLength)
			followingChunk="NULL";
		else
			followingChunk=(String)words.get(1).get(tokenNum+1);
		features.add(Integer.toString(feaSeq++)+":"+followingChunk);
		//Following two Chunk forms
		String followingSecondChunk="";
		if(tokenNum+2>=sentenceLength)
			followingSecondChunk="NULL";
		else
			followingSecondChunk=(String)words.get(1).get(tokenNum+2);
		features.add(Integer.toString(feaSeq++)+":"+followingSecondChunk);

		//two Chunk feature combination
		String previousTwoChunks=previousSecondChunk+"+"+previousChunk;
		features.add(Integer.toString(feaSeq++)+":"+previousTwoChunks);
		String preciousCurrentChunks=previousChunk+"+"+currentChunk;
		features.add(Integer.toString(feaSeq++)+":"+preciousCurrentChunks);
		String currentFollowingChunks=currentChunk+"+"+followingChunk;
		features.add(Integer.toString(feaSeq++)+":"+currentFollowingChunks);
		String followingTwoChunks=followingChunk+"+"+followingSecondChunk;
		features.add(Integer.toString(feaSeq++)+":"+followingTwoChunks);


	}




	public void extractAndStoreFeatures (FileWriter newFile, String type) throws IOException{
		
		ArrayList<String> newArrayList=new ArrayList<String>();
		int senLen=this.sentenceLength;
		String tmpString="";
		for(int i=0;i<senLen;i++){
			newArrayList.clear();
			if(type.equals("pos"))
				this.extractTokenFeaturesPOS(newArrayList, i);
			else if(type.equals("chunk"))
				this.extractTokenFeaturesChunk(newArrayList, i);
			else if(type.equals("chunkpos"))
				this.extractTokenFeaturesChunkPOS(newArrayList, i);
			else{
				System.out.println("The input type is not found. Please check");
				System.exit(1);
			}
			String newString="";
			for(String tmp : newArrayList){
				newString+="\t"+tmp;
			}
			//int num = this.words.get(i).size();
			tmpString+=newString.trim()+"\t"+this.resultSignal.get(i)+"\n";
			//tmpString+=this.word.get(i).get(num-2)+" "+this.predictSignal.get(0).get(i)+" ---- "+newString.trim()+"\n";
		}
		newFile.write(tmpString+"\n");
	}


}
