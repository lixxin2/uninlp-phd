/**
 *
 */
package edu.hitsz.nlp.eisnerdep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import edu.hitsz.nlp.eisnerdep.Conll09Sentence;



/**
 * CoNLL 2009 Shared task
 * @author lee
 * This is Conll2009 Shared Task's file format
 *
 */

public class Conll09File {

	public int sentenceNumber;
	public int unKnownThres=4;
	//public Conll09Sentence[] totalSentence=new Conll09Sentence[40000];
	//"UNKNOWN"
	public Vector<Conll09Sentence> totalSentence;

	public Conll09File(){
		sentenceNumber=0;
		totalSentence=new Vector<Conll09Sentence>();
	}

	/**
	 * read train file
	 * @param fileName
	 * @param size: size=0,
	 */
	public void readTrainFile(String outPath,String trainName, int size){
		if(size==0)
			size=Integer.MAX_VALUE;
		String trainFileName=outPath+trainName;
		File file = new File(trainFileName);
		BufferedReader reader = null;
		int stopsignal=0;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read Training file successed");
			String tempString=null;
			Vector<String> tempSentence=new Vector<String>();
			try{
				//
				while ((tempString = reader.readLine())!= null && stopsignal==0){
					tempSentence.add(tempString);
					//
					tempString = reader.readLine();
					while (!(tempString.trim().equals(""))&&(tempString!=null)){
						tempSentence.add(tempString);
						tempString = reader.readLine();
						}
					//
					if (tempString!=null){
						System.out.println("Have Read "+sentenceNumber+"th Sentences.");
						if(sentenceNumber%1000==0)
							System.out.println("Have Read "+sentenceNumber+"th Sentences.");
						if(sentenceNumber<size){
							Conll09Sentence sentence=new Conll09Sentence();
							sentence.processTrain(tempSentence,tempSentence.size());
							totalSentence.add((Conll09Sentence)sentence.clone());
							sentenceNumber++;
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}
				System.out.println("Have Read "+sentenceNumber+" Sentences.");
				try {
					reader.close();
					}
				catch (IOException e) {
					e.printStackTrace();
					}
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
	 * read test file
	 * @param fileName
	 * @param size size=0
	 */
	public void readTestFile(String outPath,String testName, int size){
		if(size==0)
			size=100000;
		String testFileName=outPath+testName;
		File file = new File(testFileName);
		BufferedReader reader = null;
		int stopsignal=0;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("read file successed");
			String tempString=null;
			Vector<String> tempSentence=new Vector<String>();
			try{
				//
				while ((tempString = reader.readLine()) != null && stopsignal==0){
					tempSentence.add(tempString);
					//
					tempString = reader.readLine();
					while (!(tempString.trim().equals("")) &&(tempString!=null)){
						tempSentence.add(tempString);
						tempString = reader.readLine();
						}
					//
					if (tempString!=null){
						if(sentenceNumber%1000==0)
							System.out.println("Have Read "+sentenceNumber+" Sentences.");
						if(sentenceNumber<size){
							Conll09Sentence sentence=new Conll09Sentence();
							sentence.processTest(tempSentence,tempSentence.size());
							totalSentence.add((Conll09Sentence)sentence.clone());
							sentenceNumber++;
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}
				reader.close();
				System.out.println("Reading "+sentenceNumber+" Sentences is done.");
			}
			catch (FileNotFoundException e) {
				System.err.println(e);
			}
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	//
	public void preNumDel(){
		for(int i=0;i<sentenceNumber;i++){
			for(int j=0;j<totalSentence.get(i).sentenceLength;j++)
				if(totalSentence.get(i).isNumeric(totalSentence.get(i).form[j])){
					totalSentence.get(i).form[j]="000";
					totalSentence.get(i).lemma[j]="000";
				}
		}
	}

	//
	public void preNumAdd(Conll09File file2){
		for(int i=0;i<sentenceNumber;i++){
			for(int j=0;j<totalSentence.get(i).sentenceLength;j++)
				if(totalSentence.get(i).isNumeric(totalSentence.get(i).form[j])){
					totalSentence.get(i).form[j]=file2.totalSentence.get(i).form[j];
					totalSentence.get(i).lemma[j]=file2.totalSentence.get(i).lemma[j];
				}
		}
	}

	//(LeftPT,)RightTP
	public void preRBSub(){
		for(int i=0;i<sentenceNumber;i++){
			for(int j=0;j<totalSentence.get(i).sentenceLength;j++){
				if(totalSentence.get(i).form[j].equals("(")||
						totalSentence.get(i).pos[j].equals("(")){
					totalSentence.get(i).form[j]="-lrb-";
					totalSentence.get(i).lemma[j]="-lrb-";
					totalSentence.get(i).pos[j]="-LRB-";
				}
				else if(totalSentence.get(i).form[j].equals(")")||
						totalSentence.get(i).pos[j].equals(")")){
					totalSentence.get(i).form[j]="-rrb-";
					totalSentence.get(i).lemma[j]="-rrb-";
					totalSentence.get(i).pos[j]="-RRB-";
				}
			}
		}
		System.out.println("Subtitute parentheses to PTwords has done!");
	}

	//LeftPT(,RightTP)
	public void preSubRB(Conll09File file2){
		for(int i=0;i<sentenceNumber;i++){
			for(int j=0;j<totalSentence.get(i).sentenceLength;j++){
				if(totalSentence.get(i).form[j].equals("LeftPT")||
						totalSentence.get(i).form[j].equals("RightPT")){
					totalSentence.get(i).form[j]=file2.totalSentence.get(i).form[j];
					totalSentence.get(i).lemma[j]=file2.totalSentence.get(i).lemma[j];
					totalSentence.get(i).pos[j]=file2.totalSentence.get(i).pos[j];
				}
			}
		}
		System.out.println("Subtitute PTwords to parentheses has done!");
	}

	/**
	 * training file
	 * unKnownThres,UNKNOWN
	 * @param outPath
	 */
	public void preTrainFreqSub(String outPath){
		if(sentenceNumber!=0&&sentenceNumber<25)
			System.out.println("\nThe function 'preProcessTrainFile' may cause HashMap " +
					"problem, because \n\tthe input size is a bit low, and the HashMap " +
					"discard frequency lower than 5.");
		HashMap<String,Integer> wordMap=new HashMap<String,Integer>();
		String wordMapName=outPath+"wordMap.txt";
		String tempString=null;
		//word frequency less than 5 is set to UNKNOWN
		//hashmap
		for(int i=0;i<sentenceNumber;i++)
			for(int j=0;j<totalSentence.get(i).sentenceLength;j++){
				int tempNumber=0;
				String word=totalSentence.get(i).form[j];
				if(wordMap.get(word)==null)
					wordMap.put(word, 1);
				else{
					tempNumber=wordMap.get(word);
					wordMap.remove(word);
					tempNumber++;
					wordMap.put(word,tempNumber);
				}
			}
		System.out.println("read statistic to hashmap done!");
		//hashmap unKnownThres
		Iterator iter;
		ArrayList<Object> wordList=new ArrayList<Object>();
		iter=wordMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
		    Object val = entry.getValue();
		    //System.out.println(key+"\t"+val);
		    if(Integer.parseInt(val.toString())<unKnownThres)
		    	wordList.add(key);
		}
		for(int i=0;i<wordList.size();i++)
			wordMap.remove(wordList.get(i));
		System.out.println("process hashmap done!");
		try{
			FileWriter wordMapWriter=new FileWriter(wordMapName);
			iter=wordMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
			    Object val = entry.getValue();
			    tempString=key.toString()+"\t"+val.toString()+"\n";
			    wordMapWriter.write(tempString);
			}
			wordMapWriter.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
		System.out.println("store hashmap done!");
		//�滻totalSentence�еĴ�
		for(int i=0;i<sentenceNumber;i++)
			for(int j=0;j<totalSentence.get(i).sentenceLength;j++)
				if(wordMap.get(totalSentence.get(i).form[j])==null){
					totalSentence.get(i).form[j]="UNKNOWN";
					totalSentence.get(i).lemma[j]="UNKNOWN";
				}
		System.out.println("Preprocessing training file according to frequency done!");
	}

	/**
	 * test file
	 * train UNKNOWN
	 * @param outPath
	 */
	public void preTestFreqSub(String outPath){
		//�滻Training dataset��û�еĴ�ΪUNKNOWN
		HashMap<String,Integer> wordMap=new HashMap<String,Integer>();
		String wordMapName=outPath+"wordMap.txt";
		String tempString=null;
		BufferedReader reader = null;
		try{
			//���Map�ļ���Map
			reader = new BufferedReader(new FileReader(new File(wordMapName)));
			System.out.println("read HashMap successed");
			try{
				while ((tempString = reader.readLine()) != null){
					String[] words=tempString.split("\t");
					String word=words[0];
					int wordFreq=Integer.parseInt(words[1]);
					wordMap.put(word,wordFreq);
				}
				reader.close();
			}catch (FileNotFoundException e) {
				System.err.println(e);
			}
			for(int i=0;i<sentenceNumber;i++)
				for(int j=0;j<totalSentence.get(i).sentenceLength;j++)
					if(wordMap.get(totalSentence.get(i).form[j])==null){
						totalSentence.get(i).form[j]="UNKNOWN";
						totalSentence.get(i).lemma[j]="UNKNOWN";
						totalSentence.get(i).plemma[j]="UNKNOWN";
					}
			System.out.println("Process test file according to frequency done!");
		}catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	//�洢train�ļ�
	void storeTrainFile(String fileName)
	{
		String trainFileName=fileName;
		try{
			FileWriter outFileWriter=new FileWriter(trainFileName);
			for(int i=0;i<sentenceNumber;i++){
				totalSentence.get(i).storeTrainSentence(outFileWriter);
			}
			outFileWriter.close();
			System.out.println("Store train file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	public void storeTestFile(String fileName)
	{
		String testFileName=fileName;
		try{
			FileWriter outFileWriter=new FileWriter(testFileName);
			for(int i=0;i<sentenceNumber;i++){
				totalSentence.get(i).storeTrainSentence(outFileWriter);
			}
			outFileWriter.close();
			System.out.println("Store test file done!");
		}catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}



	void outputTrainFile(){
		for(int i=0;i<sentenceNumber;i++)
			totalSentence.get(i).outputTrainSentence();
	}

	void outputTestFile(){
		for(int i=0;i<sentenceNumber;i++)
			totalSentence.get(i).outputTestSentence();
	}



/////////////////////////////////////////////////////////////////////////////////////

	//���ͷ��ͬ��POS��ͬ�Ľڵ��Ա��������ѧϰ
	static void statTermHeadForMaxnet(int headsin){
		Conll09File trainFile=new Conll09File();
		Conll09File testFile=new Conll09File();
		String outPath="E:\\codespace\\workspace\\laparser\\";
		String trainName="num-CoNLL2009-ST-Chinese-train.txt";
		String devName="num-CoNLL2009-ST-Chinese-development.txt";
		String testName="num-CoNLL2009-ST-Chinese-Joint.txt";
		String testoodName="preprocess-CoNLL2009-ST-English-Joint-ood.txt";
		String trainHeadStatName="nohead-"+trainName;
		String devHeadStatName="nohead-"+devName;
		String testHeadStatName="nohead-"+testName;
		String testoodHeadStatName="nohead-"+testoodName;
		trainFile.readTrainFile(outPath,trainName, 0);
		try{
			FileWriter outFileWriter=new FileWriter(trainHeadStatName);
			for(int i=0;i<trainFile.sentenceNumber;i++)
				trainFile.totalSentence.get(i).statHeadForMaxentS(outFileWriter,i,headsin);
			outFileWriter.close();
			System.out.println("Store headList file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
		//testFile.preProcessTestFile(path,devName,devNameP,0);
		//trainFile.outputTrainFile();
	}

	//�鿴ͷ��ͬ��POS��ͬ�Ľڵ�
	static void statTermHead(){
		Conll09File trainFile=new Conll09File();
		Conll09File testFile=new Conll09File();
		String outPath="E:\\codespace\\workspace\\laparser\\";
		String trainName="CoNLL2009-ST-English-train.txt";
		String devName="CoNLL2009-ST-English-development.txt";
		String testName="CoNLL2009-ST-English-Joint.txt";
		String testoodName="CoNLL2009-ST-English-Joint-ood.txt";
		String trainNameP="preprocess-"+trainName;
		String devNameP="preprocess-"+devName;
		String testNameP="preprocess-"+testName;
		String testoodNameP="preprocess-"+testoodName;
		trainFile.readTrainFile(outPath, trainName, 0);
		ArrayList<ArrayList> headList=new ArrayList<ArrayList>();
		for(int i=0;i<trainFile.sentenceNumber;i++)
			trainFile.totalSentence.get(i).statSameHead(headList,i);
		trainFile.storeStatHead(headList);
		//testFile.preProcessTestFile(path,devName,devNameP,0);
		//trainFile.outputTrainFile();
	}

	void outputStatHead(ArrayList headList){
		if(headList.size()!=0)
			for(int i=0;i<headList.size();i++)
				if(((ArrayList<String>)headList.get(i)).size()!=0)
					for(int j=0;j<((ArrayList<String>) headList.get(i)).size();j++)
						System.out.println(((ArrayList<String>) headList.get(i)).get(j));
				else
					System.out.println("the "+i+"th arraylist is empty.");
		else
			System.out.println("the head arraylist is empty.");
	}

	void storeStatHead(ArrayList headList){
		Conll09File trainFile=new Conll09File();
		String headStatName="E:\\codespace\\workspace\\laparser\\headStat.txt";
		try{
			FileWriter outFileWriter=new FileWriter(headStatName);
			if(headList.size()!=0)
				for(int i=0;i<headList.size();i++)
					if(((ArrayList<String>)headList.get(i)).size()!=0){
							outFileWriter.write(((ArrayList<String>) headList.get(i)).get(0)+"\n");
							outFileWriter.write(((ArrayList<String>) headList.get(i)).get(1)+"\n");
							outFileWriter.write(((ArrayList<String>) headList.get(i)).get(2)+"\n");
							outFileWriter.write(((ArrayList<String>) headList.get(i)).get(3)+"\n");
					}
			outFileWriter.write("\n\n\n\n\n");
			if(headList.size()!=0)
				for(int i=0;i<headList.size();i++)
					if(((ArrayList<String>)headList.get(i)).size()!=0)
						for(int j=0;j<((ArrayList<String>) headList.get(i)).size();j++)
							outFileWriter.write(((ArrayList<String>) headList.get(i)).get(j)+"\n");

			outFileWriter.close();
			System.out.println("Store headList file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

//////////////////////////////////////////////////////////////////////////





	//
	static void test1(){
		Conll09File trainFile=new Conll09File();
		Conll09File testFile=new Conll09File();
		String outPath="E:\\codespace\\workspace\\laparser\\";
		String trainName="CoNLL2009-ST-English-train.txt";
		String devName="CoNLL2009-ST-English-development.txt";
		String testName="CoNLL2009-ST-English-Joint.txt";
		String testoodName="CoNLL2009-ST-English-Joint-ood.txt";
		String trainNameP="preprocess-"+trainName;
		String devNameP="preprocess-"+devName;
		String testNameP="preprocess-"+testName;
		String testoodNameP="preprocess-"+testoodName;
		trainFile.readTrainFile(outPath, devName, 0);
		trainFile.preRBSub();
		trainFile.storeTrainFile(devNameP);
		//testFile.preProcessTestFile(path,devName,devNameP,0);
		//trainFile.outputTrainFile();
	}

	public static void main(String[] args){

	}


}








