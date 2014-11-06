/**
 *
 */
package edu.hitsz.nlp.segpos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;



/**
 * 
 * 中文分词和词性标注联合学习主程序
 * joint chinese segmentation and part-of-speech tagging
 * Ng and Low, 2004; Jiang, 2008； Zhang and Clark, 2008)
 * @author tm
 * 
 */
public class Parser {

	public Options options;
	
	public CharPipe charPipe;
	public WordPipe wordPipe;
	
	public Parameters charParams;
	public Parameters wordParams;
	
	public CharDecoder charDecoder;
	public WordDecoder wordDecoder;
	public JointDecoder jointDecoder;
	
	

	public Parser(CharPipe pipe, Options options){
		this.options = options;
		this.charPipe = pipe;
		this.charParams = new Parameters();
		this.charDecoder = new CharDecoder(pipe);
	}
	
	public Parser(WordPipe pipe, Options options){
		this.options = options;
		this.wordPipe = pipe;
		this.wordParams = new Parameters();
		this.wordDecoder = new WordDecoder(pipe);
	}	
	
	public Parser(CharPipe charPipe, WordPipe wordPipe, Options options) {
		this.options = options;
		this.charPipe = charPipe;
		this.wordPipe = wordPipe;
		this.charParams = new Parameters();
		this.wordParams = new Parameters();
		this.jointDecoder =  new JointDecoder(charPipe, wordPipe);
	}
	
   public void trainChar(int[] instanceLengths, String trainfile, File train_forest)
		   throws IOException {
	   	//
	   int[] iter = new int[1];
	   iter[0] = 0;
	   
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print("\nIteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print(" [");

		    long start = System.currentTimeMillis();

		    //trainingIterChar(instanceLengths, trainfile, train_forest, i+1);
		    trainingIterChar(instanceLengths, trainfile, train_forest, iter);
		    
		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");		

			Parameters newParams = charParams.copy();			
			//charParams.averagedAll(i+1, instanceLengths.length);
			charParams.updateAll(iter[0]);
			charParams.averagedAll(iter[0]);	
			saveCharModel(options.charModelFile+"-"+i);			
			charParams = newParams;
			
			if(options.dev){
				outputBestChar(options.testFile, options.outputFile + "-" + i);
				if(options.eval) {
					Evaluation.eval(options.trainFile, options.goldFile, options.outputFile+"-"+i, options.evalFile+"-"+i);
				}
			}
		
		}
    }


	//private void trainingIterChar(int[] instanceLengths, String trainfile,
	//		      File train_forest, int iter) throws IOException {
	private void trainingIterChar(int[] instanceLengths, String trainfile,
			      File train_forest, int[] iter) throws IOException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));

		int numInstances = instanceLengths.length;

		for(int i = 0; i < numInstances; i++) {
			
			if(i%100 == 0) {
				System.out.print((i+1)+" ");
				if(i%1000 == 100) {
					System.out.print("\n");
				}
			}

		    Instance inst;

		    inst = charPipe.readInstance(in);
		    
		    FeatureVector cfv = charDecoder.getFeatureVector(charParams, inst, options.K);

		    //charParams.update(inst.fv.fv, cfv.fv, numInstances, i);
		    charParams.update(inst.fv.fv, cfv.fv, iter[0]);
		    iter[0] += 1;
		}
		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");

		System.out.println(numInstances);

		in.close();

	}
	
	/** 
	 * 找到最优结果
	 * @since Aug 28, 2012
	 * @param testfile
	 * @param outputfile
	 * @throws IOException
	 */
	public void outputBestChar(String testfile, String outputfile) throws IOException{
    	
		long start = System.currentTimeMillis();

    	charPipe.initInputFile(testfile);
    	charPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = null;
    	inst = charPipe.nextRawInstance();
    	
    	int cnt = 0;
    	while(inst != null) {
    		System.out.print((cnt+1)+" ");
    	    if(cnt%100 == 0) {
				System.out.print((cnt+1)+" ");
				if(cnt%1000 == 100) {
					System.out.print("\n");
				}
			}
    	    cnt++;
    	    if(cnt==13)
    	    	System.out.println();
    	    
		    Instance newInst = charDecoder.getBestInstance(charParams, inst, options.K);   	    
    	    
		    charPipe.outputInstance(newInst);
		   
		    inst = charPipe.nextRawInstance();  
		       	    
    	}    	
    	charPipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));
	}
	
	
	public void outputBestCharMultiThread(String testfile, String outputfile) throws IOException, InterruptedException, ExecutionException{
    	long start = System.currentTimeMillis();

    	ExecutorService pool = Executors.newFixedThreadPool(options.multiThread); 
    	ArrayList<Instance> insts = new ArrayList<Instance>();
    	
    	charPipe.initInputFile(testfile);
    	charPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = null;
    	inst = charPipe.nextRawInstance();
    	
    	int cnt = 0;
    	while(inst != null) {
    	    System.out.print(cnt+" ");
    	    int count = 0;
    	    while(count < options.multiThread && inst !=null ) {
    	    	insts.add(inst);
    	    	count++;
    	    	inst = charPipe.nextRawInstance();
    	    }
    	    Hashtable<Integer, Instance> newInsts = new Hashtable<Integer,Instance>();
    	    for(int i=0; i<count; i++) {
    	    	Callable c = new DecoderMultiThread(charDecoder, charParams, insts.get(i), options.K);
    	    	Future f = pool.submit(c);
    	    	newInsts.put(i, (Instance) f.get());
    	    }
    	    while(true) {
    	    	if(newInsts.size() == count){
		    	    for(int i=0; i<count; i++) {
		    	    	 charPipe.outputInstance(newInsts.get(i));
		    	    }
		    	    insts.clear();
		    	    break;
    	    	}
    	    	else
    	    		Thread.sleep(10); 	    	     
    	    }  
    	    cnt += count;
    	    pool.shutdown();
    	    pool = Executors.newFixedThreadPool(options.multiThread);
    	}
    	
    	charPipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	
	
	
	public void trainWord(int[] instanceLengths, String trainfile, File train_forest)
			throws IOException {

		//
		int[] iter = new int[1];
		iter[0] = 0;
		   
		//System.out.print("About to train. ");
		//System.out.print("Num Feats: " + pipe.dataAlphabet.size());
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print(" Iteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print("[");

		    long start = System.currentTimeMillis();

		    //trainingIterWord(instanceLengths, trainfile, train_forest, i+1);
		    trainingIterWord(instanceLengths, trainfile, train_forest, iter);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");
		    
		    Parameters newParams = wordParams.copy();			
			//wordParams.averagedAll(i+1, instanceLengths.length);		
			wordParams.updateAll(iter[0]);
			wordParams.averagedAll(iter[0]);	
			saveWordModel(options.wordModelFile+"-"+i);			
			wordParams = newParams;			

			if(options.dev){
				outputBestWord(options.testFile, options.outputFile + "-" + i);
				if(options.eval) {
					Evaluation.eval(options.trainFile, options.goldFile, options.outputFile+"-"+i, options.evalFile+"-"+i);
				}
			}		
		}
	}


	private void trainingIterWord(int[] instanceLengths, String trainfile,
			      File train_forest, int[] iter) throws IOException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));

		int numInstances = instanceLengths.length;

		for(int i = 0; i < numInstances; i++) {
			
			if(i%100 == 0) {
				System.out.print((i+1)+" ");
				if(i%1000 == 100) {
					System.out.print("\n");
				}
			}

		    Instance inst;

		    inst = wordPipe.readInstance(in);
		    
		    FeatureVector cfv = wordDecoder.getFeatureVector(wordParams, inst, options.K);

		    //wordParams.update(inst.fv.fv, cfv.fv, numInstances, i);
		    wordParams.update(inst.fv.fv, cfv.fv, iter[0]);
		    iter[0] += 1;

		}

		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");

		System.out.println(numInstances);

		in.close();

	}



	public void outputBestWord(String testfile, String outputfile) throws IOException{
    	long start = System.currentTimeMillis();

    	wordPipe.initInputFile(testfile);
    	wordPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = wordPipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    		if(cnt%100 == 0) {
				System.out.print((cnt+1)+" ");
				if(cnt%1000 == 100) {
					System.out.print("\n");
				}
			}
    	    cnt++;
    	    
		    int K = 16;		    
		    Instance newInst = wordDecoder.getBestInstance(wordParams, inst, K);   	    
    	    
		    wordPipe.outputInstance(newInst);
		    
    	    inst = wordPipe.nextRawInstance();     	    
    	    
    	}
    	
    	wordPipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	
	public void outputBestJoint(String testfile, String outputfile, double alpha) throws IOException{
    	long start = System.currentTimeMillis();

    	wordPipe.initInputFile(testfile);
    	wordPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = wordPipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    System.out.print(cnt+" ");
    	    
		    int K = options.K;		    
		    Instance newInst = jointDecoder.getBest(charParams, wordParams, inst, K, alpha);   	    
    	    
		    wordPipe.outputInstance(newInst);
		    
    	    inst = wordPipe.nextRawInstance();     	    
    	    
    	}
    	
    	wordPipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	public void outputBestJointMultiThread(String testfile, String outputfile, double alpha) throws IOException, InterruptedException, ExecutionException{
    	long start = System.currentTimeMillis();

    	ExecutorService pool = Executors.newFixedThreadPool(options.multiThread); 
    	ArrayList<Instance> insts = new ArrayList<Instance>();
    	
    	wordPipe.initInputFile(testfile);
    	wordPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = wordPipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    System.out.print(cnt+" ");
    	    int count = 0;
    	    while(count < options.multiThread && inst !=null ) {
    	    	insts.add(inst);
    	    	count++;
    	    	inst = wordPipe.nextRawInstance();
    	    }
    	    Hashtable<Integer, Instance> newInsts = new Hashtable<Integer,Instance>();
    	    for(int i=0; i<count; i++) {
    	    	Callable c = new DecoderMultiThread(jointDecoder, charParams, wordParams, insts.get(i), options.K, alpha);
    	    	Future f = pool.submit(c);
    	    	newInsts.put(i, (Instance) f.get());
    	    }    	    
    	    while(true) {
    	    	if(newInsts.size() == count){
		    	    for(int i=0; i<count; i++) {
		    	    	 charPipe.outputInstance(newInsts.get(i));
		    	    }
		    	    insts.clear();
		    	    break;
    	    	}
    	    	else
    	    		Thread.sleep(10); 	    	     
    	    }  
    	    cnt += count;
    	    pool.shutdown();
    	    pool = Executors.newFixedThreadPool(options.multiThread);    	    
    	}    	
    	
    	wordPipe.close();
    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	
	public void trainSWord(int instanceNumber, String trainfile)
			throws IOException {

		//System.out.print("About to train. ");
		//迭代次数
		int[] iter = new int[1];
		iter[0] = 0;
		
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print(" Iteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print("[");

		    long start = System.currentTimeMillis();

		    trainingIterSWord(instanceNumber, trainfile, iter, i);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");
		    
		    Parameters newParams = wordParams.copy();
		    wordParams.updateAll(iter[0]);
			wordParams.averagedAll(iter[0]);			
			saveSWordModel(options.wordModelFile+"-"+i);			
			wordParams = newParams;

			if(options.dev){
				outputBestSWord(options.testFile, options.outputFile + "-" + i);
				if(options.eval)
					Evaluation.eval(options.trainFile, options.goldFile, options.outputFile+"-"+i, options.evalFile+"-"+i);
			}
		
		}

		//wordParams.averagedAll(i, instanceLengths.length);

	}
	
	private void trainingIterSWord(int instanceNumber, String trainfile,
			      int[] iter, int iteration) throws IOException {

		wordPipe.initInputFile(trainfile);
		if(iteration == 0) 
			wordPipe.dict.initialize(wordPipe.types);
		
		for(int i = 0; i < instanceNumber; i++) {
			
			if(i%100 == 0) {
				System.out.print((i+1)+" ");
				if(i%1000 == 100) {
					System.out.print("\n");
				}
			}
			
		   Instance instance = wordPipe.nextInstance();
		   //wordPipe.createFeatureVectorSingleBeam(instance);
		    
		   if(iteration == 0)
			   wordPipe.dict.processInstance(instance);
		   
		   wordDecoder.kbeam(wordParams, instance, options.K, iter, true);
		   
		}

		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");

		System.out.println(instanceNumber);
		
	}
	
	
	public void outputBestSWord(String testfile, String outputfile) throws IOException{
    	long start = System.currentTimeMillis();
    	
    	int[] iter = new int[1];

    	wordPipe.initInputFile(testfile);
    	wordPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = wordPipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    		if(cnt%100 == 0) {
				System.out.print((cnt+1)+" ");
				if(cnt%1000 == 100) {
					System.out.print("\n");
				}
			}
    	    cnt++;
    	    	    
		    Instance newInst = wordDecoder.kbeam(wordParams, inst, options.K, iter, false);   	    
    	    
		    wordPipe.outputInstance(newInst);
		    
    	    inst = wordPipe.nextRawInstance();     	    
    	    
    	}
    	
    	wordPipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	
	/**
	 * 训练POS Tagging
	 * @since Dec 3, 2012
	 * @param instanceLengths
	 * @param trainfile
	 * @param train_forest
	 * @throws IOException
	 */
	public void trainPOS(int[] instanceLengths, String trainfile, File train_forest)
			throws IOException {

		//System.out.print("About to train. ");
		//System.out.print("Num Feats: " + pipe.dataAlphabet.size());
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print(" Iteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print("[");

		    long start = System.currentTimeMillis();

		    trainingIterPOS(instanceLengths, trainfile, train_forest, i+1);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");
		    
		    Parameters newParams = wordParams.copy();			
			wordParams.averagedAll(i+1, instanceLengths.length);			
			saveWordModel(options.wordModelFile+"-"+i);			
			wordParams = newParams;
		
		}

		//wordParams.averagedAll(i, instanceLengths.length);

	}


	private void trainingIterPOS(int[] instanceLengths, String trainfile,
			      File train_forest, int iter) throws IOException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));

		int numInstances = instanceLengths.length;

		for(int i = 0; i < numInstances; i++) {
			
			if(i%10 == 0) {
				System.out.print((i+1)+" ");
			}

		    Instance inst;

		    inst = wordPipe.readInstance(in);
		    
		    FeatureVector cfv = wordDecoder.getFeatureVectorPOS(wordParams, inst, options.K);

		    wordParams.update(inst.fv.fv, cfv.fv, numInstances, i);

		}

		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");

		System.out.println(numInstances);

		in.close();

	}



	public void outputBestPOS(String testfile, String outputfile) throws IOException{
    	long start = System.currentTimeMillis();

    	wordPipe.initInputFile(testfile);
    	wordPipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	Instance inst = wordPipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    if(cnt % 1000 == 0) {
    	    	System.out.print(cnt+" ");
    	    	 if(cnt % 10000 == 0) {
    	    		 System.out.println();
    	    	 }
    	    }
    	    	    
		    Instance newInst = (Instance) wordDecoder.getBestPOS(wordParams, inst, options.K)[0];   	    
    	    
		    wordPipe.outputInstance(newInst);
		    
    	    inst = wordPipe.nextRawInstance();     	    
    	    
    	}
    	System.out.println(cnt);
    	wordPipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	public Object[] getBestPOS(Instance instance) {
		
		return wordDecoder.getBestPOS(wordParams, instance, options.K); 
	}

	

	/**
	 * 检测charparser产生的lattice的oracle值
	 * @since Apr 19, 2012
	 * @throws Exception
	 */
	public void outputLatticeChar(String charModelFile,
			String testFile,
			String oracleFile,
			int oracleK,
			boolean endAlign) throws Exception{
		
    	long start = System.currentTimeMillis();    	
		loadCharModel(charModelFile);
		charPipe.initInputFile(testFile);
		charPipe.initOutputFile(oracleFile);
		Instance inst = charPipe.nextRawInstance();
		    	
    	int cnt = 0;
		while(inst != null) { 
		    System.out.print(cnt+" ");
		    Lattice lattice = charDecoder.kbeamHeap(charParams, inst, oracleK);
		    if(options.devReverse)
		    	lattice = lattice.reverseLattice();
		    if(!endAlign)
		    	lattice = lattice.flap2WordBgnLattice();
		    charPipe.outputWPLattice(lattice, cnt);
	    	inst = charPipe.nextRawInstance();   
		    cnt++;    	   
		}
		charPipe.close();
    			
		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end-start));
	}
		

	
	/**
	 * 计算Word Parser产生的wordpos lattice的oracle值
	 * @since Apr 19, 2012
	 * @throws Exception
	 */
	public static void outputLatticeWord(Options options) throws Exception{

    	long start = System.currentTimeMillis();
		
    	WordPipe pipe = new WordPipe(options);
		Parser parser = new Parser(pipe, options);
		parser.loadWordModel(options.wordModelFile);
		parser.wordPipe.initInputFile(options.testFile);
    	
    	System.out.print("Processing Sentence: ");
    	Instance inst = parser.wordPipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    System.out.print(cnt+" ");
    	    Lattice lattice = parser.wordDecoder.mkbeam(parser.wordParams, inst, options.oracleK);		
	    	Lattice newForwardLattice = lattice.flap2WordBgnLattice();	    	
	    	parser.charPipe.outputWPLattice(newForwardLattice, cnt);
	    	
    	    inst = parser.wordPipe.nextRawInstance();  
    	    cnt++;    	    
    	}
    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
		
	public Object[] getCharLMR(Instance inst) {
		return charDecoder.getCharLMR(charParams, inst);
	}
	

    public void saveCharModel(String file) throws IOException {
    	System.out.print("Saving model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
    			new RandomAccessFile(file, "wr").getFD()));
    	out.writeObject(charParams);
    	out.writeObject(charPipe.freq);
    	out.writeObject(charPipe.dataMap);
    	out.writeObject(charPipe.tagMap);
    	out.writeObject(charPipe.types);
    	out.close();
    	System.out.println(" done.");
    	System.out.println("feature numbers: "+charPipe.dataMap.getMap().size());
    }

    public void loadCharModel(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
    			new RandomAccessFile(file, "r").getFD()));
    	//ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    	charParams = (Parameters) in.readObject();
    	charPipe.freq = (WordPosFreq) in.readObject();
    	charPipe.dataMap = (FeatureMap) in.readObject();
    	charPipe.tagMap = (FeatureMap) in.readObject();
    	charPipe.types = (String[]) in.readObject();
    	in.close();
    	System.out.println("done");
    }
    
    public void saveWordModel(String file) throws IOException {
    	System.out.print("Loading model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
    			new RandomAccessFile(file, "wr").getFD()));
    	out.writeObject(wordParams);
    	out.writeObject(wordPipe.freq);
    	out.writeObject(wordPipe.dataMap);
    	out.writeObject(wordPipe.tagMap);
    	out.writeObject(wordPipe.types);
    	out.close();
    	System.out.println("done.");
    	System.out.println("feature numbers: "+wordPipe.dataMap.getMap().size());
    }
    
    public void loadWordModel(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
    			new RandomAccessFile(file, "r").getFD()));
    	wordParams = (Parameters) in.readObject();
    	wordPipe.freq = (WordPosFreq) in.readObject();
    	wordPipe.dataMap = (FeatureMap) in.readObject();
    	wordPipe.tagMap = (FeatureMap) in.readObject();
    	wordPipe.types = (String[]) in.readObject();
    	in.close();
    	System.out.println("done");    	
    }
    
    
    public void saveSWordModel(String file) throws IOException {
    	System.out.print("Saving model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
    			new RandomAccessFile(file, "wr").getFD()));
    	out.writeObject(wordParams);
    	out.writeObject(wordPipe.dataMap);
    	out.writeObject(wordPipe.tagMap);
    	out.writeObject(wordPipe.dict);
    	out.writeObject(wordPipe.morphFea);
    	out.writeObject(wordPipe.types);
    	out.close();
    	System.out.println("done.");
    	System.out.println("feature numbers: "+wordPipe.dataMap.getMap().size());
    }
    
    public void loadSWordModel(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
    			new RandomAccessFile(file, "r").getFD()));
    	wordParams = (Parameters) in.readObject();
    	wordPipe.dataMap = (FeatureMap) in.readObject();
    	wordPipe.tagMap = (FeatureMap) in.readObject();
    	wordPipe.dict = (TagDict) in.readObject();
    	wordPipe.morphFea = (MorphFeature) in.readObject();
    	wordPipe.types = (String[]) in.readObject();
    	in.close();
    	System.out.println("done");
    	
    }
    
    public void loadJointModel(String charfile, String wordfile) throws Exception {
 		System.out.print("Loading model...");
     	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
    			new RandomAccessFile(charfile, "r").getFD()));
     	charParams = (Parameters) in.readObject();
     	charPipe.freq = (WordPosFreq) in.readObject();
     	charPipe.dataMap = (FeatureMap) in.readObject();
     	charPipe.tagMap = (FeatureMap) in.readObject();
     	charPipe.types = (String[]) in.readObject();
     	in.close();
     	in = new ObjectInputStream(new FileInputStream(
    			new RandomAccessFile(wordfile, "r").getFD()));
     	wordParams = (Parameters) in.readObject();
     	wordPipe.freq = (WordPosFreq) in.readObject();
     	wordPipe.dataMap = (FeatureMap) in.readObject();
     	wordPipe.tagMap = (FeatureMap) in.readObject();
     	wordPipe.types = (String[]) in.readObject();
     	in.close();
     	System.out.println("done");
     }


    public static void usage() {
    	
		System.out.println("Train in char model:\n modelType:char [charFeatureType:nglow] train:true " +
				"trainFile:train-file " + "latticeFile:lattice-file " +
				"modelFile:model-file " + "numIters:iter-number");
		System.out.println("Test in char model:\n modelType:char [charFeatureType:nglow] test:true " +
				"modelFile:model-file " +
				"testFile:test-file " + "latticeFile:lattice-file " +
				"outputFile:output-file " +
				"startIter:start-iter " + "numIters:iter-number");
		System.out.println("Eval in char model:\n modelType:char [charFeatureType:nglow] eval:true " +
				"trainFile:train-file goldFile:gold-file " +
				"outputFile:test-file " + "evalFile:eval-file " +
				"startIter:start-iter " + "numIters:iter-number ");
		System.out.println("\nFor word model: change every char in above command string into word");
		
	}



	public static void main(String[] args) throws Exception{
		
		for(String arg : args)
			System.out.print(arg + " ");
		System.out.println();
		long start = System.currentTimeMillis();
		Options options = new Options(args);
		if(options.modelType.equals("char")) {
			if(options.train){
				CharPipe pipe = new CharPipe(options);
				int[] instanceLengths = pipe.createInstances(options.trainFile, new File(options.latticeFile));
				Parser charParser = new Parser(pipe, options);	
				int numFeats = pipe.dataMap.size();
				int numTypes = pipe.types.length;
				System.out.print("Current Num Feats: " + numFeats);
				System.out.println(".\tNum Labels: " + numTypes);
				charParser.trainChar(instanceLengths, options.trainFile, new File(options.latticeFile));
			}
			if(!options.train && options.dev){
				for(int i = 0; i < options.numIters; i++) {
					CharPipe pipe = new CharPipe(options);
					Parser charParser = new Parser(pipe, options);
					charParser.loadCharModel(options.charModelFile + "-" + i);
					charParser.outputBestChar(options.testFile, options.outputFile + "-" + i);
					if(options.eval) {
						Evaluation.eval(options.trainFile, options.goldFile, options.outputFile+"-"+i, options.evalFile+"-"+i);
					}
				}
			}
			else if(options.test){
				CharPipe pipe = new CharPipe(options);
				Parser charParser = new Parser(pipe, options);
				charParser.loadCharModel(options.charModelFile);
				charParser.outputBestChar(options.testFile, options.outputFile);		
				//charParser.outputBestCharMultiThread(options.testFile, options.outputFile);
			}
			else if(options.oracle) {
		    	CharPipe forwardPipe = new CharPipe(options);
				Parser parser = new Parser(forwardPipe, options);
				parser.outputLatticeChar(options.charModelFile, options.testFile, options.oracleFile, options.oracleK, false);
			}
		}
		if(options.modelType.equals("word")) {
			if(options.train){
				WordPipe pipe = new WordPipe(options);
				int[] instanceLengths = pipe.createInstances(options.trainFile, new File(options.latticeFile));	
				Parser wordParser = new Parser(pipe, options);	
			    int numFeats = pipe.dataMap.size();
			    int numTypes = pipe.types.length;
			    System.out.print("Num Feats: " + numFeats);
			    System.out.println(".\tNum Labels: " + numTypes);
			    wordParser.trainWord(instanceLengths, options.trainFile, new File(options.latticeFile));		    			    
			}
			if(!options.train && options.dev){
				for(int i = 0; i < options.numIters; i++) {
					WordPipe pipe = new WordPipe(options);
					Parser wordParser = new Parser(pipe, options);
					wordParser.loadWordModel(options.wordModelFile + "-" + i);
					wordParser.outputBestWord(options.testFile, options.outputFile + "-" + i);
					if(options.eval) {
						Evaluation.eval(options.trainFile, options.goldFile, options.outputFile+"-"+i, options.evalFile+"-"+i);
					}	
				}
			}
			else if(options.test){
				WordPipe pipe = new WordPipe(options);
				Parser wordParser = new Parser(pipe, options);
				wordParser.loadWordModel(options.wordModelFile);
				wordParser.outputBestWord(options.testFile, options.outputFile);
			}
			else if(options.oracle) {
				outputLatticeWord(options);
			}
		}
		else if(options.modelType.equals("joint")) {
			if(options.dev){
				CharPipe charPipe = new CharPipe(options);
				WordPipe wordPipe = new WordPipe(options);
				Parser jointParser = new Parser(charPipe, wordPipe, options);
				jointParser.loadCharModel(options.charModelFile);
				jointParser.loadWordModel(options.wordModelFile);
				for (int i=1; i<=9; i++) {
					double alpha = i/(double)10;
					jointParser.outputBestJoint(options.testFile, options.outputFile + "-" + i, alpha);
					if(options.eval) {
						Evaluation.eval(options.trainFile, options.goldFile, options.outputFile + "-" + i, options.evalFile + "-" + i);
					}
				}
			}
			if(options.test){
				CharPipe charPipe = new CharPipe(options);
				WordPipe wordPipe = new WordPipe(options);
				Parser jointParser = new Parser(charPipe, wordPipe, options);
				jointParser.loadCharModel(options.charModelFile);
				jointParser.loadWordModel(options.wordModelFile);
				jointParser.outputBestJoint(options.testFile, options.outputFile, options.jointParam);			
			}
		}		
		if(options.modelType.equals("sword")) {
			if(options.train){
				WordPipe pipe = new WordPipe(options);
				int instanceNumber = pipe.preProcessTrainFile(options.trainFile);	
				Parser sWordParser = new Parser(pipe, options);	
				sWordParser.trainSWord(instanceNumber, options.trainFile);			    			    
			}
			if(options.test){
				WordPipe pipe = new WordPipe(options);
				Parser wordParser = new Parser(pipe, options);
				wordParser.loadSWordModel(options.wordModelFile);
				wordParser.outputBestSWord(options.testFile, options.outputFile);
			}			
		}
		if(options.modelType.equals("pos")) {
			if(options.train){
				WordPipe pipe = new WordPipe(options);
				int[] instanceLengths = pipe.createInstancesPOS(options.trainFile, new File(options.latticeFile));
				Parser posParser = new Parser(pipe, options);		
			    int numFeats = pipe.dataMap.size();
			    int numTypes = pipe.types.length;
			    System.out.print("Num Feats: " + numFeats);
			    System.out.println(".\tNum Labels: " + numTypes);
			    posParser.trainPOS(instanceLengths, options.trainFile, new File(options.latticeFile));
			    			    
			}
			if(options.test){
				WordPipe pipe = new WordPipe(options);
				Parser posParser = new Parser(pipe, options);
				posParser.loadWordModel(options.wordModelFile);
				posParser.outputBestPOS(options.testFile, options.outputFile);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("end - start:" + String.valueOf(end-start));
		//usage();
		
		
	}


}
