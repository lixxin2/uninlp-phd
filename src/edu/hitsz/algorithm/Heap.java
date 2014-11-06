package edu.hitsz.algorithm;

import java.util.ArrayList;

/**
 * 堆排序：
 * <p> 如果求N个数中的前K个最大（小）的数的方法，
 * 则取N个元素中的前K个元素来建立一个由K个元素组成的小（大）顶堆，这样堆顶元素便是当前已读取元素中的第K大（小）者；
 * <p> 然后，依次读取剩下的N-K个元素，而对于其中的每个元素x，若x大于（小于）当前的堆顶元素，则将堆顶元素替换为x，并自堆顶至向下调整堆；
 * <p> 这样，在读取完所有元素后，堆中的K个元素即为这N个数中的前K个最大（小）元素，同时堆顶元素为这N个数中的第K大（小）元素。
 * <p> 默认为最小堆，可以取前K个最大的数
 * @author Xinxin Li
 * @since Oct 22, 2012
 * @param <E>
 */
public class Heap<E> {

	private ArrayList<HeapPair<E>> heaps;	
	private int K;
	private String comp = ">";
	private boolean sorted = false;
	
	public Heap() {		
		heaps = new ArrayList<HeapPair<E>>();
		K = -1;
	}
	
	public Heap(int K) {
		heaps = new ArrayList<HeapPair<E>>(2*K+1);
		this.K = K;
	}
	
	/**设置比较符*/
	public void setComp(String comp) {
		this.comp = comp;
	}
	
	/*父亲节点*/
	int parent(int i)
	{
	    return (int)Math.floor((i - 1) / 2);
	}
	 
	/*左子結點*/
	int left(int i)
	{
	    return (2 * i + 1);
	}
	 
	/*右子結點*/
	int right(int i)
	{
	    return (2 * i + 2);
	}
	
	/**
	 * 添加新元素
	 * @since Oct 22, 2012
	 * @param obj 现在的对象
	 * @param prob 对象的权重
	 */
	public void add(E e, Double prob) {
		
		HeapPair<E> hp = new HeapPair<E>(e, prob);
		/**如果heap不限制长度，或现在长度小于限定长度*/
		if( K == -1 || heaps.size() < K) {
			heaps.add(hp);
			int heapSize = heaps.size();
			if(heapSize == K)
				buildHeap();
		}
		//int heapSize = heaps.size();
		//if(heapSize == K) {
		//	insert(hp);
		//}		
		else
			insert(hp);
	}
	
	public void add(E obj, int prob){
		add(obj, (double) prob);
	}
	
	/**
	 * 初始化建立堆
	 * @since Oct 23, 2012
	 */
	public void buildHeap() {
		if(!sorted) {
			int heapSize = heaps.size();
			for(int i=heapSize/2; i>=0; i--) 
				heapify(i);
			sorted = true;
		}			
	}

	
	/**
	 * 插入新元素
	 * @since Oct 23, 2012
	 * @param hp
	 */
	public void insert(HeapPair<E> hp) {
		if(compare(hp, heaps.get(0))) {
			heaps.set(0, hp);
			heapify(0);
		}			
	}	
	
	
	/**
	 * 或者K个最大（小）值
	 * @since Oct 23, 2012
	 * @return
	 */
	public ArrayList<E> getK() {
		int heapSize = Math.min(K, heaps.size());
		ArrayList<E> lists = new ArrayList<E>(heapSize);
		for(int i=0; i<heapSize; i++)
			lists.add(null);
		for(int i=heapSize-1; i>=0; i--) {
			E e = extract();
			lists.set(i, e);
		}
		return lists;		
	}
	
	public void outputK() {
		ArrayList<E> lists = getK();
		for(int i=0; i<lists.size(); i++) {
			System.out.println(lists.get(i).toString());
		}
	}
	
	/** 不用删除元素得到其中的K个元素 */
	public ArrayList<E> getKNoDelete() {
		ArrayList<E> lists = new ArrayList<E>();
		int k=0;
		for(HeapPair<E> pair : heaps) {
			if(pair != null) {
				if(k<K) {
					lists.add(pair.e);
					k++;
				}
				else
					break;
			}				
		}
		return lists;
	}
	
	
	
	
	
	
	
	/**
	 * 对位于位置i的元素排序:
	 * 比较i与其左右子节点，选择最大的子节点j与i进行替换，并递归子节点j进行排序。
	 * @since Oct 22, 2012
	 * @param i
	 */
	public void heapify(int i) {
		int l = left(i);
		int r = right(i);
		int heapSize = heaps.size();
		
		int largest = i;
		if(l < heapSize && !compare(heaps.get(l), heaps.get(i)))
			largest = l;
		
		if(r < heapSize && !compare(heaps.get(r), heaps.get(largest)))
			largest = r;
		
		if(largest != i) {
			exchange(i, largest);
			heapify(largest);			
		}		
	}
	
	
	public E extract() {
		int heapSize = heaps.size();
		if(heapSize > 0) {
			HeapPair<E> hp = heaps.get(0);
			/*如果只有一个元素*/
			if(heapSize > 1) {
				exchange(0, heapSize-1);
				heaps.remove(heapSize-1);
				heapify(0);
			}
			return hp.e;		
		}
		else
			return null;		
	}
	
	
	public void exchange(int i, int j) {
		HeapPair<E> tmp = heaps.get(i);
		heaps.set(i, heaps.get(j));
		heaps.set(j, tmp);
	}
	
	/**
	 * 比较两个值
	 * @since Oct 22, 2012
	 * @param f1
	 * @param f2
	 * @return
	 */
	public boolean compare(HeapPair<E> hp1, HeapPair<E> hp2) {
		return hp1.compare(hp2, comp);
	}
	
	
	public static void main(String[] args) {
		
		Heap<Integer> heap = new Heap<Integer>(5);
		heap.add(-3, -3);
		heap.add(-2, -2);
		heap.add(-7, -7);
		heap.add(3, 3);
		heap.add(-4, -4);
		heap.add(-5, -5);
		heap.add(-6, -6);
		heap.buildHeap();
		heap.outputK();
		System.out.println();
		
		
	}
	
	
	
}

class HeapPair<E> {

	E e;
	double f;
	
	HeapPair(E e, double f) {
		this.e = e;
		this.f = f;
	}
	
	boolean compare(HeapPair<E> hp, String comp) {
		if(comp.equals(">"))
			return  this.f > hp.f;
		else if(comp.equals("<"))
			return this.f < hp.f;
		else
			return true;
	}
	
}
