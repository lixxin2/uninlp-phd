package edu.hitsz.nlp.mstjoint;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.ml.onlinelearning.ap.Parameters;
import edu.hitsz.nlp.segpos.CharPipe;
import edu.hitsz.nlp.segpos.Options;
import edu.hitsz.nlp.segpos.Parser;


/**
 * 主程序，程序开始点
 * 采用averaged perceptron训练
 * @author Xinxin Li
 * @since Dec 9, 2011
 */
public class DependencyParserJoint {

    public ParserOptionsJoint options;

    private DependencyPipeJoint pipe;
    private DependencyDecoderJoint decoder;
    private Parameters params;
        

    public DependencyParserJoint(DependencyPipeJoint pipe, ParserOptionsJoint options) {
		
    	this.pipe=pipe;
		this.options = options;

		// Set up arrays
		params = new Parameters(pipe.dataAlphabet.size()*2);
		decoder = options.secondOrder ?
		    new DependencyDecoder2OJoint(pipe) : new DependencyDecoderJoint(pipe);
    }
    
    public DependencyParserJoint(DependencyPipeJoint pipe, ParserOptionsJoint options,
    		Parser charParser) {
		this.pipe=pipe;
		this.options = options;

		// Set up arrays
		params = new Parameters(pipe.dataAlphabet.size()*2);
		decoder = options.secondOrder ?
		    new DependencyDecoder2OJoint(pipe) : new DependencyDecoderJoint(pipe);
		    
    }




    
    
    /**
     * 训练模型,没有存储所有特征
     * @param instanceLengths
     * @param trainfile
     * @param train_forest
     * @throws IOException
     */
    public void train(int[] instanceLengths, 
    		String trainfile, String train_forest)
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

		    trainingIter(instanceLengths,trainfile,train_forest,i+1);

		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");
		    System.out.println(pipe.dataAlphabet.size());
		    
		    //for many iteration training
		    Parameters newParams = params.copy();			
			params.averagedAll(i, instanceLengths.length);			
			saveModel(options.modelName+"-"+i);			
			params = newParams;
		}
		//for final iteration training
		//params.averagedAll(options.numIters, instanceLengths.length);//averageParams(i*instanceLengths.length);		
		//saveModel(options.modelName);

    }

    /**
     * 一次迭代训练
     * @param instanceLengths 句子长度数组
     * @param trainfile 训练文件
     * @param train_forest 训练数据森林
     * @param iter 迭代次数
     * @throws IOException
     */
    private void trainingIter(int[] instanceLengths, String trainfile,
			      String train_forest, int iter) throws IOException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));

		int numInstances = instanceLengths.length;

		for(int i = 0; i < numInstances; i++) {
		    if((i+1) % 500 == 0) {
			System.out.print((i+1)+",");
			//System.out.println("  "+(i+1)+" instances");
		    }

		    int length = instanceLengths[i];

		    // Get production crap.
		    FeatureVector[][][] fvs = new FeatureVector[length][length][2];
		    double[][][] probs = new double[length][length][2];
		    FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
		    double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
		    FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
		    double[][][] probs_trips = new double[length][length][length];
		    FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
		    double[][][] probs_sibs = new double[length][length][2];

		    DependencyInstanceJoint inst;
		    
		    if(options.secondOrder) {
		    	inst = pipe.readInstanceLess(in);
		    	/*
		    	inst = ((DependencyPipe2OJoint)pipe).readInstance(in,length,fvs,probs,
					     fvs_trips,probs_trips,
					     fvs_sibs,probs_sibs,
					     nt_fvs,nt_probs,params);
		    	*/
				((DependencyPipe2OJoint)pipe).fillFeatureVectors(inst,fvs,probs,
									    fvs_trips,probs_trips,
									    fvs_sibs,probs_sibs,
									    nt_fvs,nt_probs,params);
		    }
			else {
				inst = pipe.readInstanceLess(in);
				pipe.fillFeatureVectors(inst,fvs,probs,nt_fvs,nt_probs,params);
			}		    

		    //double upd = (double)(options.numIters*numInstances - (numInstances*(iter-1)+(i+1)) + 1);
		    int K = options.trainK;
		    Object[][] d = null;
		    if(options.decodeType.equals("proj")) {
				if(options.secondOrder)
				    d = ((DependencyDecoder2OJoint)decoder).decodeProjective(inst,fvs,probs,
											fvs_trips,probs_trips,
											fvs_sibs,probs_sibs,
											nt_fvs,nt_probs,K);
				else
				    d = decoder.decodeProjective(inst,fvs, probs, nt_fvs, nt_probs, K);
		    }
		    if(options.decodeType.equals("non-proj")) {
				if(options.secondOrder)
				    d = ((DependencyDecoder2OJoint)decoder).decodeNonProjective(inst,fvs,probs,
										       fvs_trips,probs_trips,
										       fvs_sibs,probs_sibs,
										       nt_fvs,nt_probs,K);
				else
				    d = decoder.decodeNonProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
		    }
		    
		   //((FeatureVector) d[0][0]).sfv2fv(pipe);
		    
		    params.update(inst.fv.fv, ((FeatureVector) d[0][0]).fv, numInstances, i);//updateParamsMIRA(inst,d,upd);
		    //System.out.println(i+":"+pipe.dataAlphabet.size());

		}
	
		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");
	
		System.out.print(numInstances);
	
		in.close();

    }
    
    
    /**
     * 联合训练模型，训练数据采用的是存储word lattice,
     * 没有存储所有特征,存储的是word lattice
     * @param instanceSize
     * @param trainfile
     * @param train_forest
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void trainJoint( 
    		String trainfile,
    		String trainforest, 
    		Parser forwardCharParser,
    		Parser backwardCharParser)
	throws IOException, ClassNotFoundException {

	//System.out.print("About to train. ");
	//System.out.print("Num Feats: " + pipe.dataAlphabet.size());

		int i = 0;
		int sentenceNumber = 0;
		for(i = 0; i < options.numIters; i++) {

		    System.out.print(" Iteration "+i);
		    //System.out.println("========================");
		    //System.out.println("Iteration: " + i);
		    //System.out.println("========================");
		    System.out.print("[");

		    long start = System.currentTimeMillis();

		    trainingIterJointLater(trainfile, trainforest, i+1);
		    
		    long end = System.currentTimeMillis();
		    //System.out.println("Training iter took: " + (end-start));
		    System.out.println("|Time:"+(end-start)+"]");
		    System.out.println(pipe.dataAlphabet.size());
		    
		    //for many iteration training
		    Parameters newParams = params.copy();			
			params.averagedAll(i, sentenceNumber);			
			saveModel(options.modelName+"-"+i);			
			params = newParams;
			
		}

    }
    
        
    
    /** lattice已生成，读取的lattice */
    private void trainingIterJointLater(
    		String trainfile,
    		String trainforest, 
    		int iter) throws IOException, ClassNotFoundException {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(trainforest));
	
		int[] sentenceLengths = (int[]) in.readObject();
		int sentenceNumber = sentenceLengths.length;
			
		for(int i = 0; i < sentenceNumber; i++) {
		    //if((i+1) % 500 == 0) {
			System.out.print((i+1)+",");
			//System.out.println("  "+(i+1)+" instances"); 
		    //}			    		
		    
		    Object[] obj = pipe.readInstanceJoint(in, params);
		    HashMap<WordPos, Integer> allWordPos = (HashMap<WordPos, Integer>) obj[0];
		    WordPos[][] leftWordPoses = (WordPos[][]) obj[1];
		    WordPos[][] rightWordPoses = (WordPos[][]) obj[2];
		    WordPos[] leftWordPos = (WordPos[]) obj[3];
		    WordPos[] rightWordPos = (WordPos[]) obj[4];
		    int[][] leftIndexes = (int[][]) obj[5];
		    int[][] rightIndexes = (int[][]) obj[6];		    
		    DependencyInstanceJoint instance = (DependencyInstanceJoint) obj[7];
		    
		    
	    	//int K = options.trainK;
		    FeatureVector fv = null;
		    if(options.decodeType.equals("proj")) {
				if(options.secondOrder){}
				   
				else{
					//Object[] resultObj = decoder.decodeProjectiveJoint(instance, allWordPos, leftWordPoses, rightWordPoses, 
					//		leftWordPos, rightWordPos, options.trainK);
					ParseForestItemJoint[] items = decoder.decodeProjectiveJointIndex(params, instance, allWordPos, leftWordPoses, rightWordPoses, 
							leftWordPos, rightWordPos, leftIndexes, rightIndexes, options.trainK);
				    fv = (FeatureVector) items[0].getFeatureVector();
				}
		    }
		    else if(options.decodeType.equals("non-proj")) {
			
		    }
		    
		    params.update(instance.fv.fv, fv.fv, sentenceNumber, i);//updateParamsMIRA(inst,d,upd);		    
			
		    
		}
		
		System.out.print(sentenceNumber);		
		in.close();
		
    }
	
	    
	    ///////////////////////////////////////////////////////
	    // Saving and loading models
	    ///////////////////////////////////////////////////////
    public void saveModel(String file) throws IOException {
    	System.out.print("Saving model...");
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    	out.writeObject(params);
    	out.writeObject(pipe.dataAlphabet);
    	out.writeObject(pipe.typeAlphabet);
    	out.close();
    	System.out.print("done.");
    }

    /**
     * 导入模型文件，用于测试
     * @since Mar 30, 2012
     * @param file
     * @throws Exception
     */
    public void loadModel(String file) throws Exception {
    	System.out.print("\tLoading model..."+file);    
	    
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		params = (Parameters)in.readObject();
		pipe.dataAlphabet = (FeatureMap) in.readObject();
		pipe.typeAlphabet = (FeatureMap) in.readObject();
		in.close();
		pipe.closeAlphabets();
		System.out.println(" done.");
    }

    //////////////////////////////////////////////////////
    // Get Best Parses ///////////////////////////////////
    //////////////////////////////////////////////////////
    public void outputParses (String testFile, String outFile) throws IOException {


		long start = System.currentTimeMillis();
	
		pipe.initInputFile(testFile);
		pipe.initOutputFile(outFile);
	
		System.out.print("Processing Sentence: ");
		DependencyInstanceJoint instance = pipe.nextInstance();
		int cnt = 0;
		while(instance != null) {
		    cnt++;
		    System.out.print(cnt+" ");
		    String[] forms = instance.forms;
	
		    int length = forms.length;
	
		    FeatureVector[][][] fvs = new FeatureVector[forms.length][forms.length][2];
		    double[][][] probs = new double[forms.length][forms.length][2];
		    FeatureVector[][][][] nt_fvs = new FeatureVector[forms.length][pipe.types.length][2][2];
		    double[][][][] nt_probs = new double[forms.length][pipe.types.length][2][2];
		    FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
		    double[][][] probs_trips = new double[length][length][length];
		    FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
		    double[][][] probs_sibs = new double[length][length][2];
		    if(options.secondOrder)
			((DependencyPipe2OJoint)pipe).fillFeatureVectors(instance,fvs,probs,
								    fvs_trips,probs_trips,
								    fvs_sibs,probs_sibs,
								    nt_fvs,nt_probs,params);
		    else
			pipe.fillFeatureVectors(instance,fvs,probs,nt_fvs,nt_probs,params);
	
		    int K = options.testK;
		    Object[][] d = null;
		    if(options.decodeType.equals("proj")) {
				if(options.secondOrder)
				    d = ((DependencyDecoder2OJoint)decoder).decodeProjective(instance,fvs,probs,
											fvs_trips,probs_trips,
											fvs_sibs,probs_sibs,
											nt_fvs,nt_probs,K);
				else
				    d = decoder.decodeProjective(instance,fvs,probs,nt_fvs,nt_probs,K);
		    }
		    if(options.decodeType.equals("non-proj")) {
				if(options.secondOrder)
				    d = ((DependencyDecoder2OJoint)decoder).decodeNonProjective(instance,fvs,probs,
										       fvs_trips,probs_trips,
										       fvs_sibs,probs_sibs,
										       nt_fvs,nt_probs,K);
				else
				    d = decoder.decodeNonProjective(instance,fvs,probs,nt_fvs,nt_probs,K);
		    }
	
		    String[] res = ((String)d[0][1]).split(" ");
	
		    String[] pos = instance.postags;
	
		    String[] formsNoRoot = new String[forms.length-1];
		    String[] posNoRoot = new String[formsNoRoot.length];
		    String[] labels = new String[formsNoRoot.length];
		    int[] heads = new int[formsNoRoot.length];
	
		    Arrays.toString(forms);
		    Arrays.toString(res);
		    for(int j = 0; j < formsNoRoot.length; j++) {
				formsNoRoot[j] = forms[j+1];
				posNoRoot[j] = pos[j+1];
		
				String[] trip = res[j].split("[\\|:]");
				labels[j] = pipe.types[Integer.parseInt(trip[2])];
				heads[j] = Integer.parseInt(trip[0]);
		    }
	
		    pipe.outputInstance(new DependencyInstanceJoint(formsNoRoot, posNoRoot, labels, heads));
	
		    //String line1 = ""; String line2 = ""; String line3 = ""; String line4 = "";
		    //for(int j = 1; j < pos.length; j++) {
		    //	String[] trip = res[j-1].split("[\\|:]");
		    //	line1+= sent[j] + "\t"; line2 += pos[j] + "\t";
		    //	line4 += trip[0] + "\t"; line3 += pipe.types[Integer.parseInt(trip[2])] + "\t";
		    //}
		    //pred.write(line1.trim() + "\n" + line2.trim() + "\n"
		    //	       + (pipe.labeled ? line3.trim() + "\n" : "")
		    //	       + line4.trim() + "\n\n");
	
		    instance = pipe.nextInstance();
		}
		pipe.close();
	
		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end-start));

    }
    
    
    
    //////////////////////////////////////////////////////
    // Get Best Parses ///////////////////////////////////
    //////////////////////////////////////////////////////
    public void outputParsesJointLater(
    		String testforestFile, 
    		String outFile) throws IOException, ClassNotFoundException {

    	pipe.initOutputFile(outFile);
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(testforestFile));
    	    	
		int[] sentenceLengths = (int[]) in.readObject();
		int sentenceNumber = in.readInt();
			
		for(int i = 0; i < sentenceNumber; i++) {
		    //if((i+1) % 500 == 0) {
			System.out.print((i+1)+",");
			//System.out.println("  "+(i+1)+" instances"); 
		    //}			    		
		    		    
		    Object[] obj = pipe.readInstanceJoint(in, params);
		    HashMap<WordPos, Integer> allWordPos = (HashMap<WordPos, Integer>) obj[0];
		    WordPos[][] leftWordPoses = (WordPos[][]) obj[1];
		    WordPos[][] rightWordPoses = (WordPos[][]) obj[2];
		    WordPos[] leftWordPos = (WordPos[]) obj[3];
		    WordPos[] rightWordPos = (WordPos[]) obj[4];
		    int[][] leftIndexes = (int[][]) obj[5];
		    int[][] rightIndexes = (int[][]) obj[6];		    
		    DependencyInstanceJoint instance = (DependencyInstanceJoint) obj[7];
		    
		
		    DependencyInstanceJoint inst = null;
		    if(options.decodeType.equals("proj")) {
				if(options.secondOrder){}
				   
				else{
					//Object[] resultObj = decoder.decodeProjectiveJoint(instance, allWordPos, leftWordPoses, rightWordPoses, leftWordPos, rightWordPos, options.trainK);
					ParseForestItemJoint[] items = decoder.decodeProjectiveJointIndex(params, instance, allWordPos, leftWordPoses, rightWordPoses, 
							leftWordPos, rightWordPos, leftIndexes, rightIndexes, options.testK);
				    
					inst = items[0].getBestParse();
				}
		    }
		    
		    pipe.outputInstance(inst);
		}

		//System.out.println("");
		//System.out.println("  "+numInstances+" instances");
		in.close();
		pipe.close();
		System.out.print(sentenceNumber);	
		
    }
    	
    	
    

    
    
    
    
    

    /////////////////////////////////////////////////////
    // RUNNING THE PARSER
    ////////////////////////////////////////////////////
    public static void main (String[] args) throws FileNotFoundException, Exception
    {
    	for(String arg : args)
			System.out.print(arg + " ");
		System.out.println();
    	
    	long programStart = System.currentTimeMillis();
		ParserOptionsJoint options = new ParserOptionsJoint(args);
		DependencyPipeJoint pipe = options.secondOrder ?
				new DependencyPipe2OJoint (options) : new DependencyPipeJoint (options);
				
		if (options.joint) {
			if (options.train) {	

				/*
				//产生train的word lattice，然后存储，以便以后不用重复生成，节省时间				
				//导入正向char model
				Options forwardCharOptions = new Options(args);
				forwardCharOptions.charModelFile = options.forwardCharModel;
				CharPipe forwardCharPipe = new CharPipe(forwardCharOptions);
				Parser forwardCharParser = new Parser(forwardCharPipe, forwardCharOptions);
				forwardCharParser.loadCharModel(options.forwardCharModel);
				//导入反向char model
				Options backwardCharOptions = new Options(args);
				backwardCharOptions.charModelFile = options.backwardCharModel;
				CharPipe backwardCharPipe = new CharPipe(backwardCharOptions);	
				Parser backwardCharParser = new Parser(backwardCharPipe, backwardCharOptions);
				backwardCharParser.loadCharModel(options.backwardCharModel);
								
				long start = System.currentTimeMillis();	
				int[] instanceLengths = pipe.createInstancesJoint(options.trainfile,options.trainforest, forwardCharParser, backwardCharParser);

				long end = System.currentTimeMillis();
				System.out.println(end-start);
				pipe.write(options.trainpipe);
				end = System.currentTimeMillis();
				System.out.println(end-start);
			    int numFeats = pipe.dataAlphabet.size();
			    int numTypes = pipe.typeAlphabet.size();
			    System.out.print("Num Feats: " + numFeats);
			    System.out.println(".\tNum Edge Labels: " + numTypes);			    
			    
				
			    //可以直接采用上面存储的word lattice	
				pipe.read(options.trainpipe);
				DependencyParserJoint dp = new DependencyParserJoint(pipe, options);
				dp.trainJoint(options.trainfile, options.trainlattice, null, null);
				*/
			}
			
			if( options.test) {		
				
				/*
			    //产生test的word lattice，然后存储，以便以后不用重复生成，节省时间
				Options forwardCharOptions = new Options(args);
				forwardCharOptions.charModelFile = options.forwardCharModel;
				CharPipe forwardCharPipe = new CharPipe(forwardCharOptions);
				Parser forwardCharParser = new Parser(forwardCharPipe, forwardCharOptions);
				forwardCharParser.loadCharModel(options.forwardCharModel);
				//导入反向char model
				Options backwardCharOptions = new Options(args);
				backwardCharOptions.charModelFile = options.backwardCharModel;
				CharPipe backwardCharPipe = new CharPipe(backwardCharOptions);	
				Parser backwardCharParser = new Parser(backwardCharPipe, backwardCharOptions);
				backwardCharParser.loadCharModel(options.backwardCharModel);
								
				pipe.createTestInstancesJoint(options.testfile,options.testlattice, forwardCharParser, backwardCharParser, options.latticeK);
				*/
				
				for(int i=1; i<options.numIters; i++) {
				    DependencyParserJoint dp = new DependencyParserJoint(pipe, options);	
				    dp.loadModel(options.modelName + "-" + i);
				    dp.outputParsesJointLater(options.testlattice, options.outfile + "-" + i);
				}
				
			}
			if (options.eval) {
			    System.out.println("\nEVALUATION PERFORMANCE:");
			    for(int i=1; i<options.numIters; i++) {
				    DependencyEvaluatorJoint.evaluateJoint(options.goldfile,
								 options.outfile + "-" + i,
								 options.format);
			   }
			}
		}
		else {
			if (options.train) {	
				int[] instanceLengths = pipe.createInstances(options.trainfile,options.trainforest);	
				
			    DependencyParserJoint dp = new DependencyParserJoint(pipe, options);
	
			    int numFeats = pipe.dataAlphabet.size();
			    int numTypes = pipe.typeAlphabet.size();
			    System.out.print("Num Feats: " + numFeats);
			    System.out.println(".\tNum Edge Labels: " + numTypes);
	
			    dp.train(instanceLengths,options.trainfile,options.trainforest);
			    System.out.println(pipe.dataAlphabet.size());
			}
	
			if (options.test) {
				for(int i=1; i<options.numIters; i++) {
		
				    DependencyParserJoint dp = new DependencyParserJoint(pipe, options);
		
				    System.out.print("\tLoading model...");
				    dp.loadModel(options.modelName+"-"+i);
				    System.out.println("done.");
		
				    pipe.closeAlphabets();
		
				    dp.outputParses(options.testfile, options.outfile+"-"+i);
				}
			}
	
			System.out.println();
	
			if (options.eval) {
			    System.out.println("\nEVALUATION PERFORMANCE:");
			    for(int i=1; i<options.numIters; i++) {
			    	DependencyEvaluatorJoint eval = new DependencyEvaluatorJoint();
				    eval.eval(options.goldfile,
								 options.outfile + "-" +i,
								 options.format,
								 false);
			    }
			}
		}
		
		long programEnd = System.currentTimeMillis();
		System.out.println("The program runs in " + Long.toString(programEnd - programStart) +"s.");
    }

}
