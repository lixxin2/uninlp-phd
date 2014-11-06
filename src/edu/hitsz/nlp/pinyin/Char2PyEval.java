package edu.hitsz.nlp.pinyin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.hitsz.java.file.local.FileEncoding;
import edu.hitsz.nlp.util.Array;
import edu.hitsz.nlp.asr.eval.ASREval;
import edu.hitsz.nlp.corpus.ctb.CTB;
import edu.hitsz.nlp.corpus.ptb.PTB;

public class Char2PyEval {
	
	private ArrayList<String> notChinese;

	public Char2PyEval() {
		notChinese = new ArrayList<String>();
	    notChinese.addAll(Array.toArrayList(CTB.punctuations));
	    notChinese.addAll(Array.toArrayList(CTB.arabNumbers));
	    notChinese.addAll(Array.toArrayList(CTB.letters));
	    notChinese.addAll(Array.toArrayList(PTB.punctuations));
	}
	
	
	public static void eval() throws IOException {		

		String goldFileName = "/home/tm/disk/disk1/pinyin2character/pinyin-UTF-8";
		String predictFileName = "/home/tm/disk/disk1/pinyin2character/pinyin-4j";//pinyin-4j";//pinyin-minwordnum";//pinyin-backmaxmatch";
		
		ASREval eval = new ASREval();
		
		int all = 0;
		int corr = 0;
		int allChn = 0;
		int corrChn = 0;
		
		ArrayList<String> noChinese =  new Char2PyEval().notChinese;

		String encoding = FileEncoding.getCharset(goldFileName);
        BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), encoding));
        
        String encoding2 = FileEncoding.getCharset(predictFileName);
        BufferedReader predictReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), encoding2));
        
        String goldLine = null;
        String predictLine = null;
        
        int count = 0;
        
        while((goldLine = goldReader.readLine()) != null
        	&& (predictLine = predictReader.readLine()) != null) {
        	
        	count++;
        	//System.out.println(count);
        	
        	ArrayList<String> goldYins = eval.getZiYins(goldLine);
        	ArrayList<String> predictYins = eval.getZiYins(predictLine);
        	
        	if(goldYins.size() != predictYins.size()) {
        		System.out.println(goldLine);
        		System.out.println(predictLine);
        	}
        	
        	//eval.updateEval(goldYins, predictYins);
        	
        	for(int i=0; i<goldYins.size(); i++) {
        		String goldYin = goldYins.get(i);
        		String predictYin = predictYins.get(i);
        		all++;
        		if(goldYin.endsWith(predictYin))
        			corr++;
        		if(!noChinese.contains(goldYin)) {
        			allChn++;
            		if(goldYin.endsWith(predictYin))
            			corrChn++;
        		}
        		else {
        			if(!goldYin.equals(predictYin)) {
                		System.out.println(goldLine);
                		System.out.println(predictLine);

                		System.out.println(goldYin);
                		System.out.println(predictYin);
        			}
        		}
        	}
        	
        }
        
        goldReader.close();
        predictReader.close();
        eval.output();        

    	System.out.println("corr:    " + corr);
    	System.out.println("all:     " + all);
    	System.out.println("acc:     " + corr/(double) all);
    	System.out.println("corrChn: " + corrChn);
    	System.out.println("allChn:  " + allChn);
    	System.out.println("accChn:  " + corrChn/(double) allChn);
        
	}
	
	public static void diff() throws IOException {
		
		String goldFileName = "/home/tm/disk/disk1/pinyin2character/pinyin-gold-linux";
		String predictFileName = "/home/tm/disk/disk1/pinyin2character/pinyin-minwordnum";//pinyin-4j";//pinyin-minwordnum";//pinyin-backmaxmatch";
						
		ArrayList<String> noChinese =  new Char2PyEval().notChinese;

		String encoding = FileEncoding.getCharset(goldFileName);
        BufferedReader goldReader = new BufferedReader(new InputStreamReader(new FileInputStream(goldFileName), encoding));
        
        String encoding2 = FileEncoding.getCharset(predictFileName);
        BufferedReader predictReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictFileName), encoding2));
        
        String goldLine = null;
        String predictLine = null;
        
        int count = 0;
        
        while((goldLine = goldReader.readLine()) != null
        	&& (predictLine = predictReader.readLine()) != null) {
        	
        	count++;
        	
        	String[] goldYins = goldLine.split("\\s+");
        	String[] predictYins = predictLine.split("\\s+");
        	
        	if(goldYins.length != predictYins.length) {
        		System.out.println(goldLine);
        		System.out.println(predictLine);
        	}        	
        	
        	boolean diff = false;
        	for(int i=0; i<goldYins.length; i++) {
        		String goldYin = goldYins[i];
	        		if(!noChinese.contains(goldYin)) {
	        		String predictYin = predictYins[i];
	        		if(!goldYin.equals(predictYin)) {
	        			System.out.println(goldYin + ", " + predictYin);
	        			diff = true;
	        		}
	        	}
        	}
        	if(diff) {
        		System.out.println(goldLine);
        		System.out.println(predictLine);
        	}
        	
        }
        
        goldReader.close();
        predictReader.close();     

	}
	
	
	public static void main(String[] args) throws IOException {
		
		Char2PyEval.diff();		
        
	}
	
	
}
