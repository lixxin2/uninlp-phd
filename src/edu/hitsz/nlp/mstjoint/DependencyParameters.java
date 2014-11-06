package edu.hitsz.nlp.mstjoint;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 将特征用两个结构表示，一个hashmap将特征替换为数字，另一个，用
 * @author tm
 *
 */
public class DependencyParameters implements Serializable{
	/**
	 *
	 */
	public int iterNum=5;
	public double errorRate=1e-5;
	public double step=0.1;
	private double[] singleWeights;           //
	private double[] allWeights;              //
	private int[] starts;
	private int weightLength;


	public DependencyParameters(int max){
		weightLength = max;
		singleWeights = new double[max];
		allWeights = new double[max];
		starts = new int[max];
		ensureCapacity(max-1);
		initAll(0, max-1);
	}

	public DependencyParameters(){
		this(10);
	}

	public void initAll(int start, int end){
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
	public void update(HashMap<Integer, Integer> senFeature, int sentenceNum, int ith){
		Iterator<Map.Entry<Integer, Integer>> iter = senFeature.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<Integer, Integer> entry = iter.next();
		    int fea = (Integer) entry.getKey();
		    int num = (Integer)entry.getValue();
		    updateOne(fea, num, ith);
		}
		if(ith + 1 == sentenceNum){
			updateAll(ith);
		}
	}


	/**
	 * update weights according 根据输入特征值更新权值
	 *
	 * @since Jan 12, 2012
	 * @param addFeatures 正确的特征
	 * @param delFeatures 错误的特征
	 * @param sentenceNum
	 * @param ith
	 */
	public void update(int[] addFeatures, int[] delFeatures, int sentenceNum, int ith){
		for(int fea : addFeatures){
			updateOne(fea, 1, ith);
		}
		for(int fea : addFeatures){
			updateOne(fea, -1, ith);
		}
		if(ith + 1 == sentenceNum){
			updateAll(ith);
		}
	}

	/**
	 * update weights according 根据输入特征值更新权值
	 * @since 2012-3-1
	 * @param addFeatures 正确的特征
	 * @param delFeatures 错误的特征
	 * @param sentenceNum
	 * @param ith
	 */
	public void update(ArrayList<Integer> addFeatures, ArrayList<Integer> delFeatures, int sentenceNum, int ith){
		for(int fea : addFeatures){
			updateOne(fea, 1, ith);
		}
		for(int fea : delFeatures){
			updateOne(fea, -1, ith);
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
	/*
	public void update(ArrayList<Integer> senFeatures, ArrayList<Integer> senWeights, int sentenceNum, int ith){
		for(int i=0; i<senFeatures.size(); i++){
			int feature = senFeatures.get(i);
			int weight = senWeights.get(i);
		    updateOne(feature, weight, ith);
		}
		if(ith + 1 == sentenceNum){
			updateAll(ith);
		}
	}
	*/


	public void readObjects(String fileName){
		try{
			System.out.println("Reading the parameters of averaged perceptron algorithm...");
			FileInputStream fis = new FileInputStream(fileName);
		    ObjectInputStream iis = new ObjectInputStream(fis);
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
			averagedAll(iterNum, sentenceNum);
			System.out.println("Storing the parameters of averaged perceptron algorithm...");
			FileOutputStream fos = new FileOutputStream(fileName);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
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
	 * 根据输入特征
	 * @since Jan 10, 2012
	 * @param oneFeature
	 * @return
	 */
	public double compSingleWeights(ArrayList<Integer> oneFeature){
		double tmpWeight=0;
		for(Integer feature : oneFeature){
			//add every feature with its corresponding tag
			tmpWeight+=getSingleWeight(feature);
		}
		return tmpWeight;
	}

	public double compAllWeights(ArrayList<Integer> oneFeature){
		double tmpWeight=0;
		for(Integer feature : oneFeature){
			//add every feature with its corresponding tag
			tmpWeight+=getAveragedWeight(feature);
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
	 * 检查并扩充数组,判断是否包含minCapacity+1个特征
	 * @since Dec 12, 2011
	 * @param minCapacity
	 */
	public void ensureCapacity(int minCapacity) {
		if(minCapacity >= weightLength) {
			int oldCapacity = weightLength;
            double[] oldSingleWeights = singleWeights;
            double[] oldAllWeights = allWeights;
            int[] oldStart = starts;
            weightLength = Math.max(oldCapacity * 2, minCapacity+1);
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
			allWeights[i] = allWeights[i]+(ith-starts[i])*singleWeights[i];
			starts[i] = -1;
		}
	}

	public void averagedAll(int iterNum, int sentenceNum){
		for(int i =0; i<starts.length; i++){
			allWeights[i] *= 1.0/(iterNum*sentenceNum);
		}
		singleWeights = allWeights;
	}

}
