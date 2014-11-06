package edu.hitsz.nlp.segpos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;





public class Lattice {

	public Item[][] lattice;
	private String[] chars, charpos;
	private String[] words, tags;
	private int start, end;
	private int K;

	public String[] getChars() {
		return chars;
	}
	
	public Lattice(int start, int end, int K){
		this.K = K;
		lattice = new Item[end+1][K];
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
	public Lattice(int start, int end, Instance instance, int K){
		this(start, end, K);
		this.chars = instance.chars;
		this.charpos = instance.chartags;
	}
	
	public Lattice(int start, int end, Instance instance, int K, boolean isWord){
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
	public boolean addBegin(int t, double score, Item item){

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
				Item tmp = lattice[t][i];
				lattice[t][i] = item;
				for(int j = i+1; j < K && tmp.prob != Double.NEGATIVE_INFINITY; j++) {
				    Item tmp1 = lattice[t][j];
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
	public boolean addEnd(int t, double score, Item item){

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
	public boolean add(int t, int k, Item item){

		lattice[t][k] = item;		
		return false;		
	}
	

    /**
     * 获得概率最大的路径的所有特征向量
     * @param pfi
     * @return
     */
    public FeatureVector getFeatureVector() {
    	Item cur = lattice[end][0];
    	FeatureVector allFV = cur.fv;
		while(cur.left != null){
			allFV.cat(cur.left.fv);
			cur = cur.left;
		}
		return allFV;
    }
    
    public FeatureVector getFeatureVectorPOS() {
    	Item cur = lattice[end][0];
    	FeatureVector allFV = new FeatureVector();
		while(cur != null){
			allFV.cat(cur.fv);
			cur = cur.left;
		}
		return allFV;
    }
    
    /** 获取最优的实例 */
    public Instance getBestInstance() {
    	Item cur = lattice[end][0];
    	return cur.getInstance();    	
	}
    
    public double getBestProb() {
    	Item cur = lattice[end][0];
    	return cur.prob;
    }
    
    
    public Instance[] getBestInstances(int seq) {
    	Item[] items = lattice[seq];
    	int itemSize = items.length;
    	Instance[] instances = new Instance[itemSize];
    	for(int i=0; i<itemSize; i++) {
    		if(items[i] != null) {
    			instances[i] = items[i].getInstance();
    		}
    	}
    	return instances;   	
    }
    
    public Instance[] getBestInstances() {
    	
    	return getBestInstances(end);
    }
    
    
    
    /** 得到lattice中所有的wordpos*/
    public ArrayList<ArrayList<BasicWordPos>> getWordPos(){
    	ArrayList<ArrayList<BasicWordPos>> allwordpos = new ArrayList<ArrayList<BasicWordPos>>();
    	int length = lattice.length;
    	for(int i=0; i<length; i++) {
    		ArrayList<BasicWordPos> wordpos = new ArrayList<BasicWordPos>();
    		for(int j=0; j<K; j++){
    			Item newItem = lattice[i][j];
    			if(newItem != null) {
    				wordpos.add(new BasicWordPos(newItem.form, newItem.pos, newItem.s, newItem.t-1));
    			}
    		}
    		allwordpos.add(wordpos);    		
    	}
    	return allwordpos;   	
    }
    
    
    /** 得到最后的items */
    public ArrayList<Item> getLastItems(){
    	int length = lattice.length;
		ArrayList<Item> items = new ArrayList<Item>();
		for(int j=0; j<K; j++){
			Item item = lattice[length-1][j];
			if(item != null) {
				items.add(item);
			}   		
    	}
    	return items;   	
    }
    
    
    /**
     * 以一种格式输出lattice中的词和词性
     * [start, end, word, pos, prob, curProb]
     * @since Jun 12, 2012
     * @param writer
     * @throws IOException
     */
    public void writeWP(BufferedWriter writer, int cnt) throws IOException {
    	int latticeLength = lattice.length;
    	int size = lattice[0].length;
    	
    	StringBuffer strbuf = new StringBuffer();
    	strbuf.append(cnt + " " + latticeLength + " " + size + "\n");
    	for(int i=0; i<latticeLength; i++) {
    		for(int j=0; j<size; j++) {
    			Item item = lattice[i][j];
    			if(item != null) {
    				strbuf.append(item.s + " " + item.t + " " + item.form + " " + item.pos
    						+ " " + item.prob + " " + item.curProb + "\n");
    			}
    			else
    				break;
    		}
    	}	
    	writer.write(strbuf.toString() + "\n");
    }
    
    /**
     * 消减策略
     * @since Sep 26, 2012
     */
    public void cutting() {
    	
    }
    
    
    /** 前后翻转整个kbeam lattice
     * <p> 比如长度为4的句子，lattice如下 
     *  forward：  0 1 2 3 4
     *  backward：   4 3 2 1 0
     *  
     * */
    public Lattice reverseLattice() {
    	    	   	
    	if(lattice != null) {
    		int length = lattice.length;
    		if(length > 0) {
    			int size = lattice[0].length;
    			if(size > 0) {
    				Item[][] newlattice = new Item[length][size];
    				for(int i=0; i<length; i++) { 
    					for(int j=0; j<size; j++) {
    						if(lattice[i][j] != null)
    							newlattice[length-i][j] = lattice[i][j].reverse(false, length-1);
    						else
    							break;
    					}
    				}
        			Lattice newLattice = new Lattice(0, length-1, size);
        			newLattice.lattice = newlattice;
        			return newLattice;
    			}
    		}    		
    	}    
    	return null;
    }
    
    
    /** 合并两个lattice */
    public Lattice expand(Lattice backwardLattice) {
    	
    	if(backwardLattice != null) {
    		Item[][] backwardlattice = backwardLattice.lattice;    		
    		int length = lattice.length;               //长度，字数长+1
    		int maxWordBgn = lattice[0].length + backwardlattice[0].length;
    		//
    		int[] curWordSize = new int[length];
    		Item[][] newLattice = new Item[length][maxWordBgn];
    		for(int i=0; i<length; i++) {
    			int size = lattice[i].length;
    			for(int j=0; j<size; j++) {
    				Item curItem = lattice[i][j];
    				if(curItem != null) {
    					newLattice[curItem.s+1][curWordSize[curItem.s+1]++] = lattice[i][j];
    				}
    				else
    					break;
    			}
    		}
    		for(int i=0; i<length; i++) {
    			int size = backwardlattice[i].length;
    			for(int j=0; j<size; j++) {
    				Item curItem = backwardlattice[i][j];
    				if(curItem != null) {
    					for(int k=0; k<maxWordBgn; k++) {
    						if(newLattice[curItem.s+1][k].equals(curItem))
    							break;
    						if(newLattice[curItem.s+1][k] == null) {
    							newLattice[curItem.s+1][k] = backwardlattice[i][j];
    		    				break;
    						}
    					}
    				}
    				else
    					break;
    			}
    		}
    		Lattice nLattice =  new Lattice(0, length-1, maxWordBgn);
    		nLattice.lattice = newLattice;
    		return nLattice;    		
    	}
    	return this;    	
    	
    }
       
    
    /** 每个词的前后索引翻转 
     * <p> 原来的是按照词的后向建立索引，即以词结束的位置存入lattice
     * <p> 前后索引翻转，以词开始的位置存入lattice
     * */ 
    public Lattice flap2WordBgnLattice() {
    	
    	int length = lattice.length; 
    	int maxWordBgn = getMaxWordBegin();
		//存储
		int[] curWordSize = new int[length];
		Item[][] newLattice = new Item[length][maxWordBgn];
		for(int i=0; i<length; i++) {
			int size = lattice[i].length;
			for(int j=0; j<size; j++) {
				Item curItem = lattice[i][j];
				if(curItem != null) {
					int index = curItem.s+1;
					newLattice[index][curWordSize[index]++] = lattice[i][j];
				}
				else
					break;
			}
		}
		Lattice nLattice =  new Lattice(0, length-1, maxWordBgn);
		nLattice.lattice = newLattice;
		return nLattice;
    }
    
    
    /** 获得每个字位置开始的最大词数 */
    public int getMaxWordBegin() {
    	    	
		int length = lattice.length;               //长度，字数长+1
		//统计以每个字开始的Item的数目，以便统计每个字位置开始的最大Item数目
		int[] candWordSize = new int[length];      
		for(int i=0; i<length; i++) { 
			int size = lattice[i].length;          //记录每个字结束的Item的最多个数
			for(int j=0; j<size; j++) {
				Item curItem = lattice[i][j];
				if(curItem != null) {
					candWordSize[curItem.s+1] += 1;
				}
				else
					break;
			}
		}
		//统计以每个字开始的Item的最大数目
		int max = 0;
		for(int i=0; i<length; i++) {
			if(max < candWordSize[i])
				max = candWordSize[i];
		}
		return max;    	
    }
    
    /** 将以词开头字对齐的词 转换为 词结尾字对齐的词 */
    public Lattice flap2WordEndLattice() {
    	
    	int length = lattice.length; 
    	int maxWordEnd = getMaxWordEnd();
		//存储
		int[] curWordSize = new int[length];
		Item[][] newLattice = new Item[length][maxWordEnd];
		for(int i=0; i<length; i++) {
			int size = lattice[i].length;
			for(int j=0; j<size; j++) {
				Item curItem = lattice[i][j];
				if(curItem != null) {
					newLattice[curItem.t][curWordSize[curItem.t]++] = lattice[i][j];
				}
				else
					break;
			}
		}
		Lattice nLattice =  new Lattice(0, length-1, maxWordEnd);
		nLattice.lattice = newLattice;
		return nLattice;    	   	
    }
    
    /** 得到每个字位置结束的最大词数 */
    public int getMaxWordEnd() {
    	
    	int length = lattice.length;               //长度，字数长+1
		//统计以每个字开始的Item的数目
		int[] candWordSize = new int[length];      //记录每个字开始的Item的个数
		for(int i=0; i<length; i++) { 
			int size = lattice[i].length;          //记录每个字结束的Item的最多个数
			for(int j=0; j<size; j++) {
				Item curItem = lattice[i][j];
				if(curItem != null) {
					candWordSize[curItem.t] += 1;
				}
				else
					break;
			}
		}
		//统计以每个字开始的Item的最大数目
		int max = 0;
		for(int i=0; i<length; i++) {
			if(max < candWordSize[i])
				max = candWordSize[i];
		}
		return max;
    }
    
    
    public static Lattice readWPLattice(Reader reader) throws IOException {
    	
    	String line = reader.inputReader.readLine();
    	if(line != null && line.trim().length() > 0) {
    		String[] parts = line.split("\\s+");
    		int latticeLength = Integer.parseInt(parts[1]);
    		int size = Integer.parseInt(parts[2]);
    		Lattice newLattice = new Lattice(0, latticeLength-1, size);
    		line = reader.inputReader.readLine();
    		int[] curSize = new int[latticeLength];
    		while( line != null && line.trim().length() > 0) {
    			parts = line.split("\\s+");
    			int s = Integer.parseInt(parts[0]);
    			int t = Integer.parseInt(parts[1]);
    			String form = parts[2];
    			String tag = parts[3];
    			double prob = Double.parseDouble(parts[4]);
    			double curProb = Double.parseDouble(parts[5]);
    			Item item = new Item(s, t, form, tag, prob, null, null);
    			item.curProb = curProb;
    			newLattice.lattice[s+1][curSize[s+1]++] = item;
    			line = reader.inputReader.readLine();
    		}    
    		return newLattice;
    	}
    	else {
    		reader.inputReader.close();
    		return null;
    	}
    }
    
    
    /*过滤掉概率小于prob的Item*/
    public int filter(double prob) {
    	int allSize = 0;      //所有Item的数目
    	int length = lattice.length;
    	int size = lattice[0].length;
    	for(int i=0; i<length; i++) {
    		for(int j=0; j<size; j++) {
    			Item item = lattice[i][j];
    			if(item != null) {
    				if(item.curProb < prob) {
    					lattice[i][j] = null;
    					for(int k=j; k+1 < size && lattice[i][k+1] != null; k++) {
    						lattice[i][k] = lattice[i][k+1];
    						lattice[i][k+1] = null; 
    					}
    				}
    				if(lattice[i][j] != null)
    					allSize++;
    			}
    			else
    				break;
    		}
    	}
    	return allSize;
    }
    
    
    public static void combLattice(String firstLatticeFileName, String secondLatticeFileName,
    		String combLatticeFileName) throws IOException {
    	
    	System.out.print("Processing Sentence: ");
    	Options options = new Options();
    	CharPipe firstPipe = new CharPipe(options);
    	firstPipe.initInputFile(firstLatticeFileName);
    	firstPipe.initOutputFile(combLatticeFileName);
    	Lattice firstLattice = firstPipe.readWPLattice();
    	
    	CharPipe secondPipe = new CharPipe(options);
    	secondPipe.initInputFile(secondLatticeFileName);
    	Lattice secondLattice = secondPipe.readWPLattice();
    	
    	int cnt = 0;
    	while(firstLattice != null && secondLattice != null) {
    		System.out.println(cnt+".");
    		Lattice combLattice = combOr(firstLattice, secondLattice);
    		firstPipe.outputWPLattice(combLattice, cnt);
    		
    		firstLattice = firstPipe.readWPLattice();
    		secondLattice = secondPipe.readWPLattice();
    		cnt++;
    	}
    	firstPipe.close();
    	
    }
    
    /** 合并两个lattice，去掉重复项 */
    public static Lattice combOr(Lattice firstLattice, Lattice secondLattice) {
    	
    	if(firstLattice != null && secondLattice != null) {
    		Item[][] firstlattice = firstLattice.lattice;
    		Item[][] secondlattice = secondLattice.lattice;
    		if(firstlattice != null && secondlattice != null) {
    			//长度
	    		int length = firstlattice.length;
	    		//column，词个数
	    		int maxWordBgn = firstlattice[0].length + secondlattice[0].length;
	    		Item[][] newLattice = new Item[length][maxWordBgn];
	    		for(int i=0; i<length; i++) {
	    			Set<Item> items = new HashSet<Item>();
	    			for(Item item : firstlattice[i]) {
	    				if(item != null)	
	    					items.add(item);
	    				else
	    					break;
	    			}	    				
	    			for(Item item : secondlattice[i]){
	    				if(item != null ) {	
	    					if(!items.contains(item))
	    						items.add(item);
	    				}
	    				else
	    					break;
	    			}	    				
		    		int j=0;
		    		for(Item item : items)
		    			newLattice[i][j++] = item;
	    		}
	    		Lattice nLattice =  new Lattice(0, length-1, maxWordBgn);
	    		nLattice.lattice = newLattice;	    
	    		return nLattice;
    		}
    	}
    	return null;       	
    }
    
 
    public static void evalLattice(String goldFileName, String latticeFileName) throws IOException {

    	long start = System.currentTimeMillis();

    	int goldNumber = 0;
    	int allPredictNumber = 0;
    	int wordpredictNumber = 0;
    	int wordpospredictNumber = 0;
    	int jiangpredictNumber = 0;    	
    	
    	Options options = new Options();
    	CharPipe pipe = new CharPipe(options);
    	Parser parser = new Parser(pipe, options);    	
    	
    	CharPipe goldPipe = new CharPipe(options);
    	goldPipe.initInputFile(goldFileName);
    	System.out.print("Processing Sentence: ");
    	Instance ginst = goldPipe.nextInstance();
    	
		parser.charPipe.initInputFile(latticeFileName);
		Lattice frontLattice = parser.charPipe.readWPLattice();
		
		int cnt = 0;
		while(frontLattice != null && ginst != null) {
			
			System.out.println("sentence: " + cnt);
		    ArrayList<BasicWordPos> goldWordPos = ginst.getWordPos();
		    goldNumber += goldWordPos.size();
		    int length = ginst.charLength();
		    
		    Lattice endLattice = frontLattice.flap2WordEndLattice();
		    //endLattice.filter(-20.0d);
		    int predictNumber = endLattice.itemNumbers();
		    allPredictNumber += predictNumber;
			cnt++;
			
			ArrayList<ArrayList<BasicWordPos>> allWordPos = endLattice.getWordPos();
		    wordpredictNumber += Oracle.compOracleWordSimple(allWordPos, goldWordPos);
		    wordpospredictNumber += Oracle.compOracleWordPosSimple(allWordPos, goldWordPos);
		    jiangpredictNumber += Oracle.compOracleWordPosJiang(allWordPos, goldWordPos, length);			    
		   	
		    frontLattice = parser.charPipe.readWPLattice();	
		    ginst = goldPipe.nextInstance();
		    System.out.println("goldNumber: " + goldNumber+"\n"+
		    		"wordpredictNumber: " + wordpredictNumber+"\n"+
		    		"wordpospredictNumber: " + wordpospredictNumber+"\n"+
		    		"jiangpredictNumber: " + jiangpredictNumber);
		    		    
		}

		if(goldNumber <= 0)
			System.out.println("computer number is wrong");
		else
	    	System.out.println("goldNumber:\t"+goldNumber+"\n"+
	    			"allPredictNumber:\t"+allPredictNumber+"\n"+
	    			"wordpredictNumber:\t"+wordpredictNumber+"\t"+((double)wordpredictNumber/goldNumber)+ "\n"+
	    			"wordpospredictNumber:\t"+wordpospredictNumber+"\t"+((double)wordpospredictNumber/goldNumber)+"\n"+
	    			"jiangpredictNumber:\t"+jiangpredictNumber+"\t"+((double)jiangpredictNumber/goldNumber)+"\n");
		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end-start));
	}
    
    /** 包含的Item的数目 */
    public int itemNumbers() {
    	int number = 0;
    	int length = lattice.length;
    	int size = lattice[0].length;
    	for(int i=0; i<length; i++) {
    		for(int j=0; j<size; j++) {
    			Item item = lattice[i][j];
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
    	Lattice.evalLattice(goldFileName, newLatticeName);
    }

}
