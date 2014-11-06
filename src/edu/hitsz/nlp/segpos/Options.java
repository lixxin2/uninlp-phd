package edu.hitsz.nlp.segpos;

public class Options {

	//"char":基于字符的
	//"word"：基于词的，multibeam
	//"joint"：字词联合的
	//"sword":基于词的，单beam
	//"pos",单独的词性标注
	public String modelType = "char";
	// jiang, nglow: 两种不同的字符特征选择方式
	public String charFeatureType = "nglow";
	
	public boolean train = false;
	/** 用来评测数据的，会对每个模型都产生结果*/
	public boolean dev = false;
	/** 用来生成最终结果，只用一个模型*/
	public boolean test = false;
	public boolean eval = false;
	public boolean oracle = false;
	public boolean oracleKbeam = false;
	
	public int numIters = 10;
	/**lattice的kbeam*/
	public int K = 16;
	/**计算oracle时的k*/
	public int oracleK = 100;
	
	public int multiThread = 1;
	
	public String inputFileType = "column"; //column, row
	public String outputFileType = "column"; //column, wpRow, wRow, wpTwoRows
	
	public boolean trainReverse = false;
	public boolean devReverse = false;
	public boolean writeReverse = false;
	
	/** 训练文件 */
	public String trainFile = "";
	public String latticeFile = "";
	
	public String charModelFile = "";
	public String wordModelFile = "";
	public String posModelFile = "";

	public String goldFile = "";
	public String testFile = "";	
	public String outputFile = "";
	public String evalFile = "";
	public String oracleFile = "";
	
	
	public double jointParam = 0.6;
	
	
	public Options() {
		
	}
	
	
	/**
	 * 从命令行读取参数配置
	 * @since Dec 9, 2011
	 * @param args
	 */
	public Options (String[] args) {
		for(int i = 0; i < args.length; i++) {
			String[] pair = args[i].split(":");
			if (pair[0].equals("modelType")) {
		    	modelType = pair[1];
		    }
			if (pair[0].equals("charFeatureType")) {
				charFeatureType = pair[1];
			}
			if (pair[0].equals("train")) {
				train = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("dev")) {
				dev = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("test")) {
				test = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("eval")) {
				eval = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("oracle")) {
				oracle = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("numIters")) {
				numIters = Integer.parseInt(pair[1]);
			}
			if (pair[0].equals("K")) {
				K = Integer.parseInt(pair[1]);
			}
			if (pair[0].equals("oracleK")) {
				oracleK = Integer.parseInt(pair[1]);
			}
			if (pair[0].equals("multiThread")) {
				multiThread = Integer.parseInt(pair[1]);
			}
			if (pair[0].equals("inputFileType")) {
				inputFileType = pair[1];
			}
			if (pair[0].equals("outputFileType")) {
				outputFileType = pair[1];
			}
			if(pair[0].equals("trainReverse")){
				trainReverse = pair[1].equals("true") ? true : false;
			}
			if(pair[0].equals("devReverse")){
				devReverse = pair[1].equals("true") ? true : false;
			}
			if(pair[0].equals("writeReverse")){
				writeReverse = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("trainFile")) {
				trainFile = pair[1];
			}
			if (pair[0].equals("goldFile")) {
				goldFile = pair[1];
			}
			if (pair[0].equals("latticeFile")) {
				latticeFile = pair[1];
			}
			if (pair[0].equals("charModelFile")) {
				charModelFile = pair[1];
			}
			if (pair[0].equals("wordModelFile")) {
				wordModelFile = pair[1];
			}			
			if (pair[0].equals("testFile")) {
				testFile = pair[1];
			}
			if (pair[0].equals("outputFile")) {
				outputFile = pair[1];
			}
			if (pair[0].equals("evalFile")) {
				evalFile = pair[1];
			}
			if (pair[0].equals("oracleFile")) {
				oracleFile = pair[1];
			}
			if (pair[0].equals("jointParam")) {
				jointParam = Double.parseDouble(pair[1]);
			}
		}		    
	}
	
	
	public static void Usage() {
		System.out.println("Character-based Model");
		
	}
		
	
	
}
