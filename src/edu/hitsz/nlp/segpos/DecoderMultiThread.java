package edu.hitsz.nlp.segpos;

import java.util.concurrent.Callable;

import edu.hitsz.ml.onlinelearning.ap.Parameters;

public class DecoderMultiThread implements Callable<Instance>{
	
	private CharDecoder mcharDecoder;
	private WordDecoder mwordDecoder;
	private JointDecoder mjointDecoder;
	
	private Parameters mcharParams;
	private Parameters mwordParams;
	
	private Instance minst;
	private int mK;
	private double malpha;
	
	private String mtype;
	
	private Instance rinst;
	
	public DecoderMultiThread(CharDecoder decoder, 
			Parameters charParams,
			Instance inst,
			int K) {
		
		mcharDecoder = decoder;
		mcharParams = charParams;
		minst = inst;
		mK = K;
		mtype = "char";		
		
	}
	
	public DecoderMultiThread(WordDecoder decoder, 
			Parameters wordParams,
			Instance inst,
			int K) {
		
		mwordDecoder = decoder;
		mwordParams = wordParams;
		minst = inst;
		mK = K;
		mtype = "word";		
		
	}
	
	
	public DecoderMultiThread(JointDecoder decoder,
			Parameters charParams,
			Parameters wordParams,
			Instance inst,
			int K,
			double alpha) {
		
		mjointDecoder = decoder;
		mcharParams = charParams;
		mwordParams = wordParams;
		minst = inst;
		mK = K;
		malpha = alpha;
		mtype = "joint";		
		
	}
	
		
	public Instance call() throws Exception {
		if(mtype.equals("char"))
			rinst = mcharDecoder.getBestInstance(mcharParams, minst, mK);
		else if(mtype.equals("word"))
			rinst = mwordDecoder.getBestInstance(mwordParams, minst, mK);
		else if(mtype.equals("joint"))
			rinst = mjointDecoder.getBest(mcharParams, mwordParams, minst, mK, malpha);
		return rinst;
	}
	
	
	
	
}
