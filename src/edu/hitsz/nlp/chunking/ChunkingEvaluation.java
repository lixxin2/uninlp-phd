package edu.hitsz.nlp.chunking;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.hitsz.nlp.struct.ConllFile;


public class ChunkingEvaluation {

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

	public ChunkingEvaluation(){
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
	 * Computer the evaluation result from gold and predict file
	 * <p> 从正确和预测的文件中评估其准确率
	 * @param goldFileName Name of Golden File with the golden tags in last column of every line
	 * <p>
	 * @param predictFileName Name of predicted File with the golden tags in last column of every line
	 */
	public void eval(String goldFileName, String predictFileName){
		ConllFile goldFile=new ConllFile();
		goldFile.readFrom(goldFileName, 0);
		ConllFile predictFile=new ConllFile();
		predictFile.readFrom(predictFileName, 0);
		if(goldFile.getSentenceNumber() != predictFile.getSentenceNumber()){
			System.out.println("The sentence number is different. Please check");
		}
		eval(goldFile, predictFile);
	}

	/**
	 * 从正确和预测的文件中评估其准确率
	 * @since Dec 14, 2011
	 * @param goldFile
	 * @param predictFile
	 */
	public void eval(ConllFile goldFile, ConllFile predictFile){
		for(int i=0;i<goldFile.getSentenceNumber();i++){
			//System.out.println(i+" has been processed!");
			int seqLen=goldFile.getSentence(i).getResultSignal().size();
			if(seqLen!=predictFile.getSentence(i).getResultSignal().size()){
				System.out.println("The sentence Length is different!");
				System.exit(-1);
			}
			//read tags from sequence(sentence)
			evalSentenceTag(goldFile.getSentence(i).getResultSignal(),
					predictFile.getSentence(i).getResultSignal());
		}
		totalSentenceNumber=goldFile.getSentenceNumber();
		sentenceCorrectProb=sentenceCorrectNumber/(double)totalSentenceNumber;
		stat();
	}

	/**
	 * 评估句子
	 * @since Dec 14, 2011
	 * @param goldTags
	 * @param predictTags
	 */
	public void evalSentenceTag(ArrayList<String> goldTags, ArrayList<String> predictTags){
		ArrayList<BasicChunk> gold = readTags(goldTags);
		ArrayList<BasicChunk> predict = readTags(predictTags);
		this.evalSentence(gold, predict);
	}

	/**
	 * 从标识中读取出所有的chunk标识，BIO标识表示
	 * @since Dec 14, 2011
	 * @param tags
	 * @return chunk list
	 */
	public ArrayList<BasicChunk> readTags(ArrayList<String> tags){
		ArrayList<BasicChunk> allChunks = new ArrayList<BasicChunk>();
		int seqLen=tags.size();
		//read tags from gold sequence
		for(int i=0;i<seqLen;i++){
			if(tags.get(i).equals("O")){
				BasicChunk newChunk = new BasicChunk("O",i,i);
				allChunks.add(newChunk);
			}
			else{
				String[] curTag=tags.get(i).split("-");
				String curTagL=curTag[0];
				String curTagR=curTag[1];
				String curSig=curTag[1];
				int start=i;
				int end=i;
				if(curTagL.equals("B")){
					while(i+1<seqLen && !tags.get(i+1).equals("O")){
						curTag=tags.get(i+1).split("-");
						curTagL=curTag[0];
						curTagR=curTag[1];
						if(curTagL.equals("I")&&curTagR.equals(curSig)){
							end=++i;
						}
						else{
							break;
						}
					}
					BasicChunk newChunk = new BasicChunk(curSig, start, end);
					allChunks.add(newChunk);
				}
				else if(curTagL.equals("I")){
					while(i+1<seqLen && !tags.get(i+1).equals("O")){
						curTag = tags.get(i+1).split("-");
						curTagL = curTag[0];
						curTagR = curTag[1];
						if(curTagL.equals("I") && curTagR.equals(curSig)){
							end=++i;
						}
						else{
							break;
						}
					}
					BasicChunk newChunk = new BasicChunk(curSig, start, end);
					allChunks.add(newChunk);
				}
			}
		}
		return allChunks;
	}

	/**
	 * 读取标识，如果每个字对应一个标识
	 * @param seq
	 * @param vec
	 */
	public ArrayList<BasicChunk> readTagsSingle(ArrayList<String> tags){
		ArrayList<BasicChunk> allChunks = new ArrayList<BasicChunk>();
		int tagsLength=tags.size();
		//read tags from gold sequence
		for(int i=0;i<tagsLength;i++){
			BasicChunk newChunk = new BasicChunk(tags.get(i),i,i);
			allChunks.add(newChunk);
		}
		return allChunks;
	}

	/**
	 * 评估一句话中的正确标识和错误标识
	 * @since Dec 14, 2011
	 * @param gold
	 * @param predict
	 */
	public void evalSentence(ArrayList<BasicChunk>gold, ArrayList<BasicChunk>predict){
			//compare tags
		int senCorrectSignal = 1;
		int goldNum = gold.size();
		int predictNum = predict.size();
		int i=0,j=0;
		while(i<goldNum || j<predictNum){
			//if the end of the tags are same
			if(gold.get(i).getEnd() == predict.get(j).getEnd() ){
				//if the tags are same
				if(gold.get(i).getTag().equals(predict.get(j).getTag() )&&
					gold.get(i).getStart() == predict.get(j).getStart() ){
					String predictTag = predict.get(j).getTag();
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
					String predictTag = predict.get(j).getTag();
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
					String goldTag = gold.get(i).getTag();
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
			else if(gold.get(i).getEnd() < predict.get(j).getEnd()){
				senCorrectSignal=0;
				String goldTag = gold.get(i).getTag();
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
				String predictTag= predict.get(j).getTag();
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
	}


	/**
	 * 统计文件中的正确和错误的统计信息
	 */
	public void stat(){
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
	 * 存储评测文件
	 * Store the evaluation file
	 * @param outFileName
	 */
	public void store(String outFileName){
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
	 * 统计正确标识与多个预测标识的相同和不同处
	 * statistic about the chunk agreement in those predicted sequences and golden sequence
	 * @param seqStat The ArrayList to store the number of 4(Total number, All Correct, At least one correct, all Wrong)
	 * @param predictSeqs ArrayList of sequences
	 * @param goldSeq
	 */
	public void compAgreement(ArrayList<String> goldSeq, ArrayList<ArrayList<String>> predictSeqs, ArrayList<Integer> seqStat){
		int seqNum=predictSeqs.size();
		if(seqStat.size()!=4){
			System.out.println("Number of seqStat is not equal to 4. Please check");
			System.exit(1);
		}
		else{
			//read tags for predicted sequences and golden sequence
			ArrayList<BasicChunk> gold=new ArrayList<BasicChunk>();
			ArrayList<ArrayList<BasicChunk>> predict=new ArrayList<ArrayList<BasicChunk>>();
			gold =  readTags(goldSeq);
			for(int i=0;i<seqNum;i++){
				ArrayList<BasicChunk> newVec= readTags(predictSeqs.get(i));
				predict.add(newVec);
			}
			//
			int tagSize=gold.size();
			for(int i=0;i<tagSize;i++){
				//number of correct predicted chunk for each golden chunk
				int correctOneTag=0;
				//gold ArrayList to store one chunk
				String goldTag = gold.get(i).getTag();
				int goldStart =  gold.get(i).getStart();
				int goldEnd = gold.get(i).getEnd();
				if(!goldTag.equals("O")){
					seqStat.set(0, seqStat.get(0)+1);
					for(int j=0;j<seqNum;j++){
						int preSize = predict.get(j).size();
						int matchSig=0;
						for(int k=0;k<preSize;k++){
							String predictTag = predict.get(j).get(k).getTag();
							int preStart = predict.get(j).get(k).getStart();
							int preEnd = predict.get(j).get(k).getEnd();
							if(goldTag.equals(predictTag)&&goldStart==preStart&&goldEnd==preEnd){
								matchSig=1;
								break;
							}
						}
						if(matchSig==1)
							correctOneTag++;
					}
					if(correctOneTag==seqNum)
						seqStat.set(1, seqStat.get(1)+1);
					else if(correctOneTag>=1)
						seqStat.set(2, seqStat.get(2)+1);
					else if(correctOneTag==0)
						seqStat.set(3, seqStat.get(3)+1);
				}
			}
		}

	}

	/**
	 * 从多个文件中计算相同度
	 * @param goldFileName
	 * @param predictFileNames
	 */
	public void compAgreementFromFile(String goldFileName, String[] predictFileNames, String agreementFile){
		//read golden and predicted tags from files
		ConllFile goldFile=new ConllFile();
		goldFile.readFrom(goldFileName, 0);
		int predictNum=predictFileNames.length;
		ArrayList<ConllFile> predictFiles=new ArrayList<ConllFile>();
		for(String predicteFileName : predictFileNames){
			ConllFile newFile=new ConllFile();
			newFile.readFrom(predicteFileName, 0);
			predictFiles.add(newFile);
		}
		//initial evaluation ArrayList
		ArrayList<Integer> evalVec=new ArrayList<Integer>();
		for(int i=0;i<4;i++)
			evalVec.add(0);
		int senNumber=goldFile.getSentenceNumber();
		if(senNumber!=predictFiles.get(0).getSentenceNumber()){
			System.out.println("The sentence number is different. Please check");
		}
		for(int i=0;i<senNumber;i++){
			ArrayList<ArrayList<String>> preVec=new ArrayList<ArrayList<String>>();
			for(int j=0;j<predictNum;j++)
				preVec.add(predictFiles.get(j).getSentence(i).getResultSignal());
			compAgreement(goldFile.getSentence(i).getResultSignal(),preVec,evalVec);
		}
		//store
		try{
			FileWriter newWriter=new FileWriter(agreementFile);
			//System.out.println("Total     AllCorrect   AtLeastOneCorrect  AllWrong");
			//for(int i : evalVec)
			//	System.out.print(i+"\t\t");
			newWriter.write("Total     AllCorrect   AtLeastOneCorrect  AllWrong\n");
			for(int i : evalVec)
				newWriter.write(i+"\t\t");
			newWriter.write("\n");
			for(int i : evalVec)
				newWriter.write(Math.round(10000*i/(double)evalVec.get(0))/100f+"\t\t");
			newWriter.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
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
			ArrayList<BasicChunk> goldVec = readTags(goldArrayList);
			ArrayList<BasicChunk> predictVec = readTags(inputFile.getSentence(i).getResultSignal());
			this.evalSentence(goldVec, predictVec);
		}
		totalSentenceNumber=inputFile.getSentenceNumber();
		sentenceCorrectProb=sentenceCorrectNumber/(double)totalSentenceNumber;
		stat();
	}



}
