package edu.hitsz.nlp.partofspeech;

import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.segpos.Item;

public class POSDecoder {

	POSPipe pipe;

	public POSDecoder(POSPipe pipe){
		this.pipe = pipe;
	}

	
	
	public POSFeatureVector getFeatureVectorPOS(Parameters params,
			POSInstance instance, 
			int K){

		POSLattice forest = kbeamPOS(params, instance, K);
		
		POSFeatureVector sfv = forest.getFeatureVectorPOS();
		
		sfv.sfv2fv(pipe);
		
		return sfv;
	}
	
	public POSInstance getBestPOS(Parameters params,
			POSInstance inst,
			int K){

		POSLattice forest = kbeamPOS(params, inst, K);		
		POSInstance instance = forest.getBestInstance();
		return instance;		
	}
	
	public Object[] getBestKPOS(Parameters params,
			POSInstance inst,
			int K,
			int outK){

		POSLattice forest = kbeamPOS(params, inst, K);		
		Object[] objs = forest.getBestKInstances(outK);
		return objs;
	}


	public POSLattice kbeamPOS(Parameters params,
			POSInstance inst,
			int K) {
	
		//int l = 20;
		//double d = -1e-5;

		String[] words = inst.words;
		int length = words.length;
		
		POSLattice forest = new POSLattice(0, length-1, inst, K, true);
		//ArrayList<String> allPos = Array.toArrayList(pipe.types);
		//i: 终止点

		String[] candidatePos = pipe.types;
		for(int i=0; i<length; i++){

			//当前词和可能的词性
			String currentWord = words[i];
			//每个当前词词性
			for(String pos : candidatePos){	
				
				//前一个词
				for(int m=0; m<K; m++){						
					POSItem pre = null;
					double preWeight = 0.0;
					if(i > 0) {							
						pre =  (forest.lattice)[i-1][m];
						if (pre == null) break;
						preWeight = pre.prob;							
					} 
					
					POSFeatureVector fv = new POSFeatureVector();					
					POSItem newItem = new POSItem(i, currentWord, pos, 0.0, fv, pre);					
					pipe.addPOSFeatures(inst, newItem, fv, false);
											
					double newWeight = preWeight + fv.getScore(params);
					
					newItem.prob = newWeight;
					
					forest.addEnd(i, newWeight, newItem);
					if(i == 0) break;
				}//每一个前一个词					
			}//每个当前词词性		
		}//每个终止点

		return forest;
	}

	
}
