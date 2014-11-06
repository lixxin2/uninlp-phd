package edu.hitsz.nlp.segpos;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.hitsz.ml.onlinelearning.AveragedPerceptron;
import edu.hitsz.nlp.struct.ConllFile;

/**
 * 计算Oracle值
 * @author Xinxin Li
 * Dec 3, 2011
 */
public class Oracle {

	/**
	 * 根据输入 Word Lattice 和 golden 计算Oracle
	 * Dec 3, 2011
	 * @param allWordPos
	 * @param golden
	 * @param length 句子长度
	 * @return
	 */
	public static int compOracleWordPosJiang(ArrayList<ArrayList<BasicWordPos>> allWordPos,
			ArrayList<BasicWordPos> golden, int length){
		int [][][] oracles = new int[length][length+1][length+1];
		for(int i=0; i<length; i++){
			for(int j=0; j<length+1; j++){
				for(int k=0; k<length+1; k++){
					oracles[i][j][k]=-1;
				}
			}
		}
		for(int i=0; i<length; i++){
			for(int j=1; j<=length; j++){
				ArrayList<BasicWordPos> currentWordPos = allWordPos.get(j);
				for(BasicWordPos wordPos : currentWordPos){
					if(wordPos.getStart() == i){
						if(wordPos.isIn(golden)){
							oracles[i][j][1]=1;
							break;
						}
						else{
							oracles[i][j][1]=0;
						}
					}
				}
			}
		}
		//
		for(int i=0; i<length; i++){
			for(int j=1; j<=length; j++){
				for(int k=i+1; k<j; k++){
					for(int m=0; m<length; m++){
						for(int n=0; n<length; n++){
							if(oracles[i][k][m]>=0 && oracles[k][j][n]>=0){
								int tmp = oracles[i][k][m]+oracles[k][j][n];
								if(tmp>oracles[i][j][m+n]){
									oracles[i][j][m+n]=tmp;
								}
							}
						}
					}
				}
			}
		}
		double max = 0;
		int t=0;
		for(int i=0; i<length; i++){
			double f1 = 2.0 * oracles[0][length][i]/((double)i+golden.size());
			if(oracles[0][length][i] >= 0 && f1 > max){
				max = f1;
				t = oracles[0][length][i];
			}
		}
		return t;
	}

	
	public static int compOracleWordPosSimple(ArrayList<ArrayList<BasicWordPos>> allWordPos,
			ArrayList<BasicWordPos> golden){
		int tmp =0;
		step: for(BasicWordPos goldWordPos : golden){
			int end = goldWordPos.getEnd();
			ArrayList<BasicWordPos> oneWordPos = allWordPos.get(end+1);
			for(BasicWordPos predictWordPos : oneWordPos){
				if(goldWordPos.equals(predictWordPos)){
					tmp+=1;
					continue step;
				}
			}
		}
		return tmp;
	}
	


	/** 简单计算word的Oracle值，计算其中相同的词的个数*/
	public static int compOracleWordSimple(ArrayList<ArrayList<BasicWordPos>> allWordPos,
			ArrayList<BasicWordPos> golden){
		int tmp =0;
		step: for(BasicWordPos goldWordPos : golden){
			int end = goldWordPos.getEnd();
			ArrayList<BasicWordPos> oneWordPos = allWordPos.get(end+1);
			if(oneWordPos != null && oneWordPos.size() > 0)	{
				for(BasicWordPos predictWordPos : oneWordPos) {
					if(goldWordPos.wordEquals(predictWordPos)) {
						tmp += 1;
						continue step;
					}
				}
			}
		}
		return tmp;
	}
	
	
	

	/**
	 * statistic about the segmentation agreement in those predicted sequences and golden sequence
	 * 统计预测的两个分词结构的oracle值
	 * @param goldWordVec
	 * @param predictWordVec1
	 * @param predictWordVec2
	 * @return
	 */
	public void compSegOracle(
			HashMap<String, Integer> vTags,
			ArrayList<String> goldWordVec, 
			ArrayList<String> predictWordVec1, 
			ArrayList<String> predictWordVec2,
			int[] number){
		
		//统计每个词的结束位置
		ArrayList<Integer> goldWordInt = new ArrayList<Integer>();
		for(int i=0; i<goldWordVec.size(); i++){
			int wordLength = goldWordVec.get(i).length();
			if(goldWordInt.size() == 0)
				goldWordInt.add(wordLength);
			else
				goldWordInt.add(goldWordInt.get(goldWordInt.size()-1)+wordLength);
		}
		ArrayList<Integer> predictWordInt1 = new ArrayList<Integer>();
		for(int i=0; i<predictWordVec1.size(); i++){
			int wordLength = predictWordVec1.get(i).length();
			if(predictWordInt1.size() == 0)
				predictWordInt1.add(wordLength);
			else
				predictWordInt1.add(predictWordInt1.get(predictWordInt1.size()-1)+wordLength);
		}
		ArrayList<Integer> predictWordInt2 = new ArrayList<Integer>();
		for(int i=0; i<predictWordVec2.size(); i++){
			int wordLength = predictWordVec2.get(i).length();
			if(predictWordInt2.size() == 0)
				predictWordInt2.add(wordLength);
			else
				predictWordInt2.add(predictWordInt2.get(predictWordInt2.size()-1)+wordLength);
		}
		//统计
		int wordSize=goldWordVec.size();
		int predictSize1 = predictWordVec1.size();
		int predictSize2 = predictWordVec2.size();
		int j=0, k=0;
		for(int i=0;i<wordSize;i++){
			//number of correct predicted chunk for each golden chunk
			int correctOneTag=0;
			//gold ArrayList to store one chunk
			String goldWord= goldWordVec.get(i);
			int goldEnd= goldWordInt.get(i);
			String predictWord1 = "";
			int predictEnd1 = -1;
			if(j<predictSize1){
				predictWord1 = predictWordVec1.get(j);
				predictEnd1 = predictWordInt1.get(j);
			}
			String predictWord2 = "";
			int predictEnd2 = -1;
			if(k<predictSize2){
				predictWord2 = predictWordVec2.get(k);
				predictEnd2 = predictWordInt2.get(k);
			}
			//判断当前词为iv或oov
			if(vTags.containsKey(goldWord))
				number[2]++;
			else
				number[3]++;
			if( (goldWord.equals(predictWord1) && goldEnd==predictEnd1)
					|| (goldWord.equals(predictWord2) && goldEnd==predictEnd2)){
				number[1]++;
				if(vTags.containsKey(goldWord))
					number[4]++;
				else
					number[5]++;
			}
			if(predictEnd1 > goldEnd){}
			else if (predictEnd1 == goldEnd) {j++;}
			else{
				while(j<predictSize1 && predictWordInt1.get(j)<=goldEnd)
					j++;
			}
			if(predictEnd2 > goldEnd){}
			else if (predictEnd2 == goldEnd) {k++;}
			else{
				while(k<predictSize2 && predictWordInt2.get(k)<=goldEnd)
					k++;
			}
		}
	}

	
	/**
	 * Computer Oracle from Files,计算两个预测文件的Oracle
	 * @param goldFileName
	 * @param predictFileNames
	 * @throws IOException 
	 */
	public void compSegOracleFromFile(
			String dictFileName,
			String goldFileName, 
			String predictFileName1,  
			String predictFileName2, 
			String agreeFileName) throws IOException{
		
		Evaluation eval = new Evaluation();
		eval.loadIvWords(dictFileName, "seg");
		HashMap<String, Integer> vTags = eval.vTags;
		
		//read golden and predicted tags from files
		ConllFile goldFile=new ConllFile();
		goldFile.readFrom(goldFileName, 0);
		ConllFile predictFile1=new ConllFile();
		predictFile1.readFrom(predictFileName1, 0);
		ConllFile predictFile2=new ConllFile();
		predictFile2.readFrom(predictFileName2, 0);

		int senNumber=goldFile.getSentenceNumber();
		if(senNumber!=predictFile1.getSentenceNumber() || senNumber!=predictFile2.getSentenceNumber()){
			System.out.println("The sentence number is different. Please check");
		}
		int[] allNumber = new int[6];
		//goldNumber, agreeNumber, ivGoldNumber, oovGoldNumber, ivAgreeNumber, oovAgreeNumber
		allNumber[0]=0;allNumber[1]=0;allNumber[2]=0;allNumber[3]=0;allNumber[4]=0;allNumber[5]=0;
		for(int i=0; i<senNumber; i++){
			allNumber[0] += goldFile.getSentence(i).getSentenceLength();
			compSegOracle(vTags,
				goldFile.getSentence(i).getResultSignal(),
				predictFile1.getSentence(i).getResultSignal(),
				predictFile2.getSentence(i).getResultSignal(),
				allNumber);
		}
		try{
			FileWriter outFileWriter=new FileWriter(agreeFileName);
			outFileWriter.write("Agree "+allNumber[1]+" in gold "+ allNumber[0]+", about "+ allNumber[1]/(double)allNumber[0]+"\n");
			outFileWriter.write("IV "+allNumber[4]+" in gold "+ allNumber[2]+", about "+ allNumber[4]/(double)allNumber[2]+"\n");
			outFileWriter.write("OOV "+allNumber[5]+" in gold "+ allNumber[3]+", about "+ allNumber[5]/(double)allNumber[3]+"\n");
			outFileWriter.close();
		}
		catch (Exception e){
			System.out.println();
			System.exit(-1);
		}
	}
	
	

	/**
	 * statistic about the segmentation agreement in those predicted sequences and golden sequence
	 * @param goldWordVec
	 * @param predictWordVec1
	 * @param predictWordVec2
	 * @return
	 */
	public void compSegPosOracle(
			HashMap<String, Integer> vTags,
			ArrayList<String> goldWordVec, 
			ArrayList<String> goldPosVec,
			ArrayList<String> predictWordVec1, 
			ArrayList<String> predictPosVec1,
			ArrayList<String> predictWordVec2, 
			ArrayList<String> predictPosVec2, 
			int[] number){
		
		//统计每个词的结束位置
		//统计每个词的结束位置
		ArrayList<Integer> goldWordInt = new ArrayList<Integer>();
		for(int i=0; i<goldWordVec.size(); i++){
			int wordLength = goldWordVec.get(i).length();
			if(goldWordInt.size() == 0)
				goldWordInt.add(wordLength);
			else
				goldWordInt.add(goldWordInt.get(goldWordInt.size()-1)+wordLength);
		}
		ArrayList<Integer> predictWordInt1 = new ArrayList<Integer>();
		for(int i=0; i<predictWordVec1.size(); i++){
			int wordLength = predictWordVec1.get(i).length();
			if(predictWordInt1.size() == 0)
				predictWordInt1.add(wordLength);
			else
				predictWordInt1.add(predictWordInt1.get(predictWordInt1.size()-1)+wordLength);
		}
		ArrayList<Integer> predictWordInt2 = new ArrayList<Integer>();
		for(int i=0; i<predictWordVec2.size(); i++){
			int wordLength = predictWordVec2.get(i).length();
			if(predictWordInt2.size() == 0)
				predictWordInt2.add(wordLength);
			else
				predictWordInt2.add(predictWordInt2.get(predictWordInt2.size()-1)+wordLength);
		}
		//统计
		int wordSize=goldWordVec.size();
		int predictSize1 = predictWordVec1.size();
		int predictSize2 = predictWordVec2.size();
		int j=0, k=0;
		for(int i=0;i<wordSize;i++){
			//number of correct predicted chunk for each golden chunk
			int correctOneTag=0;
			//gold ArrayList to store one chunk
			String goldWord= goldWordVec.get(i);
			String goldPos = goldPosVec.get(i);
			int goldEnd= goldWordInt.get(i);
			String predictWord1 = "";
			String predictPos1="";
			int predictEnd1 = -1;
			if(j<predictSize1){
				predictWord1 = predictWordVec1.get(j);
				predictPos1 = predictPosVec1.get(j);
				predictEnd1 = predictWordInt1.get(j);
			}
			String predictWord2 = "";
			String predictPos2="";
			int predictEnd2 = -1;
			if(k<predictSize2){
				predictWord2 = predictWordVec2.get(k);
				predictPos2 = predictPosVec2.get(k);
				predictEnd2 = predictWordInt2.get(k);
			}
			//判断当前词为iv或oov
			if(vTags.containsKey(goldWord+Evaluation.delimiter+goldPos))
				number[2]++;
			else
				number[3]++;
			if( (goldWord.equals(predictWord1) && goldPos.equals(predictPos1) && goldEnd==predictEnd1)
					|| (goldWord.equals(predictWord2) && goldPos.equals(predictPos2) && goldEnd==predictEnd2)){
				number[1]++;
				if(vTags.containsKey(goldWord+Evaluation.delimiter+goldPos))
					number[4]++;
				else
					number[5]++;
			}
			if(predictEnd1 > goldEnd){}
			else if (predictEnd1 == goldEnd) {j++;}
			else{
				while(j<predictSize1 && predictWordInt1.get(j)<=goldEnd)
					j++;
			}
			if(predictEnd2 > goldEnd){}
			else if (predictEnd2 == goldEnd) {k++;}
			else{
				while(k<predictSize2 && predictWordInt2.get(k)<=goldEnd)
					k++;
			}
		}
	}


	/**
	 * Computer Tag Agreement from Files,计算相同度
	 * @param goldFileName
	 * @param predictFileName1
	 * @param predictFileName2
	 * @param agreeFileName
	 * @throws IOException 
	 */
	public void compSegPosOracleFromFile(
			String dictFileName,
			String goldFileName, 
			String predictFileName1,  
			String predictFileName2, 
			String agreeFileName) throws IOException{
		
		
		Evaluation eval = new Evaluation();
		eval.loadIvWords(dictFileName, "segpos");
		HashMap<String, Integer> vTags = eval.vTags;
			
		//read golden and predicted tags from files
		ConllFile goldFile=new ConllFile();
		goldFile.readFrom(goldFileName, 0);
		ConllFile predictFile1=new ConllFile();
		predictFile1.readFrom(predictFileName1, 0);
		ConllFile predictFile2=new ConllFile();
		predictFile2.readFrom(predictFileName2, 0);

		int senNumber=goldFile.getSentenceNumber();
		if(senNumber!=predictFile1.getSentenceNumber() || senNumber!=predictFile2.getSentenceNumber()){
			System.out.println("The sentence number is different. Please check");
		}
		
		int[] allWordNumber = new int[6];
		int[] allWordPosNumber = new int[6];
		
		//goldNumber, agreeNumber, ivGoldNumber, oovGoldNumber, ivAgreeNumber, oovAgreeNumber
		allWordNumber[0]=0;allWordNumber[1]=0;allWordNumber[2]=0;
		allWordNumber[3]=0;allWordNumber[4]=0;allWordNumber[5]=0;
		allWordPosNumber[0]=0;allWordPosNumber[1]=0;allWordPosNumber[2]=0;
		allWordPosNumber[3]=0;allWordPosNumber[4]=0;allWordPosNumber[5]=0;
		
		for(int i=0; i<senNumber; i++){
			System.out.println(i);
			if(i==85)
				System.out.println();
			allWordNumber[0] += goldFile.getSentence(i).getSentenceLength();
			allWordPosNumber[0] += goldFile.getSentence(i).getSentenceLength();
			ArrayList<String> goldWord = new ArrayList<String>();
			for(int j=0; j<goldFile.getSentence(i).getSentenceLength(); j++)
				goldWord.add(goldFile.getSentence(i).getWords().get(0).get(j));
			System.out.println(goldWord);
			ArrayList<String> predictWord1 = new ArrayList<String>();
			for(int j=0; j<predictFile1.getSentence(i).getSentenceLength(); j++)
				predictWord1.add(predictFile1.getSentence(i).getWords().get(0).get(j));
			ArrayList<String> predictWord2 = new ArrayList<String>();
			for(int j=0; j<predictFile2.getSentence(i).getSentenceLength(); j++)
				predictWord2.add(predictFile2.getSentence(i).getWords().get(0).get(j));
			compSegOracle(vTags, goldWord, predictWord1, predictWord2, allWordNumber);
			compSegPosOracle(vTags, goldWord, goldFile.getSentence(i).getResultSignal(),
				predictWord1, predictFile1.getSentence(i).getResultSignal(),
				predictWord2, predictFile2.getSentence(i).getResultSignal(),
				allWordPosNumber);
		}
		try{
			FileWriter outFileWriter=new FileWriter(agreeFileName);
			outFileWriter.write("Segmentation:\n");
			outFileWriter.write("Agree "+allWordNumber[1]+" in gold "+ allWordNumber[0]+", about "+ allWordNumber[1]/(double)allWordNumber[0]+"\n");
			outFileWriter.write("IV "+allWordNumber[4]+" in gold "+ allWordNumber[2]+", about "+ allWordNumber[4]/(double)allWordNumber[2]+"\n");
			outFileWriter.write("OOV "+allWordNumber[5]+" in gold "+ allWordNumber[3]+", about "+ allWordNumber[5]/(double)allWordNumber[3]+"\n");
			outFileWriter.write("Segmentation and POS Tagging:\n");
			outFileWriter.write("Agree "+allWordPosNumber[1]+" in gold "+ allWordPosNumber[0]+", about "+ allWordPosNumber[1]/(double)allWordPosNumber[0]+"\n");
			outFileWriter.write("IV "+allWordPosNumber[4]+" in gold "+ allWordPosNumber[2]+", about "+ allWordPosNumber[4]/(double)allWordPosNumber[2]+"\n");
			outFileWriter.write("OOV "+allWordPosNumber[5]+" in gold "+ allWordPosNumber[3]+", about "+ allWordPosNumber[5]/(double)allWordPosNumber[3]+"\n");
			outFileWriter.close();
		}
		catch (Exception e){
			System.out.println();
			System.exit(-1);
		}
	}

	
	
	



}
