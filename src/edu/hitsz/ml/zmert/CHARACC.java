package edu.hitsz.ml.zmert;

import java.util.ArrayList;

/**
 * 字的准确率，用于汉语音转字
 * @author Xinxin Li
 * @since Oct 21, 2013
 */
public class CHARACC extends EvaluationMetric {

	public CHARACC() {
		initialize();
	}
	
	public CHARACC(String[] ZOL_options) {
	    this();
	}
	
	protected void initialize() {
		metricName = "PY2CHAR";
	    toBeMinimized = true;
	    suffStatsCount =  2;
		
	}

	public double bestPossibleScore() { return 1.0; }
	public double worstPossibleScore() { return 0.0; }

	/**
	 * 统计候选结果的准确性
	 */
	public int[] suffStats(String cand_str, int i) {
	    
		int[] stats = new int[suffStatsCount];

	    for (int r = 0; r < refsPerSen; ++r) {
	        ArrayList<String> refChars = splitChars(refSentences[i][r]);
	        ArrayList<String> candChars = splitChars(cand_str);
	        int length = refChars.size();
	        if(length != candChars.size()) {
	        	System.out.println("cand and ref sentence length are different");
	        	System.out.println("ref:  " + refSentences[i][r]);
	        	System.out.println("cand: " + cand_str);
	        	System.exit(-1);
	        }
	        int corr = 0;
	        for(int j=0; j<length; j++) {
	        	if(refChars.get(j).equals(candChars.get(j)))
	        		corr++;
	        }
	        stats[0] = corr;
	        stats[1] = length;	 
	    }
	    
	    return stats;

	}

	/**
	 * 得到当前候选集的分值,1-准确率
	 */
	public double score(int[] stats) {
	    if (stats.length != suffStatsCount) {
	      System.out.println("Mismatch between stats.length and suffStatsCount (" + stats.length + " vs. " + suffStatsCount + ") in ZeroOneLoss.score(int[])");
	      System.exit(1);
	    }
	    return 1-stats[0]/(double)stats[1];
	}
	
	

	public void printDetailedScore_fromStats(int[] stats, boolean oneLiner) {
	    if (oneLiner) {
	      System.out.println("PY2CHAR = " + stats[0] + "/" + stats[1] + " = " + f4.format(stats[0]/(double)stats[1]));
	    } else {
	      System.out.println("# correct = " + stats[0]);
	      System.out.println("# sentences = " + stats[1]);
	      System.out.println("PY2CHAR = " + stats[0] + "/" + stats[1] + " = " + f4.format(stats[0]/(double)stats[1]));
	    }
	}
	
	/** 得到句子中的每一个字*/
	private ArrayList<String> splitChars(String s) {
		ArrayList<String> chars = new ArrayList<String>();
		String[] subs = s.split("[ \t]");
		for(String sub : subs) {
			if(sub.length() > 0) {
				for(int i=0; i<sub.length(); i++)
					chars.add(sub.substring(i, i+1));
			}
		}
		return chars;
		
	}
	
	
}
