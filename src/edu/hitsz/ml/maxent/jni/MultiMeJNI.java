package edu.hitsz.ml.maxent.jni;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiMeJNI {

	static {
		System.loadLibrary("MultiMeJNI");
	}
	
	public native void print();
			
	public native void begin_add_event(String strIndex);
	
	public native void add_event(String strIndex, String[] context, String outcome, int i);
	
	public void add_event(String strIndex, ArrayList<String> context, String outcome, int i){
		String[] contexts = getVec(context);
		add_event(strIndex, contexts, outcome, i);
	}
	
	public native void add_heldout_event(String strIndex, String[] context, String outcome, int i);
	
	public void add_heldout_event(String strIndex, ArrayList<String> context, String outcome, int i){
		String[] contexts = getVec(context);
		add_heldout_event(strIndex, contexts, outcome, i);
	}
	
	public native void end_add_event_all();
	
	public native void end_add_event_all(int cutoff);
	
	//public native void dump_events(String model, boolean binary);
	
	public native void train_all();
		
	public native void train_all(int iterNumber, String iterMethod);
	
	public native void train_all(int iterNumber, String iterMethod, double variance);
		
	public native void train_all(int iterNumber, String iterMethod, double variance, 
			double tolerance);
		
	public native void save_all(String modelPath);
	
	/**模型放到文件夹里面，所以首先需要建立文件夹*/
	public void save_all_path(String modelPath) {
		
	}
	
	public native void save(String model, boolean binary);
	
	public native void load_all(String modelPath);
	
	public native double eval(String strIndex, String[] context, String outcome);
	
	public double eval(String strIndex, ArrayList<String> context, String outcome) {
		String[] contexts = getVec(context);
		return eval(strIndex, contexts, outcome);
	}
	
	public native HashMap<String, Double> eval_all(String strIndex, String[] context);
	
	public HashMap<String, Double> eval_all(String strIndex, ArrayList<String> context){
		String[] contexts = getVec(context);
		return eval_all(strIndex, contexts);
	}
	
	public native HashMap<String, Double> eval_all(String strIndex, String[] context, boolean sort_result);
	
	public HashMap<String, Double> eval_all(String strIndex, ArrayList<String> context, boolean sort_result) {
		String[] contexts = getVec(context);
		return eval_all(strIndex, contexts, sort_result);
	}
	
	public native void eval_all(String strIndex, String[] context, String[] outcomes, double[] probs);
	
	public void eval_all(String strIndex, ArrayList<String> context, String[] outcomes, double[] probs) {
		String[] contexts = getVec(context);
		eval_all(strIndex, contexts, outcomes, probs);
	}
	
	public native void eval_all(String strIndex, String[] context, String[] outcomes, 
			double[] probs, boolean sort_result);
	
	public void eval_all(String strIndex, ArrayList<String> context, String[] outcomes, double[] probs, boolean sort_result) {
		String[] contexts = getVec(context);
		eval_all(strIndex, contexts, outcomes, probs, sort_result);
	}
	
	public native String predict(String strIndex, String[] context);
		
	public String predict(String strIndex, ArrayList<String> context){
		String[] contexts = getVec(context);
		return predict(strIndex, contexts);
	}
	
	public native int getClassNumber(String strIndex);
	
	private static String[] getVec(ArrayList<String> context){
		int number = context.size();
		String[] contexts = new String[number];
		for(int p=0; p<number; p++) {
			contexts[p] = context.get(p);
		}
		return contexts;
	}
	
	
	
	
}
