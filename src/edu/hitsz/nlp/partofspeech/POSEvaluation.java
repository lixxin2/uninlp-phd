package edu.hitsz.nlp.partofspeech;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.ConllFile;

/**
 * @author tm
 * evaluation for sentences which denote multiple tokens as one tag, such as chunking, named entity recognition
 */
public class POSEvaluation {

	private ArrayList<String> tags;
	private ArrayList<Integer> goldTags;
	private ArrayList<Integer> predictTags;
	private ArrayList<Integer> trueTags;
	private ArrayList<Double> tagPrecision;
	private ArrayList<Double> tagRecall;
	private ArrayList<Double> tagFValue;
	private int totalGoldTag;
	private int totalPredictTag;
	private int totalTrueTag;
	private double totalPrecision;
	private double totalRecall;
	private double totalFValue;
	private int sentenceCorrectNumber;
	private int totalSentenceNumber;
	private double sentenceCorrectProb;

	public POSEvaluation(){
		tags=new ArrayList<String>();
		goldTags=new ArrayList<Integer>();
		predictTags=new ArrayList<Integer>();
		trueTags=new ArrayList<Integer>();
		tagPrecision=new ArrayList<Double>();
		tagRecall=new ArrayList<Double>();
		tagFValue=new ArrayList<Double>();
		totalGoldTag=0;
		totalPredictTag=0;
		totalTrueTag=0;
		totalPrecision=0;
		totalRecall=0;
		totalFValue=0;
		sentenceCorrectNumber=0;
		totalSentenceNumber=0;
		sentenceCorrectProb=0;
	}


	public ArrayList<String> getTags(){
		return this.tags;
	}

	public ArrayList<Double> getTagPrecision(){
		return this.tagPrecision;
	}
	public ArrayList<Double> getTagRecall(){
		return this.tagRecall;
	}

	public double getTotalPrecision(){
		return this.totalPrecision;
	}
	public double getTotalRecall(){
		return this.totalRecall;
	}

	/**
	 * extract part-of-speech tags in tokens, the structure is [symbol,start_position,end_position,]
	 * @param seq
	 * @param vec
	 */
	public void readTags(ArrayList<String> seq, ArrayList<ArrayList> vec){
		int seqLen=seq.size();
		//read tags from gold sequence
		for(int i=0;i<seqLen;i++){
			ArrayList newVec=new ArrayList();
			newVec.add(seq.get(i));
			newVec.add(i);
			newVec.add(i);
			newVec.add(1);
			vec.add(newVec);
		}
	}



	/**
	 * computer the information in every sentence
	 * @param goldSeq Gold sequence
	 * @param predictSeq Predicted sequence
	 * @return
	 */
	public boolean evalSentence(ArrayList<ArrayList> goldVec, ArrayList<ArrayList> predictVec){

		//compare tags
		int senCorrectSignal=1;
		int goldNum=goldVec.size();
		int predictNum=predictVec.size();
		int i=0,j=0;
		while(i<goldNum||j<predictNum){
			//if the end of the tags are same
			if(goldVec.get(i).get(2)==predictVec.get(j).get(2)){
				//if the tags are same
				if(goldVec.get(i).get(0).equals(predictVec.get(j).get(0))&&
					goldVec.get(i).get(1)==predictVec.get(j).get(1)){
					String predictTag=(String)predictVec.get(j).get(0);
					if(tags.contains(predictTag)){
						int position=tags.indexOf(predictTag);
						int tmpNum=goldTags.get(position);
						goldTags.set(position,tmpNum+1);
						tmpNum=predictTags.get(position);
						predictTags.set(position,tmpNum+1);
						tmpNum=trueTags.get(position);
						trueTags.set(position,tmpNum+1);
					}
					else{
						int m=0;
						for(;m<tags.size()&&predictTag.compareTo(tags.get(m))>0;m++);
						if(m<tags.size()){
							tags.add(m,predictTag);
							goldTags.add(m,1);
							predictTags.add(m,1);
							trueTags.add(m,1);
						}
						else{
							tags.add(predictTag);
							goldTags.add(1);
							predictTags.add(1);
							trueTags.add(1);
						}
					}
					i++;
					j++;
				}
				//if the tags is different, add goldTag and predictTag
				else{
					senCorrectSignal=0;
					String predictTag=(String)predictVec.get(j).get(0);
					if(tags.contains(predictTag)){
						int position=tags.indexOf(predictTag);
						int tmpNum=predictTags.get(position);
						predictTags.set(position,tmpNum+1);
					}
					else{
						int m=0;
						for(;m<tags.size()&&predictTag.compareTo(tags.get(m))>0;m++);
						if(m<tags.size()){
							tags.add(m,predictTag);
							predictTags.add(m,1);
							goldTags.add(m,0);
							trueTags.add(m,0);
						}
						else{
							tags.add(predictTag);
							predictTags.add(1);
							goldTags.add(0);
							trueTags.add(0);
						}
					}
					String goldTag=(String)goldVec.get(i).get(0);
					if(tags.contains(goldTag)){
						int position=tags.indexOf(goldTag);
						int tmpNum=goldTags.get(position);
						goldTags.set(position,tmpNum+1);
					}
					else{
						int m=0;
						for(;m<tags.size()&&goldTag.compareTo(tags.get(m))>0;m++);
						if(m<tags.size()){
							tags.add(m,goldTag);
							goldTags.add(m,1);
							predictTags.add(m,0);
							trueTags.add(m,0);
						}
						else{
							tags.add(goldTag);
							goldTags.add(1);
							predictTags.add(0);
							trueTags.add(0);
						}
					}
					i++;
					j++;
				}
			}
			//if gold is before predict, add goldTag
			else if((Integer)goldVec.get(i).get(2)<(Integer)predictVec.get(j).get(2)){
				senCorrectSignal=0;
				String goldTag=(String)goldVec.get(i).get(0);
				if(tags.contains(goldTag)){
					int position=tags.indexOf(goldTag);
					int tmpNum=goldTags.get(position);
					goldTags.set(position,tmpNum+1);
				}
				else{
					int m=0;
					for(;m<tags.size()&&goldTag.compareTo(tags.get(m))>0;m++);
					if(m<tags.size()){
						tags.add(m,goldTag);
						goldTags.add(m,1);
						predictTags.add(m,0);
						trueTags.add(m,0);
					}
					else{
						tags.add(goldTag);
						goldTags.add(1);
						predictTags.add(0);
						trueTags.add(0);
					}
				}
				i++;
			}
			//if predict is before gold, add predictTag
			else{
				senCorrectSignal=0;
				String predictTag=(String)predictVec.get(j).get(0);
				if(tags.contains(predictTag)){
					int position=tags.indexOf(predictTag);
					int tmpNum=predictTags.get(position);
					predictTags.set(position,tmpNum+1);
				}
				else{
					int m=0;
					for(;m<tags.size()&&predictTag.compareTo(tags.get(m))>0;m++);
					if(m<tags.size()){
						tags.add(m,predictTag);
						predictTags.add(m,1);
						goldTags.add(m,0);
						trueTags.add(m,0);
					}
					else{
						tags.add(predictTag);
						predictTags.add(1);
						goldTags.add(0);
						trueTags.add(0);
					}
				}
				j++;
			}
		}
		if(senCorrectSignal==1)
			sentenceCorrectNumber+=1;
		return true;
	}



	/**
	 * Computer the information in all sentences
	 * @param gold Gold sentences
	 * @param predict Predicted sentences
	 * @return
	 */
	public boolean evalAll(ArrayList<ArrayList<String>> gold, ArrayList<ArrayList<String>> predict, String tagType){
		if(gold.size()==0||predict.size()==0)
		{
			System.out.println("The number of sentence is 0 in one of the gold and predict file. Please check");
			return false;
		}
		if(gold.size()!=predict.size()){
			System.out.println("The Number of Sentence is different. Please check");
			return false;
		}
		int senSize=gold.size();
		for(int i=0;i<senSize;i++){
			int seqLen=gold.get(i).size();
			if(seqLen!=predict.get(i).size()){
				System.out.println("The sentence Length is different!");
				System.exit(-1);
			}
			//read tags from sequence(sentence)
			ArrayList<ArrayList> goldVec=new ArrayList<ArrayList>();
			ArrayList<ArrayList> predictVec=new ArrayList<ArrayList>();
			this.readTags(gold.get(i), goldVec);
			this.readTags(predict.get(i), predictVec);
			this.evalSentence(goldVec, predictVec);
			evalSentence(goldVec, predictVec);
		}

		return true;
	}

	/**
	 * compute the statistics of all data
	 */
	public void compEvalStat(){
		int tagSize=tags.size();
		if(tagSize==0){
			System.out.println("The Number of Tags is zero. Please check");
		}
		for(int i=0;i<tagSize;i++){
			//computer precision
			double precision=0;
			if(predictTags.get(i)==0){
				if(trueTags.get(i)>0){
					System.out.println("The number of predictTags is 0 and trueTags is larger than 0. Please check");
					System.exit(1);
				}
			}
			else
				precision=100*trueTags.get(i)/(double)predictTags.get(i);
			tagPrecision.add(precision);
			//computer recall
			double recall=0;
			if(goldTags.get(i)==0){
				if(trueTags.get(i)>0){
					System.out.println("The number of goldTags is 0 and trueTags is larger than 0. Please check");
					System.exit(1);
				}
			}
			else
				recall=100*trueTags.get(i)/(double)goldTags.get(i);
			tagRecall.add(recall);
			//computer f1-value
			double fvalue=0;
			if(Math.abs(precision+recall)>0.000001)
				fvalue=2*precision*recall/(precision+recall);
			tagFValue.add(fvalue);
			//subtract the
			if(!tags.get(i).equals("O")){
				totalGoldTag+=goldTags.get(i);
				totalPredictTag+=predictTags.get(i);
				totalTrueTag+=trueTags.get(i);
			}
		}
		totalPrecision=0;
		if(totalPredictTag!=0)
			totalPrecision=100*totalTrueTag/(double)totalPredictTag;
		totalRecall=0;
		if(totalGoldTag!=0)
			totalRecall=100*totalTrueTag/(double)totalGoldTag;
		totalFValue=0;
		if(Math.abs(totalPrecision+totalRecall)>0.00001)
			totalFValue=2*totalPrecision*totalRecall/(totalPrecision+totalRecall);

	}


	/**
	 * Computer the evaluation result from gold and predict file
	 * @param goldFile Golden File with the golden tags in last column of every line
	 * @param predictFile predicted File with the golden tags in last column of every line
	 */
	public void eval(ConllFile goldFile, ConllFile predictFile){
		if(goldFile.getSentenceNumber() != predictFile.getSentenceNumber()){
			System.out.println("The sentence number is different. Please check");
		}
		for(int i=0;i<goldFile.getSentenceNumber();i++){
			//System.out.println(i+" has been processed!");
			int seqLen=goldFile.getSentence(i).getResultSignal().size();
			if(seqLen!=predictFile.getSentence(i).getResultSignal().size()){
				System.out.println("The sentence Length is different!");
				System.exit(-1);
			}
			//read tags from sequence(sentence)
			ArrayList<ArrayList> goldVec=new ArrayList<ArrayList>();
			ArrayList<ArrayList> predictVec=new ArrayList<ArrayList>();
			this.readTags(goldFile.getSentence(i).getResultSignal(), goldVec);
			this.readTags(predictFile.getSentence(i).getResultSignal(), predictVec);
			this.evalSentence(goldVec, predictVec);
		}
		totalSentenceNumber=goldFile.getSentenceNumber();
		sentenceCorrectProb=sentenceCorrectNumber/(double)totalSentenceNumber;
		compEvalStat();
	}


	/**
	 * Computer the evaluation result from gold and predict file
	 * <p> 根据正确和预测的文件，评估其准确率
	 * @param goldFileName Name of Golden File with the golden tags in last column of every line
	 * <p>
	 * @param predictFileName Name of predicted File with the golden tags in last column of every line
	 */
	public void eval(String goldFileName, String predictFileName){
		ConllFile goldFile=new ConllFile();
		goldFile.readFrom(goldFileName, 0);
		ConllFile predictFile=new ConllFile();
		predictFile.readFrom(predictFileName, 0);
		eval(goldFile, predictFile);
	}


	/**
	 * Computer the evaluation result from one file containing both gold and predict tags
	 *
	 */
	public void evalSingle(String inputFileName, String tagType){
		ConllFile inputFile=new ConllFile();
		inputFile.readFrom(inputFileName, 0);
		int wordNumber=inputFile.getSentence(0).getWords().get(0).size();
		for(int i=0;i<inputFile.getSentenceNumber();i++){
			ArrayList<String> goldArrayList=new ArrayList<String>();
			goldArrayList.clear();
			for(int j=0;j<inputFile.getSentence(i).getSentenceLength();j++)
				goldArrayList.add(inputFile.getSentence(i).getWords().get(wordNumber-2).get(j));
			//read tags from sequence(sentence)
			ArrayList<ArrayList> goldVec=new ArrayList<ArrayList>();
			ArrayList<ArrayList> predictVec=new ArrayList<ArrayList>();
			this.readTags(goldArrayList, goldVec);
			this.readTags(inputFile.getSentence(i).getResultSignal(), predictVec);
			this.evalSentence(goldVec, predictVec);
		}
		totalSentenceNumber=inputFile.getSentenceNumber();
		sentenceCorrectProb=sentenceCorrectNumber/(double)totalSentenceNumber;
		compEvalStat();
	}





	/**
	 * Store the evaluation file
	 * @param outFileName
	 */
	public void storeEvalFile(String outFileName){
		int tagSize=tags.size();
		if(tagSize==0){
			System.out.println("The Number of Tags is zero. Please check");
			System.exit(1);
		}
		try{
			FileWriter outFileWriter=new FileWriter(outFileName);

			DecimalFormat df = new DecimalFormat("######0.00");
			outFileWriter.write("processed "+totalGoldTag+" tags; found: "+totalPredictTag+" tags; Correct: "+totalTrueTag+"\n");
			outFileWriter.write("accuracy:\t\t\t\t\t\t\t\t\t\t"+"precision:\t"+df.format(totalPrecision)+";\t"+"recall:\t"+df.format(totalRecall)+";\t"+"FB1:\t"+df.format(totalFValue)+"\n");
			for(int i=0;i<tagSize;i++){
				outFileWriter.write("\t\t"+tags.get(i)+":\t"+"gold:\t"+goldTags.get(i)+";\t"+"predict:\t"+predictTags.get(i)+";\t"+"correct:\t"+trueTags.get(i));
				outFileWriter.write("\tprecision:\t"+df.format(tagPrecision.get(i))+";\t"+"recall:\t"+df.format(tagRecall.get(i))+";\t"+"FB1:\t"+df.format(tagFValue.get(i))+"\n");
			}
			outFileWriter.write("\nprocessed "+totalSentenceNumber+" sentences; correct: "+sentenceCorrectNumber+" sentences; Accuracy: "+sentenceCorrectProb+"\n");
			outFileWriter.close();
			System.out.println("\nStore Evaluation file done!");
		}catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * Display the evaluation result
	 */
	public void printEvalStat(){
		int tagSize=tags.size();
		if(tagSize==0){
			System.out.println("The Number of Tags is zero. Please check");
			System.exit(1);
		}
		DecimalFormat df = new DecimalFormat("######0.00");
		System.out.println("processed "+totalGoldTag+" tags; found: "+totalPredictTag+" tags; Correct: "+totalTrueTag);
		System.out.println("accuracy:\t\t\t\t\t\t\t\t\t\t"+"precision:\t"+df.format(totalPrecision)+";\t"+"recall:\t"+df.format(totalRecall)+";\t"+"FB1:\t"+df.format(totalFValue));
		for(int i=0;i<tagSize;i++){
			System.out.print("\t\t"+tags.get(i)+":\t"+"gold:\t"+goldTags.get(i)+";\t"+"predict:\t"+predictTags.get(i)+";\t"+"correct:\t"+trueTags.get(i));
			System.out.println("\tprecision:\t"+df.format(tagPrecision.get(i))+";\t"+"recall:\t"+df.format(tagRecall.get(i))+";\t"+"FB1:\t"+df.format(tagFValue.get(i)));
		}
		System.out.println("processed "+totalSentenceNumber+" sentences; found: "+sentenceCorrectNumber+" sentences; Accuracy: "+sentenceCorrectProb);
	}

	/**
	 * return the f-value of the results
	 * @return
	 */
	public double getFValue(){
		return totalFValue;
	}



	static void usage(){
		System.out.println("Usage:");
		System.out.println("  Help: -h");
		System.out.println("  Evaluation: -g goldFile -t testFile -e evalFile");
		System.out.println("  Evaluation2: -i inputFile -e evalFile");
		System.out.println("    inputFile contains both gold and test tags in ConllFile format");
		System.out.println("  Agreement: -g goldFile -t testFile1-AND-testFile2-AND-testFilen -a agreementFile");
	}




	private static void test(){
		
		for(int i=0; i<10; i++) {
			String goldFileName = "/home/tm/disk/disk1/pos-chunk-rerank/8.perceptron/20wp";
			String predictFileName = "/home/tm/disk/disk1/pos-chunk-rerank/8.perceptron/20.percetron.result.pos.f2-"+i+"-wp";
			String evalFile = predictFileName + "-eval";
			POSEvaluation newEval = new POSEvaluation();
			newEval.eval(goldFileName, predictFileName);
			newEval.storeEvalFile(evalFile);
		}
		
		/*
		String goldFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/mert_result/20wp";
		String predictFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/mert_result/500best.mert10.poschunk0.33.noglobal.wp";
		String evalFile = predictFileName + ".poseval";
		POSEvaluation newEval = new POSEvaluation();
		newEval.eval(goldFileName, predictFileName);
		newEval.storeEvalFile(evalFile);
		*/
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//oneExample(args);
		test();
	}

}
