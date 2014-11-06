package edu.hitsz.ml.maxent.lindekang;

import java.util.ArrayList;

public class LogProbs {

	/**
	 * The input array contains a set of log probabilities lp1, lp2, lp3
     * ... The return value should be the log of the sum of the
     * probabilities: log(e^lp1 + e^lp2 + e^lp3 + ...)
     * 
     * <p> 输入值为一组log probabilities lp1, lp2, lp3, 输出为log(e^lp1 + e^lp2 + e^lp3 + ...)
     * 
	 * @param logprobs
	 * @return
	 */
	public static double sumLogProb (ArrayList<Double> logprobs){
		double max = 0;
		int i;
		for( i=0; i<logprobs.size(); i++){
			if( i==0 || logprobs.get(i) > max)
				max = logprobs.get(i);
		}
		if(Double.isInfinite(max)) // the largest probability is 0 (log prob= -inf)
			return max;            // return log 0
		double p = 0;
		// p = e^(lp1-lpx) + e^(lp2-lpx) + ... + e^(lpn-lpx)
		for( i=0; i<logprobs.size(); i++){
			p += Math.exp(logprobs.get(i)-max);
		}
		// lpx + log(p) = log(e^lpx) + log(p) = log(e^lpx * p) = log(e^lp1 + e^lp2 + ... + e^lpn)
		return max + Math.log(p);
		
	}

	/**
	 *  returns log (e^logprob1 + e^logprob2).
	 * @param logprob1
	 * @param logprob2
	 * @return
	 */
	public static double sumLogProb(double logprob1, double logprob2){
	  if (Double.isInfinite(logprob1) && Double.isInfinite(logprob2))
		    return logprob1; // both prob1 and prob2 are 0, return log 0.
		  if (logprob1>logprob2)
		    return logprob1+Math.log(1+Math.exp(logprob2-logprob1));
		  else
		    return logprob2+Math.log(1+Math.exp(logprob1-logprob2));
	}

	public static void main(String[] args){
		double a = sumLogProb(-10, -10);
		System.out.println(a);
	}

}
