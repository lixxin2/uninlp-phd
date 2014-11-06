/**
 * 
 */
package edu.hitsz.ml.maxent.jni;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author tm
 *
 */
public class MaximumEntropyJNI {
	
	
	static {
		System.loadLibrary("MaximumEntropyJNI");
	}
		
	public native void print();
		
	public native void begin_add_event();
	
	public native void add_event(String[] context, String outcome, int i);
	
	public void add_event(ArrayList<String> context, String outcome, int i){
		String[] contexts = getVec(context);
		add_event(contexts, outcome, i);
	}
	
	public native void add_heldout_event(String[] context, String outcome, int i);
	
	public void add_heldout_event(ArrayList<String> context, String outcome, int i){
		String[] contexts = getVec(context);
		add_heldout_event(contexts, outcome, i);
	}
	
	public native void end_add_event();
	
	public native void end_add_event(int cutoff);
	
	//public native void dump_events(String model, boolean binary);
	
	public native void train();
	
	public native void train(int iterNumber, String iterMethod);
	
	public native void train(int iterNumber, String iterMethod, double variance);
	
	public native void train(int iterNumber, String iterMethod, double variance, 
			double tolerance);
	
	public native void save(String model);
	
	public native void save(String model, boolean binary);
	
	public native void load(String model);
	
	public native double eval(String[] context, String outcome);
	
	public double eval(ArrayList<String> context, String outcome) {
		String[] contexts = getVec(context);
		return eval(contexts, outcome);
	}
	
	public native HashMap<String, Double> eval_all(String[] context);
	
	public HashMap<String, Double> eval_all(ArrayList<String> context){
		String[] contexts = getVec(context);
		return eval_all(contexts);
	}
	
	public native HashMap<String, Double> eval_all(String[] context, boolean sort_result);
	
	public HashMap<String, Double> eval_all(ArrayList<String> context, boolean sort_result) {
		String[] contexts = getVec(context);
		return eval_all(contexts, sort_result);
	}
	
	public native void eval_all(String[] context, String[] outcomes, double[] probs);
	
	public void eval_all(ArrayList<String> context, String[] outcomes, double[] probs) {
		String[] contexts = getVec(context);
		eval_all(contexts, outcomes, probs);
	}
	
	public native void eval_all(String[] context, String[] outcomes, double[] probs, boolean sort_result);
	
	public void eval_all(ArrayList<String> context, String[] outcomes, double[] probs, boolean sort_result) {
		String[] contexts = getVec(context);
		eval_all(contexts, outcomes, probs, sort_result);
	}
	
	public native String predict(String[] context);
	
	public String predict(ArrayList<String> context) {
		String[] contexts = getVec(context);
		return predict(contexts);
	}
	
	public native int getClassNumber();
	
	private static String[] getVec(ArrayList<String> context){
		int number = context.size();
		String[] contexts = new String[number];
		for(int p=0; p<number; p++) {
			contexts[p] = context.get(p);
		}
		return contexts;
	}
	
	
	

}
