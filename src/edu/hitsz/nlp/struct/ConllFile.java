package edu.hitsz.nlp.struct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;

//import edu.hitsz.nlp.util.FeatureTemplate;

/**
 * a conll-like file
 *  
 * <p> whose sentences are usually seperated by a blank line,
 * and one sentence contains many lines, usually one token one line.
 * 
 * <p> such as
 * <pre> word1 tag1 ...
 * word2 tag2 ...
 * ...
 * 
 * word1 tag1 ...
 * ....
 * 
 * </pre>
 * 
 * <p> 包含的功能如下
 * <p> 
 * This class is commonly used for file transformation.
 * @author tm
 *
 */
public class ConllFile {

	public int sentenceNumber;
	public ArrayList<ConllSentence> totalSentence;

	public ConllFile(){
		sentenceNumber = 0;
		totalSentence = new ArrayList<ConllSentence>();
	}

	public int getSentenceNumber(){
		return this.sentenceNumber;
	}

	public ArrayList<ConllSentence> getTotalSentence(){
		return this.totalSentence;
	}

	public ConllSentence getSentence(int i){
		if(i< this.sentenceNumber)
			return this.totalSentence.get(i);
		else{
			System.out.println("the required sentence "+i+" is exceed the number of sentences "+this.sentenceNumber+" in file");
			return null;
		}
	}

	/**
	 * read file
	 * @param trainFileName Name of the Input File
	 * @param size Number of inputing sentences: size=-1 denotes all the sentences
	 */
	public void readFrom(String trainFileName, int size){
		if(size <= 0)
			size=Integer.MAX_VALUE;
		int stopsignal=0;
		try{
			String encoding = FileEncoding.getCharset(trainFileName); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(trainFileName)), encoding)); 
			
			System.out.println("Read Training file successed:");
			String tempString=null;
			ArrayList<String> tempSentence=new ArrayList<String>();
			System.out.print("Have Read ");
			while ((tempString = reader.readLine())!= null && stopsignal==0){
				if(!tempString.trim().equals("")){
					tempSentence.add(tempString);
					tempString = reader.readLine();
					while ( tempString != null && !tempString.trim().equals("") ) {
						tempSentence.add(tempString);
						tempString = reader.readLine();
					}
					//
					if (tempSentence != null && tempSentence.size() > 0){
						//System.out.println("Have Read the "+sentenceNumber+"th Sentence;");
						if(sentenceNumber%10000==0)
							System.out.print(sentenceNumber+"...\n");
						else if(sentenceNumber%1000==0)
							System.out.print(sentenceNumber+"...");
						if(sentenceNumber<size){
							//System.out.println("Have Read "+sentenceNumber+"th Sentences.");
							ConllSentence sentence=new ConllSentence();
							sentence.process(tempSentence,tempSentence.size());
							//sentence.outputTrainSentence();
							totalSentence.add(sentence);
							sentenceNumber++;
						}
						else
							stopsignal=1;
						tempSentence.clear();
					}
				}
			}
			System.out.println(sentenceNumber+" Sentences is done.");
			reader.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}




	/**
	 * 将字符串特征转化为数字特征,用于简化训练样本的大小
	 * 
	 * @param hashmapFileName
	 * @param ResultFile
	 */
	public void trainChar2Numeric(String hashmapFileName, String ResultFile){
		
		if(this.sentenceNumber>0){
			
			HashMap<String,Integer> featureMap = new HashMap<String,Integer>();
			ConllFile newFile = new ConllFile();
			for(int s=0; s<this.sentenceNumber; s++){
				ArrayList<ArrayList<String>> x = this.totalSentence.get(s).getWords();
				int sampleNumber = x.size();
				int featureNumber = x.get(0).size()-1;
				ArrayList<ArrayList<String>> transFeatures = new ArrayList<ArrayList<String>>();
				for(int i=0; i<sampleNumber; i++){
					transFeatures.add(new ArrayList<String>());
				}
				//iterative every feature in each sample
				Integer tmp = 1;
				for(int i=0; i<featureNumber; i++){
					for(int j=0; j<sampleNumber; j++){
						if(featureMap.containsKey(x.get(j).get(i)))
							transFeatures.get(j).add(i+":"+featureMap.get(x.get(j).get(i)));
						else{
							featureMap.put(x.get(j).get(i), tmp);
							transFeatures.get(j).add(i+":"+tmp);
							tmp++;
						}
					}
				}
				for(int j=0; j<sampleNumber; j++){
					transFeatures.get(featureNumber).add(x.get(featureNumber).get(j));
				}
				ConllSentence newSen = new ConllSentence();
				newSen.setWords(transFeatures);
				newFile.addSentence(newSen);
				this.totalSentence.get(s).clearWords();
			}
			//store hashmap
			try{
				FileWriter outWriter = new FileWriter(hashmapFileName);
				StringBuffer newBuffer = new StringBuffer();
				Iterator<Entry<String, Integer>> iter = featureMap.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<String, Integer> entry = iter.next();
					newBuffer.append(entry.getKey()+"\t"+entry.getValue()+"\n");
				}
				newBuffer.append("\n");
				outWriter.write(newBuffer.toString());
				outWriter.close();
			}
			catch(Exception e){
				System.out.println("Exception" + e);
				System.exit(-1);
			}
			// store trainFile
			try{
				FileWriter outWriter = new FileWriter(ResultFile);
				newFile.store(ResultFile, true, true);
			}
			catch(Exception e){
				System.out.println("Exception" + e);
				System.exit(-1);
			}
			System.out.println("numerize features done");
		}
		
	}






	/**
	 * select the columns from the file, and delete the other columns
	 * @param columnString Like 0,1-4,6
	 */
	public void selectColumns(String columnString){
		ArrayList<Integer> columns = getColumnsFromString(columnString);
		if(sentenceNumber > 0){
			for(int i=0; i<sentenceNumber; i++)
				totalSentence.get(i).selectColumns(columns);
		}
	}



	/**
	 * add another file with given columns， 加入另一个文件的特定列
	 * @param addFileName
	 * @param addColumnString
	 */
	public void addFileColumns(String addFileName, String addColumnString){
		ConllFile addFile = new ConllFile();
		addFile.readFrom(addFileName, -1);
		addFileColumns(addFile, addColumnString);
	}

	
	/**
	 * add another file with given columns， 加入另一个文件的特定列
	 * @param addFile
	 * @param addColumnString
	 */
	public void addFileColumns(ConllFile addFile, String addColumnString){
		int addFileSentenceNumber = addFile.getSentenceNumber();
		if(addFileSentenceNumber != this.sentenceNumber){
			System.out.println("Error: the two files have different number of sentences");
			System.exit(-1);
		}
		ArrayList<Integer> addColumns = getColumnsFromString(addColumnString);
		if(sentenceNumber > 0){
			for(int i=0; i<sentenceNumber; i++)
				totalSentence.get(i).addSentenceColumns(addFile.getSentence(i),addColumns);
		}
	}


	/**
	 * convert columnString in ArrayList of columns， 从字符串中得到需要的列
	 * "0,1-4,6" to [0,1,2,3,4,6]
	 * @param columnString
	 * @return
	 */
	public ArrayList<Integer> getColumnsFromString(String columnString){
		ArrayList<Integer> columns = new ArrayList<Integer>();
		String[] partColumnString = columnString.split(",");
		if(partColumnString.length > 0){
			for(String tmp : partColumnString){
				String[] part = tmp.split("-");
				if(part.length == 1)
					columns.add(Integer.parseInt(part[0]));
				else if(part.length == 2){
					int start = Integer.parseInt(part[0]);
					int end = Integer.parseInt(part[1]);
					if((columns.size() > 0 && start < columns.get(columns.size()-1))
						|| start > end){
						System.out.println("Your input "+columnString+" is wrong");
						System.exit(-1);
					}
					for(int i=start; i<=end; i++){
						columns.add(i);
					}
				}
				else{
					System.out.println("Your input "+columnString+" is wrong");
					System.exit(-1);
				}
			}
		}
		if(columns.size()<1){
			System.out.println("Your input "+columnString+" is less than one");
			System.exit(-1);
		}
		return columns;
	}




	/**
	 * split sentences in input file into k files according to sentence,
	 * 将整个文件分成k份，每test子份有"句子数目/k"个句子,每个train子份有"句子数目*(kth-1)/kth"
	 * @param kth
	 * @param outFileName
	 * @param encodeType type of encoding: acsii, utf8
	 */
	public void split(int kth, String outFileName){
		for(int i=0;i<kth;i++){
			String trainName=outFileName+"-train-"+i;
			String testName=outFileName+"-test-"+i;
			ConllFile trainFile=new ConllFile();
			ConllFile testFile=new ConllFile();
			for(int j=0;j<this.sentenceNumber;j++){
				if(j>=(i*this.sentenceNumber/kth)&&j<((i+1)*this.sentenceNumber/kth))
					testFile.addSentence(this.totalSentence.get(j));
				else
					trainFile.addSentence(this.totalSentence.get(j));
			}
			testFile.store(testName, true, true);
			trainFile.store(trainName, true, true);
		}
	}




	/**
	 * split sentences in input file into k files according to random selection
	 * 将整个文件分成k份，句子的顺序是随机的，但是句子数目大致相等
	 * @param kth
	 * @param outFileName
	 * @param randomListName 随机句子列表
	 */
	public void splitRandom(int kth, String outFileName, String randomListName){
		ArrayList<Integer> randomList=new ArrayList<Integer>();
		//generate random number in (0,kth)
		for(int i=0;i<this.sentenceNumber;i++){
			int randomNumber=(int)Math.floor(Math.random()*5);
			randomList.add(randomNumber);
		}
		//
		for(int i=0;i<kth;i++){
			String trainName=outFileName+"-train-"+i;
			String testName=outFileName+"-test-"+i;
			ConllFile trainFile=new ConllFile();
			ConllFile testFile=new ConllFile();
			for(int j=0;j<this.sentenceNumber;j++){
				if(randomList.get(j)==i)
					testFile.addSentence(this.totalSentence.get(j));
				else
					trainFile.addSentence(this.totalSentence.get(j));
			}
			testFile.store(testName, true, true);
			trainFile.store(trainName, true, true);
		}
		//store random list file
		try{
			FileWriter listFile=new FileWriter(randomListName);
			for(Integer i : randomList){
				listFile.write(i+"\n");
			}
			listFile.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}


	/**
	 * combine sentences in many files into one file
	 * 将多个文件合成一个文件
	 * @param inName File name with number k, the format is "name-i"
	 * @param kth k files
	 */
	public void combine(String inName, int kth){
		for(int i=0;i<kth;i++){
			String inFileName=inName+"-"+i;
			ConllFile testFile=new ConllFile();
			testFile.readFrom(inFileName, 0);
			this.addFile(testFile);
		}
	}

	/**
	 * 合并随机文件
	 * @param fileName 输入文件名，“file"表示个个随机文件为“file-i"
	 * @param randomListName
	 * @param kth
	 */
	public void combineRandom(String fileName, String randomListName, int kth){
		//read random list file
		ArrayList<Integer> randomList=new ArrayList<Integer>();
		File listFile=new File(randomListName);
		BufferedReader reader=null;
		try{
			reader=new BufferedReader(new FileReader(listFile));
			String tmp=null;
			while((tmp=reader.readLine())!=null)
				randomList.add(Integer.parseInt(tmp.trim()));
			reader.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
		int listNumber=randomList.size();
		//create seqlist to record the sequence of each sentence in each subfile at the entire file
		ArrayList<Integer> seqList=new ArrayList<Integer>();
		ArrayList<Integer> sigList=new ArrayList<Integer>();
		for(int i=0;i<kth;i++)
			sigList.add(-1);
		for(int i=0;i<listNumber;i++){
			int j=randomList.get(i);
			int k=sigList.get(j)+1;
			sigList.set(j,k);
			seqList.add(k);
		}
		//
		ConllFile outFile=new ConllFile();
		outFile.addEmptySentences(listNumber);
		int testSig=0;
		for(int i=0;i<kth;i++){
			String inFileName=fileName+"-"+i;
			ConllFile testFile=new ConllFile();
			testFile.readFrom(inFileName, 0);
			testSig+=testFile.sentenceNumber;
			for(int j=0;j<listNumber;j++){
				if(randomList.get(j)==i){
					outFile.totalSentence.set(j, testFile.totalSentence.get(seqList.get(j)));
				}
			}
		}
		if(testSig!=listNumber){
			System.out.println("The number in list and separate file don't matter. Please check");
		}
	}

	
	/**
	 * 根据标点符号分割句子，用于中文
	 */
	public void splitPuncts(){
		ArrayList<String> pucts = new ArrayList<String>();
		pucts.add("。");pucts.add("？");pucts.add("！");
		//pucts.add("，");pucts.add("。");pucts.add("；");
		ArrayList<ConllSentence> newSentences = new ArrayList<ConllSentence>();
		System.out.println("spliting 0...");
		for(int i=0; i<sentenceNumber; i++){
			if(i%10000 == 0)
				System.out.println(i+"...");
			else if(i%1000 == 0)
				System.out.print(i+"...");
			newSentences.addAll(totalSentence.get(i).splitPuncts(pucts));
		}
		this.totalSentence = newSentences;
		this.sentenceNumber = newSentences.size();
	}


	/**
	 * 根据标点符号分割句子，用于中文,同时与另一个文件保持一致
	 */
	public void splitPuncts(ConllFile secondFile){
		if(this.sentenceNumber != secondFile.sentenceNumber){
			System.out.println(this.getClass().getName()+ ":"+ this.getClass().getMethods()[0].getName());
			System.exit(-1);
		}
		ArrayList<String> pucts = new ArrayList<String>();
		pucts.add("。");pucts.add("？");pucts.add("！");
		ArrayList<ConllSentence> newSentences = new ArrayList<ConllSentence>();
		System.out.println("spliting 0...");
		for(int i=0; i<sentenceNumber; i++){
			if(i%10000 == 0)
				System.out.println(i+"...");
			else if(i%1000 == 0)
				System.out.print(i+"...");
			newSentences.addAll(totalSentence.get(i).splitPuncts(pucts, secondFile.totalSentence.get(i)));
		}
		this.totalSentence = newSentences;
		this.sentenceNumber = newSentences.size();
	}


	/**
	 * add new sentence to file, not hard copy
	 * @param newSentence
	 */
	public void addSentence(ConllSentence newSentence){
		this.sentenceNumber++;
		this.totalSentence.add(newSentence);
	}

	/**
	 * add an empty sentence
	 * @param length
	 */
	public void addEmptySentence(int length){
		ConllSentence newSen=new ConllSentence();
		newSen.setSentenceLength(length);
		this.addSentence(newSen);
	}

	/**
	 * add several empty sentences to file
	 * @param nth
	 */
	public void addEmptySentences(int nth){
		for(int i=0;i<nth;i++){
			ConllSentence newSen=new ConllSentence();
			this.addSentence(newSen);
		}
	}

	/**
	 * add sentences of one file into this file, not hard copy
	 * 将一个文件中的所有句子加入到另一个文件中，用于文件合并
	 * @param newFile
	 */
	public void addFile(ConllFile newFile){
		this.sentenceNumber+=newFile.sentenceNumber;
		this.totalSentence.addAll(newFile.totalSentence);
	}


	/**
	 * reverse order of sentences. 翻转文件中的句子顺序
	 * It operates like first storing the nth sentence, then n-1th, ... , final the first one.
	 * @param fileName
	 */
	public void reverseSentences(){
		ArrayList<ConllSentence> newTotalSentence = new ArrayList<ConllSentence>();
		for(int i=sentenceNumber-1; i>=0; i--){
			newTotalSentence.add(this.totalSentence.get(i));
		}
		this.totalSentence = newTotalSentence;
	}

	/**
	 * reverse order of columns in sentences，翻转每个句子中的列次序
	 */
	public void reverseColumn(){
		for(int i=0;i<sentenceNumber;i++){
			totalSentence.get(i).reverseColumn();
		}
	}

	/**
	 * reverse order of rows in sentence，翻转每个句子中的行次序
	 */
	public void reverseRow(){
		for(int i=0;i<sentenceNumber;i++){
			totalSentence.get(i).reverseRow();
		}
	}
	
	/**
	 * overturn the matrix of words in sentences, 以矩阵翻转整个句子
	 * 1 a    1 2 3
	 * 2 b -> a b c
	 * 3 c
	 */
	public void overturn(){
		if(sentenceNumber > 0){
			for(int i=0; i<sentenceNumber; i++)
				totalSentence.get(i).overturn();
		}
	}

	
	/**
	 * 删除文件的某个句子
	 * @param i
	 */
	public void removeSentence(int i){
		if(i>=this.sentenceNumber){
			System.out.println(this.getClass().getName()+": sentence number removed is larger than sentencenumber in file");
			System.exit(-1);
		}
		this.totalSentence.remove(i);
		this.sentenceNumber--;
	}

	
	
	/** 选择列表中的句子数*/
	public void selectSentences(ArrayList<Integer> number) {
		ArrayList<ConllSentence> newSentences = new ArrayList<ConllSentence>();
		
		for(int i : number) {
			if(i < sentenceNumber) {
				newSentences.add(totalSentence.get(i));
			}
			else {
				System.out.println("wrong sentence number");
				System.exit(-1);
			}
		}
		this.totalSentence = newSentences;
		this.sentenceNumber = newSentences.size();
		
	}
	
	
	/**
	 * 选择长度>=minLength的所有句子
	 * @param minLength
	 */
	public void selectSentenceMinLength(int minLength){
		for(int i=sentenceNumber-1; i>=0; i--){
			if(this.totalSentence.get(i).getSentenceLength()<minLength)
				this.totalSentence.remove(i);
		}
		this.sentenceNumber = this.totalSentence.size();
	}


	/**
	 * 选取文件中的部分句子
	 * @param start 句子的起始点
	 * @param end 句子的结束点
	 */
	public void subFile(int start, int end){
		if(start>=end || start<0 || end >sentenceNumber){
			System.out.println(this.getClass().getName()+ ": the range between start and end is wrong");
			System.out.println();
		}
		ArrayList<ConllSentence> newSentences = new ArrayList<ConllSentence>();
		for(int i=start; i<end; i++)
			newSentences.add(this.totalSentence.get(i));
		this.totalSentence = newSentences;
		this.sentenceNumber = newSentences.size();
	}
	
	/**
	 * 选取文件中的部分句子
	 * @param start 句子的起始点
	 */
	public void subFile(int start){
		subFile(start, sentenceNumber);
	}


	
	/**
	 * 根据问题类型抽取特征，包括pos，chunk，poschunk, chunkpos
	 * <p> 最好不同
	 * @param newFile
	 * @param newTemplate
	 * @throws IOException
	 */
	public void extractAndStoreFeatures(FileWriter newFile, String type) throws IOException{
		System.out.println("\nStore features in file:");
		int senNum=this.sentenceNumber;
		for(int i=0;i<senNum;i++){
			if(i%100==0)
				System.out.println(i+"...");
			this.totalSentence.get(i).extractAndStoreFeatures(newFile, type);
		}
		System.out.println("Store features done in total "+senNum+" sentences.");
	}


	/**
	 * generate all possible denotations of a file
	 *
	 */
	public ArrayList<String> generateResultTags(){
		ArrayList<String> fileResultSignal = new ArrayList<String>();
		for(int i=0;i<sentenceNumber;i++){
			int sentenceLen=totalSentence.get(i).getSentenceLength();
			for(int j=0;j<sentenceLen;j++){
				if(!fileResultSignal.contains(totalSentence.get(i).getResultSignal().get(j))){
					fileResultSignal.add(totalSentence.get(i).getResultSignal().get(j));
				}
			}
		}
		return fileResultSignal;
	}

	
	/**
	 * generate all possible denotations of a file
	 *
	 * @param outFileName
	 */
	public void generateResultTags(String outFileName){
		ArrayList<String> fileResultSignal = generateResultTags();
		//store it
		try{
			FileWriter outFileWriter=new FileWriter(outFileName);
			int resultSignalNum=fileResultSignal.size();
			for(int i=0;i<resultSignalNum;i++){
				outFileWriter.write(fileResultSignal.get(i)+"\n");
			}
			outFileWriter.close();
			System.out.println("\nStore result Signal (Containing Tags) file done!");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	
	/**
	 * 将句子相互连接，如果句子的末尾为逗号，用于连接中文句子
	 * @param newFile
	 */
	public void concatSentence(){
		ArrayList<String> words = new ArrayList<String>();
		words.add("，");words.add("：");words.add("；");
		for(int i=0; i<sentenceNumber; i++){
			if(i%10000 == 0)
				System.out.println(i+"...");
			else if(i%1000 == 0)
				System.out.print(i+"...");
			ConllSentence newSentence = totalSentence.get(i);
			int sentenceLength = newSentence.getSentenceLength();
			if(words.contains(newSentence.getWords(0).get(sentenceLength-1))){
				if(i<sentenceNumber){
					ConllSentence nextSentence = totalSentence.get(i+1);
					newSentence.addSentence(nextSentence);
					removeSentence(i+1);
					i--;
				}
			}
		}
	}

	/**
	 * 删除在某列中包含某个tag的句子
	 * @param column
	 * @param tag
	 */
	public void removeSentenceWithTag(int column, String tag){
		for(int i=sentenceNumber-1; i>=0; i--){
			if(totalSentence.get(i).containsTag(column,tag)){
				totalSentence.remove(i);
			}
		}
		this.sentenceNumber = this.totalSentence.size();
	}


	/**
	 * store sentences in file
	 * @param outfileName
	 * @param isSentenceSpaced Whether sentences is spaced: true for yes, false for no
	 * @param isWordTabed Type of separator between words: true for Tab, false for blank space
	 */
	public void store(String outfileName, boolean isSentenceSpaced, boolean isWordTabed){
		try{
			FileWriter outWriter = new FileWriter(outfileName);
			for(int i=0;i<sentenceNumber;i++){
				totalSentence.get(i).store(outWriter, isWordTabed);
				if(isSentenceSpaced)
					outWriter.write("\n");
			}
			outWriter.close();
			System.out.println("store file "+outfileName+" is done");
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * display sentences in file
	 */
	public void output(){
		for(int i=0;i<sentenceNumber;i++)
			totalSentence.get(i).output();
	}

	

	
	
	public static void splitExample() {
		
		String firstFileName = "/home/tm/disk/disk1/pos-chunk-rerank/pos/15-18wp";
		String outFileName = "/home/tm/disk/disk1/pos-chunk-rerank/pos/15-18wp";
		
		ConllFile file = new ConllFile();
		file.readFrom(firstFileName, -1);
		file.split(5, outFileName);
		
	}

	

	public static void main(String[] args){		
		splitExample();
	}

}
