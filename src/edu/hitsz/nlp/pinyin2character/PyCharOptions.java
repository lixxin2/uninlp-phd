package edu.hitsz.nlp.pinyin2character;

import java.io.File;

public class PyCharOptions {

	public boolean ngram = false;
	
	public boolean charTrain = false;
	public boolean charDev = false;
	public boolean charTest = false;
		
	public boolean mertTrain = false;
	public boolean mertDevOffline = false;
	public boolean mertDevOnline = false;
	
	public boolean rerankTrain = false;
	public boolean rerankDev = false;
	public boolean rerankTest = false;
	
	public int numIters = 8;
			
	public final int charK = 16;
	public final int gramK = 500;
	public int mertK = 500;
	
	public int initWeight = 2;
	public int featureSet = 5;
	
	//public double[] mertWeights = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};//ngram	
	double[] mertWeights = {1008.7968240620257, 445.01126323028666, 16.139977338315713, 210.1652161254812, 17.820653264675308, -111.20815089643853, 1.3938775785525181, 129.95186721331638, 180.1429543813514}; //lcmc
	//public double[] mertWeights = {557.7131934392476, 195.42921923334933, 47.86338960130212, 35.499447735848506, 0.8766062706385094, -26.469717140786003, 86.34955790003748, 51.88321939091966, 106.21815470382447};//pd
	
	
	/** 训练基于字的模型的文件 */
	public String trainFile = "/home/tm/disk/disk1/pinyin2character/PD/data/train.pinyinsword.align";
	/** 子模型相关文件 */
	public String modelDir = "/home/tm/disk/disk1/pinyin2character/lm";
	public String wordPinyinFile = modelDir + File.separator + "dict/wordlist5.pinyin.10";		
	public String binLmFile = modelDir + File.separator + "lm.seg.5.bin";
	public String charLmFile = modelDir + File.separator + "lm.char.4.bin";	
	public String baseModelFile = modelDir + File.separator + "pd.train.char.model-5";	
	public String pinyinWordCoFile = modelDir + File.separator + "stat.pinyinWord.0.0";	
	public String posModelFile = modelDir + File.separator + "posModel-8";
	public String POSWordCoFile = modelDir + File.separator + "stat.POSword.0.0";
	public String wordPOSCoFile = modelDir + File.separator + "stat.wordPOS.0.0";	
	public String dependencyModelFile = modelDir + File.separator + "ctb5-train-dep-unlabelled-UTF-8.mst.model.ord2";
		
	public String testFile = "/home/tm/disk/disk1/pinyin2character/PD/data/test.pinyins";//
	//mert(linear reranking) 模型
	public String mertCandFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/cand_database_pd_dev.txt"; 
	public String mertDependencyFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/cand_dependency_pd_dev";
	
	public String outputFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/dev.pinyins.result.mert.ngram";//
		
	//AP reranking
	//正确的文件
	public String rerankerTrainFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/ref.dev.nodict";
	public String mertCandTrainFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/cand_database_pd_dev.txt";	
	public String mertDependencyTrainFile = "/home/tm/disk/disk1/pinyin2character/PD/mert/cand_dependency_pd_dev";
	public String rerankerModelFile = "/home/tm/disk/disk1/pinyin2character/PD/rerank/rerank.train.model-2-2-7";
	
	public String mertCandTestFile = "/home/tm/disk/disk1/pinyin2character/mert/cand_database_lcmctest.txt"; 
	public String mertDependencyTestFile = "/home/tm/disk/disk1/pinyin2character/mert/cand_dependency_lcmctest";
	public String rerankTestOutputFile = "/home/tm/disk/disk1/pinyin2character/mert/lcmctest_result-2-2-7"; 
	
	
	/**
	 * 从命令行读取参数配置
	 * @since Dec 9, 2011
	 * @param args
	 */
	public PyCharOptions (String[] args) {
		for(int i = 0; i < args.length; i++) {
			String[] pair = args[i].split(":");
			if (pair[0].equals("ngram")) {
				ngram = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("charTrain")) {
				charTrain = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("charDev")) {
				charDev = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("charTest")) {
				charTest = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("mertTrain")) {
				mertTrain = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("mertDevOffline")) {
				mertDevOffline = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("mertDevOnline")) {
				mertDevOnline = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("rerankTrain")) {
				rerankTrain = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("rerankDev")) {
				rerankDev = pair[1].equals("true")?true:false;
		    }
			if (pair[0].equals("rerankTest")) {
				rerankTest = pair[1].equals("true")?true:false;
		    }
						
			
			if (pair[0].equals("trainFile")) {
				trainFile = pair[1];
		    }		
			
			
			if (pair[0].equals("modelDir")) {
				modelDir = pair[1];
				resetSubModels();
		    }
			if (pair[0].equals("wordPinyinFile")) {
				wordPinyinFile = pair[1];
		    }
			if (pair[0].equals("binLmFile")) {
				binLmFile = pair[1];
		    }
			if (pair[0].equals("charLmFile")) {
				charLmFile = pair[1];
		    }
			if (pair[0].equals("baseModelFile")) {
				baseModelFile = pair[1];
		    }
			if (pair[0].equals("pinyinWordCoFile")) {
				pinyinWordCoFile = pair[1];
		    }
			if (pair[0].equals("posModelFile")) {
				posModelFile = pair[1];
		    }
			if (pair[0].equals("POSWordCoFile")) {
				POSWordCoFile = pair[1];
		    }
			if (pair[0].equals("wordPOSCoFile")) {
				wordPOSCoFile = pair[1];
		    }
			if (pair[0].equals("dependencyModelFile")) {
				dependencyModelFile = pair[1];
		    }
			
			if (pair[0].equals("testFile")) {
				testFile = pair[1];
		    }
			if (pair[0].equals("mertCandFile")) {
				mertCandFile = pair[1];
		    }
			if (pair[0].equals("mertDependencyFile")) {
				mertDependencyFile = pair[1];
		    }
			if (pair[0].equals("outputFile")) {
				outputFile = pair[1];
		    }
						
			if (pair[0].equals("rerankerTrainFile")) {
				rerankerTrainFile = pair[1];
		    }
			if (pair[0].equals("mertCandTrainFile")) {
				mertCandTrainFile = pair[1];
			}
			if (pair[0].equals("mertDependencyTrainFile")) {
				mertDependencyTrainFile = pair[1];
			}
			if (pair[0].equals("rerankerModelFile")) {
				rerankerModelFile = pair[1];
		    }
			if (pair[0].equals("mertCandTestFile")) {
				mertCandTestFile = pair[1];
			}
			if (pair[0].equals("mertDependencyTestFile")) {
				mertDependencyTestFile = pair[1];
			}
			if (pair[0].equals("rerankTestOutputFile")) {
				rerankTestOutputFile = pair[1];
		    }
		}		    
	}
	
	
	public void resetSubModels() {
		wordPinyinFile = modelDir + File.separator + "dict/wordlist5.pinyin.10";		
		binLmFile = modelDir + File.separator + "lm.seg.5.bin";
		charLmFile = modelDir + File.separator + "lm.char.4.bin";	
		baseModelFile = modelDir + File.separator + "pd.train.char.model-5";	
		pinyinWordCoFile = modelDir + File.separator + "stat.pinyinWord.0.0";	
		posModelFile = modelDir + File.separator + "posModel-8";
		POSWordCoFile = modelDir + File.separator + "stat.POSword.0.0";
		wordPOSCoFile = modelDir + File.separator + "stat.wordPOS.0.0";	
		dependencyModelFile = modelDir + File.separator + "ctb5-train-dep-unlabelled-UTF-8.mst.model.ord2";
	}
	
	
}
