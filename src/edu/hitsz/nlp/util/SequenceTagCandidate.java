package edu.hitsz.nlp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Map.Entry;

import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.struct.CfgTreeHead.subList;

/**
 * 根据规则选取候选词
 * select the candidate token according to this line, like BEMS, BIO formats
 *
 * @author tm
 *
 * example:
 * 	B	s	B I O
 * 	I	n	B I O
 * 	O	s	B O
 * where s, n represent whether the tag can be the start of the sentence, or not
 */
public class SequenceTagCandidate {

	public HashMap<String,ArrayList<String>> candidates;
	public ArrayList<String> starts;

	public SequenceTagCandidate(){
		candidates = new HashMap<String,ArrayList<String>>();
		starts = new ArrayList<String>();
	}

	public int getTagCandidateNumber(){
		return candidates.size();
	}

	/**
	 * read tag candidate list from file, 从文件中读取tagcandidate类
	 * @param fileName
	 */
	public void readFrom(String fileName){
		ArrayList<String> tempSentence=new ArrayList<String>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read tag candidate file successed:");
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
				String[] words = tempSentence.get(i).split("\t");
				if(words.length!=3){
					System.out.println("the tag candidate file is wrong, 1");
					System.exit(0);
				}
				if(words[1].equals("s"))
					starts.add(words[0]);
				ArrayList<String> newVec = new ArrayList<String>();
				String[] cands = words[2].split(" ");
				if(cands.length<1){
					System.out.println("the tag candidate file is wrong, 2");
					System.exit(0);
				}
				for(int j=0; j<cands.length; j++)
					newVec.add(cands[j]);
				candidates.put(words[0], newVec);
			}
		}

	}

	/**
	 * store tag candidate list, 存储tagcandidate结构到文件中
	 * @param tagCandidateFileName
	 */
	public void store(String tagCandidateFileName){
		StringBuffer newBuffer = new StringBuffer();
		Iterator<Entry<String, ArrayList<String>>> iter = candidates.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, ArrayList<String>> entry = iter.next();
			String curSignal = entry.getKey();
			ArrayList<String> curList = entry.getValue();
			newBuffer.append(curSignal+"\t");
			if(starts.contains(curSignal))
				newBuffer.append("s\t");
			else
				newBuffer.append("n\t");
			if(curList.size()<1){
				System.out.println("generating candidates goes wrong");
				System.exit(-1);
			}
			newBuffer.append(curList.get(0));
			if(curList.size()>1){
				for(int i=1; i<curList.size(); i++)
					newBuffer.append(" "+curList.get(i));
			}
			newBuffer.append("\n");
		}
		try{
			FileWriter outWriter = new FileWriter(tagCandidateFileName);
			outWriter.write(newBuffer.toString());
			outWriter.close();
		}
		catch (IOException e){
			System.out.println("IOException" + e);
		}
	}

	/**
	 * get the start tags, 获取序列起点的标识
	 * @return
	 */
	public ArrayList<String> getStart(){
		return this.starts;
	}

	/**
	 * get the next tag, 根据前一个节点的标识获取下一个节点的标识
	 * @param previousTag
	 * @return
	 */
	public ArrayList<String> getNext(String previousTag){
		if(!candidates.containsKey(previousTag)){
			System.out.println("wrong in previousTag");
			System.exit(-1);
		}
		return candidates.get(previousTag);
	}

	/**
	 * 得到所有tags
	 * @since Aug 12, 2014
	 * @return
	 */
	public ArrayList<String> getAll() {
		ArrayList<String> tags = new ArrayList<String>();
		Iterator<Map.Entry<String, ArrayList<String>>> iter = candidates.entrySet().iterator();
		while(iter.hasNext()) 
			tags.add(iter.next().getKey());
		return tags;
	}
	
	/**
	 * check the tag candidate with input file, 检查tagcandidate类与输入文件的一致性，并输入不一致的标识
	 * @param trainFileName
	 */
	public void checkTagCandidateConsistency(String trainFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(trainFileName, -1);
		int sentenceNumber = newFile.getSentenceNumber();
		for(int i=0; i<sentenceNumber; i++){
			//if(i%1000==0)
			//	System.out.print(i+"...");
			ConllSentence newSentence = newFile.getSentence(i);
			int sentenceLength = newSentence.getSentenceLength();
			int lastPosition = newSentence.getWords().size()-1;
			ArrayList<String> signals= newSentence.getWords(lastPosition);
			if(!starts.contains(signals.get(0))){
				System.out.println("no \""+signals.get(0)+"\" in the starts");
				System.exit(-1);
			}
			for(int j=0; j<signals.size()-1; j++){
				if(!candidates.containsKey(signals.get(j))){
					System.out.println("no \""+signals.get(0)+"\" in the candidates");
					System.exit(-1);
				}
				ArrayList<String> myCand = candidates.get(signals.get(j));
				if(!myCand.contains(signals.get(j+1))){
					System.out.println("no \""+signals.get(j+1)+"\" in the candidates of \""+signals.get(j)+"\"");
					System.exit(-1);
				}
			}
		}
		System.out.println("It's consistency");
	}


	/**
	 * generate tag candidate from file, 从训练文件中生成tagcandidate类
	 * @param trainFileName
	 */
	public void generateFrom(String trainFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(trainFileName, -1);
		int sentenceNumber = newFile.getSentenceNumber();
		for(int i=0; i<sentenceNumber; i++){
			ConllSentence newSentence = newFile.getSentence(i);
			int sentenceLength = newSentence.getSentenceLength();
			if(i>0){
				ArrayList<String> signals= newSentence.getWords(newSentence.getWords().size()-1);
				if(!starts.contains(signals.get(0))){
					starts.add(signals.get(0));
				}
				if(i>1){
					for(int j=0; j<signals.size()-1; j++){
						if(!candidates.containsKey(signals.get(j))){
							candidates.put(signals.get(j), new ArrayList<String>());
						}
						ArrayList<String> myCand = candidates.get(signals.get(j));
						if(!myCand.contains(signals.get(j+1))){
							myCand.add(signals.get(j+1));
							candidates.put(signals.get(j), myCand);
						}
					}
				}
			}
		}
		System.out.println("generate tag candidate done");
	}






}
