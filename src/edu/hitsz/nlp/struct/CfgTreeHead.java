/**
 *
 */
package edu.hitsz.nlp.struct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * head file for phrase structure treebank, usually deployed to convert them to dependency Structure
 * from (Collins, 1999; Nirve, 2006; Johansson and Nudues, 2007)
 *
 * example:
 * NP	r POS|NN|NNP|NNPS|NNS|NX|JJR;r NP;l $|ADJP|PRN;r CD;r JJ|JJS|RB|QP;r
 *
 * @author tm
 */
public class CfgTreeHead {

	public class subList{
		String direction;
		Vector<String> corList;

		public subList(){
			corList = new Vector<String>();
		}
	}

	private Vector<String> phraseType;
	private Vector<String> direction;
	private Vector<Vector<subList>> phraseList;

	public CfgTreeHead(){
		phraseType = new Vector<String>();
		direction = new Vector<String>();
		phraseList = new Vector<Vector<subList>>();
	}



	public boolean isRight(String newDirection){
		if(newDirection.equals("r"))
			return true;
		else
			return false;
	}

	public boolean isLeft(String newDirection){
		if(newDirection.equals("l"))
			return true;
		else
			return false;
	}


	/**
	 * load head file for phrase structure treebank, usually deploying to converte them to dependency Structure
	 * @param headFile
	 */
	public void loadHeadFile(String headFile){
		//read head file
		Vector<String> tempSentence=new Vector<String>();
		File file = new File(headFile);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read Training file successed:");
			String tempString=null;
			try{
				while ((tempString = reader.readLine())!= null ){
					if(tempString.trim().length()==0 || tempString.startsWith("#") ||tempString.startsWith("//"))
						continue;
					tempSentence.add(tempString.trim());
				}
				reader.close();
			}
			catch (FileNotFoundException e) {
				System.err.println(e);
			}
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
		//process
		int senLen = tempSentence.size();
		if(tempSentence.size()>0){
			for(int i=0; i<senLen; i++){
				Vector<subList> subVector = new Vector<subList>();
				String tmp = tempSentence.get(i);
				String word = tmp.replace("\\s{1,}"," ").replace("\t", " ");
				String[] words = word.split(" ");
				if(words.length<2){
					System.out.println("the headrule file is wrong");
					System.exit(0);
				}
				phraseType.add(words[0]);
				direction.add(words[1]);
				if(words.length == 2 || words[2].equals("**")){
					subList sub = new subList();
					sub.direction = words[1];
					sub.corList.add("**");
					subVector.add(sub);
				}
				else{
					for(int j=2; j<words.length; j++){
						subList sub = new subList();
						String tmpDirection = words[j].split(";")[1];
						String tmpPhrase = words[j].split(";")[0];
						sub.direction = tmpDirection;
						if(tmpPhrase.contains("|")){
							String[] phraseSplit = tmpPhrase.split("\\|");
							for(String one : phraseSplit)
								sub.corList.add(one);
						}
						else
							sub.corList.add(tmpPhrase);
						subVector.add(sub);
					}
				}
				phraseList.add(subVector);
			}
		}
	}




	/**
	 *
	 * @param newPhraseType
	 * @param sonPhraseType
	 * @return
	 */
	public int findHead(String newPhraseType, Vector<String> sonPhraseType){
		int sonNumber = sonPhraseType.size();
		int seq = -1;
		if(sonNumber == 1)
			seq = 0;
		else if(phraseType.contains(newPhraseType)){
			int phraseTypeSeq = phraseType.indexOf(newPhraseType);
			String newDirection = direction.get(phraseTypeSeq);
			Vector<subList> matchPhrase = phraseList.get(phraseTypeSeq);
			int matchNumber = matchPhrase.size();
			for(int i=0; i<matchNumber; i++){
				subList newList = matchPhrase.get(i);
				int subNumber = newList.corList.size();
				if(subNumber == 1){
					String tmp = newList.corList.get(0);
					if(isRight(newList.direction))
						seq = findHeadFromRight(tmp,sonPhraseType);
					else
						seq = findHeadFromLeft(tmp,sonPhraseType);
				}
				else{
					if(isRight(newList.direction))
						seq = findHeadFromRight(newList.corList,sonPhraseType);
					else
						seq = findHeadFromLeft(newList.corList,sonPhraseType);
				}
				if(seq != -1)
					break;
			}
			if(seq == -1){
				if(isRight(newDirection))
					seq = sonNumber - 1;
				else
					seq = 0;
			}
		}
		else
				seq = 0;
		return seq;
	}




	/**
	 * find a son phrase matching the matchPhrse from left to right
	 * @param curPhraseType
	 * @param sonPhraseType
	 * @return
	 */
	public int findHeadFromLeft(String matchPhraseType, Vector<String> sonPhraseType){
		int sonNumber = sonPhraseType.size();
		if(matchPhraseType.contains("**"))
			return 0;
		else if(matchPhraseType.contains("*")){
			Pattern aP = Pattern.compile(matchPhraseType.replace("*", ".*"));
			for(int i=0; i<sonNumber; i++){
				Matcher m = aP.matcher(sonPhraseType.get(i));
				if(m.matches())
					return i;
			}
		}
		else{
			for(int i=0; i<sonNumber; i++){
				if(matchPhraseType.equals(sonPhraseType.get(i)))
					return i;
			}
		}
		return -1;
	}

	/**
	 * find a son phrase matching the matchPhrse from right to left
	 * @param curPhraseType
	 * @param sonPhraseType
	 * @return
	 */
	public int findHeadFromRight(String matchPhraseType, Vector<String> sonPhraseType){
		int sonNumber = sonPhraseType.size();
		if(matchPhraseType.contains("**"))
			return sonNumber-1;
		else if(matchPhraseType.contains("*")){
			Pattern aP = Pattern.compile(matchPhraseType.replace("*", ".*"));
			for(int i=sonNumber-1; i>=0; i--){
				Matcher m = aP.matcher(sonPhraseType.get(i));
				if(m.matches())
					return i;
			}
		}
		else{
			for(int i=sonNumber-1; i>=0; i--){
				if(matchPhraseType.equals(sonPhraseType.get(i)))
					return i;
			}
		}
		return -1;
	}

	/**
	 * find any son phrase matching the matchPhrse from left to right
	 * @param matchPhraseType
	 * @param sonPhraseType
	 * @return
	 */
	public int findHeadFromLeft(Vector<String> matchPhraseType, Vector<String> sonPhraseType){
		int matchNumber = matchPhraseType.size();
		int sonNumber = sonPhraseType.size();
		for(int i=0; i<sonNumber; i++){
			for(int j=0; j<matchNumber; j++){
				if(matchPhraseType.get(j).contains("**"))
					return i;
				else if(matchPhraseType.get(j).contains("*")){
					Pattern aP = Pattern.compile(matchPhraseType.get(j).replace("*", ".*"));
					Matcher m = aP.matcher(sonPhraseType.get(i));
					if(m.matches())
							return i;
				}
				else if(matchPhraseType.contains(sonPhraseType.get(i)))
					return i;
			}
		}
		return -1;
	}

	/**
	 * find any son phrase matching the matchPhrse from right to left
	 * @param matchPhraseType
	 * @param sonPhraseType
	 * @return
	 */
	public int findHeadFromRight(Vector<String> matchPhraseType, Vector<String> sonPhraseType){
		int matchNumber = matchPhraseType.size();
		int sonNumber = sonPhraseType.size();
		for(int i=sonNumber-1; i>=0; i--){
			for(int j=0; j<matchNumber; j++){
				if(matchPhraseType.get(j).contains("**"))
					return i;
				else if(matchPhraseType.get(j).contains("*")){
					Pattern aP = Pattern.compile(matchPhraseType.get(j).replace("*", ".*"));
					Matcher m = aP.matcher(sonPhraseType.get(i));
					if(m.matches())
							return i;
				}
				else if(matchPhraseType.contains(sonPhraseType.get(i)))
					return i;
			}
		}
		return -1;
	}








	static void test(){
		CfgTreeHead newTreeHead = new CfgTreeHead();
		newTreeHead.loadHeadFile("/mnt/d5/test/headrules.txt");
		Vector<String> sonPhrase= new Vector<String>();
		sonPhrase.add("PP");
		sonPhrase.add("NNS");
		sonPhrase.add("TO");
		sonPhrase.add("CD");
		System.out.println(newTreeHead.findHead("PP",sonPhrase));
	}



}
