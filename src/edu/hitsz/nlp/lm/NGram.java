package edu.hitsz.nlp.lm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.hitsz.java.file.local.FileEncoding;

public class NGram {
	
	int numbers[];
	Map<String, float[]> unigram;
	Map<String, float[]> bigram;
	Map<String, float[]> trigram;
	Map<String, float[]> fourgram;
	Map<String, float[]> fivegram;
	
	public NGram() {
		numbers = new int[5];
		unigram = new TreeMap<String, float[]>();
		bigram = new TreeMap<String, float[]>();
		trigram = new TreeMap<String, float[]>();
		fourgram = new TreeMap<String, float[]>();
		fivegram = new TreeMap<String, float[]>();
	}
	
	public void load(String fileName) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
        String line = reader.readLine();
        
        int count = 0;
        
        while( line!= null) {
        	//去掉空行
        	while( line != null && line.trim().length() == 0)
        		line = reader.readLine();
        	//
        	if(line.equals("\\data\\")) {
        		line = reader.readLine();
        		int i=0;
        		while (line != null && line.trim().length() != 0 && !line.startsWith("\\")) {
        			String[] parts = line.trim().split("=");
        			numbers[i++] = Integer.parseInt(parts[1]);
        			line = reader.readLine();
        		}
        		continue;
        	}
        	else if(line.equals("\\1-grams:")) {
        		line = reader.readLine();
        		while (line != null && line.trim().length() != 0 && !line.startsWith("\\")) {
        			String[] parts = line.trim().split("\t");
        			String gram = parts[1];
        			float[] value = new float[2];
        			value[0] = Float.parseFloat(parts[0]);
        			if(parts.length == 3)
        				value[1] = Float.parseFloat(parts[2]);
        			unigram.put(gram, value);
        			line = reader.readLine();
        		}
        		System.out.println("load unigram done...");
        		continue;
        	}
        	else if(line.equals("\\2-grams:")) {
        		line = reader.readLine();
        		while (line != null && line.trim().length() != 0 && !line.startsWith("\\")) {
        			String[] parts = line.trim().split("\t");
        			String gram = parts[1];
        			float[] value = new float[2];
        			value[0] = Float.parseFloat(parts[0]);
        			if(parts.length == 3)
        				value[1] = Float.parseFloat(parts[2]);
        			bigram.put(gram, value);
        			line = reader.readLine();
        		}
        		System.out.println("load bigram done...");
        		continue;
        	}
        	else if(line.equals("\\3-grams:")) {
        		line = reader.readLine();
        		while (line != null && line.trim().length() != 0 && !line.startsWith("\\")) {
        			String[] parts = line.trim().split("\t");
        			String gram = parts[1];
        			float[] value = new float[2];
        			value[0] = Float.parseFloat(parts[0]);
        			if(parts.length == 3)
        				value[1] = Float.parseFloat(parts[2]);
        			trigram.put(gram, value);
        			line = reader.readLine();
        		}
        		System.out.println("load trigram done...");
        		continue;
        	}
        	else if(line.equals("\\4-grams:")) {
        		line = reader.readLine();
        		while (line != null && line.trim().length() != 0 && !line.startsWith("\\")) {
        			String[] parts = line.trim().split("\t");
        			String gram = parts[1];
        			float[] value = new float[2];
        			value[0] = Float.parseFloat(parts[0]);
        			if(parts.length == 3)
        				value[1] = Float.parseFloat(parts[2]);
        			fourgram.put(gram, value);
        			line = reader.readLine();
        		}
        		System.out.println("load fourgram done...");
        		continue;
        	}
        	else if(line.equals("\\5-grams:")) {
        		line = reader.readLine();
        		while (line != null && line.trim().length() != 0 && !line.startsWith("\\")) {
        			String[] parts = line.trim().split("\t");
        			String gram = parts[1];
        			float[] value = new float[2];
        			value[0] = Float.parseFloat(parts[0]);
        			if(parts.length == 3)
        				value[1] = Float.parseFloat(parts[2]);
        			fivegram.put(gram, value);
        			line = reader.readLine();
        		}
        		System.out.println("load fivegram done...");
        		continue;
        	}
        	else if(line.equals("\\end\\")) {
        		break;
        	}        	
        }		
		reader.close();	
	}
	
	public void printDetails() {
		int length = numbers.length;
		for(int i=0; i<length; i++) {
			if(numbers[i] != 0)
				System.out.println("the number of " + i+1 + " gram is " + numbers[i]);
		}
	}
	
	public void checkProb() {
		checkProb(unigram);
		checkProb(bigram);
		checkProb(trigram);
	}
	
	public void checkProb(Map<String, float[]> map) {
		Iterator<Entry<String, float[]>> iter = map.entrySet().iterator();
		float probs = 0.0F;
		while(iter.hasNext()) {
			Entry<String, float[]> entry = iter.next();
			float probLog = entry.getValue()[0];
			probs += Math.exp(probLog);
		}
		System.out.println("probabilities of gram is " + probs);
		
	}
	
	
	public static void main(String[] args) throws IOException {
		
		String modelName = "/home/tm/windows/asr/dict/lm/pr-hanzi.lm";
		NGram gram = new NGram();
		gram.load(modelName);
		gram.printDetails();
		gram.checkProb();
		
		
	}

}

class Value {
	double probLog;
	double weightLog;
	
	public String toString() {
		return "probLog: " + probLog + ", weightLog: " + weightLog;
	}
}
