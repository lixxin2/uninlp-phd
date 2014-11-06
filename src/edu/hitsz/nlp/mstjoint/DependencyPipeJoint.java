package edu.hitsz.nlp.mstjoint;


import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.mstjoint.io.*;
import edu.hitsz.nlp.segpos.BasicWordPos;
import edu.hitsz.nlp.segpos.Instance;
import edu.hitsz.nlp.segpos.Item;
import edu.hitsz.nlp.segpos.Lattice;
import edu.hitsz.nlp.segpos.Parser;
import edu.hitsz.nlp.util.Array;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import gnu.trove.*;


/**
 * 依存的管道（包含：特征map，依存关系map，参数配置，读写类。动作：读取文件，初始化特征）
 * @author tm
 *
 */
public class DependencyPipeJoint {

    public FeatureMap dataAlphabet; //特征map

    public FeatureMap typeAlphabet; //依存关系map

    public DependencyReader depReader;
    private DependencyWriter depWriter;

    public String[] types; //依存关系数组
    //public int[] typesInt;

    public boolean labeled = false;

    public ParserOptionsJoint options;

    /**
     * 初始化参数,
     * @param options
     * @throws IOException
     */
    public DependencyPipeJoint (ParserOptionsJoint options) throws IOException {
		this.options = options;

		dataAlphabet = new FeatureMap();
		typeAlphabet = new FeatureMap();

		depReader = DependencyReader.createDependencyReader(options.format, options.discourseMode);
    }

    /**
     * 初始化写入文件
     * @param file
     * @throws IOException
     */
    public void initInputFile (String file) throws IOException {
    	labeled = depReader.startReading(file);
    }

    /**
     * 初始化输出文件
     * @param file
     * @throws IOException
     */
    public void initOutputFile (String file) throws IOException {
		depWriter =
		    DependencyWriter.createDependencyWriter(options.format, labeled);
		depWriter.startWriting(file);
    }

    /**
     * 输出一个依存句子
     * @param instance
     * @throws IOException
     */
    public void outputInstance (DependencyInstanceJoint instance) throws IOException {
    	depWriter.write(instance);
    }

    /**
     * 输出器关闭
     * @throws IOException
     */
    public void close () throws IOException {
		if (null != depWriter) {
		    depWriter.finishWriting();
		}
    }

    public String getType (int typeIndex) {
    	return types[typeIndex];
    }

    /**
     * 获取下一个依存实例，并初始化特征（数字化特征，并初始化为1.0)
     * @return
     * @throws IOException
     */
    protected final DependencyInstanceJoint nextInstance() throws IOException {
		DependencyInstanceJoint instance = depReader.getNext();
		if (instance == null || instance.forms == null) return null;

		instance.setFeatureVector(createFeatureVector(instance, true));

		String[] labs = instance.deprels;
		int[] heads = instance.heads;

		StringBuffer spans = new StringBuffer(heads.length*5);
		for(int i = 1; i < heads.length; i++) {
		    spans.append(heads[i]).append("|").append(i).append(":").append(typeAlphabet.get(labs[i])).append(" ");
		}
		instance.actParseTree = spans.substring(0,spans.length()-1);

		return instance;
    }
  
    
    /**
     * 生成依存实例,生成所有实例中存在的特征向量，存储依存森林（没有所有的可能特征）
     * @param file 训练文件名称
     * @param featFileName 森林文件名称
     * @return 句子长度组成的数组
     * @throws IOException
     */
    public int[] createInstances(String file,
				 String featFileName) throws IOException {

		createAlphabet(file);

		System.out.println("Num Features: " + dataAlphabet.size());

		labeled = depReader.startReading(file);

		TIntArrayList lengths = new TIntArrayList();

		ObjectOutputStream out = options.createForest
		    ? new ObjectOutputStream(new FileOutputStream(new File(featFileName)))
		    : null;

		DependencyInstanceJoint instance = depReader.getNext();
		int num1 = 0;

		System.out.println("Creating Feature Vector Instances: ");
		while(instance != null) {
			if(num1 % 10 == 0)
				System.out.print(num1 + " ");

		    instance.setFeatureVector(createFeatureVector(instance, true));

		    String[] labs = instance.deprels;
		    int[] heads = instance.heads;

		    StringBuffer spans = new StringBuffer(heads.length*5);
		    for(int i = 1; i < heads.length; i++) {
		    	spans.append(heads[i]).append("|").append(i).append(":").append(typeAlphabet.get(labs[i])).append(" ");
		    }
		    instance.actParseTree = spans.substring(0,spans.length()-1);

		    lengths.add(instance.length());

		    if(options.createForest)
		    	writeInstanceLess(instance,out);
		    
		    //System.out.println(" "+dataAlphabet.size()+" ");
		    
		    instance = null;

		    instance = depReader.getNext();

		    num1++;
		}

		System.out.println();

		closeAlphabets();

		if(options.createForest)
		    out.close();

		return lengths.toNativeArray();

    }
       
    
    /**
     * 生成训练文件的依存实例,生成并存储所有的word lattice，初始特征是遍历文件得到的
     * @param file 训练文件名称
     * @param latticeFileName 
     * @param forwardCharParser
     * @param backwardCharParser
     * @param latticeK
     * @return
     * @throws IOException
     */
    public int[] createInstancesJoint(String file, 
    		String latticeFileName, 
    		Parser forwardCharParser,
    		Parser backwardCharParser,
    		int latticeK) throws IOException {

		//int sentenceNumber = createAlphabet(file);
    	int[] sentenceLengths = getSentenceNumber(file);
    	int sentenceNumber = sentenceLengths.length;
		System.out.println("Num Features: " + dataAlphabet.size()+", sentence number is "+sentenceNumber);
		labeled = depReader.startReading(file);
		TIntArrayList lengths = new TIntArrayList();
		ObjectOutputStream out = options.createForest
		    ? new ObjectOutputStream(new FileOutputStream(new File(latticeFileName)))
		    : null;
		if(options.createForest) {
			out.writeObject(sentenceLengths);
			out.writeObject(sentenceNumber);
		}
		DependencyInstanceJoint instance = depReader.getNext();
		int num1 = 0;
		System.out.println("Creating Feature Vector Instances: ");
		while(instance != null) {
			if(num1 % 100 == 0) {
				System.out.print(num1 + " ");
				if(num1 % 1000 == 0)
					System.out.println();
			}
		    instance.setFeatureVector(createFeatureVector(instance, true));		    	    
		    int length = 1;		    
		    for(int i = 1; i < instance.heads.length; i++) {
		    	length += instance.forms[i].length();
		    }
		    lengths.add(length);
		    if(options.createForest)
		    	writeInstanceJoint(instance, out, forwardCharParser, backwardCharParser, latticeK);			
		    instance = null;
		    instance = depReader.getNext();
		    num1++;
		}	    	    
		closeAlphabets();

		if(options.createForest)
		    out.close();
		
		return lengths.toNativeArray();
		
    }
    
     
    
    /**
     * 生成测试文件的word lattice
     * @since Feb 21, 2013
     * @param file
     * @param latticeFileName
     * @param forwardCharParser
     * @param backwardCharParser
     * @param latticeK
     * @return
     * @throws IOException
     */
    public int[] createTestInstancesJoint(String file,
			 String latticeFileName, 
			 Parser forwardCharParser,
			 Parser backwardCharParser,
			 int latticeK) throws IOException {
    	
    	int[] sentenceLengths = getRawSentenceNumber(file);
    	int sentenceNumber = sentenceLengths.length;
    	System.out.println("sentence number is "+sentenceNumber);
		labeled = depReader.startReading(file);
		TIntArrayList lengths = new TIntArrayList();
		ObjectOutputStream out = options.createForest
		    ? new ObjectOutputStream(new FileOutputStream(new File(latticeFileName)))
		    : null;
		//存储文件中句子数目和每个句子的字数
	    if(options.createForest) {
	    	out.writeObject(sentenceLengths);
			out.writeInt(sentenceNumber);
	    }
		DependencyInstanceJoint instance = depReader.getNext();
		int num1 = 0;
		//
		System.out.println("Creating Feature Vector Instances: ");
		while(instance != null) {
		    System.out.print(num1 + " ");
		    instance.setFeatureVector(new FeatureVector());	    
		    int length = 1;		    
		    for(int i = 1; i < instance.forms.length; i++) {
		    	length += instance.forms[i].length();
		    }
		    
		    lengths.add(length);
		    //存储每个句子对应的word lattice
		    if(options.createForest)
		    	writeInstanceJoint(instance, out, forwardCharParser, backwardCharParser, latticeK);		    
		    System.out.println(" "+dataAlphabet.size()+" ");		    
		    instance = null;
		    instance = depReader.getNext();
		    num1++;
		}
		System.out.println();
		//closeAlphabets();
	
		if(options.createForest)
		    out.close();
	
		return lengths.toNativeArray();

    }
   

    /**
     * 读取文件，初始化特征，label types map
     * 生成特征 in golden sentence (not all possible features)
     * @param file
     * @throws IOException
     */
    private int createAlphabet(String file) throws IOException {

		System.out.print("Creating Alphabet ");

		labeled = depReader.startReading(file);

		DependencyInstanceJoint instance = depReader.getNext();
		
		int sentenceNumber = 0;
		
		while(instance != null) {
			//依存关系
			if(sentenceNumber % 10 == 0)
				System.out.print(sentenceNumber+" ");
			sentenceNumber ++;
		    String[] labs = instance.deprels;
		    for(int i = 0; i < labs.length; i++)
		    	typeAlphabet.add(labs[i]);
		    //特征
		    createFeatureVector(instance, true);

		    instance = depReader.getNext();
		}

		closeAlphabets();

		System.out.println("Done.");
		
		return sentenceNumber;
    }
    
    
    
    /**
     * 获得文件中句子的数目
     * @since Mar 28, 2012
     * @param file
     * @return
     * @throws IOException
     */
    public int[] getSentenceNumber(String file) throws IOException {

		System.out.print("Getting sentence number ... ");
		labeled = depReader.startReading(file);
		DependencyInstanceJoint instance = depReader.getNext();	

		TIntArrayList lengths = new TIntArrayList();

		while(instance != null) {
			//依存关系
			String[] labs = instance.deprels;
		    for(int i = 0; i < labs.length; i++)
		    	typeAlphabet.add(labs[i]);
		    
			lengths.add(instance.length());		    
		    instance = null;
		    instance = depReader.getNext();

		}
		
		return lengths.toNativeArray();
		
    }
    
    /**
     * 获取原始文件中句子
     * @since Jan 11, 2013
     * @param file
     * @return
     * @throws IOException
     */
    private int[] getRawSentenceNumber(String file) throws IOException {

		System.out.print("Getting sentence number ... ");
		labeled = depReader.startReading(file);
		DependencyInstanceJoint instance = depReader.getNext();	
		
		TIntArrayList lengths = new TIntArrayList();
		
		while(instance != null) {
			lengths.add(instance.length());	
			instance = null;
		    instance = depReader.getNext();
		}
		return lengths.toNativeArray();
		
    }
    
  
    /**
     * 关闭特征map和依存关系map,将依存关系从map存到数组
     */
    public void closeAlphabets() {
		//dataAlphabet.stopGrowth();
		//typeAlphabet.stopGrowth();

    	/*
		types = new String[typeAlphabet.size()];		
		Object[] keys = typeAlphabet.toArray();
		for(int i = 0; i < keys.length; i++) {
		    int indx = typeAlphabet.lookupIndex(keys[i]);
		    types[indx] = (String)keys[i];
		}
		*/
		HashMap<String, Integer> tagsMap = typeAlphabet.getMap();
		int typeNumber = tagsMap.size();
		types = new String[typeNumber];
		Iterator<Entry<String, Integer>> iter = tagsMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, Integer> entry =  iter.next();
			types[entry.getValue()] = entry.getKey();
		}
		KBestParseForest.rootType = typeAlphabet.get("<root-type>");
    }


    /**
     * add with default 1.0
     * @param feat
     * @param fv
     */
    public final void add(String feat, FeatureVector fv, boolean b) {
    	if(b == true) {
			int num = dataAlphabet.add(feat);
			if(num >= 0)
			    fv.add(num);
    	}
    	else {
    		//fv.addString(feat);
    		int num = dataAlphabet.get(feat);
    		if(num >= 0)
			    fv.add(num);    		
    	}    	
    }

    /*
    public final void add(String feat, double val, FeatureVector fv) {
		int num = dataAlphabet.lookupIndex(feat);
		if(num >= 0)
		    fv.add(num, val);
    }
    */
	

    /**
     * 从依存实例创造特征向量,把所有的特征都生成出来 (only those in sentences, not all possible ones)
     * @param instance
     * @param added true:表示添加所有特征，把新特征加到特征集dataAlphabet;false:表示只添加特征集dataAlphabet中有的特征
     * @return
     */
    public FeatureVector createFeatureVector(DependencyInstanceJoint instance, boolean added) {

		final int instanceLength = instance.length();

		String[] labs = instance.deprels;
		int[] heads = instance.heads;

		FeatureVector fv = new FeatureVector();
		
		for(int i = 0; i < instanceLength; i++) {
		    if(heads[i] == -1)
		    	continue;
		    int small = i < heads[i] ? i : heads[i]; //该节点和其头节点两者最左边的
		    int large = i > heads[i] ? i : heads[i]; //该节点和其头节点两者最右边的
		    boolean attR = i < heads[i] ? false : true; //该节点是否位于其头节点的右边
		    addCoreFeatures(instance,small,large,attR,fv, added);
		    if(labeled) {
				addLabeledFeatures(instance,i,labs[i],attR,true,fv, added);
				addLabeledFeatures(instance,heads[i],labs[i],attR,false,fv, added);
		    }
		}

		addExtendedFeatures(instance, fv, added);

		return fv;
    }
       

    protected void addExtendedFeatures(DependencyInstanceJoint instance,
				       FeatureVector fv, boolean added) {}

    


    /**
     * 主要的特征向量
     * @param instance
     * @param small
     * @param large
     * @param attR
     * @param fv
     */
    public void addCoreFeatures(DependencyInstanceJoint instance,
				int small,
				int large,
				boolean attR,
				FeatureVector fv,
				boolean added) {

		String[] forms = instance.forms;
		String[] pos = instance.postags;
		String[] posA = instance.cpostags;

		//依存方向
		String att = attR ? "RA" : "LA";

		//距离
		int dist = Math.abs(large-small);
		String distBool = "0";
		if (dist > 10)
		    distBool = "10";
		else if (dist > 5)
		    distBool = "5";
		else
		    distBool = Integer.toString(dist-1);
		
		String attDist = "&"+att+"&"+distBool;

		addLinearFeatures("POS", pos, small, large, attDist, fv, added);
		addLinearFeatures("CPOS", posA, small, large, attDist, fv, added);


		//////////////////////////////////////////////////////////////////////

		int headIndex = small;
		int childIndex = large;
		if (!attR) {
		    headIndex = large;
		    childIndex = small;
		}

		addTwoObsFeatures("HC", forms[headIndex], pos[headIndex],
				  forms[childIndex], pos[childIndex], attDist, fv, added);

		/*
	    int hL = forms[headIndex].length();
	    int cL = forms[childIndex].length();
	    if (hL > 2 || cL > 2) {
		addOldMSTStemFeatures(instance.lemmas[headIndex],
				      pos[headIndex],
				      instance.lemmas[childIndex],
				      pos[childIndex],
				      attDist, hL, cL, fv, added);
	    }
	    */
		

    }
    

    
    
    /**
     * 主要的特征向量
     * @param instance
     * @param small
     * @param large
     * @param attR
     * @param fv
     */
    public void addCoreFeaturesJoint(DependencyInstanceJoint instance,
    		BasicWordPos leftleft, BasicWordPos left, BasicWordPos leftright,
    		BasicWordPos rightleft, BasicWordPos right, BasicWordPos rightright,
    		ArrayList<String> midPos,
    		boolean attR, FeatureVector fv, boolean added) {
    		
		//依存方向
		String att = attR ? "RA" : "LA";
		
		//距离
		int dist = 1;
		if(midPos != null)
			dist += midPos.size();
		String distBool = "0";
		if (dist > 10)
		    distBool = "10";
		else if (dist > 5)
		    distBool = "5";
		else
		    distBool = Integer.toString(dist-1);
		
		String attDist = "&"+att+"&"+distBool;

		addLinearFeaturesJoint("POS", leftleft, left, leftright, rightleft, right, rightright,
	    		midPos, attDist, fv, added);
		addLinearFeaturesJointCoarse("CPOS",leftleft, left, leftright, rightleft, right, rightright,
	    		midPos, attDist, fv, added);


		//////////////////////////////////////////////////////////////////////
		
		BasicWordPos headIndex = left;
		BasicWordPos childIndex = right;
		if(!attR) {
			headIndex = right;
			childIndex = left;
		}

		addTwoObsFeatures("HC", headIndex.getWord(), headIndex.getPos(),
				  childIndex.getWord(), childIndex.getPos(), attDist, fv, added);

		
    }
    
  
    
    

    /**
     * pos线性特征,两词之间词的特征（p-pos,b-pos,c-pos），和两词的特征(addCorePosFeatures)
     * @param type pos或者cpos
     * @param obsVals 词性
     * @param first
     * @param second
     * @param attachDistance
     * @param fv
     */
    private final void addLinearFeatures(String type, String[] obsVals,
					 int first, int second,
					 String attachDistance,
					 FeatureVector fv,
					 boolean added) {
    	//第一个词的左边词
		String pLeft = first > 0 ? obsVals[first-1] : "STR";
		//第二个词的右边词
		String pRight = second < obsVals.length-1 ? obsVals[second+1] : "END";
		//第一个词的右边词
		String pLeftRight = first < second-1 ? obsVals[first+1] : "MID";
		//第二个词的左边词
		String pRightLeft = second > first+1 ? obsVals[second-1] : "MID";

		// feature posR posMid posL
		StringBuilder featPos =
		    new StringBuilder(type+"PC="+obsVals[first]+" "+obsVals[second]);

		for(int i = first+1; i < second; i++) {
		    String allPos = featPos.toString() + ' ' + obsVals[i];
		    add(allPos, fv, added);
		    add(allPos+attachDistance, fv, added);

		}

		addCorePosFeatures(type+"PT", pLeft, obsVals[first], pLeftRight,
					pRightLeft, obsVals[second], pRight, attachDistance, fv, added);

    }
    
    
    /**
     * pos线性特征,两词之间词的特征（p-pos,b-pos,c-pos），和两词的特征(addCorePosFeatures)
     * @param type pos或者cpos
     * @param obsVals 词性
     * @param first
     * @param second
     * @param attachDistance
     * @param fv
     */
    private final void addLinearFeaturesJoint(String type, 
    		BasicWordPos leftleft, BasicWordPos left, BasicWordPos leftright,
    		BasicWordPos rightleft, BasicWordPos right, BasicWordPos rightright,
    		ArrayList<String> midPos,
    		String attachDistance,
			FeatureVector fv,
			boolean added) {
    		
    	//第一个词的左边词
		String pLeft = leftleft != null ? leftleft.getPos() : "STR";
		//第二个词的右边词
		String pRight = rightright != null ? rightright.getPos() : "END";
		//第一个词的右边词
		String pLeftRight = leftright != null && leftright.getEnd() < right.getStart() ? leftright.getPos() : "MID";
		//第二个词的左边词
		String pRightLeft = rightleft !=null && rightleft.getStart() > left.getEnd() ? rightleft.getPos() : "MID";
		
		String leftPos = left.getPos();		
		String rightPos = right.getPos();

		// feature posR posMid posL
		StringBuilder featPos =
		    new StringBuilder(type+"PC="+leftPos+" "+rightPos);

		if(midPos != null) {
			for(String mid : midPos) {
			    String allPos = featPos.toString() + ' ' + mid;
			    add(allPos, fv, added);
			    add(allPos+attachDistance, fv, added);
			}
		}

		addCorePosFeatures(type+"PT", pLeft, leftPos, pLeftRight,
					pRightLeft, rightPos, pRight, attachDistance, fv, added);

    }

    /**
     * pos线性特征,两词之间词的特征（p-pos,b-pos,c-pos），和两词的特征(addCorePosFeatures)
     * @param type pos或者cpos
     * @param obsVals 词性
     * @param first
     * @param second
     * @param attachDistance
     * @param fv
     */
    private final void addLinearFeaturesJointCoarse(String type, 
    		BasicWordPos leftleft, BasicWordPos left, BasicWordPos leftright,
    		BasicWordPos rightleft, BasicWordPos right, BasicWordPos rightright,
    		ArrayList<String> midPos,
    		String attachDistance,
			FeatureVector fv,
			boolean added) {
    		
    	//第一个词的左边词
		//String pLeft = leftleft != null ? leftleft.getPos().substring(0,1) : "STR";
		String pLeft = "STR";
		if(leftleft != null) {
			pLeft = leftleft.getPos().substring(0,1);
			if(pLeft.equals("<"))
				pLeft = "<root-CPOS>";
		}
		//第二个词的右边词
		String pRight = rightright != null ? rightright.getPos().substring(0,1) : "END";
		//第一个词的右边词
		String pLeftRight = leftright != null && leftright.getEnd() < right.getStart() ? leftright.getPos().substring(0,1) : "MID";
		//第二个词的左边词
		String pRightLeft = rightleft !=null && rightleft.getStart() > left.getEnd() ? rightleft.getPos().substring(0,1) : "MID";
		
		String leftPos = left.getPos().substring(0,1);		
		if(leftPos.equals("<"))
			leftPos = "<root-CPOS>";
		String rightPos = right.getPos().substring(0,1);

		// feature posR posMid posL
		StringBuilder featPos =
		    new StringBuilder(type+"PC="+leftPos+" "+rightPos);

		if(midPos != null) {
			for(String mid : midPos) {
			    String allPos = featPos.toString() + ' ' + mid.substring(0,1);
			    add(allPos, fv, added);
			    add(allPos+attachDistance, fv, added);
			}
		}

		addCorePosFeatures(type+"PT", pLeft, leftPos, pLeftRight,
					pRightLeft, rightPos, pRight, attachDistance, fv, added);

    }


    private final void
	addCorePosFeatures(String prefix,
			   String leftOf1, String one, String rightOf1,
			   String leftOf2, String two, String rightOf2,
			   String attachDistance,
			   FeatureVector fv,
			   boolean added) {

		// feature posL-1 posL posR posR+1

		add(prefix+"="+leftOf1+" "+one+" "+two+"*"+attachDistance, fv, added);

		StringBuilder feat =
		    new StringBuilder(prefix+"1="+leftOf1+" "+one+" "+two);
		add(feat.toString(), fv, added);
		feat.append(' ').append(rightOf2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2="+leftOf1+" "+two+" "+rightOf2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"3="+leftOf1+" "+one+" "+rightOf2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"4="+one+" "+two+" "+rightOf2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		/////////////////////////////////////////////////////////////
		prefix = "A"+prefix;

		// feature posL posL+1 posR-1 posR
		add(prefix+"1="+one+" "+rightOf1+" "+leftOf2+"*"+attachDistance, fv, added);

		feat = new StringBuilder(prefix+"1="+one+" "+rightOf1+" "+leftOf2);
		add(feat.toString(), fv, added);
		feat.append(' ').append(two);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2="+one+" "+rightOf1+" "+two);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"3="+one+" "+leftOf2+" "+two);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"4="+rightOf1+" "+leftOf2+" "+two);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		///////////////////////////////////////////////////////////////
		prefix = "B"+prefix;

		//// feature posL-1 posL posR-1 posR
		feat = new StringBuilder(prefix+"1="+leftOf1+" "+one+" "+leftOf2+" "+two);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		//// feature posL posL+1 posR posR+1
		feat = new StringBuilder(prefix+"2="+one+" "+rightOf1+" "+two+" "+rightOf2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

    }



    /**
     * Add features for two items, each with two observations, e.g. head,
     * head pos, child, and child pos.
     * <p> 两结点相互的word+pos特征(p-word,p-pos,c-word,c-pos)
     *
     * The use of StringBuilders is not yet as efficient as it could
     * be, but this is a start. (And it abstracts the logic so we can
     * add other features more easily based on other items and
     * observations.)
     **/
    private final void addTwoObsFeatures(String prefix,
					 String item1F1, String item1F2,
					 String item2F1, String item2F2,
					 String attachDistance,
					 FeatureVector fv,
					 boolean added) {
    	//p-word
		StringBuilder feat = new StringBuilder(prefix+"2FF1="+item1F1);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);
		//p-word,p-pos
		feat = new StringBuilder(prefix+"2FF1="+item1F1+" "+item1F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);
		//p-word,p-pos,c-pos
		feat = new StringBuilder(prefix+"2FF1="+item1F1+" "+item1F2+" "+item2F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);
		//p-word,p-pos,c-pos,c-word
		feat = new StringBuilder(prefix+"2FF1="+item1F1+" "+item1F2+" "+item2F2+" "+item2F1);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);
		//p-word,c-word
		feat = new StringBuilder(prefix+"2FF2="+item1F1+" "+item2F1);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);
		//p-word,c-pos
		feat = new StringBuilder(prefix+"2FF3="+item1F1+" "+item2F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		//p-pos,c-word
		feat = new StringBuilder(prefix+"2FF4="+item1F2+" "+item2F1);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);
		//p-pos,c-word,c-pos
		feat = new StringBuilder(prefix+"2FF4="+item1F2+" "+item2F1+" "+item2F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2FF5="+item1F2+" "+item2F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2FF6="+item2F1+" "+item2F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2FF7="+item1F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2FF8="+item2F1);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

		feat = new StringBuilder(prefix+"2FF9="+item2F2);
		add(feat.toString(), fv, added);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv, added);

    }

    /**
     * 依存关系的特征
     * @since Apr 5, 2012
     * @param instance
     * @param word 当前词
     * @param type 依存类型
     * @param attR 方向
     * @param childFeatures 是否为子节点
     * @param fv
     * @param added
     */
    public void addLabeledFeatures(DependencyInstanceJoint instance,
				   int word,
				   String type,
				   boolean attR,
				   boolean childFeatures,
				   FeatureVector fv,
				   boolean added) {

		if(!labeled)
		    return;

		String[] forms = instance.forms;
		String[] pos = instance.postags;

		String att = "";
		if(attR)
		    att = "RA";
		else
		    att = "LA";

		att+="&"+childFeatures;

		String w = forms[word];
		String wP = pos[word];

		String wPm1 = word > 0 ? pos[word-1] : "STR";
		String wPp1 = word < pos.length-1 ? pos[word+1] : "END";

		add("NTS1="+type+"&"+att,fv, added);
		add("ANTS1="+type,fv, added);
		for(int i = 0; i < 2; i++) {
		    String suff = i < 1 ? "&"+att : "";
		    suff = "&"+type+suff;

		    add("NTH="+w+" "+wP+suff,fv, added);
		    add("NTI="+wP+suff,fv, added);
		    add("NTIA="+wPm1+" "+wP+suff,fv, added);
		    add("NTIB="+wP+" "+wPp1+suff,fv, added);
		    add("NTIC="+wPm1+" "+wP+" "+wPp1+suff,fv, added);
		    add("NTJ="+w+suff,fv, added); //this

		}
    }

    
    private void addDiscourseFeatures (DependencyInstanceJoint instance,
				       int small,
				       int large,
				       int headIndex,
				       int childIndex,
				       String attDist,
				       FeatureVector fv,
				       boolean added) {

		addLinearFeatures("FORM", instance.forms, small, large, attDist, fv, added);
		addLinearFeatures("LEMMA", instance.lemmas, small, large, attDist, fv, added);

		addTwoObsFeatures("HCB1", instance.forms[headIndex],
				  instance.lemmas[headIndex],
				  instance.forms[childIndex],
				  instance.lemmas[childIndex],
				  attDist, fv, added);

		addTwoObsFeatures("HCB2", instance.forms[headIndex],
				  instance.lemmas[headIndex],
				  instance.forms[childIndex],
				  instance.postags[childIndex],
				  attDist, fv, added);

		addTwoObsFeatures("HCB3", instance.forms[headIndex],
				  instance.lemmas[headIndex],
				  instance.forms[childIndex],
				  instance.cpostags[childIndex],
				  attDist, fv, added);

		addTwoObsFeatures("HC2", instance.forms[headIndex],
				  instance.postags[headIndex],
				  instance.forms[childIndex],
				  instance.cpostags[childIndex], attDist, fv, added);

		addTwoObsFeatures("HCC2", instance.lemmas[headIndex],
				  instance.postags[headIndex],
				  instance.lemmas[childIndex],
				  instance.cpostags[childIndex],
				  attDist, fv, added);


		//// Use this if your extra feature lists all have the same length.
		for (int i=0; i<instance.feats.length; i++) {

			addLinearFeatures("F"+i, instance.feats[i], small, large, attDist, fv, added);

			addTwoObsFeatures("FF"+i,
					  instance.forms[headIndex],
					  instance.feats[i][headIndex],
					  instance.forms[childIndex],
					  instance.feats[i][childIndex],
					  attDist, fv, added);

			addTwoObsFeatures("LF"+i,
					  instance.lemmas[headIndex],
					  instance.feats[i][headIndex],
					  instance.lemmas[childIndex],
					  instance.feats[i][childIndex],
					  attDist, fv, added);

			addTwoObsFeatures("PF"+i,
					  instance.postags[headIndex],
					  instance.feats[i][headIndex],
					  instance.postags[childIndex],
					  instance.feats[i][childIndex],
					  attDist, fv, added);

			addTwoObsFeatures("CPF"+i,
					  instance.cpostags[headIndex],
					  instance.feats[i][headIndex],
					  instance.cpostags[childIndex],
					  instance.feats[i][childIndex],
					  attDist, fv, added);


			for (int j=i+1; j<instance.feats.length; j++) {

			    addTwoObsFeatures("CPF"+i+"_"+j,
					      instance.feats[i][headIndex],
					      instance.feats[j][headIndex],
					      instance.feats[i][childIndex],
					      instance.feats[j][childIndex],
					      attDist, fv, added);

			}

			for (int j=0; j<instance.feats.length; j++) {

			    addTwoObsFeatures("XFF"+i+"_"+j,
					      instance.forms[headIndex],
					      instance.feats[i][headIndex],
					      instance.forms[childIndex],
					      instance.feats[j][childIndex],
					      attDist, fv, added);

			    addTwoObsFeatures("XLF"+i+"_"+j,
					      instance.lemmas[headIndex],
					      instance.feats[i][headIndex],
					      instance.lemmas[childIndex],
					      instance.feats[j][childIndex],
					      attDist, fv, added);

			    addTwoObsFeatures("XPF"+i+"_"+j,
					      instance.postags[headIndex],
					      instance.feats[i][headIndex],
					      instance.postags[childIndex],
					      instance.feats[j][childIndex],
					      attDist, fv, added);


			    addTwoObsFeatures("XCF"+i+"_"+j,
					      instance.cpostags[headIndex],
					      instance.feats[i][headIndex],
					      instance.cpostags[childIndex],
					      instance.feats[j][childIndex],
					      attDist, fv, added);


			}

		}


		// Test out relational features
		if (options.useRelationalFeatures) {

		    //for (int rf_index=0; rf_index<2; rf_index++) {
		    for (int rf_index=0;
			 rf_index<instance.relFeats.length;
			 rf_index++) {

			String headToChild =
			    "H2C"+rf_index+instance.relFeats[rf_index].getFeature(headIndex, childIndex);

			addTwoObsFeatures("RFA1",
					  instance.forms[headIndex],
					  instance.lemmas[headIndex],
					  instance.postags[childIndex],
					  headToChild,
					  attDist, fv, added);

			addTwoObsFeatures("RFA2",
					  instance.postags[headIndex],
					  instance.cpostags[headIndex],
					  instance.forms[childIndex],
					  headToChild,
					  attDist, fv, added);

		    	addTwoObsFeatures("RFA3",
					  instance.lemmas[headIndex],
					  instance.postags[headIndex],
					  instance.forms[childIndex],
					  headToChild,
					  attDist, fv, added);

		    	addTwoObsFeatures("RFB1",
					  headToChild,
					  instance.postags[headIndex],
					  instance.forms[childIndex],
					  instance.lemmas[childIndex],
					  attDist, fv, added);

		    	addTwoObsFeatures("RFB2",
					  headToChild,
					  instance.forms[headIndex],
					  instance.postags[childIndex],
					  instance.cpostags[childIndex],
					  attDist, fv, added);

		    	addTwoObsFeatures("RFB3",
					  headToChild,
					  instance.forms[headIndex],
					  instance.lemmas[childIndex],
					  instance.postags[childIndex],
					  attDist, fv, added);

		    }
		}
    }

    /**从实例中产生所有的特征和概率，用于后面的decoding*/
    public void fillFeatureVectors(DependencyInstanceJoint instance,
				   FeatureVector[][][] fvs,
				   double[][][] probs,
				   FeatureVector[][][][] nt_fvs,
				   double[][][][] nt_probs, Parameters params) {

		final int instanceLength = instance.length();

		// Get production crap.
		for(int w1 = 0; w1 < instanceLength; w1++) {
		    for(int w2 = w1+1; w2 < instanceLength; w2++) {
			for(int ph = 0; ph < 2; ph++) {
			    boolean attR = ph == 0 ? true : false;

			    //int childInt = attR ? w2 : w1;
			    //int parInt = attR ? w1 : w2;

			    FeatureVector prodFV = new FeatureVector();
			    addCoreFeatures(instance,w1,w2,attR, prodFV, false);

			    double prodProb = prodFV.getScore(params); //params.getScore(prodFV);
			    fvs[w1][w2][ph] = prodFV;
			    probs[w1][w2][ph] = prodProb;
			}
		    }
		}

		if(labeled) {
		    for(int w1 = 0; w1 < instanceLength; w1++) {
				for(int t = 0; t < types.length; t++) {
				    String type = types[t];
				    for(int ph = 0; ph < 2; ph++) {

						boolean attR = ph == 0 ? true : false;
						for(int ch = 0; ch < 2; ch++) {

						    boolean child = ch == 0 ? true : false;

						    FeatureVector prodFV = new FeatureVector();
						    addLabeledFeatures(instance,w1,
								       type,attR,child, prodFV, false);

						    double nt_prob = prodFV.getScore(params);//params.getScore(prodFV);
						    nt_fvs[w1][t][ph][ch] = prodFV;
						    nt_probs[w1][t][ph][ch] = nt_prob;

						}
				    }
				}
		    }
		}
    }
    
    
    
    /**
     * 将所有的特征，概率写入文件，mstparser中使用的
     * <p> 将实例写入文件,将所有可能的特征存入out
     *
     **/
    protected void writeInstance(DependencyInstanceJoint instance, ObjectOutputStream out) {

		int instanceLength = instance.length();

		try {
			
		    for(int w1 = 0; w1 < instanceLength; w1++) {
				for(int w2 = w1+1; w2 < instanceLength; w2++) {
				    for(int ph = 0; ph < 2; ph++) {
					boolean attR = ph == 0 ? true : false;
					FeatureVector prodFV = new FeatureVector();
					addCoreFeatures(instance,w1,w2,attR,prodFV, true);
					out.writeObject(prodFV.ints());//keys());
				    }
				}
		    }
		    out.writeInt(-3);

		    if(labeled) {
			for(int w1 = 0; w1 < instanceLength; w1++) {
			    for(int t = 0; t < types.length; t++) {
					String type = types[t];
					for(int ph = 0; ph < 2; ph++) {
					    boolean attR = ph == 0 ? true : false;
					    for(int ch = 0; ch < 2; ch++) {
						boolean child = ch == 0 ? true : false;
						FeatureVector prodFV = new FeatureVector();
						addLabeledFeatures(instance,w1,
								   type, attR,child,prodFV, true);
						out.writeObject(prodFV.ints());//keys());
					    }
					}
			    }
			}
			out.writeInt(-3);
		    }

		    writeExtendedFeatures(instance, out);
			
		    out.writeObject(instance.fv.ints());//keys());
		    out.writeInt(-4);

		    out.writeObject(instance);
		    out.writeInt(-1);

		    out.reset();

		} catch (IOException e) {}

    }
    
    
    /**
     * 只将实例写入文件，不保存特征，特征在读取时产生
     *
     **/
    protected void writeInstanceLess(DependencyInstanceJoint instance, ObjectOutputStream out) {

		try {
			out.writeObject(instance.fv.ints());//keys());
		    out.writeInt(-4);

		    out.writeObject(instance);
		    out.writeInt(-1);

		    out.reset();

		} catch (IOException e) {}

    }

    
    /**
     * <p> 将实例写入文件,将产生的word lattice存入out,特征在读取时产生
     * <p> 
     *
     **/
    protected void writeInstanceJoint(DependencyInstanceJoint instance, 
    		ObjectOutputStream out,
    		Parser forwardCharParser,
    		Parser backwardCharParser,
    		int latticeK) {        	
    	
		try {		
			
			Instance forwardCharInstance = new Instance(instance.forms, instance.postags, true);
	    	Object[] obj = decomposeLattice(forwardCharInstance, forwardCharParser, backwardCharParser, latticeK);
	    	    	
			out.writeObject(obj[0]);			
			out.writeInt(-3);
			out.writeObject(obj[1]);			
			out.writeInt(-3);
			out.writeObject(obj[2]);			
			out.writeInt(-3);			
			out.writeObject(obj[3]);			
			out.writeInt(-3);
			out.writeObject(obj[4]);			
			out.writeInt(-3);
			out.writeObject(obj[5]);			
			out.writeInt(-3);
			out.writeObject(obj[6]);			
			out.writeInt(-3);
		
			out.writeObject(instance.fv.ints());//keys());
			out.writeInt(-4);
			
			out.writeObject(instance);
			out.writeInt(-1);
			
			out.writeObject(forwardCharInstance.getChars());
			out.writeInt(-2);
			
			out.reset();
			

		} catch (IOException e) {}

    }
    
   
    
    
    /**
     * 将当前的pipe状态，保存下来，包括：dataAlphbet, typeAlphabet, types, typesInt
     * @since Mar 28, 2012
     * @param pipeFile
     */
    public void write(String pipeFile) {
    	try{
	    	ObjectOutputStream out = options.createForest
		    ? new ObjectOutputStream(new FileOutputStream(new File(pipeFile)))
		    : null;

		    System.out.println("dataAlphabet:"+dataAlphabet.size());	
			out.writeObject(dataAlphabet);
			out.writeInt(-1);
			System.out.println("typeAlphabet:"+typeAlphabet.size());	
			out.writeObject(typeAlphabet);
			out.writeInt(-1);
			System.out.println("types:"+types.length);	
			out.writeObject(types);
			out.writeInt(-1);
			//System.out.println("typesInt:"+typesInt.length);	
			//out.writeObject(typesInt);			
			//out.writeInt(-1);			
					    
		    out.reset();
			
    	} catch (IOException e) {}

    }
    
    /**
     * 读取存储的训练状态
     * @since Apr 11, 2012
     * @param pipeFile
     * @throws ClassNotFoundException
     */
    public void read(String pipeFile) throws ClassNotFoundException {
    	
    	try{    		
    		ObjectInputStream in = new ObjectInputStream(new FileInputStream(pipeFile));    		
    		
    		dataAlphabet = (FeatureMap) in.readObject();
    		int last = in.readInt();
		    if(last != -1) { System.out.println("Error reading file."); System.exit(0); }
		    typeAlphabet = (FeatureMap) in.readObject();
    		last = in.readInt();
		    if(last != -1) { System.out.println("Error reading file."); System.exit(0); }
		    types = (String[]) in.readObject();
		    last = in.readInt();
		    if(last != -1) { System.out.println("Error reading file."); System.exit(0); }
		    //typesInt = (int[]) in.readObject();
		    //last = in.readInt();
		    //if(last != -1) { System.out.println("Error reading file."); System.exit(0); }
		    						
    	} catch (IOException e) {}
    	
    }
    
    
    public Object[] getCharInfo(Instance charInstance, Parser forwardCharParser) {
    	
    	
    	Lattice allLattice = forwardCharParser.charDecoder.kbeamHeap(forwardCharParser.charParams, charInstance, forwardCharParser.options.oracleK); 	        
    	Object[] charLMRs = forwardCharParser.getCharLMR(charInstance);
    	
    	Object[] objects = new Object[3];
    	objects[0] = allLattice;
    	objects[1] = charLMRs[0]; //allWordPos
    	objects[2] = charLMRs[1]; //linkItems
    	
    	return objects;    	
    }

 
    /**
     * 分解SegPosParser产生的lattice, 获得dependency parser需要的WordPos[][]
     * @since 2012-3-10
     * @return
     */
    public Object[] decomposeLattice(Instance forwardCharInstance, 
    		Parser forwardCharParser,
    		Parser backwardCharParser,
    		int latticeK) {    	
    	//前向parser产生的lattice
    	Lattice forwardLattice = forwardCharParser.charDecoder.kbeamHeap(forwardCharParser.charParams, forwardCharInstance, forwardCharParser.options.oracleK); 
    	Item[][] lattice = forwardLattice.lattice;
    	//后向parser产生的lattice
    	Instance backwardCharInstance = forwardCharInstance.reverse();
		Lattice backwardLattice = backwardCharParser.charDecoder.kbeamHeap(backwardCharParser.charParams, backwardCharInstance, backwardCharParser.options.oracleK);
		backwardLattice.reverseLattice();
		Item[][] backLattice = backwardLattice.lattice;    	
    	return decomposeLattice(lattice, backLattice);
    }
    
    
    /** */
    private Object[] decomposeLattice(Item[][] lattice, Item[][] backwardLattice) {
    	
    	Object[] obj = new Object[7];
    	
    	int length = lattice.length;
    	int K = lattice[0].length;    	

    	//描述从leftWordPoses和rightWordPoses的每一个位置字符中，保存最多WordPos的数目    	
    	int leftMost = K;                              // 当前位置左边的WordPos的数目，由于是前向Parser，所以为Kbeam的K 
    	int[] rightSize = new int[length];             // 当前位置右边的WordPos的数目
    	
    	//找到leftWordPoses和rightWordPoses的每一个位置字符中，保存WordPos的数目
    	for(int i=1; i<length; i++) {    		
    		for(int j=0; j<lattice[i].length; j++) {
    			Item item = lattice[i][j];
    			if(item != null){
    				int start = item.s+1;
    				rightSize[start]++; //统计当前位置右边的数目
    			}
    			else
    				break;
    		}
    	}
    	//找到从leftWordPoses和rightWordPoses的每一个位置字符中，保存最多WordPos的数目
    	int rightMost = 1;                             //当前位置右边的WordPos的数目，通常大于Kbeam的K 
    	for(int i=0; i<length; i++) {
    		if(rightSize[i] > rightMost)
    			rightMost = rightSize[i];
    	}    	
    	
    	/**
    	 *	比如句子： 我们都是一家
    	 *	lattice.length 是从null，0，..., #sentence-1（6－1＝5），等于7
    	 *	所以，allWordPos从<root>, 0, ..., #sentence-1（5），因为包括了头节点，表示从第i到第j个字对应的词（allWordPos[1][2]表示词“我们”）
    	 *	leftWordPos[i]表示从字i开始向左的两个词（比如,leftWordPos[2][0]="们“，leftWordPos[2][1]="我"，或者leftWordPos[2][0]="我们“，leftWordPos[2][1]=null）
    	 *	同样的，rightWordPos[i]表示从字i开始向右的两个词
    	 */    	
    	WordPos[][] leftWordPoses = new WordPos[length][leftMost];    //当前位置左边的WordPos们
    	WordPos[][] rightWordPoses = new WordPos[length][rightMost];  //当前位置右边的WordPos们
    	WordPos[] leftWordPos = new WordPos[length];                  //当前位置左边最靠近的WordPos
    	WordPos[] rightWordPos = new WordPos[length];                 //当前位置右边最靠近的WordPos
    	int[][] leftIndexes = new int[length][leftMost];              //所有右边的Wordpos的索引
    	int[][] rightIndexes = new int[length][rightMost];            //所有左边的WordPos的索引
    	
    	//记录同一个起始位置，的不同候选词的次序
    	int[] leftWordPosesSize = new int[length];
    	int[] rightWordPosesSize = new int[length];
    	
    	int allIndex = 1;
    	
    	//添加头节点
    	WordPos newWP = new WordPos("<root>","<root-POS>", 0, 0, 0.0, 0, 0, allIndex);    	
    	leftWordPoses[0][0] = newWP;
    	rightWordPoses[0][0] = newWP;
    	leftWordPos[0] = newWP;
    	rightWordPos[0] = newWP;
    	leftIndexes[0][0] = allIndex;
    	rightIndexes[0][0] = allIndex;
    	allIndex++;    	
    	
    	//每个字的位置
    	for(int i=1; i<length; i++) {    		       
    		for(int j=0; j<lattice[i].length; j++) {
    			Item item = lattice[i][j];
    			if(item != null){
    				int start = item.s+1;
    				int end = item.t;
    				String form = item.form;
    				String pos = item.pos;
    				double prob = item.prob;
    				if(item.left != null)
    					prob -= item.left.prob;
    				//int k=0;
    				//start+1, end+1
    				int leftIndex = rightWordPosesSize[start];        //记录位置start开始的右边的WordPos词的索引
    				int rightIndex = leftWordPosesSize[end];          //记录位置end开始的左边的WordPos词的索引
    				newWP = new WordPos(form, pos, start, end, prob, leftIndex, rightIndex, allIndex);    				
    				//
					leftWordPoses[end][rightIndex] = newWP;           //
					leftIndexes[end][rightIndex] = allIndex;          //
					leftWordPosesSize[end] += 1;                      //
    				//
					rightWordPoses[start][leftIndex] = newWP;
					rightIndexes[start][leftIndex] = allIndex;
					rightWordPosesSize[start] += 1;
					//
					if(j==0)
						leftWordPos[end] = newWP;
					//
					allIndex++;
    				
    			}
    			else break;   			
    		}    		
    	}
    	
    	for(int i=1; i<length; i++) {    		
			Item item = backwardLattice[i][0];
			if(item != null) {
				int start = item.s+1;
				int end = item.t;
				String form = item.form;
				String pos = item.pos;
				double prob = item.prob;
				newWP = new WordPos(form, pos, start, end, prob, 0, 0, 0);    				
				rightWordPos[start] = newWP;
			}
    	}
    	
    	HashMap<WordPos, Integer> allWordPos = new HashMap<WordPos, Integer>();
    	for(int i=0; i<length; i++) {    		
    		for(int j=0; j<leftWordPoses[i].length; j++) {
    			WordPos wp = leftWordPoses[i][j];
    			if(wp != null)
    				allWordPos.put(wp, wp.index);   
    		}
    	}

    	obj[0] = allWordPos;   
    	obj[1] = leftWordPoses;
    	obj[2] = rightWordPoses;
    	obj[3] = leftWordPos;
    	obj[4] = rightWordPos;
    	obj[5] = leftIndexes;
    	obj[6] = rightIndexes;
    	
    	return obj;
    }
    
 
    
    
    /**
     * @deprecated
     * 通过保存的leftWordPoses和rightWordPoses,得到leftWordPos和rightWordPos.
     * <p> 其中leftWordPoses为WordPos[][],保存的是以某个点开始向左的所有WordPos;
     * <p> rightWordPoses为WordPos[][],保存的是以某个点开始向右的所有WordPos;
     * <p> leftWordPos为WordPos[],保存的是以某个点开始向左的一个WordPos;
     * <p> rightWordPos为WordPos[],保存的是以某个点开始向右的一个WordPos;
     * @since Apr 18, 2012
     * @param obj 保存上面的结构
     * @param chars 句子的所有字符
     */
    public void fillLattice(Object[] obj, String[] chars) {
    	WordPos[][] leftWordPoses = (WordPos[][]) obj[1];
    	WordPos[][] rightWordPoses = (WordPos[][]) obj[2];
    	int length = leftWordPoses.length;
    	WordPos[] leftWordPos = new WordPos[length];
    	WordPos[] rightWordPos = new WordPos[length];
    	
    	int number = 0;
    	WordPos newWP = leftWordPoses[0][0];
    	leftWordPos[0] = newWP;
    	rightWordPos[0] = newWP;
    	
    	double[] tmpProb = new double[length];
    	for(int i=0; i<length; i++)
    		tmpProb[i] = Double.NEGATIVE_INFINITY;
    	
    	for(int i=1; i<length; i++) {    		
    		for(int j=0; j<leftWordPoses[i].length; j++) {
    			WordPos item = leftWordPoses[i][j];
    			if(item != null){
    				int start = item.getStart();
    				int end = item.getEnd();
    				String form = item.getWord();
    				String pos = item.getPos();
    				double prob = item.getProb();
    	
					if(leftWordPos[end] == null)
						leftWordPos[end] = item;
					//选择策略1：选择最先出现的哪一个
					//if(rightWordPos[start] == null)
					//	rightWordPos[start] = allWordPos[start][end][k];
					//选择策略2：选择概率最大的哪一个    				
					if(prob > tmpProb[start] || rightWordPos[start] == null) {
						rightWordPos[start] = item;
						tmpProb[start] = prob;
					}    				
					//选择策略3:
					
    			}
    		}
    	}
    	
    	
    	obj[3] = leftWordPos;
    	//
    	//怎样处理那些没有的词
    	//策略1：生成一个<e-pos>词性
    	for(int i=0; i<length; i++){
    		if(rightWordPos[i] == null) {
    			int j=i;
    			while(j<length && rightWordPos[j] == null)
    				j++;    			
    			String form = Array.toWord(chars, i-1, j-1);
    			rightWordPos[i] = new WordPos(form, "<r-pos>", i, j, 0.0);
    		}
    	}
    	//策略2： 直接在lattice中生成，使其没有空余
    	
    	obj[4] = rightWordPos; 	
    	
    	
    }
    
    
    
    
    
    
    
    /**
     * Override this method if you have extra features that need to be
     * written to disk. For the basic DependencyPipe, nothing happens.
     * <p> 从文件中读取实例
     */
    protected void writeExtendedFeatures (DependencyInstanceJoint instance, ObjectOutputStream out)
	throws IOException {}


    /**
     * Read an instance from an input stream.
     *
     **/
    public DependencyInstanceJoint readInstance(ObjectInputStream in,
					   int length,
					   FeatureVector[][][] fvs,
					   double[][][] probs,
					   FeatureVector[][][][] nt_fvs,
					   double[][][][] nt_probs,
					   Parameters params) throws IOException {

	try {
		
	    // Get production crap.
	    for(int w1 = 0; w1 < length; w1++) {
		for(int w2 = w1+1; w2 < length; w2++) {
		    for(int ph = 0; ph < 2; ph++) {
			FeatureVector prodFV = new FeatureVector((int[])in.readObject());
			double prodProb = prodFV.getScore(params);//params.getScore(prodFV);
			fvs[w1][w2][ph] = prodFV;
			probs[w1][w2][ph] = prodProb;
		    }
		}
	    }
	    int last = in.readInt();
	    if(last != -3) { System.out.println("Error reading file."); System.exit(0); }

	    if(labeled) {
		for(int w1 = 0; w1 < length; w1++) {
		    for(int t = 0; t < types.length; t++) {
			//String type = types[t];

			for(int ph = 0; ph < 2; ph++) {
			    for(int ch = 0; ch < 2; ch++) {
				FeatureVector prodFV = new FeatureVector((int[])in.readObject());
				double nt_prob = prodFV.getScore(params); //params.getScore(prodFV);
				nt_fvs[w1][t][ph][ch] = prodFV;
				nt_probs[w1][t][ph][ch] = nt_prob;
			    }
			}
		    }
		}
		last = in.readInt();
		if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
	    }
	    

	    FeatureVector nfv = new FeatureVector((int[])in.readObject());
	    last = in.readInt();
	    if(last != -4) { System.out.println("Error reading file."); System.exit(0); }

	    DependencyInstanceJoint marshalledDI;
	    marshalledDI = (DependencyInstanceJoint)in.readObject();
	    marshalledDI.setFeatureVector(nfv);

	    last = in.readInt();
	    if(last != -1) { System.out.println("Error reading file."); System.exit(0); }

	    return marshalledDI;

	} catch(ClassNotFoundException e) {
	    System.out.println("Error reading file."); System.exit(0);
	}

	// this won't happen, but it takes care of compilation complaints
	return null;
    }
    
    /**
     * Read an instance from an input stream.
     * 
     **/
    public DependencyInstanceJoint readInstanceLess(ObjectInputStream in) throws IOException {

		try {
			FeatureVector nfv = new FeatureVector((int[])in.readObject());
		    int last = in.readInt();
		    if(last != -4) { System.out.println("Error reading file."); System.exit(0); }
	
		    DependencyInstanceJoint marshalledDI;
		    marshalledDI = (DependencyInstanceJoint)in.readObject();
		    marshalledDI.setFeatureVector(nfv);
	
		    last = in.readInt();
		    if(last != -1) { System.out.println("Error reading file."); System.exit(0); }
	
		    return marshalledDI;
	
		} catch(ClassNotFoundException e) {
		    System.out.println("Error reading file."); System.exit(0);
		}
	
		// this won't happen, but it takes care of compilation complaints
		return null;
    }
    
    
    
    public Object[] readInstanceJoint(ObjectInputStream in, 
    		Parameters params) throws IOException {

		try {
			
			HashMap<WordPos, Integer> allWordPos = (HashMap<WordPos, Integer>) in.readObject();
			int last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
			
			WordPos[][] leftWordPoses = (WordPos[][])in.readObject();
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
			
			WordPos[][] rightWordPoses = (WordPos[][])in.readObject();
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
						
			WordPos[] leftWordPos = (WordPos[])in.readObject();
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
			
			WordPos[] rightWordPos = (WordPos[])in.readObject();
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
			
			int[][] leftIndexes = (int[][])in.readObject();
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
			
			int[][] rightIndexes = (int[][])in.readObject();
			last = in.readInt();
			if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
						
			//
			FeatureVector nfv = new FeatureVector((int[])in.readObject());
		    last = in.readInt();
		    if(last != -4) { System.out.println("Error reading file."); System.exit(0); }		    
					    
			DependencyInstanceJoint marshalledDI;
			marshalledDI = (DependencyInstanceJoint)in.readObject();
			marshalledDI.setFeatureVector(nfv);			
			last = in.readInt();
			if(last != -1) { System.out.println("Error reading file."); System.exit(0); }
			
			String[] chars = (String[]) in.readObject();
			last = in.readInt();
			if(last != -2) { System.out.println("Error reading file."); System.exit(0); }
			
			Object[] obj = new Object[8];
			obj[0] = allWordPos; 
			obj[1] = leftWordPoses; 
			obj[2] = rightWordPoses;
			obj[3] = leftWordPos;
			obj[4] = rightWordPos;
			obj[5] = leftIndexes;
			obj[6] = rightIndexes;
			obj[7] = marshalledDI;
			
			int length = leftWordPoses.length;
			//从读取的word lattice中得到所有可能的概率值
			int leftSize = leftWordPoses[0].length; //从该字符向左的最多BasicWordPos个数，一般为K
			int rightSize = rightWordPoses[0].length; ///从该字符向右的最多BasicWordPos个数，一般为K,用作索引	
						
			return obj;
		
		} catch(ClassNotFoundException e) {
		System.out.println("Error reading file."); System.exit(0);
		}
		
		// this won't happen, but it takes care of compilation complaints
		return null;
    }
    
    
    /** 获得依存特征,在word lattice结构中找到所有特征 */
    public FeatureVector createDependencyFeature(DependencyInstanceJoint marshalledDI, 
    		int length, 
    		int s,
    		int t,
    		WordPos[] leftWordPos,
    		WordPos[] rightWordPos,
    		int[][] leftIndexes,
    		int[][] rightIndexes,
    		int ph, 
    		WordPos left, 
    		WordPos right, 
    		ArrayList<WordPos> allWordPos,
    		boolean added) {    	
    	
    	WordPos leftleft = null;
    	if(s > 0)
    		leftleft = leftWordPos[s-1];
    	WordPos rightright = null;
    	if(t < length-1)
    		rightright = rightWordPos[t+1];
    	
		WordPos leftright = null;
		WordPos rightleft = null;
		ArrayList<String> midPos = new ArrayList<String>();
		int midLength = allWordPos.size();
		if(midLength > 2) {
			leftright = allWordPos.get(1);
			rightleft = allWordPos.get(midLength-2);
			for(int i=1; i<midLength-1;i++) {
				//System.out.println(allWordPos.get(i).getWord() + "," + allWordPos.get(i).getPos());
				midPos.add(allWordPos.get(i).getPos());
			}
		}					
			
		boolean attR = ph == 0 ? true : false;
		FeatureVector prodFV = new FeatureVector();
		addCoreFeaturesJoint(marshalledDI, leftleft, left, leftright,
				rightleft, right, rightright,
				midPos, attR, prodFV, added);
		
		return prodFV;
    }
       
    
    /**
     * Get features for stems the old way. The only way this differs
     * from calling addTwoObsFeatures() is that it checks the
     * lengths of the full lexical items are greater than 5 before
     * adding features.
     *
     */
    private final void
	addOldMSTStemFeatures(String hLemma, String headP,
			      String cLemma, String childP, String attDist,
			      int hL, int cL, FeatureVector fv,
			      boolean b) {

		String all = hLemma + " " + headP + " " + cLemma + " " + childP;
		String hPos = headP + " " + cLemma + " " + childP;
		String cPos = hLemma + " " + headP + " " + childP;
		String hP = headP + " " + cLemma;
		String cP = hLemma + " " + childP;
		//String oPos = headP + " " + childP;
		String oLex = hLemma + " " + cLemma;

		add("SA="+all+attDist,fv, b); //this
		add("SF="+oLex+attDist,fv, b); //this
		add("SAA="+all,fv, b); //this
		add("SFF="+oLex,fv, b); //this

		if(cL > 5) {
		    add("SB="+hPos+attDist,fv, b);
		    add("SD="+hP+attDist,fv, b);
		    add("SK="+cLemma+" "+childP+attDist,fv, b);
		    add("SM="+cLemma+attDist,fv, b); //this
		    add("SBB="+hPos,fv, b);
		    add("SDD="+hP,fv, b);
		    add("SKK="+cLemma+" "+childP,fv, b);
		    add("SMM="+cLemma,fv, b); //this
		}
		if(hL > 5) {
		    add("SC="+cPos+attDist,fv, b);
		    add("SE="+cP+attDist,fv, b);
		    add("SH="+hLemma+" "+headP+attDist,fv, b);
		    add("SJ="+hLemma+attDist,fv, b); //this

		    add("SCC="+cPos,fv, b);
		    add("SEE="+cP,fv, b);
		    add("SHH="+hLemma+" "+headP,fv, b);
		    add("SJJ="+hLemma,fv, b); //this
		}

    }

}

