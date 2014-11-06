package edu.hitsz.algorithm.sorting;

public class SelectionSort {

	public static void sort(int[] vec) {
		int i,j;
		int iMin;
		int n = vec.length;
		
		/* advance the position through the entire array */
		/*   (could do j < n-1 because single element is also min element) */
		for (j = 0; j < n-1; j++) {
		    /* find the min element in the unsorted a[j .. n-1] */
		 
		    /* assume the min is the first element */
		    iMin = j;
		    /* test against elements after j to find the smallest */
		    for ( i = j+1; i < n; i++) {
		        /* if this element is less, then it is the new minimum */  
		        if (vec[i] < vec[iMin]) {
		            /* found new minimum; remember its index */
		            iMin = i;
		        }
		    }
		 
		    /* iMin is the index of the minimum element. Swap it with the current position */
		    if ( iMin != j ) {
		        //swap(vec[j], vec[iMin]);
		    	int tmp = vec[j];
		    	vec[j] = vec[iMin];
		    	vec[iMin] = tmp;
		    }
		}
	}
	
	
	
	
	
	
}
