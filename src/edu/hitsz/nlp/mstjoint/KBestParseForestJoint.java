package edu.hitsz.nlp.mstjoint;

import java.util.ArrayList;

import edu.hitsz.algorithm.Heap;


/**
 * 依存句法树
 * @author tm
 *
 */
public class KBestParseForestJoint {

    public static int rootType;

    public ParseForestItemJoint[][][][][][] chart;
    //private String[] sent,pos;
    private int start,end;
    private int K;

    /**
     * 初始化
     * @param start
     * @param end
     * @param inst
     * @param K
     */
    public KBestParseForestJoint(int start, int end, DependencyInstanceJoint inst, int leftRightK, int K) {
		this.K = K;
		chart = new ParseForestItemJoint[end+1][end+1][2][2][leftRightK][K];
		this.start = start;
		this.end = end;
		//this.sent = inst.forms;
		//this.pos = inst.postags;
    }

  
   /**
    * 添加初始span
    * @since Mar 26, 2012
    * @param s 开始位置
    * @param t 结束位置
    * @param seq 插入位置:因为左边有L个，右边有R个，所有插入位置为l*R+r
    * @param type 依存类型
    * @param dir 方向
    * @param comp 完整性，总为1
    * @param score 概率
    * @param leftK 左边词索引
    * @param rightK 右边词索引
    * @param K
    * @param leftWP 左边词
    * @param rightWP 右边词
    * @param allWP
    * @return
    */
   public boolean addLR(int s, int t, 
		   int seq, 
		   int type, 
		   int dir, 
		   int comp,
		   double score, 
		   int leftK, int rightK, 
		   int k,
		   WordPos leftWP, WordPos rightWP,
		   ArrayList<WordPos> allWP) {
		
		if(chart[s][t][dir][0][seq][k] == null || chart[s][t][dir][0][seq][k].prob < score) {
			chart[s][t][dir][0][seq][k] =
				new ParseForestItemJoint(s,t,type,dir,comp,score,null,null,null,leftK,rightK,leftWP,rightWP, allWP);
			return true;
		}		
		return false;
	   
   }
   
   
   	
   
   
   public void addLR(int s, int r, int t,
		   int dir, int comp,
		   int seq, int k,
		   ParseForestItemJoint item) {
	   chart[s][t][dir][comp][seq][k] = item;
   }
    


    /**
     * 从s到t的所有span，given (dir方向，comp完全还是不完全)
     * @since Jan 11, 2012
     * @param s
     * @param t
     * @param dir
     * @param comp
     * @return
     */    
    public ParseForestItemJoint[][] getItemsLR(int s, int t, int dir, int comp) {
		if(chart[s][t][dir][comp] != null)
		    return chart[s][t][dir][comp];
		return null;
    }

    
    /**
     * 得到最优span的特征集和字符串
     * @return
     */
    public Object[] getBestParseObj(int lSize, int rSize) {
		Object[] d = new Object[3];
		ParseForestItemJoint item = getBestItem(lSize, rSize);
		d[0] = item;
		d[1] = getFeatureVector(item);
		d[2] = getDepString(item);
		return d;
    }

    /**
     * 得到最优K个span的特征集和字符串
     * @return
     */
    public Object[][] getBestParseObjs(int lSize, int rSize) {
		Object[][] d = new Object[K][3];
		ParseForestItemJoint[] items = getBestItems(lSize, rSize);
		for(int k = 0; k < K; k++) {
		    if(items[k] != null) {
		    	d[k][0] = items[k];
				d[k][1] = getFeatureVector(items[k]);
				d[k][2] = getDepString(items[k]);
		    }
		    else {
				d[k][0] = null;
				d[k][1] = null;
				d[k][2] = null;
		    }
		}
		return d;
    }
    
   
    /**
     * 获取最优的item
     * @since Mar 26, 2012
     * @param pipe
     * @param inst
     * @param K
     * @return
     */
    public ParseForestItemJoint getBestItem(int lSize, int rSize) {		
		double tmp = Double.NEGATIVE_INFINITY;
		ParseForestItemJoint best = null;
		for(int i=0; i<lSize; i++) {
			for(int j=0; j<lSize; j++) {
				ParseForestItemJoint[] items = chart[start][end][0][1][i*lSize+rSize];
				for(int k = 0; k < K; k++) {
				    if(items[i] != null) {
				    	if(items[i].prob > tmp) {
					    	best = items[i];
					    	tmp = items[i].prob;
				    	}
				    }
				    else
				    	break;
				}
			}
		}		
		return best;
    }
    
    
    /** 最优的K个items */
    public ParseForestItemJoint[] getBestItems(int lSize, int rSize) {		
	
		ParseForestItemJoint best = null;
		Heap<ParseForestItemJoint> heap = new Heap<ParseForestItemJoint>(K);
		for(int i=0; i<lSize; i++) {
			for(int j=0; j<lSize; j++) {
				ParseForestItemJoint[] items = chart[start][end][0][1][i*lSize+rSize];
				for(int k = 0; k < K; k++) {
				    if(items[i] != null) {
				    	heap.add(items[i], items[i].prob);
				    }
				    else
				    	break;
				}
			}
		}		
		heap.buildHeap();
		ParseForestItemJoint[] items = new ParseForestItemJoint[K];
		ArrayList<ParseForestItemJoint> itemList = heap.getK();
		int k=0;
		for(ParseForestItemJoint item : itemList)
			items[k++] = item;
		
		return items;
    }
    

    /**
     * 得到依存span的特征向量
     * @param pfi
     * @return
     */
    public FeatureVector getFeatureVector(ParseForestItemJoint pfi) {
    	
		if(pfi.left == null)
		    return pfi.fv;

		return cat(pfi.fv,cat(getFeatureVector(pfi.left),getFeatureVector(pfi.right)));
    }

    public FeatureVector cat(FeatureVector fv1, FeatureVector fv2) {
    	return fv1.cat(fv2);
    }
    
    
    public FeatureVector getFeatureVector(DependencyPipeJoint pipe, 
    		DependencyInstanceJoint inst, String bestParse){
    	String[] res = bestParse.split(" ");
	   for(int j = 0; j < res.length; j++) {
			String[] trip = res[j].split("[\\|:]");
			inst.deprels[j+1] = pipe.types[Integer.parseInt(trip[2])];
			inst.heads[j+1] = Integer.parseInt(trip[0]);
	    }
	   return pipe.createFeatureVector(inst, true);
    	
    }
    
    
    
    /**
     * 得到span的字符串
     * @param pfi
     * @return
     */
    public String getDepString(ParseForestItemJoint pfi) {
		if(pfi.left == null || pfi.right == null) {
			if(pfi.dir == 0) {
				return " "+pfi.leftWP.getStart()+"|"+pfi.leftWP.getEnd()+":"+
						pfi.leftWP.getWord()+":"+pfi.leftWP.getPos();
			}
			else {
				return " "+pfi.rightWP.getStart()+"|"+pfi.rightWP.getEnd()+
						":"+pfi.rightWP.getWord()+":"+pfi.rightWP.getPos();
			}			
		}
		else {
		    return (getDepString(pfi.left)+" "+getDepString(pfi.right)).trim();
		}
    }
    
    
    
    

    



    /**
     * 得到最好的k个对
     * @since Jan 11, 2012
     * @param items1
     * @param items2
     * @return pairs of indeces and -1,-1 if < K pairs 返回值为左右对的索引
     */
    public int[][] getKBestPairs(ParseForestItemJoint[] items1, ParseForestItemJoint[] items2) {
	// in this case K = items1.length

		boolean[][] beenPushed = new boolean[K][K];

		int[][] result = new int[K][2];
		for(int i = 0; i < K; i++) {
		    result[i][0] = -1;
		    result[i][1] = -1;
		}

		if(items1 == null || items2 == null || items1[0] == null || items2[0] == null)
		    return result;

		BinaryHeap heap = new BinaryHeap(K+1);
		int n = 0;
		ValueIndexPair vip = new ValueIndexPair(items1[0].prob+items2[0].prob,0,0);

		heap.add(vip);
		beenPushed[0][0] = true;

		while(n < K) {
		    vip = heap.removeMax();

		    if(vip.val == Double.NEGATIVE_INFINITY)
			break;

		    result[n][0] = vip.i1;
		    result[n][1] = vip.i2;

		    n++;
		    if(n >= K)
		    	break;

		    if(!beenPushed[vip.i1+1][vip.i2]) {
				heap.add(new ValueIndexPair(items1[vip.i1+1].prob+items2[vip.i2].prob,vip.i1+1,vip.i2));
				beenPushed[vip.i1+1][vip.i2] = true;
		    }
		    if(!beenPushed[vip.i1][vip.i2+1]) {
				heap.add(new ValueIndexPair(items1[vip.i1].prob+items2[vip.i2+1].prob,vip.i1,vip.i2+1));
				beenPushed[vip.i1][vip.i2+1] = true;
		    }

		}

		return result;
   	}
}


class ValueIndexPairJoint {
    public double val;
    public int i1, i2;

    public ValueIndexPairJoint(double val, int i1, int i2) {
		this.val = val;
		this.i1 = i1;
		this.i2 = i2;
    }

    public int compareTo(ValueIndexPair other) {
		if(val < other.val)
		    return -1;
		if(val > other.val)
		    return 1;
		return 0;
    }

}

/**
 * Max Heap
 * We know that never more than K elements on Heap
 * @author tm
 *
 */
class BinaryHeapJoint {
    private int DEFAULT_CAPACITY;
    private int currentSize;
    private ValueIndexPair[] theArray;

    public BinaryHeapJoint(int def_cap) {
		DEFAULT_CAPACITY = def_cap;
		theArray = new ValueIndexPair[DEFAULT_CAPACITY+1];
		// theArray[0] serves as dummy parent for root (who is at 1)
		// "largest" is guaranteed to be larger than all keys in heap
		theArray[0] = new ValueIndexPair(Double.POSITIVE_INFINITY,-1,-1);
		currentSize = 0;
    }

    public ValueIndexPair getMax() {
    	return theArray[1];
    }

    private int parent(int i) { return i / 2; }
    private int leftChild(int i) { return 2 * i; }
    private int rightChild(int i) { return 2 * i + 1; }

    public void add(ValueIndexPair e) {

		// bubble up:
		int where = currentSize + 1; // new last place
		while ( e.compareTo(theArray[parent(where)]) > 0 ){
		    theArray[where] = theArray[parent(where)];
		    where = parent(where);
		}
		theArray[where] = e; currentSize++;
    }

    public ValueIndexPair removeMax() {
		ValueIndexPair min = theArray[1];
		theArray[1] = theArray[currentSize];
		currentSize--;
		boolean switched = true;
		// bubble down
		for ( int parent = 1; switched && parent < currentSize; ) {
		    switched = false;
		    int leftChild = leftChild(parent);
		    int rightChild = rightChild(parent);

		    if(leftChild <= currentSize) {
				// if there is a right child, see if we should bubble down there
				int largerChild = leftChild;
				if ((rightChild <= currentSize) &&
				    (theArray[rightChild].compareTo(theArray[leftChild])) > 0){
				    largerChild = rightChild;
				}
				if (theArray[largerChild].compareTo(theArray[parent]) > 0) {
				    ValueIndexPair temp = theArray[largerChild];
				    theArray[largerChild] = theArray[parent];
				    theArray[parent] = temp;
				    parent = largerChild;
				    switched = true;
				}
		    }
		}
		return min;
    }

}



