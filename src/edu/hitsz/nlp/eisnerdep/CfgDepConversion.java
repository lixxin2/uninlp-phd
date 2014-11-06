package edu.hitsz.nlp.eisnerdep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.nlp.struct.CfgNode;
import edu.hitsz.nlp.struct.CfgTree;


/**
 * 短语树结构和依存树结构的相互转换
 * @author tm
 *
 */
public class CfgDepConversion {

	public static String headAdd="+P"; //the denotation added to dependency head for converting to phrase category

	//*****************************************************************
	// begin: conversion of Dependency and CFG

	/**
	 * generate the cfg tree structure from dependency tree structure for Conll09Sentenceת,
	 * in which non-terminal=non-terminal+POS	 * @param sen
	 * @param length
	 * @param signal
	 * @return
	 */
	public CfgNode depToFlatTree(Conll09Sentence sen, int length, int signal){
		CfgNode node=new CfgNode();
		int sin=0;
		CfgNode son=new CfgNode();
		for(int i=0;i<length;i++)
			if(sen.isFather(signal,i))
				sin=1;
		if(sin==1){
			node.setLabel(sen.pos[signal]+headAdd);//+headAdd;
			node.setSeq(signal+1);
			if(signal==0)
				node.setHead(0);
			if(signal>0)
				for(int i=0;i<signal;i++)
					if(sen.head[i].equals(Integer.toString(signal+1))){
						son=depToFlatTree(sen,length,i);
						son.setHead(node.getSeq());
						node.getSonNode().add(son);
					}
			son=depToUnaryTree(sen,length,signal);
			son.setHead(node.getSeq());
			node.getSonNode().add(son);
			if(signal<length-1)
				for(int j=signal+1;j<length;j++)
					if(sen.head[j].equals(Integer.toString(signal+1))){
						son=depToFlatTree(sen,length,j);
						son.setHead(node.getSeq());
						node.getSonNode().add(son);
					}
			return node;
		}
		else{
			son=depToUnaryTree(sen,length,signal);
			son.setHead(node.getSeq());
			return son;
		}
	}

	/**
	 * depToFlatTree()
	 * @param sen
	 * @param length
	 * @param signal
	 * @return
	 */
	private CfgNode depToUnaryTree(Conll09Sentence sen, int length, int signal){
		CfgNode node=new CfgNode();
		node.setLabel(sen.pos[signal]);
		node.setSeq(signal+1);
		CfgNode son=new CfgNode();
		son.setLabel(sen.form[signal]);
		son.setSeq(signal+1);
		node.getSonNode().add(son);
		return node;
	}

	/**
	*/
	public void processCTree(String outPath, String inName, String outName){

		String tempString;
		try{
			String inFileName=outPath+inName;
			BufferedReader newReader=new BufferedReader(new FileReader(new File(inFileName)));
			String outFileName=outPath+outName;
			FileWriter newWriter=new FileWriter(outFileName);
			int count=0;
			while((tempString=newReader.readLine())!=null){
				CfgTree newTree=new CfgTree();
				newTree.load(tempString,0,tempString.length(), true);
				newTree.head.compHead2Tail();
				//newTree.outputCTree();
				newTree.store(newWriter);
				newWriter.write("\n");
				System.out.println(count++);
			}
			newReader.close();
			newWriter.close();
			System.out.println("store CTree file done.");
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * ����,���Ժ���
	 * ��dependency�ļ�ת����PTB�ļ�
	 * Dep2CTree("F:\\codespace\\workspace\\laparser\\",
	 * "preprocess-CoNLL2009-ST-English-train.txt",
	 * "treep-preprocess-CoNLL2009-ST-English-train.txt");
	 * @param outPath
	 * @param inName
	 * @param outName
	 */
	public void Dep2CTree(String outPath,String inName,String outName){
		Conll09File trainFile=new Conll09File();
		trainFile.readTrainFile(outPath, inName, 0);
		//testFile.preProcessTestFile(path,devName,devNameP,0);
		//trainFile.outputTrainFile();
		CfgTree newTree=new CfgTree();
		int signal=0;
		try{
			String outFileName=outPath+outName;
			FileWriter newWriter=new FileWriter(outFileName);
			for(int i=0;i<trainFile.totalSentence.size();i++){
				signal=0;
				for(int j=0;j<trainFile.totalSentence.get(i).sentenceLength;j++)
					if(trainFile.totalSentence.get(i).head[j].equals("0")){
						signal=j;
						break;
					};
				if(i%1000==0)
					System.out.println("Store "+i+"th Ctrees.");
				newTree.head=depToFlatTree(trainFile.totalSentence.get(i),
						trainFile.totalSentence.get(i).sentenceLength,signal);
				//newTree.displayCTree();
				newTree.store(newWriter);
				newWriter.write("\n");
			}
			newWriter.close();
			System.out.println("store CTree file done.");
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
	}


	/**
	 * ��ȡԭʼ�����ļ��Ͷ�Ӧ�����ṹ�������ṹ��Ӧ�������ϵ
	 * cTreeToDep("F:\\codespace\\workspace\\laparser\\",
	 * 				"preprocess-CoNLL2009-ST-English-development.txt",
	 * 				"tree-preprocess-CoNLL2009-ST-English-development.txt",
	 * 				"simple-nohead-num-CoNLL2009-ST-Chinese-Joint.txt",
	 * 				"out-head-num-CoNLL2009-ST-Chinese-Joint.txt")
	 * @param outPath
	 * @param inName
	 * @param inCTreeName
	 * @param inMapName
	 * @param outName
	 */
	public void cTreeToDep(String outPath,String inName,String inCTreeName,String inMapName,String outName){
		//
		Conll09File testFile=new Conll09File();
		testFile.readTestFile(outPath, inName, 0);
		String tempString;
		//
		ArrayList<CfgTree> newList=new ArrayList<CfgTree>();
		try{
			String inFileName=outPath+inCTreeName;
			BufferedReader newReader=new BufferedReader(new FileReader(new File(inFileName)));
			while((tempString=newReader.readLine())!=null){
				CfgTree newTree=new CfgTree();
				newTree.load(tempString,0,tempString.length(), true);
				newList.add(newTree);
			}
			newReader.close();
			System.out.println("load CTree file done.");
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
		System.out.println(testFile.sentenceNumber);
		System.out.println(newList.size());
		//����ͷ����map
		HashMap<String, Double> feaMap=new HashMap<String, Double>();
		try{
			String inFileName=outPath+inMapName;
			BufferedReader newReader=new BufferedReader(new FileReader(new File(inFileName)));
			while((tempString=newReader.readLine())!=null){
				String[] words=tempString.split("\t");
				String word=words[0];
				double wordFreq=Double.parseDouble(words[1]);
				feaMap.put(word,wordFreq);
			}
			newReader.close();
			System.out.println("load CTreeFeaMap file done.");
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
		System.out.println(feaMap.size());
		for(int i=0;i<testFile.sentenceNumber;i++){
			System.out.println(i+"th sentence is processing");
			//testFile.totalSentence.get(i).outputTrainSentence();
			//newList.get(i).outputCTree();
			//newList.get(i).compCTreeSeqAndHead(testFile.totalSentence.get(i), feaMap);
		}
		//newList.get(0).outputCTree();
		//testFile.totalSentence.get(0).outputTrainSentence();
		//
		try{
			String outFileName=outPath+outName;
			FileWriter newWriter=new FileWriter(outFileName);
			for(int i=0;i<testFile.sentenceNumber;i++){
				testFile.totalSentence.get(i).storeTrainSentence(newWriter);
			}
			newWriter.close();
			System.out.println("store CTree file done.");
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
		System.out.println("store processed file all done");

	}

	/**
	 *
	 * exDepHead("F:\\codespace\\workspace\\laparser\\",
	 * 			"preprocess-CoNLL2009-ST-English-train.txt",
	 * 			"headstat-preprocess-CoNLL2009-ST-English-train.txt",
	 * 			"headfea-preprocess-CoNLL2009-ST-English-train.txt");
	 * @param outPath
	 * @param inDepName
	 * @param outHeadStatName
	 * @param outHeadFeaName
	 */
	public void exDepHead(String outPath,String inDepName,String outHeadStatName,String outHeadFeaName){
		Conll09File trainFile=new Conll09File();
		trainFile.readTrainFile(outPath, inDepName, 0);
		//testFile.preProcessTestFile(path,devName,devNameP,0);
		//trainFile.outputTrainFile();
		CfgTree newTree=new CfgTree();
		int signal=0;
		try{
			String headStatName=outPath+outHeadStatName;
			FileWriter headStatWriter=new FileWriter(headStatName);
			String headFeaName=outPath+outHeadFeaName;
			FileWriter headFeaWriter=new FileWriter(headFeaName);
			for(int i=0;i<trainFile.totalSentence.size();i++){
				signal=0;
				for(int j=0;j<trainFile.totalSentence.get(i).sentenceLength;j++)
					if(trainFile.totalSentence.get(i).head[j].equals("0")){
						signal=j;
						break;
					};
				if(i%1000==0)
					System.out.println("Store "+i+"th Ctrees.");
				newTree.head=depToFlatTree(trainFile.totalSentence.get(i),
						trainFile.totalSentence.get(i).sentenceLength,signal);
				newTree.output();

			}

			headStatWriter.close();
			System.out.println("store CTree file done.");
			headFeaWriter.close();
			System.out.println("store CTree file done.");
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
	}

	//****************************************************************
	// end: conversion of Dependency and CFG


}
