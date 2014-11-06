package edu.hitsz.nlp.mstjoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.hitsz.algorithm.Heap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;

import gnu.trove.TIntIntHashMap;


/**
 * 依存句法分析器
 * @author tm
 *
 */

public class DependencyDecoderJoint {

    DependencyPipeJoint pipe;

    public DependencyDecoderJoint(DependencyPipeJoint pipe) {
    	this.pipe = pipe;
    }

    /**
     * 返回是两个点之间概率最大的依存关系数组
     * @param nt_probs
     * @param len
     * @return
     */
    protected int[][] getTypes(double[][][][] nt_probs, int len) {
    	
		int[][] static_types = new int[len][len];
		for(int i = 0; i < len; i++) {
		    for(int j = 0; j < len; j++) {
			if(i == j) {static_types[i][j] = 0; continue; }
			int wh = -1;
			double best = Double.NEGATIVE_INFINITY;
			for(int t = 0; t < pipe.types.length; t++) {
			    double score = 0.0;
			    if(i < j)
			    	score = nt_probs[i][t][0][1] + nt_probs[j][t][0][0];
			    else
			    	score = nt_probs[i][t][1][1] + nt_probs[j][t][1][0];

			    if(score > best) { wh = t; best = score; }
			}
			static_types[i][j] = wh;
		    }
		}
		return static_types;
    }

    /**
     * static type for each edge: run time O(n^3 + Tn^2) T is number of types
     * @param inst
     * @param fvs
     * @param probs
     * @param nt_fvs
     * @param nt_probs
     * @param K
     * @return
     */
    public Object[][] decodeProjective(DependencyInstanceJoint inst,
				       FeatureVector[][][] fvs,
				       double[][][] probs,
				       FeatureVector[][][][] nt_fvs,
				       double[][][][] nt_probs, int K) {

		String[] forms = inst.forms;
		//String[] pos = inst.postags;

		int[][] static_types = null;
		if(pipe.labeled) {
		    static_types = getTypes(nt_probs,forms.length);
		}

		KBestParseForest pf = new KBestParseForest(0,forms.length-1,inst,K);

		for(int s = 0; s < forms.length; s++) {
		    pf.add(s,-1,0,0.0,new FeatureVector());
		    pf.add(s,-1,1,0.0,new FeatureVector());
		}

		//j represent the length of the span
		for(int j = 1; j < forms.length; j++) {
			//i represents the start of the span
			for(int s = 0; s < forms.length && s+j < forms.length; s++) {
				//t represent the end of the span
				int t = s+j;

				FeatureVector prodFV_st = fvs[s][t][0];
				FeatureVector prodFV_ts = fvs[s][t][1];
				double prodProb_st = probs[s][t][0];
				double prodProb_ts = probs[s][t][1];

				int type1 = pipe.labeled ? static_types[s][t] : 0;
				int type2 = pipe.labeled ? static_types[t][s] : 0;

				FeatureVector nt_fv_s_01 = nt_fvs[s][type1][0][1];
				FeatureVector nt_fv_s_10 = nt_fvs[s][type2][1][0];
				FeatureVector nt_fv_t_00 = nt_fvs[t][type1][0][0];
				FeatureVector nt_fv_t_11 = nt_fvs[t][type2][1][1];
				double nt_prob_s_01 = nt_probs[s][type1][0][1];
				double nt_prob_s_10 = nt_probs[s][type2][1][0];
				double nt_prob_t_00 = nt_probs[t][type1][0][0];
				double nt_prob_t_11 = nt_probs[t][type2][1][1];

				//r represent the mid of the span
				for(int r = s; r <= t; r++) {

				    /** first is direction, second is complete*/
				    /** _s means s is the parent*/
				    if(r != t) {
						ParseForestItem[] b1 = pf.getItems(s,r,0,0);
						ParseForestItem[] c1 = pf.getItems(r+1,t,1,0);

						if(b1 != null && c1 != null) {
						    int[][] pairs = pf.getKBestPairs(b1,c1);
						    for(int k = 0; k < pairs.length; k++) {

								if(pairs[k][0] == -1 || pairs[k][1] == -1)
								    break;

								int comp1 = pairs[k][0]; int comp2 = pairs[k][1];

								double bc = b1[comp1].prob+c1[comp2].prob;

								double prob_fin = bc+prodProb_st;
								FeatureVector fv_fin = prodFV_st;
								if(pipe.labeled) {
								    fv_fin = nt_fv_s_01.cat(nt_fv_t_00.cat(fv_fin));
								    prob_fin += nt_prob_s_01+nt_prob_t_00;
								}
								pf.add(s,r,t,type1,0,1,prob_fin,fv_fin,b1[comp1],c1[comp2]);

								prob_fin = bc+prodProb_ts;
								fv_fin = prodFV_ts;
								if(pipe.labeled) {
								    fv_fin = nt_fv_t_11.cat(nt_fv_s_10.cat(fv_fin));
								    prob_fin += nt_prob_t_11+nt_prob_s_10;
								}
								pf.add(s,r,t,type2,1,1,prob_fin,fv_fin,b1[comp1],c1[comp2]);

						    }
						}
				    }
				}


				for(int r = s; r <= t; r++) {

				    if(r != s) {
						ParseForestItem[] b1 = pf.getItems(s,r,0,1);
						ParseForestItem[] c1 = pf.getItems(r,t,0,0);
						if(b1 != null && c1 != null) {
						    int[][] pairs = pf.getKBestPairs(b1,c1);
						    for(int k = 0; k < pairs.length; k++) {

								if(pairs[k][0] == -1 || pairs[k][1] == -1)
								    break;

								int comp1 = pairs[k][0]; int comp2 = pairs[k][1];

								double bc = b1[comp1].prob+c1[comp2].prob;

								if(!pf.add(s,r,t,-1,0,0,bc,
									   new FeatureVector(),
									   b1[comp1],c1[comp2]))
								    break;
						    }
						}
				    }

				    if(r != t) {
						ParseForestItem[] b1 = pf.getItems(s,r,1,0);
						ParseForestItem[] c1 = pf.getItems(r,t,1,1);
						if(b1 != null && c1 != null) {
						    int[][] pairs = pf.getKBestPairs(b1,c1);
						    for(int k = 0; k < pairs.length; k++) {

								if(pairs[k][0] == -1 || pairs[k][1] == -1)
								    break;

								int comp1 = pairs[k][0]; int comp2 = pairs[k][1];

								double bc = b1[comp1].prob+c1[comp2].prob;

								if(!pf.add(s,r,t,-1,1,0,bc,
									   new FeatureVector(),b1[comp1],c1[comp2]))
								    break;
						    }
						}
				    }
				}
			}
		}

		return pf.getBestParsesAfter(pipe, inst);
    }
    

    
    
    
   /** 解析算法,包括两个数据结构，
    */
    public ParseForestItemJoint[] decodeProjectiveJointIndex(Parameters param,
    		DependencyInstanceJoint inst, 
    		HashMap<WordPos, Integer> allWordPos,
    		WordPos[][] leftWordPoses,
    		WordPos[][] rightWordPoses,
    		WordPos[] leftWordPos,
    		WordPos[] rightWordPos,
    		int[][] leftIndexes,
    		int[][] rightIndexes,
		    int K) {
    	
     	int length = rightWordPoses.length;               //句子中字的数目，包括root
    	int lSize = rightWordPoses[0].length;             //从左边开始往右数的WordPos的数目
    	int rSize = leftWordPoses[0].length;              //从右边开始往左数的WordPos的数目，为K
    	
    	int nodeSize = allWordPos.size();                 //lattice中WordPos的数目
    	
    	//当前词的概率
		double wordProb = 0;
		double depProb = 1 - wordProb;
		
		KBestParseForestJointIndex pf = new KBestParseForestJointIndex(0,nodeSize,inst,K);
		
		// dir: 0 -> head left; 1 <- head right
		// comp: 0 complete; 1 incomplete
    	
		//首先将所有的WordPos放到forest作为最底层的span
		for(int s = 0; s < length; s++) {	
			for(int r=0; r < lSize; r++) {
				WordPos cur = rightWordPoses[s][r];
				if( cur != null){
					int index = cur.index;
					//int start = cur.getStart();
					//int end = cur.getEnd();
					//int leftK = cur.leftIndex;
					//int rightK = cur.rightIndex;
					double prob = wordProb * cur.getProb();	
					ArrayList<WordPos> allWP = new ArrayList<WordPos>();
					allWP.add(cur);
					pf.addLR(index, -1,0, 0, prob, 0, 0, 0, cur, cur, allWP);
					pf.addLR(index, -1,1, 0, prob, 0, 0, 0, cur, cur, allWP);
				}
				else
					break;
			}	    
		}
		
		//j represent the length of the span
		for(int j = 1; j < length; j++) {                      //span的宽度
			//i represents the start of the span
			for(int s = 0; s < length && s+j < length; s++) {  //初始位置s，和结束位置t
				//t represent the end of the span
				int t = s+j;
				
				//System.out.println("s:" + s +",t:" +t);

				int type1 = pipe.labeled ? 0 : 0;
				int type2 = pipe.labeled ? 0 : 0;				

		    	int[] leftLeft = rightIndexes[s];
		    	int[] rightRight = leftIndexes[t];
		    	
		    	//对每个span建立一个heap,存储最优的k个span
		    	//存储complete span
    			Heap[][] leftHeap = new Heap[lSize][rSize];
    			Heap[][] rightHeap = new Heap[lSize][rSize];
				for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
					for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
						leftHeap[ll][rr] = new Heap<ParseForestItemJoint>(K);
						rightHeap[ll][rr] = new Heap<ParseForestItemJoint>(K);
					}
				}
				
				//完整的span合并，span[s][r],span[r+1][t]
				//span[s][t][0][1]=span[s][r][0][0]+span[r+1][t][1][0]
				//span[s][t][1][1]=span[s][r][0][0]+span[r+1][t][1][0]
				//r represent the mid of the span
				for(int r = s; r < t; r++) {                   //中间位置t
					
				    /** first is direction, second is complete*/
				    /** _s means s is the parent*/
										
				    if(r != t) {
				    	int[] leftRight = leftIndexes[r];
				    	int[] rightLeft = rightIndexes[r+1];
							
				    	//每个左span的左index和每个右span的右index
				    	for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
				    		for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
				    			
								//每个左span的右index和右span的左index			    			
						    	for(int lr=0; lr<rSize && leftRight[lr] != 0; lr++) {
						    		for(int rl=0; rl<lSize && rightLeft[rl] != 0; rl++) {
						    			
						    			ParseForestItemJoint[] leftItems = pf.chart[leftLeft[ll]][leftRight[lr]][0][0];
						    			ParseForestItemJoint[] rightItems = pf.chart[rightLeft[rl]][rightRight[rr]][1][0];
						    			
						    			for(int lk=0; lk<K && leftItems[lk] != null; lk++) {
							    			for(int rk=0; rk<K && rightItems[rk] != null; rk++) {
							    				
							    				double bc = leftItems[lk].prob + rightItems[rk].prob;
												//int leftK = leftItems[lk].leftK;
												//int rightK = rightItems[rk].rightK;
												WordPos leftWP = leftItems[lk].leftWP;
												WordPos rightWP = rightItems[rk].rightWP;
												ArrayList<WordPos> allWP = new ArrayList<WordPos>();
												allWP.addAll(leftItems[lk].allWP);
												allWP.addAll(rightItems[rk].allWP);
												//prob[leftK][rightK][0];
												FeatureVector fv = pipe.createDependencyFeature(inst, length, s, t, leftWordPos, rightWordPos, leftIndexes, rightIndexes, 0, leftWP, rightWP, allWP, false);
												double fatherSonProb = fv.getScore(param);
												double prob_fin = bc + depProb * fatherSonProb;
												ParseForestItemJoint item = new ParseForestItemJoint(s,r,t,type1,0,1,prob_fin,
														null,leftItems[lk],rightItems[rk],0, 0, leftWP, rightWP, allWP);
												leftHeap[ll][rr].add(item, prob_fin);
												//prob[leftK][rightK][1]
												fv = pipe.createDependencyFeature(inst, length, s, t, leftWordPos, rightWordPos, leftIndexes, rightIndexes, 1, leftWP, rightWP, allWP, false);
												fatherSonProb = fv.getScore(param);
												prob_fin = bc + depProb * fatherSonProb;
												item = new ParseForestItemJoint(s,r,t,type2,1,1,prob_fin,
														null,leftItems[lk],rightItems[rk],0, 0, leftWP, rightWP, allWP);
												rightHeap[ll][rr].add(item, prob_fin);
							    			}
						    			}
						    		}
					    		}						    	
					    	}
				    	}				    	
				    }			    	
				}
				//
				for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
					for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
						leftHeap[ll][rr].buildHeap();
				    	ArrayList<ParseForestItemJoint> items = leftHeap[ll][rr].getK();
				    	int k=0;
				    	for(ParseForestItemJoint item : items) {
				    		pf.addLR(leftLeft[ll],rightRight[rr], 0, 1, k, item);
				    		k++;
				    	}
				    	rightHeap[ll][rr].buildHeap();
				    	items = rightHeap[ll][rr].getK();
				    	k=0;
				    	for(ParseForestItemJoint item : items) {
				    		pf.addLR(leftLeft[ll],rightRight[rr], 1, 1, k, item);
				    		k++;
				    	}
					}
				}
								

		    	//对每个span建立一个heap,存储最优的k个span
				//存储incomplete span
				//span[r][t][0][0]=span[r][s][0][1]+span[s][t][0][0]
    			Heap[][] lheap = new Heap[lSize][rSize];
				for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
					for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
						lheap[ll][rr] = new Heap<ParseForestItemJoint>(K);
					}
				}
			    //
		    	//对每个span建立一个heap,存储最优的k个span
		    	//存储incomplete span
				//span[r][t][1][0]=span[r][s][1][0]+span[s][t][1][1]
    			Heap[][] rheap = new Heap[lSize][rSize];
				for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
					for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
						rheap[ll][rr] = new Heap<ParseForestItemJoint>(K);
					}
				}
				for(int r = s; r <= t; r++) {					
									
				    if(r != s) {
				    	
				    	int[] leftRight = leftIndexes[r];				    	
				    	
				    	for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
				    		for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
				  								
								for(int lr=0; lr<rSize && leftRight[lr] != 0; lr++) {
									
					    			ParseForestItemJoint[] leftItems = pf.chart[leftLeft[ll]][leftRight[lr]][0][1];
					    			ParseForestItemJoint[] rightItems = pf.chart[leftRight[lr]][rightRight[rr]][0][0];
					    			
				    				for(int lk=0; lk<K && leftItems[lk] != null; lk++) {
						    			for(int rk=0; rk<K && rightItems[rk] != null; rk++) {
						    				
						    				double bc = leftItems[lk].prob + rightItems[rk].prob;
											int leftK = leftItems[lk].leftK;
											int rightK = rightItems[rk].rightK;
											WordPos leftWP = leftItems[lk].leftWP;
											WordPos rightWP = rightItems[rk].rightWP;
											ArrayList<WordPos> allWP = new ArrayList<WordPos>();
											allWP.addAll(leftItems[lk].allWP);
											allWP.remove(allWP.size()-1);
											allWP.addAll(rightItems[rk].allWP);
											//prob[leftK][rightK][0];
											ParseForestItemJoint item = new ParseForestItemJoint(s,r,t,-1,0,0,bc,
													null,leftItems[lk],rightItems[rk],leftK, rightK, leftWP, rightWP, allWP);
											lheap[ll][rr].add(item, bc);											
						    			}
					    			}
								}
				    		}
				    	}
				    }
				    if(r != t) {
				    	int[] leftRight = leftIndexes[r];				    	
				    	
				    	for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
				    		for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {
				    											
								for(int lr=0; lr<rSize && leftRight[lr] != 0; lr++) {
					    			ParseForestItemJoint[] leftItems = pf.chart[leftLeft[ll]][leftRight[lr]][1][0];
					    			ParseForestItemJoint[] rightItems = pf.chart[leftRight[lr]][rightRight[rr]][1][1];
					    			
				    				for(int lk=0; lk<K && leftItems[lk] != null; lk++) {
						    			for(int rk=0; rk<K && rightItems[rk] != null; rk++) {
						    				double bc = leftItems[lk].prob + rightItems[rk].prob;
											int leftK = leftItems[lk].leftK;
											int rightK = rightItems[rk].rightK;
											WordPos leftWP = leftItems[lk].leftWP;
											WordPos rightWP = rightItems[rk].rightWP;
											ArrayList<WordPos> allWP = new ArrayList<WordPos>();
											allWP.addAll(leftItems[lk].allWP);
											allWP.remove(allWP.size()-1);
											allWP.addAll(rightItems[rk].allWP);
											//prob[leftK][rightK][0];
											ParseForestItemJoint item = new ParseForestItemJoint(s,r,t,-1,1,0,bc,
													null,leftItems[lk],rightItems[rk],leftK, rightK, leftWP, rightWP, allWP);
											rheap[ll][rr].add(item, bc);											
						    			}
					    			}
								}								
				    		}
				    	}
				    }
				}
		    	for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
					for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {								
						lheap[ll][rr].buildHeap();
				    	ArrayList<ParseForestItemJoint> items = lheap[ll][rr].getK();
				    	int k=0;
				    	for(ParseForestItemJoint item : items) {
				    		pf.addLR(leftLeft[ll],rightRight[rr], 0, 0, k, item);
				    		k++;
				    	}
					}
				}
		    	for(int ll=0; ll<lSize && leftLeft[ll] != 0; ll++) {
					for(int rr=0; rr<rSize && rightRight[rr] !=0; rr++) {								
						rheap[ll][rr].buildHeap();
				    	ArrayList<ParseForestItemJoint> items = rheap[ll][rr].getK();
				    	int k=0;
				    	for(ParseForestItemJoint item : items) {
				    		pf.addLR(leftLeft[ll],rightRight[rr], 1, 0, k, item);
				    		k++;
				    	}
					}
				}
			}
		}
		int[] left = rightIndexes[0];
    	int[] right = leftIndexes[length-1];
		Heap<ParseForestItemJoint> heap = new Heap<ParseForestItemJoint>(K);
    	for(int ll=0; ll<lSize && left[ll] != 0; ll++) {
    		for(int rr=0; rr<rSize && right[rr] !=0; rr++) {
    			ParseForestItemJoint[] items = pf.chart[left[ll]][right[rr]][0][0];
    			for(ParseForestItemJoint item: items) {
    				if(item != null) {
	    				double prob = item.prob;
	    				heap.add(item, prob);
    				}
    			}
    		}
    	}    		
		
		ArrayList<ParseForestItemJoint> items = heap.getK();
		ParseForestItemJoint[] itemVec = new ParseForestItemJoint[K];
		int k=0;
		if(items.size() > 0)
			for(ParseForestItemJoint item : items)
				itemVec[k++] = item;
		return itemVec;
    
    }
   
    
    
 

    
    /**
     * 解析得到最优树，然后得到最优句法树的所有特征
     * @since Mar 27, 2012
     * @param inst
     * @param obj
     * @param probs
     * @param added true:用于训练过程，表示添加特征到特征集； false,用于测试，不填加新的特征到特征集
     * @return
     */
    public FeatureVector getBestFeatureVector(DependencyInstanceJoint inst, 
    		Object[] obj, 
    		ParseForestItemJoint item, 
    		boolean added) {
    	
    	WordPos[][] leftWordPoses = (WordPos[][]) obj[1];
    	WordPos[][] rightWordPoses = (WordPos[][]) obj[2];
    	WordPos[] leftWordPos = (WordPos[]) obj[3];
    	WordPos[] rightWordPos = (WordPos[]) obj[4];    	
    	int length = rightWordPoses.length;   
    	
    	
    	//找到最优parse
    	String parse = item.getParse();
    	String[] subs = parse.split(" ");
    	int i = 0;
    	ArrayList<WordPos> allWP = new ArrayList<WordPos>();
    	WordPos preWP = null;
    	for(String sub : subs) {
    		if(sub.length() > 1) {
    			if(i> 1 && i % 2 == 0){    				
    				String[] parts = sub.split(":");
    				WordPos wp = new WordPos(parts[2], parts[3], Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    				if(preWP == null || wp.getStart() != preWP.getStart()) {
    					allWP.add(wp);
    					preWP = wp;
    				}
    			}    
    			i++;
    		}
    	}
    	//
    	String dep = item.getDep();
    	//System.out.println(dep);
    	FeatureVector fv = new FeatureVector();
    	String[] res = dep.split(" ");
    	
    	//
    	for(WordPos wp: allWP) {    
			String match1 = "s:"+wp.getStart()+":.*:dir:1.*";
    		String match2 = ".*t:"+wp.getEnd()+":dir:0.*";
		    for(String oneDep : res) {
		    	if(oneDep.matches(match1) || oneDep.matches(match2)){
			    	String[] trip = oneDep.split(":");
			    	int s = Integer.parseInt(trip[1]);
			    	int t = Integer.parseInt(trip[3]);
			    	int dir = Integer.parseInt(trip[5]);
			    	//int comp = Integer.parseInt(trip[7]);
			    	int leftK = Integer.parseInt(trip[9]);
			    	int rightK = Integer.parseInt(trip[11]);
			    	WordPos left = rightWordPoses[s][leftK];
			    	WordPos right = leftWordPoses[t][rightK];
			    	//fv.add(pipe.createDependencyFeature(inst, length, leftWordPoses, rightWordPoses, leftWordPos, rightWordPos, dir, left, right, added));
			    	break;
		    	}
		    }
    	}
    	
    	
    	return fv;
    	
    }
        
    
    public FeatureVector getBestFeatureVector(DependencyInstanceJoint inst,
    		HashMap<WordPos, Integer> allWordPos, 
    		WordPos[][] leftWordPoses, 
    		WordPos[][] rightWordPoses,
    		WordPos[] leftWordPos,
    		WordPos[] rightWordPos,    	
    		
    		ArrayList<ArrayList<ArrayList<Integer>>> linkItems,
    		ParseForestItemJoint item, 
    		boolean added) {
    	   	
    	int length = rightWordPoses.length;   
    	
    	
    	//找到最优parse
    	String parse = item.getParse();
    	String[] subs = parse.split(" ");
    	int i = 0;
    	ArrayList<WordPos> allWP = new ArrayList<WordPos>();
    	WordPos preWP = null;
    	for(String sub : subs) {
    		if(sub.length() > 1) {
    			if(i> 1 && i % 2 == 0){    				
    				String[] parts = sub.split(":");
    				WordPos wp = new WordPos(parts[2], parts[3], Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    				if(preWP == null || wp.getStart() != preWP.getStart()) {
    					allWP.add(wp);
    					preWP = wp;
    				}
    			}    
    			i++;
    		}
    	}
    	//
    	String dep = item.getDep();
    	//System.out.println(dep);
    	FeatureVector fv = new FeatureVector();
    	String[] res = dep.split(" ");
    	
    	//
    	for(WordPos wp: allWP) {    
			String match1 = "s:"+wp.getStart()+":.*:dir:1.*";
    		String match2 = ".*t:"+wp.getEnd()+":dir:0.*";
		    for(String oneDep : res) {
		    	if(oneDep.matches(match1) || oneDep.matches(match2)){
			    	String[] trip = oneDep.split(":");
			    	int s = Integer.parseInt(trip[1]);
			    	int t = Integer.parseInt(trip[3]);
			    	int dir = Integer.parseInt(trip[5]);
			    	//int comp = Integer.parseInt(trip[7]);
			    	int leftK = Integer.parseInt(trip[9]);
			    	int rightK = Integer.parseInt(trip[11]);
			    	WordPos left = rightWordPoses[s][leftK];
			    	WordPos right = leftWordPoses[t][rightK];
			    	//fv.add(pipe.createDependencyFeature(inst, length, leftWordPoses, rightWordPoses, wordPoses, linkItems, dir, left, right, added));
			    	break;
		    	}
		    }
    	}
    	
    	
    	return fv;
    	
    }
    
    
    
    

    public Object[][] decodeNonProjective(DependencyInstanceJoint inst,
					  FeatureVector[][][] fvs,
					  double[][][] probs,
					  FeatureVector[][][][] nt_fvs,
					  double[][][][] nt_probs, int K) {

	String[] pos = inst.postags;

	int numWords = inst.length();

	int[][] oldI = new int[numWords][numWords];
	int[][] oldO = new int[numWords][numWords];
	double[][] scoreMatrix = new double[numWords][numWords];
	double[][] orig_scoreMatrix = new double[numWords][numWords];
	boolean[] curr_nodes = new boolean[numWords];
	TIntIntHashMap[] reps = new TIntIntHashMap[numWords];

	int[][] static_types = null;
	if(pipe.labeled) {
	    static_types = getTypes(nt_probs,pos.length);
	}

	for(int i = 0; i < numWords; i++) {
	    curr_nodes[i] = true;
	    reps[i] = new TIntIntHashMap();
	    reps[i].put(i,0);
	    for(int j = 0; j < numWords; j++) {
		// score of edge (i,j) i --> j
		scoreMatrix[i][j] = probs[i < j ? i : j][i < j ? j : i][i < j ? 0 : 1]
		    + (pipe.labeled ? nt_probs[i][static_types[i][j]][i < j ? 0 : 1][1]
		       + nt_probs[j][static_types[i][j]][i < j ? 0 : 1][0]
		       : 0.0);
		orig_scoreMatrix[i][j] = probs[i < j ? i : j][i < j ? j : i][i < j ? 0 : 1]
		    + (pipe.labeled ? nt_probs[i][static_types[i][j]][i < j ? 0 : 1][1]
		       + nt_probs[j][static_types[i][j]][i < j ? 0 : 1][0]
		       : 0.0);
		oldI[i][j] = i;
		oldO[i][j] = j;
		if(i == j || j == 0) continue; // no self loops of i --> 0
	    }
	}

	TIntIntHashMap final_edges = chuLiuEdmonds(scoreMatrix,curr_nodes,oldI,oldO,false,new TIntIntHashMap(),reps);
	int[] par = new int[numWords];
	int[] ns = final_edges.keys();
	for(int i = 0; i < ns.length; i++) {
	    int ch = ns[i]; int pr = final_edges.get(ns[i]);
	    par[ch] = pr;
	}

	int[] n_par = getKChanges(par,orig_scoreMatrix,Math.min(K,par.length));
	int new_k = 1;
	for(int i = 0; i < n_par.length; i++)
	    if(n_par[i] > -1) new_k++;

	// Create Feature Vectors;
	int[][] fin_par = new int[new_k][numWords];
	FeatureVector[][] fin_fv = new FeatureVector[new_k][numWords];
	fin_par[0] = par;
	int c = 1;
	for(int i = 0; i < n_par.length; i++) {
	    if(n_par[i] > -1) {
		int[] t_par = new int[par.length];
		for(int j = 0; j < t_par.length; j++)
		    t_par[j] = par[j];
		t_par[i] = n_par[i];
		fin_par[c] = t_par;
		c++;
	    }
	}
	for(int k = 0; k < fin_par.length; k++) {
	    for(int i = 0; i < fin_par[k].length; i++) {
		int ch = i; int pr = fin_par[k][i];
		if(pr != -1) {
		    fin_fv[k][ch] = fvs[ch < pr ? ch : pr][ch < pr ? pr : ch][ch < pr ? 1 : 0];
		    if(pipe.labeled) {
			fin_fv[k][ch] =
			    fin_fv[k][ch].cat(nt_fvs[ch][static_types[pr][ch]][ch < pr ? 1 : 0][0]);
			fin_fv[k][ch] =
			    fin_fv[k][ch].cat(nt_fvs[pr][static_types[pr][ch]][ch < pr ? 1 : 0][1]);
		    }
		}
		else
		    fin_fv[k][ch] = new FeatureVector();
	    }
	}


	FeatureVector[] fin = new FeatureVector[new_k];
	String[] result = new String[new_k];
	for(int k = 0; k < fin.length; k++) {
	    fin[k] = new FeatureVector();
	    for(int i = 1; i < fin_fv[k].length; i++)
		fin[k] = fin_fv[k][i].cat(fin[k]);
	    result[k] = "";
	    for(int i = 1; i < par.length; i++)
		result[k] += fin_par[k][i]+"|"+i + (pipe.labeled ? ":"+static_types[fin_par[k][i]][i] : ":0") +" ";
	}

	// create d.
	Object[][] d = new Object[new_k][2];

	for(int k = 0; k < new_k; k++) {
	    d[k][0] = fin[k];
	    d[k][1] = result[k].trim();
	}

	return d;
    }

    private int[] getKChanges(int[] par, double[][] scoreMatrix, int K) {
	int[] result = new int[par.length];
	int[] n_par = new int[par.length];
	double[] n_score = new double[par.length];
	for(int i = 0; i < par.length; i++) {
	    result[i] = -1;
	    n_par[i] = -1;
	    n_score[i] = Double.NEGATIVE_INFINITY;
	}

	boolean[][] isChild = calcChilds(par);

	for(int i = 1; i < n_par.length; i++) {
	    double max = Double.NEGATIVE_INFINITY;
	    int wh = -1;
	    for(int j = 0; j < n_par.length; j++) {
		if(i == j || par[i] == j || isChild[i][j]) continue;
		if(scoreMatrix[j][i] > max) { max = scoreMatrix[j][i]; wh = j; }
	    }
	    n_par[i] = wh;
	    n_score[i] = max;
	}

	for(int k = 0; k < K; k++) {
	    double max = Double.NEGATIVE_INFINITY;
	    int wh = -1;
	    int whI = -1;
	    for(int i = 0; i < n_par.length; i++) {
		if(n_par[i] == -1) continue;
		double score = scoreMatrix[n_par[i]][i];
		if(score > max) {
		    max = score; whI = i; wh = n_par[i];
		}
	    }

	    if(max == Double.NEGATIVE_INFINITY)
		break;
	    result[whI] = wh;
	    n_par[whI] = -1;
	}

	return result;
    }

    private boolean[][] calcChilds(int[] par) {
	boolean[][] isChild = new boolean[par.length][par.length];
	for(int i = 1; i < par.length; i++) {
	    int l = par[i];
	    while(l != -1) {
		isChild[l][i] = true;
		l = par[l];
	    }
	}
	return isChild;
    }

    private static TIntIntHashMap chuLiuEdmonds(double[][] scoreMatrix, boolean[] curr_nodes,
						int[][] oldI, int[][] oldO, boolean print,
						TIntIntHashMap final_edges, TIntIntHashMap[] reps) {

	// need to construct for each node list of nodes they represent (here only!)

	int[] par = new int[curr_nodes.length];
	int numWords = curr_nodes.length;

	// create best graph
	par[0] = -1;
	for(int i = 1; i < par.length; i++) {
	    // only interested in current nodes
	    if(!curr_nodes[i]) continue;
	    double maxScore = scoreMatrix[0][i];
	    par[i] = 0;
	    for(int j = 0; j < par.length; j++) {
		if(j == i) continue;
		if(!curr_nodes[j]) continue;
		double newScore = scoreMatrix[j][i];
		if(newScore > maxScore) {
		    maxScore = newScore;
		    par[i] = j;
		}
	    }
	}

	if(print) {
	    System.out.println("After init");
	    for(int i = 0; i < par.length; i++) {
		if(curr_nodes[i])
		    System.out.print(par[i] + "|" + i + " ");
	    }
	    System.out.println();
	}

	//Find a cycle
	ArrayList cycles = new ArrayList();
	boolean[] added = new boolean[numWords];
	for(int i = 0; i < numWords && cycles.size() == 0; i++) {
	    // if I have already considered this or
	    // This is not a valid node (i.e. has been contracted)
	    if(added[i] || !curr_nodes[i]) continue;
	    added[i] = true;
	    TIntIntHashMap cycle = new TIntIntHashMap();
	    cycle.put(i,0);
	    int l = i;
	    while(true) {
		if(par[l] == -1) {
		    added[l] = true;
		    break;
		}
		if(cycle.contains(par[l])) {
		    cycle = new TIntIntHashMap();
		    int lorg = par[l];
		    cycle.put(lorg,par[lorg]);
		    added[lorg] = true;
		    int l1 = par[lorg];
		    while(l1 != lorg) {
			cycle.put(l1,par[l1]);
			added[l1] = true;
			l1 = par[l1];

		    }
		    cycles.add(cycle);
		    break;
		}
		cycle.put(l,0);
		l = par[l];
		if(added[l] && !cycle.contains(l))
		    break;
		added[l] = true;
	    }
	}

	// get all edges and return them
	if(cycles.size() == 0) {
	    //System.out.println("TREE:");
	    for(int i = 0; i < par.length; i++) {
		if(!curr_nodes[i]) continue;
		if(par[i] != -1) {
		    int pr = oldI[par[i]][i];
		    int ch = oldO[par[i]][i];
		    final_edges.put(ch,pr);
		    //System.out.print(pr+"|"+ch + " ");
		}
		else
		    final_edges.put(0,-1);
	    }
	    //System.out.println();
	    return final_edges;
	}

	int max_cyc = 0;
	int wh_cyc = 0;
	for(int i = 0; i < cycles.size(); i++) {
	    TIntIntHashMap cycle = (TIntIntHashMap)cycles.get(i);
	    if(cycle.size() > max_cyc) { max_cyc = cycle.size(); wh_cyc = i; }
	}

	TIntIntHashMap cycle = (TIntIntHashMap)cycles.get(wh_cyc);
	int[] cyc_nodes = cycle.keys();
	int rep = cyc_nodes[0];

	if(print) {
	    System.out.println("Found Cycle");
	    for(int i = 0; i < cyc_nodes.length; i++)
		System.out.print(cyc_nodes[i] + " ");
	    System.out.println();
	}

	double cyc_weight = 0.0;
	for(int j = 0; j < cyc_nodes.length; j++) {
	    cyc_weight += scoreMatrix[par[cyc_nodes[j]]][cyc_nodes[j]];
	}


	for(int i = 0; i < numWords; i++) {

	    if(!curr_nodes[i] || cycle.contains(i)) continue;


	    double max1 = Double.NEGATIVE_INFINITY;
	    int wh1 = -1;
	    double max2 = Double.NEGATIVE_INFINITY;
	    int wh2 = -1;

	    for(int j = 0; j < cyc_nodes.length; j++) {
		int j1 = cyc_nodes[j];

		if(scoreMatrix[j1][i] > max1) {
		    max1 = scoreMatrix[j1][i];
		    wh1 = j1;//oldI[j1][i];
		}

		// cycle weight + new edge - removal of old
		double scr = cyc_weight + scoreMatrix[i][j1] - scoreMatrix[par[j1]][j1];
		if(scr > max2) {
		    max2 = scr;
		    wh2 = j1;//oldO[i][j1];
		}
	    }

	    scoreMatrix[rep][i] = max1;
	    oldI[rep][i] = oldI[wh1][i];//wh1;
	    oldO[rep][i] = oldO[wh1][i];//oldO[wh1][i];
	    scoreMatrix[i][rep] = max2;
	    oldO[i][rep] = oldO[i][wh2];//wh2;
	    oldI[i][rep] = oldI[i][wh2];//oldI[i][wh2];

	}

	TIntIntHashMap[] rep_cons = new TIntIntHashMap[cyc_nodes.length];
	for(int i = 0; i < cyc_nodes.length; i++) {
	    rep_cons[i] = new TIntIntHashMap();
	    int[] keys = reps[cyc_nodes[i]].keys();
	    Arrays.sort(keys);
	    if(print) System.out.print(cyc_nodes[i] + ": ");
	    for(int j = 0; j < keys.length; j++) {
		rep_cons[i].put(keys[j],0);
		if(print) System.out.print(keys[j] + " ");
	    }
	    if(print) System.out.println();
	}

	// don't consider not representative nodes
	// these nodes have been folded
	for(int i = 1; i < cyc_nodes.length; i++) {
	    curr_nodes[cyc_nodes[i]] = false;
	    int[] keys = reps[cyc_nodes[i]].keys();
	    for(int j = 0; j < keys.length; j++)
		reps[rep].put(keys[j],0);
	}

	chuLiuEdmonds(scoreMatrix,curr_nodes,oldI,oldO,print,final_edges,reps);

	// check each node in cycle, if one of its representatives
	// is a key in the final_edges, it is the one.
	int wh = -1;
	boolean found = false;
	for(int i = 0; i < rep_cons.length && !found; i++) {
	    int[] keys = rep_cons[i].keys();
	    for(int j = 0; j < keys.length && !found; j++) {
		if(final_edges.contains(keys[j])) {
		    wh = cyc_nodes[i];
		    found = true;
		}
	    }
	}

	int l = par[wh];
	while(l != wh) {
	    int ch = oldO[par[l]][l];
	    int pr = oldI[par[l]][l];
	    final_edges.put(ch,pr);
	    l = par[l];
	}

	if(print) {
	    int[] keys = final_edges.keys();
	    Arrays.sort(keys);
	    for(int i = 0; i < keys.length; i++)
		System.out.print(final_edges.get(keys[i])+"|"+keys[i]+" ");
	    System.out.println();
	}

	return final_edges;

    }



}
