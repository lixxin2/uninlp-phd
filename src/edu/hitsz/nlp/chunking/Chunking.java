package edu.hitsz.nlp.chunking;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import edu.hitsz.ml.onlinelearning.apold.AP;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.SequenceTagCandidate;

public class Chunking {
	
	static String[] PTBChunkTags = {"ADJP","ADVP","CONJP","INTJ","LST","NP","PP",
		"PRT","SBAR","UCP","VP"};

	public static void generateCandidate(String tagCandidateFileName){
		SequenceTagCandidate newCand = new SequenceTagCandidate();
		for(int i=0; i<PTBChunkTags.length; i++){
			String beginTag ="B-"+PTBChunkTags[i];
			if(!newCand.candidates.containsKey(beginTag)){
				newCand.starts.add(beginTag);
				ArrayList<String> newVec= new ArrayList<String>();
				for(int j=0; j<PTBChunkTags.length; j++){
					String tmp = "B-"+PTBChunkTags[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				String tmp = "I-"+PTBChunkTags[i];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				tmp = "O";
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				newCand.candidates.put(beginTag, newVec);
			}
		}
		for(int i=0; i<PTBChunkTags.length; i++){
			String middleTag ="I-"+PTBChunkTags[i];
			if(!newCand.candidates.containsKey(middleTag)){
				ArrayList<String> newVec= new ArrayList<String>();
				for(int j=0; j<PTBChunkTags.length; j++){
					String tmp = "B-"+PTBChunkTags[j];
					if(!newVec.contains(tmp))
						newVec.add(tmp);
				}
				String tmp = "I-"+PTBChunkTags[i];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				tmp = "O";
				if(!newVec.contains(tmp))
					newVec.add(tmp);
				newCand.candidates.put(middleTag, newVec);
			}
		}
		String outTag= "O";
		if(!newCand.candidates.containsKey(outTag)){
			newCand.starts.add(outTag);
			ArrayList<String> newVec= new ArrayList<String>();
			for(int j=0; j<PTBChunkTags.length; j++){
				String tmp = "B-"+PTBChunkTags[j];
				if(!newVec.contains(tmp))
					newVec.add(tmp);
			}
			String tmp = "O";
			if(!newVec.contains(tmp))
				newVec.add(tmp);
			newCand.candidates.put(outTag, newVec);
		}
		newCand.store(tagCandidateFileName);
	}

	//得到语料库中的所有tag
	public String[] getTags(String trainName) {
		HashMap<String, Integer> tagMap = new HashMap<String, Integer>();
		ConllFile trainFile=new ConllFile();
		trainFile.readFrom(trainName, 0);
		int sentenceNum=trainFile.getSentenceNumber();
		System.out.print("Sentence ");
		for(int j=0;j<sentenceNum;j++){
			ConllSentence sentence = trainFile.getSentence(j);
			for(String tag : sentence.getResultSignal()) 
				if(!tagMap.containsKey(tag))
					tagMap.put(tag, tagMap.size());
		}
		String[] tags = new String[tagMap.size()];
		Iterator<Map.Entry<String, Integer>> iter = tagMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = iter.next();
			tags[entry.getValue()] = entry.getKey();
		}
		return tags;
	}



	public void APTrainBeam(String trainName, 
			String candidateName, 
			String modelName, 
			int iterNum){
		AP trainAp = new AP();
		ConllFile trainFile=new ConllFile();
		trainFile.readFrom(trainName, 0);
		SequenceTagCandidate newCand = new SequenceTagCandidate();
		newCand.readFrom(candidateName);
		for(int i=1;i<=iterNum;i++){
			System.out.println("\nThis is the "+Integer.toString(i)+"th iteration...");
			int sentenceNum=trainFile.getSentenceNumber();
			System.out.print("Sentence ");
			for(int j=0;j<sentenceNum;j++){
				if(j%100==0){
					System.out.print(Integer.toString(j)+"...");
					if(j%1000==0)
						System.out.println();
				}
				APViterbiBeam(trainAp, trainFile.getSentence(j), newCand, false, true);
				APUpdate(trainAp, trainFile, j);
			}
			System.out.println(Integer.toString(sentenceNum));
			String singleFeatureName=modelName+"-"+Integer.toString(i);
			System.out.println(System.currentTimeMillis());
			trainAp.storeWeights(singleFeatureName,i,sentenceNum);
			System.out.println(System.currentTimeMillis());
		}
		System.out.println("\nTraining process is done!");
	}
	

	
	public double APViterbiBeam(AP trainAp, ConllSentence sentence,
			SequenceTagCandidate newCand, boolean isAveraged,
			boolean bTrain){
		ArrayList<String> words = sentence.getWords(0);
		ArrayList<String> poses = sentence.getWords(1);
		
		int sentenceLength = words.size();
		int K=16;
		LinkedList<ArrayList<String>> oldTagList = new LinkedList<ArrayList<String>>();
		LinkedList<Double> oldWeights = new LinkedList<Double>();
		oldTagList.add(new ArrayList<String>());
		oldWeights.add(0.0);
		LinkedList<ArrayList<String>> newTagList = new LinkedList<ArrayList<String>>();
		LinkedList<Double> newWeights = new LinkedList<Double>();
		
		ArrayList<String> tags = newCand.getAll();
		for(int j=0;j<sentenceLength;j++){
			int max = Math.min(oldTagList.size(), K);
			ArrayList<String> oneFeature=extractFeature(words, poses, j, sentenceLength);
			
			//iterative every possible tag
			for(String tag : tags){
				double tmpWeight= 0.0;
				if(isAveraged)
					tmpWeight = trainAp.compAllWeights(oneFeature, tag, bTrain);
				else
					tmpWeight = trainAp.compSingleWeights(oneFeature, tag);
				
				//所有可能的前面tags
				for(int i=0; i<max; i++) { 
					double preWeight = oldWeights.get(i);
					ArrayList<String> preTags = oldTagList.get(i);
					ArrayList<String> suitableTags = null;
					if(j==0)
						suitableTags = newCand.getStart();
					else {
						String prevTag = preTags.get(j-1);
						suitableTags = newCand.getNext(prevTag);
					}					
					if(suitableTags.contains(tag)) {
						double newWeight = preWeight + tmpWeight;
						ArrayList<String> newTags = new ArrayList<String>(preTags);
						newTags.add(tag);
						
						int listSize = newTagList.size();
						boolean added = false;
						int k=0;
						for(; k<Math.min(listSize, K); k++)
							if(newWeight > newWeights.get(k)) {
								newWeights.add(k, newWeight);
								newTagList.add(k, newTags);
								added = true;
								break;
							}
						if(!added  && k < K){
							newWeights.add(newWeight);
							newTagList.add(newTags);
						}
					}
				}
			}
			oldTagList = newTagList;
			newTagList = new LinkedList<ArrayList<String>>();
			oldWeights = newWeights;
			newWeights = new LinkedList<Double>();
		}
		ArrayList<String> finalTags = oldTagList.get(0);
		sentence.predictSignal = finalTags;
		return oldWeights.get(0);
	}
	
	

	public void APUpdate(AP trainAp, ConllFile file, int ithSentence){
		ConllSentence sentence=file.getSentence(ithSentence);
		int sentenceLength=sentence.getSentenceLength();
		ArrayList<String> words = sentence.getWords(0);
		ArrayList<String> poses = sentence.getWords(1);
		ArrayList<String> senFeatures = new ArrayList<String>();
		ArrayList<Integer> senWeights = new ArrayList<Integer>();
		for(int j=0;j<sentenceLength;j++){
			ArrayList<String> oneFeature=extractFeature(words, poses, j, sentenceLength);
			for(int o=0;o<oneFeature.size();o++){
				String one=oneFeature.get(o)+"+"+sentence.getResultSignal().get(j);
				senFeatures.add(one);
				senWeights.add(1);

				one=oneFeature.get(o)+"+"+sentence.predictSignal.get(j);
				senFeatures.add(one);
				senWeights.add(-1);
			}
		}
		if((!sentence.getResultSignal().equals(sentence.predictSignal))||(ithSentence+1==file.getSentenceNumber()))
			trainAp.update(senFeatures, senWeights, file.getSentenceNumber(),ithSentence);
	}


	
	public void APPredictBeam(String testName, 
			String tagCandidateName, 
			String modelName, 
			String resultName,
			String resultWeightName) throws IOException{
		
		AP testAp = new AP();
		testAp.readWeights(modelName);
		
		SequenceTagCandidate newCand = new SequenceTagCandidate();
		newCand.readFrom(tagCandidateName);
		
		ConllFile testFile=new ConllFile();
		testFile.readFrom(testName, 0);

		FileWriter weightWriter = null;
		if(resultWeightName != null)
			weightWriter = new FileWriter(resultWeightName);
				
		int sentenceNum=testFile.getSentenceNumber();
		System.out.print("\nCompute Viterbi Path for Sentence ");
		int j=0;
		for(;j<sentenceNum;j++){
			//System.out.println(j);
			if(j%100==0){
				System.out.print(Integer.toString(j)+"...");
				if(j%1000==0)
					System.out.println();
			}
			double weight = APViterbiBeam(testAp, testFile.getSentence(j), newCand, true, false);
			if(weightWriter != null)
				weightWriter.write(weight + "\n");
		}
		System.out.println("Total Sentence "+Integer.toString(j)+" have been processed.");
		FileWriter writer = new FileWriter(resultName);
		for(j=0; j<sentenceNum; j++) {
			ArrayList<String> predictTags = testFile.getSentence(j).predictSignal;
			for(String predictTag : predictTags)
				writer.write(predictTag + "\n");
			writer.write("\n");
		}
		writer.close();
		if(weightWriter != null)
			weightWriter.close();
	}
	

	/**
	 * 标准的特征
	 * @param oneFeature
	 * @param characters
	 * @param currentLength
	 * @param sentenceLength
	 */
	private ArrayList<String> extractFeature(ArrayList<String> words,
			ArrayList<String> poses, int currentLength, int sentenceLength){
		ArrayList<String> features = new ArrayList<String>();
		int featureNumber = 0;
		String curWord = words.get(currentLength);
		String preWord = "NONE";
		String pre2Word = "NONE";
		String nextWord = "NONE";
		String next2Word = "NONE";
		String curPos = poses.get(currentLength);
		String prePos = "NONE";
		String pre2Pos = "NONE";
		String nextPos = "NONE";
		String next2Pos = "NONE";
		if(currentLength>0){
			preWord = words.get(currentLength-1);
			prePos = poses.get(currentLength-1);
		}
		if(currentLength>1){
			pre2Word = words.get(currentLength-2);
			pre2Pos = poses.get(currentLength-2);
		}
		if(currentLength<sentenceLength-1){
			nextWord = words.get(currentLength+1);
			nextPos = poses.get(currentLength+1);
		}
		if(currentLength<sentenceLength-2){
			next2Word = words.get(currentLength+2);
			next2Pos = poses.get(currentLength+2);
		}
		//feature
		features.add(++featureNumber+":"+curWord);
		features.add(++featureNumber+":"+preWord);
		features.add(++featureNumber+":"+pre2Word);
		features.add(++featureNumber+":"+nextWord);
		features.add(++featureNumber+":"+next2Word);
		features.add(++featureNumber+":"+pre2Word+"+"+preWord);
		features.add(++featureNumber+":"+preWord+"+"+curWord);
		features.add(++featureNumber+":"+curWord+"+"+nextWord);
		features.add(++featureNumber+":"+nextWord+"+"+next2Word);
		features.add(++featureNumber+":"+curPos);
		features.add(++featureNumber+":"+prePos);
		features.add(++featureNumber+":"+pre2Pos);
		features.add(++featureNumber+":"+nextPos);
		features.add(++featureNumber+":"+next2Pos);
		features.add(++featureNumber+":"+pre2Pos+"+"+prePos);
		features.add(++featureNumber+":"+prePos+"+"+curPos);
		features.add(++featureNumber+":"+curPos+"+"+nextPos);
		features.add(++featureNumber+":"+nextPos+"+"+next2Pos);
		features.add(++featureNumber+":"+pre2Pos+"+"+prePos+"+"+curPos);
		features.add(++featureNumber+":"+prePos+"+"+curPos+"+"+nextPos);
		features.add(++featureNumber+":"+curPos+"+"+nextPos+"+"+next2Pos);
		return features;
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generateCandidate("/home/tm/disk/disk1/pos-chunk-rerank/chunk-tag");
	}

}
