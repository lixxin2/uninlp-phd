package edu.hitsz.nlp.pinyin2character;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.algorithm.Heap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.segpos.FeatureVector;
import edu.hitsz.nlp.util.Array;

public class PyCharDecoder {
	
	public PyCharPipe pipe;

	public PyCharItem[][] lattice;
	
	public PyCharDecoder(PyCharPipe pipe) {
		this.pipe = pipe;
	}
	
	
	/**
	 * 返回最优的实例结果
	 * @since Oct 24, 2012
	 * @param params
	 * @param inst
	 * @param K
	 * @return
	 */
	public PyCharInstance getBestBase(Parameters params,
			PyCharInstance inst,
			int K){

		PyCharLattice forest = mkbeamChar(params, inst, K);
				
		PyCharInstance instance = forest.getBest(inst);
				
		return instance;
	}
	
	
	
	/**
	 * 解析算法
	 * @since Oct 24, 2012
	 * @param params
	 * @param inst
	 * @param K
	 * @return
	 */
	public PyCharLattice mkbeamChar(Parameters params,
			PyCharInstance inst,
			int K) {
	
		int length = inst.length;
		String[] characterYins = inst.characterYins;
		
		PyCharLattice forest = new PyCharLattice(0, length-1, K);
		//ArrayList<String> allPos = Array.toArrayList(pipe.types);
		//i: 终止点
		for(int i=0; i<length; i++){
			
			Heap<PyCharItem> heap = new Heap<PyCharItem>(K);			
			
			String currentPinyin = characterYins[i];
			ArrayList<String> candidateWords = pipe.pwPair.getWords(currentPinyin);
			if(candidateWords != null) {
				//每个当前词
				for(String word : candidateWords){
					
					//前一个词
					for(int m=0; m<K; m++){						
						PyCharItem pre = null;
						double preWeight = 0.0;
						if(i > 0) {							
							pre =  (forest.lattice)[i-1][m];
							if (pre == null) break;
							preWeight = pre.prob;							
						} 
						
						FeatureVector fv = new FeatureVector();
						PyCharItem newItem = new PyCharItem(i, i+1, currentPinyin, word, preWeight, fv, pre);
					
						pipe.addWordFeatures(inst, newItem, fv, false);
												
						preWeight += fv.getScore(params);
						
						newItem.prob = preWeight;
						
						heap.add(newItem, preWeight);
						
						if(i == 0) break;
					}//每一个前一个词
					
				}//每个当前词				
				
			}//每个起始点
			
			ArrayList<PyCharItem> items = heap.getK();
			forest.put(items, i);
						
		}//每个终止点

		return forest;
	}
	
	
	
	public PyCharLattice mkbeamWord(Parameters params,
			PyCharInstance inst,
			int K) {
	
		int l = 20;
		double d = -1e-5;

		int length = inst.length;
		String[] characterYins = inst.characterYins;
		
		PyCharLattice forest = new PyCharLattice(0, length, K);
		//ArrayList<String> allPos = Array.toArrayList(pipe.types);
		//i: 终止点
		for(int i=0; i<length+1; i++){
			
			Heap<PyCharItem> heap = new Heap<PyCharItem>(K);
						
			int j = Math.max(0, i-l);//候选词的初始节点

			//k: 起始点
			for(int k=j; k<i; k++) {
				//当前读音和可能的词
				String currentPinyin = Array.toPinyin(characterYins,k,i);
				ArrayList<String> candidateWords = pipe.pwPair.getWords(currentPinyin);

				if(candidateWords != null) {
				//每个当前词
					for(String word : candidateWords){
						
						//前一个词
						for(int m=0; m<K; m++){						
							PyCharItem pre = null;
							double preWeight = 0.0;
							if(k > 0) {							
								pre =  (forest.lattice)[k][m];
								if (pre == null) break;
								preWeight = pre.prob;							
							} 
							
							FeatureVector fv = new FeatureVector();
							PyCharItem newItem = new PyCharItem(k, i, currentPinyin, word, preWeight, fv, pre);
						
							pipe.addWordFeatures(inst, newItem, fv, false);
													
							preWeight += fv.getScore(params);
							
							newItem.prob = preWeight;
							
							heap.add(newItem, preWeight);
							
							if(k == 0) break;
						}//每一个前一个词
						
					}//每个当前词	
					
				}
				
			}//每个起始点
			
			ArrayList<PyCharItem> items = heap.getK();
			forest.put(items, i);
						
		}//每个终止点

		return forest;
	}
	
	

	
	public PyCharInstance getNgramBest(
			PyCharInstance inst,
			int K){

		PyCharLattice forest = mkbeamNgram(inst, K);
				
		PyCharInstance instance = forest.getBest(inst);
				
		return instance;
	}
	
	
	public PyCharLattice mkbeamNgram(
			PyCharInstance inst,
			int K) {
		int l = 20;
		double d = -1e-5;
		int wordLmOrder = pipe.wordLm.getLmOrder();

		int length = inst.length;
		String[] characterYins = inst.characterYins;
		
		PyCharLattice forest = new PyCharLattice(0, length, K);
		//ArrayList<String> allPos = Array.toArrayList(pipe.types);
		//i: 终止点
		for(int i=0; i<length+1; i++){
			
			Heap<PyCharItem> heap = new Heap<PyCharItem>(K);
			
			int j = Math.max(0, i-l);//候选词的初始节点

			//k: 起始点
			for(int k=j; k<i; k++) {
				//当前读音和可能的词
				String currentPinyin = Array.toPinyin(characterYins,k,i);
				ArrayList<String> candidateWords = pipe.pwPair.getWords(currentPinyin);

				if(candidateWords != null) {
					//每个当前词
					for(String word : candidateWords){
						
						//前一个词
						for(int m=0; m<K; m++){						
							PyCharItem pre = null;
							double preWeight = 0.0;
							if(k > 0) {							
								pre =  (forest.lattice)[k][m];
								if (pre == null) break;
								preWeight = pre.prob;							
							} 							
							FeatureVector fv = new FeatureVector();
							PyCharItem newItem = new PyCharItem(k, i, currentPinyin, word, preWeight, fv, pre);
																										
							List<String> ngram = newItem.getNgram(wordLmOrder);							
							preWeight += pipe.wordLm.getLogProb(ngram);
							//
							if(i==length) {
								ngram = newItem.getNgramEnd(wordLmOrder);
								preWeight += pipe.wordLm.getLogProb(ngram);
							}
														
							newItem.prob = preWeight;
							
							heap.add(newItem, preWeight);
							
							if(k == 0) break;
						}//每一个前一个词
						
					}//每个当前词	
					
				}
				
			}//每个起始点
			heap.buildHeap();
			ArrayList<PyCharItem> items = heap.getK();
			forest.put(items, i);
						
		}//每个终止点

		return forest;
	}
	
	
	
	/** 产生K个最优的词序列，和对应的词序列的概率 */
	public Object[] getNgramKbest(
			PyCharInstance inst,
			int K){
		PyCharLattice forest = mkbeamNgram(inst, K);				
		Object[] obj = forest.getNgramBestK(inst);				
		return obj;
	}
	

	/** 获得多个实例的Ngram模型的概率 */
	public List<Double> getNgramWeights(List<PyCharInstance> insts) {
		List<Double> weights = new ArrayList<Double>();
		for(PyCharInstance inst : insts) {
			weights.add(getNgramWeight(inst));
		}
		return weights;		
	}
	
	/** 获得一个实例的Ngram模型的概率 */
	public double getNgramWeight(PyCharInstance inst) {
		int wordLmOrder = pipe.wordLm.getLmOrder();
		PyCharItem[] items = inst.getItems();
		int length = items.length;
		double weight = 0.0;
		for(int i=0; i<length; i++) {
			PyCharItem newItem = items[i];
			List<String> ngram = newItem.getNgram(wordLmOrder);							
			weight += pipe.wordLm.getLogProb(ngram);
			//
			if(i==length-1) {
				ngram = newItem.getNgramEnd(wordLmOrder);
				weight += pipe.wordLm.getLogProb(ngram);
			}
		}
		return weight;
	}
	

	
	
	
	
	
}
