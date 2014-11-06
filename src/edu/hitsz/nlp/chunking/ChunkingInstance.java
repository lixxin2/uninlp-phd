package edu.hitsz.nlp.chunking;

import java.io.IOException;

import edu.hitsz.nlp.struct.ConllFile;

public class ChunkingInstance {

	public static void train(String trainName,String tagCandidateName,String modelName, int iterativeTimes){
		Chunking newChunk = new Chunking();
		newChunk.APTrainBeam(trainName,tagCandidateName,modelName,iterativeTimes);
	}
	
	public static void dev(String testName,String tagCandidateName,String modelName,
			String resultName, int iterateTimes) throws IOException{
		Chunking newChunk = new Chunking();
		for(int i=1; i<=iterateTimes; i++) 
			newChunk.APPredictBeam(testName,tagCandidateName,modelName+"-"+i,resultName+"-"+i, null);
	}	

	public static void combine(String testName,String resultName,String finalName, int iterateTimes){
		for(int i=1; i<=iterateTimes; i++){
			ConllFile testFile = new ConllFile();
			testFile.readFrom(testName, -1);
			testFile.addFileColumns(resultName+"-"+i, "0");
			testFile.store(finalName+"-"+i, true, true);
		}
	}

	private static void eval(String testName,String resultName,String evalName, int iterateTimes){
		for(int i=1; i<=iterateTimes; i++){
			ChunkingEvaluation newEval = new ChunkingEvaluation();
			newEval.eval(testName, resultName+"-"+i);
			newEval.store(evalName+"-"+i);
		}
	}
	
	private static void eval(String testName,String resultName,String evalName){
		ChunkingEvaluation newEval = new ChunkingEvaluation();
		newEval.eval(testName, resultName);
		newEval.store(evalName);
	}


	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		System.out.println(System.currentTimeMillis());
		String dir = "/home/tm/disk/disk1/pos-chunk-rerank/chunk/";
		dir = "./";
		String trainName = dir + "train";
		String tagCandidateName = dir + "chunk-tag";
		String modelName = dir + "train.model";
		int iterateTimes = 10;
		String testName = dir + "test";
		String resultName = dir + "test.result";
		
		String testGoldName = dir + "test";
		String evalName = dir + "test.result.eval";
		
		/*
		//训练和测试
		train(trainName,tagCandidateName,modelName,iterateTimes);
		dev(testName, tagCandidateName, modelName,resultName, iterateTimes);
		//测试结果
		eval(testGoldName, resultName, evalName, iterateTimes);
		*/
		
		/*
		//生成单个结果
		dir = "/home/tm/disk/disk1/pos-chunk-rerank/";
		modelName = dir + "15-18wpc.model-7";
		tagCandidateName = dir + "chunk-tag";
		testName = dir + "brownptb.wp.result";
		resultName = testName + ".wpc";
		new Chunking().APPredictBeam(testName,tagCandidateName,modelName,resultName, null);
		*/
		
		
		//测试单个结果
		dir = "/home/tm/disk/disk1/pos-chunk-rerank/8.perceptron/";
		testGoldName = dir + "20wpc";
		for(int i=0; i<10; i++) {
			resultName = dir + "20.percetron.result.pos.f2"+"-"+i;
			evalName = resultName + ".chunkeval";
			eval(testGoldName, resultName, evalName);
		}
					
		/*
		//output with probability
		dir = "./";
		testName = dir + "500best";
		tagCandidateName = dir + "chunk-tag";
		modelName = dir + "train.model";
		resultName = dir + "500best.chunk500best";
		String resultProbName = dir + "500best.chunk500best.prob";
		new Chunking().APPredictBeam(testName,tagCandidateName,modelName,resultName, resultProbName);
		*/

	}

}
