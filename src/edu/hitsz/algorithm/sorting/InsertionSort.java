package edu.hitsz.algorithm.sorting;

/**
 * 插入排序
 * @author Xinxin Li
 * @since Oct 27, 2012
 */
public class InsertionSort {
	
	/** 从小到大 */
	public static void sort(int[] vec) {
		
		int length  = vec.length;		
		for (int i=0; i<length; i++) {
		     // A[ i ] is added in the sorted sequence A[0, .. i-1]
		     // save A[i] to make a hole at index iHole
		     int item = vec[i];
		     int iHole = i;
		     // keep moving the hole to next smaller index until A[iHole - 1] is <= item
		     while(iHole > 0 && vec[iHole - 1] > item) {
		         // move hole to next smaller index
		         vec[iHole] = vec[iHole - 1];
		         iHole = iHole - 1;
		     }
		     // put item in the hole
		     vec[iHole] = item;
		}
		
	}
		


}
