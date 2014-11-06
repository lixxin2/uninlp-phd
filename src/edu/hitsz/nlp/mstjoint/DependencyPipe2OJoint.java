package edu.hitsz.nlp.mstjoint;

import java.io.*;
import java.util.HashMap;

import edu.hitsz.ml.onlinelearning.ap.Parameters;

/**
 * second-order dependency pipe
 * @author tm
 *
 */
public class DependencyPipe2OJoint extends DependencyPipeJoint {

    public DependencyPipe2OJoint(ParserOptionsJoint options) throws IOException {
    	super(options);
    }


    protected void addExtendedFeatures(DependencyInstanceJoint instance,
				       FeatureVector fv, boolean added) {

		final int instanceLength = instance.length();
		int[] heads = instance.heads;

		// find all trip features
		for(int i = 0; i < instanceLength; i++) {
		    if(heads[i] == -1 && i != 0) continue;
		    // right children
		    int prev = i;
		    for(int j = i+1; j < instanceLength; j++) {
				if(heads[j] == i) {
				    addTripFeatures(instance,i,prev,j,fv, added);
				    addSiblingFeatures(instance,prev,j,prev==i,fv, added);
				    prev = j;
				}
		    }
		    prev = i;
		    for(int j = i-1; j >= 0; j--) {
				if(heads[j] == i) {
				    addTripFeatures(instance,i,prev,j,fv, added);
				    addSiblingFeatures(instance,prev,j,prev==i,fv, added);
				    prev = j;
				}
		    }
		}
    }

    public void fillFeatureVectors(DependencyInstanceJoint instance,
				   FeatureVector[][][] fvs,
				   double[][][] probs,
				   FeatureVector[][][] fvs_trips,
				   double[][][] probs_trips,
				   FeatureVector[][][] fvs_sibs,
				   double[][][] probs_sibs,
				   FeatureVector[][][][] nt_fvs,
				   double[][][][] nt_probs, 
				   Parameters params) {

		fillFeatureVectors(instance, fvs, probs, nt_fvs, nt_probs, params);

		final int instanceLength = instance.length();

		for(int w1 = 0; w1 < instanceLength; w1++) {
		    for(int w2 = w1; w2 < instanceLength; w2++) {
				for(int w3 = w2+1; w3 < instanceLength; w3++) {
				    FeatureVector prodFV = new FeatureVector();
				    addTripFeatures(instance,w1,w2,w3,prodFV, false);
				    double prodProb = prodFV.getScore(params);
				    fvs_trips[w1][w2][w3] = prodFV;
				    probs_trips[w1][w2][w3] = prodProb;
				}
		    }
		    for(int w2 = w1; w2 >= 0; w2--) {
				for(int w3 = w2-1; w3 >= 0; w3--) {
				    FeatureVector prodFV = new FeatureVector();
				    addTripFeatures(instance,w1,w2,w3,prodFV, false);
				    double prodProb = prodFV.getScore(params);
				    fvs_trips[w1][w2][w3] = prodFV;
				    probs_trips[w1][w2][w3] = prodProb;
				}
		    }
		}

		for(int w1 = 0; w1 < instanceLength; w1++) {
		    for(int w2 = 0; w2 < instanceLength; w2++) {
				for(int wh = 0; wh < 2; wh++) {
				    if(w1 != w2) {
					FeatureVector prodFV = new FeatureVector();
					addSiblingFeatures(instance,w1,w2,wh == 0,prodFV, false);
					double prodProb = prodFV.getScore(params);
					fvs_sibs[w1][w2][wh] = prodFV;
					probs_sibs[w1][w2][wh] = prodProb;
				    }
				}
		    }
		}
    }


    /**
     * 兄弟节点特征
     * @since Apr 5, 2012
     * @param instance
     * @param ch1
     * @param ch2
     * @param isST 第一个节点是否是头结点
     * @param fv
     */
    private final void addSiblingFeatures(DependencyInstanceJoint instance,
					  int ch1, int ch2,
					  boolean isST,
					  FeatureVector fv,
					  boolean added) {

		String[] forms = instance.forms;
		String[] pos = instance.postags;

		// ch1 is always the closes to par
		String dir = ch1 > ch2 ? "RA" : "LA";

		String ch1_pos = isST ? "STPOS" : pos[ch1];
		String ch2_pos = pos[ch2];
		String ch1_word = isST ? "STWRD" : forms[ch1];
		String ch2_word = forms[ch2];

		add("CH_PAIR="+ch1_pos+"_"+ch2_pos+"_"+dir, fv, added);
		add("CH_WPAIR="+ch1_word+"_"+ch2_word+"_"+dir,fv, added);
		add("CH_WPAIRA="+ch1_word+"_"+ch2_pos+"_"+dir, fv, added);
		add("CH_WPAIRB="+ch1_pos+"_"+ch2_word+"_"+dir, fv, added);
		add("ACH_PAIR="+ch1_pos+"_"+ch2_pos, fv, added);
		add("ACH_WPAIR="+ch1_word+"_"+ch2_word, fv, added);
		add("ACH_WPAIRA="+ch1_word+"_"+ch2_pos, fv, added);
		add("ACH_WPAIRB="+ch1_pos+"_"+ch2_word, fv, added);

		int dist = Math.max(ch1,ch2)-Math.min(ch1,ch2);
		String distBool = "0";
		if(dist > 1)
		    distBool = "1";
		if(dist > 2)
		    distBool = "2";
		if(dist > 3)
		    distBool = "3";
		if(dist > 4)
		    distBool = "4";
		if(dist > 5)
		    distBool = "5";
		if(dist > 10)
		    distBool = "10";
		add("SIB_PAIR_DIST="+distBool+"_"+dir, fv, added);
		add("ASIB_PAIR_DIST="+distBool, fv, added);
		add("CH_PAIR_DIST="+ch1_pos+"_"+ch2_pos+"_"+distBool+"_"+dir, fv, added);
		add("ACH_PAIR_DIST="+ch1_pos+"_"+ch2_pos+"_"+distBool, fv, added);

    }


    /**
     * 三元组特征
     * @since Apr 5, 2012
     * @param instance
     * @param par 父节点
     * @param ch1 字节点1
     * @param ch2 字节点2
     * @param fv
     * @param added
     */
    private final void addTripFeatures(DependencyInstanceJoint instance,
				       int par,
				       int ch1, int ch2,
				       FeatureVector fv,
				       boolean added) {

		String[] pos = instance.postags;

		// ch1 is always the closest to par
		String dir = par > ch2 ? "RA" : "LA";

		String par_pos = pos[par];
		String ch1_pos = ch1 == par ? "STPOS" : pos[ch1];
		String ch2_pos = pos[ch2];

		String pTrip = par_pos+"_"+ch1_pos+"_"+ch2_pos;
		add("POS_TRIP="+pTrip+"_"+dir, fv, added);
		add("APOS_TRIP="+pTrip, fv, added);

    }



    /**
     * Write out the second order features.
     *
     **/
    protected void writeExtendedFeatures (DependencyInstanceJoint instance, ObjectOutputStream out)
	throws IOException {

		final int instanceLength = instance.length();

		for(int w1 = 0; w1 < instanceLength; w1++) {
		    for(int w2 = w1; w2 < instanceLength; w2++) {
			for(int w3 = w2+1; w3 < instanceLength; w3++) {
			    FeatureVector prodFV = new FeatureVector();
			    addTripFeatures(instance,w1,w2,w3,prodFV, true);
			    out.writeObject(prodFV.ints());
			}
		    }
		    for(int w2 = w1; w2 >= 0; w2--) {
			for(int w3 = w2-1; w3 >= 0; w3--) {
			    FeatureVector prodFV = new FeatureVector();
			    addTripFeatures(instance,w1,w2,w3,prodFV, true);
			    out.writeObject(prodFV.ints());
			}
		    }
		}

		out.writeInt(-3);

		for(int w1 = 0; w1 < instanceLength; w1++) {
		    for(int w2 = 0; w2 < instanceLength; w2++) {
				for(int wh = 0; wh < 2; wh++) {
				    if(w1 != w2) {
					FeatureVector prodFV = new FeatureVector();
					addSiblingFeatures(instance,w1,w2,wh == 0,prodFV, true);
					out.writeObject(prodFV.ints());
				    }
				}
		    }
		}

		out.writeInt(-3);
    }


    public DependencyInstanceJoint readInstance(ObjectInputStream in,
					   int length,
					   FeatureVector[][][] fvs,
					   double[][][] probs,
					   FeatureVector[][][] fvs_trips,
					   double[][][] probs_trips,
					   FeatureVector[][][] fvs_sibs,
					   double[][][] probs_sibs,
					   FeatureVector[][][][] nt_fvs,
					   double[][][][] nt_probs,
					   Parameters params) throws IOException {

		try {
		    // Get production crap.
		    for(int w1 = 0; w1 < length; w1++) {
			for(int w2 = w1+1; w2 < length; w2++) {
			    for(int ph = 0; ph < 2; ph++) {
				FeatureVector prodFV = new FeatureVector((int[])in.readObject());
				double prodProb = prodFV.getScore(params);
				fvs[w1][w2][ph] = prodFV;
				probs[w1][w2][ph] = prodProb;
			    }
			}
		    }
		    int last = in.readInt();
		    if(last != -3) { System.out.println("Error reading file."); System.exit(0); }

		    if(labeled) {
			for(int w1 = 0; w1 < length; w1++) {
			    for(int t = 0; t < types.length; t++) {
				String type = types[t];
				for(int ph = 0; ph < 2; ph++) {
				    for(int ch = 0; ch < 2; ch++) {
					FeatureVector prodFV = new FeatureVector((int[])in.readObject());
					double nt_prob = prodFV.getScore(params);
					nt_fvs[w1][t][ph][ch] = prodFV;
					nt_probs[w1][t][ph][ch] = nt_prob;
				    }
				}
			    }
			}
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
		    }

		    for(int w1 = 0; w1 < length; w1++) {
			for(int w2 = w1; w2 < length; w2++) {
			    for(int w3 = w2+1; w3 < length; w3++) {
				FeatureVector prodFV = new FeatureVector((int[])in.readObject());
				double prodProb = prodFV.getScore(params);
				fvs_trips[w1][w2][w3] = prodFV;
				probs_trips[w1][w2][w3] = prodProb;
			    }
			}
			for(int w2 = w1; w2 >= 0; w2--) {
			    for(int w3 = w2-1; w3 >= 0; w3--) {
				FeatureVector prodFV = new FeatureVector((int[])in.readObject());
				double prodProb = prodFV.getScore(params);
				fvs_trips[w1][w2][w3] = prodFV;
				probs_trips[w1][w2][w3] = prodProb;
			    }
			}
		    }
		    last = in.readInt();
		    if(last != -3) { System.out.println("Error reading file."); System.exit(0); }

		    for(int w1 = 0; w1 < length; w1++) {
			for(int w2 = 0; w2 < length; w2++) {
			    for(int wh = 0; wh < 2; wh++) {
				if(w1 != w2) {
				    FeatureVector prodFV = new FeatureVector((int[])in.readObject());
				    double prodProb = prodFV.getScore(params);
				    fvs_sibs[w1][w2][wh] = prodFV;
				    probs_sibs[w1][w2][wh] = prodProb;
				}
			    }
			}
		    }
		    last = in.readInt();
		    if(last != -3) { System.out.println("Error reading file."); System.exit(0); }

		    FeatureVector nfv = new FeatureVector((int[])in.readObject());
		    last = in.readInt();
		    if(last != -4) { System.out.println("Error reading file."); System.exit(0); }

		    DependencyInstanceJoint marshalledDI;
		    marshalledDI = (DependencyInstanceJoint)in.readObject();
		    marshalledDI.setFeatureVector(nfv);
		    last = in.readInt();
		    if(last != -1) { System.out.println("Error reading file."); System.exit(0); }

		    return marshalledDI;

		} catch(ClassNotFoundException e) {
		    System.out.println("Error reading file."); System.exit(0);
		}

		// this won't happen, but it takes care of compilation complaints
		return null;

    }
    
    
    
    public void getProbabilityTripJoint(DependencyInstanceJoint marshalledDI, int length,
    		HashMap<WordPos, Integer> allWordPos, 
    		WordPos[][] leftWordPoses, WordPos[][] rightWordPoses,
    		WordPos[] leftWordPos, WordPos[] rightWordPos, 
    		double [][][] tripProbs, Parameters params){
    	int lSize = rightWordPoses[0].length;
    	int rSize = leftWordPoses[0].length;
		//i 表示 父节点, 位于左边
		for(int i=0; i<length; i++) {
			//System.out.println("i "+i+",");
			for(int k=0; k<lSize; k++) {
				WordPos father = rightWordPoses[i][k];
				if(father != null) {
					// j 表示 左子节点				
					for(int j=i; j<length; j++){		
						//System.out.print("j "+j+",");
						for(int l=0; l<rSize; l++){
							WordPos leftSon = leftWordPoses[j][l];
							if(leftSon != null) { 									
								if(father.getEnd() < leftSon.getStart() ||
										(father.getStart() == leftSon.getStart() && father.getEnd() == leftSon.getEnd() && father.getPos().equals(leftSon.getPos()))){
									//表示右子节点
									for(int m=j+1; m<rSize; m++) {
										for(int n=0; n<rSize; n++) {
											WordPos rightSon = leftWordPoses[m][n];
											if(rightSon != null) {
												if(leftSon.getEnd() < rightSon.getStart()) {
													FeatureVector prodFV = new FeatureVector();
													createTripFeatures(father, leftSon, rightSon, prodFV, false);
													int fatherIndex = allWordPos.get(father);
													int leftSonIndex = allWordPos.get(leftSon);
													int rightSonIndex = allWordPos.get(rightSon);
													tripProbs[fatherIndex][leftSonIndex][rightSonIndex] = prodFV.getScore(params);
												}
											}
											else
												break;
										}										
									}
								}
							}
							else
								break;
						}							
					}
				}
				else
					break;				
			}
		}
		
		//i 表示 父节点
		for(int i=0; i<length; i++) {
			//System.out.println("i "+i+",");
			for(int k=0; k<rSize; k++) {
				WordPos father = leftWordPoses[i][k];
				if(father != null) {
					// j 表示 右边子节点				
					for(int j=i; j>=0; j--){		
						//System.out.print("j "+j+",");
						for(int l=0; l<lSize; l++){
							WordPos rightSon = rightWordPoses[j][l];
							if(rightSon != null) { 									
								if(rightSon.getEnd() < father.getStart() ||
										(rightSon.getStart() == father.getStart() && rightSon.getEnd() == father.getEnd() && rightSon.getPos().equals(father.getPos()))){
									for(int m=j-1; m>=0; m--) {
										for(int n=0; n<lSize; n++) {
											WordPos leftSon = rightWordPoses[m][n];
											if(leftSon != null) {
												if(leftSon.getEnd() < rightSon.getStart()) {
													FeatureVector prodFV = new FeatureVector();
													createTripFeatures(father, rightSon, leftSon, prodFV, false);
													int fatherIndex = allWordPos.get(father);
													int leftSonIndex = allWordPos.get(leftSon);
													int rightSonIndex = allWordPos.get(rightSon);
													tripProbs[fatherIndex][rightSonIndex][leftSonIndex] = prodFV.getScore(params);
												}
											}
											else
												break;
										}										
									}
								}
							}
							else
								break;
						}							
					}
				}
				else
					break;				
			}
		}    	
    	
    }
    
    
    
    /**
     * 三元组特征
     * @since Apr 5, 2012
     * @param instance
     * @param par 父节点
     * @param ch1 字节点1
     * @param ch2 字节点2
     * @param fv
     * @param added
     */
    private final void createTripFeatures(WordPos father, WordPos ch1, WordPos ch2,
    		FeatureVector fv, boolean added) {
				
		// ch1 is always the closest to par		    	   
		String dir = father.getStart() > ch2.getStart() ? "RA" : "LA";
				    	   
		String par_pos = father.getPos();
		String ch1_pos = father.getStart() == ch1.getStart() && father.getEnd() == ch1.getEnd()
				&& father.getPos().equals(ch1.getPos()) ? "STPOS" : ch1.getPos();
		String ch2_pos = ch2.getPos();

		String pTrip = par_pos+"_"+ch1_pos+"_"+ch2_pos;
		add("POS_TRIP="+pTrip+"_"+dir, fv, added);
		add("APOS_TRIP="+pTrip, fv, added);
		

    }
    
    
    public void getProbabilitySiblingJoint(DependencyInstanceJoint marshalledDI, int length,
    		HashMap<WordPos, Integer> allWordPos, 
    		WordPos[][] leftWordPoses, WordPos[][] rightWordPoses,
    		WordPos[] leftWordPos, WordPos[] rightWordPos, 
    		double [][][] siblingProbs, Parameters params){
    	
    	
    	
    	
    }
    
    
    

}

