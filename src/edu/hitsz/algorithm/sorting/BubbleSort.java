package edu.hitsz.algorithm.sorting;

public class BubbleSort {
	
	/**
	 * 冒泡排序
	 * @since Oct 27, 2012
	 * @param lists
	 */
	public static void sort(int[] lists) {
		
		boolean swapped = true;
		while(swapped) {			
			swapped = false;
			int length = lists.length;			
			for(int i=1; i<length; i++) {
				/* if this pair is out of order */
				if(lists[i-1] > lists[i]) {
					/* swap them and remember something changed */
					int tmp = lists[i-1];
					lists[i-1] = lists[i];
					lists[i] = tmp;	
					swapped = true;
				}			
			}
		}
		
	}
	
	public static void sort1(int[] lists) {
		
		boolean swapped = true;
		int length = lists.length;	
		while(swapped) {			
			swapped = false;		
			for(int i=1; i<length; i++) {
				if(lists[i-1] > lists[i]) {
					int tmp = lists[i-1];
					lists[i-1] = lists[i];
					lists[i] = tmp;	
					swapped = true;
				}			
			}
			length--;
		}
		
	}
	
	
	public static void sort2(int[] lists) {
		
		int n = lists.length;	
		while(n != 0) {			
			int newn = 0;		
			for(int i=1; i<n; i++) {
				if(lists[i-1] > lists[i]) {
					int tmp = lists[i-1];
					lists[i-1] = lists[i];
					lists[i] = tmp;	
					newn = i;
				}			
			}
			n = newn;
		}
		
	}
	
	
	public static void main(String[] args) {
		int a[] = {5,1,4,2,8};
		BubbleSort.sort2(a);
		System.out.println(a);
		
	}
	

}
