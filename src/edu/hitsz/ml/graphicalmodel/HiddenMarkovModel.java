/**
 *
 */
package edu.hitsz.ml.graphicalmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import edu.hitsz.nlp.struct.ConllFile;
import edu.hitsz.nlp.struct.ConllSentence;

/**
 * Hidden Markov Model (Rabiner, 1989)
 *
 * @author tm
 *
 */
public class HiddenMarkovModel {

	String divideSymbol="-@-";
	String splitSymbol="-&-";
	//initial state
	HashMap<String, Integer> initialStates;
	HashMap<String, Double> initialStateProb;
	//state transition
	HashMap<String, Integer> oneState;
	HashMap<String, Integer> stateOverOneState;
	HashMap<String, Integer> twoStates;
	HashMap<String, Integer> stateOverTwoStates;
	HashMap<String, Integer> threeStates;
	HashMap<String, Integer> stateOverThreeStates;
	HashMap<String, Double> stateTransitionProb;
	//state observation
	HashMap<String, Integer> states;
	HashMap<String, Integer> observations;
	HashMap<String, Integer> observationOverStates;
	HashMap<String, Double> stateObservationProb;
	//all states
	int statesNumber;
	Vector<String> allStates;
	int observationsNumber;
	Vector<String> allObservations;
	//forward, backward
	double[][] forwardProb;
	double[][] backwardProb;


	public HiddenMarkovModel(){
		//initial state
		initialStates=new HashMap<String,Integer>();
		initialStateProb=new HashMap<String, Double>();
		//state transition
		oneState=new HashMap<String, Integer>();
		stateOverOneState=new HashMap<String, Integer>();
		twoStates=new HashMap<String, Integer>();
		stateOverTwoStates=new HashMap<String, Integer>();
		threeStates=new HashMap<String, Integer>();
		stateOverThreeStates=new HashMap<String, Integer>();
		stateTransitionProb=new HashMap<String, Double>();
		//state observation
		states=new HashMap<String, Integer>();
		observations=new HashMap<String, Integer>();
		observationOverStates=new HashMap<String, Integer>();
		stateObservationProb=new HashMap<String, Double>();
		//
		statesNumber=0;
		allStates = new Vector<String>();
		observationsNumber=0;
		allObservations = new Vector<String>();
	}


	/**
	 * read all tags(states) from state file into allStates
	 * @param file
	 * @param stateFileName
	 */
	public void readStates(String stateFileName){
		ConllFile stateFile = new ConllFile();
		stateFile.readFrom(stateFileName, 0);
		for(ConllSentence sentence : stateFile.getTotalSentence()){
			int sentenceLength = sentence.getSentenceLength();
			for(int j=0; j<sentenceLength; j++){
				String currentState = sentence.getWords().get(1).get(j);
				if(!allStates.contains(currentState))
					allStates.add(currentState);
			}
		}
		statesNumber = allStates.size();
	}

	/**
	 * generate observation from conllfile
	 * @param file
	 */
	public void readObservations(ConllFile file){
		int sentenceNumber = file.getSentenceNumber();
		for(ConllSentence sentence : file.getTotalSentence()){
			int sentenceLength = sentence.getSentenceLength();
			for(int j=0; j<sentenceLength; j++){
				String currentObservation = sentence.getWords().get(0).get(j);
				if(!allObservations.contains(currentObservation))
					allObservations.add(currentObservation);
			}
		}
		observationsNumber = allObservations.size();
	}


	/**
	 * read observation from observation file
	 * @param observationFileName
	 */
	public void readObservations(String observationFileName){
		ConllFile observationFile = new ConllFile();
		observationFile.readFrom(observationFileName, 0);
		for(ConllSentence sentence : observationFile.getTotalSentence()){
			int sentenceLength = sentence.getSentenceLength();
			for(int j=0; j<sentenceLength; j++){
				String currentObservation = sentence.getWords().get(0).get(j);
				if(!allObservations.contains(currentObservation))
					allObservations.add(currentObservation);
			}
		}
	}

	public void randomProb( ){
		if(statesNumber <= 0 || observationsNumber <= 0){
			System.out.println("The Number of states and observations exists");
			System.exit(1);
		}
		//
		java.util.Random r=new java.util.Random();
		//
		long allInitialStateNumber = 0;
		Vector<Integer> initialStateNumber = new Vector<Integer>();
		for(int i=0; i<statesNumber; i++){
			int tmp = Math.abs(r.nextInt());
			initialStateNumber.add(tmp);
			allInitialStateNumber += tmp;
		}
		for(int i=0; i<statesNumber; i++){
			initialStateProb.put(allStates.get(i), initialStateNumber.get(i)/(double)allInitialStateNumber);
		}
		//
		Vector<Integer> currentStateNumber = new Vector<Integer>();
		for(int i=0; i<statesNumber; i++){
			long allCurrentStateNumber = 0;
			currentStateNumber.clear();
			for(int j=0; j<statesNumber; j++){
				int tmp = Math.abs(r.nextInt());
				currentStateNumber.add(tmp);
				allCurrentStateNumber += tmp;
			}
			for(int j=0; j<statesNumber; j++){
				stateTransitionProb.put(allStates.get(j)+divideSymbol+allStates.get(i), currentStateNumber.get(j)/(double)allCurrentStateNumber);
			}
		}
		//
		Vector<Integer> currentObservationNumber = new Vector<Integer>();
		for(int i=0; i<statesNumber; i++){
			long allCurrentObservationNumber =  0;
			currentObservationNumber.clear();
			for(int j=0; j<observationsNumber; j++){
				int tmp = Math.abs(r.nextInt());
				currentObservationNumber.add(tmp);
				allCurrentObservationNumber += tmp;
			}
			for(int j=0; j<observationsNumber; j++){
				stateObservationProb.put(allObservations.get(j)+divideSymbol+allStates.get(i), currentObservationNumber.get(j)/(double)allCurrentObservationNumber);
			}
		}

	}


	public void baumWelch(ConllSentence sentence){
		int iter = 100;
		int iround = 0;
		double iratio = 0;

		double sentenceObservationProb = compForwardProb(sentence);
		double sentenceObservationProb2 = compBackwardProb(sentence);
		int sentenceLength = sentence.getSentenceLength();
		do{
			//computer
			double[][][] stateTransitionTimeProb = new double[sentenceLength-1][statesNumber][statesNumber];
			for(int t=0; t<sentenceLength-1; t++)
				for(int i=0; i<statesNumber; i++)
					for(int j=0; j<statesNumber; j++){
						String currentStateTransition = allStates.get(j) + divideSymbol + allStates.get(i);
						double currentStateTransitionProb = 0;
						if(stateTransitionProb.containsKey(currentStateTransition))
							currentStateTransitionProb = stateTransitionProb.get(currentStateTransition);
						String currentStateObservation = sentence.getWords().get(0).get(t+1)+ divideSymbol +allStates.get(j);
						double currentStateObservationProb = 0;
						if(stateObservationProb.containsKey(currentStateObservation))
							currentStateObservationProb = stateObservationProb.get(currentStateObservation);
						stateTransitionTimeProb[t][i][j] = forwardProb[t][i] * currentStateTransitionProb * currentStateObservationProb * backwardProb[t+1][j] / sentenceObservationProb;
					}
			double[][] stateTimeProb = new double[sentenceLength][statesNumber];
			for(int t=0; t<sentenceLength; t++)
				for(int i=0; i<statesNumber; i++)
					stateTimeProb[t][i] = forwardProb[t][i] * backwardProb[t][i] / sentenceObservationProb;
			//reestimation
			double allInitialStateProb = 0;
			for(int i=0; i<statesNumber; i++)
				allInitialStateProb += stateTimeProb[0][i];
			for(int i=0; i<statesNumber; i++)
				initialStateProb.put(allStates.get(i), stateTimeProb[0][i]/allInitialStateProb);
			for(int i=0; i<statesNumber; i++){
				double totalStateProb = 0;
				for(int t=0; t<sentenceLength-1; t++)
					totalStateProb += stateTimeProb[t][i];
				for(int j=0; j<statesNumber; j++){
					double totalStateTransitionProb = 0;
					for(int t=0; t<sentenceLength-1; t++)
						totalStateTransitionProb += stateTransitionTimeProb[t][i][j];
					if(Math.abs(totalStateProb) < 1e-40 )
						stateTransitionProb.put(allStates.get(j)+divideSymbol+allStates.get(i), 0.0);
					else
						stateTransitionProb.put(allStates.get(j)+divideSymbol+allStates.get(i), totalStateTransitionProb/totalStateProb);
				}
			}
			for(int j=0; j<statesNumber; j++){
				double totalStateProb = 0;
				for(int t=0; t<sentenceLength; t++)
					totalStateProb += stateTimeProb[t][j];
				for(int k=0; k< allObservations.size(); k++){
					double totalStateobservationProb = 0;
					for(int t=0; t<sentenceLength; t++)
						if(sentence.getWords().get(0).get(t).equals(allObservations.get(k)))
							totalStateobservationProb += stateTimeProb[t][j];
					if(Math.abs(totalStateProb) < 1e-40)
						stateObservationProb.put(allObservations.get(k)+divideSymbol+allStates.get(j), 0.0);
					else
						stateObservationProb.put(allObservations.get(k)+divideSymbol+allStates.get(j), totalStateobservationProb/totalStateProb);
				}
			}
			double sentenceObservationProbNew = compForwardProb(sentence);
			iratio = (sentenceObservationProbNew - sentenceObservationProb)/sentenceObservationProb;
			sentenceObservationProb = sentenceObservationProbNew;
			iround+=1;
			if(iround > iter)
				break;
		}while(iratio > 1e-40);

	}


	public void baumWelch(ConllFile file){
		int sentenceNumber = file.getSentenceNumber();
		compForwardProb(file);
		compBackwardProb(file);
		double[] sentenceObservationProb = compObservationProb(file);

		for(int i=0; i<sentenceNumber; i++){
			ConllSentence sentence = new ConllSentence();
			int sentenceLength = sentence.getSentenceLength();
			double[][][] transitionProb = new double[sentenceLength][statesNumber][statesNumber];
			for(int j =0; j<sentenceLength; j++){

			}
		}
	}

	/**
	 * computer viterbi path for all file
	 * @param file
	 */
	public void compViterbiPath(ConllFile file){
		int sentenceNumber = file.getSentenceNumber();
		System.out.println("computer viterbi path in sentence ");
		for(int i=0; i<sentenceNumber; i++){
			if(i%100 == 0)
				System.out.print(i + "...");
			compViterbiPath(file.getSentence(i));
		}
		System.out.println(sentenceNumber + "done.");

	}


	/**
	 * computer viterbi path
	 * @param sentence
	 * @return
	 */
	public void compViterbiPath(ConllSentence sentence){
		sentence.predictSignal.clear();
		int sentenceLength = sentence.getSentenceLength();
		double[][] viterbiProb = new double[sentenceLength][statesNumber];
		int[][] viterbiPath = new int[sentenceLength][statesNumber];
		int[] bestPath = new int[sentenceLength];
		// initialization
		int sig = 0;
		double tmp = -1e10;
		for(int i=0; i<statesNumber; i++){
			double currentStateProb = -1e10;
			if(initialStateProb.containsKey(allStates.get(i)))
				currentStateProb = Math.log10(initialStateProb.get(allStates.get(i)));
			String currentStateObservation = sentence.getWords().get(0).get(0) + divideSymbol + allStates.get(i);
			double currentStateObservationProb = -1e10;
			if(stateObservationProb.containsKey(currentStateObservation))
				currentStateObservationProb = Math.log10(stateObservationProb.get(currentStateObservation));
			viterbiProb[0][i] = currentStateProb + currentStateObservationProb;
			viterbiPath[0][i] = 0;
		}
		// recursion
		if(sentenceLength > 1){
			for( int i=1; i<sentenceLength; i++){
				for(int j=0; j<statesNumber; j++){
					sig = 0;
					tmp = -1e10;
					for(int k=0; k<statesNumber; k++){
						double currentStateTransitionProb = -1e10;
						String currentStateTransition = allStates.get(j) + divideSymbol + allStates.get(j);
						if(stateTransitionProb.containsKey(currentStateTransition))
							currentStateTransitionProb = viterbiProb[i-1][k] + Math.log10(stateTransitionProb.get(currentStateTransition));
						if(currentStateTransitionProb > tmp){
							tmp = currentStateTransitionProb;
							sig = k;
						}
					}
					viterbiPath[i][j] = sig;

					String currentStateObservation = sentence.getWords().get(0).get(i) + divideSymbol + allStates.get(j);
					double currentStateObservationProb = -1e10;
					if(stateObservationProb.containsKey(currentStateObservation))
						currentStateObservationProb = Math.log10(stateObservationProb.get(currentStateObservation));
					viterbiProb[i][j] = tmp + currentStateObservationProb;
				}
			}
		}
		//find the best state sequence
		tmp = -1e20;
		sig = 0;
		for(int i=0; i<statesNumber; i++){
			if(viterbiProb[sentenceLength-1][i] > tmp){
				tmp = viterbiProb[sentenceLength-1][i];
				sig = i;
			}
		}
		bestPath[sentenceLength-1] = sig;
		for(int i=sentenceLength-1; i>0; i--){
			bestPath[i-1] = viterbiPath[i][bestPath[i]];
		}
		for(int i=0; i<sentenceLength; i++)
			sentence.predictSignal.add(allStates.get(bestPath[i]));
	}


	/**
	 * forward procedure to computer the maximum probability of an observation sequence
	 * @param sentence
	 */
	public double compForwardProb(ConllSentence sentence){
		int sentenceLength = sentence.getSentenceLength();
		forwardProb=new double[sentenceLength][statesNumber];
		//initialization
		for(int i=0; i<statesNumber; i++) {
			double currentStateProb = 0;
			if(initialStateProb.containsKey(allStates.get(i)))
				currentStateProb = initialStateProb.get(allStates.get(i));
			double currentStateObservationProb = 0;
			String currentStateObservation = sentence.getWords().get(0).get(0) + divideSymbol + allStates.get(i);
			if(stateObservationProb.containsKey(currentStateObservation))
				currentStateObservationProb = stateObservationProb.get(currentStateObservation);
			forwardProb[0][i] = currentStateProb * currentStateObservationProb;
		}
		//induction
		if (sentenceLength > 1){
			for(int i=1; i<sentenceLength; i++){
				for(int j=0; j<statesNumber; j++){
					double tmp=0;
					for(int k=0; k<statesNumber; k++){
						double currentStateTransitionProb = 0;
						String currentStateTransition = allStates.get(j) + divideSymbol + allStates.get(k);
						if(stateTransitionProb.containsKey(currentStateTransition))
							currentStateTransitionProb = stateTransitionProb.get(currentStateTransition);
						tmp += forwardProb[i-1][k] * currentStateTransitionProb;
					}
					double currentStateObservationProb = 0;
					String currentStateObservation = sentence.getWords().get(0).get(i) + divideSymbol + allStates.get(j);
					if(stateObservationProb.containsKey(currentStateObservation))
						currentStateObservationProb = stateObservationProb.get(currentStateObservation);
					forwardProb[i][j] = tmp * currentStateObservationProb;
				}
			}
		}
		//
		double totalForwardProb = 0;
		for(int i=0; i<statesNumber; i++)
			totalForwardProb += forwardProb[sentenceLength-1][i];
		return totalForwardProb;
	}

	/**
	 * forward procedure to computer the maximum probability of all sequence in a file
	 * @param file
	 */
	public void compForwardProb(ConllFile file){
		int sentenceNumber = file.getSentenceNumber();
		for(int i=0; i<sentenceNumber; i++)
			compForwardProb(file.getSentence(i));
	}


	/**
	 * backward procedure to computer the maximum probability of an observation sequence
	 * @param sentence
	 */
	public double compBackwardProb(ConllSentence sentence){
		int sentenceLength = sentence.getSentenceLength();
		backwardProb=new double[sentenceLength][statesNumber];
		//initialization
		for(int i=0; i<statesNumber; i++) {
			backwardProb[sentenceLength-1][i] = 1;
		}
		//induction
		if (sentenceLength > 1){
			for(int i=sentenceLength-2; i>=0; i--){
				for(int j=0; j<statesNumber; j++){
					double tmp=0;
					for(int k=0; k<statesNumber; k++){
						double innerTmp=0;
						double nextStateTransitionProb = 0;
						if(stateTransitionProb.containsKey(allStates.get(k)+divideSymbol+allStates.get(j)))
							nextStateTransitionProb = stateTransitionProb.get(allStates.get(k)+divideSymbol+allStates.get(j));
						double nextStateObservationProb = 0;
						String nextObservation = sentence.getWords().get(0).get(i+1);
						if(stateObservationProb.containsKey(nextObservation+divideSymbol+allStates.get(k)))
							nextStateObservationProb = stateObservationProb.get(nextObservation+divideSymbol+allStates.get(k));
						innerTmp = nextStateTransitionProb * nextStateObservationProb * backwardProb[i+1][k];
						tmp += innerTmp;
					}
					backwardProb[i][j] = tmp;
				}
			}
		}
		//
		double totalBackwardProb = 0;
		for(int i=0; i<statesNumber; i++){
			double currentInitialStateProb = 0;
			if(initialStateProb.containsKey(allStates.get(i)))
				currentInitialStateProb = initialStateProb.get(allStates.get(i));
			double currentStateObservationProb = 0;
			String currentObservation = sentence.getWords().get(0).get(0);
			if(stateObservationProb.containsKey(currentObservation+divideSymbol+allStates.get(i)))
				currentStateObservationProb = stateObservationProb.get(currentObservation+divideSymbol+allStates.get(i));
			totalBackwardProb += currentInitialStateProb * currentStateObservationProb * backwardProb[0][i];
		}
		return totalBackwardProb;
	}

	/**
	 * forward procedure to computer the maximum probability of all sequence in a file
	 * @param file
	 */
	public void compBackwardProb(ConllFile file){
		int sentenceNumber = file.getSentenceNumber();
		for(int i=0; i<sentenceNumber; i++)
			compBackwardProb(file.getSentence(i));
	}



	/**
	 * return the probability of all sequences in a file
	 * @param sentence
	 * @return
	 */
	public double[] compObservationProb(ConllFile file){
		int sentenceNumber = file.getSentenceNumber();
		double[] observationProb = new double[sentenceNumber];
		for(int i=0; i<sentenceNumber; i++)
			observationProb[i] = compForwardProb(file.getSentence(i));
		return observationProb;
	}


	/**
	 * read statistic information of state and observation in the inputting file
	 * @param inputFileName
	 */
	public void readFromFile(ConllFile inputFile){
		//clear states
		initialStates.clear();
		initialStateProb.clear();
		//state transition
		oneState.clear();
		stateOverOneState.clear();
		twoStates.clear();
		stateOverTwoStates.clear();
		threeStates.clear();
		stateOverThreeStates.clear();
		stateTransitionProb.clear();
		//state observation
		states.clear();
		observations.clear();
		observationOverStates.clear();
		stateObservationProb.clear();

		//
		int sentenceNumber=inputFile.getSentenceNumber();
		//iterative every sentence in the file
		System.out.print("Have processed ");
		for(int i=0;i<sentenceNumber;i++){
			if(i%1000==0)
				System.out.print(i+"...");
			ConllSentence sentence=inputFile.getSentence(i);
			//record initial state
			String initialState=sentence.getResultSignal().get(0);
			if(initialStates.containsKey(initialState))
				initialStates.put(initialState, initialStates.get(initialState)+1);
			else
				initialStates.put(initialState, 1);
			//state transition
			int sentenceLength=sentence.getSentenceLength();
			if(sentenceLength>1)
				for(int j=1;j<sentenceLength;j++){
					String currentState=sentence.getResultSignal().get(j);
					String previousState=sentence.getResultSignal().get(j-1);
					String currentOverPreviousState=currentState+divideSymbol+previousState;
					if(oneState.containsKey(previousState))
						oneState.put(previousState, oneState.get(previousState)+1);
					else
						oneState.put(previousState, 1);
					if(stateOverOneState.containsKey(currentOverPreviousState))
						stateOverOneState.put(currentOverPreviousState, stateOverOneState.get(currentOverPreviousState)+1);
					else
						stateOverOneState.put(currentOverPreviousState, 1);
				}
			if(sentenceLength>2)
				for(int j=2;j<sentenceLength;j++){
					String currentState=sentence.getResultSignal().get(j);
					String previousState=sentence.getResultSignal().get(j-2)+splitSymbol+sentence.getResultSignal().get(j-1);
					String currentOverPreviousState=currentState+divideSymbol+previousState;
					if(twoStates.containsKey(previousState))
						twoStates.put(previousState, twoStates.get(previousState)+1);
					else
						twoStates.put(previousState, 1);
					if(stateOverTwoStates.containsKey(currentOverPreviousState))
						stateOverTwoStates.put(currentOverPreviousState, stateOverTwoStates.get(currentOverPreviousState)+1);
					else
						stateOverTwoStates.put(currentOverPreviousState, 1);
				}
			if(sentenceLength>3)
				for(int j=3;j<sentenceLength;j++){
					String currentState=sentence.getResultSignal().get(j);
					String previousState=sentence.getResultSignal().get(j-3)+splitSymbol+sentence.getResultSignal().get(j-2)+splitSymbol+sentence.getResultSignal().get(j-1);
					String currentOverPreviousState=currentState+divideSymbol+previousState;
					if(threeStates.containsKey(previousState))
						threeStates.put(previousState, threeStates.get(previousState)+1);
					else
						threeStates.put(previousState, 1);
					if(stateOverThreeStates.containsKey(currentOverPreviousState))
						stateOverThreeStates.put(currentOverPreviousState, stateOverThreeStates.get(currentOverPreviousState)+1);
					else
						stateOverThreeStates.put(currentOverPreviousState, 1);
				}
			//state observation
			for(int j=0;j<sentenceLength;j++){
				String currentState=sentence.getResultSignal().get(j);
				String currentObservation=sentence.getWords().get(0).get(j);
				String currentObservationOverState=currentObservation+divideSymbol+currentState;
				if(states.containsKey(currentState))
					states.put(currentState, states.get(currentState)+1);
				else
					states.put(currentState, 1);
				if(observations.containsKey(currentObservation))
					observations.put(currentObservation, observations.get(currentObservation)+1);
				else
					observations.put(currentObservation, 1);
				if(observationOverStates.containsKey(currentObservationOverState))
					observationOverStates.put(currentObservationOverState, observationOverStates.get(currentObservationOverState)+1);
				else
					observationOverStates.put(currentObservationOverState, 1);
			}
		}
		//state number
		statesNumber=states.size();
		Iterator iter=states.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String currentState = entry.getKey();
			allStates.add(currentState);
		}
		observationsNumber=observations.size();
		iter=observations.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String currentState = entry.getKey();
			allObservations.add(currentState);
		}

		System.out.println(sentenceNumber+" done!");
	}


	public void noSmooth(){
		//computer statistics
		//initial state
		int initialStateNumber=0;
		Iterator iter=initialStates.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
		    Integer val = (Integer) entry.getValue();
		    initialStateNumber+=val;
		}
		iter=initialStates.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
		    Integer val = (Integer) entry.getValue();
		    initialStateProb.put(key, val/(double)initialStateNumber);
		}
		//state transition
		iter=stateOverOneState.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String currentOverPreviousState = entry.getKey();
			Integer currentOverPreviousStateNumber= entry.getValue();
			String previousState=(currentOverPreviousState.split(divideSymbol))[1];
			int previousStateNumber=oneState.get(previousState);
			if(previousStateNumber<1){
				System.out.println("There must be something wrong with "+previousState);
				System.exit(1);
			}
			stateTransitionProb.put(currentOverPreviousState, currentOverPreviousStateNumber/(double)previousStateNumber);
		}
		iter = stateOverTwoStates.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String currentOverPreviousState = entry.getKey();
			Integer currentOverPreviousStateNumber=entry.getValue();
			String previousState=currentOverPreviousState.split(divideSymbol)[1];
			int previousStateNumber=twoStates.get(previousState);
			if(previousStateNumber<1){
				System.out.println("There must be something wrong with "+previousState);
				System.exit(1);
			}
			stateTransitionProb.put(currentOverPreviousState, currentOverPreviousStateNumber/(double)previousStateNumber);
		}
		iter=stateOverThreeStates.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String currentOverPreviousState =  entry.getKey();
			Integer currentOverPreviousStateNumber= entry.getValue();
			String previousState=currentOverPreviousState.split(divideSymbol)[1];
			int previousStateNumber=threeStates.get(previousState);
			if(previousStateNumber<1){
				System.out.println("There must be something wrong with "+previousState);
				System.exit(1);
			}
			stateTransitionProb.put(currentOverPreviousState, currentOverPreviousStateNumber/(double)previousStateNumber);
		}
		//state observation
		iter=observationOverStates.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String currentObservationOverState =  entry.getKey();
			Integer currentObservationOverStateNumber=  entry.getValue();
			String currentState=currentObservationOverState.split(divideSymbol)[1];
			int currentStateNumber=states.get(currentState);
			if(currentStateNumber<1){
				System.out.println("There must be something wrong with "+currentState);
				System.exit(1);
			}
			stateObservationProb.put(currentObservationOverState, currentObservationOverStateNumber/(double)currentStateNumber);
		}

	}

	/**
	 * store statistic information in file
	 * @param outFileName
	 */
	public void storeStat(String outFileName){
		try{
			FileWriter outFile= new FileWriter(outFileName);
			//
			outFile.write(statesNumber+"\n");
			Iterator iter=states.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
				outFile.write(entry.getKey()+"\n");
			}
			outFile.write("//\n");
			//
			iter = initialStateProb.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
				outFile.write(entry.getKey()+"\t"+Double.toString(entry.getValue())+"\n");
			}
			outFile.write("//\n");
			//
			iter=stateTransitionProb.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
				outFile.write(entry.getKey()+"\t"+Double.toString(entry.getValue())+"\n");
			}
			outFile.write("//\n");
			//
			iter=stateObservationProb.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, Double> entry = (Map.Entry<String, Double>) iter.next();
				outFile.write(entry.getKey()+"\t"+Double.toString(entry.getValue())+"\n");
			}
			outFile.close();
		}
		catch(IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * read statistic information from statistic file
	 * @param statName
	 */
	public void readStat(String statName){
		File statFile=new File(statName);
		BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(statFile));
            String tempString = null;
            int line = 1;
            // all state statistics
            statesNumber = Integer.parseInt(reader.readLine().trim());
            allStates = new Vector<String>();
            int i=0;
            while ((tempString = reader.readLine()) != null && !tempString.trim().equals("//")) {
                String currentState= tempString.trim();
                allStates.add(currentState);
            }
            // initial states
            while ((tempString = reader.readLine()) != null && !tempString.trim().equals("//")) {
                String[] temp=tempString.split("\t");
                String state=temp[0];
                double prob=Double.parseDouble(temp[1]);
                initialStateProb.put(state, prob);
            }
            // state transition
            while ((tempString = reader.readLine()) != null && !tempString.trim().equals("//")) {
                String[] temp=tempString.split("\t");
                String state=temp[0];
                double prob=Double.parseDouble(temp[1]);
                stateTransitionProb.put(state, prob);
            }
            // state observation
            while ((tempString = reader.readLine()) != null && !tempString.trim().equals("//")) {
                String[] temp=tempString.split("\t");
                String state=temp[0];
                double prob=Double.parseDouble(temp[1]);
                stateObservationProb.put(state, prob);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
	}



	static void oneExample(String[] args){


	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HiddenMarkovModel newModel=new HiddenMarkovModel();

		/*
		ConllFile inputFile=new ConllFile();
		inputFile.readTrainFile("/mnt/d5/experiments/hmm/15-18wp", 0);
		inputFile.generateResultSignal();
		newModel.readFromFile(inputFile);
		newModel.noSmooth();
		newModel.storeStat("/mnt/d5/experiments/hmm/stat");
		*/

		/*
		newModel.readStat("/mnt/d5/experiments/hmm/stat");
		ConllFile outputFile = new ConllFile();
		outputFile.readTestFile("/mnt/d5/experiments/hmm/15-18w", 0);
		newModel.compViterbiPath(outputFile);
		outputFile.storeSenResultFile("/mnt/d5/experiments/hmm/15-18www");
		*/

		ConllFile inputFile = new ConllFile();
		inputFile.readFrom("/mnt/d5/experiments/hmm/1", 0);
		newModel.readStates("/mnt/d5/experiments/hmm/state");
		newModel.readObservations(inputFile);
		newModel.randomProb();
		newModel.baumWelch(inputFile.getSentence(0));


	}

}
