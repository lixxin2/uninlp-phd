package edu.hitsz.nlp.transDep;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.hitsz.nlp.util.Array;
import edu.hitsz.ml.onlinelearning.ap.Parameters;


public class DepFeatureVector implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Integer> fv;
	public ArrayList<String> sfv;

	public DepFeatureVector(){
		fv = new ArrayList<Integer>();
		sfv = new ArrayList<String>();
	}

    public DepFeatureVector (int[] keys) {
    	this();
		for (int i=0; i<keys.length; i++)
		    fv.add(keys[i]);
    }
    
    public DepFeatureVector (String[] keys) {
    	this();
		for (int i=0; i<keys.length; i++)
		    sfv.add(keys[i]);
    }
    
    
    public DepFeatureVector(ArrayList<Integer> fv, ArrayList<String> sfv) {
    	this.fv = fv;
    	this.sfv = sfv;
    }
    
    
    public DepFeatureVector(DepFeatureVector dfv) {
    	fv = new ArrayList<Integer>(dfv.fv);
    	sfv = new ArrayList<String>(dfv.sfv);
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
    
    public DepFeatureVector cat(DepFeatureVector fv2){
    	fv2.fv.addAll(this.fv);
    	fv2.sfv.addAll(this.sfv);
    	return new DepFeatureVector(fv2.fv, fv2.sfv);
    }
    
    public void add(DepFeatureVector fv2) {
    	fv.addAll(fv2.fv);
    	sfv.addAll(fv2.sfv);
    }
    
    /**
     * 将
     * @since 2012-3-2
     * @param pipe
     */
    public void sfv2fv(TransPipe pipe){
    	fv.clear();
    	for(String f : sfv) {
    		fv.add(pipe.dataMap.add(f));
    	}    	
    }
    
    public void sfv2fvPart(TransPipe pipe){
    	fv.clear();
    	for(String f : sfv) {
    		int i = pipe.dataMap.get(f);
    		if(i >=0 )
    			fv.add(i);
    	}    	
    }
    





}

