package edu.hitsz.nlp.segpos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.algorithm.Heap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.util.Array;

public class WordDecoder{

	WordPipe pipe;

	public WordDecoder(WordPipe pipe){
		this.pipe = pipe;
	}

	/** multibeam decoding */
	public FeatureVector getFeatureVector(Parameters params,
			Instance inst,
			int K){

		Lattice forest = mkbeam(params, inst, K);
		
		FeatureVector sfv = forest.getFeatureVector();
		
		sfv.sfv2fv(pipe);
		
		return sfv;
	}
	
	
	
	
	public FeatureVector getFeatureVectorPOS(Parameters params,
			Instance instance, 
			int K){

		Lattice forest = kbeamPOS(params, instance, K);
		
		FeatureVector sfv = forest.getFeatureVectorPOS();
		
		sfv.sfv2fv(pipe);
		
		return sfv;
	}
	
	
	/**得到最优的解析解果*/
	public Instance getBestInstance(Parameters params,
			Instance inst,
			int K){

		Lattice forest = mkbeam(params, inst, K);
		
		return forest.getBestInstance();
		
	}
	
	
	public Object[] getBestPOS(Parameters params,
			Instance inst,
			int K){

		Lattice forest = kbeamPOS(params, inst, K);
		
		Instance instance = forest.getBestInstance();
		double instanceWeight = forest.getBestProb();
		Object[] obj = new Object[2];
		obj[0] = instance; obj[1] = instanceWeight;
		return obj;		
	}
	
	
	/**
	 * 解码句子，得到kbeam lattice
	 * <p> Zhang and Clark, 2008的multibeam
	 * @since 2012-3-2
	 * @param pipe
	 * @param params
	 * @param inst
	 * @param K
	 * @return
	 */	
	public Lattice mkbeam(Parameters params,
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

				//每个当前词词性
				for(String pos : candidatePos){	
					
					//前一个词
					for(int m=0; m<K; m++){						
						Item pre = null;
						double preWeight = 0.0;
						if(k > 0) {							
							pre =  (forest.lattice)[k][m];
							if (pre == null) break;
							preWeight = pre.prob;							
						} 
						
						FeatureVector fv = new FeatureVector();
						Item newItem = new Item(k, i, currentWord, pos, preWeight, fv, pre);
					
						pipe.addWordPOSFeatures(inst, newItem, fv, false);
												
						preWeight += fv.getScore(params);
						
						newItem.prob = preWeight;
						
						forest.addEnd(i, preWeight, newItem);
						if(k == 0) break;
					}//每一个前一个词
					
				}//每个当前词词性				
			}//每个起始点
		}//每个终止点

		return forest;
	}
	
	
	public Lattice mkbeamHeap(Parameters params,
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

			Heap<Item> heaps = new Heap<Item>(K);
			
			int j = Math.max(0, i-l);//候选词的初始节点

			//k: 起始点
			for(int k=j; k<i; k++) {
				//当前词和可能的词性
				String currentWord = Array.toWord(characters,k,i);
				ArrayList<String> candidatePos = pipe.freq.getPos(currentWord);

				//每个当前词词性
				for(String pos : candidatePos){	
					
					//前一个词
					for(int m=0; m<K; m++){						
						Item pre = null;
						double preWeight = 0.0;
						if(k > 0) {							
							pre =  (forest.lattice)[k][m];
							if (pre == null) break;
							preWeight = pre.prob;							
						} 
						
						FeatureVector fv = new FeatureVector();
						Item newItem = new Item(k, i, currentWord, pos, preWeight, fv, pre);
					
						pipe.addWordPOSFeatures(inst, newItem, fv, false);
												
						preWeight += fv.getScore(params);
						
						newItem.prob = preWeight;
						
						//forest.addEnd(i, preWeight, newItem);						
						heaps.add(newItem, preWeight);	
						
						if(k == 0) break;
					}//每一个前一个词
					
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
	 * "Yue Zhang and Stephen Clark 2010 single beam"
	 * <p> 
	 * @since 2013-2-8
	 * @param params
	 * @param inst
	 * @param K beam
	 * @param iter 迭代次数
	 * @param isTrain 是否为训练，训练时更新权重 
	 * @return
	 */
	public Instance kbeam(Parameters params,
			Instance inst,
			int K,
			int[] iter,
			boolean isTrain) {	

		String[] characters = inst.chars;
		int length = characters.length;
		
		Lattice forest = new Lattice(0, length, inst, K);
		
		//i: 终止点
		for(int i=1; i<length+1; i++){
			
			HashMap<String, Item> items = new HashMap<String, Item>();
			String preWord = "pW";
			String prePos = "pP";
			String curChar = characters[i-1];
			
			//前一个候选值
			for(int m=0; m<K; m++){		
				
				Item pre = null;
				double preWeight = 0.0;
				if(i > 1) {							
					pre =  (forest.lattice)[i-1][m];
					if (pre == null) break;
					preWeight = pre.prob;							
				} 
				//append, 在原有词上添加一个字
				if(pre != null) {
					if(pre.left != null) {
						preWord = pre.form;
						prePos = pre.pos;
					}
					Item newItem = pre.copy();
					newItem.t += 1;
					newItem.form += curChar;
					if(pipe.dict.isValidWordPOS(newItem.form, newItem.pos)) { //如果词有效
						FeatureVector fv = new FeatureVector();
						pipe.addAppendingFeatures(inst, newItem, fv, false);
						double curWeight = preWeight + fv.getScore(params);
						newItem.prob = curWeight;
						String fit = newItem.pos + "_" + prePos + "_" + preWord;
						//如果不包含该相同前缀 或者 已有前缀的词小于当前Item的前缀，则加入
						if(!items.containsKey(fit) ||
								items.get(fit).prob < newItem.prob) {
							items.remove(fit);
							items.put(fit, newItem);
						}
					}
				}
				//start, 以现有字开始一个新词
				if(pre != null || i== 1) {
					if(pre != null) {
						preWord = pre.form;
						prePos = pre.pos;
					}
					ArrayList<String> candidatePos = pipe.dict.getCharPosTags(curChar);
					//ArrayList<String> candidatePos = pipe.dict.allSetPOS;
					for(String pos : candidatePos) {
						FeatureVector fv = new FeatureVector();
						Item newItem = new Item(i, i, curChar, pos, preWeight, fv, pre);
						pipe.addStartingFeatures(inst, newItem, fv, false);
						double curWeight = preWeight + fv.getScore(params);
						newItem.prob = curWeight;
						String fit = newItem.pos + "_" + prePos + "_" + preWord;
						//如果不包含该相同前缀 或者 已有前缀的词小于当前Item的前缀，则加入
						if(!items.containsKey(fit) ||
								items.get(fit).prob < newItem.prob) {
							items.remove(fit);
							items.put(fit, newItem);
						}
					}
				}
				
				if(i == 1) break; //对于首字，只有一个前缀
			}
			//通过堆排序
			Heap<Item> heaps = new Heap<Item>(K);
			Iterator<Entry<String, Item>> iterator = items.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, Item> entry = iterator.next();
				Item item = entry.getValue();
				heaps.add(item, item.prob);
			}
			heaps.buildHeap();
			ArrayList<Item> itemList = heaps.getK();
			
			//如果是训练时,判断是否需要更新
			//判断原则是：所有候选集中都没有和gold相同的结果，
			if(isTrain && i != length) {
				Instance goldInstance = inst.getInstance(i-1);
				boolean isUpdate = true;        
				for(Item item : itemList) {
					Instance predictInstance = item.getInstance();
					if(goldInstance.matches(predictInstance)) {
						isUpdate =  false;
						break;
					}					
				}
				if(isUpdate) {
					Instance predictInstance = itemList.get(0).getInstance();
					FeatureVector goldFV = pipe.createFeatureVectorSingleBeam(goldInstance);
					FeatureVector predictFV = pipe.createFeatureVectorSingleBeam(predictInstance);
					params.update(goldFV.fv, predictFV.fv, iter[0]);
					iter[0] += 1;
					return null;
				}
			}
			int k=0;
			for(Item item : itemList) {
				forest.add(i, k, item);
				k++;
			}
		}//每个终止点
		if(isTrain) {
			Instance predictInstance = forest.lattice[length][0].getInstance();
			if(!inst.matches(predictInstance)) {
				FeatureVector goldFV = pipe.createFeatureVectorSingleBeam(inst);
				FeatureVector predictFV = pipe.createFeatureVectorSingleBeam(predictInstance);
				params.update(goldFV.fv, predictFV.fv, iter[0]);
				iter[0] += 1;
			}
		}		
			
		return forest.lattice[length][0].getInstance();
		
	}
	


	public Lattice kbeamPOS(Parameters params,
			Instance inst,
			int K) {
	
		//int l = 20;
		//double d = -1e-5;

		String[] words = inst.words;
		int length = words.length;
		
		Lattice forest = new Lattice(0, length-1, inst, K, true);
		//ArrayList<String> allPos = Array.toArrayList(pipe.types);
		//i: 终止点
		
		Item[] allItems = new Item[length];
		Item preItem = null;
		int start = 0;
		int end = 0;
		
		for(int i = 0; i < length; i++) {
			end = start + words[i].length();
			Item curItem = new Item(start, end, words[i], preItem, true);
			allItems[i] = curItem;
			start = end;
			preItem = curItem;
		}		
		
		for(int i=0; i<length; i++){

			//当前词和可能的词性
			String currentWord = words[i];
			ArrayList<String> candidatePos = pipe.freq.getPos(currentWord);

			//每个当前词词性
			for(String pos : candidatePos){	
				
				//前一个词
				for(int m=0; m<K; m++){						
					Item pre = null;
					double preWeight = 0.0;
					if(i > 0) {							
						pre =  (forest.lattice)[i-1][m];
						if (pre == null) break;
						preWeight = pre.prob;							
					} 
					
					FeatureVector fv = new FeatureVector();
					
					Item newItem = allItems[i].copy();
					newItem.pos = pos;
					newItem.left = pre;
					if(i+1 == length)
						newItem.right = null;
					else
						newItem.right = allItems[i+1];
					newItem.fv = fv;
					
					pipe.addPOSFeatures(inst, newItem, fv, false);
											
					preWeight += fv.getScore(params);
					
					newItem.prob = preWeight;
					
					forest.addEnd(i, preWeight, newItem);
					if(i == 0) break;
				}//每一个前一个词					
			}//每个当前词词性		
		}//每个终止点

		return forest;
	}

	
}
