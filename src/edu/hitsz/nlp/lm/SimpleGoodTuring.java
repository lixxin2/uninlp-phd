package edu.hitsz.nlp.lm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.hitsz.java.file.local.FileEncoding;

public class SimpleGoodTuring {
	

	public int TRUE = 1;
	public int FALSE = 0;
	public int MAX_ROWS = 200;
	public int MIN_INPUT = 5;
	public double CONFID_FACTOR = 1.96;
	 
	public int[] r, n; //frequency, frequency of frequency
	public double[] Z, log_r, log_Z, rStar, p; //
	
	int rows, bigN;
	double PZero, bigNprime, slope, intercept; //P0, N', a, b
		
	
	public SimpleGoodTuring( ) {
		
		r = new int[MAX_ROWS]; 
		n = new int[MAX_ROWS];
		Z = new double[MAX_ROWS];
		log_r = new double[MAX_ROWS]; 
		log_Z = new double[MAX_ROWS]; 		                
		rStar = new double[MAX_ROWS]; 
		p = new double[MAX_ROWS];
	}
	
	
	public void init(int MAX_ROWS) {
		
		if(MAX_ROWS < MIN_INPUT) {
			System.out.println("MAX_ROWS < MIN_INPUT");
			System.exit(-1);
		}
		this.MAX_ROWS = MAX_ROWS;
		r = new int[MAX_ROWS]; 
		n = new int[MAX_ROWS];
		Z = new double[MAX_ROWS];
		log_r = new double[MAX_ROWS]; 
		log_Z = new double[MAX_ROWS]; 		                
		rStar = new double[MAX_ROWS]; 
		p = new double[MAX_ROWS];
	}
	

	/**
	 * 处理原始文本语料，得到每个词的频率 及 频率的频率
	 * @since Sep 15, 2013
	 * @param rawTextFile
	 * @param freqOfFreqFile
	 * @throws IOException
	 */
	public static void readRawTextFile(String rawTextFile, String freqOfFreqFile) throws IOException {
		
		String fileEncoding = FileEncoding.getCharset(rawTextFile);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rawTextFile), fileEncoding));
        
	    //读取文件，统计所有词的频率，到HashMap
	    HashMap<String, Integer> map = new HashMap<String, Integer>();
	    String line = null;
	    while ((line = reader.readLine()) != null)
        {
	    	line = line.trim();
	    	if(line.length() > 0) {
	    		String[] words = line.split("\\s+");
	    		for(String word : words) {
	    			if(!map.containsKey(word))
	    				map.put(word, 1);
	    			else {
	    				map.put(word, map.get(word)+1);
	    			}
	    		}	    		
	    	}        
        }	    
	    reader.close();
	    System.out.println("total " + map.size() + " words in file " + rawTextFile);
	    
	    //统计频率的频率，到TreeMap
	    TreeMap<Integer, Integer> numbMap = new TreeMap<Integer, Integer>();
	    Iterator<Entry<String, Integer>> iter = map.entrySet().iterator();
	    while(iter.hasNext()) {
	    	Entry<String, Integer> entry = iter.next();
	    	int numb = entry.getValue();
	    	if(!numbMap.containsKey(numb))
	    		numbMap.put(numb, 1);
	    	else 
	    		numbMap.put(numb, numbMap.get(numb)+1);
	    }
	    System.out.println();
	    
	    //
	    FileWriter writer = new FileWriter(freqOfFreqFile);	 
	    writer.write(numbMap.size() + "\n");
	    Iterator<Entry<Integer, Integer>> numbIter = numbMap.entrySet().iterator();
	    while( numbIter.hasNext()) {
	    	Entry<Integer, Integer> entry = numbIter.next();
	    	int freq = entry.getKey();
	    	int freqOfFreq = entry.getValue();
	    	writer.write(freq + "\t" + freqOfFreq + "\n");	    	
	    }
	    writer.close();
	    
	}
	
	
	
    /*
     *      returns number of rows if input file is valid, else -1
     *      NB:  number of rows is one more than index of last row
     *
     */
	public void readFreqFile(String freqOfFreqName) throws IOException {
		
	    int rowNumber = 0;	
	    String fileEncoding = FileEncoding.getCharset(freqOfFreqName);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(freqOfFreqName), fileEncoding));
        
	    String line = null;
	    if((line = reader.readLine()) != null) {
	    	rows = Integer.parseInt(line.trim());
	    	init(rows);
	    }
	    while ((line = reader.readLine()) != null && rowNumber < MAX_ROWS)
        {
	    	line = line.trim();
	    	if(line.length() > 0) {
	    		String[] parts = line.split("\\s+");
	    		if(parts.length != 2) {
	    			System.out.println("");
	    			System.exit(-1);
	    		}
	    		int freq = Integer.parseInt(parts[0]);
	    		int freqOfFreq = Integer.parseInt(parts[1]);
	    		r[rowNumber] = freq;
	    		n[rowNumber] = freqOfFreq;	    		
	    	}
	        rowNumber++;
        }
	    reader.close();
    }
     
	
	public double sq(double x) {
		return(x * x);
    }
	
	/**
	 * 找到最佳拟合曲线, simple linear regression
	 * @since Sep 18, 2013
	 */
	public void findBestFit( ) {
		 double XYs, Xsquares, meanX, meanY;
	     int i;
	     
	     XYs = Xsquares = meanX = meanY = 0.0;
	     for (i = 0; i < rows; ++i)
         {
	         meanX += log_r[i];
	         meanY += log_Z[i];
         }
	     meanX /= rows;
	     meanY /= rows;
	     
	     for (i = 0; i < rows; ++i)
         {
	         XYs += (log_r[i] - meanX) * (log_Z[i] - meanY); //(X-xi)(Y-yi)
	         Xsquares += sq(log_r[i] - meanX);               //(X-xi)^2
         }
	     slope = XYs / Xsquares;
	     intercept = meanY - slope * meanX;
     }
     
	 public double smoothed(int i)
     {
		 return(Math.exp(intercept + slope * Math.log(i)));
     }
     
	 /**
	  * 找到频率出现次数等于i 位于的行数
	  * @since Sep 15, 2013
	  * @param i
	  * @return
	  */
	 public int row(int i)
     {
	     int j = 0;
	     
	     while (j < rows && r[j] < i)
	             ++j;
	     return((j < rows && r[j] == i) ? j : -1);
     }
     
	 public void showEstimates()
     {
	     int i;	     
	     System.out.println("0\t" + PZero +"\n");
	     for (i = 0; i < rows; ++i)
	    	 System.out.println(r[i] + "\t" + p[i] + "\n");
     }
     
	 
	 public void analyseInput( ) {
		 
	     int i, j, next_n;
	     double k, x, y;
	     boolean indiffValsSeen = false;
	     
	     bigN = 0;
	     for (j = 0; j < rows; ++j)
	             bigN += r[j] * n[j];
	     
	     next_n = row(1);  //usually equals to 0
	     
	     //P0 = n1/N;
	     PZero = (next_n < 0) ? 0 : n[next_n] / (double) bigN;
	     
	     //Zj
	     for (j = 0; j < rows; ++j)
         {
		     //i and k be the values in the r column for the immediately following rows respectively
	         i = (j == 0 ? 0 : r[j - 1]);
	         if (j == rows - 1)
                 k = (double) (2 * r[j] - i);
	         else
                 k = (double) r[j + 1];
	         
	         Z[j] = 2 * n[j] / (k - i);
	         log_r[j] = Math.log(r[j]);
	         log_Z[j] = Math.log(Z[j]);
         }
	     
	     findBestFit();
	     
	     //find the r* that uses two E(nr) (nr and S(r)) is significantly different
	     for (j = 0; j < rows; ++j)
         {
	         y = (r[j] + 1) * smoothed(r[j] + 1) / smoothed(r[j]);
	         if (row(r[j] + 1) < 0)
                 indiffValsSeen = true;
	         if (! indiffValsSeen)
             {
	             x = (r[j] + 1) * (next_n = n[row(r[j] + 1)]) / 
	                             (double) n[j];
	             if (Math.abs(x - y) <= CONFID_FACTOR * Math.sqrt(sq(r[j] + 1.0)
	                             * next_n / (sq((double) n[j]))
	                             * (1 + next_n / (double) n[j])))
                     indiffValsSeen = true;
	             else
                     rStar[j] = x;
             }
	         if (indiffValsSeen)
	                 rStar[j] = y;
         }
	     
	     //renormalized
	     bigNprime = 0.0;
	     for (j = 0; j < rows; ++j)
	             bigNprime += n[j] * rStar[j];
	     for (j = 0; j < rows; ++j)
	             p[j] = (1 - PZero) * rStar[j] / bigNprime;
	     
	     showEstimates();
     }
	
	
	public static void main(String[] args) throws IOException {
		
		//String rawTextFile = "/home/tm/disk/disk1/2007.ictclas.seg";
		String freqOfFreqFile = "/home/tm/disk/disk1/2007.fof";
		//GoodTuringEstimate.readRawTextFile(rawTextFile, freqOfFreqFile);
		
		SimpleGoodTuring est = new SimpleGoodTuring();
		est.readFreqFile(freqOfFreqFile);
		est.analyseInput();
		
	}

}
