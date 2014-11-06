package edu.hitsz.nlp.partofspeech;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.util.Array;


public class POSFeatureVector implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Integer> fv;
	public ArrayList<String> sfv;

	public POSFeatureVector(){
		fv = new ArrayList<Integer>();
		sfv = new ArrayList<String>();
	}

    public POSFeatureVector (int[] keys) {
    	this();
		for (int i=0; i<keys.length; i++)
		    fv.add(keys[i]);
    }

    public void add(int index) {
    	fv.add(index);
    }
    
    public void addString(String index) {
    	sfv.add(index);
    }

    public int[] ints(){
    	return Array.toArray(this.fv);
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

    public void cat(POSFeatureVector fv2){
    	fv.addAll(fv2.fv);
    	sfv.addAll(fv2.sfv);
    }
    
    /**
     * 将
     * @since 2012-3-2
     * @param pipe
     */
    public void sfv2fv(POSPipe pipe){
    	fv.clear();
    	for(String f : sfv) {
    		fv.add(pipe.dataMap.add(f));
    	}    	
    }
    

}

