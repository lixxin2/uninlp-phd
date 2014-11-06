package edu.hitsz.ml.learning;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author tm
 *
 */
public class Instances {
	
	/** features of original samples */
	public String originalFeatures[][];
	/** results of samples */
	public String originalResults[];
	/** features of samples */
	public double features[][];
	/** determine if is a binary classification problem*/
	public boolean isBinaryClassification;
	/** class name for each class */
	public Vector<String> classes;
	/** number of class */
	public int classNumber;
	/** transform to multiple bianry classification 将多类问题转化为多个二类问题*/
	public int results[][];
	
	
	/** number of samples*/
	public int sampleNumber;
	/** Number of original features */
	public int featureNumber;
	/** Number of features after conversion*/
	public int binFeatureNumber;
	/** HashMap to store the conversion between string and numeric */
	public Vector<HashMap<String,Integer>> featureMap;
	/** Numbers of conversed features for each original feature*/
	public Vector<Integer> subFeatureNumber; 
	/** Vector to denote whether the ith feature is numeric */
	public Vector<Boolean> featureNumeral;
	
	
	public void addFeatures(double[][] x){
		sampleNumber = x.length;
		features = x;	
		System.out.println("add features done");
	}
		
	public void addResults(String[] y){
		int yLen = y.length;
		if(yLen != sampleNumber){
			System.out.println("\n Number of results and features is different");
			System.exit(-1);
		}
		originalResults = y;
		//add y to classes
		classes = new Vector<String>();
		for(int i=0; i<yLen; i++)
			if(!classes.contains(y[i]))
				classes.add(y[i]);
		classNumber = classes.size();
		isBinaryClassification = false;
		if(classNumber == 1){
			System.out.println("\n There is only one class in the problem");
			System.exit(-1);
		}
		else if(classNumber == 2){
			isBinaryClassification = true;
			results = new int[1][sampleNumber];
			for(int j=0; j<yLen; j++)
				results[0][j] = classes.indexOf(y[j]);
		}
		else
			results = new int[classNumber][sampleNumber];
		for(int i=0; i<results.length; i++){
			String theClass = classes.get(i);
			for(int j=0; j<yLen; j++){
				if(y[j].equals(theClass))
					results[i][j] = 1;
				else
					results[i][j] = -1;
			}
		}		
		System.out.println("add results done");
	}	
	
	
	/**
	 * Binarize the features, including the original numeric(as string)
	 * 将特征二值化，包括数字特征也看作字符串
	 * @param x
	 */
	public void binarization(String[][] x){
		int xLen = x.length;
		sampleNumber = xLen;
		if(xLen<0 || x[0].length<0){
			System.out.println("\n training data is wrong");
			System.exit(-1);
		}
		int x0Len = x[0].length;
		featureNumber = x0Len;
		originalFeatures = x;
		//build the hashmap for feature
		featureMap = new Vector<HashMap<String,Integer>>();
		featureNumeral = new Vector<Boolean>();
		for(int i=0; i<x0Len; i++){
			featureMap.add(new HashMap<String, Integer>());
		}
		subFeatureNumber = new Vector<Integer>();
		//iterative every feature in each sample
		for(int i=0; i<x0Len; i++){
			int subBinFeaNumber = 0;
			for(int j=0; j<xLen; j++){
				if(!featureMap.get(i).containsKey(x[j][i]))
					featureMap.get(i).put(x[j][i], subBinFeaNumber++);
			}
			featureNumeral.add(false);
			if(i == 0){
				subFeatureNumber.add(0);
				subFeatureNumber.add(subBinFeaNumber);
			}
			else
				subFeatureNumber.add(subFeatureNumber.get(i)+subBinFeaNumber);			
		}
		binFeatureNumber = subFeatureNumber.get(x0Len);
		features = new double[xLen][binFeatureNumber];
		//binary the features
		for(int i=0; i<xLen; i++){
			for(int j=0; j<x0Len; j++){
				int m = subFeatureNumber.get(j);
				if(featureMap.get(j).containsKey(x[i][j])){
					int n = featureMap.get(j).get(x[i][j]);					
					features[i][m+n] = 1;
				}
			}
		}	
		System.out.println("binary features done");
	}

	

	
	/**
	 * Binarize the features, excluding the original numeric features
	 * 将特征二值化，原来的数字特征仍看做数字
	 * @param x
	 */
	public void binarizationWithoutNumeric(String[][] x){
		int xLen = x.length;
		sampleNumber = xLen;
		if(xLen<0 || x[0].length<0){
			System.out.println("\n training data is wrong");
			System.exit(-1);
		}
		int x0Len = x[0].length;
		featureNumber = x0Len;
		originalFeatures = x;
		//build the hashmap for feature
		featureMap = new Vector<HashMap<String,Integer>>();
		featureNumeral = new Vector<Boolean>();
		for(int i=0; i<x0Len; i++){
			featureMap.add(new HashMap<String, Integer>());
		}
		subFeatureNumber = new Vector<Integer>();
		//iterative every feature in each sample
		for(int i=0; i<x0Len; i++){
			boolean isNumeral = true;
			int subBinFeaNumber = 0;
			for(int j=0; j<xLen; j++){
				if(!featureMap.get(i).containsKey(x[j][i]))
					featureMap.get(i).put(x[j][i], subBinFeaNumber++);
				if(isNumeral == true && !isInteger(x[j][i]) && !isDouble(x[j][i]))
					isNumeral = false;
			}
			featureNumeral.add(isNumeral);
			if(isNumeral){				
				subBinFeaNumber = 1;
				featureMap.get(i).clear();
			}			
			if(i == 0){
				subFeatureNumber.add(0);
				subFeatureNumber.add(subBinFeaNumber);
			}
			else
				subFeatureNumber.add(subFeatureNumber.get(i)+subBinFeaNumber);			
		}
		binFeatureNumber = subFeatureNumber.get(x0Len);
		features = new double[xLen][binFeatureNumber];
		//binary the features
		for(int i=0; i<xLen; i++){
			for(int j=0; j<x0Len; j++){
				int m = subFeatureNumber.get(j);
				if(featureNumeral.get(j))
					features[i][m++] = Double.parseDouble(x[i][j]);
				else if(featureMap.get(j).containsKey(x[i][j])){
					int n = featureMap.get(j).get(x[i][j]);					
					features[i][m+n] = 1;
				}
			}
		}
		System.out.println("binary features done");
	}
	

	
	public double[] getInstance(String[] x){
		int xLength = x.length;
		if(xLength != featureNumber){
			System.out.println("Number of features inputting is different from the original one");
		}
		double[] fea = new double[binFeatureNumber];
		for(int i=0; i<xLength; i++){
			int m = subFeatureNumber.get(i);
			if(featureNumeral.get(i))
				fea[m++] = Double.parseDouble(x[i]);
			else if(featureMap.get(i).containsKey(x[i])){
				int n = featureMap.get(i).get(x[i]);					
				fea[m+n] = 1;
			}
		}
		return fea;
	}
	
	public double[][] getInstances(String[][] x){
		int xLength = x.length;
		int x0Length = x[0].length;
		if(x0Length != featureNumber){
			System.out.println("Number of features inputting is different from the original one");
		}
		double[][] fea = new double[xLength][binFeatureNumber];
		for(int i=0; i<xLength; i++){
			for(int j=0; j<x0Length; j++){
				int m = subFeatureNumber.get(j);
				if(featureNumeral.get(j))
					fea[i][m++] = Double.parseDouble(x[i][j]);
				else if(featureMap.get(j).containsKey(x[i][j])){
					int n = featureMap.get(j).get(x[i][j]);					
					fea[i][m+n] = 1;
				}
			}			
		}
		return fea;
	}
	
	
	
	
	public boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches(); 
	}
	
	public boolean isDouble(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
		return pattern.matcher(str).matches();
	} 
	

	public void store(String outfileName){
		try{
			FileWriter outWriter = new FileWriter(outfileName);
			if(features.length>0){
				int xLen = features.length;				
				for(int i=0; i<xLen; i++){
					int x0Len = features[i].length;
					StringBuffer newString= new StringBuffer();
					for(int j=0; j<x0Len; j++)
						newString.append(j+1+":"+features[i][j]+"\t");
					outWriter.write(newString.toString().trim()+"\n");
				}
			}
			outWriter.close();
		}
		
		catch (IOException e){
			System.out.println("IOException" + e);
		}
	}
	
	
	public void storeClear(String outfileName){
		try{
			FileWriter outWriter = new FileWriter(outfileName);
			if(features.length>0){
				int xLen = features.length;				
				for(int i=0; i<xLen; i++){
					int x0Len = features[i].length;
					StringBuffer newString= new StringBuffer();
					for(int j=0; j<x0Len; j++)
						if(Math.abs(features[i][j])>1e-5)
							newString.append(j+1+":"+features[i][j]+"\t");
					outWriter.write(newString.toString().trim()+"\n");
				}
			}
			outWriter.close();
		}
		
		catch (IOException e){
			System.out.println("IOException" + e);
		}
	}
	
	private static void test(){
		String[][] x = {{"a","1","x","1"},{"a","2","y","0.2"},{"b","3","z","0.5"}};
		Instances newInst = new Instances();
		newInst.binarization(x);
		String[] x1= {"a","1","x","1"};
		double[][] fea = newInst.getInstances(x);
		newInst.storeClear("/media/4083BE7D790F6BE0/ocr/x.train");
		String[] y={"a","b","c"};
		newInst.addResults(y);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
