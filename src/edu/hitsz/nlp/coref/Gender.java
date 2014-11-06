/**
 * 
 */
package edu.hitsz.nlp.coref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author tm
 *
 */
public class Gender {
	public class OneInst{
		public int masculine;
		public int feminine;
		public int neutral;
		public int plural;		
	}
	public HashMap<String,OneInst> startMap;
	public HashMap<String,OneInst> endMap;
	public HashMap<String,OneInst> wordMap;
	
	public Gender(){
		startMap = new HashMap<String,OneInst>();
		endMap = new HashMap<String,OneInst>();
		wordMap = new HashMap<String,OneInst>();
	}
	
	/**
	 * load gender structure from file
	 * @param genderFileName
	 */
	public void readFile(String genderFileName){
		//read gender file
		File file = new File(genderFileName);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Reading gender file begin:");
			String tempString=null;	
			int count=0;
			try{				
				while ((tempString = reader.readLine())!= null ){
					if(tempString.trim().length()==0)
						continue;
					count++;
					if(count%10000==0)
						System.out.print(count+".");
					//process the sentence
					String[] tmp=tempString.split("\t");
					String[] tmpNumber = tmp[1].split(" ");
					if(tmpNumber.length<4){
						System.out.println("the format in gender file is wrong in "+tmp[1]);
						System.exit(1);
					}
					OneInst tmpInst = new OneInst();
					tmpInst.masculine = Integer.parseInt(tmpNumber[0]);
					tmpInst.feminine = Integer.parseInt(tmpNumber[1]);
					tmpInst.neutral = Integer.parseInt(tmpNumber[2]);
					tmpInst.plural = Integer.parseInt(tmpNumber[3]);
					if(tmp[0].startsWith("! ")){
						if(tmp[0].length()<3){
							System.out.println("the format in gender file is wrong in "+tmp[1]);
							System.exit(1);
						}
						startMap.put(tmp[0].substring(2), tmpInst);
					}
					else if(tmp[0].endsWith(" !")){
						if(tmp[0].length()<3){
							System.out.println("the format in gender file is wrong in "+tmp[1]);
							System.exit(1);
						}
						endMap.put(tmp[0].substring(0,tmp[0].length()-2), tmpInst);
					}
					else{
						String tmpWord = tmp[0].replace(" ", "+");
						wordMap.put(tmpWord, tmpInst);
					}					
				}
				
				reader.close();
				System.out.println("\nReading gender file done");
			}
			catch (FileNotFoundException e) {
				System.err.println(e);
			}
		}		
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	/**
	 * determine whether the strings is plural
	 * @param newStrings
	 * @param headWord
	 * @return t:true; f:false, u:unknown
	 */
	public int isPlural(ArrayList<String> newStrings, String headWord){
		ArrayList<String> singleWords = new ArrayList<String>();
		singleWords.add("a");singleWords.add("an");singleWords.add("this");
		ArrayList<String> pluralWords = new ArrayList<String>();
		pluralWords.add("those");pluralWords.add("these");pluralWords.add("some");pluralWords.add("many");
		if(singleWords.contains(newStrings.get(0)))
			return -1;
		else if (pluralWords.contains(newStrings.get(0)))
			return 1;
		int newStringsNumber = newStrings.size();
		String cleanHeadWord = this.replaceNumber(headWord);
		String tmp="";
		for(int i=0; i<newStringsNumber; i++)
			tmp+="+"+this.replaceNumber(newStrings.get(i));
		tmp = tmp.substring(1);
		if(wordMap.containsKey(tmp)){
			OneInst newInst = wordMap.get(tmp);
			int tmpMasculine = newInst.masculine;
			int tmpFeminine = newInst.feminine;
			int tmpNeutral = newInst.neutral;
			int tmpPlural = newInst.plural;
			if(tmpPlural > (tmpMasculine+tmpFeminine+tmpNeutral))
				return 1;
			else if(tmpPlural < (tmpMasculine+tmpFeminine+tmpNeutral))
				return -1;
		}		
		else if(wordMap.containsKey(cleanHeadWord)){
			OneInst newInst = wordMap.get(cleanHeadWord);
			int tmpMasculine = newInst.masculine;
			int tmpFeminine = newInst.feminine;
			int tmpNeutral = newInst.neutral;
			int tmpPlural = newInst.plural;
			if(tmpPlural > (tmpMasculine+tmpFeminine+tmpNeutral))
				return 1;
			else if(tmpPlural < (tmpMasculine+tmpFeminine+tmpNeutral))
				return -1;
		}
		else if(endMap.containsKey(this.replaceNumber(newStrings.get(newStringsNumber-1)))
				|| startMap.containsKey(this.replaceNumber(newStrings.get(0)))){
			int tmpMasculine = 0;
			int tmpFeminine = 0;
			int tmpNeutral = 0;
			int tmpPlural = 0;
			if(endMap.containsKey(this.replaceNumber(newStrings.get(newStringsNumber-1)))){
				OneInst newInst = endMap.get(this.replaceNumber(newStrings.get(newStringsNumber-1)));
				tmpMasculine += newInst.masculine;
				tmpFeminine += newInst.feminine;
				tmpNeutral += newInst.neutral;
				tmpPlural += newInst.plural;				
			}
			else if(startMap.containsKey(this.replaceNumber(newStrings.get(0)))){
				OneInst newInst = startMap.get(this.replaceNumber(newStrings.get(0)));
				tmpMasculine += newInst.masculine;
				tmpFeminine += newInst.feminine;
				tmpNeutral += newInst.neutral;
				tmpPlural += newInst.plural;
			}
			if(tmpPlural > (tmpMasculine+tmpFeminine+tmpNeutral))
				return 1;
			else if(tmpPlural < (tmpMasculine+tmpFeminine+tmpNeutral))
				return -1;
		}
		return 0;
	}
	
	public int isPlural(String newWord){
		String cleanHeadWord = this.replaceNumber(newWord);
		if(wordMap.containsKey(cleanHeadWord)){
			OneInst newInst = wordMap.get(cleanHeadWord);
			int tmpMasculine = newInst.masculine;
			int tmpFeminine = newInst.feminine;
			int tmpNeutral = newInst.neutral;
			int tmpPlural = newInst.plural;
			if(tmpPlural > (tmpMasculine+tmpFeminine+tmpNeutral))
				return 1;
			else if(tmpPlural < (tmpMasculine+tmpFeminine+tmpNeutral))
				return -1;
		}		
		return 0;
	}
	
	
	public String getGender(String newString){
		String tmp = this.replaceNumber(newString);
		if(wordMap.containsKey(tmp)){
			OneInst newInst = wordMap.get(tmp);
			int tmpMasculine = newInst.masculine;
			int tmpFeminine = newInst.feminine;
			int tmpNeutral = newInst.neutral;
			if(tmpMasculine > tmpFeminine && tmpMasculine > tmpNeutral)
				return "masculine";
			else if(tmpFeminine > tmpMasculine && tmpFeminine > tmpNeutral)
				return "feminine";
			else if(tmpNeutral > tmpMasculine && tmpNeutral > tmpFeminine)
				return "neutral";
			else
				return "none";
		}
		else
			return "none";
	}
	
	public String getGender(ArrayList<String> newStrings, String headWord){
		int newStringsNumber = newStrings.size();
		String cleanHeadWord = this.replaceNumber(headWord);
		String tmp="";
		for(int i=0; i<newStringsNumber; i++)
			tmp+="+"+this.replaceNumber(newStrings.get(i));
		if(tmp.length()>1)
			tmp = tmp.substring(1);
		if(wordMap.containsKey(tmp)){
			OneInst newInst = wordMap.get(tmp);
			int tmpMasculine = newInst.masculine;
			int tmpFeminine = newInst.feminine;
			int tmpNeutral = newInst.neutral;
			int tmpPlural = newInst.plural;
			if(tmpMasculine > tmpFeminine && tmpMasculine > tmpNeutral)
				return "masculine";
			else if(tmpFeminine > tmpMasculine && tmpFeminine > tmpNeutral)
				return "feminine";
			else if(tmpNeutral > tmpMasculine && tmpNeutral > tmpFeminine)
				return "neutral";
			else
				return "none";
		}
		else if(wordMap.containsKey(cleanHeadWord)){
			OneInst newInst = wordMap.get(cleanHeadWord);
			int tmpMasculine = newInst.masculine;
			int tmpFeminine = newInst.feminine;
			int tmpNeutral = newInst.neutral;
			int tmpPlural = newInst.plural;
			if(tmpMasculine > tmpFeminine && tmpMasculine > tmpNeutral)
				return "masculine";
			else if(tmpFeminine > tmpMasculine && tmpFeminine > tmpNeutral)
				return "feminine";
			else if(tmpNeutral > tmpMasculine && tmpNeutral > tmpFeminine)
				return "neutral";
			else
				return "none";
		}
		else if(endMap.containsKey(this.replaceNumber(this.replaceNumber(newStrings.get(newStringsNumber-1))))
				|| startMap.containsKey(this.replaceNumber(newStrings.get(0))) ) {
			int tmpMasculine = 0;
			int tmpFeminine = 0;
			int tmpNeutral = 0;
			int tmpPlural = 0;
			if(wordMap.containsKey(this.replaceNumber(newStrings.get(newStringsNumber-1)))){			
				OneInst newInst = wordMap.get(this.replaceNumber(newStrings.get(newStringsNumber-1)));
				tmpMasculine += newInst.masculine;
				tmpFeminine += newInst.feminine;
				tmpNeutral += newInst.neutral;
				tmpPlural += newInst.plural;
			}
			else if(wordMap.containsKey(this.replaceNumber(newStrings.get(0)))){
				OneInst newInst = wordMap.get(this.replaceNumber(newStrings.get(0)));
				tmpMasculine += newInst.masculine;
				tmpFeminine += newInst.feminine;
				tmpNeutral += newInst.neutral;
				tmpPlural += newInst.plural;
			}
			if(tmpMasculine > tmpFeminine && tmpMasculine > tmpNeutral)
				return "masculine";
			else if(tmpFeminine > tmpMasculine && tmpFeminine > tmpNeutral)
				return "feminine";
			else if(tmpNeutral > tmpMasculine && tmpNeutral > tmpFeminine)
				return "neutral";
			else
				return "none";
		}
		else
			return "none";
	}
	
	
	
	
	
	
	private String replaceNumber(String inputString){	
		String tmp = inputString;
		if(inputString != null)
			tmp = inputString.replaceAll("[0-9]+", "#");
		return tmp;		
	}
	
	
	private static void test(){
		Gender newGender = new Gender();
		newGender.readFile("/home/tm/conll/test/gender.data");
		ArrayList<String> newStr = new ArrayList<String>();
		newStr.add("mr");newStr.add("Li");
		System.out.println("Mr. Li: "+newGender.getGender(newStr,"mr"));
		System.out.print("a");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.print("! \"senten ce".replace(" ", "+"));
		test();
	}

}
