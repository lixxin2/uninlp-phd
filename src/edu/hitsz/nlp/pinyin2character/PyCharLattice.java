package edu.hitsz.nlp.pinyin2character;

import java.util.ArrayList;


public class PyCharLattice {
	
	public PyCharItem[][] lattice;	
	private int start, end;
	private int K;
	
	
	public PyCharLattice(int start, int end, int K){
		this.K = K;
		lattice = new PyCharItem[end+1][K];
		this.start = start;
		this.end = end;
	}
	
	
	/**
	 * 将items存入以i结尾的表中
	 * @since Oct 23, 2012
	 * @param items
	 * @param i
	 */
	public void put(ArrayList<PyCharItem> items, int i) {
		int itemSize = items.size();
		for(int j=0; j<Math.min(itemSize, K); j++)
			lattice[i][j] = items.get(j);		
		
	}
	
	
	/**
	 * 得到最优实例
	 * @since Oct 23, 2012
	 * @return
	 */
	public PyCharInstance getBest(PyCharInstance inst) {
		PyCharItem cur = lattice[end][0];
    	return PyCharInstance.getInstance(inst, cur); 	
	}
	
	
	/**
	 * 得到最优的k个实例和其权重
	 * @since Nov 5, 2012
	 * @param inst
	 * @return
	 */
	public Object[] getNgramBestK(PyCharInstance inst) {
		Object[] obj = new Object[2];
		ArrayList<PyCharInstance> instances = new ArrayList<PyCharInstance>();
		ArrayList<Double> ngramWeights = new ArrayList<Double>();
		for(int i=0; i<K; i++) {
			PyCharItem cur = lattice[end][i];
			if(cur != null) {
				ngramWeights.add(cur.prob);
				instances.add(PyCharInstance.getInstance(inst, cur));
			}
		}
		obj[0] = instances;
		obj[1] = ngramWeights;
    	return obj;
	}
	
	
	
	
	
	

}
