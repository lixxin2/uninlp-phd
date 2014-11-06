package edu.hitsz.nlp.segpos;

import java.util.ArrayList;

import edu.hitsz.algorithm.Heap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.mstjoint.WordPos;
import edu.hitsz.nlp.util.Array;

public class CharDecoder{

	CharPipe pipe;

	public CharDecoder(CharPipe pipe){
		this.pipe = pipe;
	}

	/**
	 * 得到最优序列中的特征
	 * @since Sep 6, 2012
	 * @param params
	 * @param inst
	 * @param K
	 * @return
	 */
	public FeatureVector getFeatureVector(Parameters params,
			Instance inst,
			int K){

		Lattice forest = kbeamHeap(params, inst, K);
		
		//return forest.getFeatureVector();
		
		Instance instance = forest.getBestInstance();
		
		FeatureVector fv = pipe.createFeatureVectorWord(instance);
		
		return fv;
	}
	
	
	
	public Instance getBestInstance(Parameters params,
			Instance inst,
			int K){

		Lattice forest = kbeamHeap(params, inst, K);
		
		return forest.getBestInstance();
		
	}
	
	
	
	/**
	 * 解码句子，得到kbeam lattice.
	 * <p> End表示在每个beam中的排序方式
	 * <p> preItem 都只采用最好的，最优的是正确的，但是最优的Kbest个并不一定是最优的
	 * @since 2012-3-2
	 * @param pipe
	 * @param params
	 * @param inst
	 * @param K
	 * @return
	 */
	public Lattice kbeamEnd(Parameters params,
			Instance inst, 
			int K) {
	
		int l = 20;
		//double d = -1e-5;

		int length = inst.charLength();
		String[] characters = inst.chars;
		
		Lattice forest = new Lattice(0, length, inst, K);
		//ArrayList<String> allPos = Array.toArrayList(pipe.types);
		//i: 终止点
		for(int i=0; i<length+1; i++){
			int j = Math.max(0, i-l);//候选词的初始节点

			//k: 起始点
			for(int k=j; k<i; k++) {
				
				//当前词和可能的词性
				String currentWord = Array.toWord(characters,k,i);
				ArrayList<String> candidatePos = pipe.freq.getPos(currentWord);

				//词长
				//int wordLength = i-k;

				//每个当前词词性
				for(String pos : candidatePos){					
									
					FeatureVector fv = new FeatureVector();	
					pipe.addCharFeatures(inst, k, i, pos, fv, false);
					double weight = fv.getScore(params);		
											
					Item pre = null;
					double preWeight = 0.0;
					if(k > 0) {							
						pre =  (forest.lattice)[k][0];
						preWeight = pre.prob;							
					} 
					preWeight += weight; 
					Item tmpItem = new Item(k, i, currentWord, pos, preWeight, fv, pre);											
					forest.addEnd(i, preWeight, tmpItem);					
					
					
				}//每个当前词词性
			}//每个起始点
		}//每个终止点

		return forest;
	}
	

	/**
	 * 解码句子，得到kbeam lattice.
	 * 
	 * <p> 采用堆对每个位置i的所有可能item排序
	 */
	public Lattice kbeamHeap(Parameters params,
			Instance inst, 
			int K) {
	
		int l = 20;

		int length = inst.charLength();
		String[] characters = inst.chars;
		
		Lattice forest = new Lattice(0, length, inst, K);
		
		//i: 终止点
		for(int i=0; i<length+1; i++){
						
			Heap<Item> heaps = new Heap<Item>(K);
			
			int j = Math.max(0, i-l);//候选词的初始节点

			//k: 起始点
			//for(int k=j; k<i; k++) {
			
			for(int k=i-1; k>=j; k--) {
				//当前词和可能的词性
				if(k < i-1 && (
						(characters[k].matches("[a-zA-Z]+\\.") && characters[k+1].matches("[a-zA-Z]+"))
						)) {
						break;
				}
								
				String currentWord = Array.toWord(characters,k,i);
				ArrayList<String> candidatePos = pipe.freq.getPos(currentWord);

				//词长
				//int wordLength = i-k;

				//每个当前词词性
				for(String pos : candidatePos){					
									
					FeatureVector fv = new FeatureVector();	
					pipe.addCharFeatures(inst, k, i, pos, fv, false);
					double weight = fv.getScore(params);		
											
					Item pre = null;
					double preWeight = 0.0;
					if(k > 0) {							
						pre =  (forest.lattice)[k][0];
						preWeight = pre.prob;							
					} 
					preWeight += weight; 
					Item tmpItem = new Item(k, i, currentWord, pos, preWeight, fv, pre);
					//forest.addEnd(i, preWeight, tmpItem);												
					heaps.add(tmpItem, preWeight);				
					
				}//每个当前词词性
								
			}//每个起始点
			heaps.buildHeap();
			ArrayList<Item> items = heaps.getK();
			int k=0;
			for(Item item : items) {
				forest.add(i, k, item);
				k++;
			}
			
		}//每个终止点

		return forest;
	}
	
	
	
	

	
	/**
	 * 获取依存句法需要的各种材料
	 * 每个词前面，后面，中间的词
	 * @since Sep 9, 2012
	 * @param params
	 * @param inst
	 * @param K
	 * @return
	 */
	public Object[] getCharLMR(	Parameters params,
			Instance inst) {
	
		int l = 20;
		int length = inst.charLength();
		String[] chars = inst.chars;
		WordPos[] wordPoses = new WordPos[length*length+length];
		//找到所有可能的词
		for(int i=0; i<=length; i++) {			
			for(int j=1; j<=l; j++) {				
				int k = i+j;
				if(k <= length) {
					String currentWord = Array.toWord(chars,i,k);
					ArrayList<String> candidatePos = pipe.freq.getPos(currentWord);
					String tmpPos = "";
					double tmpWeight = -1e5;
					for(String pos : candidatePos){						
						FeatureVector fv = new FeatureVector();	
						pipe.addCharFeatures(inst, i, k, pos, fv, false);
						double weight = fv.getScore(params);	
						if(weight > tmpWeight) {
							tmpPos = pos;
							tmpWeight = weight;
						}
					}
					wordPoses[i*length+k] = new WordPos(currentWord, tmpPos, i, k, tmpWeight);
				}
			}			
		}
		//存储最基本的链表
		ArrayList<ArrayList<ArrayList<Integer>>> linkItems = new ArrayList<ArrayList<ArrayList<Integer>>>(length+1);
		ArrayList<ArrayList<Double>> linkWeights = new ArrayList<ArrayList<Double>>(length+1);
		for(int i=0; i<=length; i++) {
			ArrayList<ArrayList<Integer>> linkItem = new ArrayList<ArrayList<Integer>>(length+1);
			ArrayList<Double> linkWeight = new ArrayList<Double>(length+1);
			for(int j=0; j<=length; j++) {
				linkItem.add(new ArrayList<Integer>());
				linkWeight.add(-1e5);
			}
			linkItems.add(linkItem);
			linkWeights.add(linkWeight);
		}
		//找到最好的链接		
		for(int subLength=1; subLength<=length; subLength++) {
			//句首
			for(int i=0; i<=length; i++) {
				//句子末尾
				int k = subLength+i;
				if(k <= length) {
				//句子中
					double tmpWeight = -1e5;
					if(wordPoses[i*length+k] != null){
						tmpWeight = wordPoses[i*length+k].getProb();
					}
					int tmpJ = 0;
					for(int j=i+1; j<k; j++) {
						double leftWeight = linkWeights.get(i).get(j);
						double rightWeight = linkWeights.get(j).get(k);					
						double curWeight = leftWeight + rightWeight;
						if(curWeight > tmpWeight) {
							tmpJ = j;
							tmpWeight = curWeight;
						}					
					}
					
					ArrayList<Integer> items = new ArrayList<Integer>();
					if(tmpJ == 0){
						items.add(i*length+k);
					}
					else {
						ArrayList<Integer> left = linkItems.get(i).get(tmpJ);
						ArrayList<Integer> right = linkItems.get(tmpJ).get(k);
						items.addAll(left);
						items.addAll(right);
					}	
					linkItems.get(i).set(k, items);
					linkWeights.get(i).set(k, tmpWeight);
				}
			}
		}		
		//找到所有的
		Object[] objects = new Object[2];
		objects[0] = wordPoses;
		objects[1] = linkItems;	
		
		return objects;	
		
	}
	
	


}
