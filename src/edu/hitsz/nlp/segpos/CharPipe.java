package edu.hitsz.nlp.segpos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.corpus.OntoNotes;
import edu.hitsz.nlp.util.Array;


public class CharPipe {

	public FeatureMap dataMap;
	public FeatureMap tagMap;

    private Reader segposReader;
    private Writer segposWriter;

    public WordPosFreq freq;

    public String[] types;
    public int typeNumber;
    
    public Options options;


    /**
     * 
     * @since 2012-3-4
     * @param method char, or word
     * @throws IOException
     */
    public CharPipe(Options options) throws IOException {

    	dataMap = new FeatureMap();
    	tagMap = new FeatureMap();

    	segposReader =  new Reader();
    	segposWriter = new Writer();
    	freq = new WordPosFreq();
    	this.options = options;
    }




    /**
     * 生成实例,生成所有特征向量
     * @param file 训练文件名称
     * @param featFileName 所有特征向量文件名称
     * @param isInverse 句子是否翻转
     * @return 句子长度组成的数组
     * @throws IOException
     */
    public int[] createInstances(String file,
				 File featFileName) throws IOException {

    	readTags(file);    	
    	freq.extract(file, types, options);
		
		System.out.println("Type number is "+ types.length);
		System.out.println("Num Features: " + dataMap.size());

		ArrayList<Integer> lengths = new ArrayList<Integer>();
		segposReader.startReading(file, options);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(featFileName));
		Instance instance = null;
		instance = segposReader.getNext();
		int count = 0;

		long begin = System.currentTimeMillis();
		System.out.println("Creating Feature Vector Instances: ");
		while(instance != null) {
			if(count %100 == 0) {	
				System.out.print(count+"...");
				if(count %1000 == 0) {
					System.out.println();
				}
			}
		    count ++;
		    instance.setFeatureVector(createFeatureVector(instance));
		    lengths.add(instance.charLength());
		    writeInstance(instance,out);
		    instance = null;
		    instance = segposReader.getNext();
		}		

		System.out.println(count + ".");	
		
		System.out.println(System.currentTimeMillis()-begin);

		System.out.println();

	    out.close();

		return Array.toArray(lengths);

    }
    


    
    

    public void writeInstance(Instance instance, ObjectOutputStream out){

    	try{    		
	    	out.writeObject(instance.fv.ints());
	    	out.writeInt(-2);

		    out.writeObject(instance);
		    out.writeInt(-3);

		    out.reset();
    	}
    	catch (IOException e) {
    		System.out.println("Store instances wrong");
    	}
    }
    

    /**
     * Read an instance from an input stream.
     *
     **/
    public Instance readInstance(ObjectInputStream in) throws IOException {

		try {
	
		    FeatureVector nfv = new FeatureVector((int[]) in.readObject());
		    int last = in.readInt();
		    if(last != -2) { System.out.println("Error reading file."); System.exit(0); }
	
		    Instance marshalledDI;
		    marshalledDI = (Instance)in.readObject();
		    marshalledDI.setFeatureVector(nfv);
	
		    last = in.readInt();
		    if(last != -3) { System.out.println("Error reading file."); System.exit(0); }
	
		    return marshalledDI;
	
		} catch(ClassNotFoundException e) {
		    System.out.println("Error reading file."); System.exit(0);
		}
	
		// this won't happen, but it takes care of compilation complaints
		return null;
    }
    
    
  
    
    

    /**
     * 读一遍文件，将所有的tag读出来
     * @since Jan 9, 2012
     * @param file
     * @throws IOException
     */
    public void readTags(String file) throws IOException{

    	System.out.print("Read tags from file " +file + "...");
		segposReader.startReading(file, options);
		Instance instance = segposReader.getNext();
		int count = 0;
		while(instance != null) {
			if(count % 1000 == 0) {
				System.out.print(count+"...");
				if(count % 10000 == 0) 
					System.out.println();
			}
			//词性
		    String[] tags = instance.tags;
		    for(int i = 0; i < tags.length; i++)
		    	tagMap.add(tags[i]);
		    instance = segposReader.getNext();
		    count++;
		}

		HashMap<String, Integer> tagsMap = tagMap.getMap();
		typeNumber = tagsMap.size();
		types = new String[typeNumber];
		Iterator<Entry<String, Integer>> iter = tagsMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, Integer> entry =  iter.next();
			types[entry.getValue()] = entry.getKey();
		}
		System.out.println(" ...done");
    }
    


    /**
     * 根据已有的词性标注生成标注集
     * @since Jan 9, 2012
     */
    public void generateTagsOntonotes(){
    	types = OntoNotes.ctbpostags;
    	typeNumber = types.length;
    	for(int i=0; i<typeNumber; i++)
    		tagMap.add(types[i],i);
    }



    /**
     * 初始化写入文件
     * @param file
     * @throws IOException
     */
    public void initInputFile (String file) throws IOException {
    	segposReader.startReading(file, options);
    }
  
    
    public Instance nextInstance() throws IOException {
    	Instance instance = segposReader.getNext();
    	return instance;
    }
    
    protected Instance nextRawInstance() throws IOException {
    	Instance instance = segposReader.getNextRaw();
    	return instance;
    }
    


    /**
     * 初始化输出文件
     * @param file
     * @throws IOException
     */
    public void initOutputFile (String file) throws IOException {
		segposWriter.startWriting(file, options);
    }

    
    public void outputInstance(Instance instance) throws IOException {
    	segposWriter.write(instance);    	
    }
    
    public void outputWPLattice(Lattice lattice, int cnt) throws IOException {
    	segposWriter.writeWPLattice(lattice, cnt);    	
    }
    
    public Lattice readWPLattice() throws IOException {
    	return Lattice.readWPLattice(segposReader);    	
    }
    
    /**
     * 输出器关闭
     * @throws IOException
     */
    public void close () throws IOException {
		if (null != segposWriter) {
		    segposWriter.finishWriting();
		}
    }
    
    /**
     * 从实例中提取出所有的特征，及其概率
     * @since Apr 9, 2012
     * @param instance
     * @param fvs
     * @param probs
     * @param params
     */
    public void fillFeatureVectors(Instance instance,
			   FeatureVector[][][] fvs,
			   double[][][] probs,
			   Parameters params) {

		final int instanceLength = instance.charLength();
		final String[] characters = instance.chars;
		//ArrayList<String> allPos = Array.toArrayList(types);
		// Get production crap.
		for(int w1 = 0; w1 < instanceLength+1; w1++) {
		    for(int w2 = w1+1; w2 < instanceLength+1; w2++) {
		    	String currentWord = Array.toWord(characters,w1,w2);	
		    	ArrayList<String> candidatePos = freq.getPos(currentWord);
		    	for(String pos : candidatePos) {
		    		int position = tagMap.get(pos);
				    FeatureVector prodFV = new FeatureVector();
				    addCharFeatures(instance,w1,w2,pos,prodFV,false);	
				    double prodProb = prodFV.getScore(params); //params.getScore(prodFV);
				    //fvs[w1][w2][position] = prodFV;
				    probs[w1][w2][position] = prodProb;
		    	}
		    }
		}
    }
    
    
    /**
     * 从依存实例创造特征向量,把其中的特征都生成出来
     * <p> 用于初始化时遍历所有的特征
     * @param instance
     * @return
     */
    public FeatureVector createFeatureVector(Instance instance) {

		final int instanceLength = instance.charLength();
		String[] chartags = instance.chartags;

		FeatureVector fv = new FeatureVector();

		for(int i = 0; i < instanceLength; i++) {
			//System.out.println(i);
			addCharFeature(instance, i, chartags[i], fv, true);
		}
		return fv;
    }
    
        
    
    
    
    public FeatureVector createFeatureVectorWord(Instance instance) {
		
    	String[] words = instance.words;
		String[] tags = instance.tags;
		
		//判断句子字符长    	
		String[][] charposes = Reader.getCharPOS(words, tags);
		String[] chars = charposes[0];
		String[] charPos = charposes[1];
		instance.chars = chars;
		instance.chartags = charPos;	
		//		
		if(options.charFeatureType.equals("nglow")) {
			String[] puncts = Reader.isPunc(chars);
			instance.puncts = puncts;
			String[] cs = Reader.ngLowClasses(chars);
			instance.ngLowClasses = cs;
		}
		
		int instanceLength = chars.length;
		
		FeatureVector fv = new FeatureVector();		
		for(int i = 0; i < instanceLength; i++) {			
			addCharFeature(instance, i, charPos[i], fv, true);
		}
		return fv;
    }
    

    
    

    /**
     * 添加一个词和它的词性组成的特征,用于对最优标注序列抽取特征
     * @since Apr 9, 2012
     * @param inst
     * @param start 词的起始位置
     * @param end 词的结束位置
     * @param pos 当前词的词性
     * @param fv
     * @param added
     */
	public void addCharFeatures(Instance inst,
			int start, int end, String pos,
			FeatureVector fv, boolean added){
		int wordLength = end-start;
		String[] tags = Word2Char.generateTag(wordLength, pos);
		for(int m=0; m<wordLength; m++){
			addCharFeature(inst, start+m, tags[m], fv, false);		
		}		 
	 }
	

	
	
	
	
	
	/**
	 * 添加当前字符的特征
	 * @since Aug 22, 2012
	 * @param instance
	 * @param currentLength
	 * @param charPos
	 * @param fv
	 * @param added
	 */
	public void addCharFeature(Instance instance, int currentLength, String charPos,
    		FeatureVector fv, boolean added) {
		if(options.charFeatureType.toLowerCase().equals("jiang"))
			addCharFeatureJiangLiu(instance, currentLength, charPos, fv, added);
		else if(options.charFeatureType.toLowerCase().equals("nglow"))
			addCharFeatureNgLow(instance, currentLength, charPos, fv, added);
		else {
			System.out.println("no such character feature selection");
			System.out.println();
		}
	}
	
    
    
	/**
	 * 添加当前字符的特征,Jiang wenbin论文
	 * @since Apr 9, 2012
	 * @param instance
	 * @param currentLength 当前字符的位置
	 * @param charPos 字符的词性 B-NN
	 * @param fv 
	 * @param added
	 */
   private void addCharFeatureJiangLiu(Instance instance, int currentLength, String charPos,
    		FeatureVector fv, boolean added){
	   
		String[] characters = instance.chars;
		int sentenceLength = instance.charLength();

		String curChar = characters[currentLength];
		String preChar = "START";
		String pre2Char = "START2";
		String nextChar = "END";
		String next2Char = "END2";
		if(currentLength>0){
			preChar = characters[currentLength-1];
		}
		if(currentLength>1){
			pre2Char = characters[currentLength-2];
		}
		if(currentLength<sentenceLength-1){
			nextChar = characters[currentLength+1];
		}
		if(currentLength<sentenceLength-2){
			next2Char = characters[currentLength+2];
		}

		//feature
		add("1:"+pre2Char+"_"+charPos, fv, added);
		add("2:"+preChar+"_"+charPos, fv, added);
		add("3:"+curChar+"_"+charPos, fv, added);
		add("4:"+nextChar+"_"+charPos, fv, added);
		add("5:"+next2Char+"_"+charPos, fv, added);
		add("6:"+pre2Char+"_"+preChar+"_"+charPos, fv, added);
		add("7:"+preChar+"_"+curChar+"_"+charPos, fv, added);
		add("8:"+curChar+"_"+nextChar+"_"+charPos, fv, added);
		add("9:"+nextChar+"_"+next2Char+"_"+charPos, fv, added);
		add("10:"+preChar+"_"+nextChar+"_"+charPos, fv, added);
		
		add("11:"+curChar+"_"+pre2Char+"_"+charPos, fv, added);
		add("12:"+curChar+"_"+preChar+"_"+charPos, fv, added);
		add("13:"+curChar+"_"+curChar+"_"+charPos, fv, added);
		add("14:"+curChar+"_"+nextChar+"_"+charPos, fv, added);
		add("15:"+curChar+"_"+next2Char+"_"+charPos, fv, added);
		add("16:"+curChar+"_"+pre2Char+"_"+preChar+"_"+charPos, fv, added);
		add("17:"+curChar+"_"+preChar+"_"+curChar+"_"+charPos, fv, added);
		add("18:"+curChar+"_"+curChar+"_"+nextChar+"_"+charPos, fv, added);
		add("19:"+curChar+"_"+nextChar+"_"+next2Char+"_"+charPos, fv, added);
		add("20:"+curChar+"_"+preChar+"_"+nextChar+"_"+charPos, fv, added);
		
    }
   
	/**
	 * 添加当前字符的特征
	 * @since Apr 9, 2012
	 * @param instance
	 * @param currentLength 当前字符的位置
	 * @param charPos 字符的词性 B-NN
	 * @param fv 
	 * @param added
	 */

   public void addCharFeatureNgLow(Instance instance, int currentLength, String charPos,
		   FeatureVector fv, boolean added){	   

		int sentenceLength = instance.charLength();
		String[] characters = instance.chars;
		String[] ngLowClasses = instance.ngLowClasses;

		String curChar = characters[currentLength];
		String preChar = "START";
		String pre2Char = "START2";
		String nextChar = "END";
		String next2Char = "END2";
		String curPunct = instance.puncts[currentLength];
		String curClass = ngLowClasses[currentLength];
		String preClass = "SC";
		String pre2Class = "S2C";
		String nextClass = "EC";
		String next2Class = "E2C";
		
		
		if(currentLength>0){
			preChar = characters[currentLength-1];
			preClass = ngLowClasses[currentLength-1];
		}
		if(currentLength>1){
			pre2Char = characters[currentLength-2];
			pre2Class = ngLowClasses[currentLength-2];
		}
		if(currentLength<sentenceLength-1){
			nextChar = characters[currentLength+1];
			nextClass = ngLowClasses[currentLength+1];
		}
		if(currentLength < sentenceLength-2){
			next2Char = characters[currentLength+2];
			next2Class = ngLowClasses[currentLength+2];
		}
		
		String ngLowClass = pre2Class+preClass+curClass+nextClass+next2Class;

		//feature
		add("1:"+pre2Char+"_"+charPos, fv, added);
		add("2:"+preChar+"_"+charPos, fv, added);
		add("3:"+curChar+"_"+charPos, fv, added);
		add("4:"+nextChar+"_"+charPos, fv, added);
		add("5:"+next2Char+"_"+charPos, fv, added);
		add("6:"+pre2Char+"_"+preChar+"_"+charPos, fv, added);
		add("7:"+preChar+"_"+curChar+"_"+charPos, fv, added);
		add("8:"+curChar+"_"+nextChar+"_"+charPos, fv, added);
		add("9:"+nextChar+"_"+next2Char+"_"+charPos, fv, added);
		add("10:"+preChar+"_"+nextChar+"_"+charPos, fv, added);
		add("11:"+curPunct+"_"+charPos,fv, added);
		add("12:"+ngLowClass+"_"+charPos, fv, added);
		
   }
   
    
   

    public void add(String feat, FeatureVector fv, boolean added) {    	
    	if(added) {
    		int num = dataMap.add(feat);
        	if(num >= 0)
        		fv.add(num);
    	}
    	else {
    		fv.addString(feat);
	    	int num = dataMap.get(feat);
	    	if(num >= 0)
	    		fv.add(num);
    	}    	
    }
    
    



}
