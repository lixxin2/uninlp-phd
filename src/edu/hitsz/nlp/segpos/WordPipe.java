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
import edu.hitsz.nlp.util.Array;

public class WordPipe {

	public FeatureMap dataMap;
	public FeatureMap tagMap;

    private Reader segposReader;
    private Writer segposWriter;

    public WordPosFreq freq; //Yue Zhang and Stephen Clark 2008 
    public TagDict dict; //Yue Zhang and Stephen Clark 2010
    public MorphFeature morphFea;

    public String[] types;
    public int typeNumber;
    public Options options;

    /**
     * 
     * @since 2012-3-4
     * @param method char, or word
     * @throws IOException
     */
    public WordPipe(Options options) throws IOException {

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
     * @return 句子长度组成的数组
     * @throws IOException
     */
    public int[] createInstances(String file,
				 File featFileName) throws IOException {

    	readTags(file);
    	
    	freq.extract(file, types, options);
    			
		//generateTagsOntonotes( );
		//createMap(file);

		System.out.println("Num Features: " + dataMap.size() + ",tag Numbers: "+types.length);

		ArrayList<Integer> lengths = new ArrayList<Integer>();
		segposReader.startReading(file, options);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(featFileName));

		Instance instance = segposReader.getNext();
		int num1 = 0;

		long begin = System.currentTimeMillis();
		System.out.println("Creating Feature Vector Instances: ");
		while(instance != null) {
			
			if(num1%100 == 0) {
				System.out.print(num1 + " ");
				if(num1%1000 == 0) {
					System.out.print("\n");
				}
			}

		    instance.setFeatureVector(createFeatureVector(instance));

		    lengths.add(instance.charLength());

		    writeInstance(instance,out);

		    instance = null;

		    instance = segposReader.getNext();

		    num1++;
		}
		
		System.out.print(num1 + " ");
		
		System.out.println(System.currentTimeMillis()-begin);
		System.out.println();

	    out.close();

		return Array.toArray(lengths);

    }
    
    
    
    /**
     * 生成实例,生成所有特征向量
     * @param file 训练文件名称
     * @param featFileName 所有特征向量文件名称
     * @return 句子长度组成的数组
     * @throws IOException
     */
    public int[] createInstancesPOS(String file,
				 File featFileName) throws IOException {

    	readTags(file);
    	
    	freq.extract(file, types, options);
    			
		//generateTagsOntonotes( );
		//createMap(file);

		System.out.println("Num Features: " + dataMap.size() + ",tag Numbers: "+types.length);

		ArrayList<Integer> lengths = new ArrayList<Integer>();
		segposReader.startReading(file, options);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(featFileName));

		Instance instance = segposReader.getNext();
		int num1 = 0;

		long begin = System.currentTimeMillis();
		System.out.println("Creating Feature Vector Instances: ");
		while(instance != null) {
			
			if(num1%100 == 0) {
				System.out.print(num1 + " ");
				if(num1%1000 == 0) {
					System.out.print("\n");
				}
			}

		    instance.setFeatureVector(createFeatureVectorPOS(instance));

		    lengths.add(instance.words.length);

		    writeInstance(instance,out);

		    instance = null;

		    instance = segposReader.getNext();

		    num1++;
		}
		
		System.out.print(num1 + " ");
		
		System.out.println(System.currentTimeMillis()-begin);
		System.out.println();

	    out.close();
		return Array.toArray(lengths);
    }
    
 
    
    /**
     * 预处理训练文件，包括
     * <li>读取POS tags,
     * <li> 预处理Morph Feature
     * <li> 其中的closed set POS tags
     * @since 2013-2-6
     * @param file
     * @return
     * @throws IOException 
     */
    public int preProcessTrainFile(String file) throws IOException {
    	
    	int sentenceNumber = readTags(file);
    	
    	morphFea = new MorphFeature();
    	morphFea.extractFromFile(file, types, options);

    	dict = new TagDict();  	
    	return sentenceNumber;
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
     * 读一遍文件，将所有的POS tag读出来，返回句子的数目
     * @since Jan 9, 2012
     * @param file
     * @throws IOException
     */
    public int readTags(String file) throws IOException{

    	System.out.print("Read tags from file " +file + "...");
		segposReader.startReading(file, options);
		Instance instance = segposReader.getNext();
		int sentenceNumber = 0;
		while(instance != null) {
			//
			sentenceNumber++;
		    String[] tags = instance.tags;
		    for(int i = 0; i < tags.length; i++)
		    	tagMap.add(tags[i]);

		    instance = segposReader.getNext();
		}
		System.out.println(" ...done");
		
		System.out.print("Tags: ");
		HashMap<String, Integer> tagsMap = tagMap.getMap();
		typeNumber = tagsMap.size();
		types = new String[typeNumber];
		Iterator<Entry<String, Integer>> iter = tagsMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, Integer> entry =  iter.next();
			types[entry.getValue()] = entry.getKey();
			System.out.print(entry.getKey()+",");
		}
		System.out.println("\n");
		return sentenceNumber;
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
     * 从实例创造特征向量,把其中的特征都生成出来
     * @param instance
     * @return
     */
    public FeatureVector createFeatureVector(Instance instance) {
		
		String[] words = instance.words;
		String[] tags = instance.tags;				
		
		FeatureVector fv = new FeatureVector();
		
		final int length = words.length;
		Item preItem = null;
		int start = 0;
		int end = 0;
		
		for(int i = 0; i < length; i++) {
			end = start + words[i].length();
			Item curItem = new Item(start, end, words[i], tags[i], preItem);
			addWordPOSFeatures(instance, curItem, fv, true);
			start = end;
			preItem = curItem;
		}
		return fv;
    }
    
    /**
     * 从实例创造特征向量,把其中的特征都生成出来,用于单beam的情况
     * @since 2013-2-6
     * @param instance
     * @return
     */
    public FeatureVector createFeatureVectorSingleBeam(Instance instance) {
		
		String[] words = instance.words;
		String[] tags = instance.tags;
		String[] chars = instance.chars;
		//String[] charTags = instance.chartags;
		
		FeatureVector fv = new FeatureVector();
		
		final int length = chars.length;
		Item preItem = null;
		Item curItem = null;
		//记录每个词的前后位置
		int start = 0;
		int end = 0;
		int wordSeq = -1;
		
		for(int i = 0; i < length; i++) {
			if(i == start) {
				wordSeq++;
				end = start + words[wordSeq].length()-1;
				curItem = new Item(start, start, chars[i], tags[wordSeq], preItem);
				addStartingFeatures(instance, curItem, fv, true);
			}
			else {
				curItem.t += 1;
				curItem.form += chars[i];
				addAppendingFeatures(instance, curItem, fv, true);
			}
			if (i == end) {
				start = end+1;
			}
		}
		return fv;
	}
    
    
   

    
    /**
     * 从依存实例创造特征向量,把其中的特征都生成出来
     * @param instance
     * @return
     */
    public FeatureVector createFeatureVectorPOS(Instance instance) {
		
		String[] words = instance.words;
		String[] tags = instance.tags;				
		
		FeatureVector fv = new FeatureVector();
		
		final int length = words.length;
		Item preItem = null;
		int start = 0;
		int end = 0;
		
		Item startItem = null;
		
		for(int i = 0; i < length; i++) {
			end = start + words[i].length();
			Item curItem = new Item(start, end, words[i], tags[i], preItem, true);
			if(i==0) startItem = curItem;
			start = end;
			preItem = curItem;
		}
		while(startItem != null) {
			addPOSFeatures(instance, startItem, fv, true);
			startItem = startItem.right;
		}
		return fv;
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
    
    /**
     * 添加一个词和词性组合类的特征
     * @since Apr 9, 2012
     * @param inst
     * @param item 当前词和词性
     * @param fv
     * @param added
     */
    public void addWordPOSFeatures(Instance inst, Item item, 
    		FeatureVector fv, boolean added) {
    	
    	int MaxWordLength = 15;
    	int sentenceLength = inst.charLength();

    	String[] chars = inst.chars;
    	
    	Item preItem = null;
    	Item pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos;  //当前词的长度
    	
    	int wordLength = curWord.length();
    	if(wordLength > MaxWordLength)
			wordLength = MaxWordLength;
    	
		String startChar = curWord.substring(0,1);         //当前词的首字符
		String endChar  = curWord.substring(wordLength-1); //当前词的尾字符
		String preWord = "preWord";                        //前一个词
		String endChar_preWord = "eCpW";                //前一词的尾字符
		int preWordLength = 0;						    //前一词的长度
		if(preItem != null){
			preWord = preItem.form;
			preWordLength = preWord.length();
			if(preWordLength > MaxWordLength)
				preWordLength = 15;
			endChar_preWord = preWord.substring(preWordLength-1);
		}
		
		String startChar_nextWord = "sCnW";              //后一词的首字符
		int endCharPosition = item.t;
		if(endCharPosition < sentenceLength){
			startChar_nextWord = chars[endCharPosition];
		}
		
		add("1="+curWord, fv, added); //word w: w_{i}
		add("2="+preWord+"_"+curWord, fv, added); //word bigram w1w2: w_{i-1}w_{i}
		if(wordLength == 1)                                         //single-character word w
			add("3="+curWord, fv, added);
		add("4="+startChar+"_"+wordLength, fv, added); //a word starting with character c and having length l		
		add("5="+endChar+"_"+wordLength, fv, added);//a word ending with character c and having length l
		add("6="+endChar+"_"+startChar_nextWord, fv, added); //space-serarated characters c1 and c2
		for(int i=0; i<wordLength-1; i++)
			add("7="+curWord.substring(i,i+2), fv, added);
		add("8="+startChar+"_"+endChar,fv, added);//the first and last characters c1 and c2 of any word
		add("9="+curWord+"_"+startChar_nextWord, fv, added);//word w immediately before character c
		add("10="+endChar_preWord+"_"+curWord, fv, added); //character c immediately before word w
		add("11="+startChar+"_"+startChar_nextWord, fv, added); //the starting character c1 and c2 of two consecutive words
		add("12="+endChar_preWord+"_"+endChar, fv, added); //the ending character c1 and c2 of two consecutive words
		add("13="+preWord+"_"+wordLength, fv, added);  //a word of length l and previous word w
		add("14="+preWordLength+"_"+curWord, fv, added); //a word of length l and the next word w

		//String pos = item.pos;       //当前POS
		String prePos = "NONE";                        //前一个POS
		String pre2Pos = "NONE";                       //前第二个POS
		if(preItem != null){
			prePos = preItem.pos;
		}
		if(pre2Item != null){
			pre2Pos = pre2Item.pos;
		}
		
		add("15="+curWord+"_"+curPos, fv, added); //tag t with word w
		add("16="+prePos+"_"+curPos, fv, added); //tag bigram t1t2
		add("17="+pre2Pos+"_"+prePos+"_"+curPos, fv, added); //tag trigram t1t2t3
		add("18="+preWord+"_"+curPos, fv, added); //tag t followed by word w
		add("19="+prePos+"_"+curWord, fv, added); //word w followed by tag t
		add("20="+endChar_preWord+"_"+curWord+"_"+curPos, fv, added); //word w with tag t and previous character c
		add("21="+curWord+"_"+curPos+"_"+startChar_nextWord, fv, added);//word w with tag t and next character c
		if(wordLength == 1)        //tag t on single-character word w in character trigram c1wc2  
			add("22="+endChar_preWord+"_"+curWord+"_"+curPos+"_"+startChar_nextWord, fv, added);
		add("23="+startChar+"_"+curPos, fv, added); //tag t on a word starting with char c
		add("24="+endChar+"_"+curPos, fv, added); // tag t on a word ending with char c  
		if(wordLength > 2) {        
			for(int i=1; i<wordLength-1; i++) {
				add("25="+curWord.substring(i,i+1)+"_"+curPos, fv, added); // tag t on a word containing char c(not the starting or ending character)
			}
		}   
		if(wordLength > 1) {        
			for(int i=1; i<wordLength-1; i++) {
				add("26="+startChar+"_"+curWord.substring(i,i+1)+"_"+curPos, fv, added); // tag t on a word starting with char c0 and containing char c
				add("27="+endChar+"_"+curWord.substring(i-1,i)+"_"+curPos, fv, added); // tag t on a word ending with char c0 and containing char c
			}
		}			
		if (wordLength>1) {
			for (int i=0; i<wordLength-1; i++){
				if (curWord.charAt(i) == curWord.charAt(i+1))
					add("28="+curWord.substring(i,i+2)+"_"+curPos, fv, added); // tag t on a word containing repeated char cc
			}
		}
		if (freq.wordCTBMorphPre.containsKey(startChar))
			add("29="+freq.wordCTBMorphPre.get(startChar)+"_"+curPos, fv, added);
		else
			add("29=NONE_"+curPos, fv, added);
		if(freq.wordCTBMorphSuf.containsKey(endChar))
			add("30="+freq.wordCTBMorphSuf.get(endChar)+"_"+curPos, fv, added);
		else
			add("30=NONE_"+curPos, fv, added);    	
    	
    }
    
    
    public void addWordFeatures(Instance inst, Item item, 
    		FeatureVector fv, boolean added) {
    	
    	int MaxWordLength = 15;
    	int sentenceLength = inst.charLength();

    	String[] chars = inst.chars;
    	
    	Item preItem = null;
    	Item pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos;  //当前词的长度
    	
    	int wordLength = curWord.length();
    	if(wordLength > MaxWordLength)
			wordLength = MaxWordLength;
    	
		String startChar = curWord.substring(0,1);         //当前词的首字符
		String endChar  = curWord.substring(wordLength-1); //当前词的尾字符
		String preWord = "preWord";                        //前一个词
		String endChar_preWord = "eCpW";                //前一词的尾字符
		int preWordLength = 0;						    //前一词的长度
		if(preItem != null){
			preWord = preItem.form;
			preWordLength = preWord.length();
			if(preWordLength > MaxWordLength)
				preWordLength = 15;
			endChar_preWord = preWord.substring(preWordLength-1);
		}
		
		String startChar_nextWord = "sCnW";              //后一词的首字符
		int endCharPosition = item.t;
		if(endCharPosition < sentenceLength){
			startChar_nextWord = chars[endCharPosition];
		}
		
		add("1="+curWord, fv, added); //word w: w_{i}
		add("2="+preWord+"_"+curWord, fv, added); //word bigram w1w2: w_{i-1}w_{i}
		if(wordLength == 1)                                         //single-character word w
			add("3="+curWord, fv, added);
		add("4="+startChar+"_"+wordLength, fv, added); //a word starting with character c and having length l		
		add("5="+endChar+"_"+wordLength, fv, added);//a word ending with character c and having length l
		add("6="+endChar+"_"+startChar_nextWord, fv, added); //space-serarated characters c1 and c2
		for(int i=0; i<wordLength-1; i++)
			add("7="+curWord.substring(i,i+2), fv, added);
		add("8="+startChar+"_"+endChar,fv, added);//the first and last characters c1 and c2 of any word
		add("9="+curWord+"_"+startChar_nextWord, fv, added);//word w immediately before character c
		add("10="+endChar_preWord+"_"+curWord, fv, added); //character c immediately before word w
		add("11="+startChar+"_"+startChar_nextWord, fv, added); //the starting character c1 and c2 of two consecutive words
		add("12="+endChar_preWord+"_"+endChar, fv, added); //the ending character c1 and c2 of two consecutive words
		add("13="+preWord+"_"+wordLength, fv, added);  //a word of length l and previous word w
		add("14="+preWordLength+"_"+curWord, fv, added); //a word of length l and the next word w
    
    }
    
    
    
    /**
     * 添加新开始词的第一个字时的特征，参考"Yue Zhang and Stephen Clark 2010"
     * @since 2013-2-6
     * @param inst
     * @param item
     * @param fv
     * @param added
     */
    public void addStartingFeatures(Instance inst, Item item, 
    		FeatureVector fv, boolean added) {
    	
    	int MaxWordLength = 16;
    	
    	Item preItem = null;
    	Item pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos; 
    	
    	int wordLength = curWord.length();  //当前词的长度
    	if(wordLength > MaxWordLength)
			wordLength = MaxWordLength;
    	
		String startChar = curWord.substring(0,1);         //当前词的首字符
    	
		String preWord = "pW";                        //前一个词
		int preWordLength =0;                              //前面第一个词的长度
		String startChar_preWord = "sCpW";                 //前面第一个词的首字符
		String endChar_preWord = "eCpW";                   //前一词的尾字符
		
		String pre2Word = "p2W";                      //前面第二个词
		int pre2WordLength = 0;                         //前面第二个词的长度
		String endChar_pre2Word = "eCp2W";                 //前面第二个词的尾字符
		
		if(preItem != null){
			preWord = preItem.form;
			preWordLength = preWord.length();
			if(preWordLength > MaxWordLength)
				preWordLength = MaxWordLength;
			startChar_preWord = preWord.substring(0,1);
			endChar_preWord = preWord.substring(preWordLength-1);
			if(pre2Item != null) {
				pre2Word = pre2Item.form;
				pre2WordLength = pre2Word.length();
				endChar_pre2Word = pre2Word.substring(pre2WordLength-1);
			}
		}
				
		add("1="+preWord, fv, added); //1: w-1
		add("2="+pre2Word+"_"+preWord, fv, added); // 2: w-2w-1
		if(preWordLength == 1)                    // 3: w-1, where len(w-1)=1
			add("3="+preWord, fv, added);
		add("4="+startChar_preWord+"_"+preWordLength, fv, added); //4: start(w-1)len(w-1)
		add("5="+endChar_preWord+"_"+preWordLength, fv, added);   //5: end(w-1)len(w-1)
		add("6="+endChar_preWord+"_"+startChar, fv, added);       //6: end(w-1)c0
		
		add("8="+startChar_preWord+"_"+endChar_preWord,fv, added);//8: start(w-1)end(w-1)
		add("9="+preWord+"_"+startChar, fv, added);               //9: w-1c0
		add("10="+endChar_pre2Word+"_"+preWord, fv, added);       //10： end(w-2)w-1
		add("11="+startChar_preWord+"_"+startChar, fv, added);    //11: start(w-1)c0
		add("12="+endChar_pre2Word+"_"+endChar_preWord, fv, added); //12: end(w-2)end(w-1)
		add("13="+pre2Word+"_"+preWordLength, fv, added);           //13: w-2len(w-1)
		add("14="+pre2WordLength+"_"+preWord, fv, added);           //14: len(w-2)w-1

		//String pos = item.pos;       //当前POS
		String prePos = "pP";                        //前一个POS
		String pre2Pos = "p2P";                       //前第二个POS
		if(preItem != null){
			prePos = preItem.pos;
		}
		if(pre2Item != null){
			pre2Pos = pre2Item.pos;
		}
		
		add("15="+preWord+"_"+prePos, fv, added);            //15: w-1t-1
		add("16="+prePos+"_"+curPos, fv, added);             //16: t-1t0
		add("17="+pre2Pos+"_"+prePos+"_"+curPos, fv, added); //17: t-2t-1t0
		add("18="+preWord+"_"+curPos, fv, added);            //18: w-1t0
		add("19="+pre2Pos+"_"+preWord, fv, added);            //19: t-2w-1
		add("20="+preWord+"_"+prePos+"_"+endChar_pre2Word, fv, added); //20: w-1t-1end(w-2)
		add("21="+preWord+"_"+prePos+"_"+startChar, fv, added);      //21: w-1t-1c0
		if(preWordLength == 1)
			add("22:"+endChar_pre2Word+"_"+preWord+"_"+startChar+"_"+prePos, fv, added); //22:c-2c-1c0t-1, where len(w-1)=1
		add("23="+startChar+"_"+curPos, fv, added); //23: start(w0)t0
		add("24="+prePos+"_"+startChar_preWord, fv, added); // 24:t-1start(w-1) 
		add("25="+curPos+"_"+startChar, fv, added); //25: t0c0
		
		if(preWordLength > 1) {        //27:ct-1end(w-1),where c \in w-1 and c != end(w-1)
			for(int i=0; i<wordLength-1; i++) {
				add("27="+preWord.substring(i,i+1)+"_"+prePos+"_"+endChar_preWord, fv, added); 
			}
		}
		String startCharCategory = "sCC";        //cat(start(w0))
		if (morphFea.wordCTBMorphPre.containsKey(startChar))   
			startCharCategory = morphFea.wordCTBMorphPre.get(startChar);
		add("28"+startChar+"_"+curPos+"_"+startCharCategory, fv, added);    //28: c0t0cat(start(w0))
		if(preWordLength > 1) {        //29:ct-1cat(end(w-1)), where c \in w-1 and c != end(w-1)
			String endCharPreWordCategory = "eCpwC";
			if(morphFea.wordCTBMorphSuf.containsKey(endChar_preWord))
				endCharPreWordCategory = morphFea.wordCTBMorphSuf.get(endChar_preWord);
			for(int i=0; i<wordLength-1; i++) {
				add("29="+preWord.substring(i,i+1)+"_"+prePos+"_"+endCharPreWordCategory, fv, added); 
			}
		}
		add("30="+startChar+"_"+curPos+"_"+endChar_preWord+"_"+prePos, fv, added); //30:c0t0c-1t-1    	
    	
    }
    
    
    /**
     * 添加词的第二个字开始的特征，参考"Yue Zhang and Stephen Clark 2010"
     * @since 2013-2-6
     * @param inst
     * @param item
     * @param fv
     * @param added
     */
    public void addAppendingFeatures(Instance inst, Item item, 
    		FeatureVector fv, boolean added) {
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos;    	    
		
		int wordLength = curWord.length();
		String startChar = curWord.substring(0,1);                       //当前词的首字符
		String curChar = curWord.substring(wordLength-1);                //当前词的当前字
		String preChar = curWord.substring(wordLength-2, wordLength-1);  //当前字的前一个字
		
		add("7="+preChar+"_"+curChar, fv, added);                        //7: c-1c0
		add("25="+curPos+"_"+curChar, fv, added);                        //25: t0c0
		add("26="+curChar+"_"+curPos+"_"+startChar, fv, added);          //26: c0t0start(w0)
		add("31="+curChar+"_"+curPos+"_"+preChar, fv, added);            //31: c0t0c-1
    			
    }
    
    
  
    
    /**
     * 添加一个词和词性组合类的特征
     * @since Apr 9, 2012
     * @param inst
     * @param item 当前词和词性
     * @param fv
     * @param added
     */
    public void addPOSFeatures(Instance inst, Item item, 
    		FeatureVector fv, boolean added) {
    	
    	int MaxWordLength = 15;
    	int sentenceLength = inst.charLength();

    	String[] chars = inst.chars;
    	
    	Item preItem = null;
    	Item pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}
    	Item nextItem = null;
    	Item next2Item = null;
    	if(item.right != null) {
    		nextItem = item.right;
    		if(nextItem.right != null) {
    			next2Item = nextItem.right;
    		}
    	}    	
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos;  //当前词的长度
    	
    	int wordLength = curWord.length();
    	if(wordLength > MaxWordLength)
			wordLength = MaxWordLength;
    	
		String startChar = curWord.substring(0,1);         //当前词的首字符
		String endChar  = curWord.substring(wordLength-1); //当前词的尾字符
		
		String preWord = "preWord";                        //前一个词
		String pre2Word = "pre2Word";
		String endChar_preWord = "eCpW";                //前一词的尾字符
		int preWordLength = 0;						    //前一词的长度
		if(preItem != null){
			preWord = preItem.form;
			preWordLength = preWord.length();
			if(preWordLength > MaxWordLength)
				preWordLength = 15;
			endChar_preWord = preWord.substring(preWordLength-1);
		}
		if(pre2Item != null)
			pre2Word = pre2Item.form;
		
		String startChar_nextWord = "sCnW";              //后一词的首字符
		int endCharPosition = item.t;
		if(endCharPosition < sentenceLength){
			startChar_nextWord = chars[endCharPosition];
		}		

		String nextWord = "NextWord";                       
		String next2Word = "Next2Word";
		if(nextItem != null)
			nextWord = nextItem.form;
		if(next2Item != null)
			next2Word = next2Item.form;
		

		//String pos = item.pos;       //当前POS
		String prePos = "PrePOS";                        //前一个POS
		String pre2Pos = "Pre2POS";                       //前第二个POS
		if(preItem != null){
			prePos = preItem.pos;
		}
		if(pre2Item != null){
			pre2Pos = pre2Item.pos;
		}
		
		add("12="+pre2Word+"_"+curPos, fv, added);
		add("13="+nextWord+"_"+curPos, fv, added);
		add("14="+next2Word+"_"+curPos, fv, added);
		
		add("15="+curWord+"_"+curPos, fv, added); //tag t with word w
		add("16="+prePos+"_"+curPos, fv, added); //tag bigram t1t2
		add("17="+pre2Pos+"_"+prePos+"_"+curPos, fv, added); //tag trigram t1t2t3
		add("18="+preWord+"_"+curPos, fv, added); //tag t followed by word w
		add("19="+prePos+"_"+curWord, fv, added); //word w followed by tag t
		add("20="+endChar_preWord+"_"+curWord+"_"+curPos, fv, added); //word w with tag t and previous character c
		add("21="+curWord+"_"+curPos+"_"+startChar_nextWord, fv, added);//word w with tag t and next character c
		if(wordLength == 1)        //tag t on single-character word w in character trigram c1wc2  
			add("22="+endChar_preWord+"_"+curWord+"_"+curPos+"_"+startChar_nextWord, fv, added);
		add("23="+startChar+"_"+curPos, fv, added); //tag t on a word starting with char c
		add("24="+endChar+"_"+curPos, fv, added); // tag t on a word ending with char c  
		if(wordLength > 2) {        
			for(int i=1; i<wordLength-1; i++) {
				add("25="+curWord.substring(i,i+1)+"_"+curPos, fv, added); // tag t on a word containing char c(not the starting or ending character)
			}
		}   
		if(wordLength > 1) {        
			for(int i=1; i<wordLength-1; i++) {
				add("26="+startChar+"_"+curWord.substring(i,i+1)+"_"+curPos, fv, added); // tag t on a word starting with char c0 and containing char c
				add("27="+endChar+"_"+curWord.substring(i-1,i)+"_"+curPos, fv, added); // tag t on a word ending with char c0 and containing char c
			}
		}			
		if (wordLength>1) {
			for (int i=0; i<wordLength-1; i++){
				if (curWord.charAt(i) == curWord.charAt(i+1))
					add("28="+curWord.substring(i,i+2)+"_"+curPos, fv, added); // tag t on a word containing repeated char cc
			}
		}
		if (freq.wordCTBMorphPre.containsKey(startChar))
			add("29="+freq.wordCTBMorphPre.get(startChar)+"_"+curPos, fv, added);
		else
			add("29=NONE_"+curPos, fv, added);
		if(freq.wordCTBMorphSuf.containsKey(endChar))
			add("30="+freq.wordCTBMorphSuf.get(endChar)+"_"+curPos, fv, added);
		else
			add("30=NONE_"+curPos, fv, added);       	
    }
    
    
    public void addEnglishPOSFeatures(Instance inst, Item item, 
    		FeatureVector fv, boolean added) {
    	
    	int sentenceLength = inst.charLength();
    	String[] chars = inst.chars;
    	
    	Item preItem = null;
    	Item pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}
    	Item nextItem = null;
    	Item next2Item = null;
    	if(item.right != null) {
    		nextItem = item.right;
    		if(nextItem.right != null) {
    			next2Item = nextItem.right;
    		}
    	}    	
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos;  //当前词的长度
    	
    	int wordLength = curWord.length();
    	
		String startChar = curWord.substring(0,1);         //当前词的首字符
		String endChar  = curWord.substring(wordLength-1); //当前词的尾字符
		
		String preWord = "preWord";                        //前一个词
		String pre2Word = "pre2Word";
		String endChar_preWord = "eCpW";                //前一词的尾字符
		int preWordLength = 0;						    //前一词的长度
		if(preItem != null){
			preWord = preItem.form;
			
		}
		if(pre2Item != null)
			pre2Word = pre2Item.form;
		
		String startChar_nextWord = "sCnW";              //后一词的首字符
		int endCharPosition = item.t;
		if(endCharPosition < sentenceLength){
			startChar_nextWord = chars[endCharPosition];
		}		

		String nextWord = "NextWord";                       
		String next2Word = "Next2Word";
		if(nextItem != null)
			nextWord = nextItem.form;
		if(next2Item != null)
			next2Word = next2Item.form;
		

		//String pos = item.pos;       //当前POS
		String prePos = "PrePOS";                        //前一个POS
		String pre2Pos = "Pre2POS";                       //前第二个POS
		if(preItem != null){
			prePos = preItem.pos;
		}
		if(pre2Item != null){
			pre2Pos = pre2Item.pos;
		}		
		add("1="+curWord+"_"+curPos, fv, added);
		    	
    }
    
    
	
	
}
