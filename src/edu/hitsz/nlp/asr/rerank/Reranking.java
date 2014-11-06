package edu.hitsz.nlp.asr.rerank;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.asr.eval.ASREval;
import edu.hitsz.nlp.mst.DependencyInstance;
import edu.hitsz.nlp.pinyin2character.PyCharInstance;
import edu.hitsz.nlp.pinyin2character.PyCharOptions;
import edu.hitsz.nlp.pinyin2character.PyCharParser;
import edu.hitsz.nlp.pinyin2character.PyCharPipe;
import edu.hitsz.nlp.segpos.Instance;

public class Reranking {

	/** 
	 * 针对Julius的nbest输出结果，提取出最优结果（词序列）
	 * @since Sep 21, 2013
	 * @param testfile
	 * @param syllableFile
	 * @param resultFile
	 * @throws IOException
	 */
	public static void outputOneBestFromJulius(
			String testfile,
			String syllableFile,
			String resultFile) throws IOException {
		
		JuliusResultReader reader = new JuliusResultReader();
    	reader.startReading(testfile);
    	reader.loadSyllableMap(syllableFile);
    	
    	ArrayList<PyCharInstance> instances = reader.getJuliusResults();
    	
    	FileWriter writer = new FileWriter(resultFile);
    	
    	int cnt = 0;    		
    	while(instances != null && instances.size() > 0) {
    		
    		cnt++;
    	    if( cnt%10 == 0) {
    	    	System.out.println();
    	    }
    	    String[] words = instances.get(0).words;
    	    StringBuffer strbuf = new StringBuffer();
    	    for(String word : words)
    	    	strbuf.append(word + " ");
    	    writer.append(strbuf.toString().trim() + "\n");
    	    
    	    instances = reader.getJuliusResults();
    	}
    	writer.close();
	}
	
	
	/**
	 * 从Mert文件中提取出最优结果
	 * @since Sep 21, 2013
	 * @param mertFileName
	 * @param outName
	 * @throws IOException
	 */
	public static void outputOneBestFromMert(String mertFileName, String outName) throws IOException {
		
		FileWriter writer = new FileWriter(outName);
		//		
		int cur = -1;
				
		String fileEncoding = FileEncoding.getCharset(mertFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mertFileName), fileEncoding));
        
		String line = null;
		while((line = reader.readLine()) != null) {
			
			int delimFirst = line.indexOf("|||");
			int sentenceIndex = Integer.parseInt(line.substring(0, delimFirst).trim());
			if(sentenceIndex != cur) {
				cur = sentenceIndex;
				
				int delimSecond = delimFirst+3 + line.substring(delimFirst+3).indexOf("|||");
				String result = line.substring(delimFirst+3, delimSecond).trim() + "\n";
				writer.write(result);
			}
							
		}	
		
		reader.close();
		cur = -1;
		
		writer.close();	
		
	}
	
	
	/** 
	 * 针对Julius的nbest输出结果，提取出最优结果（拼音序列）
	 * @since Sep 21, 2013
	 * @param testfile
	 * @param syllableFile
	 * @param resultFile
	 * @throws IOException
	 */
	public static void outputOneBestYinFromJulius(
			String testfile,
			String syllableFile,
			String resultFile) throws IOException {
		
		JuliusResultReader reader = new JuliusResultReader();
    	reader.startReading(testfile);
    	reader.loadSyllableMap(syllableFile);
    	
    	ArrayList<PyCharInstance> instances = reader.getJuliusResults();
    	
    	FileWriter writer = new FileWriter(resultFile);
    	
    	int cnt = 0;    		
    	while(instances != null && instances.size() > 0) {
    		
    		cnt++;
    	    if( cnt%10 == 0) {
    	    	System.out.println();
    	    }
    	    String[] characterYins = instances.get(0).characterYins;
    	    StringBuffer strbuf = new StringBuffer();
    	    for(String characterYin : characterYins)
    	    	strbuf.append(characterYin + " ");
    	    writer.append(strbuf.toString().trim() + "\n");
    	    
    	    instances = reader.getJuliusResults();
    	}
    	writer.close();
	}
	
	
	/** 
	 * 针对Julius的nbest输出结果，根据最优选择候选序列，提取出最优结果
	 * @since Sep 21, 2013
	 * @param testfile
	 * @param syllableFile
	 * @param bestCandSeqFile
	 * @param resultFile
	 * @throws IOException
	 */
	public static void outputOneBestYinFromJulius(
			String testfile,
			String syllableFile,
			String bestCandSeqFile,
			String resultFile) throws IOException {
		
		JuliusResultReader reader = new JuliusResultReader();
    	reader.startReading(testfile);
    	reader.loadSyllableMap(syllableFile);
    	
    	String fileEncoding = FileEncoding.getCharset(bestCandSeqFile);
    	BufferedReader bestCandReader = new BufferedReader(new InputStreamReader(new FileInputStream(bestCandSeqFile), fileEncoding));
        String bestCandLine = bestCandReader.readLine();
    	
    	ArrayList<PyCharInstance> instances = reader.getJuliusResults();
    	
    	FileWriter writer = new FileWriter(resultFile);
    	
    	int cnt = 0;    		
    	while(instances != null && instances.size() > 0 && 
    			bestCandLine != null) {
    		
    		cnt++;
    	    if( cnt%10 == 0) {
    	    	System.out.println();
    	    }
    	    
    	    int bestCandIndex = Integer.parseInt(bestCandLine.split("\\s+")[1]);
    	    String[] characterYins = instances.get(bestCandIndex).characterYins;
    	    StringBuffer strbuf = new StringBuffer();
    	    for(String characterYin : characterYins)
    	    	strbuf.append(characterYin + " ");
    	    writer.append(strbuf.toString().trim() + "\n");
    	    
    	    instances = reader.getJuliusResults();
    	    bestCandLine = bestCandReader.readLine();
    	}
    	
    	writer.close();
    	bestCandReader.close();
	}
	
	/**
	 * 输出mertFile的oracle
	 * @since Nov 18, 2013
	 * @param mertFileName
	 * @param oracleK
	 * @param goldFileName
	 * @param outputFileName
	 * @throws IOException
	 */
	public static void outputOracleFromMert(
			String mertFileName,
			int oracleK,
			String goldFileName,
			String outputFileName) throws IOException {
		
		FileWriter writer = new FileWriter(outputFileName);
				
		String mertFileEncoding = FileEncoding.getCharset(mertFileName);
		BufferedReader mertReader = new BufferedReader(new InputStreamReader(new FileInputStream(mertFileName), mertFileEncoding));
        
		String goldFileEncoding = FileEncoding.getCharset(mertFileName);
		BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), goldFileEncoding));
       
		//		
		int cur = -1;
		int count = 0;
		String goldLine = null;
		String oracleLine = null;
		String line = null;
		double tmpErr = 1e2;  //
		int tmpLength = 1000; //
		
		while((line = mertReader.readLine()) != null) {
			
			int delimFirst = line.indexOf("|||");
			int sentenceIndex = Integer.parseInt(line.substring(0, delimFirst).trim());
			
			//如果换为新的句子
			if(sentenceIndex != cur) {
				if(cur > -1)
					writer.write(oracleLine + "\n");	
				goldLine = goldReader.readLine().trim();		
				cur = sentenceIndex;
				count = 0;
				oracleLine = null;
				tmpErr = 1e2;  //
				tmpLength = 1000; //	
			}
			//对于每一个句子
			if(count < oracleK) {
				int delimSecond = delimFirst+3 + line.substring(delimFirst+3).indexOf("|||");
				String predLine = line.substring(delimFirst+3, delimSecond).trim();
				ASREval eval = new ASREval();
				ArrayList<String> golds = eval.getZis(goldLine);
				ArrayList<String> preds = eval.getZis(predLine);
				int predLength = predLine.split("\\s+").length;
				eval.updateEval(golds, preds);
				double curErr = eval.getErr();
				if(Math.abs(curErr-tmpErr) < 1e-5 && predLength < tmpLength) {
					tmpLength = predLength;
					oracleLine = predLine;
				}
				else if(curErr < tmpErr) {
					tmpErr = curErr;
					tmpLength = predLength;
					oracleLine = predLine;
				}					
			}
			count++;										
		}	
		
		mertReader.close();
		goldReader.close();		
		writer.close();			
		
	}
	
	
	/** 
	 * 针对Julius的nbest list候选结果，提取出K个最优结果, 用于分析和判断julius生成结果是否正确
	 * @since Sep 21, 2013
	 * @param parser
	 * @param testfile
	 * @param syllableFile
	 * @param K
	 * @param resultFile
	 * @throws IOException
	 */
	public static void outputKBestFromJulius(
			PyCharParser parser,
			String testfile,
			String syllableFile,
			int K,
			String resultFile) throws IOException {
		
		JuliusResultReader reader = new JuliusResultReader();
    	reader.startReading(testfile);
    	reader.loadSyllableMap(syllableFile);
    	ArrayList<PyCharInstance> instances = reader.getJuliusResults();
    	
    	FileWriter writer = new FileWriter(resultFile);
    	int cnt = 0;    
		
    	while(instances != null && instances.size() > 0) {
    		
    		cnt++;
    		if(cnt == 1832)
    			System.out.println();
    	    if( cnt%10 == 0) {
    	    	System.out.println();
    	    }    	    
    	    int instanceSize = instances.size();   
    	    System.out.println(cnt + ": instanceSize: " + instanceSize);
    	    //补充为K个
    	    //for(int i=instanceSize; i<K; i++)
    	    //	instances.add(instances.get(0));
    	    parser.pipe.renewInstances(instances, K);
    	    
    	    ArrayList<ArrayList<Double>> allWeights = new ArrayList<ArrayList<Double>>();		    
		    parser.outputInstancesWeights(writer, cnt-1, instances, allWeights, K);    	    
    	    instances = reader.getJuliusResults();
    	}    	
    	writer.close();
	}
	

		
	
	/** 
	 * 针对Julius产生的nbest list候选结果，提取出其子模型文件，输出包括对应子模型的概率
	 * @since Sep 21, 2013
	 * @param parser
	 * @param testfile
	 * @param syllableFile
	 * @param K
	 * @param outputWeightFile
	 * @param outputInstanceFile
	 * @throws IOException
	 */
	public static void outputASRMertNbest(
		   PyCharParser parser,
		   String testfile, 
		   String syllableFile,
		   int K,
		   String outputWeightFile, 
		   String outputInstanceFile) throws IOException{
    	
    	long start = System.currentTimeMillis();
    	System.out.print("Processing Sentence: ");
    	
		JuliusResultReader reader = new JuliusResultReader();
    	reader.startReading(testfile);
    	reader.loadSyllableMap(syllableFile);
    	ArrayList<PyCharInstance> instances = reader.getJuliusResults();
	    
    	FileWriter weightWriter = new FileWriter(outputWeightFile);
    	FileWriter instanceWriter = new FileWriter(outputInstanceFile);

    	int cnt = 0;    	
    		
    	while(instances != null && instances.size() > 0) {
    		cnt++;
    		System.out.print(cnt + "...");
    	    if( cnt%10 == 0) {
    	    	System.out.println();
    	    }    	    
    	    int instanceSize = instances.size();  
    	    ArrayList<Double> AMWeights = new ArrayList<Double>(instanceSize);
    	    ArrayList<Double> LMWeights = new ArrayList<Double>(instanceSize);
    	    for(int i=0; i<instanceSize; i++) {
    	    	JuliusResultInstance instance = (JuliusResultInstance) instances.get(i);
    	    	AMWeights.add(instance.amWeight);
    	    	LMWeights.add(instance.lmWeight);
    	    }    	        	    
    	    ArrayList<Double> ngramWeights = (ArrayList<Double>) parser.decoder.getNgramWeights(instances); // word n-gram
		    		    
    	    parser.pipe.renewInstances(instances, K);
    	    parser.pipe.renewWeights(AMWeights, K);
    	    parser.pipe.renewWeights(LMWeights, K);
    	    parser.pipe.renewWeights(ngramWeights, K);
    	    
		    ArrayList<Double> charNGramWeight = parser.pipe.getCharNGramWeights(instances);
		    ArrayList<Double> charModelWeights = parser.pipe.getCharModelWeights(parser.baseParams, instances);		    
		    ArrayList<Double> pinyinWordCoWeights = parser.pipe.getPinyinWordCoWeights(instances);
		    
		    ArrayList<Object[]> POSModelObjs = parser.getWordPOSInstances(parser.posParser, instances);
		    ArrayList<Instance> POSModelInstances = new ArrayList<Instance>();
		    ArrayList<Double> POSModelWeights = new ArrayList<Double>();
		    for(Object[] POSModelObj : POSModelObjs) {
		    	POSModelInstances.add((Instance) POSModelObj[0]);
		    	POSModelWeights.add((Double) POSModelObj[1]);
		    }
		   
		    ArrayList<Double> POSWordCoWeights = parser.pipe.getPOSWordCoWeights(POSModelInstances);
		    ArrayList<Double> wordPOSCoWeights = parser.pipe.getWordPOSCoWeights(POSModelInstances);
		    ArrayList<Double> POSNGramWeights = parser.pipe.getPOSNGramWeights(POSModelInstances);
		    		    
		    Object[] depObj = parser.getDependencyInstances(POSModelInstances);
		    ArrayList<DependencyInstance> depInstances = (ArrayList<DependencyInstance>) depObj[0];
		    ArrayList<Double> depWeights = (ArrayList<Double>) depObj[1];
    	    
    	    assert(instances.size() == K); 
    	    assert(AMWeights.size() == K); 
    	    assert(LMWeights.size() == K);
    	    assert(ngramWeights.size() == K); 
    	    assert(charNGramWeight.size() == K); 
    	    assert(charModelWeights.size() == K);
    	    assert(pinyinWordCoWeights.size() == K); 
    	    assert(POSModelWeights.size() == K); 
    	    assert(POSWordCoWeights.size() == K);
    	    assert(wordPOSCoWeights.size() == K); 
    	    assert(POSNGramWeights.size() == K); 
    	    assert(depWeights.size() == K);
		    
		    ArrayList<ArrayList<Double>> allWeights = new ArrayList<ArrayList<Double>>();
		    allWeights.add(AMWeights); 
		    allWeights.add(LMWeights);
		    allWeights.add(ngramWeights); 
		    allWeights.add(charNGramWeight); 
		    allWeights.add(charModelWeights);
		    allWeights.add(pinyinWordCoWeights); 
		    allWeights.add(POSModelWeights); 
		    allWeights.add(POSWordCoWeights);
		    allWeights.add(wordPOSCoWeights); 
		    allWeights.add(POSNGramWeights); 
		    allWeights.add(depWeights);		    
		    
		    parser.outputInstancesWeights(weightWriter, cnt-1, instances, allWeights, K);
		    parser.outputDependencyInstances(instanceWriter, depInstances, K);
		    
    	    instances = reader.getJuliusResults();
    	}
    	
    	weightWriter.close();
    	instanceWriter.close();    	
    	//pipe.close();
    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	
	
	/** 
	 * 合并多个mert训练文件,以便于一块学习
	 * @since Sep 21, 2013
	 * @param fileNames
	 * @param outName
	 * @throws IOException
	 */
	public static void mertComb(String[] fileNames, String outName) throws IOException {
		
		FileWriter writer = new FileWriter(outName);
		//		
		int cur = -1;
		
		int start = -1;
		for(String fileName : fileNames) {
			System.out.println(fileName);
			
			String fileEncoding = FileEncoding.getCharset(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
	        
			String line = null;
			while((line = reader.readLine()) != null) {
				
				int delimStart = line.indexOf("|||");
				int sentenceIndex = Integer.parseInt(line.substring(0, delimStart).trim());
				if(sentenceIndex != cur) {
					start++;
					cur = sentenceIndex;
				}
				
				String afterSent = start + " " + line.substring(delimStart) + "\n";
				writer.write(afterSent);				
			}	
			
			reader.close();
			cur = -1;
		}
		
		writer.close();			
	}
	
	
	
	
	public static void usage() {
		
		System.out.println("Usage:");
		System.out.println("\t oneBestJulius:true testFile:file modelDir:directory outputFile:file");
		System.out.println("\t oneBestYinJulius:true testFile:file modelDir:directory outputFile:file");
		System.out.println("\t oneBestMert:true testFile:file outputFile:file");	
		System.out.println("\t OracleMert:true testFile:file k:500 goldFile:file outputFile:file");
		System.out.println("\t kBest:true testFile:file modelDir:directory bestK:500 outputFile:file");
		System.out.println("\t mertTrain:true testFile:file modelDir:directory bestK:500 mertCandFile:file mertDependencyFile:file");
		System.out.println("\t mertComb:true testFile:file1;file2 outputFile:file");
		System.out.println("\t mertDevOffline:true mertCandFile:file outputFile:file");
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
	
		if(args.length == 0)
			usage();

		PyCharOptions options = new PyCharOptions(args);
		AsrRerankOptions asrOptions = new AsrRerankOptions(args);
		
		if(asrOptions.oneBestJulius) {
			Reranking.outputOneBestFromJulius(asrOptions.testFile, asrOptions.syllableFile, asrOptions.outputFile);
		}
		if(asrOptions.oneBestYinJulius) {
			if(asrOptions.bestCandSeqFile == null)
				Reranking.outputOneBestYinFromJulius(asrOptions.testFile, asrOptions.syllableFile, asrOptions.outputFile);
			else
				Reranking.outputOneBestYinFromJulius(asrOptions.testFile, asrOptions.syllableFile, asrOptions.bestCandSeqFile, asrOptions.outputFile);
		}
		if(asrOptions.oneBestMert) {
			Reranking.outputOneBestFromMert(asrOptions.testFile, asrOptions.outputFile);
		}
		if(asrOptions.oracleMert) {
			Reranking.outputOracleFromMert(asrOptions.testFile, asrOptions.oracleK, asrOptions.goldFile, asrOptions.outputFile);
		}
		
		
		PyCharPipe pipe = new PyCharPipe(options);
		PyCharParser parser = new PyCharParser(pipe, options);
		if(asrOptions.kBest) {
			outputKBestFromJulius(parser, asrOptions.testFile, asrOptions.syllableFile, asrOptions.bestK, asrOptions.outputFile);
		}
		if(asrOptions.mertTrain) {		
			parser.loadCharModel(asrOptions.baseModelFile);	
			pipe.loadWordPinyinPair(asrOptions.wordPinyinFile);			
			pipe.loadBerkeleyLmBin(asrOptions.binLmFile);
			pipe.loadCharLmBin(asrOptions.charLmFile);
			pipe.loadPinyinWordCo(asrOptions.pinyinWordCoFile);
			parser.loadPOSModel(asrOptions.posModelFile);
			pipe.loadPOSWordCo(asrOptions.POSWordCoFile);
			pipe.loadWordPOSCo(asrOptions.wordPOSCoFile);
			parser.loadDependencyModel(asrOptions.dependencyModelFile);			
			outputASRMertNbest(parser, asrOptions.testFile, asrOptions.syllableFile, asrOptions.bestK, asrOptions.mertCandFile, asrOptions.mertDependencyFile);			
		}
		if(asrOptions.mertDevOffline) {					
			parser.outputMertBestOffline(asrOptions.mertCandFile, asrOptions.outputFile, asrOptions.mertWeights);			
		}
		if(asrOptions.mertComb) {
			String[] fileNames = asrOptions.testFile.split(";");
			Reranking.mertComb(fileNames, asrOptions.outputFile);
		}
		
	}
	
}
