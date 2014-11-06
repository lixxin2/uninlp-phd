package edu.hitsz.ml.zmert;

import java.util.ArrayList;

import edu.hitsz.nlp.sentence.Distance;
import edu.hitsz.nlp.sentence.LevenshteinDistance;

/** 
 * 汉明距离，用于语音识别评价 */
public class MED extends EvaluationMetric {

	public MED() {
		initialize();
	}
	
	public MED(String[] ZOL_options) {
	    this();
	}
	
	protected void initialize() {
		metricName = "SpeechReg";
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
	        String refChars = refSentences[i][r];
	        int length = getCharNumber(refChars);
		    LevenshteinDistance dist = new LevenshteinDistance();
		    Distance newDist = dist.stringDistance(refChars, cand_str);
	        
	        stats[0] = Math.max(length - (newDist.deletion + newDist.insertion + newDist.substitution), 0);
	        stats[1] = length;	 
	    }
	    
	    return stats;
	}
	
	/**
	 * 得到句子中字的数目
	 * @since Oct 21, 2013
	 * @param str
	 * @return
	 */
	public int getCharNumber(String str) {
		
		int count = 0;
		for(int i=0; i<str.length(); i++) {
			String s = str.substring(i, i+1);
			if(s.trim().length() != 0)
				count++;
		}
		return count;
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
	      System.out.println("MED = " + stats[0] + "/" + stats[1] + " = " + f4.format(stats[0]/(double)stats[1]));
	    } else {
	      System.out.println("# characters-insert-delete-replace = " + stats[0]);
	      System.out.println("# characters = " + stats[1]);
	      System.out.println("MED = " + stats[0] + "/" + stats[1] + " = " + f4.format(stats[0]/(double)stats[1]));
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
