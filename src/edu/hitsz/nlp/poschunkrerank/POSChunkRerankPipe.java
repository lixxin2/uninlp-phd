package edu.hitsz.nlp.poschunkrerank;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.hitsz.ml.onlinelearning.ap.FeatureMap;
import edu.hitsz.nlp.mst.DependencyInstance;
import edu.hitsz.nlp.pinyin2character.CoOccurrence;
import edu.hitsz.nlp.segpos.FeatureVector;


public class POSChunkRerankPipe {
	
	public FeatureMap dataMap;
	
	NgramLanguageModel<String> posLm;
	NgramLanguageModel<String> chunkLm;		
	CoOccurrence wordPOSCo;
	CoOccurrence POSWordCo;
	CoOccurrence wordChunkCo;
	CoOccurrence chunkWordCo;
	CoOccurrence POSChunkCo;
	CoOccurrence chunkPOSCo;
	
	public POSChunkRerankPipe() {
		dataMap = new FeatureMap();
	}
	
    /** 载入POSLM */
    public void loadPOSLmBin(String binLmFile) {    	
    	System.out.print("load BerkeleyLM model: " + binLmFile + "... ");
    	posLm = (ArrayEncodedNgramLanguageModel) LmReaders.readLmBinary(binLmFile);
    	posLm.setOovWordLogProb(-10.0f);    	
    	System.out.println("done");
    }
    
    public void loadChunkLmBin(String binLmFile) {    	
    	System.out.print("load BerkeleyLM model: " + binLmFile + "... ");
    	chunkLm = (ArrayEncodedNgramLanguageModel) LmReaders.readLmBinary(binLmFile);
    	chunkLm.setOovWordLogProb(-10.0f);    	
    	System.out.println("done");
    }
    
    public void loadWordPOSCo(String coFile) {    	
    	System.out.print("load WordPOS CoOccurrence: " + coFile + "... ");
    	wordPOSCo = new CoOccurrence(0, 0);		
    	wordPOSCo.read(coFile);    	
    }
    
    public void loadPOSWordCo(String coFile) {    	
    	System.out.print("load POSWord CoOccurrence: " + coFile + "... ");
    	POSWordCo = new CoOccurrence(0, 0);		
    	POSWordCo.read(coFile);    	
    }
    
    public void loadWordChunkCo(String coFile) {    	
    	System.out.print("load WordChunk CoOccurrence: " + coFile + "... ");
    	wordChunkCo = new CoOccurrence(0, 0);		
    	wordChunkCo.read(coFile);    	
    }
    
    public void loadChunkWordCo(String coFile) {    	
    	System.out.print("load ChunkWord CoOccurrence: " + coFile + "... ");
    	chunkWordCo = new CoOccurrence(0, 0);		
    	chunkWordCo.read(coFile);    	
    }
    
    public void loadPOSChunkCo(String coFile) {    	
    	System.out.print("load POSChunk CoOccurrence: " + coFile + "... ");
    	POSChunkCo = new CoOccurrence(0, 0);		
    	POSChunkCo.read(coFile);    	
    }
    
    public void loadChunkPOSCo(String coFile) {    	
    	System.out.print("load ChunkPOS CoOccurrence: " + coFile + "... ");
    	chunkPOSCo = new CoOccurrence(0, 0);		
    	chunkPOSCo.read(coFile);    	
    }
    
    
    public double getPOSLmProb(String[] poses) {
    	return getLmProb(posLm, poses);
    }
    
    public double getChunkLmProb(String[] chunks){
    	return getLmProb(chunkLm, chunks);
    }
    
    public double getLmProb(NgramLanguageModel<String> lm, String[] tags) {
    	int lmOrder = lm.getLmOrder();
    	int length = tags.length;
    	
    	double weight = 0.0;
    	for(int i=0; i<length; i++) {
			List<String> ngram = getNgram(tags, i, lmOrder);							
			weight += lm.getLogProb(ngram);
			//
			if(i==length-1) {
				ngram = getNgramEnd(tags, lmOrder);
				weight += lm.getLogProb(ngram);
			}					
    	}
    	return weight;   
    }
    
    /** 得到ngram的串 */
    public List<String> getNgram(String[] tags, int index, int lmOrder) {
		ArrayList<String> ngram = new ArrayList<String>();
		int i=0;
		while (i<lmOrder && index-i >= 0) {
			ngram.add(0, tags[index-i]);
			i++;
		}		
		if(i < lmOrder) 
			ngram.add(0, "<s>");
		return ngram;			
	}
    
    /** 最后一个词，加</s> */
	public List<String> getNgramEnd(String[] tags, int lmOrder) {
		int length = tags.length;
		List<String> ngram = getNgram(tags, length-1, lmOrder -1);
		ngram.add("</s>");
		return ngram;		
	}
	
	
	public double getWordPOSCoProb(String[] words, String[] poses){
		return getCoProb(wordPOSCo, words, poses);
	}	

	public double getPOSWordCoProb(String[] poses, String[] words){
		return getCoProb(POSWordCo, poses, words);
	}
	
	public double getWordChunkCoProb(String[] words, String[] chunks){
		return getCoProb(wordChunkCo, words, chunks);
	}
	
	public double getChunkWordCoProb(String[] chunks, String[] words){
		return getCoProb(chunkWordCo, chunks, words);
	}
	
	public double getPOSChunkCoProb(String[] poses, String[] chunks){
		return getCoProb(POSChunkCo, poses, chunks);
	}
	
	public double getChunkPOSCoProb(String[] chunks, String[] poses){
		return getCoProb(chunkPOSCo, chunks, poses);
	}
	
	
	public double getCoProb(CoOccurrence co, String[] lefts, String[] rights) {
		int length = lefts.length;
		double weight = 0.0;
		for(int i=0; i<length; i++)
			weight += co.get(lefts[i], rights[i]);
		return weight;		
	}
	

	/** 生成reranker的特征 
	 * @param featureSet 1：词特征；2：词和词性特征；5：dep特征
	 * */
	 public FeatureVector createFeatureVectorReranker(ArrayList<String[]> allWords, 
			 boolean added,
			 int featureSet) {

			FeatureVector fv = new FeatureVector();	
			
			String[] words = allWords.get(0);
			String[] poses = allWords.get(1);
			String[] chunks = allWords.get(2);

			int length = words.length;								
									
			//word, pos pair
			for(int i=0; i<length; i++) {
				
				String curWord = words[i];
				String curPos = poses[i];
				String curChunk = chunks[i];
				
				String preWord = "pw";
				String prePos = "pp";
				String preChunk = "pc";
				String pre2Word = "p2w";
				String pre2Pos = "p2p";
				String pre2Chunk = "p2c";
				if(i>0) {
					preWord = words[i-1];
					prePos = poses[i-1];
					preChunk = chunks[i-1];
					if(i>1) {
						pre2Word = words[i-2];
						pre2Pos = poses[i-2];
						pre2Chunk = chunks[i-2];
					}
				}
				if(featureSet == 1 || featureSet == 2) {
					add("1:"+pre2Word+"-"+preWord+"-"+curWord, fv, added);
					add("2:"+preWord+"-"+curWord, fv, added);
					add("3:"+curWord, fv, added);
					
					add("4:"+pre2Pos+"-"+prePos+"-"+curPos, fv, added);
					add("5:"+prePos+"-"+curPos, fv, added);
					add("6:"+curPos, fv, added);
					add("7:"+curPos+"-"+curWord, fv, added);
				}
				
				if(featureSet == 2) {
					add("8:"+prePos+"-"+curPos+"-"+curWord, fv, added);
					add("9:"+prePos+"-"+preWord+"-"+curPos+"-"+curWord, fv, added);
					add("10:"+pre2Pos+"-"+prePos+"-"+preWord+"-"+curPos+"-"+curWord, fv, added);
					add("11:"+pre2Pos+"-"+pre2Word+"-"+prePos+"-"+preWord+"-"+curPos+"-"+curWord, fv, added);
				}
				
				if(featureSet == 1 || featureSet == 2) {
					add("12:"+pre2Chunk+"-"+preChunk+"-"+curChunk, fv, added);
					add("13:"+preChunk+"-"+curChunk, fv, added);
					add("14:"+curChunk, fv, added);
					add("15:"+curChunk+"-"+curWord, fv, added);
				}

				if(featureSet == 2) {
					add("16:"+preChunk+"-"+curChunk+"-"+curWord, fv, added);
					add("17:"+preChunk+"-"+preWord+"-"+curChunk+"-"+curWord, fv, added);
					add("18:"+pre2Chunk+"-"+preChunk+"-"+preWord+"-"+curChunk+"-"+curWord, fv, added);
					add("19:"+pre2Chunk+"-"+pre2Word+"-"+preChunk+"-"+preWord+"-"+curChunk+"-"+curWord, fv, added);
				}
				
				if(featureSet == 1 || featureSet == 2) {
					add("20:"+pre2Pos+"-"+prePos+"-"+curPos+"-"+pre2Chunk+"-"+preChunk+"-"+curChunk, fv, added);
					add("21:"+prePos+"-"+curPos+"-"+preChunk+"-"+curChunk, fv, added);
					add("22:"+curPos+"-"+curChunk, fv, added);
					add("23:"+curPos+"-"+curChunk+"-"+curWord, fv, added);
				}
				
				if(featureSet == 2) {
					add("24:"+prePos+"-"+preChunk+"-"+curPos+"-"+curChunk+"-"+curWord, fv, added);
					add("25:"+prePos+"-"+preChunk+"-"+preWord+"-"+curPos+"-"+curChunk+"-"+curWord, fv, added);
					add("26:"+pre2Pos+"-"+pre2Chunk+"-"+prePos+"-"+preChunk+"-"+preWord+"-"+curPos+"-"+curChunk+"-"+curWord, fv, added);
					add("27:"+pre2Pos+"-"+pre2Chunk+"-"+pre2Word+"-"+prePos+"-"+preChunk+"-"+preWord+"-"+curPos+"-"+curChunk+"-"+curWord, fv, added);
				}
			}							
			return fv;				
	 }
	 
	/**
	 * 添加一个特征
	 * @since Oct 23, 2012
	 * @param feat
	 * @param fv
	 * @param added
	 */
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
