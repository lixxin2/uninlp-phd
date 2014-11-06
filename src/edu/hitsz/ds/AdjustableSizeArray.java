package edu.hitsz.ds;

/**
 * 给定一个数组[2][1][2]， 递归返回每种可能
 * @author Xinxin Li
 * @since Nov 28, 2012
 */
public class AdjustableSizeArray {
	
	private int length;
	private int[] bits; //数组中每个位的最大值
	private int[] curs; //当前数组，从[0][0][0]开始
	private int allSize = 1; 
	private boolean start = false;
	
	public AdjustableSizeArray(int length, int size){
		this.length = length;
		bits = new int[length];
		curs = new int[length];
		for(int i=0; i<length; i++) {
			allSize *= size;
			bits[i] = size;
		}
	}
	
	public AdjustableSizeArray(int[] newBits){
		length = newBits.length;
		bits = new int[length];
		curs = new int[length];
		for(int i=0; i<length; i++) {
			allSize *= newBits[i];
			bits[i] = newBits[i];
		}
		
	}
	
	public int getNumber() {
		return allSize;
	}
	
	
	public int[] getNext() {
		
		if(!start) {
			start = true;
			return curs;
		}
		
		for(int i=length-1; i>=0; i--) {
			if(curs[i]+1 == bits[i]) {
				curs[i] = 0;
				continue;
			}
			else {
				curs[i]++;
				break;
			}			
		}
		return curs;
		
	}
	
	
	private static void test() {
		
		//BitArray bArr = new BitArray(3,2);
		int[] oldArr = new int[3];
		oldArr[0] = 2; oldArr[1] = 1; oldArr[2] =2;
		AdjustableSizeArray bArr = new AdjustableSizeArray(oldArr);
		int number = bArr.getNumber();
		for(int i=0; i<number; i++) {
			int[] arr = bArr.getNext();
			System.out.println(arr[0]+","+arr[1]+","+arr[2]);
		}		
	}
	
	public static void main(String[] args) {
		
		AdjustableSizeArray.test();
		
	}
	
	
	

}
