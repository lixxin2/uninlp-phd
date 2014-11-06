package edu.hitsz.nlp.nermusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 评测类 
 * @author Xinxin Li
 * @since May 15, 2012
 */
public class Eval {
	int gold = 0;
	int predict = 0;
	int correct = 0;
	ArrayList<Integer> golds;
	ArrayList<Integer> predicts;
	ArrayList<Integer> corrects;
	HashMap<String, Integer> types;
	
	public Eval( ) {		
		golds = new ArrayList<Integer>();
		predicts = new ArrayList<Integer>();
		corrects = new ArrayList<Integer>();
		
		types = new HashMap<String, Integer>();
		
	}
	
	/** 读取测试文件, 并进行评测 */
	public void eval(String fileName) {
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
			ArrayList<String> chars = new ArrayList<String>();
			ArrayList<String> goldTags = new ArrayList<String>();
			ArrayList<String> predictTags = new ArrayList<String>();
			while((line = reader.readLine()) != null) {
				if(line.length() > 0) {
					String[] parts = line.split("[ \t]");
					chars.add(parts[0]);
					goldTags.add(parts[parts.length-2]);
					predictTags.add(parts[parts.length-1]);					
				}
				else {
					ArrayList<NER> goldNers = getNER(chars, goldTags);
					ArrayList<NER> predictNers = getNER(chars, predictTags);

					for(NER ner : goldNers) {
						String label = ner.tag;
						if(!types.containsKey(label)) {
							types.put(label, types.size());
							golds.add(0);
							predicts.add(0);
							corrects.add(0);
						}
					}
					for(NER ner : predictNers) {
						String label = ner.tag;
						if(!types.containsKey(label)) {
							types.put(label, types.size());
							golds.add(0);
							predicts.add(0);
							corrects.add(0);
						}
					}
					eval(goldNers, predictNers);
					goldTags.clear();
					predictTags.clear();
				}				
			}
			reader.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
	
	
	/**
	 * 比较两个标注序列,统计其中的正确实体数目
	 * @since May 15, 2012
	 * @param goldTags
	 * @param predictTags
	 */
	public void eval(ArrayList<NER> goldNers, ArrayList<NER> predictNers) {
		
		int senGoldNumber = goldNers.size();
		gold += senGoldNumber;
		int senPredictNumber = predictNers.size();
		predict += senPredictNumber;
		
		int senCorrect = 0;
		
		//对于每个正确的名实体
		for(int i=0; i<senGoldNumber; i++) {
			NER goldNer = goldNers.get(i);
			boolean correctSig = false;
			for(int j=0; j<senPredictNumber; j++) {
				if(goldNer.equals(predictNers.get(j))) {
					correctSig = true;
					break;
				}		
				if(goldNer.end < predictNers.get(j).end) 
					break;
			}

			int index = types.get(goldNer.tag);
			if(correctSig) {
				corrects.set(index, corrects.get(index) + 1);	
				senCorrect += 1;
			}
			golds.set(index, golds.get(index) + 1);		
		}
		
		correct += senCorrect;
		
		int senCorrect2 = 0;
		for(int j=0; j<senPredictNumber; j++) {
			NER predictNer = predictNers.get(j);
			
			boolean correctSig = false;
			for(int i=0; i<senGoldNumber; i++) {
				if(predictNer.equals(goldNers.get(i))) {
					correctSig = true;
					break;
				}
				if(predictNer.end < goldNers.get(i).end)
					break;
			}
			
			if(correctSig) {
				senCorrect2 += 1;
			}
			
			int index = types.get(predictNer.tag);
			predicts.set(index, predicts.get(index)+1);
		}
		
		if(senCorrect != senCorrect2) {
			System.out.println("different correct numbers in gold tags and predict tags. please check");
			System.exit(1);
		}	
		
	}
	
	
	/**
	 * 从一个序列中获取他的名实体集合
	 * @since May 15, 2012
	 * @param tags
	 * @return
	 */
	public ArrayList<NER> getNER(ArrayList<String> chars, ArrayList<String> tags) {
		ArrayList<NER> ners = new ArrayList<NER>();
		
		int length = tags.size();
		for(int i=0; i<length; i++) {
			String curTag = tags.get(i);
			if(curTag.startsWith("S")) {
				String[] parts = curTag.split("-");
				String label = parts[1];
				String word = chars.get(i);
				ners.add(new NER(i,i,parts[1], word));
				continue;				
			}
			else if(curTag.startsWith("B")) {
				String[] parts = curTag.split("-");
				String word = chars.get(i);
				int j=i+1;
				while(j<length && !tags.get(j).startsWith("E")) {
					if(chars.get(j).matches("[0-9a-zA-Z].*"))
						word += " " + chars.get(j);
					else
						word += chars.get(j);
					j++;					
				}
				if(j<length) {
					word += chars.get(j);
					String label = parts[1];
					ners.add(new NER(i,j,parts[1], word));
				}
				i=j;
			}			
		}	
		return ners;
	}
	
	
	
	public void outputEval() {
		System.out.println("\tEval: gold, predict, correct, precision, recall, f-1 measure");
		System.out.println("\tALL:" + gold +", " + predict + ", " + correct 
				 + ", " + correct/(double) predict + ", " + correct/(double) gold
				 + ", " + 2 * correct/(double)(predict + gold) );
		Iterator<Map.Entry<String, Integer>> iter = types.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			String tag = entry.getKey();
			int index = entry.getValue();			
			System.out.println("\t" + tag + ": " + golds.get(index) + "," + predicts.get(index) 
					+ "," + corrects.get(index) + "," + corrects.get(index)/(double)predicts.get(index)
					+ "," + corrects.get(index)/(double)golds.get(index)
					+ "," + 2 * corrects.get(index)/(double)(predicts.get(index) + golds.get(index)));
		}
	}
	
	/** 错误分析 */
	public void errorAnalysis(String resultFileName) {
		BufferedReader reader = null;
		String line = null;
		
		ArrayList<HashMap<String, Integer>> missNers = new ArrayList<HashMap<String, Integer>>();
		ArrayList<HashMap<String, Integer>> addNers = new ArrayList<HashMap<String, Integer>>();
		for(int i=0; i<4; i++) {
			missNers.add(new HashMap<String, Integer>());
			addNers.add(new HashMap<String, Integer>());
		}
		
		try {
			reader = new BufferedReader(new FileReader(new File(resultFileName)));
			ArrayList<String> chars = new ArrayList<String>();
			ArrayList<String> goldTags = new ArrayList<String>();
			ArrayList<String> predictTags = new ArrayList<String>();
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					String[] parts = line.split("\t");
					if(parts.length >2) { 
						chars.add(parts[0]);
						goldTags.add(parts[parts.length-2]);
						predictTags.add(parts[parts.length-1]);		
					}
				}
				else {
					ArrayList<NER> goldNers = getNER(chars, goldTags);
					ArrayList<NER> predictNers = getNER(chars, predictTags);
					error(missNers, addNers, goldNers, predictNers);
					chars.clear();
					goldTags.clear();
					predictTags.clear();
				}				
			}
			System.out.println();
			reader.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}			
		
		
	}
	
	
	
	
	/**
	 * 比较两个标注序列,统计其中的正确实体数目
	 * @since May 15, 2012
	 * @param goldTags
	 * @param predictTags
	 */
	public void error(ArrayList<HashMap<String, Integer>> missNers, ArrayList<HashMap<String, Integer>> addNers,
			ArrayList<NER> goldNers, ArrayList<NER> predictNers) {
		
		int senGoldNumber = goldNers.size();
		int senPredictNumber = predictNers.size();
				
		//对于每个正确的名实体
		for(int i=0; i<senGoldNumber; i++) {
			NER goldNer = goldNers.get(i);
			boolean correctSig = false;
			for(int j=0; j<senPredictNumber; j++) {
				if(goldNer.equals(predictNers.get(j))) {
					correctSig = true;
					break;
				}		
				if(goldNer.end < predictNers.get(j).end) 
					break;
			}

			if(!correctSig) {
				AddErrorNer(missNers, goldNer);
			}	
		}
		
		
		for(int j=0; j<senPredictNumber; j++) {
			NER predictNer = predictNers.get(j);
			
			boolean correctSig = false;
			for(int i=0; i<senGoldNumber; i++) {
				if(predictNer.equals(goldNers.get(i))) {
					correctSig = true;
					break;
				}
				if(predictNer.end < goldNers.get(i).end)
					break;
			}			
			if(!correctSig) {
				AddErrorNer(addNers, predictNer);
			}
			
		}
		
		
	}
	
	/**
	 * 将NER放入相应的map中，用来统计其出现次数
	 * @since May 23, 2012
	 * @param ners
	 * @param ner
	 */
	public void AddErrorNer(ArrayList<HashMap<String, Integer>> ners, NER ner) {
		String word = ner.word;
		String label = ner.tag;
		int num = 0;
		if(label.equals("ARTIST")) {
			num = 0;
		}
		else if(label.equals("BAND"))
			num = 1;
		else if(label.equals("SONG"))
			num = 2;
		else if(label.equals("ALBUM"))
			num = 3;
		
		HashMap<String, Integer> sners = ners.get(num);
		if(!sners.containsKey(word)) {
			sners.put(word,1);
		}
		else {
			sners.put(word, sners.get(word)+1);
		
		}	
	}
	
	

	public static void main(String[] args) {
		String fileName = "/home/tm/disk/disk1/nermusic/test-maxent-result";
		fileName = "/home/tm/disk/disk1/nermusic/test-liblinear-final-result-end-post";
		Eval eval = new Eval();
		eval.eval(fileName);
		eval.outputEval();
		//eval.errorAnalysis(fileName);		
	}
		
	
	
	
	
	
	class NER {
		int start;
		int end;
		String tag;
		String word;
		
		NER(int start, int end, String tag) {
			this.start = start;
			this.end = end;
			this.tag = tag;
		}
		
		NER(int start, int end, String tag, String word) {
			this.start = start;
			this.end = end;
			this.tag = tag;
			this.word = word;
		}
		
		public boolean equals(NER ner) {			
			return start==ner.start && end ==ner.end && tag.equals(ner.tag);
		}
		
		public boolean equals2(NER ner) {			
			return start==ner.start && end ==ner.end && (tag.equals(ner.tag) || 
					(tag.matches("ARTIST") && ner.tag.matches("ALIAS")) ||
					(tag.matches("ALIAS") && ner.tag.matches("ARTIST")));
		}
		
		
	}
	
	

	

}
