package edu.hitsz.nlp.segpos;

import java.util.ArrayList;

import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.util.Array;

public class JointDecoder{

	CharPipe charPipe;
	WordPipe wordPipe;

	public JointDecoder(CharPipe charPipe, WordPipe wordPipe){
		this.charPipe = charPipe;
		this.wordPipe = wordPipe;
	}
	
	
	public Instance getBest(Parameters charParams,
			Parameters wordParams,
			Instance inst,
			int K,
			double alpha){

		Lattice forest = mkbeam(charParams, wordParams, inst, K, alpha);
		
		return forest.getBestInstance();
		
	}

	public Lattice mkbeam(Parameters charParams,
			Parameters wordParams,
			Instance inst,
			int K,
			double alpha) {
		double beta = 1- alpha;
	
		int l = 20;
		//double d = -1e-5;

		int length = inst.charLength();
		String[] characters = inst.chars;
		
		Lattice forest = new Lattice(0, length, inst, K);
		//ArrayList<String> allPos = Array.toArrayList(charPipe.types);
		//i: 终止点
		for(int i=0; i<length+1; i++){
			int j=Math.max(0, i-l);//候选词的初始节点

			//k: 起始点
			for(int k=j; k<i; k++){
				//当前词和可能的词性
				String currentWord = Array.toWord(characters,k,i);
				ArrayList<String> candidatePos =  charPipe.freq.getPos(currentWord);
				
				//词长
				int wordLength = i-k;

				//每个当前词词性
				for(String pos : candidatePos){					
					
					String[] tags = Word2Char.generateTag(wordLength, pos);
					double charWeight = 0.0;
					FeatureVector fv = new FeatureVector();					
					for(int m=0; m<wordLength; m++){
						charPipe.addCharFeature(inst, k+m, tags[m], fv, false);						
					}
					
					charWeight += fv.getScore(charParams);

					//前一个词
					for(int m=0; m<K; m++){
						Item pre = null;
						double preWeight = 0.0;
						if(k > 0){							
							pre =  (forest.lattice)[k][m];
							if (pre == null) break;
							preWeight = pre.prob;
						} 
						FeatureVector wfv = new FeatureVector();
						Item newItem = new Item(k, i, currentWord, pos, preWeight, wfv, pre);
					
						wordPipe.addWordPOSFeatures(inst, newItem, wfv, false);
						
						double wordWeight = wfv.getScore(wordParams);
						
						preWeight += beta * wordWeight + alpha * charWeight;
						
						newItem.prob = preWeight;
						
						forest.addEnd(i, preWeight, newItem);
						if(k == 0) break;
					}//每一个前一个词
				}//每个当前词词性
			}//每个起始点
		}//每个终止点

		return forest;
	}




	
}
