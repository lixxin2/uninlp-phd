package edu.hitsz.java.math;

/** 欧基里德算法计算最小公约数 */
public final class EuclidAlgorithm {

	public static int comp(int a, int b){
		
		if (a < b) {
			int c = a; c = b; b = c;
		}
		int r = a % b;
		while (r != 0) {
			a = b;
			b = r;
			r = a % b;
		}
		return b;
	}

	public static void main(String[] argv){
		System.out.println(EuclidAlgorithm.comp(100, 14));
	}

}
