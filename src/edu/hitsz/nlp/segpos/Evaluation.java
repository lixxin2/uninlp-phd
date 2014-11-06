package edu.hitsz.nlp.segpos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.segmentation.BasicWord;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.util.ArgumentsParser;

/**
 * @author tm
 * evaluation for sentences which denote multiple tokens as one tag, such as chunking, named entity recognition
 */
public class Evaluation {

	public static String delimiter="-";

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
	public int totalSentenceNumber;
	private double sentenceCorrectProb;

	/** all tags in dictionary **/
	public HashMap<String, Integer> vTags;    	//词和对应的位置
	private ArrayList<String> oovTags;         	//oov词
	private ArrayList<Integer> oovPredictTags; 	//预测的词
	private ArrayList<Integer> oovAllTags;     	//
	private ArrayList<String> ivTags;
	private ArrayList<Integer> ivPredictTags;
	private ArrayList<Integer> ivAllTags;
	private int totalOovTags;
	private int totalTrueOovTags;
	private int totalIvTags;
	private int totalTrueIvTags;
	private double oovRecall;
	private double ivRecall;
	private double oovRate;

	public Evaluation(){

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

		vTags = new HashMap<String, Integer>();
		oovTags = new ArrayList<String>();
		oovPredictTags = new ArrayList<Integer>();
		oovAllTags = new ArrayList<Integer>();
		ivTags = new ArrayList<String>();
		ivPredictTags = new ArrayList<Integer>();
		ivAllTags = new ArrayList<Integer>();
		totalOovTags = 0;
		totalTrueOovTags = 0;
		totalIvTags = 0;
		totalTrueIvTags = 0;
		oovRecall = 0;
		ivRecall = 0;
		oovRate = 0;
	}

	

	/**
	 * load in vocabulary words from file, where every entity contains only word or combination of word and tag
	 * @param dictFileName
	 */
	public void loadIvWords(String dictFileName, String tagType){
		
		System.out.println("Loading word dictionary: ");
		String  encoding = FileEncoding.getCharset(dictFileName);
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dictFileName), encoding));
	        //
			String line = null;
			int count = 0;
			while((line = reader.readLine()) != null) {
				
				if(count % 10000 == 0) 
					System.out.print(count + ".");
				line = line.trim();
				if(line.length() == 0)
					continue;
				
				String[] parts = line.split("\\s+");			
				String tmp = getWordPos(parts, tagType);
				
				if(!vTags.containsKey(tmp))
					vTags.put(tmp, 1);
				else
					vTags.put(tmp, vTags.get(tmp)+1);
				
				line = reader.readLine();
				count++;
			}	
			
			reader.close();
			System.out.println("\nThe word dictionary has totally " + vTags.size() + " words.");
		}
		catch (IOException e) {
			
		}
	}
	
	/**
	 * 根据类型，输入词或词和词性
	 * @since Oct 20, 2013
	 * @param parts 数组[词，词性]
	 * @param tagType seg,segpos
	 * @return
	 */
	public String getWordPos(String[] parts, String tagType) {
		String tmp = "";
		if(tagType.equals("seg"))
			tmp = parts[0];
		else if(tagType.equals("segpos")) 
			tmp = parts[0] + (parts.length > 1 ? delimiter + parts[1] : "");
		return tmp;
	}
	
	
	public BasicWordPos[] getWordPoses(Instance instance, String tagType){
		
		if(tagType.equals("seg") && instance.words != null ){
			return getWordPoses(instance.words);
		}
		else if(tagType.equals("segpos") && instance.words != null && instance.tags != null){
			return getWordPoses(instance.words, instance.tags);
		}
		else if(tagType.equals("pos") && instance.tags != null){
			return getWordPoses(instance.tags);
		}
		else{
			System.out.println("The format of input file is wrong");
			System.exit(-1);
		}
		return null;
	}
	

	
	
	/**
	 * 获取对应的WordPos
	 * @since 2013-2-14
	 * @param words
	 * @return
	 */
	public BasicWordPos[] getWordPoses(String[] words) {
		return getWordPoses(words, null);
	}
	
	
	/**
	 * 获取对应的WordPos
	 * @since 2013-2-14
	 * @param words
	 * @return
	 */
	public BasicWordPos[] getWordPoses(String[] words, String[] poses) {
		
		int length = words.length;
		BasicWordPos[] wordposes = new BasicWordPos[length];
		
		if(poses == null) {
			poses = new String[length];
			for(int i = 0; i < length; i++)
				poses[i] = "";
		}
		
		int start=0;
		
		for(int i=0; i<length; i++){
			String word = words[i];
			int end = start + word.length()-1;
			BasicWordPos wordpos = new BasicWordPos(word, poses[i], start, end);
			wordposes[i] = wordpos;
			start = end+1;
		}
		
		return wordposes;
	}
	
	
	/** 
	 * return the number of correct words which is same between gold and predict sentences
	 * <p> 从而可以计算precision, recall, F-1 value
	 * @since 2013-2-14
	 * @param goldWords
	 * @param predictWords
	 * @return
	 */
	public int wordCorr(BasicWord[] goldWords, BasicWord[] predictWords) {	
		
		int corr = 0;
		
		int goldNumber = goldWords.length;
		int predictNumber = predictWords.length;
		
		if(goldWords[goldNumber-1].getEnd() != predictWords[predictNumber-1].getEnd()) {
			System.out.println("error. the sentences in gold and predict are different");
		}
		
		int i=0;
		int j=0;
		
		while(i<goldNumber && j<predictNumber) {
			BasicWord goldWord = goldWords[i];
			BasicWord predictWord = predictWords[j];
			int goldEnd = goldWord.getEnd();
			int predictEnd = predictWord.getEnd();
			
			if(goldWord.equals(predictWord)) {
				corr += 1;
			}
			if(goldEnd == predictEnd) {
				i++;
				j++;
			}
			else if(goldEnd > predictEnd)
				j++;
			else
				i++;			
		}
		return corr;		
	}
	
	
	
	

	/** 添加到oov的gold 和 predict，预测正确*/
	public void addToOOVAll(String s) {
		if(oovTags.contains(s)){
			int position=oovTags.indexOf(s);
			int tmpNum=oovPredictTags.get(position);
			oovPredictTags.set(position,tmpNum+1);
			tmpNum=oovAllTags.get(position);
			oovAllTags.add(position,tmpNum+1);
		}
		else{	//添加新词
			int m=0;
			for(;m<oovTags.size()&&s.compareTo(oovTags.get(m))>0;m++);
			if(m<oovTags.size()){
				oovTags.add(m,s);
				oovPredictTags.add(m,1);
				oovAllTags.add(m,1);
			}
			else{
				oovTags.add(s);
				oovPredictTags.add(1);
				oovAllTags.add(1);
			}
		}
	}
	
	/** 添加到oov的gold ，预测错误*/
	public void addToOOVGold(String s) {
		if(oovTags.contains(s)){ 
			int position=oovTags.indexOf(s);
			int tmpNum=oovAllTags.get(position);
			oovAllTags.set(position,tmpNum+1);
		}
		else{
			int m=0;
			for(;m<oovTags.size()&&s.compareTo(oovTags.get(m))>0;m++);
			if(m<oovTags.size()){
				oovTags.add(m,s);
				oovPredictTags.add(m,0);
				oovAllTags.add(m,1);
			}
			else{
				oovTags.add(s);
				oovPredictTags.add(0);
				oovAllTags.add(1);
			}
		}	

	}
	
	/** 添加到iv的gold 和 predict，预测正确*/
	public void addToIVAll(String s) {
		if(ivTags.contains(s)){
			int position=ivTags.indexOf(s);
			int tmpNum=ivPredictTags.get(position);
			ivPredictTags.set(position,tmpNum+1);
			tmpNum=ivAllTags.get(position);
			ivAllTags.add(position,tmpNum+1);
		}
		else{
			int m=0;
			for(;m<ivTags.size()&&s.compareTo(ivTags.get(m))>0;m++);
			if(m<ivTags.size()){
				ivTags.add(m,s);
				ivPredictTags.add(m,1);
				ivAllTags.add(m,1);
			}
			else{
				ivTags.add(s);
				ivPredictTags.add(1);
				ivAllTags.add(1);
			}
		}
	}
	
	/** 添加到iv的gold ，预测错误*/
	public void addToIVGold(String s) {
		if(ivTags.contains(s)){
			int position=ivTags.indexOf(s);
			int tmpNum=ivAllTags.get(position);
			ivAllTags.set(position,tmpNum+1);
		}
		else{
			int m=0;
			for(;m<ivTags.size()&&s.compareTo(ivTags.get(m))>0;m++);
			if(m<ivTags.size()){
				ivTags.add(m,s);
				ivPredictTags.add(m,0);
				ivAllTags.add(m,1);
			}
			else{
				ivTags.add(s);
				ivPredictTags.add(0);
				ivAllTags.add(1);
			}
		}
	}




	
	public void compEvalAll() {
		
		sentenceCorrectProb=sentenceCorrectNumber/(double)totalSentenceNumber;
		oovRecall = 100*totalTrueOovTags/(double)totalOovTags;
		ivRecall = 100*totalTrueIvTags/(double)totalIvTags;
		oovRate = 100*totalOovTags/(double)(totalOovTags+totalIvTags);
	}

	/**
	 * compute the statistics of all data
	 */
	public void compEvalStat(){
		
		totalGoldTag = 0;
		totalPredictTag = 0;
		totalTrueTag = 0;
		
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

			outFileWriter.write("\nout of vacabulary\tall\tpredict\n");
			for(int i=0; i<oovTags.size(); i++){
				outFileWriter.write(oovTags.get(i)+"\t\t"+oovAllTags.get(i)+"\t\t"+oovPredictTags.get(i)+"\n");
			}
			outFileWriter.write("\nin vacabulary\tall\tpredict\n");
			for(int i=0; i<ivTags.size(); i++){
				outFileWriter.write(ivTags.get(i)+"\t\t"+ivAllTags.get(i)+"\t\t"+ivPredictTags.get(i)+"\n");
			}
			outFileWriter.write("\nout of vacabulary\n"+"true:\t"+totalTrueOovTags+"\nall:\t"+totalOovTags+"\nRecall:\t"+oovRecall+"\n\n");
			outFileWriter.write("\nin vacabulary\n"+"true:\t"+totalTrueIvTags+"\nall:\t"+totalIvTags+"\nRecall:\t"+ivRecall+"\n\n");
			outFileWriter.write("oov Rate:\t"+oovRate+"\n\n");
			outFileWriter.write("processed "+totalSentenceNumber+" sentences; found: "+sentenceCorrectNumber+" sentences; Accuracy: "+sentenceCorrectProb+"\n\n");

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
		//for(int i=0;i<tagSize;i++){
		//	System.out.print("\t\t"+tags.get(i)+":\t"+"gold:\t"+goldTags.get(i)+";\t"+"predict:\t"+predictTags.get(i)+";\t"+"correct:\t"+trueTags.get(i));
		//	System.out.println("\tprecision:\t"+df.format(tagPrecision.get(i))+";\t"+"recall:\t"+df.format(tagRecall.get(i))+";\t"+"FB1:\t"+df.format(tagFValue.get(i)));
		//}

		//System.out.println("out of vacabulary:\noov\t\tall\t\tpredict");
		//for(int i=0; i<oovTags.size(); i++){
		//	System.out.println(oovTags.get(i)+"\t"+oovPredictTags.get(i)+"\t\t"+oovAllTags.get(i));
		//}
		//System.out.println("in vacabulary:\niv\t\tall\t\tpredict");
		//for(int i=0; i<ivTags.size(); i++){
		//	System.out.println(ivTags.get(i)+"\t"+ivPredictTags.get(i)+"\t\t"+ivAllTags.get(i));
		//}
		System.out.println("out of vacabulary\n"+"true:\t"+totalTrueOovTags+"\nall:\t"+totalOovTags+"\nRecall:\t"+oovRecall+"\n");
		System.out.println("in vacabulary\n"+"true:\t"+totalTrueIvTags+"\nall:\t"+totalIvTags+"\nRecall:\t"+ivRecall+"\n");
		System.out.println("oov Rate:\t"+oovRate);

		System.out.println("processed "+totalSentenceNumber+" sentences; found: "+sentenceCorrectNumber+" sentences; Accuracy: "+sentenceCorrectProb+"\n\n");

	}


	/**
	 * return the f-value of the results
	 * @return
	 */
	public double getFValue(){
		return totalFValue;
	}

	
	public double getTotalPrecision(){
		return this.totalPrecision;
	}


	
	/**
	 * Computer the evaluation result from gold and predict file，
	 * 从gold和predict文件中计算评测结果
	 * @param goldFileName Name of Golden File with the golden tags in last column of every line
	 * @param predictFileName Name of predicted File with the golden tags in last column of every line
	 * @param tagType type: seg, segpos
	 */
	public void evalFromFile(String goldFileName, 
			String predictFileName, 
			String tagType) throws IOException{
		
		Options options = new Options(new String[0]);
		CharPipe goldPipe = new CharPipe(options);
		goldPipe.initInputFile(goldFileName);
		Instance goldInstance = goldPipe.nextInstance();

		CharPipe predictPipe = new CharPipe(options);
		predictPipe.initInputFile(predictFileName);
		Instance predictInstance = predictPipe.nextInstance();
		
		int count = 0;
		while(goldInstance != null && predictInstance != null) {
			
			if(count%100 == 0) {
				if(count % 1000 == 0 && count % 100 != 0)
					System.out.println(count+".");
				else
					System.out.print(count+".");
			}
			//ArrayList<ArrayList> goldVec=new ArrayList<ArrayList>();
			//ArrayList<ArrayList> predictVec=new ArrayList<ArrayList>();
			//readTags(goldInstance, goldVec, tagType);
			//readTags(predictInstance, predictVec, tagType);
			//evalSentence(goldVec, predictVec);
			//evalSentenceOOV(goldVec, predictVec);
			
			BasicWordPos[] goldWordPoses = getWordPoses(goldInstance, tagType);
			BasicWordPos[] predictWordPoses = getWordPoses(predictInstance, tagType);
			evalSentence(goldWordPoses, predictWordPoses);
			evalSentenceOOV(goldWordPoses, predictWordPoses);			
			goldInstance = goldPipe.nextInstance();
			predictInstance = predictPipe.nextInstance();	
			count++;
		}

		totalSentenceNumber = count;
		compEvalAll();
		compEvalStat();
		System.out.println(count);
	}	
	

	/**
	 * evaluation the tokens in every sentence，评测句子
	 * @param goldSeq Gold sequence
	 * @param predictSeq Predicted sequence
	 */
	public void evalSentence(BasicWordPos[] goldWordPoses, BasicWordPos[] predWordPoses){

		//compare tags
		int senCorrectSignal = 1;
		int goldNum = goldWordPoses.length;
		int predictNum = predWordPoses.length;
		int i=0, j=0;
		
		while(i < goldNum || j < predictNum){
			
			BasicWordPos goldWordPos = goldWordPoses[i];
			BasicWordPos predWordPos = predWordPoses[j];
			int goldEnd = goldWordPos.getEnd();
			int predictEnd = predWordPos.getEnd();
			String goldTag = goldWordPos.getPos();
			String predictTag = predWordPos.getPos();
			int goldStart = goldWordPos.getStart();
			int predictStart = predWordPos.getStart();
			
			//if the end of the tags are same
			if(goldEnd == predictEnd){
				//if the tags are same
				if(goldTag.equals(predictTag) && goldStart == predictStart){
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
				}
				//if the tags is different, add goldTag and predictTag
				else{
					senCorrectSignal=0;
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
				}
				i++;
				j++;
			}
			//if gold is before predict, add goldTag
			else if(goldEnd < predictEnd){
				senCorrectSignal=0;
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
	 * evaluate out of vocabulary in every sentence， 只计算recall
	 * <p> 评测句子中的oov
	 * @param goldSeq Gold sequence
	 * @param predictSeq Predicted sequence
	 * @return
	 */
	public void evalSentenceOOV(BasicWordPos[] goldWordPoses, BasicWordPos[] predWordPoses){

		int senCorrectSignal = 1;
		int goldNum = goldWordPoses.length;
		int predictNum = predWordPoses.length;
		int i=0, j=0;
		
		while(i < goldNum || j < predictNum){
			
			BasicWordPos goldWordPos = goldWordPoses[i];
			BasicWordPos predWordPos = predWordPoses[j];
			int goldEnd = goldWordPos.getEnd();
			int predictEnd = predWordPos.getEnd();
			String goldTag = goldWordPos.getPos();
			String predictTag = predWordPos.getPos();
			int goldStart = goldWordPos.getStart();
			int predictStart = predWordPos.getStart();
		
			if(goldEnd == predictEnd){
				//if the tags are same 如果词相同				
				if(goldTag.equals(predictTag) && goldStart == predictStart){
					//if goldTag is not in vTags( no in vocabulary) 如果是oov
					if(!vTags.containsKey(goldTag)){
						addToOOVAll(goldTag);
						totalTrueOovTags += 1;
						totalOovTags += 1;
					}
					else{	//如果是iv
						addToIVAll(goldTag);
						totalTrueIvTags += 1;
						totalIvTags += 1;
					}
				}				
				else{	//if the tags is different 如果词不相同
					//if goldTag is not in vTags( no in vocabulary)  
					if(!vTags.containsKey(goldTag)){
						addToOOVGold(goldTag);
						totalOovTags += 1;
					}
					else{
						addToIVGold(goldTag);
						totalIvTags += 1;
					}
				}
				i++;
				j++;
			}
			else if(goldEnd < predictEnd){	//if gold is before predict, add goldTag, 如果gold在predict之前
				//if goldTag is not in vTags( no in vocabulary)
				if(!vTags.containsKey(goldTag)){
					addToOOVGold(goldTag);
					totalOovTags += 1;
				}
				else{
					addToIVGold(goldTag);
					totalIvTags += 1;
				}
				i++;
			}
			//if predict is before gold, add predictTag
			else{
				j++;
			}
		}
	}
	
	
	
	/**
	 * 评估文件, 按后面的迭代次数依次评估
	 * @param trainName
	 * @param testGoldName
	 * @param testResultName
	 * @param testEvalName
	 * @param startIter
	 * @param endIter
	 * @throws IOException 
	 */
	public static void evalIter(String trainName, String testGoldName, String testResultName, String testEvalName, int startIter, int endIter) throws IOException{
		if(startIter == 0 && endIter == 0)
			eval(trainName, testGoldName, testResultName, testEvalName);
		for(int i=startIter; i<endIter; i++)
			eval(trainName, testGoldName, testResultName+"-"+i, testEvalName+"-"+i);		
	}

	public static void eval(String testGoldName, String testResultName, String testEvalName) throws IOException {
		eval(null, testGoldName, testResultName, testEvalName);
	}
	
	
	public static void eval(String trainName, String testGoldName, 
			String testResultName, String testEvalName) throws IOException{
		
		System.out.println("goldFile: "+testGoldName+", testResultFile: "+testResultName);
		
		Evaluation newEval = new Evaluation();
		if(trainName != null && trainName.trim().length() > 0)
			newEval.loadIvWords(trainName, "seg");
		newEval.evalFromFile(testGoldName, testResultName, "seg");
		newEval.storeEvalFile(testEvalName+"-seg");
		
		newEval = new Evaluation();
		if(trainName != null && trainName.trim().length() > 0)
			newEval.loadIvWords(trainName, "segpos");
		newEval.evalFromFile(testGoldName, testResultName, "segpos");
		newEval.storeEvalFile(testEvalName+"-segpos");	
	}
	
	
	public static void oneExample(String[] args) throws IOException {
		String trainName = null;
		int i=0;
		if(args.length == 4) {
			trainName = args[i];
			i++;
		}		
		String testGoldName = args[i];
		String testResultName = args[i+1];
		String testEvalName = args[i+2];
		eval(trainName, testGoldName, testResultName, testEvalName);		
	}


	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//
		if(args.length == 3 || args.length == 4)
			oneExample(args);
		else {	
			String data = "ctb5-test";
			String type = "segpos";
			for(int i=1; i<50; i++) {
				String trainName = "/home/tm/disk/disk1/pos-chunk-rerank/20wp";
				String testGoldName = "/home/tm/disk/disk1/pos-chunk-rerank/20wp";
				String testResultName = "/home/tm/disk/disk1/pos-chunk-rerank/15-18pos-result/20w.result-" + i;
				String testEvalName = "/home/tm/disk/disk1/pos-chunk-rerank/15-18pos-result/20w.eval-" + i;
				eval(trainName, testGoldName, testResultName, testEvalName);
			}			
		}
	}
}
