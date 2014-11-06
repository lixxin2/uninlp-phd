package edu.hitsz.ml.learning;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;

import edu.hitsz.nlp.chunking.ChunkingEvaluation;
import edu.hitsz.nlp.partofspeech.POSEvaluation;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.util.ArgumentsParser;

/**
 *
 * [Halteren, Zavrel and Daelemans, 1998; Brill and Wu, 1998; Halteren, Zavrel and Daelemans, 2001]
 * @author tm
 *
 */
public class SequenceClassifierCombination {

	String divideSymbol="|||";
	String splitSymbol=",";
	//for TagPair
	HashMap<String,Integer> tagOneTag;
	HashMap<String,Integer> oneTag;
	HashMap<String,Integer> tagTwoTags;
	HashMap<String,Integer> twoTags;
	//for WPDV
	HashMap<String,Integer> wpdvFeatures;
	ArrayList<ArrayList<Integer>> wpdaFeatureTemplate;

	public SequenceClassifierCombination(){
		//for TagPair
		tagOneTag=new HashMap<String,Integer>();
		oneTag=new HashMap<String,Integer>();
		tagTwoTags=new HashMap<String,Integer>();
		twoTags=new HashMap<String,Integer>();
		//for WPDV
		wpdvFeatures=new HashMap<String, Integer>();
		wpdaFeatureTemplate=new ArrayList<ArrayList<Integer>>();
	}

	/**
	 * Simple Voting: Majority, means that all classifiers has equal weight. choose the one with
	 * @param testFiles
	 * @param resultFile
	 */
	public void SimpleVotingMajority(ArrayList<ConllFile> testFiles, ConllFile resultFile){

		int fileNumber=testFiles.size();
		int senNumber=resultFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		for(int m=0;m<senNumber;m++){
			ArrayList<ArrayList<String>> totalSignal=new ArrayList<ArrayList<String>>();
			for(int i=0;i<fileNumber;i++)
				totalSignal.add(testFiles.get(i).getSentence(m).getResultSignal());
			int classifierNumber=totalSignal.size();
			if(classifierNumber<1){
				System.out.println("There are less than one classifier");
				System.exit(1);
			}
			int senLength=totalSignal.get(0).size();
			if(senLength<1){
				System.out.println("Length of the sentence is less than 1");
				System.exit(1);
			}
			resultFile.getSentence(m).getResultSignal().clear();
			HashMap<String,Integer> tmpMap=new HashMap<String,Integer>();
			for(int i=0;i<senLength;i++){
				tmpMap.clear();
				for(int j=0;j<classifierNumber;j++){
					if(tmpMap.containsKey(totalSignal.get(j).get(i))){
						int tmp=tmpMap.get(totalSignal.get(j).get(i));
						tmpMap.put(totalSignal.get(j).get(i), tmp+1);
					}
					else
						tmpMap.put(totalSignal.get(j).get(i), 1);
				}
				Iterator<Entry<String, Integer>> iter=tmpMap.entrySet().iterator();
				int tmp=-1;
				String tmpString=null;
				while(iter.hasNext()){
					Entry<String, Integer> entry=iter.next();
					if(entry.getValue()>tmp){
						tmpString=entry.getKey();
						tmp=entry.getValue();
					}
				}
				resultFile.getSentence(m).getResultSignal().add(tmpString);
			}
		}
	}




	/**
	 *
	 * @param totalSignal
	 * @param totalEval
	 * @param resultSignal
	 */
	public void SimpleVotingTotalPrecision(ArrayList<ArrayList<String>> totalSignal, ArrayList<POSEvaluation> totalEval, ArrayList<String> resultSignal){
		int classifierNumber=totalSignal.size();
		if(classifierNumber<1){
			System.out.println("There are less than one classifier");
			System.exit(1);
		}
		int senLength=totalSignal.get(0).size();
		if(senLength<1){
			System.out.println("Length of the sentence is less than 1");
			System.exit(1);
		}
		if(classifierNumber!=totalEval.size()){
			System.out.println("The number of test results and number of classifiers is different");
			System.exit(1);
		}
		resultSignal.clear();
		HashMap<String,Double> tmpMap=new HashMap<String,Double>();
		for(int i=0;i<senLength;i++){
			tmpMap.clear();
			for(int j=0;j<classifierNumber;j++)
				tmpMap.put(totalSignal.get(j).get(i), 0.0);
			for(int j=0;j<classifierNumber;j++){
				double tmp=tmpMap.get(totalSignal.get(j).get(i));
				tmpMap.put(totalSignal.get(j).get(i), tmp+totalEval.get(j).getTotalPrecision());
			}
			Iterator<Entry<String, Double>> iter=tmpMap.entrySet().iterator();
			double tmp=-1.0;
			String tmpString=null;
			while(iter.hasNext()){
				Entry<String, Double> entry=iter.next();
				if(entry.getValue()>tmp){
					tmpString=entry.getKey();
					tmp=entry.getValue();
				}
			}
			resultSignal.add(tmpString);
		}
	}



	public void SimpleVotingTotalPrecision(ArrayList<ConllFile> trainFiles,ConllFile goldFile,ArrayList<ConllFile> testFiles, ConllFile resultFile){
		int fileNumber=trainFiles.size();
		int senNumber=goldFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		ArrayList<ChunkingEvaluation> totalEval=new ArrayList<ChunkingEvaluation>();
		for(int i=0;i<fileNumber;i++){
			ChunkingEvaluation newEval=new ChunkingEvaluation();
			newEval.eval(trainFiles.get(i), goldFile);
			totalEval.add(newEval);
		}
		senNumber=resultFile.getSentenceNumber();
		for(int m=0;m<senNumber;m++){
			ArrayList<ArrayList<String>> totalSignal=new ArrayList<ArrayList<String>>();
			for(int i=0;i<fileNumber;i++)
				totalSignal.add(testFiles.get(i).getSentence(m).getResultSignal());
			int classifierNumber=totalSignal.size();
			if(classifierNumber<1){
				System.out.println("There are less than one classifier");
				System.exit(1);
			}
			int senLength=totalSignal.get(0).size();
			if(senLength<1){
				System.out.println("Length of the sentence is less than 1");
				System.exit(1);
			}
			if(classifierNumber!=totalEval.size()){
				System.out.println("The number of test results and number of classifiers is different");
				System.exit(1);
			}
			resultFile.getSentence(m).getResultSignal().clear();
			HashMap<String,Double> tmpMap=new HashMap<String,Double>();
			for(int i=0;i<senLength;i++){
				tmpMap.clear();
				for(int j=0;j<classifierNumber;j++)
					tmpMap.put(totalSignal.get(j).get(i), 0.0);
				for(int j=0;j<classifierNumber;j++){
					double tmp=tmpMap.get(totalSignal.get(j).get(i));
					tmpMap.put(totalSignal.get(j).get(i), tmp+totalEval.get(j).getTotalPrecision());
				}
				Iterator<Entry<String, Double>> iter=tmpMap.entrySet().iterator();
				double tmp=-1.0;
				String tmpString=null;
				while(iter.hasNext()){
					Entry<String, Double> entry=iter.next();
					if(entry.getValue()>tmp){
						tmpString=entry.getKey();
						tmp=entry.getValue();
					}
				}
				resultFile.getSentence(m).getResultSignal().add(tmpString);
			}
		}
	}






	public void SimpleVotingTagPrecision(ArrayList<ArrayList<String>> totalSignal, ArrayList<POSEvaluation> totalEval, ArrayList<String> resultSignal){
		int classifierNumber=totalSignal.size();
		if(classifierNumber<1){
			System.out.println("There are less than one classifier");
			System.exit(1);
		}
		int senLength=totalSignal.get(0).size();
		if(senLength<1){
			System.out.println("Length of the sentence is less than 1");
			System.exit(1);
		}
		if(classifierNumber!=totalEval.size()){
			System.out.println("The number of test results and number of classifiers is different");
			System.exit(1);
		}
		resultSignal.clear();
		HashMap<String,Double> tmpMap=new HashMap<String,Double>();
		for(int i=0;i<senLength;i++){
			tmpMap.clear();
			for(int j=0;j<classifierNumber;j++)
				tmpMap.put(totalSignal.get(j).get(i), 0.0);
			for(int j=0;j<classifierNumber;j++){
				double tmp=tmpMap.get(totalSignal.get(j).get(i));
				if(totalEval.get(j).getTags().contains(totalSignal.get(j).get(i))){
					int pos=totalEval.get(j).getTags().indexOf(totalSignal.get(j).get(i));
					tmp+=totalEval.get(j).getTagPrecision().get(pos);
				}
				else
					tmp+=totalEval.get(j).getTotalPrecision();
				tmpMap.put(totalSignal.get(j).get(i), tmp);
			}
			Iterator<Entry<String, Double>> iter=tmpMap.entrySet().iterator();
			double tmp=-1.0;
			String tmpString=null;
			while(iter.hasNext()){
				Entry<String, Double> entry=iter.next();
				if(entry.getValue()>tmp){
					tmpString=entry.getKey();
					tmp=entry.getValue();
				}
			}
			resultSignal.add(tmpString);
		}
	}




	public void SimpleVotingTagPrecision(ArrayList<ConllFile> trainFiles,ConllFile goldFile,ArrayList<ConllFile> testFiles, ConllFile resultFile){
		int fileNumber=trainFiles.size();
		int senNumber=goldFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		ArrayList<ChunkingEvaluation> totalEval=new ArrayList<ChunkingEvaluation>();
		for(int i=0;i<fileNumber;i++){
			ChunkingEvaluation newEval=new ChunkingEvaluation();
			newEval.eval(trainFiles.get(i), goldFile);
			totalEval.add(newEval);
		}
		senNumber=resultFile.getSentenceNumber();
		for(int m=0;m<senNumber;m++){
			ArrayList<ArrayList<String>> totalSignal=new ArrayList<ArrayList<String>>();
			for(int i=0;i<fileNumber;i++)
				totalSignal.add(testFiles.get(i).getSentence(m).getResultSignal());
			int classifierNumber=totalSignal.size();
			if(classifierNumber<1){
				System.out.println("There are less than one classifier");
				System.exit(1);
			}
			int senLength=totalSignal.get(0).size();
			if(senLength<1){
				System.out.println("Length of the sentence is less than 1");
				System.exit(1);
			}
			if(classifierNumber!=totalEval.size()){
				System.out.println("The number of test results and number of classifiers is different");
				System.exit(1);
			}
			resultFile.getSentence(m).getResultSignal().clear();
			HashMap<String,Double> tmpMap=new HashMap<String,Double>();
			for(int i=0;i<senLength;i++){
				tmpMap.clear();
				for(int j=0;j<classifierNumber;j++)
					tmpMap.put(totalSignal.get(j).get(i), 0.0);
				for(int j=0;j<classifierNumber;j++){
					double tmp=tmpMap.get(totalSignal.get(j).get(i));
					if(totalEval.get(j).getTags().contains(totalSignal.get(j).get(i))){
						int pos=totalEval.get(j).getTags().indexOf(totalSignal.get(j).get(i));
						tmp+=totalEval.get(j).getTagPrecision().get(pos);
					}
					else
						tmp+=totalEval.get(j).getTotalPrecision();
					tmpMap.put(totalSignal.get(j).get(i), tmp);
				}
				Iterator<Entry<String, Double>> iter=tmpMap.entrySet().iterator();
				double tmp=-1.0;
				String tmpString=null;
				while(iter.hasNext()){
					Entry<String, Double> entry=iter.next();
					if(entry.getValue()>tmp){
						tmpString=entry.getKey();
						tmp=entry.getValue();
					}
				}
				resultFile.getSentence(m).getResultSignal().add(tmpString);
			}
		}
	}





	public void SimpleVotingPrecisionRecall(ArrayList<ArrayList<String>> totalSignal, ArrayList<POSEvaluation> totalEval, ArrayList<String> resultSignal){
		int classifierNumber=totalSignal.size();
		if(classifierNumber<1){
			System.out.println("There are less than one classifier");
			System.exit(1);
		}
		int senLength=totalSignal.get(0).size();
		if(senLength<1){
			System.out.println("Length of the sentence is less than 1");
			System.exit(1);
		}
		if(classifierNumber!=totalEval.size()){
			System.out.println("The number of test results and number of classifiers is different");
			System.exit(1);
		}
		resultSignal.clear();
		HashMap<String,Double> tmpMap=new HashMap<String,Double>();
		for(int i=0;i<senLength;i++){
			tmpMap.clear();
			for(int j=0;j<classifierNumber;j++)
				tmpMap.put(totalSignal.get(j).get(i), 0.0);
			Iterator<Entry<String, Double>> iter=tmpMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, Double> entry=iter.next();
				String tmpString=entry.getKey();
				double tmp=entry.getValue();
				for(int j=0;j<classifierNumber;j++){
					if(tmpString.equals(totalSignal.get(j).get(i))){
						if(totalEval.get(j).getTags().contains(totalSignal.get(j).get(i))){
							int pos=totalEval.get(j).getTags().indexOf(totalSignal.get(j).get(i));
							tmp+=totalEval.get(j).getTagPrecision().get(pos);
						}
						else
							tmp+=totalEval.get(j).getTotalPrecision();
					}
					else{
						if(totalEval.get(j).getTags().contains(totalSignal.get(j).get(i))){
							int pos=totalEval.get(j).getTags().indexOf(totalSignal.get(j).get(i));
							tmp=tmp+1-totalEval.get(j).getTagRecall().get(pos);
						}
						else
							tmp=tmp+1-totalEval.get(j).getTotalRecall();
					}
				}
				tmpMap.put(tmpString, tmp);
			}
			Iterator<Entry<String, Double>> iter2=tmpMap.entrySet().iterator();
			double tmp=-100000;
			String tmpString=null;
			while(iter2.hasNext()){
				Entry<String, Double> entry2=iter2.next();
				if(entry2.getValue()>tmp){
					tmpString=entry2.getKey();
					tmp=entry2.getValue();
				}
			}
			resultSignal.add(tmpString);
		}
	}




	public void SimpleVotingPrecisionRecall(ArrayList<ConllFile> trainFiles,ConllFile goldFile,ArrayList<ConllFile> testFiles, ConllFile resultFile){
		int fileNumber=trainFiles.size();
		int senNumber=goldFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		ArrayList<POSEvaluation> totalEval=new ArrayList<POSEvaluation>();
		for(int i=0;i<fileNumber;i++){
			POSEvaluation newEval=new POSEvaluation();
			newEval.eval(trainFiles.get(i), goldFile);
			totalEval.add(newEval);
		}
		senNumber=resultFile.getSentenceNumber();
		for(int m=0;m<senNumber;m++){
			ArrayList<ArrayList<String>> totalSignal=new ArrayList<ArrayList<String>>();
			for(int i=0;i<fileNumber;i++)
				totalSignal.add(testFiles.get(i).getSentence(m).getResultSignal());
			int classifierNumber=totalSignal.size();
			if(classifierNumber<1){
				System.out.println("There are less than one classifier");
				System.exit(1);
			}
			int senLength=totalSignal.get(0).size();
			if(senLength<1){
				System.out.println("Length of the sentence is less than 1");
				System.exit(1);
			}
			if(classifierNumber!=totalEval.size()){
				System.out.println("The number of test results and number of classifiers is different");
				System.exit(1);
			}
			resultFile.getSentence(m).getResultSignal().clear();
			HashMap<String,Double> tmpMap=new HashMap<String,Double>();
			for(int i=0;i<senLength;i++){
				tmpMap.clear();
				for(int j=0;j<classifierNumber;j++)
					tmpMap.put(totalSignal.get(j).get(i), 0.0);
				Iterator<Entry<String, Double>> iter=tmpMap.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String, Double> entry=iter.next();
					String tmpString=entry.getKey();
					double tmp=entry.getValue();
					for(int j=0;j<classifierNumber;j++){
						if(tmpString.equals(totalSignal.get(j).get(i))){
							if(totalEval.get(j).getTags().contains(totalSignal.get(j).get(i))){
								int pos=totalEval.get(j).getTags().indexOf(totalSignal.get(j).get(i));
								tmp+=totalEval.get(j).getTagPrecision().get(pos);
							}
							else
								tmp+=totalEval.get(j).getTotalPrecision();
						}
						else{
							if(totalEval.get(j).getTags().contains(totalSignal.get(j).get(i))){
								int pos=totalEval.get(j).getTags().indexOf(totalSignal.get(j).get(i));
								tmp=tmp+1-totalEval.get(j).getTagRecall().get(pos);
							}
							else
								tmp=tmp+1-totalEval.get(j).getTotalRecall();
						}
					}
					tmpMap.put(tmpString, tmp);
				}
				Iterator<Entry<String, Double>> iter2=tmpMap.entrySet().iterator();
				double tmp=-100000;
				String tmpString=null;
				while(iter2.hasNext()){
					Entry<String, Double> entry2=iter2.next();
					if(entry2.getValue()>tmp){
						tmpString=entry2.getKey();
						tmp=entry2.getValue();
					}
				}
				resultFile.getSentence(m).getResultSignal().add(tmpString);
			}
		}
	}





	public void tagPairTrain(ArrayList<ConllFile> trainFiles,ConllFile goldFile){
		int fileNumber=trainFiles.size();
		int senNumber=goldFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		for(int i=0;i<senNumber;i++){
			//to computer Vi,j(tag,tok)
			int senLength=goldFile.getSentence(i).getSentenceLength();
			for(int j=0;j<senLength;j++){
				String correctTag=goldFile.getSentence(i).getResultSignal().get(j);
				//to computer tag/OneTag, which is p(tag|S(tag))=N(tag,S(tag)|S(tag))
				for(int k=0;k<fileNumber;k++){
					String predictedTag=trainFiles.get(k).getSentence(i).getResultSignal().get(j);
					//add oneTag, tagOneTag to hashmap
					String tmpString=k+"+"+predictedTag;
					if(oneTag.containsKey(tmpString)){
						int tmp=oneTag.get(tmpString);
						oneTag.put(tmpString, tmp+1);
					}
					else
						oneTag.put(tmpString,1);
					String tmpString2=correctTag+divideSymbol+tmpString;
					if(tagOneTag.containsKey(tmpString2)){
						int tmp=tagOneTag.get(tmpString2);
						tagOneTag.put(tmpString2, tmp+1);
					}
					else
						tagOneTag.put(tmpString2, 1);
				}
				for(int m=0;m<fileNumber;m++)
					for(int n=m+1;n<fileNumber;n++){
						String predictedTag1=trainFiles.get(m).getSentence(i).getResultSignal().get(j);
						String predictedTag2=trainFiles.get(n).getSentence(i).getResultSignal().get(j);
						//add twoTags, tagTwoTags to hashmap
						String tmpString3=m+"+"+predictedTag1+splitSymbol+n+"+"+predictedTag2;
						if(twoTags.containsKey(tmpString3)){
							int tmp=twoTags.get(tmpString3);
							twoTags.put(tmpString3, tmp+1);
						}
						else
							twoTags.put(tmpString3,1);
						String tmpString4=correctTag+divideSymbol+tmpString3;
						if(tagTwoTags.containsKey(tmpString4)){
							int tmp=tagTwoTags.get(tmpString4);
							tagTwoTags.put(tmpString4, tmp+1);
						}
						else
							tagTwoTags.put(tmpString4, 1);
					}
			}
		}
	}

	public void tagPairPredict(ArrayList<ArrayList<String>> totalSignal, ArrayList<String> allSignals, ArrayList<String> resultSignal){
		int classifierNumber=totalSignal.size();
		if(classifierNumber<1){
			System.out.println("There are less than one classifier");
			System.exit(1);
		}
		int senLength=totalSignal.get(0).size();
		if(senLength<1){
			System.out.println("Length of the sentence is less than 1");
			System.exit(1);
		}
		resultSignal.clear();
		for(int i=0;i<senLength;i++){
			double lowValue=-10000;
			String tmpTag=null;
			//for every possible tag
			for(String currentTag : allSignals){
				//computer the voting value of this tag
				double tmp=0;
				for(int j=0;j<classifierNumber;j++)
					for(int k=j+1;k<classifierNumber;k++){
						String predictedTag1=totalSignal.get(j).get(i);
						String predictedTag2=totalSignal.get(k).get(i);
						String tmpString=j+"+"+predictedTag1+splitSymbol+k+"+"+predictedTag2;
						if(twoTags.containsKey(tmpString))
						{
							String tmpString2=currentTag+divideSymbol+tmpString;
							if(tagTwoTags.containsKey(tmpString2)){
								tmp+=tagTwoTags.get(tmpString2)/(double)twoTags.get(tmpString);
							}
						}
						else{
							String tmpString3=j+"+"+predictedTag1;
							String tmpString4=predictedTag1+divideSymbol+currentTag;
							if(tagOneTag.containsKey(tmpString4)){
								tmp+=0.5*tagOneTag.get(tmpString4)/(double)oneTag.get(tmpString3);
							}
							String tmpString5=k+"+"+predictedTag2;
							String tmpString6=predictedTag2+divideSymbol+currentTag;
							if(tagOneTag.containsKey(tmpString6)){
								tmp+=0.5*tagOneTag.get(tmpString6)/(double)oneTag.get(tmpString5);
							}
						}
					}
				if(tmp>lowValue){
					tmpTag=currentTag;
					lowValue=tmp;
				}
			}
			resultSignal.add(tmpTag);
		}
	}


	public void tagPairPredict(ArrayList<String> resultSignal, ArrayList<ConllFile> testFiles, ConllFile resultFile){
		int fileNumber=testFiles.size();
		int senNumber=resultFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		for(int m=0;m<senNumber;m++){
			ArrayList<ArrayList<String>> totalSignal=new ArrayList<ArrayList<String>>();
			for(int i=0;i<fileNumber;i++)
				totalSignal.add(testFiles.get(i).getSentence(m).getResultSignal());
			int classifierNumber=totalSignal.size();
			if(classifierNumber<1){
				System.out.println("There are less than one classifier");
				System.exit(1);
			}
			int senLength=totalSignal.get(0).size();
			if(senLength<1){
				System.out.println("Length of the sentence is less than 1");
				System.exit(1);
			}
			resultFile.getSentence(m).getResultSignal().clear();
			for(int i=0;i<senLength;i++){
				double lowValue=-10000;
				String tmpTag=null;
				//for every possible tag
				for(String currentTag : resultSignal){
					//computer the voting value of this tag
					double tmp=0;
					for(int j=0;j<classifierNumber;j++)
						for(int k=j+1;k<classifierNumber;k++){
							String predictedTag1=totalSignal.get(j).get(i);
							String predictedTag2=totalSignal.get(k).get(i);
							String tmpString=j+"+"+predictedTag1+splitSymbol+k+"+"+predictedTag2;
							if(twoTags.containsKey(tmpString))
							{
								String tmpString2=currentTag+divideSymbol+tmpString;
								if(tagTwoTags.containsKey(tmpString2)){
									tmp+=tagTwoTags.get(tmpString2)/(double)twoTags.get(tmpString);
								}
							}
							else{
								String tmpString3=j+"+"+predictedTag1;
								String tmpString4=predictedTag1+divideSymbol+currentTag;
								if(tagOneTag.containsKey(tmpString4)){
									tmp+=0.5*tagOneTag.get(tmpString4)/(double)oneTag.get(tmpString3);
								}
								String tmpString5=k+"+"+predictedTag2;
								String tmpString6=predictedTag2+divideSymbol+currentTag;
								if(tagOneTag.containsKey(tmpString6)){
									tmp+=0.5*tagOneTag.get(tmpString6)/(double)oneTag.get(tmpString5);
								}
							}
						}
					if(tmp>lowValue){
						tmpTag=currentTag;
						lowValue=tmp;
					}
				}
				resultFile.getSentence(m).getResultSignal().add(tmpTag);
			}
		}
	}



	/**
	 * Feature Template of wpdv algorithm
	 * @param fileNumber
	 */
	public void wpdvGenerateTemplate(int fileNumber){
		//template file: ArrayList[0,0,1:0,1,1] represents combination of two feature
		//feature [0,0,1] represent 0th file 0th row 1th column
		//base tag features
		//full arrangement with increment
		ArrayList<ArrayList<Integer>> baseTagVec=new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> oldVec=new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> newVec=new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> wordVec=new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> contextVec=new ArrayList<ArrayList<Integer>>();
		int basei=0;
		for(;basei<fileNumber;basei++){
			ArrayList<Integer> singleFea=new ArrayList<Integer>();
			singleFea.clear();
			singleFea.add(basei);singleFea.add(0);singleFea.add(-1);
			oldVec.add(singleFea);
			baseTagVec.add(singleFea);
		}
		int sig=1;
		int sigNum=baseTagVec.size();
		while(sig==1||sigNum!=baseTagVec.size()){
			sig=0;
			sigNum=baseTagVec.size();
			int oldNum=oldVec.size();
			for(int i=0;i<oldNum;i++){
				int newVecNum=oldVec.get(i).size();
				int j=oldVec.get(i).get(newVecNum-3)+1;
				while(j<fileNumber){
					ArrayList<Integer> tmpVec=new ArrayList<Integer>();
					tmpVec.addAll(oldVec.get(i));
					//for(int k=0;k<newVecNum;k++)
					//	tmpVec.add(oldVec.get(i).get(k));
					tmpVec.add(j);tmpVec.add(0);tmpVec.add(-1);
					newVec.add(tmpVec);
					baseTagVec.add(tmpVec);
					j++;
				}
			}
			oldVec.clear();
			oldVec.addAll(newVec);
			newVec.clear();
		}
		//Tags+word
		for(ArrayList<Integer> oneVec : baseTagVec){
			ArrayList<Integer> tmpVec=new ArrayList<Integer>();
			tmpVec.clear();
			tmpVec.addAll(oneVec);
			tmpVec.add(0);tmpVec.add(0);tmpVec.add(0);
			wordVec.add(tmpVec);
		}
		//Tags+contexts
		ArrayList<Integer> tmpVec=new ArrayList<Integer>();
		tmpVec.clear();
		for(int i=0;i<fileNumber;i++){
			tmpVec.add(i);tmpVec.add(-1);tmpVec.add(-1);
		}
		contextVec.add(tmpVec);
		ArrayList<Integer> tmpVec2=new ArrayList<Integer>();
		tmpVec2.clear();
		for(int i=0;i<fileNumber;i++){
			tmpVec2.add(i);tmpVec2.add(1);tmpVec2.add(-1);
		}
		contextVec.add(tmpVec2);
		//conclude
		wpdaFeatureTemplate.addAll(baseTagVec);
		wpdaFeatureTemplate.addAll(wordVec);
		wpdaFeatureTemplate.addAll(contextVec);
	}


	//The Weighted Probability Distribution Voting training
	public void wpdvTrain(ArrayList<ConllFile> trainFiles,ConllFile goldFile){
		int fileNumber=trainFiles.size();
		int senNumber=goldFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		for(int i=0;i<senNumber;i++){
			int senLength=goldFile.getSentence(i).getSentenceLength();
			for(int j=0;j<senLength;j++){
				int templateNumber=wpdaFeatureTemplate.size();
				for(int k=0;k<templateNumber;k++){
					ArrayList<Integer> tmp=wpdaFeatureTemplate.get(k);
					String fea=k+":";
					int subFeaNum=tmp.size()/3;
					for(int m=0;m<subFeaNum;m++){
						int subFileNum=tmp.get(3*m);
						int row=tmp.get(3*m+1);
						int column=tmp.get(3*m+2);
						if(j+row>=0&&j+row<senLength){
							if(column==-1)
								fea+=trainFiles.get(subFileNum).getSentence(i).getResultSignal().get(j+row)+splitSymbol;
							else
								fea+=trainFiles.get(subFileNum).getSentence(i).getWords().get(column).get(j+row)+splitSymbol;
						}
						else
							fea+="NONE"+splitSymbol;
					}
					fea=fea.substring(0,fea.length()-1);
					//
					if(wpdvFeatures.containsKey(fea)){
						int tmpValue=wpdvFeatures.get(fea);
						wpdvFeatures.put(fea, tmpValue+1);
					}
					else
						wpdvFeatures.put(fea, 1);
					fea+=divideSymbol+goldFile.getSentence(i).getResultSignal().get(j);
					if(wpdvFeatures.containsKey(fea)){
						int tmpValue=wpdvFeatures.get(fea);
						wpdvFeatures.put(fea, tmpValue+1);
					}
					else
						wpdvFeatures.put(fea, 1);
				}
			}
		}
		/*
		int tempNumber;
		Iterator iter=wpdvFeatures.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String fea=(String) entry.getKey();
		    int feaNum=(Integer) entry.getValue();
		}
		*/
	}


	public void wpdvPredict(ArrayList<String> resultSignal, ArrayList<ConllFile> testFiles, ConllFile resultFile){
		int fileNumber=testFiles.size();
		int senNumber=resultFile.getSentenceNumber();
		if(fileNumber<2||senNumber<1){
			System.out.println("There is something wrong with the \"fileNumber\" or \"senNumber\"");
			System.exit(1);
		}
		int templateNumber=wpdaFeatureTemplate.size();
		for(int i=0;i<senNumber;i++){
			int senLength=resultFile.getSentence(i).getSentenceLength();
			for(int j=0;j<senLength;j++){
				double lowValue=-10000;
				String tmpTag=null;
				//for every possible tag
				for(String currentTag : resultSignal){
					//computer the  value of this tag
					double tmpNum=0;
					for(int k=0;k<templateNumber;k++){
						ArrayList<Integer> tmp=wpdaFeatureTemplate.get(k);
						String fea=k+":";
						int subFeaNum=tmp.size()/3;
						for(int m=0;m<subFeaNum;m++){
							int subFileNum=tmp.get(3*m);
							int row=tmp.get(3*m+1);
							int column=tmp.get(3*m+2);
							if(j+row>=0&&j+row<senLength){
								if(column==-1)
									fea+=testFiles.get(subFileNum).getSentence(i).getResultSignal().get(j+row)+splitSymbol;
								else
									fea+=testFiles.get(subFileNum).getSentence(i).getWords().get(column).get(j+row)+splitSymbol;
							}
							else
								fea+="NONE"+splitSymbol;
						}
						fea=fea.substring(0,fea.length()-1);
						String feaTag=fea+divideSymbol+currentTag;
						if(wpdvFeatures.containsKey(feaTag))
							tmpNum+=wpdvFeatures.get(feaTag)/wpdvFeatures.get(fea);
					}
					if(tmpNum>lowValue){
						tmpTag=currentTag;
						lowValue=tmpNum;
					}
				}
				resultFile.getSentence(i).getResultSignal().add(tmpTag);
			}
		}
	}





	public static void test(){
		SequenceClassifierCombination newComb=new SequenceClassifierCombination();
		ConllFile c=new ConllFile();
		c.readFrom("/mnt/d5/experiments/joint/0", 0);
		ConllFile c1=new ConllFile();
		c1.readFrom("/mnt/d5/experiments/joint/1", 0);
		ConllFile c2=new ConllFile();
		c2.readFrom("/mnt/d5/experiments/joint/2", 0);
		ConllFile c3=new ConllFile();
		c3.readFrom("/mnt/d5/experiments/joint/3", 0);
		ConllFile c4=new ConllFile();
		c4.readFrom("/mnt/d5/experiments/joint/4", 0);
		ArrayList<ConllFile> fileVec=new ArrayList<ConllFile>();
		fileVec.add(c1);fileVec.add(c2);fileVec.add(c3);fileVec.add(c4);
		int senNumber=c.getSentenceNumber();


		//SimpleVotingMajority
		/*
		newComb.SimpleVotingMajority(fileVec, c);
		c.storeSenResultFile("/mnt/d5/experiments/joint/6");
		*/



		//SimpleVotingTotalPrecision
		/*
		SequenceEvalSingle e1=new SequenceEvalSingle();
		e1.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/1");
		SequenceEvalSingle e2=new SequenceEvalSingle();
		e2.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/2");
		SequenceEvalSingle e3=new SequenceEvalSingle();
		e3.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/3");
		SequenceEvalSingle e4=new SequenceEvalSingle();
		e4.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/4");
		ArrayList newEval=new ArrayList();
		newEval.add(e1);newEval.add(e2);newEval.add(e3);newEval.add(e4);
		for(int i=0;i<senNumber;i++){
			ArrayList newVec=new ArrayList();
			newVec.add(c1.totalSentence.get(i).senResultSignal);
			newVec.add(c2.totalSentence.get(i).senResultSignal);
			newVec.add(c3.totalSentence.get(i).senResultSignal);
			newVec.add(c4.totalSentence.get(i).senResultSignal);
			newComb.SimpleVotingTotalPrecision(newVec,newEval,c.totalSentence.get(i).senResultSignal);
		}
		c.storeSenResultFile("/mnt/d5/experiments/joint/5");

		newComb.SimpleVotingTotalPrecision(fileVec,c,fileVec,c);
		c.storeSenResultFile("/mnt/d5/experiments/joint/6");
		*/



		//SimpleVotingTagPrecision
		/*
		SequenceEvalSingle e1=new SequenceEvalSingle();
		e1.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/1");
		SequenceEvalSingle e2=new SequenceEvalSingle();
		e2.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/2");
		SequenceEvalSingle e3=new SequenceEvalSingle();
		e3.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/3");
		SequenceEvalSingle e4=new SequenceEvalSingle();
		e4.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/4");
		ArrayList newEval=new ArrayList();
		newEval.add(e1);newEval.add(e2);newEval.add(e3);newEval.add(e4);
		for(int i=0;i<senNumber;i++){
			ArrayList newVec=new ArrayList();
			newVec.add(c1.totalSentence.get(i).senResultSignal);
			newVec.add(c2.totalSentence.get(i).senResultSignal);
			newVec.add(c3.totalSentence.get(i).senResultSignal);
			newVec.add(c4.totalSentence.get(i).senResultSignal);
			newComb.SimpleVotingTagPrecision(newVec,newEval,c.totalSentence.get(i).senResultSignal);
		}
		c.storeSenResultFile("/mnt/d5/experiments/joint/5");

		newComb.SimpleVotingTagPrecision(fileVec,c,fileVec,c);
		c.storeSenResultFile("/mnt/d5/experiments/joint/6");
		*/


		/*
		//SimpleVotingPrecisionRecall
		SequenceEvalSingle e1=new SequenceEvalSingle();
		e1.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/1");
		SequenceEvalSingle e2=new SequenceEvalSingle();
		e2.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/2");
		SequenceEvalSingle e3=new SequenceEvalSingle();
		e3.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/3");
		SequenceEvalSingle e4=new SequenceEvalSingle();
		e4.compEvalFromFile("/mnt/d5/experiments/joint/0", "/mnt/d5/experiments/joint/4");
		ArrayList newEval=new ArrayList();
		newEval.add(e1);newEval.add(e2);newEval.add(e3);newEval.add(e4);
		for(int i=0;i<senNumber;i++){
			ArrayList newVec=new ArrayList();
			newVec.add(c1.totalSentence.get(i).senResultSignal);
			newVec.add(c2.totalSentence.get(i).senResultSignal);
			newVec.add(c3.totalSentence.get(i).senResultSignal);
			newVec.add(c4.totalSentence.get(i).senResultSignal);
			newComb.SimpleVotingPrecisionRecall(newVec,newEval,c.totalSentence.get(i).senResultSignal);
		}
		c.storeSenResultFile("/mnt/d5/experiments/joint/5");
		//
		newComb.SimpleVotingPrecisionRecall(fileVec,c,fileVec,c);
		c.storeSenResultFile("/mnt/d5/experiments/joint/6");
		*/

		/*
		//TagPair
		newComb.tagPairTrain(fileVec,c);
		String signalName="chunksignal";
		c.readResultSignal("/windows/F/experiments/joint2/",signalName);
		for(int i=0;i<senNumber;i++){
			ArrayList newVec=new ArrayList();
			newVec.add(c1.totalSentence.get(i).senResultSignal);
			newVec.add(c2.totalSentence.get(i).senResultSignal);
			newVec.add(c3.totalSentence.get(i).senResultSignal);
			newVec.add(c4.totalSentence.get(i).senResultSignal);
			newComb.tagPairPredict(newVec, c.resultSignal, c.totalSentence.get(i).senResultSignal);
		}
		c.storeSenResultFile("/windows/F/experiments/joint2/", "111");
		*/


		//wpdv
		/*
		newComb.wpdvGenerateTemplate(fileVec.size());
		newComb.wpdvTrain(fileVec,c);
		*/
	}



	static void usage(){
		System.out.println("Usage:");
		System.out.println("  Help: -h");
		System.out.println("  parameters: -n fileNumber -t trainFiles [-g goldFile] -s strategy -p testFiles -r resultFile");
		System.out.println("    fileNumber: number of trainingFiles, which is same as the number of testFiles, and classifiers");
		System.out.println("    trainFiles: trainFile1-AND-trainFile2-AND-trainFile3-AND-...-AND-trainFilen");
		System.out.println("    goldFile: the corresponding golden file of trainFiles");
		System.out.println("    strategy: strategy of classifier combination, including majority, totalprecision, tagrecision, precisionrecall,tagpair, wpdv");
		System.out.println("    testFiles: testFile1-AND-testFile2-AND-testFile3-AND-...-AND-testFilen");
		System.out.println("    resultFile: the resulting file of testFiles");
		System.exit(1);
	}


	/*
	static void oneExample(String[] args){

		SequenceClassifierCombination newComb=new SequenceClassifierCombination();

		int fileNumber=0;
		String trainFilesName="";
		String goldFileName="";
		String strategy="";
		String testFilesName="";
		String resultFileName="";

		String shortArgs="hn:t:g:s:p:r:";
		ArgumentsParser newParser=new ArgumentsParser();
		newParser.parseCmdLine(args, shortArgs);

		if(newParser.containsOption("h"))
			usage();
		else if(newParser.containsArgument("n")&&newParser.containsArgument("t")&&newParser.containsArgument("g")
				&&newParser.containsArgument("s")&&newParser.containsArgument("p")&&newParser.containsArgument("r")){
			fileNumber=Integer.parseInt(newParser.getArgument("n"));
			trainFilesName=newParser.getArgument("t");
			goldFileName=newParser.getArgument("g");
			strategy=newParser.getArgument("s");
			testFilesName=newParser.getArgument("p");
			resultFileName=newParser.getArgument("r");
			//read trainFiles, goldFile
			ArrayList<ConllFile> trainFiles=new ArrayList<ConllFile>();
			String[] trainFilesSplit=trainFilesName.split("-AND-");
			if(fileNumber!=trainFilesSplit.length)
				usage();
			for(int i=0;i<trainFilesSplit.length;i++){
				ConllFile newFile=new ConllFile();
				newFile.readFrom(trainFilesSplit[i],0);
				trainFiles.add(newFile);
			}
			ConllFile goldFile=new ConllFile();
			goldFile.readFrom(goldFileName, 0);
			//read testFiles, and generate resultFile
			ArrayList<ConllFile> testFiles=new ArrayList<ConllFile>();
			String[] testFilesSplit=testFilesName.split("-AND-");
			if(fileNumber!=testFilesSplit.length)
				usage();
			for(int i=0;i<trainFilesSplit.length;i++){
				ConllFile newFile=new ConllFile();
				newFile.readFrom(testFilesSplit[i], 0);
				testFiles.add(newFile);
			}
			ConllFile resultFile=new ConllFile();
			for(int i=0;i<testFiles.get(0).getSentenceNumber();i++)
				resultFile.addEmptySentence(testFiles.get(0).getSentence(i).getSentenceLength());
			//to determine strategy of classifier combination
			if(strategy.equals("majority")){
				newComb.SimpleVotingMajority(testFiles,resultFile);
				resultFile.storeSenResultFile(resultFileName);
			}
			else if(strategy.equals("totalprecision")){
				newComb.SimpleVotingTotalPrecision(trainFiles,goldFile,testFiles,resultFile);
				resultFile.storeSenResultFile(resultFileName);
			}
			else if(strategy.equals("tagprecision")){
				newComb.SimpleVotingTagPrecision(trainFiles,goldFile,testFiles,resultFile);
				resultFile.storeSenResultFile(resultFileName);
			}
			else if(strategy.equals("precisionrecall")){
				newComb.SimpleVotingPrecisionRecall(trainFiles,goldFile,testFiles,resultFile);
				resultFile.storeSenResultFile(resultFileName);
			}
			else if(strategy.equals("tagpair")){
				newComb.tagPairTrain(trainFiles,goldFile);
				ArrayList<String> fileResultSignal = goldFile.generateResultTags();
				newComb.tagPairPredict(fileResultSignal, testFiles, resultFile);
				resultFile.storeSenResultFile(resultFileName);
			}
			else if(strategy.equals("wpdv")){
				newComb.wpdvGenerateTemplate(4);
				newComb.wpdvTrain(trainFiles,goldFile);
				ArrayList<String> fileResultSignal = goldFile.generateResultTags();
				newComb.wpdvPredict(fileResultSignal, testFiles, resultFile);
				resultFile.storeSenResultFile(resultFileName);
			}
		}
	}
	*/

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//oneExample(args);
	}

}
