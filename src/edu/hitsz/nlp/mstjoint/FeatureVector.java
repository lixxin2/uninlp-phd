package edu.hitsz.nlp.mstjoint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.util.Array;


public class FeatureVector implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Integer> fv;
	public ArrayList<String> sfv;

	public FeatureVector(){
		fv = new ArrayList<Integer>();
		sfv = new ArrayList<String>();
	}

    public FeatureVector (int[] keys) {
    	this();
		for (int i=0; i<keys.length; i++)
		    fv.add(keys[i]);
    }
    
    public FeatureVector (String[] keys) {
    	this();
		for (int i=0; i<keys.length; i++)
		    sfv.add(keys[i]);
    }
    
    
    public FeatureVector(ArrayList<Integer> fv, ArrayList<String> sfv) {
    	this.fv = fv;
    	this.sfv = sfv;
    }
    

    	

    public void add(int index) {
    	fv.add(index);
    }
    
    public void addString(String index) {
    	sfv.add(index);
    }

    public int[] ints(){
    	if(this.fv != null)
    		return Array.toArray(this.fv);
    	else
    		return null;
    }
    
    public String[] strings(){
    	return Array.toStringArray(this.sfv);
    }

    public double getScore(Parameters param){
    	double score = 0.0;
    	for(Integer v : fv){
    		score += param.getSingleWeight(v);
    	}
    	return score;
    }
    
    
    public void writeObject(ObjectOutputStream out) throws IOException{
    	out.writeObject(fv);
    }

    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
    	fv = (ArrayList<Integer>) in.readObject();
    }

    /*
    public void cat(FeatureVector fv2){
    	fv.addAll(fv2.fv);
    	sfv.addAll(fv2.sfv);    	
    }
    */
    
    public FeatureVector cat(FeatureVector fv2){
    	fv2.fv.addAll(this.fv);
    	fv2.sfv.addAll(this.sfv);
    	return new FeatureVector(fv2.fv, fv2.sfv);
    }
    
    public void add(FeatureVector fv2) {
    	fv.addAll(fv2.fv);
    	sfv.addAll(fv2.sfv);
    }
    
    /**
     * 将
     * @since 2012-3-2
     * @param pipe
     */
    public void sfv2fv(DependencyPipeJoint pipe){
    	fv.clear();
    	for(String f : sfv) {
    		fv.add(pipe.dataAlphabet.add(f));
    	}    	
    }
    
    public void sfv2fvPart(DependencyPipeJoint pipe){
    	fv.clear();
    	for(String f : sfv) {
    		int i = pipe.dataAlphabet.get(f);
    		if(i >=0 )
    			fv.add(i);
    	}    	
    }
    




}
