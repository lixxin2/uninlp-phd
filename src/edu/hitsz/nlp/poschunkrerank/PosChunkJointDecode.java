package edu.hitsz.nlp.poschunkrerank;

import java.util.HashMap;
import java.util.ArrayList;

import edu.hitsz.ml.onlinelearning.AveragedPerceptron;
import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;
import edu.hitsz.nlp.util.FeatureTemplate;
import edu.hitsz.nlp.util.SequenceTagCandidate;

/**
 * 分别学习，联合decode
 * @author tm
 *
 */
public class PosChunkJointDecode {
	static String[] PTBPOSTags={"",""};
	static String[] PTBChunkTags={"ADJP","ADVP","CONJP","INTJ","LST","NP","PP","PRT","SBAR","UCP","VP"};

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



	/**
	 * Train the classifier
	 * @param trainName
	 * @param templateName
	 * @param signalName
	 * @param modelName
	 * @param iterNum the number of iteration
	 */
	public void AveragedPerceptronTrain(String trainName, String templateName, String candidateName, String modelName, int iterNum){
		AveragedPerceptron trainAp = new AveragedPerceptron();
		FeatureTemplate newTemplate=new FeatureTemplate();
		newTemplate.readFromFile(templateName);
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
				AveragedPerceptronViterbi(trainAp, trainFile.getSentence(j),newTemplate,newCand);
				//viterbi(trainFile,newTemplate,j);
				AveragedPerceptronUpdate(trainAp, trainFile,newTemplate,j);
			}
			System.out.println(Integer.toString(sentenceNum));
			String singleFeatureName=modelName+"-"+Integer.toString(i);
			trainAp.storeWeights(singleFeatureName,i,sentenceNum);
		}
		System.out.println("\nTraining process is done!");
	}


	public void AveragedPerceptronViterbi(AveragedPerceptron trainAp, ConllSentence sentence, FeatureTemplate newTemplate, SequenceTagCandidate newCand){
		int sentenceLen=sentence.getSentenceLength();
		sentence.predictSignal.clear();
		for(int j=0;j<sentenceLen;j++){
			ArrayList<String> oneFeature=new ArrayList<String>();
			//sentence.extractTokenFeaturesFromTmpt(oneFeature, newTemplate,j);
			double weight=0;
			ArrayList<String> candidateTags;
			if(j==0)
				candidateTags = newCand.getStart();
			else{
				String previousTag = sentence.predictSignal.get(j-1);
				candidateTags = newCand.getNext(previousTag);
			}
			String tmpResultSignal  = candidateTags.get(0);
			//iterative every possible tag
			for(int k=0;k<candidateTags.size();k++){
				double tmpWeight=trainAp.compSingleWeights(oneFeature, candidateTags.get(k));
				if(tmpWeight>weight){
					weight=tmpWeight;
					tmpResultSignal=candidateTags.get(k);
				}
			}
			sentence.getWords().get(j).add(tmpResultSignal);
			sentence.predictSignal.add(tmpResultSignal);
		}
		//file.printFeatures(sentence.predicateSignal);
		//file.printFeatures(sentence.senResultSignal);
	}


	/**
	 * update the parameter for Averaged Perceptron
	 * @param ap
	 * @param file
	 * @param feaStrategy
	 * @param ithSentence
	 */
	public void AveragedPerceptronUpdate(AveragedPerceptron trainAp, ConllFile file, FeatureTemplate newTemplate,int ithSentence){
		ConllSentence sentence=file.getSentence(ithSentence);
		int sentenceLen=sentence.getSentenceLength();
		HashMap<String,Integer> senFeature=new HashMap<String,Integer>();
		for(int j=0;j<sentenceLen;j++){
			ArrayList<String> oneFeature=new ArrayList<String>();
			//extract features from template
			//sentence.extractTokenFeaturesFromTmpt(oneFeature, newTemplate, j);
			//update the parameter of every parameter
			for(int o=0;o<oneFeature.size();o++){
				String one=oneFeature.get(o)+"+"+sentence.getResultSignal().get(j);
				if(senFeature.containsKey(one)){
					int num=senFeature.get(one);
					senFeature.put(one, num+1);
				}
				else
					senFeature.put(one, 1);

				one=oneFeature.get(o)+"+"+sentence.predictSignal.get(j);
				if(senFeature.containsKey(one)){
					int num=senFeature.get(one);
					senFeature.put(one, num-1);
				}
				else
					senFeature.put(one, -1);
			}
		}
		/*
		if(sentence.senResultSignal!=sentence.predictSignal){
			ap.update(trueFeature, trueFeatureNum,predicateFeature,predicateFeatureNum);
		}
		*/
		if((!sentence.getResultSignal().equals(sentence.predictSignal))||(ithSentence+1==file.getSentenceNumber()))
			trainAp.update(senFeature,file.getSentenceNumber(),ithSentence);
	}

	/**
	 * Predict results using the classifier
	 * @param testName
	 * @param templateName
	 * @param signalName
	 * @param modelName
	 * @param resultName
	 * @param kNum
	 */
	public void AveragedPerceptronPredict(String testName, String templateName,String tagCandidateName,String modelName, String resultName){
		AveragedPerceptron testAp = new AveragedPerceptron();
		ConllFile testFile=new ConllFile();
		FeatureTemplate newTemplate=new FeatureTemplate();
		newTemplate.readFromFile(templateName);
		testFile.readFrom(testName, 0);
		SequenceTagCandidate newCand = new SequenceTagCandidate();
		newCand.readFrom(tagCandidateName);
		testAp.readWeights(modelName);
		int sentenceNum=testFile.getSentenceNumber();
		System.out.print("\nCompute Viterbi Path for Sentence ");
		int j=0;
		for(;j<sentenceNum;j++){
			if(j%100==0){
				System.out.print(Integer.toString(j)+"...");
				if(j%1000==0)
					System.out.println();
			}
			AveragedPerceptronViterbiAP(testAp, testFile.getSentence(j),newTemplate,newCand);
		}
		System.out.println("Total Sentence "+Integer.toString(j)+" have been processed.");
		//testFile.storePredictFile(resultName);
	}



	public void AveragedPerceptronViterbiAP(AveragedPerceptron testAp, ConllSentence sentence, FeatureTemplate newTemplate, SequenceTagCandidate newCand){
		int sentenceLen=sentence.getSentenceLength();
		sentence.predictSignal.clear();
		for(int j=0;j<sentenceLen;j++){
			ArrayList<String> oneFeature=new ArrayList<String>();

			//extract features from template, or from hand-crafted codes
			//sentence.extractTokenFeaturesFromTmpt(oneFeature, newTemplate, j);
			double weight=0;
			ArrayList<String> candidateTags;
			if(j==0)
				candidateTags = newCand.getStart();
			else{
				String previousTag = sentence.predictSignal.get(j-1);
				candidateTags = newCand.getNext(previousTag);
			}
			String tmpResultSignal  = candidateTags.get(0);
			//iterative every possible tag
			for(int k=0;k<candidateTags.size();k++){
				double tmpWeight=testAp.compAllWeights(oneFeature, candidateTags.get(k));
				if(tmpWeight>weight){
					weight=tmpWeight;
					tmpResultSignal=candidateTags.get(k);
				}
			}
			sentence.getWords().get(j).add(tmpResultSignal);
			sentence.predictSignal.add(tmpResultSignal);
		}
		//file.printFeatures(sentence.predicateSignal);
		//file.printFeatures(sentence.senResultSignal);
	}

}
