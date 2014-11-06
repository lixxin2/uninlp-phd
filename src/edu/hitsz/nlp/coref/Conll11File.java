/**
 * 
 */
package edu.hitsz.nlp.coref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.CfgTreeHead;

/**
 * @author tm
 *
 */
public class Conll11File {

	private int documentNumber;
	private ArrayList<Conll11Document> totalDocument;
	
	public Conll11File(){
		documentNumber = 0;	
		totalDocument = new ArrayList<Conll11Document>();
	}
	
	public int getDocumentNumber(){
		return this.documentNumber;
	}
	
	public Conll11Document getDocument(int i ){
		if(i > documentNumber-1){
			System.out.println("The number i="+i+" is bigger than numbers "+documentNumber+" of document in file");
			System.exit(0);
		}
		return totalDocument.get(i);		
	}
	
	
	/**
	 * read training file, including process the parse, named entities, coreference in sentences, document
	 * @param trainName Name of the Input File
	 * @param size Number of inputing sentences: size=0 denotes all the sentences
	 */
	public void readTrainFile(String trainName, int size){
		if(size==0)
			size=Integer.MAX_VALUE;
		String trainFileName=trainName;
		File file = new File(trainFileName);
		BufferedReader reader = null;
		int stopsignal=0;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read Training file successed:");
			String tempString=null;
			ArrayList<String> tempSentence=new ArrayList<String>();
			try{
				//denote whether the document is read or not
				int sign = 0;
				System.out.print("Have Read ");
				Conll11Document newDocument = null;
				while ((tempString = reader.readLine())!= null && stopsignal==0){
					if(tempString.substring(0,1).equals("#")){
						int tempStringLength = tempString.length();
						//start a document
						if(tempString.startsWith("#begin document")){
							System.out.print("\nDocumentPart:"+totalDocument.size()+", sentences: ");
							documentNumber++;							
							newDocument = new Conll11Document();
							newDocument.setPartName(tempString.substring(tempStringLength-3));
							sign = 1;
						}
						//end a document 
						else if(tempString.equals("#end document")){
							newDocument.setDocumentName(newDocument.getSentence(0).getDocumentID());
							newDocument.setPartNumber(newDocument.getSentence(0).getPartNumber());
							//process document
							newDocument.preProcessCoreference();
							totalDocument.add(newDocument);
							sign = 0;
						}
						continue;
					}
					tempSentence.add(tempString);
					//read a sentence
					tempString = reader.readLine();
					while (tempString!=null&&!tempString.trim().equals("")){
						if(tempString.substring(0,1).equals("#")){
							tempString = reader.readLine();
							continue;
						}
						tempSentence.add(tempString);
						tempString = reader.readLine();
						}
					//process a sentence
					if (tempSentence.size()!=0){
						//System.out.println("Have Read the "+sentenceNumber+"th Sentence;");
						//if(newDocument.sentenceNumber%1000==0)
						System.out.print(newDocument.getSentenceNumber()+",");
						if(newDocument.getSentenceNumber()<size){
							//System.out.println("Have Read "+sentenceNumber+"th Sentences.");
							Conll11Sentence sentence=new Conll11Sentence(newDocument.getSentenceNumber());
							sentence.processTrain(tempSentence);
							//sentence.outputTrainSentence();
							newDocument.addSentence(sentence);							
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}		
				if(sign == 1){
					newDocument.setDocumentName(newDocument.getSentence(0).getDocumentID());
					newDocument.setPartNumber(newDocument.getSentence(0).getPartNumber());
					//process document
					newDocument.preProcessCoreference();
					totalDocument.add(newDocument);
				}
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
	 * read training file, including process the parse, named entities, coreference in sentences, document
	 * @param trainName Name of the Input File
	 * @param newTreeHead Head rule file for cfg tree of each sentence
	 * @param size Number of inputing sentences: size=0 denotes all the sentences
	 */
	public void readTrainFile(String trainName, CfgTreeHead newTreeHead, int size){
		readTrainFile(trainName, size);
		//load tree head for each docuement 
		for(int i=0;i<documentNumber;i++){
			this.totalDocument.get(i).findHead(newTreeHead);
		}		
	}
	
	
	
	
	/**
	 * read test file
	 * @param trainName Name of the Input File
	 * @param size Number of inputing sentences: size=0 denotes all the sentences
	 */
	public void readTestFile(String testName, int size){
		if(size==0)
			size=100000;
		String testFileName=testName;
		File file = new File(testFileName);
		BufferedReader reader = null;
		int stopsignal=0;		
		try{		
			reader = new BufferedReader(new FileReader(file));
			System.out.println("Read Training file successed:");
			String tempString=null;
			ArrayList<String> tempSentence=new ArrayList<String>();
			try{
				int sign = 0;
				System.out.print("Have Read ");
				Conll11Document newDocument = null;
				while ((tempString = reader.readLine())!= null && stopsignal==0){					
					if(tempString.substring(0,1).equals("#")){
						int tempStringLength = tempString.length();
						//start a document
						if(tempString.startsWith("#begin document")){
							System.out.print("\nDocumentPart:"+totalDocument.size()+", sentences: ");
							documentNumber++;							
							newDocument = new Conll11Document();
							newDocument.setPartName(tempString.substring(tempStringLength-3));
							sign = 1;
						}
						//end a document 
						else if(tempString.equals("#end document")){
							newDocument.setDocumentName(newDocument.getSentence(0).getDocumentID());
							newDocument.setPartNumber(newDocument.getSentence(0).getPartNumber());
							//process document
							newDocument.preProcessCoreference();
							totalDocument.add(newDocument);
							sign = 0;
						}
						continue;
					}
					tempSentence.add(tempString);
					//read a sentence
					tempString = reader.readLine();
					while (tempString!=null&&!tempString.trim().equals("")){
						if(tempString.substring(0,1).equals("#")){
							tempString = reader.readLine();
							continue;
						}
						tempSentence.add(tempString);
						tempString = reader.readLine();
						}
					//process a sentence
					if (tempSentence.size()!=0){
						//System.out.println("Have Read the "+sentenceNumber+"th Sentence;");
						//if(newDocument.sentenceNumber%1000==0)
						System.out.print(newDocument.getSentenceNumber()+",");
						if(newDocument.getSentenceNumber()<size){
							//System.out.println("Have Read "+sentenceNumber+"th Sentences.");
							Conll11Sentence sentence=new Conll11Sentence(newDocument.getSentenceNumber());
							sentence.processTest(tempSentence);
							//sentence.outputTrainSentence();
							newDocument.addSentence(sentence);						
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}		
				if(sign == 1){
					newDocument.setDocumentName(newDocument.getSentence(0).getDocumentID());
					newDocument.setPartNumber(newDocument.getSentence(0).getPartNumber());
					//process document
					newDocument.preProcessCoreference();
					totalDocument.add(newDocument);
				}				
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
	 * @param trainName Name of the Input File
	 * @param size Number of inputing sentences: size=0 denotes all the sentences
	 */
	public void readTestFile(String testName, CfgTreeHead newTreeHead, int size){
		readTestFile(testName, size);
		//load tree head file
		for(int i=0;i<documentNumber;i++){
			this.totalDocument.get(i).findHead(newTreeHead);
		}
		//end load
	}
	
	
	/**
	 * extract coreference features
	 * @param features Feature ArrayList
	 * @param outcomes Outcomes
	 */
	public void extractEnglishTrainFeatures(ArrayList<ArrayList<String>> features, ArrayList<String> outcomes, Gender newGender, WordNet newNet){
		if(this.documentNumber>0){
			for(int i=0; i< this.documentNumber; i++)
				this.totalDocument.get(i).extractEnglishTrainFeatures(features, outcomes, newGender, newNet);
		}
	}
	
	/**
	 * extract coreference features
	 * @param features Feature ArrayList
	 * @param outcomes Outcomes
	 * @throws IOException 
	 */
	public void extractEnglishTestFeatures(Gender newGender, WordNet newNet, FileWriter outFileWriter) throws IOException{
		if(this.documentNumber>0){
			for(int i=0; i< this.documentNumber; i++)
				this.totalDocument.get(i).extractEnglishTestFeatures(newGender, newNet, outFileWriter);
		}
	}
	
	
	/**
	 * extract coreference features into a feature file
	 * @param featureName
	 */
	public void extractEnglishTrainFeatures(String featureName){
		try{
			FileWriter newWriter=new FileWriter(featureName);
			ArrayList<ArrayList<String>> features = new ArrayList<ArrayList<String>>();
			ArrayList<String> outcomes = new ArrayList<String>();
			//
			Gender newGender = new Gender();
			newGender.readFile("/home/tm/conll/test/gender.data");
			WordNet newNet = new WordNet();
			this.extractEnglishTrainFeatures(features, outcomes, newGender, newNet);
			int trainNumber = features.size();
			if(trainNumber > 0 && outcomes.size() == trainNumber){
				for(int i=0; i<trainNumber; i++){
					int featureNumber = features.get(0).size();
					if(featureNumber > 0){
						String tmp = "";
						for(int j=0; j<featureNumber; j++)
							tmp += " "+ features.get(i).get(j);
						newWriter.write(outcomes.get(i)+" "+tmp.trim()+"\n");
					}	
				}
			}
			newWriter.close();
			System.out.println("\nStore fea file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	
	public void compTestFile(){
		if(this.documentNumber>0){
			for(int i=0; i< this.documentNumber; i++)
				this.totalDocument.get(i).compTestCoreference();
		}
	}
	

	/**
	 * Store Training File
	 * @param fileName
	 */
	public void storeTrainFile(String fileName)
	{
		try{
			FileWriter outFileWriter=new FileWriter(fileName);
			for(int i=0;i<documentNumber;i++){	
				totalDocument.get(i).storeTrainDocument(outFileWriter);
			}
			outFileWriter.close();
			System.out.println("\nStore train file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	/**
	 * Store Training File
	 */
	public void storeTrainFile(FileWriter outFileWriter) throws IOException
	{
		for(int i=0;i<documentNumber;i++){	
			totalDocument.get(i).storeTrainDocument(outFileWriter);
		}
	}
	
	
	/**
	 * Store Test File
	 * @param fileName
	 */
	public void storeTestFile(String fileName)
	{
		try{
			FileWriter outFileWriter=new FileWriter(fileName);
			for(int i=0;i<documentNumber;i++){	
				totalDocument.get(i).storeTestDocument(outFileWriter);
			}
			outFileWriter.close();
			System.out.println("\nStore train file has done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}
	
	/**
	 * Store Test File
	 * <p> 其中可能需要设置一些参数
	 */
	public void storeTestFile(FileWriter outFileWriter) throws IOException
	{
		for(int i=0;i<documentNumber;i++) {	
			totalDocument.get(i).storeTestDocument(outFileWriter);
		}
	}
	
	
	/**
	 * for program testing
	 */
	private static void testTrain(){			
		Conll11File trainFile=new Conll11File();
		String trainName = "/home/tm/conll/test/1.conll";
		String trainName2="/media/4083BE7D790F6BE0/wsj-23";
		trainFile.readTrainFile(trainName2, 0);		
		
		trainFile.storeTrainFile("/home/tm/conll/test/3.conll");
		trainFile.extractEnglishTrainFeatures("/home/tm/conll/test/1.fea");
	}	
	private static void testTest(){		
		Conll11File testFile=new Conll11File();
		String testName="/home/tm/conll/test/1.conll";
		testFile.readTrainFile(testName, 0);
		
		testFile.compTestFile();
		testFile.storeTestFile("/home/tm/conll/test/test.conll");	
	}
	private static void testFeature(){
		Conll11File trainFile=new Conll11File();
		String trainName = "/home/tm/conll/test/1.gold_conll";
		trainFile.readTrainFile(trainName, 0);	
		trainFile.extractEnglishTrainFeatures("/home/tm/conll/test/1.fea");		
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testTrain();
	}

}
