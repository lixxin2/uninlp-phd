package edu.hitsz.nlp.poschunkrerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.nlp.partofspeech.POSInstance;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;

public class POSChunkRerankMert {
	
	/** 
	 * 由poschunk原始文件得到Zmert训练用到的ref文件 
	 * @param inputPOSChunkFileName
	 * @param refFileName
	 * @throws IOException
	 */
	public static void getRefPOSChunk(String inputPOSChunkFileName, String refFileName) throws IOException {
		
		ConllFile trainFile=new ConllFile();
		trainFile.readFrom(inputPOSChunkFileName, 0);
		FileWriter writer = new FileWriter(new File(refFileName));
		
		int sentenceNum=trainFile.getSentenceNumber();
		System.out.print("Sentence ");
		for(int j=0;j<sentenceNum;j++){
			if(j%100==0){
				System.out.print(Integer.toString(j)+"...");
				if(j%1000==0)
					System.out.println();
			}
			ConllSentence sentence = trainFile.getSentence(j);
			ArrayList<String> words = sentence.getWords(0);
			ArrayList<String> poses = sentence.getWords(1);
			ArrayList<String> chunks = sentence.getWords(2);
			int length = words.size();
			if(length != poses.size() || length != chunks.size()) {
				System.out.println("format of input file is wrong");
				System.exit(-1);
			}
			StringBuffer sbuf = new StringBuffer();
			for(int i=0; i<length; i++)
				sbuf.append(poses.get(i) + "\t");
			for(int i=0; i<length; i++)
				sbuf.append(chunks.get(i) + "\t");
			writer.write(sbuf.toString().trim() + "\n");
		}
		writer.close();			
	}
	
	/**
	 * 由poschunk原始文件得到Zmert训练用到的src.txt文件
	 * @param inputPOSChunkFileName
	 * @param srcFileName
	 * @throws IOException
	 */
	public static void getSrcWord(String inputPOSChunkFileName, String srcFileName) throws IOException {
		ConllFile trainFile=new ConllFile();
		trainFile.readFrom(inputPOSChunkFileName, 0);
		FileWriter writer = new FileWriter(new File(srcFileName));
		
		int sentenceNum=trainFile.getSentenceNumber();
		System.out.print("Sentence ");
		for(int j=0;j<sentenceNum;j++){
			if(j%100==0){
				System.out.print(Integer.toString(j)+"...");
				if(j%1000==0)
					System.out.println();
			}
			ConllSentence sentence = trainFile.getSentence(j);
			ArrayList<String> words = sentence.getWords(0);
			int length = words.size();
			StringBuffer sbuf = new StringBuffer();
			for(int i=0; i<length; i++)
				sbuf.append(words.get(i) + "\t");
			writer.write(sbuf.toString().trim() + "\n");
		}
		writer.close();		
	}
	
	
	private static ArrayList<String []> getNextColumn(BufferedReader inputReader) throws IOException {

		ArrayList<String[]> lineList = new ArrayList<String[]>();
		String line = inputReader.readLine();
		while (line != null && !line.trim().equals("")) {
			String[] subs = line.trim().split("\\s+");
		    line = inputReader.readLine();
			if(subs.length < 1) {
				continue;
			}
		    lineList.add(subs);
		    //System.out.println("## "+line);
		}
		int wordSize = lineList.size();
		if(wordSize == 0) {
		    inputReader.close();
		    return null;
		}
		//分配词和词性
		String[] words = new String[wordSize];
		String[] poses = new String[wordSize];
		String[] chunks = new String[wordSize];
		for(int i = 0; i < wordSize; i++) {
		    String[] info = lineList.get(i);
		    words[i] = info[0];
		    if(info.length > 1) {
		    	poses[i] = info[1];
		    	if(info.length > 2)
		    		chunks[i] = info[2];
		    }
		}
		ArrayList<String []> allWords = new ArrayList<String []>();
		allWords.add(words); allWords.add(poses); allWords.add(chunks);
		return allWords;
    }
	
	/**
	 * 只通过pos chunk两个任务的
	 * @param nbestFileName
	 * @param nbestProbFileName
	 * @param sentenceNumber
	 * @param nbest
	 * @param candFileName
	 * @throws IOException
	 */
	public static void getCandDatabase(String nbestFileName, String nbestProbFileName, 
			int sentenceNumber, int nbest,
			String candFileName) throws IOException {
		
		BufferedReader nbestFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(nbestFileName)));
		BufferedReader nbestProbFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(nbestProbFileName)));
		FileWriter writer = new FileWriter(new File(candFileName));
		
		for(int i=0; i<sentenceNumber; i++) {
			for(int j=0; j<nbest; j++) {
				StringBuffer sbuf = new StringBuffer();
				sbuf.append(i); sbuf.append(" |||");
				
				ArrayList<String[] > allWords = getNextColumn(nbestFileReader);
				if(allWords == null) {
					System.out.println("no input nbest");
					System.exit(-1);
				}
				String[] poses = allWords.get(1);
				String[] chunks = allWords.get(2);
				int length = poses.length;
				for(int k=0; k<length; k++) {
					sbuf.append(" "); sbuf.append(poses[k]); 
				}
				for(int k=0; k<length; k++) {
					sbuf.append(" "); sbuf.append(chunks[k]);
				}
				sbuf.append(" |||");
				
				String line = nbestProbFileReader.readLine();
				if(line == null) {
					System.out.println("no input nbestProb");
					System.exit(-1);
				}
				String[] probs = line.split("\t");
				for(String prob : probs) {
					sbuf.append(" "); sbuf.append(prob);
				}
				writer.write(sbuf.toString() + "\n");
			}
			System.out.println(i + "...");
		}
		nbestFileReader.close();
		nbestProbFileReader.close();
		writer.close();	
				
	}
	
	
	
	public static void getFinalResultBase(String kbestFileName, String probFileName,
			int sentenceNumber, int nbest, double[] weights,
			String bestFileName) throws IOException {
		
		BufferedReader nbestFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(kbestFileName)));
		BufferedReader probFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(probFileName)));
		FileWriter writer = new FileWriter(new File(bestFileName));
		
		int weightLength = weights.length;
		
		for(int i=0; i<sentenceNumber; i++) {
			double bestProb = -1e10;
			String bestSent = "";
			
			for(int j=0; j<nbest; j++) {
				
				String curSent = getNextInString(nbestFileReader);
				String mertLine = probFileReader.readLine();
				//int position1 = mertLine.indexOf("|||");
				//int position2 = mertLine.indexOf("|||", position1 + 3);
				//String[] probString = mertLine.substring(position2+3).trim().split(" ");
				String[] probString = mertLine.trim().split("[ \t]");
				if(weightLength != probString.length) {
					System.out.println("length of weights and probs is different");
					System.exit(-1);
				}					
				double curScore = 0.0;
				for(int k=0; k<weightLength; k++)
					curScore += weights[k] * Double.parseDouble(probString[k]);
				if(curScore > bestProb) {
					bestProb = curScore;
					bestSent = curSent; 
				}				
			}
			writer.write(bestSent + "\n");
			System.out.println(i + "...");
		}
		nbestFileReader.close();
		probFileReader.close();
		writer.close();	
				
		
	}
	
	
	private static String getNextInString(BufferedReader inputReader) throws IOException {

		StringBuffer sbuf = new StringBuffer();
		String line = inputReader.readLine();
		int count = 0;
		while (line != null && !line.trim().equals("")) {
			sbuf.append(line.trim() + "\n");
		    line = inputReader.readLine();
		    count += 1;
		    //System.out.println("## "+line);
		}		
		if(count == 0) {
		    inputReader.close();
		    return null;
		}
		return sbuf.toString();
    }
	
	/** 抽取出一列作为一行, 用来训练ngram */
	public static void extractColumn2Line(String inputPOSChunkFileName, int index, 
			String outFileName) throws IOException {
		
		ConllFile trainFile=new ConllFile();
		trainFile.readFrom(inputPOSChunkFileName, 0);
		FileWriter writer = new FileWriter(new File(outFileName));
		
		int sentenceNum=trainFile.getSentenceNumber();
		System.out.print("Sentence ");
		for(int j=0;j<sentenceNum;j++){
			if(j%100==0){
				System.out.print(Integer.toString(j)+"...");
				if(j%1000==0)
					System.out.println();
			}
			ConllSentence sentence = trainFile.getSentence(j);
			ArrayList<String> words = sentence.getWords(index);
			int length = words.size();
			StringBuffer sbuf = new StringBuffer();
			for(int i=0; i<length; i++)
				sbuf.append(words.get(i) + " ");
			writer.write(sbuf.toString().trim() + "\n");
		}
		writer.close();	
	}
	
	
	public static void getCandDatabaseMert(
			String nbestFileName, String nbestBaseProbFileName, 
			int sentenceNumber, int nbest,
			String posLmFileName, String chunkLmFileName, String wordPOSCoFileName, 
			String POSWordCoFileName, String wordChunkCoFileName, String chunkWordCoFileName, 
			String POSChunkCoFileName, String ChunkPOSCoFileName,			
			String candFileName) throws IOException {
		
		POSChunkRerankPipe pipe = new POSChunkRerankPipe();
		pipe.loadPOSLmBin(posLmFileName);
		pipe.loadChunkLmBin(chunkLmFileName);
		pipe.loadWordPOSCo(wordPOSCoFileName);
		pipe.loadPOSWordCo(POSWordCoFileName);
		pipe.loadWordChunkCo(wordChunkCoFileName);
		pipe.loadChunkWordCo(chunkWordCoFileName);
		pipe.loadPOSChunkCo(POSChunkCoFileName);
		pipe.loadChunkPOSCo(ChunkPOSCoFileName);
		
		BufferedReader nbestFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(nbestFileName)));
		BufferedReader nbestBaseProbFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(nbestBaseProbFileName)));
		FileWriter writer = new FileWriter(new File(candFileName));
				
		for(int i=0; i<sentenceNumber; i++) {
			for(int j=0; j<nbest; j++) {
				// seq
				StringBuffer sbuf = new StringBuffer();
				sbuf.append(i); sbuf.append(" |||");
				// read sentence
				ArrayList<String[] > allWords = getNextColumn(nbestFileReader);
				if(allWords == null) {
					System.out.println("no input nbest");
					System.exit(-1);
				}
				String[] words = allWords.get(0);
				String[] poses = allWords.get(1);
				String[] chunks = allWords.get(2);
				// candidate
				int length = poses.length;
				for(int k=0; k<length; k++) {
					sbuf.append(" "); sbuf.append(poses[k]); 
				}
				for(int k=0; k<length; k++) {
					sbuf.append(" "); sbuf.append(chunks[k]);
				}
				sbuf.append(" |||");
				//base prob
				String line = nbestBaseProbFileReader.readLine();
				if(line == null) {
					System.out.println("no input nbestProb");
					System.exit(-1);
				}
				String[] probs = line.split("\t");
				for(String prob : probs) {
					sbuf.append(" "); sbuf.append(prob);
				}
				//
				double posLmProb = pipe.getPOSLmProb(poses);
				double chunkLmProb = pipe.getChunkLmProb(chunks);
				double wordPOSCoProb = pipe.getWordPOSCoProb(words, poses);
				double posWordCoProb = pipe.getPOSWordCoProb(poses, words);
				double wordChunkCoProb = pipe.getWordChunkCoProb(words, chunks);
				double chunkWordCoProb = pipe.getChunkWordCoProb(chunks, words);
				double posChunkCoProb = pipe.getPOSChunkCoProb(poses, chunks);
				double chunkPOSCoProb = pipe.getChunkPOSCoProb(chunks, poses);
				
				sbuf.append(" ");sbuf.append(posLmProb);
				sbuf.append(" ");sbuf.append(chunkLmProb);
				sbuf.append(" ");sbuf.append(wordPOSCoProb);
				sbuf.append(" ");sbuf.append(posWordCoProb);
				sbuf.append(" ");sbuf.append(wordChunkCoProb);
				sbuf.append(" ");sbuf.append(chunkWordCoProb);
				sbuf.append(" ");sbuf.append(posChunkCoProb);
				sbuf.append(" ");sbuf.append(chunkPOSCoProb);				
				
				writer.write(sbuf.toString() + "\n");
			}
			System.out.println(i + "...");
		}
		nbestFileReader.close();
		nbestBaseProbFileReader.close();
		writer.close();	
				
	}
	
	/** 根据权重值,从kbest中选择出最优候选 */
	public static void getFinalResult(String kbestFileName, String probFileName,
			int sentenceNumber, int nbest, double[] weights,
			String bestFileName) throws IOException {
		
		BufferedReader nbestFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(kbestFileName)));
		BufferedReader probFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(probFileName)));
		FileWriter writer = new FileWriter(new File(bestFileName));
		
		int weightLength = weights.length;
		
		for(int i=0; i<sentenceNumber; i++) {
			double bestProb = -1e10;
			String bestSent = "";
			
			for(int j=0; j<nbest; j++) {
				
				String curSent = getNextInString(nbestFileReader);
				String mertLine = probFileReader.readLine();
				int position1 = mertLine.indexOf("|||");
				int position2 = mertLine.indexOf("|||", position1 + 3);
				String[] probString = mertLine.substring(position2+3).trim().split(" ");
				if(weightLength != probString.length) {
					System.out.println("length of weights and probs is different");
					System.exit(-1);
				}					
				double curScore = 0.0;
				for(int k=0; k<weightLength; k++)
					curScore += weights[k] * Double.parseDouble(probString[k]);
				if(curScore > bestProb) {
					bestProb = curScore;
					bestSent = curSent; 
				}				
			}
			writer.write(bestSent + "\n");
			System.out.println(i + "...");
		}
		nbestFileReader.close();
		probFileReader.close();
		writer.close();	
				
		
	}
	public static void main(String[] args) throws IOException {
		/*
		String inputPOSChunkFileName = "/home/tm/disk/disk1/pos-chunk-rerank/15-18wpc";
		String refFileName = "/home/tm/disk/disk1/pos-chunk-rerank/ref";
		//GenerateMertDataset.getRefPOSChunk(inputPOSChunkFileName, refFileName);
		String srcFileName = "/home/tm/disk/disk1/pos-chunk-rerank/src.txt";
		//GenerateMertDataset.getSrcWord(inputPOSChunkFileName, srcFileName);
		 */
		
		/*
		//base weights
		String nbestFileName = "/home/tm/disk/disk1/pos-chunk-rerank/6.500best/all.500best";
		String nbestProbFileName = "/home/tm/disk/disk1/pos-chunk-rerank/6.500best/all.500best.prob";  
		String candFileName = "/home/tm/disk/disk1/pos-chunk-rerank/cand.txt";
		//GenerateMertDataset.getCandDatabaseBase(nbestFileName, nbestProbFileName, 8936, 500, candFileName);
		nbestFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/result/500best.wpc";
		String probFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/result/500best.prob";
		int sentenceNumber = 2012; //8936 , 2012
		int nbest = 500;
		double[] weights = {1.0, 0.11615531516336033};
		String bestFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/result/500best.result.poschunk0.33";
		//GenerateMertDataset.getFinalResultBase(kbestFileName, probFileName, sentenceNumber, nbest, weights, bestFileName);
		*/
		
		/*
		inputPOSChunkFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.wpc";
		String rawPOSFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.pos.raw";
		String rawChunkFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.chunk.raw";
		//GenerateMertDataset.extractColumn2Line(inputPOSChunkFileName, 1, rawPOSFileName);
		//POSChunkRerankMert.extractColumn2Line(inputPOSChunkFileName, 2, rawChunkFileName);
		*/
		
		
		String nbestFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/base_result/500best.wpc";
		String nbestBaseProbFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/base_result/500best.prob";
		String candFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/mert_result/500best.mert10";
		
		String posLmFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.pos.raw.lm4.bin";
		String chunkLmFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.chunk.raw.lm4.bin";
		String wordPOSCoFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.wordpos.stat";
		String POSWordCoFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.posword.stat";
		String wordChunkCoFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.wordchunk.stat";
		String chunkWordCoFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.chunkword.stat";
		String POSChunkCoFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.poschunk.stat";
		String ChunkPOSCoFileName = "/home/tm/disk/disk1/pos-chunk-rerank/raw/brownptb.chunkpos.stat";
		
		int sentenceNumber = 2012;
		int nbest = 500;
		
		//POSChunkRerankMert.getCandDatabaseMert(nbestFileName, nbestBaseProbFileName, sentenceNumber, 500, 
		//		posLmFileName, chunkLmFileName, wordPOSCoFileName, POSWordCoFileName,
		//		wordChunkCoFileName, chunkWordCoFileName, POSChunkCoFileName, ChunkPOSCoFileName,
		//		candFileName);
		
		double[] weights = {7.245032739845602, 0.979224152601098, 43.52293749631166, -1.6130622257008214, 145.00297938365003, 79.05004227144616, 41.82697183533859, -96.35335072063097, 0.0, 0.0
				};
		String bestFileName = "/home/tm/disk/disk1/pos-chunk-rerank/7.mert/mert_result/500best.mert10.poschunk0.33.noglobal";
		POSChunkRerankMert.getFinalResult(nbestFileName, candFileName, sentenceNumber, nbest, 
				weights, bestFileName);
		
		
		
		
	}
	

}
