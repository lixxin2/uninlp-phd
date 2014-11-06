package edu.hitsz.nlp.partofspeech;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;





public class POSLattice {

	public POSItem[][] lattice;
	private String[] words, tags;
	private int start, end;
	private int K;
	
	public POSLattice(int start, int end, int K){
		this.K = K;
		lattice = new POSItem[end+1][K];
		this.start = start;
		this.end = end;
	}
	
	/**
	 * 每个词lattice
	 * @since 2012-3-1
	 * @param start 开始位置
	 * @param end 结束位置
	 * @param instance 所在实例
	 * @param K 包含个数
	 */
	public POSLattice(int start, int end, POSInstance instance, int K){
		this(start, end, K);
	}
	
	public POSLattice(int start, int end, POSInstance instance, int K, boolean isWord){
		this(start, end, K);
		this.words = instance.words;
		this.tags = instance.tags;
	}


	/**
	 *  
	 * 向lattice中添加新词，并通过概率判断是否添加
	 * <p> begin表示每个beam都从前面开始依次比较进行添加
	 * @since 2012-3-1
	 * @param t
	 * @param score
	 * @param item
	 * @return
	 */
	public boolean addBegin(int t, double score, POSItem item){

		boolean added = false;

		for(int i = 0; i < K; i++) {
			//如果当前i为null
			if(lattice[t][i] == null){
				lattice[t][i] = item;
				added = true;
				break;
			}
			//如果当前i与item的词和词性相同
			/*
			if(lattice[t][i].form.equals(item.form) && lattice[t][i].pos.equals(item.pos)) {
				if(lattice[t][i].prob < score) {
					lattice[t][i] = item;
				}
				break;
			}
			*/
			//递归找到合适位置
			if(lattice[t][i].prob < score) {
				POSItem tmp = lattice[t][i];
				lattice[t][i] = item;
				for(int j = i+1; j < K && tmp.prob != Double.NEGATIVE_INFINITY; j++) {
				    POSItem tmp1 = lattice[t][j];
				    lattice[t][j] = tmp;
				    tmp = tmp1;
				}
			added = true;
			break;
			}
		}

		return added;

	}
	
	/**
	 * 向lattice中添加新词，并通过概率判断是否添加
	 * <p> end表示每个beam都从后面开始依次比较进行添加
	 * @since Jan 15, 2013
	 * @param t
	 * @param score
	 * @param item
	 * @return
	 */
	public boolean addEnd(int t, double score, POSItem item){

		boolean added = false;

		int i = K-1;
		//
		for(; i >= 0; i--) {
			if(lattice[t][i] == null || lattice[t][i].prob < score)
				continue;
			else
				break;
		}
		if( i != K-1) {
			for(int j = K-1; j > i+1; j--) {
				lattice[t][j] = lattice[t][j-1];
			}
			lattice[t][i+1] = item;
			added = true;
		}
		return added;
	}
	
	
	/**
	 * 向lattice中添加新词，由于是已用堆排序好的，故直接添加即可
	 * @since Jan 15, 2013
	 * @param t
	 * @param k
	 * @param item
	 * @return
	 */
	public boolean add(int t, int k, POSItem item){

		lattice[t][k] = item;		
		return false;		
	}
	

    /**
     * 获得概率最大的路径的所有特征向量
     * @param pfi
     * @return
     */
    public POSFeatureVector getFeatureVector() {
    	POSItem cur = lattice[end][0];
    	POSFeatureVector allFV = cur.fv;
		while(cur.left != null){
			allFV.cat(cur.left.fv);
			cur = cur.left;
		}
		return allFV;
    }
    
    public POSFeatureVector getFeatureVectorPOS() {
    	POSItem cur = lattice[end][0];
    	POSFeatureVector allFV = new POSFeatureVector();
		while(cur != null){
			allFV.cat(cur.fv);
			cur = cur.left;
		}
		return allFV;
    }
    
    public POSFeatureVector getFeatureVectorPOS(int position) {
    	POSItem cur = lattice[position][0];
    	POSFeatureVector allFV = new POSFeatureVector();
		while(cur != null){
			allFV.cat(cur.fv);
			cur = cur.left;
		}
		return allFV;
    }
    
    /** 获取最优的实例 */
    public POSInstance getBestInstance() {
    	POSItem cur = lattice[end][0];
    	return cur.getInstance();    	
	}
    
    /** 获取最优的实例 */
    public Object[] getBestKInstances(int outK) {
    	POSInstance[] instances = new POSInstance[outK];
    	Double[] weights = new Double[outK];
    	int i=0;
    	POSInstance bestInstance = null;
    	Double bestWeight = 0.0;
    	for(; i<outK; i++) {
    		POSItem cur = lattice[end][i];
    		if(i == 0) {
    			bestInstance = cur.getInstance();
    			bestWeight = cur.prob;
    		}
    		if(cur == null) 
    			break;
    		instances[i] = cur.getInstance();
    		weights[i] = cur.prob;
    	}
    	for(; i<outK; i++) {
    		instances[i] = bestInstance;
    		weights[i] = bestWeight;
    	}
    	
    	Object[] objs = new Object[2];
    	objs[0] = instances;
    	objs[1] = weights;
    	return objs;
	}
        
    
    public POSInstance[] getBestInstances(int seq) {
    	POSItem[] items = lattice[seq];
    	int itemSize = items.length;
    	POSInstance[] instances = new POSInstance[itemSize];
    	for(int i=0; i<itemSize; i++) {
    		if(items[i] != null) {
    			instances[i] = items[i].getInstance();
    		}
    	}
    	return instances;   	
    }
    
    public POSInstance[] getBestInstances() {    	
    	return getBestInstances(end);
    }
    
    
    
    
    /** 得到最后的items */
    public ArrayList<POSItem> getLastItems(){
    	int length = lattice.length;
		ArrayList<POSItem> items = new ArrayList<POSItem>();
		for(int j=0; j<K; j++){
			POSItem item = lattice[length-1][j];
			if(item != null) {
				items.add(item);
			}   		
    	}
    	return items;   	
    }
    
    
    
 
    
    /** 包含的Item的数目 */
    public int itemNumbers() {
    	int number = 0;
    	int length = lattice.length;
    	int size = lattice[0].length;
    	for(int i=0; i<length; i++) {
    		for(int j=0; j<size; j++) {
    			POSItem item = lattice[i][j];
    			if(item != null)
    				number++;    			
    		}
    	}
    	return number;    		
    }
    
    
    public static void main(String[] args) throws IOException {
    	
    	String forwardLatticeName = "/home/tm/disk/disk1/transDep/lattice/ctb5-dev-parse-wp.norm.lattice.8";
    	String backwardLatticeName = "/home/tm/disk/disk1/transDep/lattice/ctb5-dev-parse-wp.norm.r.lattice.8";
    	String newLatticeName = "/home/tm/disk/disk1/transDep/lattice/ctb5-dev-parse-wp.norm.lattice.8.comb";
    	//Lattice.combLattice(forwardLatticeName, backwardLatticeName, newLatticeName);
    	
    	String goldFileName = "/home/tm/disk/disk1/transDep/lattice/ctb5-dev-parse-wp";
    	
    }

}

