package edu.hitsz.nlp.partofspeech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;

public class POSParser {

	public POSOptions options;	
	public POSPipe pipe;	
	public Parameters params;
	public POSDecoder decoder;
	
	public POSParser(POSPipe pipe, POSOptions options){
		this.options = options;
		this.pipe = pipe;
		this.params = new Parameters();
		this.decoder = new POSDecoder(pipe);
	}
	
	/**
	 * 训练POS Tagging
	 * @since Dec 3, 2012
	 * @param instanceLengths
	 * @param trainfile
	 * @param train_forest
	 * @throws IOException
	 */
	public void trainPOS(int[] instanceLengths, 
			String trainfile, 
			File train_forest)
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
		    
		    Parameters newParams = params.copy();			
			params.averagedAll(i+1, instanceLengths.length);			
			saveModel(options.posModelFile+"-"+i);			
			params = newParams;
		
		}

		//wordParams.averagedAll(i, instanceLengths.length);

	}


	private void trainingIterPOS(int[] instanceLengths, 
			String trainfile,
			File train_forest, 
			int iter) throws IOException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));

		int numInstances = instanceLengths.length;

		for(int i = 0; i < numInstances; i++) {
			
			if(i%10 == 0) {
				System.out.print((i+1)+" ");
			}

		    POSInstance inst;
		    inst = pipe.readInstance(in);		    
		    POSFeatureVector cfv = decoder.getFeatureVectorPOS(params, inst, options.K);
		    params.update(inst.fv.fv, cfv.fv, numInstances, i);
		    //POSFeatureVector cfv = decoder.getFeatureVectorPOSEarlyUpdate(params, inst, options.K, numInstances, i);
		    
		}

		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");

		System.out.println(numInstances);

		in.close();

	}



	public void outputBestPOS(String testfile, String outputfile) throws IOException{
    	long start = System.currentTimeMillis();

    	pipe.initInputFile(testfile);
    	pipe.initOutputFile(outputfile);

    	System.out.print("Processing Sentence: ");
    	POSInstance inst = pipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    if(cnt % 100 == 0) {
    	    	System.out.print(cnt+" ");
    	    	 if(cnt % 1000 == 0) {
    	    		 System.out.println();
    	    	 }
    	    }
    	    //System.out.println(cnt);
    	    	    
		    POSInstance newInst = decoder.getBestPOS(params, inst, options.K);   	    
    	    
		    pipe.outputInstance(newInst);

    	    //if(cnt==52)
    	    //	System.out.println();
		    
    	    inst = pipe.nextRawInstance();

    	    cnt++;
    	    
    	}
    	System.out.println(cnt);
    	pipe.close();

    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	}
	
	public POSInstance getBestPOS(POSInstance instance) {		
		return decoder.getBestPOS(params, instance, options.K); 
	}
	
	public void outputBestKPOS(String testfile, String outputfile, String outProbFile, int outK) throws IOException{
    	long start = System.currentTimeMillis();
    	int maxK = Math.max(outK, options.K);

    	pipe.initInputFile(testfile);
    	pipe.initOutputFile(outputfile);

    	FileWriter writer = null;
	    if(outProbFile != null) {
	    	writer = new FileWriter(outProbFile);
	    }

    	System.out.print("Processing Sentence: ");
    	POSInstance inst = pipe.nextRawInstance();
    	int cnt = 0;
    	while(inst != null) {
    	    cnt++;
    	    if(cnt % 10 == 0) {
    	    	System.out.print(cnt+" ");
    	    	 if(cnt % 100 == 0) {
    	    		 System.out.println();
    	    	 }
    	    }
    	    	    
		    Object[] objs= decoder.getBestKPOS(params, inst, maxK, outK);   	
		    POSInstance[] newInsts = (POSInstance[]) objs[0];
		    Double[] weights = (Double[]) objs[1];
    	    
		    for(int i=0; i<outK; i++)
		    	pipe.outputInstance(newInsts[i]);
		    
		    if(writer != null) {
		    	for(int i=0;i<outK; i++)
		    		writer.write(weights[i] + "\n");
		    }		    
    	    inst = pipe.nextRawInstance();     	    
    	}
    	System.out.println(cnt);
    	pipe.close();
    	if(writer != null)
    		writer.close();
    	long end = System.currentTimeMillis();
    	System.out.println("Took: " + (end-start));

	} 
	
	
	public void saveModel(String file) throws IOException {
    	System.out.print("Saving model "+file+ " ... ");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
    			new RandomAccessFile(file, "rw").getFD()));
    	out.writeObject(params);
    	out.writeObject(pipe.dataMap);
    	out.writeObject(pipe.tagMap);
    	out.writeObject(pipe.types);
    	out.close();
    	System.out.println("done.");
    	System.out.println("feature numbers: " + pipe.dataMap.getMap().size());
    }
    
    public void loadModel(String file) throws Exception {
		System.out.print("Loading model "+file+ " ... ");
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
    			new RandomAccessFile(file, "r").getFD()));
    	params = (Parameters) in.readObject();
    	pipe.dataMap = (FeatureMap) in.readObject();
    	pipe.tagMap = (FeatureMap) in.readObject();
    	pipe.types = (String[]) in.readObject();
    	in.close();
    	System.out.println("done");    	
    }
	
	public static void main(String[] args) throws Exception{
		
		for(String arg : args)
			System.out.print(arg + " ");
		System.out.println();
		
		long start = System.currentTimeMillis();
		POSOptions options = new POSOptions(args);
		
		
		options.modelType = "pos";
		options.train = false;
		options.dev = false;
		
		options.trainFile = "./train";
		options.latticeFile = "./train.lattice";
		options.numIters = 10;
		options.posModelFile = "./train.model";
		options.testFile = "./test";
		options.outputFile = "./test.result";
		
		
		options.test = true;
		options.posModelFile = "/home/tm/disk/disk1/pos-chunk-rerank/1.pos/15-18wp.model-7";
		options.testFile = "/home/tm/disk/disk1/pos-chunk-rerank/raw/new.wp";
		options.outputFile = "/home/tm/disk/disk1/pos-chunk-rerank/raw/new.wp.result";
				
		/*
		options.bestK = false;
		options.outK = 500;
		options.posModelFile = "/home/tm/disk/disk1/pos-chunk-rerank/4.pos-split/4/train.model-8";
		options.testFile = "/home/tm/disk/disk1/pos-chunk-rerank/4.pos-split/4/15-18wp-test-4";
		options.outputFile = "/home/tm/disk/disk1/pos-chunk-rerank/4.pos-split/4/15-18wp-test-4.500best";
		options.outProbFile = "/home/tm/disk/disk1/pos-chunk-rerank/4.pos-split/4/15-18wp-test-4.500best.prob";
		*/
		
		if(options.modelType.equals("pos")) {
			if(options.train){
				POSPipe pipe = new POSPipe(options);
				int[] instanceLengths = pipe.createInstances(options.trainFile, new File(options.latticeFile));
				POSParser posParser = new POSParser(pipe, options);		
			    int numFeats = pipe.dataMap.size();
			    int numTypes = pipe.types.length;
			    System.out.print("Num Feats: " + numFeats);
			    System.out.println(".\tNum Labels: " + numTypes);
			    posParser.trainPOS(instanceLengths, options.trainFile, new File(options.latticeFile));
			    			    
			}
			if(options.dev){
				for(int i=0; i<options.numIters; i++) {
					POSPipe pipe = new POSPipe(options);
					POSParser posParser = new POSParser(pipe, options);
					posParser.loadModel(options.posModelFile + "-" + i);
					posParser.outputBestPOS(options.testFile, options.outputFile + "-" + i);
				}
			}
			if(options.test){
				POSPipe pipe = new POSPipe(options);
				POSParser posParser = new POSParser(pipe, options);
				posParser.loadModel(options.posModelFile);
				posParser.outputBestPOS(options.testFile, options.outputFile);
			}
			if(options.bestK){
				POSPipe pipe = new POSPipe(options);
				POSParser posParser = new POSParser(pipe, options);
				posParser.loadModel(options.posModelFile);
				posParser.outputBestKPOS(options.testFile, options.outputFile, options.outProbFile, options.outK);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("end - start:" + String.valueOf(end-start));
		//usage();
		
		
	}
}
