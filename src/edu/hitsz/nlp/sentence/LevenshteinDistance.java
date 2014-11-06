package edu.hitsz.nlp.sentence;

import java.util.ArrayList;
import java.util.List;

/**
 * Levenshtein Distance
 * "toned" <- "roses"
 * @author Xinxin Li
 * @since Aug 23, 2012
 */
public class LevenshteinDistance {
	
	public LevenshteinDistance() {	
		
	}
		
	/**
	 * 计算字符串距离,s1<-s2
	 * @since Sep 11, 2013
	 * @param s1
	 * @param s2
	 * @return
	 */
	public Distance stringDistance(String s1, String s2) {
		Distance[][] distances = stringDistances(s1, s2);
		int row = distances.length;
		int column = distances[0].length;
		return distances[row-1][column-1];
	}
	
	/**
	 * 计算字符串距离
	 * @since Aug 13, 2012
	 * @param s1 transform
	 * @param s2 gold
	 * @return
	 */
	public Distance[][] stringDistances(String s1, String s2) {
		
		s1 = s1.replaceAll("\\s+", "");
		s2 = s2.replaceAll("\\s+", "");

		int length1 = s1.length();		
		String[] words1 = new String[length1];
		for(int i=0; i<length1; i++) 
			words1[i] = s1.substring(i,i+1);
		
		int length2 = s2.length();
		String[] words2 = new String[length2];
		for(int i=0; i<length2; i++)
			words2[i] = s2.substring(i,i+1);
		
		return distances(words1, words2);
	}
	
	
	
	/**
	 * 计算两个句子间的距离，每个句子有多个词组成，词之间有空（用"\\s+"分割）
	 * @since Sep 11, 2013
	 * @param s1
	 * @param s2
	 * @return
	 */
	public Distance sentenceDistance(String s1, String s2) {
		return distance(s1.split("\\s+"), s2.split("\\s+"));
	}
	
	/**
	 * 计算两个句子间的距离，每个句子有多个词组成
	 * @since Sep 11, 2013
	 * @param s1
	 * @param s2
	 * @return
	 */
	public Distance[][] sentenceDistances(String s1, String s2) {
		return distances(s1.split("\\s+"), s2.split("\\s+"));
	}
	
	
	public Distance distance(String[] words1, String[] words2) {
		
		int length1 = words1.length;
		int length2 = words2.length;
		Distance[][] distances = distances(words1, words2);
		return distances[length1][length2];
	}
	
	/**
	 * 计算距离
	 * @since Aug 23, 2012
	 * @param list1
	 * @param list2
	 * @return
	 */
	public Distance[][] distances(String[] list1, String[] list2) {
		
		int length1 = list1.length;
		int length2 = list2.length;
		//if(length1 ==0 || length2 == 0) {
		//	System.exit(-1);
		//}
		Distance[][] distances= new Distance[length1+1][length2+1];
		/*
		for(int i=0; i<length1+1;i++)
			for(int j=0; j<length2+1;j++)
				distances[i][j] = new Distance(i,j,0,0,0);
		for(int i=0; i<length1+1;i++)
			distances[i][0] = new Distance(i,0,i,0,0);
		for(int j=0; j<length2+1;j++)
			distances[0][j] = new Distance(0,j,j,0,0);
		*/
		
		for(int i = 0; i< length1+1; i++) {
			for(int j = 0; j< length2+1; j++) {
				//System.out.println(i+","+j);
				compLevenshteinDistance(list1, list2, distances, i, j);
			}
		}		
		//print(distances);		
		return distances;			
	}
	
	
	public Distance distance(ArrayList<String> wordlist1, ArrayList<String> wordlist2) {			
		int length1 = wordlist1.size();
		String[] words1 = new String[length1];
		for(int i=0; i<length1; i++)
			words1[i] = wordlist1.get(i);
		int length2 = wordlist2.size();
		String[] words2 = new String[length2];
		for(int i=0; i<length2; i++)
			words2[i] = wordlist2.get(i);
		Distance[][] distances = distances(words1, words2);
		return distances[length1][length2];
	}
	
	
	public void compLevenshteinDistance(String[] words1, String[] words2, 
			Distance[][] distances, int i, int j) {
		
		if( i == 0 || j == 0) {
			distances[i][j] = new Distance(i,j,i,j,0);
			if( i != 0)
				distances[i][j].prev = distances[i-1][j];
			else if( j != 0)
				distances[i][j].prev = distances[i][j-1];
		}
		else {
			int insertionDist = distances[i-1][j].dist + 1;
			int deletionDist = distances[i][j-1].dist + 1;
			boolean equal = false;
			if( words1[i-1].equals( words2[j-1]))
				equal = true;
			int substitutionDist = distances[i-1][j-1].dist + (equal ? 0 : 1);
			
			Distance prev = null;
			//insertion
			if(insertionDist <= Math.min(deletionDist, substitutionDist)) {
				prev = distances[i-1][j];
				distances[i][j] = new Distance(i,j, prev.insertion+1, prev.deletion, prev.substitution);
				distances[i][j].correct = prev.correct;
				distances[i][j].action = "insertion";
			}
			else if(deletionDist <= Math.min(insertionDist, substitutionDist)) {
				prev = distances[i][j-1];
				distances[i][j] = new Distance(i,j, prev.insertion, prev.deletion+1, prev.substitution);
				distances[i][j].correct = prev.correct;
				distances[i][j].action = "deletion";
			}
			else {
				prev = distances[i-1][j-1];
				if(equal) {
					distances[i][j] = new Distance(i,j, prev.insertion, prev.deletion, prev.substitution);
					distances[i][j].correct = prev.correct + 1;
					distances[i][j].action = "keep";
				}
				else {
					distances[i][j] = new Distance(i,j, prev.insertion, prev.deletion, prev.substitution+1);
					distances[i][j].correct = prev.correct;
					distances[i][j].action = "subsutitution";
				}
			}
			distances[i][j].prev = prev;			
		}
		
	}
	
	
	public void print(Distance[][] distances) {
		int n = distances.length;
		int m = distances[0].length;
		for(int i=0; i<n; i++) {
			for(int j=0; j<m; j++) {
				System.out.print(distances[i][j].dist+"\t");
			}
			System.out.println();
		}
		System.out.println("wrong:"+distances[n-1][m-1].dist+"\t"
				+ "insert:"+distances[n-1][m-1].insertion+"\t"
				+ "delete:"+distances[n-1][m-1].deletion+"\t"
				+ "replace:"+distances[n-1][m-1].substitution+"\t"				
				+ "\nCorrect:"+distances[n-1][m-1].correct);		
	}
	
	
	
	
	public static void main(String[] args) {
		LevenshteinDistance med = new LevenshteinDistance();
		String gold = "sunday";
		String pred = "saturday";
		Distance newDist = med.stringDistance(pred, gold);
		newDist.output();
		ArrayList<Distance> dists = newDist.getDistances();
		int length = 6;
		int cor = Math.max(length - (newDist.deletion + newDist.insertion + newDist.substitution), 0);
		System.out.println(cor + "," + length);
	}
	
	
	
	
	
	
	
}


