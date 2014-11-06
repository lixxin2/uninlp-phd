package edu.hitsz.nlp.asr.rerank;

import java.io.File;

import edu.hitsz.nlp.pinyin2character.PyCharOptions;

public class AsrRerankOptions extends PyCharOptions{

	public boolean oneBestJulius = false;
	public boolean oneBestYinJulius = false;
	public boolean oneBestMert = false;
	public boolean oracleMert = false;
	public int oracleK = 500;
	public boolean kBest = false;
	public int bestK = 500;
	public boolean mertComb = false;
	public boolean eval = false;
		
	public String syllableFile = modelDir + File.separator + "dict/syllable_nosil_nosp.dict";	
	public String bestCandSeqFile = null;	
	public String goldFile = "/home/tm/disk/disk1/asr-rerank/120Gold";	
	//public double[] mertWeights = {0.5, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	//public double[] mertWeights = {9.901043055510751, 5.4215269883304895, -1.9501683518254374, 107.04913908845428, -1.1573620516707805, 7.334407815532382, 0.5922107997008705, -0.06910431412484139, -5.873031285048123, 20.84184783842793, -0.37039313888625336};
	public double[] mertWeights = {557.7131934392476, 195.42921923334933, 47.86338960130212, 35.499447735848506, 0.8766062706385094, -26.469717140786003, 86.34955790003748, 51.88321939091966, 106.21815470382447};//pd
	
	
	
	public AsrRerankOptions(String[] args) {
		
		super(args);
		
		for(int i = 0; i < args.length; i++) {
			String[] pair = args[i].split(":");
			if (pair[0].equals("oneBestJulius")) {
				oneBestJulius = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("oneBestYinJulius")) {
				oneBestYinJulius = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("oneBestMert")) {
				oneBestMert = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("oracleMert")) {
				oracleMert = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("oracleK")) {
				oracleK = Integer.parseInt(pair[1]);
			}
			if (pair[0].equals("kBest")) {
				kBest = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("mertComb")) {
				mertComb = pair[1].equals("true") ? true : false;
			}
			if (pair[0].equals("eval")) {
				eval = pair[1].equals("true") ? true : false;
			}
				
			if (pair[0].equals("bestK")) {
				bestK = Integer.parseInt(pair[1]);
		    }
			
			if (pair[0].equals("syllableFile")) {
				syllableFile = pair[1];
		    }
			if (pair[0].equals("bestCandSeqFile")) {
				bestCandSeqFile = pair[1];
		    }			
			if (pair[0].equals("goldFile")) {
				goldFile = pair[1];
		    }

	    }
	}

}
