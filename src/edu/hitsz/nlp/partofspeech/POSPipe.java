package edu.hitsz.nlp.partofspeech;

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

public class POSPipe {

	public FeatureMap dataMap;
	public FeatureMap tagMap;

    private POSReader reader;
    private POSWriter writer;

    public String[] types;
    public int typeNumber;
    public POSOptions options;
    
    public POSPipe(POSOptions options) throws IOException {

    	dataMap = new FeatureMap();
    	tagMap = new FeatureMap();

    	reader =  new POSReader();
    	writer = new POSWriter();
    	this.options = options;
    }
    
    
    public int[] createInstances(String file,
			 File featFileName) throws IOException {

		readTags(file);					
		System.out.println("Num Features: " + dataMap.size() + ", tag Numbers: "+types.length);
	
		ArrayList<Integer> lengths = new ArrayList<Integer>();
		reader.startReading(file, options);
	
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(featFileName));
	
		POSInstance instance = reader.getNext();
		int num1 = 0;
	
		long begin = System.currentTimeMillis();
		System.out.println("\nCreating Feature Vector Instances: ");
		while(instance != null) {
			
			if(num1%100 == 0 && num1 != 0) {
				System.out.print(num1 + " ");
				if(num1%1000 == 0) {
					System.out.print("\n");
				}
			}	
		    instance.setFeatureVector(createFeatureVector(instance));	
		    lengths.add(instance.getSentenceLength());	
		    writeInstance(instance, out);	
		    instance = null;	
		    instance = reader.getNext();	
		    num1++;
		}		
		System.out.println(num1 + " sentences");		
		System.out.print("time: ");
		System.out.println(System.currentTimeMillis()-begin);
		System.out.println();	
	    out.close();	
		return Array.toArray(lengths);

    }
    
    
    public void writeInstance(POSInstance instance, ObjectOutputStream out){

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
    public POSInstance readInstance(ObjectInputStream in) throws IOException {

		try {
	
		    POSFeatureVector nfv = new POSFeatureVector((int[]) in.readObject());
		    int last = in.readInt();
		    if(last != -2) { System.out.println("Error reading file."); System.exit(0); }
	
		    POSInstance marshalledDI;
		    marshalledDI = (POSInstance)in.readObject();
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
    public int readTags(String file) throws IOException{

    	System.out.print("Read tags from file " +file + "...");
		reader.startReading(file, options);
		POSInstance instance = reader.getNext();
		int sentenceNumber = 0;
		while(instance != null) {
			//
			sentenceNumber++;
		    String[] tags = instance.tags;
		    for(int i = 0; i < tags.length; i++)
		    	tagMap.add(tags[i]);

		    instance = reader.getNext();
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
		System.out.println("\ntag number: " + Integer.toString(typeNumber));
		return sentenceNumber;
    }
    
    public POSFeatureVector createFeatureVector(POSInstance instance) {
		
		String[] words = instance.words;
		String[] tags = instance.tags;				
		
		POSFeatureVector fv = new POSFeatureVector();
		
		final int length = words.length;
		POSItem preItem = null;
		
		for(int i = 0; i < length; i++) {
			POSItem curItem = new POSItem(i, words[i], tags[i], preItem);
			addPOSFeatures(instance, curItem, fv, true);
			preItem = curItem;
		}
		return fv;
    }
    
    
    public void addPOSFeatures(POSInstance inst, POSItem item, 
    		POSFeatureVector fv, boolean added) {
    	   	
    	int sentenceLength = inst.getSentenceLength();
    	String[] words = inst.words;
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos; //当前词的长度
    	
    	//prefix and suffix
    	int wordLength = curWord.length();
    	String[] prefix = new String[4];
    	String[] suffix = new String[4];
    	String prefixWord = wordLength >= 4 ? curWord : curWord + "&&&&";
    	for(int i=0; i<4; i++)
    		prefix[i] = prefixWord.substring(0, i+1);
    	String suffixWord = wordLength >= 4 ? curWord : "&&&&" + curWord;
    	int suffixLength = suffixWord.length();
    	for(int i=0; i<4; i++)
    		suffix[i] = suffixWord.substring(suffixLength-i-1, suffixLength);
    	
    	//number, uppercase character, hyphen
    	String bNumber = "None";
    	String bUppercase = "None";
    	String bHyphen = "None";
    	if(curWord.matches(".*[0-9].*"))
    		bNumber = "CNumber";
    	if(curWord.matches(".*[A-Z].*"))
    		bUppercase = "CUppercase";
    	if(curWord.contains("-"))
    		bHyphen = "CHyphen";
    	   	
    	//preword, pretag
    	POSItem preItem = null;
    	POSItem pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}    	
    	
		String preWord = "preWord";                        //前一个词
		String prePos = "prePos";
		String pre2Word = "pre2Word";
		String pre2Pos = "pre2Pos";
		if(preItem != null){
			preWord = preItem.form;
			prePos = preItem.pos;
			if(pre2Item != null) {
				pre2Word = pre2Item.form;
				pre2Pos = pre2Item.pos;
			}
		}
    	
		//next word
    	int curPosition = item.s;
    	String nextWord = "nextWord";
    	String next2Word = "next2Word";
    	if(curPosition + 1 < sentenceLength) {
    		nextWord = words[curPosition+1];
    		if(curPosition + 2 < sentenceLength)
    			next2Word = words[curPosition+2];
    	}
    	
    	add("1="+curWord+"_"+curPos, fv, added); //
    	for(int i=0; i<4; i++)
    		add(Integer.toString(i+1)+"=" + prefix[i] +"_" + curPos, fv, added);
    	for(int i=0; i<4; i++)
    		add(Integer.toString(i+5)+"=" + suffix[i] +"_" + curPos, fv, added);
    	
    	add("9=" + bNumber +"_" + curPos, fv, added);
    	add("10=" + bUppercase + "_" + curPos, fv, added);
    	add("11=" + bHyphen + "_" + curPos, fv, added);
    	
    	add("12=" + prePos + "_" + curPos, fv, added);
    	add("13=" + pre2Pos + "_" + prePos + "_" + curPos, fv, added);
    	
    	add("14=" + pre2Word + "_" + curPos, fv, added);
    	add("15=" + preWord + "_" + curPos, fv, added);
    	add("16=" + nextWord + "_" + curPos, fv, added);
    	add("17=" + next2Word + "_" + curPos, fv, added);		
		
    }
    
    public void addPOSFeaturesClean(POSInstance inst, POSItem item, 
    		POSFeatureVector fv, boolean added) {

    	int sentenceLength = inst.getSentenceLength();
    	String[] words = inst.words;
    	
    	String curWord = item.form; //当前词
    	String curPos = item.pos; //当前词的长度
    	
    	//prefix and suffix
    	int wordLength = curWord.length();
    	String[] prefix = new String[4];
    	String[] suffix = new String[4];
    	int i=0;
    	for(; i<Math.min(wordLength, 4); i++)
    		prefix[i] = curWord.substring(0, i+1);
    	for(; i<4; i++)
    		prefix[i] = "";
    	i = 0;
    	for(; i<Math.min(wordLength, 4); i++)
    		suffix[i] = curWord.substring(wordLength-i-1, wordLength);
    	for(; i<4; i++)
    		suffix[i] = "";
    	
    	//number, uppercase character, hyphen
    	String bNumber = "None";
    	String bUppercase = "None";
    	String bHyphen = "None";
    	if(curWord.matches(".*[0-9].*"))
    		bNumber = "CNumber";
    	if(curWord.matches(".*[A-Z].*"))
    		bUppercase = "CUppercase";
    	if(curWord.contains("-"))
    		bHyphen = "CHyphen";
    	   	
    	//preword, pretag
    	POSItem preItem = null;
    	POSItem pre2Item = null;
    	if(item.left != null) {
    		preItem = item.left;
    		if(preItem.left != null) {
    			pre2Item = preItem.left;
    		}
    	}    	
    	
		String preWord = "preWord";                        //前一个词
		String prePos = "prePos";
		String pre2Word = "pre2Word";
		String pre2Pos = "pre2Pos";
		if(preItem != null){
			preWord = preItem.form;
			prePos = preItem.pos;
			if(pre2Item != null) {
				pre2Word = pre2Item.form;
				pre2Pos = pre2Item.pos;
			}
		}
    	
		//next word
    	int curPosition = item.s;
    	String nextWord = "nextWord";
    	String next2Word = "next2Word";
    	if(curPosition + 1 < sentenceLength) {
    		nextWord = words[curPosition+1];
    		if(curPosition + 2 < sentenceLength)
    			next2Word = words[curPosition+2];
    	}
    	
    	add("1="+curWord+"_"+curPos, fv, added); //
    	for(i=0; i<4; i++)
    		if(prefix[i].length() > 0)
    			add(Integer.toString(i+1)+"=" + prefix[i] +"_" + curPos, fv, added);
    	for(i=0; i<4; i++)
    		if(suffix[i].length() > 0)
    			add(Integer.toString(i+5)+"=" + suffix[i] +"_" + curPos, fv, added);
    	
    	if(bNumber != "None")
    		add("9=" + bNumber +"_" + curPos, fv, added);
    	if(bUppercase != "None")
    		add("10=" + bUppercase + "_" + curPos, fv, added);
    	if(bUppercase != "None")
    		add("11=" + bHyphen + "_" + curPos, fv, added);
    	
    	add("12=" + prePos + "_" + curPos, fv, added);
    	add("13=" + pre2Pos + "_" + prePos + "_" + curPos, fv, added);
    	
    	add("14=" + pre2Word + "_" + curPos, fv, added);
    	add("15=" + preWord + "_" + curPos, fv, added);
    	add("16=" + nextWord + "_" + curPos, fv, added);
    	add("17=" + next2Word + "_" + curPos, fv, added);		
		
    }
    
    
    public void add(String feat, POSFeatureVector fv, boolean added) {    	
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
     * 初始化写入文件
     * @param file
     * @throws IOException
     */
    public void initInputFile (String file) throws IOException {
    	reader.startReading(file, options);
    }
    
    
    public POSInstance nextInstance() throws IOException {
    	POSInstance instance = reader.getNext();
    	return instance;
    }
    
    protected POSInstance nextRawInstance() throws IOException {
    	POSInstance instance = reader.getNextRaw();
    	return instance;
    }
    

    /**
     * 初始化输出文件
     * @param file
     * @throws IOException
     */
    public void initOutputFile (String file) throws IOException {
		writer.startWriting(file, options);
    }
    
    
    public void outputInstance(POSInstance instance) throws IOException {
    	writer.write(instance);    	
    }

    public void close () throws IOException {
		if (null != writer) {
		    writer.finishWriting();
		}
    }
    

	public static void main(String[] argv) {
		
		String curWord = "a9-aa";
		String bNumber = "None";
		String bUppercase = "None";
		String bHyphen = "None";
		if(curWord.matches(".*[0-9].*"))
			bNumber = "CNumber";
		if(curWord.matches(".*[A-Z].*"))
			bUppercase = "CUppercase";
		if(curWord.contains("-"))
			bHyphen = "CHyphen";
		System.out.println(bNumber);
		System.out.println(bUppercase);
		System.out.println(bHyphen);
	}
	    
}
	

