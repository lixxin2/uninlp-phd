package edu.hitsz.ml.zmert;

import java.awt.List;
import java.util.ArrayList;

import edu.hitsz.nlp.sentence.Distance;
import edu.hitsz.nlp.sentence.LevenshteinDistance;

/**
 * tag的准确率，用于词性标注等
 * @author Xinxin Li
 * @since Oct 21, 2013
 */
public class TAGACC extends EvaluationMetric {

	public TAGACC() {
		initialize();
	}
	
	public TAGACC(String[] ZOL_options) {
	    this();
	}
	
	protected void initialize() {
		metricName = "TAGGING";
	    toBeMinimized = true;
	    suffStatsCount =  2;
		
	}

	public double bestPossibleScore() { return 1.0; }
	public double worstPossibleScore() { return 0.0; }

	/**
	 * 统计候选结果的准确性
	 */
	public int[] suffStats(String cand_str, int i) {
		//return suffStatsSingleTag(cand_str, i, 0.0, 1.0);	
		//return suffStatsSingleTag(cand_str, i, 0.0, 0.5);		
		//return suffStatsBIOTag(cand_str, i, 0.5, 1.0);
		//return suffStatsMultiPOSChunk(cand_str, i, 0.5);
		return suffStatsMultiPOSChunk(cand_str, i, 0.33);
	}
	
	/** 
	 * single tag, such as POS Tagging, 
	 * @param cand_str candidate string
	 * @param i 
	 * @param startPart "part of the candidate string"
	 * @param endPart
	 * @return
	 */
	public int[] suffStatsSingleTag(String cand_str, int i, double startPart, double endPart) {
		int[] stats = new int[suffStatsCount];

	    for (int r = 0; r < refsPerSen; ++r) {
	    	
	        ArrayList<String> refChars = splitTags(refSentences[i][r]);
	        ArrayList<String> newRefChars = new ArrayList<String>();
	        int size = refChars.size();
	        for(int j=(int)(startPart * size); j<(int)(endPart * size); j++)
	        	newRefChars.add(refChars.get(j));
	        ArrayList<String> candChars = splitTags(cand_str);
	        ArrayList<String> newCandChars = new ArrayList<String>();
	        size = candChars.size();
	        for(int j=(int)(startPart * size); j<(int)(endPart * size); j++)
	        	newCandChars.add(candChars.get(j));
	        
	        int length = newRefChars.size();
	        if(length != newCandChars.size()) {
	        	System.out.println("cand and ref sentence length are different");
	        	System.out.println("ref size: " + length);
	        	System.out.println("ref:  " + refSentences[i][r]);
	        	System.out.println("cand size: " + newCandChars.size());
	        	System.out.println("cand: " + cand_str);
	        	System.exit(-1);
	        }
	        int corr = 0;
	        for(int j=0; j<length; j++) {
	        	if(newRefChars.get(j).equals(newCandChars.get(j)))
	        		corr++;
	        }
	        stats[0] = corr;
	        stats[1] = length;	 
	    }
	    
	    return stats;
	}
	
	public int[] suffStatsBIOTag(String cand_str, int i, double startPart, double endPart) {
		int[] stats = new int[suffStatsCount];

	    for (int r = 0; r < refsPerSen; ++r) {	 
	    	ArrayList<String> refChars = splitTags(refSentences[i][r]);
	    	ArrayList<String> newRefChars = new ArrayList<String>();
	        int size = refChars.size();
	        for(int j=(int)(startPart * size); j<(int)(endPart * size); j++)
	        	newRefChars.add(refChars.get(j));
	    	newRefChars = getBIOTags(newRefChars);	    	

	        ArrayList<String> candChars = splitTags(cand_str);
	        ArrayList<String> newCandChars = new ArrayList<String>();
	        size = candChars.size();
	        for(int j=(int)(startPart * size); j<(int)(endPart * size); j++)
	        	newCandChars.add(candChars.get(j));
	        newCandChars = getBIOTags(newCandChars);
	        
	        int length = newRefChars.size();	        
		    LevenshteinDistance dist = new LevenshteinDistance();
		    Distance newDist = dist.distance(newRefChars, newCandChars);	        
	        stats[0] = Math.max(length - (newDist.deletion + newDist.insertion + newDist.substitution), 0);
	        stats[1] = length;	 		     
	    }	    
	    return stats;
	}
	
	
	public int[] suffStatsMultiPOSChunk(String cand_str, int i, double prob) {
		int[] stats = new int[suffStatsCount];

	    for (int r = 0; r < refsPerSen; ++r) {	
	    	ArrayList<String> refChars = splitTags(refSentences[i][r]);
	    	ArrayList<String> firstRefChars = new ArrayList<String>();
	        int size = refChars.size();
	        for(int j=(int)(0.0 * size); j<(int)(0.5 * size); j++)
	        	firstRefChars.add(refChars.get(j));
	        ArrayList<String> candChars = splitTags(cand_str);
	        ArrayList<String> firstCandChars = new ArrayList<String>();
	        size = candChars.size();
	        for(int j=(int)(0.0 * size); j<(int)(0.5 * size); j++)
	        	firstCandChars.add(candChars.get(j));
	        int length = firstRefChars.size();
	        if(length != firstCandChars.size()) {
	        	System.out.println("cand and ref sentence length are different");
	        	System.out.println("ref size: " + length);
	        	System.out.println("ref:  " + refSentences[i][r]);
	        	System.out.println("cand size: " + firstCandChars.size());
	        	System.out.println("cand: " + cand_str);
	        	System.exit(-1);
	        }
	        int corr = 0;
	        for(int j=0; j<length; j++) {
	        	if(firstRefChars.get(j).equals(firstCandChars.get(j)))
	        		corr++;
	        }
	        stats[0] = (int) ((int) 100 * prob * corr);
	        stats[1] = (int) ((int) 100 * prob * length);	 
	    		    	
	        ArrayList<String> secondRefChars = new ArrayList<String>();
	        size = refChars.size();
	        for(int j=(int)(0.5 * size); j<(int)(1.0 * size); j++)
	        	secondRefChars.add(refChars.get(j));
	        secondRefChars = getBIOTags(secondRefChars);
	        ArrayList<String> secondCandChars = new ArrayList<String>();
	        size = candChars.size();
	        for(int j=(int)(0.5 * size); j<(int)(1.0 * size); j++)
	        	secondCandChars.add(candChars.get(j));
	        secondCandChars = getBIOTags(secondCandChars);
	        
	        length = secondRefChars.size();	        
		    LevenshteinDistance dist = new LevenshteinDistance();
		    Distance newDist = dist.distance(secondRefChars, secondCandChars);	        
	        stats[0] +=  100 * (1-prob) * Math.max(length - (newDist.deletion + newDist.insertion + newDist.substitution), 0);
	        stats[1] += 100 * (1-prob) * length;	 		     
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
	      System.out.println("TAGGING = " + stats[0] + "/" + stats[1] + " = " + f4.format(stats[0]/(double)stats[1]));
	    } else {
	      System.out.println("# correct = " + stats[0]);
	      System.out.println("# sentences = " + stats[1]);
	      System.out.println("TAGGING = " + stats[0] + "/" + stats[1] + " = " + f4.format(stats[0]/(double)stats[1]));
	    }
	}
	
	/** 得到句子中的每一个tag*/
	private ArrayList<String> splitTags(String s) {
		ArrayList<String> chars = new ArrayList<String>();
		String[] subs = s.split("[ \t]");
		for(String sub : subs) {
			chars.add(sub);
		}
		return chars;		
	}
	
	private ArrayList<String> getBIOTags(ArrayList<String> tags) {
		ArrayList<String> BIOTags = new ArrayList<String>();
		int length = tags.size();
		for(int i=0; i<length; i++) {
			String tag = tags.get(i);
			if(tag.startsWith("O"))
				//BIOTag = tag + "-" + i + "-" + i;
				continue;
			else if(tag.startsWith("B")) {
				String BIOTag = tag.substring(2) + "-" + i;
				int j = i+1;
				for(; j<length; j++) {
					tag = tags.get(j);
					if(tag.startsWith("O") || tag.startsWith("B"))
						break;
				}
				i = j - 1;
				BIOTag += "-" + i;
				BIOTags.add(BIOTag);
			}
			
		}		
		return BIOTags;
	}
		
	
}
