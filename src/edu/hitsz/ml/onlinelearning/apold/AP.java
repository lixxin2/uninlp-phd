package edu.hitsz.ml.onlinelearning.apold;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



/**
 * AveragedPerceptron 算法
 * 将特征用两个结构表示，一个hashmap将特征替换为数字，另一个，用
 * @author tm
 *
 */
public class AP{
	/**
	 *
	 */
	public int iterNum=5;
	public double errorRate=1e-5;
	public double step=0.1;
	public FeatureMap allFeatures;
	private double[] singleWeights;           //
	private double[] allWeights;              //
	private int[] starts;
	private int weightLength;


	public AP(int max){
		allFeatures=new FeatureMap();
		weightLength = max;
		singleWeights = new double[max];
		allWeights = new double[max];
		starts = new int[max];
		initAll(0, max);
	}

	public AP(){
		this(1000000);
	}

	public void initAll(int start, int end){
		ensureCapacity(end-1);
		for(int i=start; i<end; i++){
			singleWeights[i] = 0.0;
			allWeights[i] = 0.0;
			starts[i] = -1;
		}
	}

	public void setIter(int inputIter){
		iterNum=inputIter;
	}

	public void setError(int inputError){
		errorRate=inputError;
	}


	/**
	 * update weights according senFeature， 根据输入特征值更新权值
	 * @param senFeature
	 * @param sentenceNum 所有句子树
	 * @param ith 当前句子树
	 */
	public void update(HashMap<String, Integer> senFeature, int sentenceNum, int ith){
		Iterator<Map.Entry<String, Integer>> iter = senFeature.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String, Integer> entry = iter.next();
		    String stringFeature = (String) entry.getKey();
		    int num = (Integer)entry.getValue();
		    int intFeature = allFeatures.add(stringFeature, true);
		    updateOne(intFeature, num, ith);
		}
		if(ith + 1 == sentenceNum){
			updateAll(ith);
		}
	}


	/**
	 * 根据输入特征值更新权值
	 * @since Dec 12, 2011
	 * @param senFeature
	 * @param sentenceNum
	 * @param ith
	 */
	public void update(ArrayList<String> senFeatures, ArrayList<Integer> senWeights, int sentenceNum, int ith){
		for(int i=0; i<senFeatures.size(); i++){
			String feature = senFeatures.get(i);
			int weight = senWeights.get(i);
		    int intFeature = allFeatures.add(feature, true);
		    updateOne(intFeature, weight, ith);
		}
		if(ith + 1 == sentenceNum){
			updateAll(ith);
		}
	}


	public void readObjects(String fileName){
		try{
			System.out.println("Reading the parameters of averaged perceptron algorithm...");
			FileInputStream fis = new FileInputStream(fileName);
		    ObjectInputStream iis = new ObjectInputStream(fis);
		    allFeatures.readObject(iis);
			singleWeights =  (double[])iis.readObject();
			allWeights = (double[])iis.readObject();
			starts = (int[])iis.readObject();
			weightLength = (Integer)iis.readObject();
			System.out.println("done");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}


	public void storeObjects(String fileName, int iterNum, int sentenceNum){
		try{
			AverageAll(iterNum, sentenceNum);
			System.out.println("Storing the parameters of averaged perceptron algorithm...");
			FileOutputStream fos = new FileOutputStream(fileName);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    allFeatures.writeObject(oos);
		    oos.writeObject(singleWeights);
		    oos.writeObject(allWeights);
		    oos.writeObject(starts);
		    oos.writeObject(weightLength);
		    System.out.println("done");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}


	/**
	 * read weights( single weights and all weights) from file
	 * @param fileName
	 */
	public void readWeights(String fileName){
		File file = new File(fileName);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
			System.out.println("\nOpening Weight file ...");
			String tempString=null;
			try{
				weightLength = Integer.parseInt(reader.readLine().trim());
				singleWeights = new double[weightLength];
				allWeights = new double[weightLength];
				while ((tempString = reader.readLine())!= null){
					if (!(tempString.trim().equals(""))&&(tempString!=null)){
						//System.out.println(tempString.trim());
						String[] singleString=tempString.split("\t");
						String feature = singleString[0];
						int featureSeq = Integer.parseInt(singleString[1]);
						allFeatures.add(feature, featureSeq);
						double singleWeight=Double.parseDouble(singleString[2]);
						double allWeight=Double.parseDouble(singleString[3]);
						singleWeights[featureSeq] = singleWeight;
						allWeights[featureSeq] = allWeight;
					}
				}
				System.out.println("There are totally "+weightLength+" features.");
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




	public void storeWeights(String fileName, int iterNum, int sentenceNum){
		try{
			FileWriter outFileWriter=new FileWriter(fileName);
			outFileWriter.write(weightLength+"\n");
			Iterator<Map.Entry<String, Integer>> iter = allFeatures.getMap().entrySet().iterator();
			while (iter.hasNext()) {
			    Map.Entry<String, Integer> entry = iter.next();
			    String feature = entry.getKey();
			    int featureSeq = entry.getValue();
			    outFileWriter.write(feature+"\t"+featureSeq+"\t"
			    		+singleWeights[featureSeq]+"\t"+allWeights[featureSeq]/(iterNum*sentenceNum)+"\n");
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
		for(String feature : oneFeature){
			//add every feature with its corresponding tag
			int intFeature = allFeatures.get(feature+"+"+tag);
			tmpWeight+=getSingleWeight(intFeature);
		}
		return tmpWeight;
	}

	public double compSingleWeights(ArrayList<String> oneFeature, boolean bTrain){
		double tmpWeight=0;
		for(String feature : oneFeature){
			//add every feature with its corresponding tag
			int intFeature = allFeatures.add(feature, bTrain);
			tmpWeight+=getSingleWeight(intFeature);
		}
		return tmpWeight;
	}

	public double compAllWeights(ArrayList<String> oneFeature, String tag, boolean bTrain){
		double tmpWeight=0;
		for(String feature : oneFeature){
			//add every feature with its corresponding tag
			int intFeature = allFeatures.add(feature+"+"+tag, bTrain);
			tmpWeight+=getAveragedWeight(intFeature);
		}
		return tmpWeight;
	}

	public double compAllWeights(ArrayList<String> oneFeature, boolean bTrain){
		double tmpWeight=0;
		for(String feature : oneFeature){
			//add every feature with its corresponding tag
			int intFeature = allFeatures.add(feature, bTrain);
			tmpWeight+=getAveragedWeight(intFeature);
		}
		return tmpWeight;
	}


	public double getAveragedWeight(int i){
		if(i == -1)
			return 0.0;
		ensureCapacity(i);
		return allWeights[i];
	}

	public double getSingleWeight(int i){
		if(i == -1)
			return 0.0;
		ensureCapacity(i);
		return singleWeights[i];
	}

	/**
	 * 检查并扩充数组
	 * @since Dec 12, 2011
	 * @param minCapacity
	 */
	public void ensureCapacity(int minCapacity) {
		if(minCapacity >= weightLength) {
			int oldCapacity = weightLength;
            double[] oldSingleWeights = singleWeights;
            double[] oldAllWeights = allWeights;
            int[] oldStart = starts;
            weightLength = (oldCapacity * 3)/2 + 1;
            if (weightLength < minCapacity)
                weightLength = minCapacity;
            singleWeights = new double[weightLength];
            allWeights = new double[weightLength];
            starts = new int[weightLength];
            System.arraycopy(oldSingleWeights, 0, singleWeights, 0, oldCapacity);
            System.arraycopy(oldAllWeights, 0, allWeights, 0, oldCapacity);
            System.arraycopy(oldStart, 0, starts, 0, oldCapacity);
            initAll(oldCapacity, weightLength);
        }
	}

	/**
	 * 第ith句话中，第i个特征的变化量为num，更新该特征
	 * @param i 第i个特征
	 * @param num 第i个特征的更新值
	 * @param ith 第ith个句子实例
	 */
	public void updateOne(int i, int num, int ith){
		ensureCapacity(i);
		double tmpWeight = singleWeights[i];
		singleWeights[i] = tmpWeight+num;
		allWeights[i] = allWeights[i]+(ith-starts[i])*tmpWeight+num;
		starts[i] = ith;
	}


	public void updateAll(int ith){
		for(int i =0; i<starts.length; i++){
			Double tmpWeight = singleWeights[i];
			allWeights[i] = allWeights[i]+(ith-starts[i])*tmpWeight;
			starts[i] = -1;
		}
	}

	public void AverageAll(int iterNum, int sentenceNum){
		for(int i =0; i<starts.length; i++){
			allWeights[i] = allWeights[i]/(iterNum*sentenceNum);
		}
	}

}
