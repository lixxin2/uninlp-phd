/**
 *
 */
package edu.hitsz.ml.onlinelearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;






/**
 * Averaged Perceptron algorithm (Collins, 2001), an example in Chunking class
 * 最好不直接调用，因为寻找最佳路径的策略不一定相同，不一定是Viterbi,可能是dynamic programming.
 * 可以在实际应用类中重写的函数包括: train, viterbi, viterbiAp, kBeam, predict
 * @author tm
 * @deprecated
 */
public class AveragedPerceptron {
	//
	public class Weights{
		public double singleWeights;           //
		public double allWeights;              //
		public int start;
	}

	public int iterNum=5;
	public double errorRate=1e-5;
	public double step=0.1;
	public HashMap<String, Weights> allFeatures;
	public int initialWeight=0;
	public int dataNum=1;


	public AveragedPerceptron(){
		allFeatures=new HashMap<String, Weights>();
	}

	public void setIter(int inputIter){
		iterNum=inputIter;
	}

	public void setError(int inputError){
		errorRate=inputError;
	}


	/**
	 * update weights according senFeature， 根据输入特征值更新权值
	 * @param senFeature 已经处理好的一句话中的特征和权值
	 * @param sentenceNum
	 * @param ith
	 */
	public void update(HashMap<String, Integer> senFeature, int sentenceNum, int ith){
		Iterator<Map.Entry<String, Integer>> iter = senFeature.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String, Integer> entry = iter.next();
		    String feature = (String)entry.getKey();
		    int num = (Integer)entry.getValue();
		    //
		    if(allFeatures.containsKey(feature)){
				Weights newWeights=allFeatures.get(feature);
				double tmpWeight=newWeights.singleWeights;
				newWeights.singleWeights=tmpWeight+num;
				newWeights.allWeights=newWeights.allWeights+(ith-newWeights.start)*tmpWeight+num;
				newWeights.start=ith;
				allFeatures.put(feature, newWeights);
			}
		    else{
		    	Weights newWeights=new Weights();
				newWeights.singleWeights=num;
				newWeights.allWeights=num;
				newWeights.start=ith;
				allFeatures.put(feature,newWeights);
		    }
		}
		if(ith+1==sentenceNum){
			Iterator<Map.Entry<String, Weights>> allIter = allFeatures.entrySet().iterator();
			while (allIter.hasNext()) {
			    Map.Entry<String, Weights> entry = allIter.next();
			    String feature = (String) entry.getKey();
			    Weights newWeights = (Weights) entry.getValue();
			    double tmpWeight=newWeights.singleWeights;
			    newWeights.allWeights=newWeights.allWeights+(ith-newWeights.start)*tmpWeight;
			    newWeights.start=-1;
			    allFeatures.put(feature, newWeights);
			}
		}
	}


	/**
	 * 更新特征的权重
	 * @since Dec 12, 2011
	 * @param senFeatures 一个句子中的所有特征
	 * @param senWeights 一个句子中的所有特征对应的权重
	 * @param sentenceNum
	 * @param ith
	 */
	public void update(ArrayList<String> senFeatures, ArrayList<Integer> senWeights, int sentenceNum, int ith){
		for(int i=0; i<senFeatures.size(); i++){
			String feature = senFeatures.get(i);
			int weight = senWeights.get(i);
		    if(allFeatures.containsKey(feature)){
				Weights newWeights=allFeatures.get(feature);
				double tmpWeight=newWeights.singleWeights;
				newWeights.singleWeights=tmpWeight+weight;
				newWeights.allWeights=newWeights.allWeights+(ith-newWeights.start)*tmpWeight+weight;
				newWeights.start=ith;
				allFeatures.put(feature, newWeights);
			}
		    else{
		    	Weights newWeights=new Weights();
				newWeights.singleWeights=1;
				newWeights.allWeights=1;
				newWeights.start=ith;
				allFeatures.put(feature,newWeights);
		    }
		}
		if(ith+1==sentenceNum){
			Iterator<Map.Entry<String, Weights>> iter = allFeatures.entrySet().iterator();
			while (iter.hasNext()) {
			    Map.Entry<String, Weights> entry = iter.next();
			    String feature = (String) entry.getKey();
			    Weights newWeights = (Weights) entry.getValue();
			    double tmpWeight=newWeights.singleWeights;
			    newWeights.allWeights=newWeights.allWeights+(ith-newWeights.start)*tmpWeight;
			    newWeights.start=-1;
			    allFeatures.put(feature, newWeights);
			}
		}
	}


	public void readObjects(String fileName){
		try{
			FileInputStream fis = new FileInputStream(fileName);
		    ObjectInputStream iis = new ObjectInputStream(fis);
			allFeatures = (HashMap<String, Weights>) iis.readObject();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}


	public void storeWeights(String fileName){
		try{
			FileOutputStream fos = new FileOutputStream(fileName);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    oos.writeObject(allFeatures);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}



	/**
	 * read weights( single weights and all weights) from file
	 * @param fileName
	 */
	public void readWeights(String fileName){
		allFeatures.clear();
		File file = new File(fileName);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("\nOpening Weight file ...");
			String tempString=null;
			try{
				while ((tempString = reader.readLine())!= null){
					if (!(tempString.trim().equals(""))&&(tempString!=null)){
						//System.out.println(tempString.trim());
						String[] singleString=tempString.split("\t");
						Weights newWeight=new Weights();
						newWeight.singleWeights=Double.parseDouble(singleString[1]);
						newWeight.allWeights=Double.parseDouble(singleString[2]);
						allFeatures.put(singleString[0],newWeight);
					}
				}
				System.out.println("There are totally "+allFeatures.size()+" features.");
				System.out.println("Read Weight file successed.");
				try {
					reader.close();
					}
				catch (IOException e) {
					e.printStackTrace();
					}
				}
			catch (FileNotFoundException e) {
				System.err.println(e);
				}
			}
		catch (IOException e){
			System.out.println("IOException: " + e);
			}
	}

	/**
	 * store weights( single weights and all weights) into file
	 * @param fileName
	 * @param iterNum
	 * @param sentenceNum
	 */
	public void storeWeights(String fileName, int iterNum, int sentenceNum){
		try{
			FileWriter outFileWriter=new FileWriter(fileName);
			Iterator<Map.Entry<String, Weights>> iter = allFeatures.entrySet().iterator();
			while (iter.hasNext()) {
			    Map.Entry<String, Weights> entry = iter.next();
			    String feature = (String) entry.getKey();
			    Weights newWeights = (Weights) entry.getValue();
			    outFileWriter.write(feature+"\t"+newWeights.singleWeights+"\t"+newWeights.allWeights/(iterNum*sentenceNum)+"\n");
			}
			outFileWriter.close();
			System.out.println("\nStore "+Integer.toString(iterNum)+"th feature file done!");
		}catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * 根据输入特征，和候选标识，输入权重值
	 * @param oneFeature
	 * @param tag
	 * @return
	 */
	public double compSingleWeights(ArrayList<String> oneFeature, String tag){
		double tmpWeight=0;
		for(int o=0;o<oneFeature.size();o++){
			//add every feature with its corresponding tag
			String one=oneFeature.get(o)+"+"+tag;
			if(allFeatures.containsKey(one)){
				tmpWeight+=allFeatures.get(one).singleWeights;
			}
		}
		return tmpWeight;
	}

	public double compSingleWeights(ArrayList<String> oneFeature){
		double tmpWeight=0;
		for(int o=0;o<oneFeature.size();o++){
			//add every feature with its corresponding tag
			String one=oneFeature.get(o);
			if(allFeatures.containsKey(one)){
				tmpWeight+=allFeatures.get(one).singleWeights;
			}
		}
		return tmpWeight;
	}


	public double compAllWeights(ArrayList<String> oneFeature, String tag){
		double tmpWeight=0;
		for(int o=0;o<oneFeature.size();o++){
			//add every feature with its corresponding tag
			String one=oneFeature.get(o)+"+"+tag;
			if(allFeatures.containsKey(one)){
				tmpWeight+=allFeatures.get(one).allWeights;
			}
		}
		return tmpWeight;
	}

	public double compAllWeights(ArrayList<String> oneFeature){
		double tmpWeight=0;
		for(int o=0;o<oneFeature.size();o++){
			//add every feature with its corresponding tag
			String one=oneFeature.get(o);
			if(allFeatures.containsKey(one)){
				tmpWeight+=allFeatures.get(one).allWeights;
			}
		}
		return tmpWeight;
	}




	public static void main(String[] args){
		//oneExample(args);
		float a = (float) 1.5e-15;
		System.out.println(a);
	}
}