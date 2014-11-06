/**
 * 
 */
package edu.hitsz.util;

/**
 * @author tm
 *
 */
public class StringMatch {

	public static int LevenshteinDistance(String aString, String bString){
		// for all i and j, d[i,j] will hold the Levenshtein distance between
		// the first i characters of s and the first j characters of t;
		// note that d has (m+1)x(n+1) values
		int aLen = aString.length();
		int bLen = bString.length();
		int[][] dist = new int[aLen+1][bLen+1];
		for(int i=0; i<=aLen; i++)
			dist[i][0] = i;// the distance of any first string to an empty second string
		for(int j=0; j<=bLen; j++)
			dist[0][j] = j;// the distance of any second string to an empty first string
		
		for(int j=1; j<=bLen; j++){
			for(int i=1; i<=aLen; i++){
				if(aString.charAt(i-1) == bString.charAt(j-1))
					dist[i][j] = dist[i-1][j-1];
				else{
					int min = dist[i-1][j] + 1;
					if(dist[i][j-1]+1 < min)
						min = dist[i][j-1] + 1;
					if(dist[i-1][j-1]+1 < min)
						min = dist[i-1][j-1] + 1;
					dist[i][j] = min;
				}
			}
		}
		return dist[aLen][bLen];
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(LevenshteinDistance("a+a","c+ab+ba"));
	}

}
