package edu.hitsz.ds;


/**
 * bit operations
 * 
 * <p> http://graphics.stanford.edu/~seander/bithacks.html
 * @author Xinxin Li
 * @since Nov 30, 2012
 */
public class IntegerBit {
	
	/** Compute the sign of an integer  */
	public static int getSign(int v) {
		
		int sign = 0;
		
		// CHAR_BIT is the number of bits per byte (normally 8).
		sign = -(v << 0);  // if v < 0 then -1, else 0. 
		
		return sign;
		
	}
	
	
	
	
	public static void main(String[] args) {
		
		System.out.println(IntegerBit.getSign(-1));
		System.out.println(IntegerBit.getSign(1));
		
	}
	
	

}
