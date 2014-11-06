package edu.hitsz.ml.maxent.lindekang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


/**
 * The super class of all trainers for Maximum Entropy Models. It is
 * also responsible for converting string form of features and
 * classes into integer ids.
 * 
 * <p> 最大熵的超类， 它把字符串特征转换为数字特征
 * 
 * @author tm
 *
 */
public class MaxEntTrainer extends Str2IdMap{
	ArrayList<String> _classes; //类别
	double _alpha; 				// used as exponential prior，
	double _threshold; 			// stop running GIS if the log likelihood is
	                   			// smaller than this
	double _maxIterations;
	boolean _printDetails;

	MaxEntTrainer() {
		_classes = new ArrayList<String>();
		_alpha = 0.1;
		_threshold = 0;
		_maxIterations = 100;
		_printDetails = false;
	}

	/** 在运行时显示细节 */
	void setPrintDetails(boolean flag) {
		_printDetails = flag;
	}

	public void train(MaxEntModel model, EventSet events){

	}

	/** 获取所有类别 */
	public ArrayList<String> getClasses() {
		return _classes;
	}

	public String getClassName(int c) {
		return _classes.get(c);
	}

	public int getClassId(String c){
		for(int i=0; i<_classes.size(); i++){
			if(c.equals(_classes.get(i)))
				return i;
		}
		return _classes.size();
	}


	/**
	 * Test the classification of the events. Return the error rate.
	 * 
	 * @since Apr 23, 2012
	 * @param events
	 * @param model
	 * @return
	 */
	public double test(EventSet events, MaxEntModel model){
		ArrayList<Double> probs = new ArrayList<Double>();
		double total = 0, error = 0;
		for(int i=0; i<events.size(); i++){
			int c = model.getProbs(events.get(i), probs);
			if(c != events.get(i).getClassId()){
				error++;
				if(_printDetails)
					System.out.println("*");
			}
			if(_printDetails){
				System.out.print(events.get(i).getClassId() + "\t");
				for( int cl=0; cl<probs.size(); cl++)
					System.out.print(getClassName(cl)+" "+probs.get(cl)+"\t");
				System.out.println();
			}
			total++;
		}
		return error/total;
	}

	/**
	 *  Read a set of events from file. Each event occupies a line. The
     *  first token is the class. The rest of the line are the features.
	 *
	 * <p> 从文件中读取训练实例，其中每个实例占一行，每行第一个标示为类别，其他的标示为特征。
	 * 
	 * 
	 * @param istrm
	 * @param events
	 * @throws IOException
	 */
	public void readEvents(String fileName, EventSet events){
		File file = new File(fileName);
		BufferedReader reader = null;
		String delims = " ";
		try{
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null){
				MaxEntEvent event = new MaxEntEvent();
				String[] words = line.split(delims);
				getIds(words, event);
				event.setClassId(getClassId(words[0]));
				event.setCount(1);
				events.add(event);
			}
			reader.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
	}

	/**
	 * 设置参数，包括alpha, threshold, maxIterations, 类别的数目,取代从文件中读取参数
	 * @since Apr 23, 2012
	 * @param alpha
	 * @param threshold
	 * @param maxIterations
	 */
	public void setParams(double alpha, double threshold, int maxIterations) {
		
		this._alpha = alpha;
		this._threshold = threshold;
		this._maxIterations = maxIterations;
		
	}
	
	/**
	 * 从训练文件中获取所有类别
	 * @since Apr 23, 2012
	 * @param fileName
	 */
	public void setClasses(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		String delims = " ";
		HashMap<String, Integer> classes = new HashMap<String, Integer>();
		try{
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null){
				String[] words = line.split(delims);
				if(words.length > 1)
					classes.put(words[0], 1);
			}
			reader.close();
		}
		catch (IOException e){
			System.out.println("IOException: " + e);
		}
		
		Iterator<Entry<String, Integer>> iter = classes.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			this._classes.add(entry.getKey());
		}
		
	}


}
