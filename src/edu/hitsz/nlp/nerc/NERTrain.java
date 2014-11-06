package edu.hitsz.nlp.nerc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.SequenceTagCandidate;

public class NERTrain {

	static String[] sighanNERTaggers = {"ORG","PER","LOC"};
	static String delimiter = "--";
	ArrayList<String> personSurNamedict;


	/**
	 * 生成候选tag
	 * @param tagCandidateFileName
	 */
	public static void generateCandidateBMESO(String tagCandidateFileName){
		SequenceTagCandidate newCand = new SequenceTagCandidate();
		for(int i=0; i<sighanNERTaggers.length; i++){
			String singleTag ="S-"+sighanNERTaggers[i];
			if(!newCand.candidates.containsKey(singleTag)){
				newCand.starts.add(singleTag);
				ArrayList<String> newVec= new ArrayList<String>();
				for(int j=0; j<sighanNERTaggers.length; j++){
					String tmp = "B-"+sighanNERTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				String tmp = "0";
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				newCand.candidates.put(singleTag, newVec);
			}
		}
		for(int i=0; i<sighanNERTaggers.length; i++){
			String beginTag ="B-"+sighanNERTaggers[i];
			if(!newCand.candidates.containsKey(beginTag)){
				newCand.starts.add(beginTag);
				ArrayList<String> newVec= new ArrayList<String>();
				String tmp = "M-"+sighanNERTaggers[i];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				tmp = "E-"+sighanNERTaggers[i];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				newCand.candidates.put(beginTag, newVec);
			}
		}
		for(int i=0; i<sighanNERTaggers.length; i++){
			String middleTag ="M-"+sighanNERTaggers[i];
			if(!newCand.candidates.containsKey(middleTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				String tmp = "M-"+sighanNERTaggers[i];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				tmp = "E-"+sighanNERTaggers[i];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				newCand.candidates.put(middleTag, newVec);
			}
		}
		for(int i=0; i<sighanNERTaggers.length; i++){
			String endTag ="E-"+sighanNERTaggers[i];
			if(!newCand.candidates.containsKey(endTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				for(int j=0; j<sighanNERTaggers.length; j++){
					String tmp = "B-"+sighanNERTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				for(int j=0; j<sighanNERTaggers.length; j++){
					String tmp = "S-"+sighanNERTaggers[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				String tmp = "0";
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				newCand.candidates.put(endTag, newVec);
			}
		}

		String outTag= "0";
		if(!newCand.candidates.containsKey(outTag)){
			newCand.starts.add(outTag);
			ArrayList<String> newVec= new ArrayList<String>();
			for(int j=0; j<sighanNERTaggers.length; j++){
				String tmp = "B-"+sighanNERTaggers[j];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
			}
			for(int j=0; j<sighanNERTaggers.length; j++){
				String tmp = "S-"+sighanNERTaggers[j];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
			}
			String tmp = "0";
			if(!newVec.contains(tmp))
				newVec.add(tmp);
			newCand.candidates.put(outTag, newVec);
		}
		newCand.store(tagCandidateFileName);
	}

	/**
	 * 将词转换为字符结构，将名实体转换为BMESO表示
	 * @param inFileName
	 * @param outFileName
	 */
	public void trainWord2Character(String inFileName, String outFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(inFileName, -1);
		if(newFile.getSentenceNumber()>0){
			try{
				FileWriter outWriter  = new FileWriter(outFileName);
				int sentenceNumber = newFile.getSentenceNumber();
				for(int i=0; i<sentenceNumber; i++){
					if(i%1000 == 0)
						System.out.println("processing" + i);
					ConllSentence newSentence = newFile.getSentence(i);
					StringBuffer senBuffer = new StringBuffer();
					if(newSentence.getSentenceLength()>0){
						ArrayList<String> form = newSentence.getWords(0);
						ArrayList<String> pos = newSentence.getWords(1);
						ArrayList<String> ne = newSentence.getWords(2);
						for(int m=0; m<form.size(); m++){
							String singleForm = form.get(m);
							String singlePos = pos.get(m);
							String singleNE = ne.get(m);
							int formSize = singleForm.length();
							if(singleNE.equals("0")){
								if(formSize == 1)
									senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"S-"+singlePos+"\t"+singleNE+"\n");
								else{
									senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"B-"+singlePos+"\t"+singleNE+"\n");
									for(int j=1; j<formSize-1; j++)
										senBuffer.append(singleForm.charAt(j)+"\t"+singlePos+"\t"+"M-"+singlePos+"\t"+singleNE+"\n");
									senBuffer.append(singleForm.charAt(formSize-1)+"\t"+singlePos+"\t"+"E-"+singlePos+"\t"+singleNE+"\n");
								}
							}
							else if(singleNE.startsWith("B-")){
								String namedEntityType = singleNE.substring(2);
								//如果下面还有词属于同一个名实体
								if(m+1<form.size() && ne.get(m+1).startsWith("I-")){
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"S-"+singlePos+"\t"+"B-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"B-"+singlePos+"\t"+"B-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+singlePos+"\t"+"M-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+singlePos+"\t"+"E-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
									}
								}
								//单独一个词是名实体
								else{
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"S-"+singlePos+"\t"+"S-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"B-"+singlePos+"\t"+"B-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+singlePos+"\t"+"M-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+singlePos+"\t"+"E-"+singlePos+"\t"+"E-"+namedEntityType+"\n");
									}
								}
							}
							else if(singleNE.startsWith("I-")){
								String namedEntityType = singleNE.substring(2);
								//如果下面还有词属于同一个名实体
								if(m+1<form.size() && ne.get(m+1).startsWith("I-")){
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"S-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"B-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+singlePos+"\t"+"M-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+singlePos+"\t"+"E-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
									}

								}
								else{
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"S-"+singlePos+"\t"+"E-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"B-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+singlePos+"\t"+"M-"+singlePos+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+singlePos+"\t"+"E-"+singlePos+"\t"+"E-"+namedEntityType+"\n");
									}
								}
							}
						}
						senBuffer.append("\n");
					}
					outWriter.write(senBuffer.toString());
				}
				outWriter.close();
				System.out.println("store file done");
			}
			catch (IOException e){
				System.out.println("IOException" + e);
			}
		}
	}


	public void trainWord2CharacterWithoutPOS(String inFileName, String outFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(inFileName, -1);
		if(newFile.getSentenceNumber()>0){
			try{
				FileWriter outWriter  = new FileWriter(outFileName);
				int sentenceNumber = newFile.getSentenceNumber();
				for(int i=0; i<sentenceNumber; i++){
					if(i%1000 == 0)
						System.out.println("processing" + i);
					ConllSentence newSentence = newFile.getSentence(i);
					StringBuffer senBuffer = new StringBuffer();
					if(newSentence.getSentenceLength()>0){
						ArrayList<String> form = newSentence.getWords(0);
						ArrayList<String> ne = newSentence.getWords(2);
						for(int m=0; m<form.size(); m++){
							String singleForm = form.get(m);
							String singleNE = ne.get(m);
							int formSize = singleForm.length();
							if(singleNE.equals("0")){
								if(formSize == 1)
									senBuffer.append(singleForm.charAt(0)+"\t"+"S"+"\t"+singleNE+"\n");
								else{
									senBuffer.append(singleForm.charAt(0)+"\t"+"B"+"\t"+singleNE+"\n");
									for(int j=1; j<formSize-1; j++)
										senBuffer.append(singleForm.charAt(j)+"\t"+"M"+"\t"+singleNE+"\n");
									senBuffer.append(singleForm.charAt(formSize-1)+"\t"+"E"+"\t"+singleNE+"\n");
								}
							}
							else if(singleNE.startsWith("B-")){
								String namedEntityType = singleNE.substring(2);
								//如果下面还有词属于同一个名实体
								if(m+1<form.size() && ne.get(m+1).startsWith("I-")){
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+"S"+"\t"+"B-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+"B"+"\t"+"B-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+"M"+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+"E"+"\t"+"M-"+namedEntityType+"\n");
									}
								}
								//单独一个词是名实体
								else{
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+"S"+"\t"+"S-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+"B"+"\t"+"B-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+"M"+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+"E"+"\t"+"E-"+namedEntityType+"\n");
									}
								}
							}
							else if(singleNE.startsWith("I-")){
								String namedEntityType = singleNE.substring(2);
								//如果下面还有词属于同一个名实体
								if(m+1<form.size() && ne.get(m+1).startsWith("I-")){
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+"S"+"\t"+"M-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+"B"+"\t"+"M-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+"\t"+"M"+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+"E"+"\t"+"M-"+namedEntityType+"\n");
									}

								}
								else{
									if(formSize == 1)
										senBuffer.append(singleForm.charAt(0)+"\t"+"S"+"\t"+"E-"+namedEntityType+"\n");
									else{
										senBuffer.append(singleForm.charAt(0)+"\t"+"B"+"\t"+"M-"+namedEntityType+"\n");
										for(int j=1; j<formSize-1; j++)
											senBuffer.append(singleForm.charAt(j)+"\t"+"M"+"\t"+"M-"+namedEntityType+"\n");
										senBuffer.append(singleForm.charAt(formSize-1)+"\t"+"E"+"\t"+"E-"+namedEntityType+"\n");
									}
								}
							}
						}
						senBuffer.append("\n");
					}
					outWriter.write(senBuffer.toString());
				}
				outWriter.close();
				System.out.println("store file done");
			}
			catch (IOException e){
				System.out.println("IOException" + e);
			}
		}
	}


	/**
	 * 将词转换为字符结构
	 * @param inFileName
	 * @param outFileName
	 */
	public void testWord2Character(String inFileName, String outFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(inFileName, -1);
		if(newFile.getSentenceNumber()>0){
			try{
				FileWriter outWriter  = new FileWriter(outFileName);
				int sentenceNumber = newFile.getSentenceNumber();
				for(int i=0; i<sentenceNumber; i++){
					if(i%1000 == 0)
						System.out.println("processing" + i);
					ConllSentence newSentence = newFile.getSentence(i);
					StringBuffer senBuffer = new StringBuffer();
					if(newSentence.getSentenceLength()>0){
						ArrayList<String> form = newSentence.getWords(0);
						ArrayList<String> pos = newSentence.getWords(1);
						for(int m=0; m<form.size(); m++){
							String singleForm = form.get(m);
							String singlePos = pos.get(m);
							int formSize = singleForm.length();
							if(formSize == 1)
								senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"S-"+singlePos+"\n");
							else{
								senBuffer.append(singleForm.charAt(0)+"\t"+singlePos+"\t"+"B-"+singlePos+"\n");
								for(int j=1; j<formSize-1; j++)
									senBuffer.append(singleForm.charAt(j)+"\t"+singlePos+"\t"+"M-"+singlePos+"\n");
								senBuffer.append(singleForm.charAt(formSize-1)+"\t"+singlePos+"\t"+"E-"+singlePos+"\n");
							}
						}
						senBuffer.append("\n");
					}
					outWriter.write(senBuffer.toString());
				}
				outWriter.close();
				System.out.println("store file done");
			}
			catch (IOException e){
				System.out.println("IOException" + e);
			}
		}
	}



	/**
	 * 将词转换为字符结构
	 */
	public static void testWord2CharacterWithoutPOS(String inFileName, String outFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(inFileName, -1);
		if(newFile.getSentenceNumber()>0){
			try{
				FileWriter outWriter  = new FileWriter(outFileName);
				int sentenceNumber = newFile.getSentenceNumber();
				for(int i=0; i<sentenceNumber; i++){
					if(i%1000 == 0)
						System.out.println("processing" + i);
					ConllSentence newSentence = newFile.getSentence(i);
					StringBuffer senBuffer = new StringBuffer();
					if(newSentence.getSentenceLength()>0){
						ArrayList<String> form = newSentence.getWords(0);
						for(int m=0; m<form.size(); m++){
							String singleForm = form.get(m);
							int formSize = singleForm.length();
							if(formSize == 1)
								senBuffer.append(singleForm.charAt(0)+"\t"+"S"+"\n");
							else{
								senBuffer.append(singleForm.charAt(0)+"\t"+"B"+"\n");
								for(int j=1; j<formSize-1; j++)
									senBuffer.append(singleForm.charAt(j)+"\t"+"M"+"\n");
								senBuffer.append(singleForm.charAt(formSize-1)+"\t"+"E"+"\n");
							}
						}
						senBuffer.append("\n");
					}
					outWriter.write(senBuffer.toString());
				}
				outWriter.close();
				System.out.println("store file done");
			}
			catch (IOException e){
				System.out.println("IOException" + e);
			}
		}
	}


	public static ArrayList<String> testChar2Word(String inFileName, String outFileName){
		ArrayList<String> allNers= new ArrayList<String>();
		File file = new File(inFileName);
		BufferedReader reader = null;
		int stopsignal=0;
		ArrayList<String> allSentence = new ArrayList<String>();
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString=null;
			while ((tempString = reader.readLine())!= null){
				if(tempString.trim().length()==0)
					continue;
				StringBuffer sentence = new StringBuffer();
				String[] word1 = tempString.trim().split("\t");
				String wordleft = word1[0];
				String wordright = word1[word1.length-1];
				String word = (wordleft.split(" "))[0];
				String ner = (wordright.split(" "))[1];
				if(ner.equals("0"))
					sentence.append(word);
				else if(ner.substring(0,1).equals("B")){
					String nerType = ner.substring(2);
					sentence.append("<"+nerType+">"+word);
					String newNer = new String();
					newNer += word;
					while ((tempString = reader.readLine())!= null){
						String[] word2 = tempString.trim().split("\t");
						String word2left = word1[0];
						String word2right = word1[word1.length-1];
						word = (wordleft.split(" "))[0];
						ner = (wordright.split(" "))[1];
					}
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return allNers;
	}



	/**
	 * 抽取特征
	 * @param trainFileName
	 * @param featureFileName
	 * @param isTrainFile
	 * @param isTabbed
	 */
	public void extractFeatures(String trainFileName, String featureFileName, boolean isTrainFile, boolean isTabbed){
		String delimiter;
		if(isTabbed)
			delimiter = "\t";
		else
			delimiter = " ";
		ConllFile newFile = new ConllFile();
		newFile.readFrom(trainFileName, -1);
		int sentenceNumber = newFile.getSentenceNumber();
		if(sentenceNumber>0){
			try{
				FileWriter outWriter  = new FileWriter(featureFileName);
				for(int i=0; i<sentenceNumber; i++){
					if(i%1000 == 0)
						System.out.println("processing" + i);
					ConllSentence newSentence = newFile.getSentence(i);
					StringBuffer feaBuffer = new StringBuffer();
					int sentenceLength = newSentence.getSentenceLength();
					for(int j=0; j<sentenceLength; j++){
						if(isTrainFile)
							feaBuffer.append(newSentence.getResultSignal().get(j)+delimiter);
						String curChar = newSentence.getWords().get(0).get(j);
						String preChar = "NONE";
						if(j>0){
							preChar = newSentence.getWords().get(0).get(j-1);
						}
						String pre2Char = "NONE";
						if(j>1){
							pre2Char = newSentence.getWords().get(0).get(j-2);
						}
						String nextChar = "NONE";
						if(j<sentenceLength-1){
							nextChar = newSentence.getWords().get(0).get(j+1);
						}
						String next2Char = "NONE";
						if(j<sentenceLength-2){
							next2Char = newSentence.getWords().get(0).get(j+2);
						}

						String pre2CurChars = pre2Char + "+"+ curChar;
						String preCurChars = preChar + "+" + curChar;
						String curNextChars = curChar + "+"+ nextChar;
						String curNext2Chars = curChar + "+"+ next2Char;

						int featureSeq = 1;
						feaBuffer.append(featureSeq+++":"+curChar+delimiter);
						feaBuffer.append(featureSeq+++":"+preChar+delimiter);
						feaBuffer.append(featureSeq+++":"+pre2Char+delimiter);
						feaBuffer.append(featureSeq+++":"+nextChar+delimiter);
						feaBuffer.append(featureSeq+++":"+next2Char+delimiter);


						feaBuffer.append("\n");
					}
					outWriter.write(feaBuffer.toString());
				}
				if(isTabbed)
					outWriter.write("\n");
				outWriter.close();
			}
			catch (IOException e){
				System.out.println("IOException" + e);
			}
		}
	}


	/**
	 * 导入带有概率的最大熵结果，根据kbeam得到最后结果
	 * @param testFileName
	 * @param probFileName
	 * @param tagCandidateFileName
	 * @param resultFileName
	 */
	public void kBeamMaximumEntropyZhang(String testFileName, String probFileName, String tagCandidateFileName, String resultFileName){
		ConllFile newFile = new ConllFile();
		newFile.readFrom(testFileName, -1);
		SequenceTagCandidate newTagCand = new SequenceTagCandidate();
		newTagCand.readFrom(tagCandidateFileName);
		int tagNumber  = newTagCand.getTagCandidateNumber();
		int kbeam = tagNumber;
		int probline = 0;
		int senline = 0;
		try{
			StringBuffer tagBuffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(new File(probFileName)));
			String [] neTags = new String[tagNumber];
			boolean tagSignal = false;
			for(int i=0; i<newFile.getSentenceNumber(); i++){
				if(i%1000 == 0)
					System.out.println("the " + i + " sentence");
				int sentenceLength = newFile.getSentence(i).getSentenceLength();
				senline += sentenceLength;
				double[][] tokenProb = new double[sentenceLength][tagNumber];
				int j=0;
				String tempString=null;
				//get the probability
				while(j<sentenceLength && (tempString = reader.readLine()) != null){
					String[] words = tempString.trim().split("\t");
					if(words.length != 2 * tagNumber){
						System.out.println("tag number is wrong");
						System.exit(-1);
					}
					if(tagSignal == false){
						for(int k=0; k<tagNumber; k++){
							neTags[k]= words[2*k];
						}
						tagSignal = true;
					}
					for(int k=0; k<tagNumber; k++)
						tokenProb[j][k] = Double.parseDouble(words[2*k +1]);
					j++;probline++;
				}
				//computer kbeam
				ArrayList<ArrayList<ArrayList<String>>> allArrayList = new ArrayList<ArrayList<ArrayList<String>>>();
				ArrayList<ArrayList<Double>> allWeight = new ArrayList<ArrayList<Double>>();
				for(int k=0; k<=sentenceLength; k++){
					allArrayList.add(new ArrayList<ArrayList<String>>());
					allWeight.add(new ArrayList<Double>());
				}
				//初始化起始节点
				allArrayList.get(0).add(new ArrayList<String>());
				allWeight.get(0).add(0.0);
				//
				for(int k=1; k<=sentenceLength; k++){
					LinkedList<ArrayList<String>> newList = new LinkedList<ArrayList<String>>();
					LinkedList<Double> newWeight = new LinkedList<Double>();
					int m = allWeight.get(k-1).size(); //当前节点的beam数目
					for(int n=0; n<m; n++){
						double preWeight = allWeight.get(k-1).get(n);
						ArrayList<String> preArrayList = allArrayList.get(k-1).get(n);
						for(int o=0; o<tagNumber; o++){
							ArrayList<String> currentArrayList = new ArrayList<String>();
							double currentWeight = Double.MIN_VALUE;
							if(k == 1){
								if(newTagCand.getStart().contains(neTags[o])){
									currentArrayList.add(neTags[o]);
									currentWeight = tokenProb[k-1][o];
								}
							}
							else{
								String preToken = preArrayList.get(preArrayList.size()-1);
								if(newTagCand.candidates.get(preToken).contains(neTags[o])){
									currentArrayList.addAll(preArrayList); currentArrayList.add(neTags[o]);
									currentWeight = preWeight + tokenProb[k-1][o];
								}
							}
							//存储当前序列标识
							if(currentArrayList.size() == k){
								int listLength = newList.size();
								if(listLength == 0){
									newList.add(currentArrayList);
									newWeight.add(currentWeight);
								}
								else{
									boolean addSignal = true;
									for(int p=0; p<Math.min(listLength, kbeam); p++){
										if(currentWeight > newWeight.get(p) ){
											newWeight.add(p, currentWeight);
											newList.add(p,currentArrayList);
											addSignal = false;
											break;
										}
									}
									if(addSignal && listLength < kbeam){
										newWeight.add(currentWeight);
										newList.add(currentArrayList);
									}
								}
							}
						}
					}

					//将最好的kbeam个存到数组
					int minKbeam = Math.min(kbeam, newWeight.size());
					for(int q=0; q<minKbeam; q++){
						allWeight.get(k).add(newWeight.get(q));
						allArrayList.get(k).add(newList.get(q));
					}
					newWeight.clear();
					newList.clear();
				}
				ArrayList<String> best = allArrayList.get(sentenceLength).get(0);
				for(int k=0; k<best.size(); k++){
					if(best.get(k).startsWith("M-"))
						tagBuffer.append(best.get(k).replaceFirst("M-", "I-")+"\n");
					else if(best.get(k).startsWith("E-"))
						tagBuffer.append(best.get(k).replaceFirst("E-", "I-")+"\n");
					else if(best.get(k).startsWith("S-"))
						tagBuffer.append(best.get(k).replaceFirst("S-", "B-")+"\n");

				}
			}
			if(senline != probline){
				System.out.println("number of tokens in test file is different from prob file");
				System.exit(-1);
			}
			else{
				System.out.println("number of tokens in test file is the same as prob file");
			}
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}

	}


	public static void callChineseNER(String infileName, String outfileName, String finalFileName){
		String nerCmd = "/home/tm/workspace/puniml/src/ner/build/exe.linux-i686-2.7/chinese";
		Process process;
		try{
			String cmd = nerCmd+" test viterbi "+infileName+" "+outfileName+" "+finalFileName;
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String str = null;
			while ((str = read.readLine()) != null) {
				System.out.println(str);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}



}
